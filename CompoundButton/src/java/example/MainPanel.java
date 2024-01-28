// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.Optional;
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
  private final double degree;

  ButtonLocation(double degree) {
    this.degree = degree;
  }

  public double getStartAngle() {
    return degree;
  }
}

class CompoundButton extends JButton {
  protected transient Shape shape;
  protected transient Shape base;
  protected final ButtonLocation bl;
  protected final Dimension dim;

  protected CompoundButton(Dimension d, ButtonLocation bl) {
    super();
    this.dim = d;
    this.bl = bl;
    setIcon(new Icon() {
      private final Color fc = new Color(100, 150, 255, 200);
      private final Color ac = new Color(230, 230, 230);
      private final Color rc = new Color(255, 165, 100);
      @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (getModel().isArmed()) {
          g2.setPaint(ac);
          g2.fill(shape);
        } else if (isRolloverEnabled() && getModel().isRollover()) {
          paintFocusAndRollover(g2, rc);
        } else if (hasFocus()) {
          paintFocusAndRollover(g2, fc);
        } else {
          g2.setPaint(getBackground());
          g2.fill(shape);
        }
        g2.dispose();
      }

      @Override public int getIconWidth() {
        return dim.width;
      }

      @Override public int getIconHeight() {
        return dim.height;
      }
    });
    initShape();
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

  @Override public final Rectangle getBounds() {
    return super.getBounds();
  }

  private void initShape() {
    Rectangle rect = getBounds();
    if (!rect.equals(base)) {
      base = rect;
      double ww = rect.width * .5;
      double xx = ww * .5;
      Shape inner = new Ellipse2D.Double(xx, xx, ww, ww);
      if (ButtonLocation.CENTER == bl) {
        shape = inner;
      } else {
        // TEST: parent.isOptimizedDrawingEnabled: false
        double dw = rect.width - 2d;
        double dh = rect.height - 2d;
        Shape outer = new Arc2D.Double(1d, 1d, dw, dh, bl.getStartAngle(), 90d, Arc2D.PIE);
        Area area = new Area(outer);
        area.subtract(new Area(inner));
        shape = area;
      }
    }
  }

  protected void paintFocusAndRollover(Graphics2D g2, Color color) {
    float x2 = getWidth() - 1f;
    float y2 = getHeight() - 1f;
    g2.setPaint(new GradientPaint(0f, 0f, color, x2, y2, color.brighter(), true));
    g2.fill(shape);
    g2.setPaint(getBackground());
  }

  @Override protected void paintComponent(Graphics g) {
    initShape();
    super.paintComponent(g);
  }

  @Override protected void paintBorder(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(Color.GRAY);
    g2.draw(shape);
    g2.dispose();
  }

  @Override public boolean contains(int x, int y) {
    // return shape != null && shape.contains(x, y);
    return Optional.ofNullable(shape)
        .map(s -> s.contains(x, y))
        .orElseGet(() -> super.contains(x, y));
  }
}
