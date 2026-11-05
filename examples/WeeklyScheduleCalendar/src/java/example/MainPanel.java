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
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new DayScheduleTabbedPane();
    updateCalendar(tabs, true);

    JCheckBox check = new JCheckBox("Center Today", true);
    check.addActionListener(e -> updateCalendar(tabs, check.isSelected()));

    add(tabs);
    add(check, BorderLayout.SOUTH);
    setBackground(ModernTabbedPaneUI.BG_DARK);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void updateCalendar(JTabbedPane tabs, boolean centerToday) {
    tabs.removeAll();
    Locale locale = Locale.getDefault();
    LocalDate today = LocalDate.now(ZoneId.systemDefault());
    LocalDate startDay = getStartDay(today, locale, centerToday);
    int todayIndex = 0;
    for (int i = 0; i < DayOfWeek.values().length; i++) {
      LocalDate date = startDay.plusDays(i);
      tabs.addTab("", createTabContent(date));
      tabs.setTabComponentAt(i, createDayTab(date, today, locale));
      if (date.equals(today)) {
        todayIndex = i;
      }
    }
    tabs.setSelectedIndex(todayIndex);
  }

  private static LocalDate getStartDay(LocalDate today, Locale locale, boolean centerToday) {
    DayOfWeek firstDay = WeekFields.of(locale).getFirstDayOfWeek();
    return centerToday
        ? today.minusDays(DayOfWeek.values().length / 2)
        : today.with(TemporalAdjusters.previousOrSame(firstDay));
  }

  private static JPanel createTabContent(LocalDate date) {
    JLabel label = new JLabel("Schedule for " + date);
    label.setForeground(ModernTabbedPaneUI.TEXT_PRIMARY);
    JPanel content = new JPanel();
    content.setBackground(ModernTabbedPaneUI.BG_DARK);
    content.add(label);
    return content;
  }

  private static JPanel createDayTab(LocalDate date, LocalDate today, Locale locale) {
    String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale);
    JLabel dayLabel = new JLabel(dayName, SwingConstants.CENTER);
    dayLabel.setFont(dayLabel.getFont().deriveFont(11f));
    dayLabel.setForeground(ModernTabbedPaneUI.TEXT_PRIMARY);

    String dayOfMonth = String.valueOf(date.getDayOfMonth());
    JLabel dateLabel = new JLabel(dayOfMonth, SwingConstants.CENTER);
    dateLabel.setFont(dateLabel.getFont().deriveFont(Font.BOLD, 18f));
    dateLabel.setForeground(ModernTabbedPaneUI.TEXT_PRIMARY);

    JPanel panel = new JPanel(new BorderLayout(0, 2));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    panel.add(dayLabel, BorderLayout.NORTH);
    panel.add(dateLabel);
    panel.add(createIndicator(date, today), BorderLayout.SOUTH);
    return panel;
  }

  private static JLabel createIndicator(LocalDate date, LocalDate today) {
    // The transparent icon keeps the layout of the other tabs the same as today's tab
    Color color = date.equals(today) ? ModernTabbedPaneUI.ACCENT_TODAY : new Color(0x0, true);
    return new JLabel(new IndicatorIcon(color, 20, 4, 2), SwingConstants.CENTER);
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

class DayScheduleTabbedPane extends JTabbedPane {
  protected DayScheduleTabbedPane() {
    super();
  }

  @Override public void updateUI() {
    super.updateUI();
    // Synth-based LookAndFeels (e.g. Nimbus) turn this off, which skips paintTabBackground(...)
    UIManager.put("TabbedPane.tabsOpaque", true);
    setUI(new ModernTabbedPaneUI());
  }
}

class IndicatorIcon implements Icon {
  private final Color color;
  private final int width;
  private final int height;
  private final int radius;

  protected IndicatorIcon(Color color, int width, int height, int radius) {
    this.color = color;
    this.width = width;
    this.height = height;
    this.radius = radius;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(color);
    int diameter = radius * 2;
    g2.fillOval(x + width / 2 - radius, y + height / 2 - radius, diameter, diameter);
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
  public static final Color BG_DARK = new Color(0x0F_0F_13);
  // public static final Color BG_PANEL = new Color(0x18_18_20);
  public static final Color BG_TAB_IDLE = new Color(0x22_22_2E);
  public static final Color BG_TAB_SELECTED = new Color(0x2A_2A_3C);
  public static final Color ACCENT = new Color(0x7C_6A_FF);
  public static final Color ACCENT_TODAY = new Color(0xFF_6B_6B);
  public static final Color TEXT_PRIMARY = new Color(0xF0_F0_F8);
  // public static final Color TEXT_MUTED = new Color(0x80_80_9A);
  // private static final Color BORDER_COLOR = new Color(0x30_30_45);

  @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
    boolean isTopBottom = tabPlacement == TOP || tabPlacement == BOTTOM;
    return isTopBottom && tabPane.getWidth() > 0
        ? calculateEvenTabWidth(tabPlacement, tabIndex)
        : super.calculateTabWidth(tabPlacement, tabIndex, metrics);
  }

  private int calculateEvenTabWidth(int tabPlacement, int tabIndex) {
    Insets insets = tabPane.getInsets();
    Insets areaInsets = getTabAreaInsets(tabPlacement);
    int horizontalInsets = insets.left + insets.right + areaInsets.left + areaInsets.right;
    int areaWidth = tabPane.getWidth() - horizontalInsets;
    int tabCount = tabPane.getTabCount();
    // Integer pixel distribution: the leading tabs take one pixel each of the remainder
    int extraPixel = tabIndex < areaWidth % tabCount ? 1 : 0;
    return areaWidth / tabCount + extraPixel;
  }

  @Override protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
    return 0;
  }

  @Override protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
    return 0;
  }

  @Override protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(isSelected ? BG_TAB_SELECTED : BG_TAB_IDLE);
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
