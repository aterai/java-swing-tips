package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTextArea textArea = new JTextArea();
    private final JButton runButton  = new JButton("Load");
    //private transient SwingWorker<String, Chunk> worker;
    private transient ProgressMonitor monitor;

    public MainPanel() {
        super(new BorderLayout(5, 5));
        textArea.setEditable(false);

        runButton.addActionListener(e -> executeWorker(e));

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(runButton);

        add(new JScrollPane(textArea));
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }

    protected void executeWorker(ActionEvent e) {
        JButton b = (JButton) e.getSource();
        b.setEnabled(false);
        textArea.setText("");

        URLConnection urlConnection = getURLConnection();
        if (Objects.isNull(urlConnection)) {
            return;
        }
        Charset cs = getCharset(urlConnection, "UTF-8");
        int length = urlConnection.getContentLength();

        try {
            InputStream is = urlConnection.getInputStream();
            ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(b.getTopLevelAncestor(), "Loading", is);
            monitor = pmis.getProgressMonitor();
            monitor.setNote(" "); //Need for JLabel#getPreferredSize
            monitor.setMillisToDecideToPopup(0);
            monitor.setMillisToPopup(0);
            monitor.setMinimum(0);
            monitor.setMaximum(length);

            new MonitorTask(pmis, cs, length).execute();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static Charset getCharset(URLConnection urlConnection, String defaultEncoding) {
        Charset cs = Charset.forName(defaultEncoding);
        String encoding = urlConnection.getContentEncoding();
        if (Objects.nonNull(encoding)) {
            cs = Charset.forName(encoding);
        } else {
            String contentType = urlConnection.getContentType();
            for (String value: contentType.split(";")) {
                value = value.trim();
                if (value.toLowerCase(Locale.ENGLISH).startsWith("charset=")) {
                    encoding = value.substring("charset=".length());
                }
            }
            System.out.println(encoding);
            if (Objects.nonNull(encoding)) {
                cs = Charset.forName(encoding);
            }
        }
        System.out.println(cs);
        return cs;
    }

    private static URLConnection getURLConnection() {
        //Random random = new Random();
        //Charset cs = Charset.forName("EUC-JP");
        int index = 19; //1 + random.nextInt(27-1);
        String path = String.format("https://docs.oracle.com/javase/8/docs/api/index-files/index-%d.html", index);
        //String path = String.format("https://docs.oracle.com/javase/7/docs/api/index-files/index-%d.html", index);
        //String path = String.format("https://docs.oracle.com/javase/jp/6/api/index-files/index-%d.html", index);
        //String path = "http://ateraimemo.com/";
        System.out.println(path);

        URLConnection urlConnection = null;
        try {
            urlConnection = new URL(path).openConnection();
            System.out.println("urlConnection.getContentEncoding(): " + urlConnection.getContentEncoding());
            System.out.println("urlConnection.getContentType(): " + urlConnection.getContentType());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return urlConnection;
    }

    private class MonitorTask extends BackgroundTask {
        protected MonitorTask(ProgressMonitorInputStream pmis, Charset cs, int length) {
            super(pmis, cs, length);
        }
        @Override protected void process(List<Chunk> chunks) {
            if (isCancelled()) {
                return;
            }
            if (!isDisplayable()) {
                cancel(true);
                return;
            }
            processChunks(chunks);
        }
        @Override public void done() {
            updateComponentDone();
            String text;
            try {
                if (Objects.nonNull(pmis)) {
                    pmis.close();
                }
                text = isCancelled() ? "Cancelled" : get();
            } catch (IOException | InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                text = "Exception";
            }
            System.out.println(text);
        }
    }

    protected void updateComponentDone() {
        runButton.setEnabled(true);
    }

    protected void processChunks(List<Chunk> chunks) {
        chunks.forEach(c -> {
            textArea.append(c.line + "\n");
            monitor.setNote(c.note);
        });
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Chunk {
    public final String line;
    public final String note;
    protected Chunk(String line, String note) {
        this.line = line;
        this.note = note;
    }
}

class BackgroundTask extends SwingWorker<String, Chunk> {
    protected final ProgressMonitorInputStream pmis;
    protected final Charset cs;
    protected final int length;
    protected BackgroundTask(ProgressMonitorInputStream pmis, Charset cs, int length) {
        super();
        this.pmis = pmis;
        this.cs = cs;
        this.length = length;
    }
    @Override public String doInBackground() {
        String ret = "Done";
//         try (BufferedReader reader = new BufferedReader(new InputStreamReader(pmis, cs))) {
//             int i = 0;
//             int size = 0;
//             String line;
//             while ((line = reader.readLine()) != null) {
//                 if (i % 50 == 0) { //Wait
//                     Thread.sleep(10);
//                 }
//                 i++;
//                 size += line.getBytes(cs).length + 1; //+1: \n
//                 String note = String.format("%03d%% - %d/%d%n", 100 * size / length, size, length);
//                 publish(new Chunk(line, note));
//             }
//         } catch (InterruptedException | IOException ex) {
//             System.out.println("Exception");
//             ret = "Exception";
//             cancel(true);
//         }
        try (Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(pmis, cs)))) {
            int i = 0;
            int size = 0;
            while (scanner.hasNextLine()) {
                if (i % 50 == 0) { //Wait
                    Thread.sleep(10);
                }
                i++;
                String line = scanner.nextLine();
                size += line.getBytes(cs).length + 1; //+1: \n
                String note = String.format("%03d%% - %d/%d%n", 100 * size / length, size, length);
                publish(new Chunk(line, note));
            }
        } catch (InterruptedException ex) {
            System.out.println("Exception");
            ret = "Exception";
            cancel(true);
        }
        return ret;
    }
}
