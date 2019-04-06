// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

public class TriangleArrowButton extends JButton {
  private static Icon triangleIcon = new TriangleIcon();

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (getModel().isArmed()) {
      g2.setPaint(new Color(0xDC_DC_DC));
    } else if (isRolloverEnabled() && getModel().isRollover()) {
      g2.setPaint(new Color(0xDC_DC_DC));
    } else if (hasFocus()) {
      g2.setPaint(new Color(0xDC_DC_DC));
    } else {
      g2.setPaint(getBackground());
    }
    Rectangle r = getBounds();
    r.grow(1, 1);
    g2.fill(r);
    g2.dispose();

    super.paintComponent(g);
    Insets i = getInsets();
    int x = r.width - i.right - triangleIcon.getIconWidth() - 2;
    int y = i.top + (r.height - i.top - i.bottom - triangleIcon.getIconHeight()) / 2;
    triangleIcon.paintIcon(this, g, x, y);
  }

  @Override public Dimension getPreferredSize() {
    Insets i = getInsets();
    Icon favicon = getIcon();
    int fw = Objects.nonNull(favicon) ? favicon.getIconWidth() : 16;
    int w = fw + triangleIcon.getIconWidth() + i.left + i.right;
    return new Dimension(w, w);
  }

  @Override public void setBorder(Border border) {
    if (border instanceof CompoundBorder) {
      super.setBorder(border);
    }
  }
}

class TriangleIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.GRAY);
    g2.drawLine(2, 3, 6, 3);
    g2.drawLine(3, 4, 5, 4);
    g2.drawLine(4, 5, 4, 5);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 9;
  }

  @Override public int getIconHeight() {
    return 9;
  }
}
