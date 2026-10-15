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
    Popup p = super.getPopup(popup, x, y);
    if (p != null) {
      // A heavyweight popup (Popup$HeavyWeightWindow) is shown outside the parent frame,
      // so its window background must also be made transparent
      EventQueue.invokeLater(() -> Optional.ofNullable(SwingUtilities.getWindowAncestor(popup))
          .filter(RoundedPopupMenuUI::isTranslucentPopupWindow)
          .ifPresent(w -> w.setBackground(new Color(0x0, true))));
      popup.setBorder(new RoundedBorder());
      popup.setOpaque(false);
    }
    return p;
  }

  private static boolean isTranslucentPopupWindow(Window w) {
    GraphicsConfiguration gc = w.getGraphicsConfiguration();
    return w.getType() == Window.Type.POPUP && gc != null && gc.isTranslucencyCapable();
  }
}

class RoundedBorder extends AbstractBorder {
  private static final int PAD = 5;
  private static final float ARC = PAD * 2f;

  // JComponent#getInsets(Insets) calls AbstractBorder#getBorderInsets(Component, Insets),
  // so override this method (getBorderInsets(Component) delegates to it)
  @Override public Insets getBorderInsets(Component c, Insets insets) {
    insets.set(PAD, PAD, PAD, PAD);
    return insets;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    Shape s = new RoundRectangle2D.Float(x, y, w - 1f, h - 1f, ARC, ARC);
    // Fill the rounded background here, since the JPopupMenu itself is transparent
    g2.setPaint(c.getBackground());
    g2.fill(s);
    g2.setPaint(Color.GRAY);
    g2.draw(s);
    g2.dispose();
  }
}
