// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    Dimension d = new Dimension(64, 64);
    add(new CompoundButton(d, ButtonLocation.NORTH));
    add(new CompoundButton(d, ButtonLocation.SOUTH));
    add(new CompoundButton(d, ButtonLocation.EAST));
    add(new CompoundButton(d, ButtonLocation.WEST));
    add(new CompoundButton(d, ButtonLocation.CENTER));
    add(new CompoundButtonPanel(d));
    setPreferredSize(new Dimension(320, 240));
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
      Logger.getGlobal().severe(ex::getMessage);
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

class CompoundButtonPanel extends JPanel {
  private final Dimension dim;

  protected CompoundButtonPanel(Dimension dim) {
    super();
    setLayout(new OverlayLayout(this));
    this.dim = dim;
    add(new CompoundButton(dim, ButtonLocation.CENTER));
    add(new CompoundButton(dim, ButtonLocation.NORTH));
    add(new CompoundButton(dim, ButtonLocation.SOUTH));
    add(new CompoundButton(dim, ButtonLocation.EAST));
    add(new CompoundButton(dim, ButtonLocation.WEST));
  }

  @Override public final void setLayout(LayoutManager mgr) {
    super.setLayout(mgr);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public Dimension getPreferredSize() {
    return dim;
  }

  @Override public boolean isOptimizedDrawingEnabled() {
    return false;
  }
}

enum ButtonLocation {
  CENTER(0d), NORTH(45d), EAST(135d), SOUTH(225d), WEST(-45d);
  private final double startAngle;

  ButtonLocation(double startAngle) {
    this.startAngle = startAngle;
  }

  /* default */ double getStartAngle() {
    return startAngle;
  }
}

class CompoundButton extends JButton {
  private transient Shape shape;
  private final Dimension shapeSize = new Dimension();
  private final ButtonLocation location;
  private final Dimension dim;

  protected CompoundButton(Dimension d, ButtonLocation location) {
    super();
    this.dim = d;
    this.location = location;
    setIcon(new ShapeIcon());
  }

  @Override public void updateUI() {
    super.updateUI();
    setFocusPainted(false);
    setContentAreaFilled(false);
    setBackground(new Color(0xFA_FA_FA));
  }

  @Override public Dimension getPreferredSize() {
    return dim;
  }

  @Override public final void setIcon(Icon defaultIcon) {
    super.setIcon(defaultIcon);
  }

  // Create the shape lazily and rebuild it only when the size changes,
  // so that contains(...) also works before the first paint
  private Shape getShape() {
    int w = getWidth();
    int h = getHeight();
    if (shape == null || shapeSize.width != w || shapeSize.height != h) {
      shapeSize.setSize(w, h);
      shape = createShape(w, h);
    }
    return shape;
  }

  private Shape createShape(int w, int h) {
    double ww = w * .5;
    double xx = ww * .5;
    Shape inner = new Ellipse2D.Double(xx, xx, ww, ww);
    Shape s;
    if (ButtonLocation.CENTER == location) {
      s = inner;
    } else {
      // The parent panel must return false from isOptimizedDrawingEnabled()
      // because the bounds of these buttons overlap each other
      Shape outer = new Arc2D.Double(
          1d, 1d, w - 2d, h - 2d, location.getStartAngle(), 90d, Arc2D.PIE);
      Area area = new Area(outer);
      area.subtract(new Area(inner));
      s = area;
    }
    return s;
  }

  private void paintFocusAndRollover(Graphics2D g2, Color color) {
    float x2 = getWidth() - 1f;
    float y2 = getHeight() - 1f;
    g2.setPaint(new GradientPaint(0f, 0f, color, x2, y2, color.brighter(), true));
    g2.fill(getShape());
  }

  @Override protected void paintBorder(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(Color.GRAY);
    g2.draw(getShape());
    g2.dispose();
  }

  @Override public boolean contains(int x, int y) {
    return getShape().contains(x, y);
  }

  private final class ShapeIcon implements Icon {
    private final Color focusColor = new Color(100, 150, 255, 200);
    private final Color armedColor = new Color(230, 230, 230);
    private final Color rolloverColor = new Color(255, 165, 100);

    // The shape is in the button's coordinate space, so x and y are ignored
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (getModel().isArmed()) {
        g2.setPaint(armedColor);
        g2.fill(getShape());
      } else if (isRolloverEnabled() && getModel().isRollover()) {
        paintFocusAndRollover(g2, rolloverColor);
      } else if (hasFocus()) {
        paintFocusAndRollover(g2, focusColor);
      } else {
        g2.setPaint(getBackground());
        g2.fill(getShape());
      }
      g2.dispose();
    }

    @Override public int getIconWidth() {
      return dim.width;
    }

    @Override public int getIconHeight() {
      return dim.height;
    }
  }
}
