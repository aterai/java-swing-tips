package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JDesktopPane desktop = new JDesktopPane();
        desktop.add(createFrame(0));
        desktop.add(createFrame(1));
        desktop.add(createFrame(2));

        JToggleButton button = new JToggleButton("InternalFrame.useTaskBar");
        button.addActionListener(e -> {
            Object c = e.getSource();
            if (c instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) c;
                UIManager.put("InternalFrame.useTaskBar", b.isSelected());
                SwingUtilities.updateComponentTreeUI(getRootPane());
            }
        });

        add(desktop);
        add(button, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private JInternalFrame createFrame(int i) {
        JInternalFrame f = new JInternalFrame("title: " + i, true, true, true, true);
        f.setSize(160, 120);
        f.setVisible(true);
        f.setLocation(10 + 20 * i, 10 + 20 * i);
        return f;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
        try {
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
