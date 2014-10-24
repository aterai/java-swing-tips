package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;

public final class MainPanel extends JPanel {
    private final JTextField field00 = new JTextField("aaaaaaaaaaaaaaaa");
    private final JTextField field01 = new JTextField("bbbbbbb");
    private final JTextField field02 = new JTextField("ccccccccccccc");

    public MainPanel() {
        super(new BorderLayout());
        Insets m = field01.getMargin();
        System.out.println(m.toString());
        Insets margin = new Insets(m.top, m.left + 10, m.bottom, m.right);
        field01.setMargin(margin);

        Border b1 = BorderFactory.createEmptyBorder(0, 20, 0, 0);
        Border b2 = BorderFactory.createCompoundBorder(field02.getBorder(), b1);
        field02.setBorder(b2);

        Box box = Box.createVerticalBox();
        box.add(makePanel(field00));
        box.add(Box.createVerticalStrut(5));
        box.add(makePanel(field01));
        box.add(Box.createVerticalStrut(5));
        box.add(makePanel(field02));
        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 200));
    }
    private static int getLeftMargin(JTextField c) {
        System.out.println("----");
        System.out.println("getMargin().left: " + c.getMargin().left);
        System.out.println("getInsets().left: " + c.getInsets().left);
        System.out.println("getBorder().getBorderInsets(c).left: " + c.getBorder().getBorderInsets(c).left);
        return c.getBorder().getBorderInsets(c).left; //c.getMargin().left;
    }
    private static JPanel makePanel(JTextField field) {
        JPanel p = new JPanel(new BorderLayout());
        String title = "left margin = " + getLeftMargin(field);
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(field);
        return p;
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
            Insets m = UIManager.getInsets("TextField.margin");
            UIManager.put("TextField.margin", new InsetsUIResource(m.top, m.left + 5, m.bottom, m.right));
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
