package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        List<String> list = Arrays.asList("aaa", "bb", "c");

        JPanel p1 = new JPanel(new GridLayout(0, 1));
        p1.setBorder(BorderFactory.createTitledBorder("Icon border"));
        p1.add(makeBreadcrumbList0(list, Color.PINK,  1));
        p1.add(makeBreadcrumbList1(list, Color.PINK, 11));
        p1.add(makeBreadcrumbList2(list, Color.PINK, 11));

        JPanel p2 = new JPanel(new GridLayout(0, 1));
        p2.setBorder(BorderFactory.createTitledBorder("JLayer border"));
        p2.add(new JLayer<JPanel>(makeBreadcrumbList0(list, Color.ORANGE,  1), new BreadcrumbLayerUI()));
        p2.add(new JLayer<JPanel>(makeBreadcrumbList1(list, Color.ORANGE, 11), new BreadcrumbLayerUI()));
        p2.add(new JLayer<JPanel>(makeBreadcrumbList2(list, Color.ORANGE, 11), new BreadcrumbLayerUI()));

        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(p1); p.add(p2);
        add(p, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPanel makePanel(int overlap) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING, -overlap, 0));
        p.setOpaque(false);
        return p;
    }
    private static JPanel makeBreadcrumbList0(List<String> list, Color color, int overlap) {
        JPanel p = makePanel(overlap);
        p.setBorder(BorderFactory.createEmptyBorder(5, overlap + 5, 5, 5));
        ButtonGroup bg = new ButtonGroup();
        for (String title: list) {
            AbstractButton b = makeButton(title, new SizeIcon(), color);
            p.add(b);
            bg.add(b);
        }
        return p;
    }
    private static JPanel makeBreadcrumbList1(List<String> list, Color color, int overlap) {
        JPanel p = makePanel(overlap);
        p.setBorder(BorderFactory.createEmptyBorder(5, overlap + 5, 5, 5));
        ButtonGroup bg = new ButtonGroup();
        for (String title: list) {
            AbstractButton b = makeButton(title, new ToggleButtonBarCellIcon(), color);
            p.add(b);
            bg.add(b);
        }
        return p;
    }
    private static JPanel makeBreadcrumbList2(List<String> list, Color color, int overlap) {
        JPanel p = makePanel(overlap);
        p.setBorder(BorderFactory.createEmptyBorder(5, overlap + 5, 5, 5));
        ButtonGroup bg = new ButtonGroup();
        for (String title: list) {
            AbstractButton b = makeButton(title, new ToggleButtonBarCellIcon2(), color);
            p.add(b);
            bg.add(b);
        }
        return p;
    }
    private static AbstractButton makeButton(String title, Icon icon, Color color) {
        AbstractButton b = new JRadioButton(title);
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
class ToggleButtonBarCellIcon implements Icon {
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
        if (c!=parent.getComponent(0)) {
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

class SizeIcon extends ToggleButtonBarCellIcon {
    @Override public Shape getShape(Container parent, Component c, int x, int y) {
        int w = c.getWidth()  - 1;
        int h = c.getHeight() - 1;
        Path2D.Float p = new Path2D.Float();
        p.moveTo(0, 0);
        p.lineTo(w, 0);
        p.lineTo(w, h);
        p.lineTo(0, h);
        p.closePath();
        return AffineTransform.getTranslateInstance(x, y).createTransformedShape(p);
    }
}

class ToggleButtonBarCellIcon2 extends ToggleButtonBarCellIcon {
    public Shape getShape(Container parent, Component c, int x, int y) {
        int r = 4;
        int w = c.getWidth()  - 1;
        int h = c.getHeight() - 1;
        int h2 = (int) (h * .5 + .5);
        Path2D.Float p = new Path2D.Float();
        p.moveTo(w - h2, 0);
        p.quadTo(w,      0,      w,      h2);
        p.quadTo(w,      0 + h,  w - h2, h);
        if (c == parent.getComponent(0)) {
            //:first-child
            p.lineTo(r, h);
            p.quadTo(0, h, 0, h - r);
            p.lineTo(0, r);
            p.quadTo(0, 0, r, 0);
        } else {
            p.lineTo(0,  h);
            p.quadTo(h2, h, h2, h2);
            p.quadTo(h2, 0, 0,  0);
        }
        p.closePath();
        return AffineTransform.getTranslateInstance(x, y).createTransformedShape(p);
    }
}

class BreadcrumbLayerUI extends LayerUI<JPanel> {
    private Shape shape;
    @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (shape != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle r = new Rectangle(0, 0, c.getWidth(), c.getHeight());
            Area area = new Area(r);
            area.subtract(new Area(shape));
            g2.setClip(area);

            g2.setPaint(new Color(0x55666666, true));
            g2.setStroke(new BasicStroke(3f));
            g2.draw(shape);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(shape);

            g2.setStroke(new BasicStroke(1f));
            g2.setClip(r);
            g2.setPaint(Color.WHITE);
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
