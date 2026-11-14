// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;

public final class CustomPopupMenuUI extends BasicPopupMenuUI {
  public static ComponentUI createUI(JComponent c) {
    return new CustomPopupMenuUI();
  }

  @Override public Popup getPopup(JPopupMenu popup, int x, int y) {
    Popup pp = super.getPopup(popup, x, y);
    if (pp != null) {
      // Make the heavyweight popup window translucent after the popup has been shown
      EventQueue.invokeLater(() -> {
        Window w = SwingUtilities.getWindowAncestor(popup);
        if (isTranslucentPopupWindow(w)) {
          w.setBackground(new Color(0x0, true));
        }
      });
      Container c = SwingUtilities.getUnwrappedParent(popup);
      if (c instanceof JComponent) {
        JComponent panel = (JComponent) c;
        panel.setBorder(new ShadowBorder());
        panel.setOpaque(false);
      }
    }
    return pp;
  }

  // A lightweight popup returns the JFrame here, which must remain opaque
  private static boolean isTranslucentPopupWindow(Window w) {
    boolean isHeavyWeight = w != null && w.getType() == Window.Type.POPUP;
    GraphicsConfiguration gc = isHeavyWeight ? w.getGraphicsConfiguration() : null;
    return gc != null && gc.isTranslucencyCapable();
  }

  private static final class ShadowBorder extends AbstractBorder {
    private static final int SIZE = 4;
    private static final int ARC = 2;
    private static final float ALPHA = .12f;

    @Override public Insets getBorderInsets(Component c) {
      return new Insets(0, 0, SIZE, SIZE);
    }

    @Override public Insets getBorderInsets(Component c, Insets insets) {
      insets.set(0, 0, SIZE, SIZE);
      return insets;
    }

    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setComposite(AlphaComposite.SrcOver.derive(ALPHA));
      g2.setPaint(Color.BLACK);
      g2.translate(x, y);
      // Overlap translucent rectangles while expanding them one pixel at a time,
      // so that the shadow gets lighter toward the bottom right edge
      for (int i = 0; i < SIZE; i++) {
        g2.fillRoundRect(SIZE, SIZE, w - SIZE - SIZE + i, h - SIZE - SIZE + i, ARC, ARC);
      }
      g2.dispose();
    }
  }
}
