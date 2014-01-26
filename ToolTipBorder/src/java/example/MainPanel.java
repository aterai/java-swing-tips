package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JButton button0 = new JButton("ToolTip") {
            public JToolTip createToolTip() {
                JToolTip tip = new JToolTip();
                Border b1 = tip.getBorder();
                Border b2 = BorderFactory.createTitledBorder("ToolTip");
                tip.setBorder(BorderFactory.createCompoundBorder(b1, b2));
                tip.setComponent(this);
                return tip;
            }
        };
        button0.setToolTipText("Test - ToolTipText0");

        JButton button1 = new JButton("ToolTip") {
            public JToolTip createToolTip() {
                JToolTip tip = new JToolTip();
                Border b1 = tip.getBorder();
                Border b2 = BorderFactory.createMatteBorder(0,10,0,0,Color.GREEN);
                tip.setBorder(BorderFactory.createCompoundBorder(b1, b2));
                tip.setComponent(this);
                return tip;
            }
        };
        button1.setToolTipText("Test - ToolTipText1");

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        p.add(makePanel("TitleBorder", button0), BorderLayout.NORTH);
        p.add(makePanel("MatteBorder", button1), BorderLayout.SOUTH);
        add(p, BorderLayout.NORTH);
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
        }catch(ClassNotFoundException | InstantiationException |
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
