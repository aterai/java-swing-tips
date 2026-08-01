// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

public final class OperaTabViewButtonUI extends BasicTabViewButtonUI {
  // private static final TabViewButtonUI tabViewButtonUI = new OperaTabViewButtonUI();
  private static final int CLOSE_ICON_WIDTH = 12;
  private static final Color TEXT_COLOR = new Color(0xE6_F5_FF);
  private static final Color OVERLAY_COLOR = new Color(0x64_00_00_00, true);
  private static final Color TOP_GRAD_START = new Color(0x84_A2_B4);
  private static final Color TOP_GRAD_END = new Color(0x67_85_98);
  private static final Color BTM_GRAD_START = new Color(0x32_49_54);
  private static final Color BTM_GRAD_END = new Color(0x3C_56_65);
  private static final Color SHADOW_GRAD_START = new Color(0x1E_00_00_00, true);
  private static final Color SHADOW_GRAD_END = new Color(0x05_00_00_00, true);
  private static final Color BORDER_COLOR = new Color(0x27_38_43);
  private static final Color TOP_HIGHLIGHT = new Color(0x1E_FF_FF_FF, true);
  private static final Color LEFT_HIGHLIGHT = new Color(0x3C_FF_FF_FF, true);
  private static final Color RIGHT_SHADOW = new Color(0xFA_27_38_43, true);
  private final Rectangle viewRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();
  private final Rectangle textRect = new Rectangle();

  public static ComponentUI createUI(JComponent c) {
    return new OperaTabViewButtonUI();
  }
  // @Override public void installUI(JComponent c) {
  //   super.installUI(c);
  // }

  @Override protected void installDefaults(AbstractButton b) {
    super.installDefaults(b);
    b.setBorder(BorderFactory.createEmptyBorder());
    b.setForeground(Color.WHITE);
    if (b instanceof TabButton) {
      TabButton tabViewButton = (TabButton) b;
      tabViewButton.setTextColor(TEXT_COLOR);
      tabViewButton.setPressedTextColor(Color.WHITE.darker());
      tabViewButton.setRolloverTextColor(Color.WHITE);
      tabViewButton.setRolloverSelectedTextColor(Color.WHITE);
      tabViewButton.setSelectedTextColor(Color.WHITE);
    }
  }

  @SuppressWarnings("ReturnCount")
  @Override public void paint(Graphics g, JComponent c) {
    if (!(c instanceof AbstractButton)) {
      return;
    }
    AbstractButton b = (AbstractButton) c;
    Font f = b.getFont();
    g.setFont(f);

    SwingUtilities.calculateInnerArea(b, viewRect);
    iconRect.setBounds(0, 0, 0, 0);
    textRect.setBounds(0, 0, 0, 0);

    Graphics2D g2 = (Graphics2D) g.create();
    tabPainter(g2, viewRect);

    Icon icon = b.getIcon();
    viewRect.width -= CLOSE_ICON_WIDTH;
    String text = SwingUtilities.layoutCompoundLabel(
        c,
        c.getFontMetrics(f),
        b.getText(),
        icon, // altIcon != null ? altIcon : getDefaultIcon(),
        b.getVerticalAlignment(),
        b.getHorizontalAlignment(),
        b.getVerticalTextPosition(),
        b.getHorizontalTextPosition(),
        viewRect,
        iconRect,
        textRect,
        Objects.nonNull(b.getText()) ? b.getIconTextGap() : 0);

    Object o = c.getClientProperty(BasicHTML.propertyKey);
    if (o instanceof View) {
      ((View) o).paint(g, textRect);
    } else {
      textRect.x += 4;
      paintText(g, b, textRect, text);
    }

    Optional.ofNullable(icon).ifPresent(icn -> {
      int ix = iconRect.x + 4;
      int iy = iconRect.y + 2;
      icn.paintIcon(c, g, ix, iy);
    });

    ButtonModel model = b.getModel();
    if (!model.isSelected() && !model.isArmed() && !model.isRollover()) {
      g2.setPaint(OVERLAY_COLOR);
      g2.fillRect(0, 0, c.getWidth(), c.getHeight());
      // g2.fill(viewRect);
    }
    g2.dispose();
  }

  public static void tabPainter(Graphics2D g2, Rectangle r) {
    Rectangle r1 = new Rectangle(r.x, r.y, r.width, r.height / 2);
    Rectangle r2 = new Rectangle(r.x, r.y + r.height / 2, r.width, r.height / 2);
    Rectangle r3 = new Rectangle(r.x, r.y + r.height / 2 - 2, r.width, r.height / 4);

    g2.setPaint(new GradientPaint(
        0f, r1.y, TOP_GRAD_START,
        0f, (float) (r1.y + r1.height), TOP_GRAD_END, true));
    g2.fill(r1);
    g2.setPaint(new GradientPaint(
        0f, r2.y, BTM_GRAD_START,
        0f, (float) (r2.y + r2.height), BTM_GRAD_END, true));
    g2.fill(r2);
    g2.setPaint(new GradientPaint(
        0f, r3.y, SHADOW_GRAD_START,
        0f, (float) (r3.y + r3.height), SHADOW_GRAD_END, true));
    g2.fill(r3);

    g2.setPaint(BORDER_COLOR);
    g2.drawLine(r.x, r.y, r.x + r.width, r.y);

    g2.setPaint(TOP_HIGHLIGHT);
    g2.drawLine(r.x + 1, r.y + 1, r.x + r.width, r.y + 1);

    g2.setPaint(LEFT_HIGHLIGHT);
    g2.drawLine(r.x, r.y, r.x, r.y + r.height);

    g2.setPaint(RIGHT_SHADOW);
    g2.drawLine(r.x + r.width - 1, r.y, r.x + r.width - 1, r.y + r.height);
    g2.drawLine(r.x, r.y + r.height - 1, r.x + r.width - 1, r.y + r.height - 1);
  }
}
