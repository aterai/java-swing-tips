package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTextField field = new JTextField(30);
    private final JTextArea textArea = new JTextArea();
    private final SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
    private final DateFormat df = DateFormat.getDateTimeInstance();
    public MainPanel() {
        super(new BorderLayout());

        field.setText(format.format(new Date()));
        textArea.setEditable(false);
        df.setTimeZone(TimeZone.getTimeZone("JST"));

        JPanel bp = new JPanel(new GridLayout(1, 0, 2, 2));
        bp.add(new JButton(new AbstractAction("format") {
            @Override public void actionPerformed(ActionEvent e) {
                field.setText(format.format(new Date()));
            }
        }));
        bp.add(new JButton(new AbstractAction("parse") {
            @Override public void actionPerformed(ActionEvent e) {
                String str = field.getText().trim();
                ParsePosition pp = new ParsePosition(0);
                Date date = format.parse(str, pp);
                String o = Objects.nonNull(date) ? df.format(date) : "error";
                textArea.append(o + "\n");
            }
        }));

        GridBagConstraints c = new GridBagConstraints();
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("DateFormat"));

        c.insets  = new Insets(2, 2, 2, 2);
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.anchor  = GridBagConstraints.LINE_END;
        c.weightx = 1d;
        p.add(field, c);

        c.insets  = new Insets(2, 0, 2, 2);
        c.fill    = GridBagConstraints.NONE;
        c.weightx = 0d;
        c.gridy   = 1;
        p.add(bp, c);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(textArea));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
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
