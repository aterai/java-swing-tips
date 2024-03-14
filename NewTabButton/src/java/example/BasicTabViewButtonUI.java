// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

public class BasicTabViewButtonUI extends TabViewButtonUI {
  // private static final TabViewButtonUI tabViewButtonUI = new BasicTabViewButtonUI();
  private final Rectangle viewRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();
  private final Rectangle textRect = new Rectangle();

  // protected TabButton tabViewButton;

  public static ComponentUI createUI(JComponent c) {
    return new BasicTabViewButtonUI();
  }

  /**
   * {@inheritDoc}
   */
  @Override protected void installDefaults(AbstractButton b) {
    super.installDefaults(b);
    b.setPreferredSize(new Dimension(0, 24));
    b.setRolloverEnabled(true);
    b.setOpaque(true);
    Border out = BorderFactory.createMatteBorder(2, 0, 0, 0, b.getBackground());
    Border in = BorderFactory.createMatteBorder(1, 1, 0, 1, Color.RED);
    b.setBorder(BorderFactory.createCompoundBorder(out, in));
    // b.setForeground(Color.GREEN);
    if (b instanceof TabButton) {
      TabButton tabViewButton = (TabButton) b;
      tabViewButton.setTextColor(new Color(0x64_64_64));
      tabViewButton.setPressedTextColor(Color.GRAY);
      tabViewButton.setRolloverTextColor(Color.BLACK);
      tabViewButton.setRolloverSelectedTextColor(Color.GRAY);
      tabViewButton.setSelectedTextColor(Color.BLACK);
    }
  }

  // @Override public void uninstallUI(JComponent c) {
  //   super.uninstallUI(c);
  //   this.tabViewButton = null;
  // }

  // @Override public void installDefaults() {
  //   /* nn */
  // }

  /**
   * {@inheritDoc}
   */
  @Override public void paint(Graphics g, JComponent c) {
    if (!(c instanceof AbstractButton)) {
      return;
    }
    AbstractButton b = (AbstractButton) c;
    Font f = b.getFont();
    g.setFont(f);
    g.setColor(b.getBackground());
    g.fillRect(0, 0, b.getWidth(), b.getHeight());

    SwingUtilities.calculateInnerArea(b, viewRect);
    iconRect.setBounds(0, 0, 0, 0);
    textRect.setBounds(0, 0, 0, 0);

    final String text = SwingUtilities.layoutCompoundLabel(
        b,
        b.getFontMetrics(f),
        b.getText(),
        null, // altIcon != null ? altIcon : getDefaultIcon(),
        b.getVerticalAlignment(),
        b.getHorizontalAlignment(),
        b.getVerticalTextPosition(),
        b.getHorizontalTextPosition(),
        viewRect,
        iconRect,
        textRect,
        0); // b.getText() == null ? 0 : b.getIconTextGap());

    ButtonModel model = b.getModel();
    if (model.isSelected() || model.isArmed()) {
      g.setColor(Color.WHITE);
    } else {
      g.setColor(new Color(0xDC_DC_DC));
    }
    g.fillRect(viewRect.x, viewRect.y, viewRect.x + viewRect.width, viewRect.y + viewRect.height);

    Color color = new Color(0xFF_78_28);
    if (model.isSelected()) {
      g.setColor(color);
      g.drawLine(viewRect.x + 1, viewRect.y - 2, viewRect.x + viewRect.width - 1, viewRect.y - 2);
      g.setColor(color.brighter());
      g.drawLine(viewRect.x + 0, viewRect.y - 1, viewRect.x + viewRect.width - 0, viewRect.y - 1);
      g.setColor(color);
      g.drawLine(viewRect.x + 0, viewRect.y - 0, viewRect.x + viewRect.width - 0, viewRect.y - 0);
    } else if (model.isRollover()) {
      g.setColor(color);
      g.drawLine(viewRect.x + 1, viewRect.y + 0, viewRect.x + viewRect.width - 1, viewRect.y + 0);
      g.setColor(color.brighter());
      g.drawLine(viewRect.x + 0, viewRect.y + 1, viewRect.x + viewRect.width - 0, viewRect.y + 1);
      g.setColor(color);
      g.drawLine(viewRect.x + 0, viewRect.y + 2, viewRect.x + viewRect.width - 0, viewRect.y + 2);
    }
    Object o = c.getClientProperty(BasicHTML.propertyKey);
    if (o instanceof View) {
      ((View) o).paint(g, textRect);
    } else {
      if (model.isSelected()) {
        textRect.y -= 2;
        textRect.x -= 1;
      }
      textRect.x += 4;
      paintText(g, b, textRect, text);
    }
  }
}
