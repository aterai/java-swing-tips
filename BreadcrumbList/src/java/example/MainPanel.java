package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        Container breadcrumb = makeContainer(10 + 1);

        JTree tree = new JTree();
        tree.setSelectionRow(0);
        tree.addTreeSelectionListener(e -> {
            Object o = tree.getLastSelectedPathComponent();
            if (o instanceof MutableTreeNode && !((MutableTreeNode) o).isLeaf()) {
                initBreadcrumbList(breadcrumb, tree);
                breadcrumb.revalidate();
                breadcrumb.repaint();
            }
        });

        initBreadcrumbList(breadcrumb, tree);
        add(new JLayer<>(breadcrumb, new BreadcrumbLayerUI<>()), BorderLayout.NORTH);

        Component c = makeBreadcrumbList(Arrays.asList("aaa", "bb", "c"));
        add(c, BorderLayout.SOUTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Container makeContainer(int overlap) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING, -overlap, 0)) {
            @Override public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        p.setBorder(BorderFactory.createEmptyBorder(4, overlap + 4, 4, 4));
        p.setOpaque(false);
        return p;
    }
    private static void initBreadcrumbList(Container p, JTree tree) {
        p.removeAll();
        ButtonGroup bg = new ButtonGroup();
        Object[] paths = tree.getSelectionPath().getPath();
        for (int i = 0; i < paths.length; i++) {
            TreePath cur = new TreePath(Arrays.copyOf(paths, i + 1));
            AbstractButton b = makeButton(tree, cur, Color.ORANGE);
            p.add(b);
            bg.add(b);
        }
    }
    private static Component makeBreadcrumbList(List<String> list) {
        Container p = makeContainer(5 + 1);
        ButtonGroup bg = new ButtonGroup();
        list.forEach(title -> {
            AbstractButton b = makeButton(null, new TreePath(title), Color.PINK);
            p.add(b);
            bg.add(b);
        });
        return p;
    }
    private static AbstractButton makeButton(JTree tree, TreePath path, Color color) {
        AbstractButton b = new JRadioButton(path.getLastPathComponent().toString()) {
            @Override public boolean contains(int x, int y) {
                Icon i = getIcon();
                if (i instanceof ArrowToggleButtonBarCellIcon) {
                    Shape s = ((ArrowToggleButtonBarCellIcon) i).getShape();
                    if (Objects.nonNull(s)) {
                        return s.contains(x, y);
                    }
                }
                return super.contains(x, y);
            }
        };
        if (Objects.nonNull(tree)) {
            b.addActionListener(e -> {
                JRadioButton r = (JRadioButton) e.getSource();
                tree.setSelectionPath(path);
                r.setSelected(true);
            });
        }
        b.setIcon(new ArrowToggleButtonBarCellIcon());
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

// https://ateraimemo.com/Swing/ToggleButtonBar.html
class ArrowToggleButtonBarCellIcon implements Icon {
    public static final int TH = 10; // The height of a triangle
    private static final int HEIGHT = TH * 2 + 1;
    private static final int WIDTH = 100;
    private Shape shape;
    public Shape getShape() {
        return shape;
    }
    protected Shape makeShape(Container parent, Component c, int x, int y) {
        int w = c.getWidth() - 1;
        int h = c.getHeight() - 1;
        int h2 = (int) (h * .5 + .5);
        int w2 = TH;
        Path2D p = new Path2D.Double();
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
        if (Objects.isNull(parent)) {
            return;
        }
        shape = makeShape(parent, c, x, y);

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
        g2.setPaint(bgc);
        g2.fill(shape);
        g2.setPaint(borderColor);
        g2.draw(shape);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return WIDTH;
    }
    @Override public int getIconHeight() {
        return HEIGHT;
    }
}

class BreadcrumbLayerUI<V extends Component> extends LayerUI<V> {
    private Shape shape;
    @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        Optional.ofNullable(shape).ifPresent(s -> {
            Graphics2D g2 = (Graphics2D) g.create();
            // g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(Color.GRAY);
            g2.draw(shape);
            g2.dispose();
        });
    }
    @Override public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
            ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        }
    }
    @Override public void uninstallUI(JComponent c) {
        if (c instanceof JLayer) {
            ((JLayer<?>) c).setLayerEventMask(0);
        }
        super.uninstallUI(c);
    }
    private void update(MouseEvent e, JLayer<? extends V> l) {
        Shape s = null;
        switch (e.getID()) {
            case MouseEvent.MOUSE_ENTERED:
            case MouseEvent.MOUSE_MOVED:
                Component c = e.getComponent();
                if (c instanceof AbstractButton) {
                    AbstractButton b = (AbstractButton) c;
                    if (b.getIcon() instanceof ArrowToggleButtonBarCellIcon) {
                        ArrowToggleButtonBarCellIcon icon = (ArrowToggleButtonBarCellIcon) b.getIcon();
                        Rectangle r = c.getBounds();
                        AffineTransform at = AffineTransform.getTranslateInstance(r.x, r.y);
                        s = at.createTransformedShape(icon.getShape());
                    }
                }
                break;
            default:
                break;
        }
        if (!Objects.equals(s, shape)) {
            shape = s;
            l.getView().repaint();
        }
    }
    @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends V> l) {
        update(e, l);
    }
    @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends V> l) {
        update(e, l);
    }
}
