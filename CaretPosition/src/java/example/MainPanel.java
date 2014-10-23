package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private static final String LINESEPARATOR = "\n";
    private static final int LIMIT = 1000;
    private final JTextPane jtp = new JTextPane();
    private final JButton startButton = new JButton("Start");
    private final JButton stopButton  = new JButton("Stop");
    private final JButton clearButton = new JButton("Clear");
    private final DateFormat df = DateFormat.getDateTimeInstance();
    private final Timer timer;

    public MainPanel() {
        super(new BorderLayout());
        df.setTimeZone(TimeZone.getTimeZone("JST"));
        jtp.setEditable(false);
        stopButton.setEnabled(false);

        timer = new Timer(200, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                append(df.format(new Date()));
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                timerStart();
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                timerStop();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                jtp.setText("");
                if (!timer.isRunning()) {
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                }
            }
        });
        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(Box.createHorizontalGlue());
        box.add(startButton);
        box.add(stopButton);
        box.add(Box.createHorizontalStrut(5));
        box.add(clearButton);

        add(new JScrollPane(jtp));
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private void timerStop() {
        timer.stop();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }
    private void timerStart() {
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        timer.start();
    }
    private void append(String str) {
        Document doc = jtp.getDocument();
        String text;
        if (doc.getLength() > LIMIT) {
            timerStop();
            startButton.setEnabled(false);
            text = "doc.getLength()>1000";
        } else {
            text = str;
        }
        try {
            doc.insertString(doc.getLength(), text + LINESEPARATOR, null);
            jtp.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
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
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
