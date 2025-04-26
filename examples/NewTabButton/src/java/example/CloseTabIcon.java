// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

class CloseTabIcon implements Icon {
  private final Color color;

  protected CloseTabIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.drawLine(2, 2, 9, 9);
    g2.drawLine(2, 3, 8, 9);
    g2.drawLine(3, 2, 9, 8);
    g2.drawLine(9, 2, 2, 9);
    g2.drawLine(9, 3, 3, 9);
    g2.drawLine(8, 2, 2, 8);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 12;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}

class PlusIcon implements Icon {
  private final Rectangle viewRect = new Rectangle();

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);

    viewRect.setBounds(c.getBounds());
    JComponent jc = c instanceof JComponent ? (JComponent) c : null;
    OperaTabViewButtonUI.tabPainter(g2, SwingUtilities.calculateInnerArea(jc, viewRect));

    g2.setPaint(Color.WHITE);
    int w = viewRect.width;
    int a = w / 2;
    int b = w / 3;
    w -= 2;
    g2.drawLine(a, b, a, w - b);
    g2.drawLine(a - 1, b, a - 1, w - b);
    g2.drawLine(b, a, w - b, a);
    g2.drawLine(b, a - 1, w - b, a - 1);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 24;
  }

  @Override public int getIconHeight() {
    return 24;
  }
}
