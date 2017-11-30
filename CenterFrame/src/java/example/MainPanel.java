package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(2, 1, 5, 5));

        JFrame frame = new JFrame("Test - JFrame");
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setSize(240, 240);

        JButton center = new JButton("frame.setLocationRelativeTo(null)");
        center.addActionListener(e -> {
            if (frame.isVisible()) {
                return;
            }
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        JButton relative = new JButton("frame.setLocationRelativeTo(button)");
        relative.addActionListener(e -> {
            if (frame.isVisible()) {
                return;
            }
            frame.setLocationRelativeTo((Component) e.getSource());
            frame.setVisible(true);
        });

        add(makeTitledPanel("in center of screen", center));
        add(makeTitledPanel("relative to this button", relative));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
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
