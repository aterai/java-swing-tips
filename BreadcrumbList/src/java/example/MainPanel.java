package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final JPanel breadcrumb = makePanel(10 + 1);
    private final JTree tree = new JTree();
    public MainPanel() {
        super(new BorderLayout());
        tree.setSelectionRow(0);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node == null || node.isLeaf()) {
                    return;
                } else {
                    initBreadcrumbList(breadcrumb, tree);
                    breadcrumb.revalidate();
                    breadcrumb.repaint();
                }
            }
        });

        initBreadcrumbList(breadcrumb, tree);
        add(new JLayer<JPanel>(breadcrumb, new BreadcrumbLayerUI()), BorderLayout.NORTH);

        JComponent c = makeBreadcrumbList(Arrays.asList("aaa", "bb", "c"));
        add(c, BorderLayout.SOUTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPanel makePanel(int overlap) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING, -overlap, 0));
        p.setBorder(BorderFactory.createEmptyBorder(4, overlap + 4, 4, 4));
        p.setOpaque(false);
        return p;
    }
    private static void initBreadcrumbList(JPanel p, JTree tree) {
        p.removeAll();
        ButtonGroup bg = new ButtonGroup();
        TreePath tp = tree.getSelectionPath();
        Object[] paths = tp.getPath();
        for (int i = 0; i < paths.length; i++) {
            TreePath cur = new TreePath(Arrays.copyOf(paths, i + 1));
            AbstractButton b = makeButton(tree, cur, Color.ORANGE);
            p.add(b);
            bg.add(b);
        }
    }
    private static JComponent makeBreadcrumbList(List<String> list) {
        JPanel p = makePanel(5 + 1);
        ButtonGroup bg = new ButtonGroup();
        for (String title: list) {
            AbstractButton b = makeButton(null, new TreePath(title), Color.PINK);
            p.add(b);
            bg.add(b);
        }
        return p;
    }
    private static AbstractButton makeButton(final JTree tree, final TreePath path, Color color) {
        final ToggleButtonBarCellIcon icon = new ToggleButtonBarCellIcon();
        AbstractButton b = new JRadioButton(path.getLastPathComponent().toString()) {
            @Override public boolean contains(int x, int y) {
                if (icon == null || icon.area == null) {
                    return super.contains(x, y);
                } else {
                    return icon.area.contains(x, y);
                }
            }
        };
        if (tree != null) {
            b.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    JRadioButton r = (JRadioButton) e.getSource();
                    tree.setSelectionPath(path);
                    r.setSelected(true);
                }
            });
        }
        b.setIcon(icon);
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setVerticalAlignment(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.CENTER);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBackground(color);
        return b;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

//http://terai.xrea.jp/Swing/ToggleButtonBar.html
class ToggleButtonBarCellIcon implements Icon, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int W = 10;
    private static final int H = 21;
    public Shape area;
    public Shape getShape(Container parent, Component c, int x, int y) {
        int w = c.getWidth()  - 1;
        int h = c.getHeight() - 1;
        int h2 = (int) (h * .5 + .5);
        int w2 = W;
        Path2D.Float p = new Path2D.Float();
        p.moveTo(0,      0);
        p.lineTo(w - w2, 0);
        p.lineTo(w,      h2);
        p.lineTo(w - w2, h);
        p.lineTo(0,      h);
        if (c != parent.getComponent(0)) {
            p.lineTo(w2, h2);
        }
        p.closePath();
        return AffineTransform.getTranslateInstance(x, y).createTransformedShape(p);
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Container parent = c.getParent();
        if (parent == null) {
            return;
        }
        area = getShape(parent, c, x, y);

        Color bgc = parent.getBackground();
        Color borderColor = Color.GRAY.brighter();
        if (c instanceof AbstractButton) {
            ButtonModel m = ((AbstractButton) c).getModel();
            if (m.isSelected() || m.isRollover()) {
                bgc = c.getBackground();
                borderColor = Color.GRAY;
            }
        }

        Graphics2D g2 = (Graphics2D) g.create();
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(bgc);
        g2.fill(area);
        g2.setPaint(borderColor);
        g2.draw(area);
        g2.dispose();
    }
    @Override public int getIconWidth()  {
        return 100;
    }
    @Override public int getIconHeight() {
        return H;
    }
}

class BreadcrumbLayerUI extends LayerUI<JPanel> {
    private Shape shape;
    @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (shape != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(Color.GRAY);
            g2.draw(shape);
            g2.dispose();
        }
    }
    @Override public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
            ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        }
    }
    @Override public void uninstallUI(JComponent c) {
        if (c instanceof JLayer) {
            ((JLayer) c).setLayerEventMask(0);
        }
        super.uninstallUI(c);
    }
    private void update(MouseEvent e, JLayer<? extends JPanel> l) {
        int id = e.getID();
        Shape s = null;
        if (id == MouseEvent.MOUSE_ENTERED || id == MouseEvent.MOUSE_MOVED) {
            Component c = e.getComponent();
            if (c instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) c;
                if (b.getIcon() instanceof ToggleButtonBarCellIcon) {
                    ToggleButtonBarCellIcon icon = (ToggleButtonBarCellIcon) b.getIcon();
                    Rectangle r = c.getBounds();
                    AffineTransform at = AffineTransform.getTranslateInstance(r.x, r.y);
                    s = at.createTransformedShape(icon.area);
                }
            }
        }
        if (!Objects.equals(s, shape)) {
            shape = s;
            l.getView().repaint();
        }
    }
    @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JPanel> l) {
        update(e, l);
    }
    @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JPanel> l) {
        update(e, l);
    }
}
