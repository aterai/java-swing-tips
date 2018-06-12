package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.Optional;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTree tree = new JTree(makeModel()) {
            @Override public boolean getScrollableTracksViewportWidth() {
                return true;
            }
            @Override public void updateUI() {
                super.updateUI();
                setCellRenderer(new TableOfContentsTreeCellRenderer());
                setBorder(BorderFactory.createTitledBorder("TreeCellRenderer"));
            }
        };
        tree.setRootVisible(false);

        JTree tree2 = new TableOfContentsTree(makeModel());
        tree2.setRootVisible(false);
        // tree2.setLargeModel(false);

        JSplitPane sp = new JSplitPane();
        sp.setResizeWeight(.5);
        sp.setLeftComponent(new JScrollPane(tree));
        sp.setRightComponent(new JScrollPane(tree2));
        add(sp);
        setPreferredSize(new Dimension(320, 240));
    }
    private static DefaultTreeModel makeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");

        DefaultMutableTreeNode s0 = new DefaultMutableTreeNode(new TableOfContents("1. Introduction", 1));
        root.add(s0);

        DefaultMutableTreeNode s1 = new DefaultMutableTreeNode(new TableOfContents("2. Chapter", 1));
        s1.add(new DefaultMutableTreeNode(new TableOfContents("2.1. Section", 2)));
        s1.add(new DefaultMutableTreeNode(new TableOfContents("2.2. Section", 4)));
        s1.add(new DefaultMutableTreeNode(new TableOfContents("2.3. Section", 8)));
        root.add(s1);

        DefaultMutableTreeNode s2 = new DefaultMutableTreeNode(new TableOfContents("3. Chapter", 10));
        s2.add(new DefaultMutableTreeNode(new TableOfContents("ddddddd", 12)));
        s2.add(new DefaultMutableTreeNode(new TableOfContents("eee", 24)));
        s2.add(new DefaultMutableTreeNode(new TableOfContents("f", 38)));
        root.add(s2);

        return new DefaultTreeModel(root);
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

class TableOfContents {
    public final String title;
    public final Integer page;
    protected TableOfContents(String title, int page) {
        this.title = title;
        this.page = page;
    }
    @Override public String toString() {
        return title;
    }
}

class TableOfContentsTreeCellRenderer extends DefaultTreeCellRenderer {
    protected static final BasicStroke READER = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] {1f}, 0f);
    protected int pn = -1;
    protected final Point pnPt = new Point();
    protected int rxs;
    protected int rxe;
    protected boolean isSynth;
    protected final JPanel renderer = new JPanel(new BorderLayout()) {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (pn >= 0) {
                String str = String.format("%3d", pn);
                FontMetrics metrics = g.getFontMetrics();
                // int xx = pnPt.x - getX() - metrics.stringWidth(str);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(isSynth ? getForeground() : getTextNonSelectionColor());
                g2.drawString(str, pnPt.x - getX() - metrics.stringWidth(str), pnPt.y);
                g2.setStroke(READER);
                g2.drawLine(rxs, pnPt.y, rxe - getX() - metrics.stringWidth("000"), pnPt.y);
                g2.dispose();
            }
        }
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = Short.MAX_VALUE;
            return d;
        }
    };
    @Override public void updateUI() {
        super.updateUI();
        isSynth = getUI().getClass().getName().contains("Synth");
        if (isSynth) {
            // System.out.println("XXX: FocusBorder bug?, JDK 1.7.0, Nimbus start LnF");
            setBackgroundSelectionColor(new Color(0x0, true));
        }
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        return Optional.ofNullable(value)
            .filter(DefaultMutableTreeNode.class::isInstance).map(DefaultMutableTreeNode.class::cast)
            .map(DefaultMutableTreeNode::getUserObject)
            .filter(TableOfContents.class::isInstance).map(TableOfContents.class::cast)
            .map(toc -> {
                renderer.removeAll();
                renderer.add(l, BorderLayout.WEST);
                if (isSynth) {
                    renderer.setForeground(l.getForeground());
                }

                int gap = l.getIconTextGap();
                Dimension d = l.getPreferredSize();
                pnPt.setLocation(tree.getWidth() - gap, l.getBaseline(d.width, d.height));
                pn = toc.page;
                rxs = d.width + gap;
                rxe = tree.getWidth() - tree.getInsets().right - gap;

                renderer.setOpaque(false);
                return (Component) renderer;
            })
            .orElseGet(() -> {
                pn = -1;
                return (Component) l;
            });
    }
    // // @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    // @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    //     JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    //     if (value instanceof DefaultMutableTreeNode) {
    //         DefaultMutableTreeNode n = (DefaultMutableTreeNode) value;
    //         Object o = n.getUserObject();
    //         if (o instanceof TableOfContents) {
    //             TableOfContents toc = (TableOfContents) o;
    //             int gap = l.getIconTextGap();
    //             Dimension d = l.getPreferredSize();
    //             Insets ins = tree.getInsets();
    //
    //             p.removeAll();
    //             p.add(l, BorderLayout.WEST);
    //             if (isSynth) {
    //                 p.setForeground(l.getForeground());
    //             }
    //
    //             pnPt.setLocation(tree.getWidth() - gap, l.getBaseline(d.width, d.height));
    //             pn = toc.page;
    //             rxs = d.width + gap;
    //             rxe = tree.getWidth() - ins.right - gap;
    //
    //             p.setOpaque(false);
    //             return p;
    //         }
    //     }
    //     pn = -1;
    //     return l;
    // }
}

