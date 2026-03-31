// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new ClippedTitleTabbedPane();
    Locale locale = Locale.getDefault();
    LocalDate today = LocalDate.now(ZoneId.systemDefault());
    DayOfWeek firstDay = WeekFields.of(locale).getFirstDayOfWeek();
    LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(firstDay));
    for (int i = 0; i < DayOfWeek.values().length; i++) {
      LocalDate date = startOfWeek.plusDays(i);
      tabs.addTab("", makeTabContent(date));
      tabs.setTabComponentAt(i, makeDayTab(date, today, locale));
    }
    add(tabs);
    setBackground(ModernTabbedPaneUI.BG_DARK);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makeTabContent(LocalDate date) {
    JLabel label = new JLabel("Schedule for " + date);
    label.setForeground(ModernTabbedPaneUI.TEXT_PRIMARY);
    JPanel content = new JPanel();
    content.setBackground(ModernTabbedPaneUI.BG_DARK);
    content.add(label);
    return content;
  }

  private static JPanel makeDayTab(LocalDate date, LocalDate today, Locale locale) {
    String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale);
    JLabel lblDay = new JLabel(dayName, SwingConstants.CENTER);
    lblDay.setFont(lblDay.getFont().deriveFont(11f));
    lblDay.setForeground(ModernTabbedPaneUI.TEXT_PRIMARY);

    String dayOfMonth = String.valueOf(date.getDayOfMonth());
    JLabel lblDate = new JLabel(dayOfMonth, SwingConstants.CENTER);
    lblDate.setFont(lblDate.getFont().deriveFont(Font.BOLD, 18f));
    lblDate.setForeground(ModernTabbedPaneUI.TEXT_PRIMARY);

    JPanel panel = new JPanel(new BorderLayout(0, 2));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    panel.add(lblDay, BorderLayout.NORTH);
    panel.add(lblDate);
    panel.add(makeIndicator(date, today), BorderLayout.SOUTH);
    return panel;
  }

  private static JLabel makeIndicator(LocalDate date, LocalDate today) {
    int iw = 20;
    int ih = 4;
    int ir = 2;
    JLabel lblIndicator = new JLabel("", SwingConstants.CENTER);
    if (date.equals(today)) {
      Color indicatorColor = ModernTabbedPaneUI.ACCENT_TODAY;
      Icon activeIcon = new IndicatorIcon(indicatorColor, iw, ih, ir);
      lblIndicator.setIcon(activeIcon);
    } else {
      Color inactiveColor = new Color(0x0, true);
      Icon inactiveIcon = new IndicatorIcon(inactiveColor, iw, ih, ir);
      lblIndicator.setIcon(inactiveIcon);
    }
    return lblIndicator;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ClippedTitleTabbedPane extends JTabbedPane {
  protected ClippedTitleTabbedPane() {
    super();
  }

  @Override public void updateUI() {
    super.updateUI();
    UIManager.put("TabbedPane.selectedLabelShift", 0);
    UIManager.put("TabbedPane.labelShift", 0);
    setUI(new ModernTabbedPaneUI());
  }

  private Insets getSynthInsets(Region region) {
    SynthStyle style = SynthLookAndFeel.getStyle(this, region);
    SynthContext ctx = new SynthContext(this, region, style, SynthConstants.ENABLED);
    return style.getInsets(ctx, null);
  }

  private Insets getTabInsets() {
    return Optional.ofNullable(UIManager.getInsets("TabbedPane.tabInsets"))
        .orElseGet(() -> getSynthInsets(Region.TABBED_PANE_TAB));
  }

  private Insets getTabAreaInsets() {
    return Optional.ofNullable(UIManager.getInsets("TabbedPane.tabAreaInsets"))
        .orElseGet(() -> getSynthInsets(Region.TABBED_PANE_TAB_AREA));
  }

  @Override public void doLayout() {
    int tabCount = getTabCount();
    if (tabCount > 0 && isVisible()) {
      Insets tabIns = getTabInsets();
      Insets tabAreaIns = getTabAreaInsets();
      Insets i = getInsets();
      int tabPlacement = getTabPlacement();
      int areaWidth = getWidth() - tabAreaIns.left - tabAreaIns.right - i.left - i.right;
      boolean isTopBottom = tabPlacement == TOP || tabPlacement == BOTTOM;
      int tabWidth = isTopBottom ? areaWidth / tabCount : areaWidth / 4;
      int gap = isTopBottom ? areaWidth - tabWidth * tabCount : 0;
      // This 3 is the magic number defined in BasicTabbedPaneUI#calculateTabWidth(...)
      tabWidth -= tabIns.left + tabIns.right + 3;
      updateAllTabWidth(tabWidth, gap);
    }
    super.doLayout();
  }

  private void updateAllTabWidth(int tabWidth, int gap) {
    Dimension dim = new Dimension();
    int rest = gap;
    int tabCount = getTabCount();
    for (int i = 0; i < tabCount; i++) {
      Component c = getTabComponentAt(i);
      if (c instanceof JComponent) {
        JComponent tab = (JComponent) c;
        int a = i == tabCount - 1 ? rest : 1;
        int w = rest > 0 ? tabWidth + a : tabWidth;
        dim.setSize(w, tab.getPreferredSize().height);
        tab.setPreferredSize(dim);
        rest -= a;
      }
    }
  }
}

class IndicatorIcon implements Icon {
  private final Color color;
  private final int width;
  private final int height;
  private final int arcRadius;

  protected IndicatorIcon(Color color, int width, int height, int arcRadius) {
    this.color = color;
    this.width = width;
    this.height = height;
    this.arcRadius = arcRadius;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(color);
    Rectangle r = SwingUtilities.calculateInnerArea((JComponent) c, null);
    int ox = (int) r.getCenterX() - arcRadius;
    int oy = (int) r.getCenterY() - arcRadius;
    int arcDiameter = arcRadius * 2;
    g2.fillOval(ox, oy, arcDiameter, arcDiameter);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return width;
  }

  @Override public int getIconHeight() {
    return height;
  }
}

class ModernTabbedPaneUI extends BasicTabbedPaneUI {
  public static final Color BG_DARK = new Color(0x0F, 0x0F, 0x13);
  // public static final Color BG_PANEL = new Color(0x18_18_20);
  public static final Color BG_TAB_IDLE = new Color(0x22_22_2E);
  public static final Color BG_TAB_SEL = new Color(0x2A_2A_3C);
  public static final Color ACCENT = new Color(0x7C_6A_FF);
  public static final Color ACCENT_TODAY = new Color(0xFF_6B_6B);
  public static final Color TEXT_PRIMARY = new Color(0xF0_F0_F8);
  // public static final Color TEXT_MUTED = new Color(0x80_80_9A);
  // private static final Color BORDER_COLOR = new Color(0x30_30_45);

  @Override protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(isSelected ? BG_TAB_SEL : BG_TAB_IDLE);
    int arc = 12;
    g2.fill(new RoundRectangle2D.Float(x + 2f, y + 2f, w - 4f, h - 2f, arc, arc));
    if (isSelected) {
      g2.setColor(ACCENT);
      // g2.fillRoundRect(x + arc, y + h - 1, w - arc * 2 - 1, 3, 6, 6);
      g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2.drawLine(x + arc, y + h - 1, x + w - arc - 1, y + h - 1);
    }
    g2.dispose();
  }

  @Override protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
    // empty paint
  }

  @Override protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    // empty paint
  }

  @Override protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    // empty paint
  }
}
