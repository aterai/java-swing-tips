package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());

        final JFrame frame = new JFrame("Test - JFrame");
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setSize(240, 240);

        JPanel p = new JPanel(new GridLayout(2,1,5,5));
        p.add(makePanel("in center of screen", new JButton(new AbstractAction("frame.setLocationRelativeTo(null)") {
            @Override public void actionPerformed(ActionEvent e) {
                if(frame.isVisible()) return;
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        })));
        p.add(makePanel("relative to this button", new JButton(new AbstractAction("frame.setLocationRelativeTo(button)") {
            @Override public void actionPerformed(ActionEvent e) {
                if(frame.isVisible()) return;
                frame.setLocationRelativeTo((Component)e.getSource());
                frame.setVisible(true);
            }
        })));
        add(p);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
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
