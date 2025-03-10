// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea textArea = new JTextArea();
  private final JButton runButton = new JButton("Load");
  // private transient SwingWorker<String, Chunk> worker;
  private transient ProgressMonitor monitor;

  private MainPanel() {
    super(new BorderLayout(5, 5));
    textArea.setEditable(false);
    runButton.addActionListener(this::executeWorker);
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(runButton);
    add(new JScrollPane(textArea));
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public void executeWorker(ActionEvent e) {
    JButton b = (JButton) e.getSource();
    b.setEnabled(false);
    textArea.setText("");

    // Random random = new Random();
    // Charset cs = Charset.forName("EUC-JP");
    int index = 19; // 1 + random.nextInt(27-1);
    String path = String.format("https://docs.oracle.com/javase/8/docs/api/index-files/index-%d.html", index);
    // String path = String.format("https://docs.oracle.com/javase/7/docs/api/index-files/index-%d.html", index);
    // String path = String.format("https://docs.oracle.com/javase/jp/6/api/index-files/index-%d.html", index);
    // String path = "https://ateraimemo.com/";
    append(path);

    URLConnection urlConnection;
    try {
      // URLConnection urlConnection = getUrlConnection(path);
      urlConnection = new URI(path).toURL().openConnection();
    } catch (URISyntaxException | IOException ex) {
      // ex.printStackTrace();
      textArea.setText("error: " + ex.getMessage());
      return;
    }
    append("urlConnection.getContentEncoding(): " + urlConnection.getContentEncoding());
    append("urlConnection.getContentType(): " + urlConnection.getContentType());

    Charset cs = getCharset(urlConnection);
    int length = urlConnection.getContentLength();
    SecondaryLoop loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
    JRootPane rp = b.getRootPane();
    try (InputStream is = urlConnection.getInputStream();
         ProgressMonitorInputStream pms = new ProgressMonitorInputStream(rp, "Loading", is)) {
      monitor = pms.getProgressMonitor();
      monitor.setNote(" "); // Need for JLabel#getPreferredSize
      monitor.setMillisToDecideToPopup(0);
      monitor.setMillisToPopup(0);
      monitor.setMinimum(0);
      monitor.setMaximum(length);
      MonitorTask task = new MonitorTask(pms, cs, length) {
        @Override protected void done() {
          super.done();
          loop.exit();
        }
      };
      task.execute();
      loop.enter();
    } catch (IOException ex) {
      // ex.printStackTrace();
      textArea.setText("error: " + ex.getMessage());
    }
  }

  private static Charset getCharset(URLConnection urlConnection) {
    String encoding = urlConnection.getContentEncoding();
    Charset cs;
    if (Objects.nonNull(encoding)) {
      cs = Charset.forName(encoding);
    } else {
      String contentType = urlConnection.getContentType();
      cs = Stream.of(contentType.split(";"))
          .map(String::trim)
          .filter(s -> !s.isEmpty() && s.toLowerCase(Locale.ENGLISH).startsWith("charset="))
          .map(s -> s.substring("charset=".length()))
          .findFirst()
          .map(Charset::forName)
          .orElse(StandardCharsets.UTF_8);
    }
    // System.out.println(cs);
    return cs;
  }

  private class MonitorTask extends BackgroundTask {
    protected MonitorTask(ProgressMonitorInputStream pms, Charset cs, int length) {
      super(pms, cs, length);
    }

    @Override protected void process(List<Chunk> chunks) {
      if (isDisplayable() && !isCancelled()) {
        chunks.forEach(MainPanel.this::update);
      } else {
        cancel(true);
      }
    }

    @Override protected void done() {
      updateComponentDone();
      String text;
      try {
        closeStream();
        text = isCancelled() ? "Cancelled" : get();
      } catch (InterruptedException ex) {
        text = "Interrupted";
        Thread.currentThread().interrupt();
      } catch (IOException | ExecutionException ex) {
        // ex.printStackTrace();
        text = "Error:" + ex.getMessage();
      }
      append(text);
    }
  }

  public void updateComponentDone() {
    runButton.setEnabled(true);
  }

  private void update(Chunk c) {
    append(c.getLine());
    monitor.setNote(c.getNote());
  }

  public void append(String str) {
    textArea.append(str + "\n");
    textArea.setCaretPosition(textArea.getDocument().getLength());
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class Chunk {
  private final String line;
  private final String note;

  protected Chunk(String line, String note) {
    this.line = line;
    this.note = note;
  }

  public String getLine() {
    return line;
  }

  public String getNote() {
    return note;
  }
}

class BackgroundTask extends SwingWorker<String, Chunk> {
  private final ProgressMonitorInputStream pms;
  private final Charset cs;
  private final int lengthOfFile;

  protected BackgroundTask(ProgressMonitorInputStream pms, Charset cs, int length) {
    super();
    this.pms = pms;
    this.cs = cs;
    this.lengthOfFile = length;
  }

  @Override protected String doInBackground() throws InterruptedException {
    String ret = "Done";
    // try (BufferedReader reader = new BufferedReader(new InputStreamReader(pms, cs))) {
    //   int i = 0;
    //   int size = 0;
    //   String line;
    //   while ((line = reader.readLine()) != null) {
    //     if (i % 50 == 0) { // Wait
    //       Thread.sleep(10);
    //     }
    //     i++;
    //     size += line.getBytes(cs).length + 1; // +1: \n
    //     String note = String.format("%03d%% - %d/%d%n", 100 * size / length, size, length);
    //     publish(new Chunk(line, note));
    //   }
    // } catch (IOException ex) {
    //   append("Exception");
    //   ret = "Exception";
    //   cancel(true);
    // }
    try (Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(pms, cs)))) {
      int i = 0;
      int readied = 0;
      while (scanner.hasNextLine()) {
        readied = doSomething(scanner, i++, readied);
      }
    }
    return ret;
  }

  protected int doSomething(Scanner scanner, int idx, int readied) throws InterruptedException {
    if (idx % 50 == 0) {
      Thread.sleep(10);
    }
    String line = scanner.nextLine();
    int size = readied + line.getBytes(cs).length + 1; // +1: \n
    int pct = 100 * size / lengthOfFile;
    String note = String.format("%03d%% - %d/%d%n", pct, size, lengthOfFile);
    publish(new Chunk(line, note));
    return size;
  }

  protected void closeStream() throws IOException {
    if (Objects.nonNull(pms)) {
      pms.close();
    }
  }
}
