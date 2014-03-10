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
    private final JTextField outf  = new JTextField(30);
    private final SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
    private final DateFormat df = DateFormat.getDateTimeInstance();
    public MainPanel() {
        super(new BorderLayout());

        field.setText("Mon, 19 Apr 2004 16:31:41 +0900");
        outf.setEditable(false);
        df.setTimeZone(TimeZone.getTimeZone("JST"));

        JButton button = new JButton(new AbstractAction("Convert") {
            @Override public void actionPerformed(ActionEvent e) {
                String str = field.getText().trim();
                ParsePosition pp = new ParsePosition(0);
                Date date = format.parse(str, pp);
                if (date == null) {
                    outf.setText("error");
                } else {
                    outf.setText(df.format(date));
                }
            }
        });

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder("DateFormat"));
        box.add(field);
        box.add(Box.createVerticalStrut(5));
        box.add(outf);
        box.add(Box.createVerticalStrut(5));
        box.add(button);
        button.setAlignmentX(1f);

        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 160));
    }

    public static void main(String[] args) {
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
