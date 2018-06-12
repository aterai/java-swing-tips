package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTree tree = new JTree();
        MouseListener ml = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                JTree tree = (JTree) e.getComponent();
                if (tree.getRowForLocation(e.getX(), e.getY()) < 0) {
                    tree.clearSelection();
                }
            }
        };
        JCheckBox check = new JCheckBox("JTree#clearSelection: when user clicks empty surface");
        check.addActionListener(e -> {
            if (((JCheckBox) e.getSource()).isSelected()) {
                tree.addMouseListener(ml);
            } else {
                tree.removeMouseListener(ml);
            }
        });

        add(check, BorderLayout.NORTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
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
