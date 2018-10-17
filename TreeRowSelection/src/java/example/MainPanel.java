package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

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

class RowSelectionTree extends JTree {
    public static final Color SELC = new Color(100, 150, 200);
    // private Handler handler;

    @Override protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(SELC);
        Arrays.stream(getSelectionRows()).forEach(i -> {
            Rectangle r = getRowBounds(i);
            g2.fillRect(0, r.y, getWidth(), r.height);
        });
        // for (int i: getSelectionRows()) {
        //     Rectangle r = getRowBounds(i);
        //     g2.fillRect(0, r.y, getWidth(), r.height);
        // }
        super.paintComponent(g);
        if (hasFocus()) {
            Optional.ofNullable(getLeadSelectionPath()).ifPresent(path -> {
                Rectangle r = getRowBounds(getRowForPath(path));
                g2.setPaint(SELC.darker());
                g2.drawRect(0, r.y, getWidth() - 1, r.height - 1);
            });
            // TreePath path = getLeadSelectionPath();
            // if (Objects.nonNull(path)) {
            //     Rectangle r = getRowBounds(getRowForPath(path));
            //     g2.setPaint(SELC.darker());
            //     g2.drawRect(0, r.y, getWidth() - 1, r.height - 1);
            // }
        }
        g2.dispose();
    }
    @Override public void updateUI() {
        // removeFocusListener(handler);
        super.updateUI();
        setUI(new BasicTreeUI() {
            @Override public Rectangle getPathBounds(JTree tree, TreePath path) {
                if (Objects.nonNull(tree) && Objects.nonNull(treeState)) {
                    return getPathBounds(path, tree.getInsets(), new Rectangle());
                }
                return null;
            }
            private Rectangle getPathBounds(TreePath path, Insets insets, Rectangle bounds) {
                Rectangle rect = treeState.getBounds(path, bounds);
                if (Objects.nonNull(rect)) {
                    rect.width = tree.getWidth();
                    rect.y += insets.top;
                }
                return rect;
            }
        });
        UIManager.put("Tree.repaintWholeRow", Boolean.TRUE);
        // handler = new Handler();
        // addFocusListener(handler);
        setCellRenderer(new Handler());
        setOpaque(false);
    }
    private static class Handler extends DefaultTreeCellRenderer { // implements FocusListener {
        @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            l.setBackground(selected ? SELC : tree.getBackground());
            l.setOpaque(true);
            return l;
        }
        // @Override public void focusGained(FocusEvent e) {
        //     e.getComponent().repaint();
        // }
        // @Override public void focusLost(FocusEvent e) {
        //     e.getComponent().repaint();
        //     // TEST:
        //     // if (Objects.nonNull(tree.getLeadSelectionPath())) {
        //     //     Rectangle r = tree.getRowBounds(tree.getRowForPath(tree.getLeadSelectionPath()));
        //     //     r.width += r.x;
        //     //     r.x = 0;
        //     //     tree.repaint(r);
        //     // }
        // }
    }
}
