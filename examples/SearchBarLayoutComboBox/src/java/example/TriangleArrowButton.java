// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

public final class TriangleArrowButton extends JButton {
  private static final Icon TRIANGLE_ICON = new TriangleIcon();
  private static final Color HIGHLIGHT = new Color(0xDC_DC_DC);

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    ButtonModel m = getModel();
    boolean highlighted = m.isArmed() || isRolloverEnabled() && m.isRollover() || hasFocus();
    g2.setPaint(highlighted ? HIGHLIGHT : getBackground());
    // Fill in this button's own coordinate space,
    // getBounds() returns the location in the parent JComboBox
    int w = getWidth();
    int h = getHeight();
    g2.fillRect(0, 0, w, h);
    g2.dispose();

    super.paintComponent(g);
    Insets i = getInsets();
    int x = w - i.right - TRIANGLE_ICON.getIconWidth() - 2;
    int y = i.top + (h - i.top - i.bottom - TRIANGLE_ICON.getIconHeight()) / 2;
    TRIANGLE_ICON.paintIcon(this, g, x, y);
  }

  @Override public Dimension getPreferredSize() {
    int iconWidth = Optional.ofNullable(getIcon()).map(Icon::getIconWidth).orElse(16);
    Insets i = getInsets();
    int w = iconWidth + TRIANGLE_ICON.getIconWidth() + i.left + i.right;
    return new Dimension(w, w);
  }

  // Ignore the LookAndFeel default border, accept only the CompoundBorder
  // set by BasicSearchBarComboBoxUI#configureArrowButton()
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
