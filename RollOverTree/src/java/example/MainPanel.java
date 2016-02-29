package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final JTree tree = new JTree();
    public MainPanel() {
        super(new BorderLayout());
        tree.setModel(makeModel());
        tree.setCellRenderer(new RollOverTreeCellRenderer(tree));
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

class RollOverTreeCellRenderer extends DefaultTreeCellRenderer implements MouseMotionListener {
    private static final Color ROLLOVER_ROW_COLOR = new Color(220, 240, 255);
    private final JTree tree;
    private int rollOverRowIndex = -1;

    public RollOverTreeCellRenderer(JTree tree) {
        super();
        this.tree = tree;
        tree.addMouseMotionListener(this);
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JComponent c = (JComponent) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (row == rollOverRowIndex) {
            c.setOpaque(true);
            c.setBackground(ROLLOVER_ROW_COLOR);
            if (selected) {
                c.setForeground(getTextNonSelectionColor());
            }
        } else {
            c.setOpaque(false);
        }
        return c;
    }
    @Override public void mouseMoved(MouseEvent e) {
        int row = tree.getRowForLocation(e.getX(), e.getY());
        if (row != rollOverRowIndex) {
            //System.out.println(row);
            rollOverRowIndex = row;
            tree.repaint();
        }
    }
    @Override public void mouseDragged(MouseEvent e) { /* not needed */ }
}
