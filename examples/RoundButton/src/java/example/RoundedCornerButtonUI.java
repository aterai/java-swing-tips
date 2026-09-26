// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

public final class RoundedCornerButtonUI extends BasicButtonUI {
  private static final double ARC = 16d;
  private static final double FOCUS_STROKE = 2d;
  private static final Color FOCUS_COLOR = new Color(100, 150, 255);
  private static final Color PRESSED_COLOR = new Color(220, 225, 230);
  private static final Color ROLLOVER_COLOR = Color.ORANGE;
  private final Dimension cachedSize = new Dimension();
  private Shape shape;
  private Shape innerShape;

  @Override protected void installDefaults(AbstractButton b) {
    super.installDefaults(b);
    b.setContentAreaFilled(false);
    b.setBorderPainted(false);
    b.setOpaque(false);
    b.setBackground(new Color(245, 250, 255));
    b.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
  }

  @Override public void paint(Graphics g, JComponent c) {
    updateShapeIfResized(c);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // ContentArea
    if (c instanceof AbstractButton) {
      AbstractButton b = (AbstractButton) c;
      ButtonModel model = b.getModel();
      if (model.isArmed()) {
        g2.setPaint(PRESSED_COLOR);
        g2.fill(shape);
      } else if (b.isRolloverEnabled() && model.isRollover()) {
        paintFocusAndRollover(g2, c, ROLLOVER_COLOR);
      } else if (b.hasFocus()) {
        paintFocusAndRollover(g2, c, FOCUS_COLOR);
      } else {
        g2.setPaint(c.getBackground());
        g2.fill(shape);
      }
    }

    // Border
    g2.setPaint(c.getForeground());
    g2.draw(shape);
    g2.dispose();
    super.paint(g, c);
  }

  // JComponent#contains(int, int) delegates to this method, so mouse events
  // (press, rollover, etc.) outside the rounded corners are not dispatched to the button.
  @Override public boolean contains(JComponent c, int x, int y) {
    updateShapeIfResized(c);
    return shape.contains(x, y);
  }

  private void updateShapeIfResized(Component c) {
    if (shape == null || !cachedSize.equals(c.getSize())) {
      c.getSize(cachedSize);
      double w = c.getWidth() - 1d;
      double h = c.getHeight() - 1d;
      double s = FOCUS_STROKE;
      shape = new RoundRectangle2D.Double(0d, 0d, w, h, ARC, ARC);
      innerShape = new RoundRectangle2D.Double(s, s, w - s * 2d, h - s * 2d, ARC, ARC);
    }
  }

  private void paintFocusAndRollover(Graphics2D g2, Component c, Color color) {
    float w = c.getWidth() - 1f;
    float h = c.getHeight() - 1f;
    g2.setPaint(new GradientPaint(0f, 0f, color, w, h, color.brighter(), true));
    g2.fill(shape);
    g2.setPaint(c.getBackground());
    g2.fill(innerShape);
  }
}
