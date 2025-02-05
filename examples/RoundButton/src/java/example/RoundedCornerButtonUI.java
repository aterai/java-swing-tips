// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.plaf.basic.BasicButtonUI;

public final class RoundedCornerButtonUI extends BasicButtonUI {
  private static final double ARC = 16d;
  private static final double FOCUS_STROKE = 2d;
  private static final Color FC = new Color(100, 150, 255);
  private static final Color AC = new Color(220, 225, 230);
  private static final Color RC = Color.ORANGE;
  private Shape shape;
  private Shape border;
  private Shape base;

  @Override protected void installDefaults(AbstractButton b) {
    super.installDefaults(b);
    b.setContentAreaFilled(false);
    b.setBorderPainted(false);
    b.setOpaque(false);
    b.setBackground(new Color(245, 250, 255));
    b.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
    initShape(b);
  }

  @Override protected void installListeners(AbstractButton button) {
    BasicButtonListener listener = new BasicButtonListener(button) {
      @Override public void mousePressed(MouseEvent e) {
        AbstractButton b = (AbstractButton) e.getComponent();
        initShape(b);
        if (isShapeContains(e.getPoint())) {
          super.mousePressed(e);
        }
      }

      @Override public void mouseEntered(MouseEvent e) {
        if (isShapeContains(e.getPoint())) {
          super.mouseEntered(e);
        }
      }

      @Override public void mouseMoved(MouseEvent e) {
        if (isShapeContains(e.getPoint())) {
          super.mouseEntered(e);
        } else {
          super.mouseExited(e);
        }
      }
    };
    // if (listener != null)
    button.addMouseListener(listener);
    button.addMouseMotionListener(listener);
    button.addFocusListener(listener);
    button.addPropertyChangeListener(listener);
    button.addChangeListener(listener);
  }

  @Override public void paint(Graphics g, JComponent c) {
    initShape(c);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // ContentArea
    if (c instanceof AbstractButton) {
      AbstractButton b = (AbstractButton) c;
      ButtonModel model = b.getModel();
      if (model.isArmed()) {
        g2.setPaint(AC);
        g2.fill(shape);
      } else if (b.isRolloverEnabled() && model.isRollover()) {
        paintFocusAndRollover(g2, c, RC);
      } else if (b.hasFocus()) {
        paintFocusAndRollover(g2, c, FC);
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

  public boolean isShapeContains(Point pt) {
    return shape != null && shape.contains(pt);
  }

  public void initShape(Component c) {
    if (!c.getBounds().equals(base)) {
      base = c.getBounds();
      double w = c.getWidth() - 1d;
      double h = c.getHeight() - 1d;
      double s = FOCUS_STROKE;
      shape = new RoundRectangle2D.Double(0d, 0d, w, h, ARC, ARC);
      border = new RoundRectangle2D.Double(s, s, w - s * 2d, h - s * 2d, ARC, ARC);
    }
  }

  public void paintFocusAndRollover(Graphics2D g2, Component c, Color color) {
    float w = c.getWidth() - 1f;
    float h = c.getHeight() - 1f;
    g2.setPaint(new GradientPaint(0f, 0f, color, w, h, color.brighter(), true));
    g2.fill(shape);
    g2.setPaint(c.getBackground());
    g2.fill(border);
  }
}
