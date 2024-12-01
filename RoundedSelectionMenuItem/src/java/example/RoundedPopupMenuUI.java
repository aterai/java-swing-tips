// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Optional;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;

public final class RoundedPopupMenuUI extends BasicPopupMenuUI {
  public static ComponentUI createUI(JComponent c) {
    return new RoundedPopupMenuUI();
  }

  @Override public Popup getPopup(JPopupMenu popup, int x, int y) {
    Popup pp = super.getPopup(popup, x, y);
    if (pp != null) {
      EventQueue.invokeLater(() -> Optional.ofNullable(SwingUtilities.getWindowAncestor(popup))
          .filter(w -> {
            boolean isHeavyWeight = w.getType() == Window.Type.POPUP;
            GraphicsConfiguration gc = w.getGraphicsConfiguration();
            return gc != null && gc.isTranslucencyCapable() && isHeavyWeight;
          })
          .ifPresent(w -> w.setBackground(new Color(0x0, true))));
      popup.setBorder(new RoundedBorder());
      popup.setOpaque(false);
    }
    return pp;
  }
}

class RoundedBorder extends AbstractBorder {
  @Override public Insets getBorderInsets(Component c) {
    return new Insets(5, 5, 5, 5);
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(c.getBackground());
    Shape s = makeShape((JComponent) c);
    g2.fill(s);
    g2.setPaint(Color.GRAY);
    g2.draw(s);
    g2.dispose();
  }

  private static Shape makeShape(JComponent c) {
    float w = c.getWidth() - 1f;
    float h = c.getHeight() - 1f;
    Insets i = c.getInsets();
    float r = Math.min(i.top + i.left, i.bottom + i.right);
    return new RoundRectangle2D.Float(0f, 0f, w, h, r, r);
  }
}
