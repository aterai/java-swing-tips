// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    List<String> list = Arrays.asList("aaa", "bb", "c");

    JPanel p1 = new JPanel(new GridLayout(0, 1));
    p1.setBorder(BorderFactory.createTitledBorder("Icon border"));
    p1.add(makeBreadcrumb(list, Color.PINK, 1));
    p1.add(makeChevronBreadcrumb(list, Color.PINK, 11));
    p1.add(makeRibbonBreadcrumb(list, Color.PINK, 11));

    BreadcrumbLayerUI<Component> layerUI = new BreadcrumbLayerUI<>();
    JPanel p2 = new JPanel(new GridLayout(0, 1));
    p2.setBorder(BorderFactory.createTitledBorder("JLayer border"));
    p2.add(new JLayer<>(makeBreadcrumb(list, Color.ORANGE, 1), layerUI));
    p2.add(new JLayer<>(makeChevronBreadcrumb(list, Color.ORANGE, 11), layerUI));
    p2.add(new JLayer<>(makeRibbonBreadcrumb(list, Color.ORANGE, 11), layerUI));

    JPanel p = new JPanel(new GridLayout(0, 1));
    p.add(p1);
    p.add(p2);
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makePanel(int overlap) {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING, -overlap, 0)) {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false;
      }
    };
    p.setOpaque(false);
    return p;
  }

  public static JPanel makeBreadcrumb(List<String> list, Color color, int overlap) {
    JPanel p = makePanel(overlap);
    p.setBorder(BorderFactory.createEmptyBorder(5, overlap + 5, 5, 5));
    ButtonGroup bg = new ButtonGroup();
    list.forEach(title -> {
      AbstractButton b = makeButton(title, new SizeIcon(), color);
      p.add(b);
      bg.add(b);
    });
    return p;
  }

  public static JPanel makeChevronBreadcrumb(List<String> list, Color color, int overlap) {
    JPanel p = makePanel(overlap);
    p.setBorder(BorderFactory.createEmptyBorder(5, overlap + 5, 5, 5));
    ButtonGroup bg = new ButtonGroup();
    list.forEach(title -> {
      AbstractButton b = makeButton(title, new ArrowToggleButtonIcon(), color);
      p.add(b);
      bg.add(b);
    });
    return p;
  }

  public static JPanel makeRibbonBreadcrumb(List<String> list, Color color, int overlap) {
    JPanel p = makePanel(overlap);
    p.setBorder(BorderFactory.createEmptyBorder(5, overlap + 5, 5, 5));
    ButtonGroup bg = new ButtonGroup();
    list.forEach(title -> {
      AbstractButton b = makeButton(title, new RibbonToggleButtonIcon(), color);
      p.add(b);
      bg.add(b);
    });
    return p;
  }

  private static AbstractButton makeButton(String title, Icon icon, Color color) {
    AbstractButton b = new JRadioButton(title) {
      @Override public boolean contains(int x, int y) {
        return Optional.ofNullable(getIcon())
            .filter(ArrowToggleButtonIcon.class::isInstance)
            .map(i -> ((ArrowToggleButtonIcon) i).getShape())
            .map(s -> s.contains(x, y))
            .orElseGet(() -> super.contains(x, y));
      }
    };
    b.setIcon(icon);
    b.setContentAreaFilled(false);
    b.setBorder(BorderFactory.createEmptyBorder());
    // b.setVerticalAlignment(SwingConstants.CENTER);
    // b.setVerticalTextPosition(SwingConstants.CENTER);
    // b.setHorizontalAlignment(SwingConstants.CENTER);
    b.setHorizontalTextPosition(SwingConstants.CENTER);
    b.setFocusPainted(false);
    b.setOpaque(false);
    b.setBackground(color);
    return b;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
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
class ArrowToggleButtonIcon implements Icon {
  public static final int TH = 10; // The height of a triangle
  private static final int HEIGHT = TH * 2 + 1;
  private static final int WIDTH = 100;
  private Shape shape;

  public Shape getShape() {
    return shape;
  }

  protected Shape makeShape(Container parent, Component c, int x, int y) {
    double w = c.getWidth() - 1d;
    double h = c.getHeight() - 1d;
    double h2 = h * .5;
    double w2 = TH;
    Path2D p = new Path2D.Double();
    p.moveTo(0d, 0d);
    p.lineTo(w - w2, 0d);
    p.lineTo(w, h2);
    p.lineTo(w - w2, h);
    p.lineTo(0d, h);
    if (!Objects.equals(c, parent.getComponent(0))) {
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
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

class SizeIcon extends ArrowToggleButtonIcon {
  @Override protected Shape makeShape(Container parent, Component c, int x, int y) {
    double w = c.getWidth() - 1d;
    double h = c.getHeight() - 1d;
    Path2D p = new Path2D.Double();
    p.moveTo(0d, 0d);
    p.lineTo(w, 0d);
    p.lineTo(w, h);
    p.lineTo(0d, h);
    p.closePath();
    return AffineTransform.getTranslateInstance(x, y).createTransformedShape(p);
  }
}

class RibbonToggleButtonIcon extends ArrowToggleButtonIcon {
  @Override protected Shape makeShape(Container parent, Component c, int x, int y) {
    double w = c.getWidth() - 1d;
    double h = c.getHeight() - 1d;
    double h2 = h * .5;
    Path2D p = new Path2D.Double();
    p.moveTo(w - h2, 0d);
    p.quadTo(w, 0d, w, h2);
    p.quadTo(w, 0d + h, w - h2, h);
    if (Objects.equals(c, parent.getComponent(0))) {
      // :first-child
      double r = 4d;
      p.lineTo(r, h);
      p.quadTo(0d, h, 0d, h - r);
      p.lineTo(0d, r);
      p.quadTo(0d, 0d, r, 0d);
    } else {
      p.lineTo(0d, h);
      p.quadTo(h2, h, h2, h2);
      p.quadTo(h2, 0d, 0d, 0d);
    }
    p.closePath();
    return AffineTransform.getTranslateInstance(x, y).createTransformedShape(p);
  }
}

class BreadcrumbLayerUI<V extends Component> extends LayerUI<V> {
  private transient Shape shape;

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (Objects.nonNull(shape)) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      Rectangle r = new Rectangle(c.getWidth(), c.getHeight());
      Area area = new Area(r);
      area.subtract(new Area(shape));
      g2.setClip(area);

      g2.setPaint(new Color(0x55_66_66_66, true));
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
      ((JLayer<?>) c).setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  private void update(MouseEvent e, JLayer<? extends V> l) {
    int id = e.getID();
    Shape s = null;
    if (id == MouseEvent.MOUSE_ENTERED || id == MouseEvent.MOUSE_MOVED) {
      Component c = e.getComponent();
      if (c instanceof AbstractButton) {
        AbstractButton b = (AbstractButton) c;
        if (b.getIcon() instanceof ArrowToggleButtonIcon) {
          ArrowToggleButtonIcon icon = (ArrowToggleButtonIcon) b.getIcon();
          Rectangle r = c.getBounds();
          AffineTransform at = AffineTransform.getTranslateInstance(r.x, r.y);
          s = at.createTransformedShape(icon.getShape());
        }
      }
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
