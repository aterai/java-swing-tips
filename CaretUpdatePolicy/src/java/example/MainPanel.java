package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox("on EDT", true);
    private final JButton start = new JButton("Start");
    private final JButton stop  = new JButton("Stop");
    private final JTextArea textArea0 = new JTextArea();
    private final JTextArea textArea1 = new JTextArea();
    private final JTextArea textArea2 = new JTextArea();
    //TEST: private final Timer timer = new Timer(500, e -> test(new Date().toString()));
    //TEST: private Thread thread;
    private transient SwingWorker<String, String> worker;

    public MainPanel() {
        super(new BorderLayout());

        ((DefaultCaret) textArea0.getCaret()).setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT); //default
        ((DefaultCaret) textArea1.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        ((DefaultCaret) textArea2.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        JPanel p = new JPanel(new GridLayout(1, 0));
        p.add(makeTitledPanel("UPDATE_WHEN_ON_EDT", new JScrollPane(textArea0)));
        p.add(makeTitledPanel("ALWAYS_UPDATE", new JScrollPane(textArea1)));
        p.add(makeTitledPanel("NEVER_UPDATE", new JScrollPane(textArea2)));

        for (int i = 0; i < 10; i++) {
            test(Integer.toString(i));
        }

        start.addActionListener(e -> startTest());
        stop.setEnabled(false);
        stop.addActionListener(e -> {
            //TEST: timer.stop();
            //TEST: thread = null;
            if (Objects.nonNull(worker)) {
                worker.cancel(true);
                worker = null;
            }
        });

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(Box.createHorizontalGlue());
        box.add(check);
        box.add(Box.createHorizontalStrut(5));
        box.add(start);
        box.add(Box.createHorizontalStrut(5));
        box.add(stop);

        add(p);
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private class Task extends SwingWorker<String, String> {
        @Override public String doInBackground() {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    return "Interrupted";
                }
                if (check.isSelected()) {
                    publish(new Date().toString()); //On EDT
                } else {
                    test(new Date().toString()); //Not on EDT
                }
            }
        }
    }
    private static JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    private static void insertText(JTextArea textArea, String s) {
        textArea.append(s + "\n");
    }
    private void test(String s) {
        insertText(textArea0, s);
        insertText(textArea1, s);
        insertText(textArea2, s);
    }
    private void startTest() {
//         //TEST:
//         if (!timer.isRunning()) {
//             timer.start();
//         }
//         //TEST:
//         if (Objects.isNull(thread)) {
//             thread = new Thread(() -> {
//                 while (thread != null) {
//                     test(new Date().toString());
//                     try {
//                         Thread.sleep(1000);
//                     } catch (InterruptedException ex) {
//                         ex.printStackTrace();
//                     }
//                 }
//             });
//             thread.start();
//         }
        if (Objects.isNull(worker)) {
            worker = new Task() {
                @Override public String doInBackground() {
                    while (true) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {
                            return "Interrupted";
                        }
                        if (check.isSelected()) {
                            publish(new Date().toString()); //On EDT
                        } else {
                            test(new Date().toString()); //Not on EDT
                        }
                    }
                }
                @Override protected void process(List<String> chunks) {
                    for (String message: chunks) {
                        test(message);
                    }
                }
                @Override public void done() {
                    check.setEnabled(true);
                    start.setEnabled(true);
                    stop.setEnabled(false);
                }
            };
            check.setEnabled(false);
            start.setEnabled(false);
            stop.setEnabled(true);
            worker.execute();
        }
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
