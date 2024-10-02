// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public final class MainPanel extends JPanel {
  private static final String FILE_NAME = "example.txt";
  private final SpinnerNumberModel model1 = new SpinnerNumberModel(0, 0, 6, 1);
  private final SpinnerNumberModel model2 = new SpinnerNumberModel(2, 0, 6, 1);
  private final JSpinner spinner1 = new JSpinner(model1);
  private final JSpinner spinner2 = new JSpinner(model2);
  private final JLabel label = new JLabel("2", SwingConstants.RIGHT);
  private final JTextPane log = new JTextPane();

  private MainPanel() {
    super(new BorderLayout());
    log.setEditable(false);
    StyledDocument doc = log.getStyledDocument();
    // Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    Style def = doc.getStyle(StyleContext.DEFAULT_STYLE);
    // Style regular = doc.addStyle(MessageType.REGULAR.toString(), def);
    // StyleConstants.setForeground(error, Color.BLACK);
    // Style error = doc.addStyle(ERROR, regular);
    StyleConstants.setForeground(doc.addStyle(MessageType.ERROR.toString(), def), Color.RED);
    StyleConstants.setForeground(doc.addStyle(MessageType.BLUE.toString(), def), Color.BLUE);

    JButton ok = new JButton("Create new " + FILE_NAME);
    ok.addActionListener(e -> addActionPerformed());

    JButton clear = new JButton("clear");
    clear.addActionListener(e -> log.setText(""));

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    box.add(Box.createHorizontalGlue());
    box.add(ok);
    box.add(Box.createHorizontalStrut(5));
    box.add(clear);

    JSpinner.NumberEditor editor1 = new JSpinner.NumberEditor(spinner1, "0");
    editor1.getTextField().setEditable(false);
    spinner1.setEditor(editor1);

    JSpinner.NumberEditor editor2 = new JSpinner.NumberEditor(spinner2, "0");
    editor2.getTextField().setEditable(false);
    spinner2.setEditor(editor2);

    ChangeListener cl = e -> {
      int i = model1.getNumber().intValue() + model2.getNumber().intValue();
      label.setText(Integer.toString(i));
    };
    model1.addChangeListener(cl);
    model2.addChangeListener(cl);

    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));

    JScrollPane scroll = new JScrollPane(log);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(25);

    add(makeNorthBox(), BorderLayout.NORTH);
    add(scroll);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private void addActionPerformed() {
    File file = new File(System.getProperty("java.io.tmpdir"), FILE_NAME);
    new BackgroundTask(file, model1.getNumber().intValue(), model2.getNumber().intValue()) {
      @Override protected void process(List<Message> chunks) {
        if (isDisplayable() && !isCancelled()) {
          chunks.forEach(MainPanel.this::append);
        } else {
          cancel(true);
        }
      }

      @Override protected void done() {
        try {
          File nf = get();
          if (Objects.isNull(nf)) {
            append(mkMsg("Failed to create backup file.", MessageType.ERROR));
          } else if (nf.createNewFile()) {
            append(mkMsg("Generated " + nf.getName() + ".", MessageType.REGULAR));
          } else {
            append(mkMsg("Failed to generate " + nf.getName() + ".", MessageType.ERROR));
          }
        } catch (InterruptedException ex) {
          append(mkMsg(ex.getMessage(), MessageType.ERROR));
          Thread.currentThread().interrupt();
        } catch (ExecutionException | IOException ex) {
          append(mkMsg(ex.getMessage(), MessageType.ERROR));
        }
        append(mkMsg("----------------------------------", MessageType.REGULAR));
      }
    }.execute();
  }

  private Component makeNorthBox() {
    JPanel northBox = new JPanel(new GridLayout(3, 2, 5, 5));
    northBox.add(new JLabel("Number of backups to keep:", SwingConstants.RIGHT));
    northBox.add(spinner1);
    northBox.add(new JLabel("Number of backups to delete in order:", SwingConstants.RIGHT));
    northBox.add(spinner2);
    northBox.add(new JLabel("Total number of backups:", SwingConstants.RIGHT));
    northBox.add(label);
    return northBox;
  }

  private void append(Message m) {
    StyledDocument doc = log.getStyledDocument();
    int len = doc.getLength();
    String txt = m.getText() + "\n";
    Style style = doc.getStyle(m.getType().toString());
    try {
      doc.insertString(len, txt, style);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
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
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

enum MessageType {
  REGULAR, ERROR, BLUE
}

class Message {
  private final String text;
  private final MessageType type;

  protected Message(String text, MessageType type) {
    this.text = text;
    this.type = type;
  }

  public String getText() {
    return text;
  }

  public MessageType getType() {
    return type;
  }
}

class BackgroundTask extends SwingWorker<File, Message> {
  private final File orgFile;
  private final int oldIdx;
  private final int newIdx;

  protected BackgroundTask(File file, int oldIdx, int newIdx) {
    super();
    this.orgFile = file;
    this.oldIdx = oldIdx;
    this.newIdx = newIdx;
  }

  @SuppressWarnings("PMD.OnlyOneReturn")
  @Override public File doInBackground() throws IOException {
    if (!orgFile.exists()) {
      return orgFile;
    }

    String newFileName = orgFile.getAbsolutePath();
    if (oldIdx == 0 && newIdx == 0) { // = backup off
      try {
        Files.delete(orgFile.toPath());
        return new File(newFileName);
      } catch (IOException ex) {
        publish(mkMsg(ex.getMessage(), MessageType.ERROR));
        return null;
      }
    }

    File tmpFile = renameAndBackup(orgFile, newFileName);
    if (Objects.nonNull(tmpFile)) {
      return tmpFile;
    }

    if (renameAndShiftBackup(orgFile)) {
      return new File(newFileName);
    } else {
      return null;
    }
  }

  @SuppressWarnings("PMD.OnlyOneReturn")
  private File renameAndBackup(File file, String newFileName) throws IOException {
    boolean simpleRename = false;
    File test = null;
    for (int i = 1; i <= oldIdx; i++) {
      test = createBackupFile(file, i);
      if (!test.exists()) {
        simpleRename = true;
        break;
      }
    }
    if (!simpleRename) {
      for (int i = oldIdx + 1; i <= oldIdx + newIdx; i++) {
        test = createBackupFile(file, i);
        if (!test.exists()) {
          simpleRename = true;
          break;
        }
      }
    }
    if (simpleRename) {
      Path path = file.toPath();
      try {
        publish(mkMsg("Rename the older file", MessageType.REGULAR));
        String msg = String.format("  %s -> %s", file.getName(), test.getName());
        publish(mkMsg(msg, MessageType.BLUE));
        Files.move(path, path.resolveSibling(test.getName()));
        return new File(newFileName);
      } catch (IOException ex) {
        publish(mkMsg(ex.getMessage(), MessageType.ERROR));
        throw ex;
      }
    }
    return null;
  }

  @SuppressWarnings("PMD.OnlyOneReturn")
  private boolean renameAndShiftBackup(File file) {
    File parent = file.getParentFile();
    File tmpFile3 = new File(parent, makeBackupFileName(file.getName(), oldIdx + 1));
    publish(mkMsg("Delete old backup file", MessageType.REGULAR));
    publish(mkMsg("  del:" + tmpFile3.getAbsolutePath(), MessageType.BLUE));
    try {
      Files.delete(tmpFile3.toPath());
    } catch (IOException ex) {
      publish(mkMsg(ex.getMessage(), MessageType.ERROR));
      return false;
    }
    for (int i = oldIdx + 2; i <= oldIdx + newIdx; i++) {
      File tmpFile1 = createBackupFile(file, i);
      File tmpFile2 = createBackupFile(file, i - 1);
      Path oldPath = tmpFile1.toPath();
      try {
        Files.move(oldPath, oldPath.resolveSibling(tmpFile2.getName()));
      } catch (IOException ex) {
        publish(mkMsg(ex.getMessage(), MessageType.ERROR));
        return false;
      }
      publish(mkMsg("Update old backup file numbers", MessageType.REGULAR));
      publish(mkMsg("  " + tmpFile1.getName() + " -> " + tmpFile2.getName(), MessageType.BLUE));
    }
    File tmp = new File(parent, makeBackupFileName(file.getName(), oldIdx + newIdx));
    publish(mkMsg("Rename the older file", MessageType.REGULAR));
    publish(mkMsg("  " + file.getName() + " -> " + tmp.getName(), MessageType.BLUE));

    Path path = file.toPath();
    try {
      Files.move(path, path.resolveSibling(tmp.getName()));
    } catch (IOException ex) {
      publish(mkMsg(ex.getMessage(), MessageType.ERROR));
      return false;
    }
    return true;
  }

  protected static Message mkMsg(String text, MessageType type) {
    return new Message(text, type);
  }

  private static String makeBackupFileName(String name, int num) {
    return String.format("%s.%d~", name, num);
  }

  private static File createBackupFile(File file, int idx) {
    return new File(file.getParentFile(), makeBackupFileName(file.getName(), idx));
  }
}
