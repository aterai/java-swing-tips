package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTextField field1 = new JTextField("aaaaaaaaaa");
    private final JTextField field2 = new JTextField();
    private final JTextField field3 = new JTextField("123465789735");
    public MainPanel() {
        super(new BorderLayout());
        Color c = field1.getBackground();
        field1.addFocusListener(new BGFocusListener(c, new Color(230,230,255)));
        field2.addFocusListener(new BGFocusListener(c, new Color(255,255,230)));
        field3.addFocusListener(new BGFocusListener(c, new Color(255,230,230)));
        Box box = Box.createVerticalBox();
        box.add(makePanel("Color(230,230,255)", field1));
        box.add(Box.createVerticalStrut(5));
        box.add(makePanel("Color(255,255,230)", field2));
        box.add(Box.createVerticalStrut(5));
        box.add(makePanel("Color(255,230,230)", field3));
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 200));
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class BGFocusListener implements FocusListener {
    private final Color dColor;
    private final Color oColor;
    public BGFocusListener(Color oColor, Color dColor) {
        this.dColor = dColor;
        this.oColor = oColor;
    }
    @Override public void focusGained(FocusEvent e) {
        e.getComponent().setBackground(dColor);
    }
    @Override public void focusLost(FocusEvent e) {
        e.getComponent().setBackground(oColor);
    }
}
