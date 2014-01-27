package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTextArea textArea = new JTextArea();
    private final JButton runButton  = new JButton(new RunAction());
    private SwingWorker<String, Chunk> worker;
    private ProgressMonitor monitor;

    public MainPanel() {
        super(new BorderLayout(5,5));
        textArea.setEditable(false);

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(runButton);

        add(new JScrollPane(textArea));
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
    }
    class RunAction extends AbstractAction {
        public RunAction() {
            super("Load");
        }
        @Override public void actionPerformed(ActionEvent e) {
            final JFrame frame = (JFrame)SwingUtilities.getWindowAncestor((Component)e.getSource());
            runButton.setEnabled(false);
            textArea.setText("");
            frame.getGlassPane().setVisible(true);
            worker = new SwingWorker<String, Chunk>() {
                @Override public String doInBackground() {
                    //Random random = new Random();
                    Charset cs = Charset.forName("EUC-JP");
                    String ret = "Done";
                    int index = 19; //1 + random.nextInt(27-1);
                    String path = String.format("http://docs.oracle.com/javase/7/docs/api/index-files/index-%d.html", index); //???: UTF-8
                    //String path = String.format("http://docs.oracle.com/javase/jp/6/api/index-files/index-%d.html", index); //EUC-JP
                    //String path = "http://terai.xrea.jp/";
                    System.out.println(path);
                    URLConnection urlConnection;
                    try{
                        urlConnection = new URL(path).openConnection();
                        System.out.println(urlConnection.getContentEncoding());
                        System.out.println(urlConnection.getContentType());

                        String encoding = urlConnection.getContentEncoding();
                        if(encoding == null) {
                            String contentType = urlConnection.getContentType();
                            for(String value: contentType.split(";")) {
                                value = value.trim();
                                if(value.toLowerCase(Locale.ENGLISH).startsWith("charset=")) {
                                    encoding = value.substring("charset=".length());
                                }
                            }
                            if(encoding != null) {
                                cs = Charset.forName(encoding);
                            }
                        }else{
                            cs = Charset.forName(encoding);
                        }
                        System.out.println(cs);
                    }catch(IOException ex) {
                        ex.printStackTrace();
                        ret = "Error";
                        return ret;
                    }
                    int length = urlConnection.getContentLength();
                    try(InputStream is = urlConnection.getInputStream();
                        ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(frame, "Loading", is);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(pmis, cs))) {

                        monitor = pmis.getProgressMonitor();
                        monitor.setNote(" "); //Need for JLabel#getPreferredSize
                        monitor.setMillisToDecideToPopup(0);
                        monitor.setMillisToPopup(0);
                        monitor.setMinimum(0);
                        monitor.setMaximum(length);

                        int i = 0;
                        int size = 0;
                        String line;
                        while((line = reader.readLine()) != null) {
                            if(i%50==0) { //Wait
                                Thread.sleep(10);
                            }
                            i++;
                            size += line.getBytes(cs).length + 1; //+1: \n
                            String note = String.format("%03d%% - %d/%d%n", 100*size/length, size, length);
                            publish(new Chunk(line, note));
                        }
                    }catch(InterruptedException | IOException ex) {
                        //System.out.println("Exception");
                        ret = "Exception";
                        cancel(true);
                    }
                    return ret;
                }
                @Override protected void process(List<Chunk> chunks) {
                    for(Chunk c: chunks) {
                        textArea.append(c.line+"\n");
                        monitor.setNote(c.note);
                        //System.out.println(c.note);
                    }
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                }
                @Override public void done() {
                    frame.getGlassPane().setVisible(false);
                    runButton.setEnabled(true);
                    String text = null;
                    try{
                        text = isCancelled() ? "Cancelled" : get();
                    }catch(InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                        text = "Exception";
                    }
                    System.out.println(text);
                }
            };
            worker.execute();
        }
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
    public Chunk(String line, String note) {
        this.line = line;
        this.note = note;
    }
}