// // TEST:
// class ShortTableOfContentsTreeCellRenderer extends DefaultTreeCellRenderer {
//     protected static final String READER = "... ";
//     protected final Point pt = new Point();
//     protected String pn;
//     protected boolean isSynth;
//     protected final JPanel p = new JPanel(new BorderLayout()) {
//         @Override protected void paintComponent(Graphics g) {
//             super.paintComponent(g);
//             if (pn != null) {
//                 g.setColor(isSynth ? getForeground() : getTextNonSelectionColor());
//                 g.drawString(pn, pt.x - getX(), pt.y);
//             }
//         }
//         @Override public Dimension getPreferredSize() {
//             Dimension d = super.getPreferredSize();
//             d.width = Short.MAX_VALUE;
//             return d;
//         }
//     };
//     @Override public void updateUI() {
//         super.updateUI();
//         isSynth = getUI().getClass().getName().contains("Synth");
//         if (isSynth) {
//             // System.out.println("XXX: FocusBorder bug?, JDK 1.7.0, Nimbus start LnF");
//             setBackgroundSelectionColor(new Color(0x0, true));
//         }
//     }
//     @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//         JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
//         if (value instanceof DefaultMutableTreeNode) {
//             DefaultMutableTreeNode n = (DefaultMutableTreeNode) value;
//             Object o = n.getUserObject();
//             if (o instanceof TableOfContents) {
//                 TableOfContents toc = (TableOfContents) o;
//                 FontMetrics metrics = l.getFontMetrics(l.getFont());
//                 int gap = l.getIconTextGap();
//
//                 p.removeAll();
//                 p.add(l, BorderLayout.WEST);
//                 if (isSynth) {
//                     p.setForeground(l.getForeground());
//                 }
//
//                 pn = String.format("%s%3d", READER, toc.page);
//                 pt.x = tree.getWidth() - metrics.stringWidth(pn) - gap;
//                 // pt.x = Math.max(pnx, titlex + metrics.stringWidth(pair.title) + gap);
//                 pt.y = (l.getIcon().getIconHeight() + metrics.getAscent()) / 2;
//                 p.setOpaque(false);
//
//                 return p;
//             }
//         }
//         pn = null;
//         return l;
//     }
// }

class TableOfContentsTree extends JTree {
    protected static final BasicStroke READER = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] {1f}, 0f);
    protected boolean isSynth;
    protected TableOfContentsTree(TreeModel model) {
        super(model);
    }
    @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createTitledBorder("JTree#paintComponent(...)"));
        isSynth = getUI().getClass().getName().contains("Synth");
    }
    protected Rectangle getVisibleRowsRect() {
        Insets i = getInsets();
        Rectangle visRect = getVisibleRect();
        if (visRect.x == 0 && visRect.y == 0 && visRect.width == 0 && visRect.height == 0 && getVisibleRowCount() > 0) {
            // The tree doesn't have a valid bounds yet. Calculate
            // based on visible row count.
            visRect.width = 1;
            visRect.height = getRowHeight() * getVisibleRowCount();
        } else {
            visRect.x -= i.left;
            visRect.y -= i.top;
        }
        // we should consider a non-visible area above
        Class<JScrollPane> clz = JScrollPane.class;
        Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, this))
            .filter(clz::isInstance).map(clz::cast)
            .map(JScrollPane::getHorizontalScrollBar)
            .filter(JScrollBar::isVisible)
            .ifPresent(bar -> {
                int height = bar.getHeight();
                visRect.y -= height;
                visRect.height += height;
            });
        // Container container = SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        // if (container instanceof JScrollPane) {
        //     JScrollPane pane = (JScrollPane) container;
        //     JScrollBar bar = pane.getHorizontalScrollBar();
        //     if (bar != null && bar.isVisible()) {
        //         int height = bar.getHeight();
        //         visRect.y -= height;
        //         visRect.height += height;
        //     }
        // }
        return visRect;
    }
    @Override protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        FontMetrics fm = g.getFontMetrics();
        int pnmaxWidth = fm.stringWidth("000");
        Insets ins = getInsets();
        Rectangle rect = getVisibleRowsRect();
        for (int i = 0; i < getRowCount(); i++) {
            Rectangle r = getRowBounds(i);
            if (rect.intersects(r)) {
                TreePath path = getPathForRow(i);
                TreeCellRenderer tcr = getCellRenderer();
                if (isSynth && isRowSelected(i)) {
                    if (tcr instanceof DefaultTreeCellRenderer) {
                        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tcr;
                        g2.setPaint(renderer.getTextSelectionColor());
                    }
                } else {
                    g2.setPaint(getForeground());
                }
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object o = node.getUserObject();
                if (o instanceof TableOfContents) {
                    TableOfContents toc = (TableOfContents) o;
                    String pn = Integer.toString(toc.page);
                    int x = getWidth() - 1 - fm.stringWidth(pn) - ins.right;
                    // int y = (int) (.5 + r.y + (r.height + fm.getAscent()) * .5);
                    int y = r.y + ((Component) tcr).getBaseline(r.width, r.height);
                    g2.drawString(pn, x, y);

                    int gap = 5;
                    int x2 = getWidth() - 1 - pnmaxWidth - ins.right;
                    Stroke s = g2.getStroke();
                    g2.setStroke(READER);
                    g2.drawLine(r.x + r.width + gap, y, x2 - gap, y);
                    g2.setStroke(s);
                }
            }
        }
        g2.dispose();
    }
}
