package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private final JLabel label1 = new JLabel();
    private final JLabel label2 = new JLabel();
    private final JButton button1 = new JButton();
    private final JButton button2 = new JButton();
    public MainPanel() {
        super(new BorderLayout());
        label1.putClientProperty("html.disable", Boolean.TRUE);
        button1.putClientProperty("html.disable", Boolean.TRUE);

        label1.setText("<html><font color=red>Html l1</font></html>");
        button1.setText("<html><font color=red>Html b1</font></html>");
        label1.setToolTipText("<html>&lt;html&gt;&lt;font color=red&gt;Html&lt;/font&gt;&lt;/html&gt;</html>");
        button1.setToolTipText("<html><font color=red>Html</font></html>");

        label2.setText("<html><font color=red>Html l2</font></html>");
        button2.setText("<html><font color=red>Html b2</font></html>");

        Box box = Box.createVerticalBox();
        box.add(label1);
        box.add(Box.createVerticalStrut(2));
        box.add(button1);
        box.add(Box.createVerticalStrut(20));
        box.add(label2);
        box.add(Box.createVerticalStrut(2));
        box.add(button2);
        add(box);
        setBorder(BorderFactory.createEmptyBorder(20,5,20,5));
        setPreferredSize(new Dimension(320,180));
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
