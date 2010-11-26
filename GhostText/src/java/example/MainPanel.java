package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    private final JTextField field1 = new JTextField("Please enter your E-mail address");
    private final JTextField field2 = new JTextField("History Search");
    public MainPanel() {
        super(new BorderLayout());
        field1.addFocusListener(new HintTextFocusListener(field1));
        field2.addFocusListener(new HintTextFocusListener(field2));

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        box.add(makePanel("E-mail", field1));
        box.add(Box.createVerticalStrut(5));
        box.add(makePanel("Search", field2));

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 180));
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
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
class HintTextFocusListener implements FocusListener {
    private static final Color INACTIVE_COLOR = UIManager.getColor("TextField.inactiveForeground");
    private static final Color ORIGINAL_COLOR = UIManager.getColor("TextField.foreground");
    private final String hintMessage;
    public HintTextFocusListener(final JTextComponent tf) {
        hintMessage = tf.getText();
        tf.setForeground(INACTIVE_COLOR);
    }
    @Override public void focusGained(final FocusEvent e) {
        JTextComponent textField = (JTextComponent)e.getSource();
        String str = textField.getText();
        Color col  = textField.getForeground();
        if(hintMessage.equals(str) && INACTIVE_COLOR.equals(col)) {
            textField.setForeground(ORIGINAL_COLOR);
            textField.setText("");
        }
    }
    @Override public void focusLost(final FocusEvent e) {
        JTextComponent textField = (JTextComponent)e.getSource();
        String str = textField.getText().trim();
        if("".equals(str)) {
            textField.setForeground(INACTIVE_COLOR);
            textField.setText(hintMessage);
        }
    }
}
