package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final JTree tree = new JTree(makeModel()) {
        protected final Color rolloverRowColor = new Color(220, 240, 255);
        protected int rollOverRowIndex = -1;
        protected transient MouseMotionListener listener;
        @Override public void updateUI() {
            removeMouseMotionListener(listener);
            super.updateUI();
            setCellRenderer(new DefaultTreeCellRenderer() {
                @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                    JComponent c = (JComponent) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                    if (row == rollOverRowIndex) {
                        c.setOpaque(true);
                        c.setBackground(rolloverRowColor);
                        if (selected) {
                            c.setForeground(getTextNonSelectionColor());
                        }
                    } else {
                        c.setOpaque(false);
                    }
                    return c;
                }
            });
            listener = new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    int row = getRowForLocation(e.getX(), e.getY());
                    if (row != rollOverRowIndex) {
                        rollOverRowIndex = row;
                        repaint();
                    }
                }
            };
            addMouseMotionListener(listener);
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private static DefaultTreeModel makeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
        DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
        DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
        set1.add(new DefaultMutableTreeNode("111111111"));
        set1.add(new DefaultMutableTreeNode("22222222222"));
        set1.add(new DefaultMutableTreeNode("33333"));
        set2.add(new DefaultMutableTreeNode("asdfasdfas"));
        set2.add(new DefaultMutableTreeNode("asdf"));
        set3.add(new DefaultMutableTreeNode("asdfasdfasdf"));
        set3.add(new DefaultMutableTreeNode("qwerqwer"));
        set3.add(new DefaultMutableTreeNode("zvxcvzxcvzxzxcvzxcv"));
        root.add(set1);
        root.add(set2);
        set2.add(set3);
        return new DefaultTreeModel(root);
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
