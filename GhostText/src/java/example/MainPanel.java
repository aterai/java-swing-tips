package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final JTextField field1 = new JTextField("Please enter your E-mail address");
    private final JTextField field2 = new JTextField("History Search");
    public MainPanel() {
        super(new BorderLayout());
        field1.addFocusListener(new PlaceholderFocusListener(field1));
        field2.addFocusListener(new PlaceholderFocusListener(field2));

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(makePanel("E-mail", field1));
        box.add(Box.createVerticalStrut(5));
        box.add(makePanel("Search", field2));

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPanel makePanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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

class PlaceholderFocusListener implements FocusListener {
    private static final Color INACTIVE = UIManager.getColor("TextField.inactiveForeground");
    private final String hintMessage;
    public PlaceholderFocusListener(JTextComponent tf) {
        hintMessage = tf.getText();
        tf.setForeground(INACTIVE);
    }
    @Override public void focusGained(FocusEvent e) {
        JTextComponent tf = (JTextComponent) e.getComponent();
        if (hintMessage.equals(tf.getText()) && INACTIVE.equals(tf.getForeground())) {
            tf.setForeground(UIManager.getColor("TextField.foreground"));
            tf.setText("");
        }
    }
    @Override public void focusLost(FocusEvent e) {
        JTextComponent tf = (JTextComponent) e.getComponent();
        if ("".equals(tf.getText().trim())) {
            tf.setForeground(INACTIVE);
            tf.setText(hintMessage);
        }
    }
}
