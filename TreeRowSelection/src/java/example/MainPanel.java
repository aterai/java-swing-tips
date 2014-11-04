package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JPanel p = new JPanel(new GridLayout(1, 2));
        p.add(new JScrollPane(new JTree()));
        p.add(new JScrollPane(new RowSelectionTree()));
        add(p);
        setPreferredSize(new Dimension(320, 240));
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

class RowSelectionTree extends JTree {
    private static final Color SELC = new Color(100, 150, 200);
    private Handler handler;

    @Override public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        if (getSelectionCount() > 0) {
            g.setColor(SELC);
            for (int i: getSelectionRows()) {
                Rectangle r = getRowBounds(i);
                g.fillRect(0, r.y, getWidth(), r.height);
            }
        }
        super.paintComponent(g);
        if (getLeadSelectionPath() != null) {
            Rectangle r = getRowBounds(getRowForPath(getLeadSelectionPath()));
            g.setColor(hasFocus() ? SELC.darker() : SELC);
            g.drawRect(0, r.y, getWidth() - 1, r.height - 1);
        }
    }
    @Override public void updateUI() {
        removeFocusListener(handler);
        super.updateUI();
        setUI(new BasicTreeUI() {
            @Override public Rectangle getPathBounds(JTree tree, TreePath path) {
                if (tree != null && treeState != null) {
                    return getPathBounds(path, tree.getInsets(), new Rectangle());
                }
                return null;
            }
            private Rectangle getPathBounds(TreePath path, Insets insets, Rectangle bounds) {
                Rectangle rect = treeState.getBounds(path, bounds);
                if (rect != null) {
                    rect.width = tree.getWidth();
                    rect.y += insets.top;
                }
                return rect;
            }
        });
        handler = new Handler();
        addFocusListener(handler);
        setCellRenderer(handler);
        setOpaque(false);
    }
    static class Handler extends DefaultTreeCellRenderer implements FocusListener {
        @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            l.setBackground(selected ? SELC : tree.getBackground());
            l.setOpaque(true);
            return l;
        }
        @Override public void focusGained(FocusEvent e) {
            e.getComponent().repaint();
        }
        @Override public void focusLost(FocusEvent e) {
            e.getComponent().repaint();
            //TEST:
            //if (tree.getLeadSelectionPath() != null) {
            //    Rectangle r = tree.getRowBounds(tree.getRowForPath(tree.getLeadSelectionPath()));
            //    r.width += r.x;
            //    r.x = 0;
            //    tree.repaint(r);
            //}
        }
    }
}
