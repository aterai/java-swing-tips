package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTree tree = new JTree(makeModel()) {
            @Override public boolean getScrollableTracksViewportWidth() { //NOPMD A getX() method which returns a boolean should be named isX()
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
        //tree2.setLargeModel(false);

        JSplitPane sp = new JSplitPane();
        sp.setResizeWeight(.5);
        sp.setLeftComponent(new JScrollPane(tree));
        sp.setRightComponent(new JScrollPane(tree2));
        add(sp);
        setPreferredSize(new Dimension(320, 240));
    }
    private static DefaultTreeModel makeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode s0 = new DefaultMutableTreeNode(new TableOfContents("1. Introductiion", 1));
        DefaultMutableTreeNode s1 = new DefaultMutableTreeNode(new TableOfContents("2. Chapter", 1));
        s1.add(new DefaultMutableTreeNode(new TableOfContents("2.1. Section", 2)));
        s1.add(new DefaultMutableTreeNode(new TableOfContents("2.2. Section", 4)));
        s1.add(new DefaultMutableTreeNode(new TableOfContents("2.3. Section", 8)));

        DefaultMutableTreeNode s2 = new DefaultMutableTreeNode(new TableOfContents("3. Chapter", 10));
        s2.add(new DefaultMutableTreeNode(new TableOfContents("ddddddd", 12)));
        s2.add(new DefaultMutableTreeNode(new TableOfContents("eee",     24)));
        s2.add(new DefaultMutableTreeNode(new TableOfContents("f",       38)));

        root.add(s0); root.add(s1); root.add(s2);
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

class TableOfContents {
    public final String title;
    public final Integer page;
    public TableOfContents(String title, int page) {
        this.title = title;
        this.page  = page;
    }
    @Override public String toString() {
        return title;
    }
}

class TableOfContentsTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final BasicStroke READER = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] {1f}, 0f);
    private String pn;
    private final Point pnPt = new Point();
    private int rxs, rxe;
    private boolean isSynth;
    private final JPanel p = new JPanel(new BorderLayout()) {
        @Override public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (pn != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(isSynth ? getForeground() : getTextNonSelectionColor());
                g2.drawString(pn, pnPt.x - getX(), pnPt.y);
                g2.setStroke(READER);
                g2.drawLine(rxs, pnPt.y, rxe - getX(), pnPt.y);
                g2.dispose();
            }
        }
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = Short.MAX_VALUE;
            return d;
        }
    };
    public TableOfContentsTreeCellRenderer() {
        super();
        p.setOpaque(false);
    }
    @Override public void updateUI() {
        super.updateUI();
        isSynth = getUI().getClass().getName().contains("Synth");
        if (isSynth) {
            //System.out.println("XXX: FocusBorder bug?, JDK 1.7.0, Nimbus start LnF");
            setBackgroundSelectionColor(new Color(0x0, true));
        }
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) value;
            Object o = n.getUserObject();
            if (o instanceof TableOfContents) {
                TableOfContents toc = (TableOfContents) o;
                FontMetrics metrics = l.getFontMetrics(l.getFont());
                int gap = l.getIconTextGap();
                Dimension d = l.getPreferredSize();
                Insets ins = tree.getInsets();

                p.removeAll();
                p.add(l, BorderLayout.WEST);
                if (isSynth) {
                    p.setForeground(l.getForeground());
                }

                pn = String.format("%3d", toc.page);
                pnPt.x = tree.getWidth() - metrics.stringWidth(pn) - gap;
                pnPt.y = l.getBaseline(d.width, d.height);

                rxs = d.width + gap;
                rxe = tree.getWidth() - ins.right - metrics.stringWidth("000") - gap;

                return p;
            }
        }
        pn = null;
        return l;
    }
}

class TableOfContentsTreeCellRenderer1 extends DefaultTreeCellRenderer {
    private static final String READER = "... ";
    private String pn;
    private int pnx = -1, pny = -1;
    private boolean isSynth;
    private final JPanel p = new JPanel(new BorderLayout()) {
        @Override public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (pn != null) {
                g.setColor(isSynth ? getForeground() : getTextNonSelectionColor());
                g.drawString(pn, pnx - getX(), pny);
            }
        }
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = Short.MAX_VALUE;
            return d;
        }
    };
    public TableOfContentsTreeCellRenderer1() {
        super();
        p.setOpaque(false);
    }
    @Override public void updateUI() {
        super.updateUI();
        isSynth = getUI().getClass().getName().contains("Synth");
        if (isSynth) {
            //System.out.println("XXX: FocusBorder bug?, JDK 1.7.0, Nimbus start LnF");
            setBackgroundSelectionColor(new Color(0x0, true));
        }
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) value;
            Object o = n.getUserObject();
            if (o instanceof TableOfContents) {
                TableOfContents toc = (TableOfContents) o;
                FontMetrics metrics = l.getFontMetrics(l.getFont());
                int gap = l.getIconTextGap();

                p.removeAll();
                p.add(l, BorderLayout.WEST);
                if (isSynth) {
                    p.setForeground(l.getForeground());
                }

                pn = String.format("%s%3d", READER, toc.page);
                pnx = tree.getWidth() - metrics.stringWidth(pn) - gap;
                //pnx = Math.max(pnx, titlex + metrics.stringWidth(pair.title) + gap);
                pny = (l.getIcon().getIconHeight() + metrics.getAscent()) / 2;

                return p;
            }
        }
        pn = null;
        return l;
    }
}

class TableOfContentsTree extends JTree {
    private static final BasicStroke READER = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] {1f}, 0f);
    private boolean isSynth;
    public TableOfContentsTree(TreeModel model) {
        super(model);
    }
    @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createTitledBorder("JTree#paintComponent(...)"));
        isSynth = getUI().getClass().getName().contains("Synth");
    }
    private Rectangle getVisibleRowsRect() {
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
        Container container = SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (container instanceof JScrollPane) {
            JScrollPane pane = (JScrollPane) container;
            JScrollBar bar = pane.getHorizontalScrollBar();
            if (bar != null && bar.isVisible()) {
                int height = bar.getHeight();
                visRect.y -= height;
                visRect.height += height;
            }
        }
        return visRect;
    }
    @Override public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        FontMetrics fm = g.getFontMetrics();
        int pnmaxWidth = fm.stringWidth("000");
        Insets ins     = getInsets();
        Rectangle rect = getVisibleRowsRect();
        for (int i = 0; i < getRowCount(); i++) {
            Rectangle r = getRowBounds(i);
            if (rect.intersects(r)) {
                TreePath path = getPathForRow(i);
                TreeCellRenderer tcr = getCellRenderer();
                JComponent c = (JComponent) tcr;
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
                    //int y = (int) (.5 + r.y + (r.height + fm.getAscent()) * .5);
                    int y = r.y + c.getBaseline(r.width, r.height);
                    g2.drawString(pn, x, y);

                    int gap = 5;
                    int x2  = getWidth() - 1 - pnmaxWidth - ins.right;
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
