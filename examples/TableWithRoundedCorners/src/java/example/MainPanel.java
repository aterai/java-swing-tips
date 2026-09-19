// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Path2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    CalendarTable monthTable = new CalendarTable();
    updateMonthView(monthTable, monthLabel, LocalDate.now(ZoneId.systemDefault()));

    JButton prevButton = new JButton("<");
    prevButton.addActionListener(e -> {
      LocalDate date = monthTable.getCurrentDate().minusMonths(1);
      updateMonthView(monthTable, monthLabel, date);
    });
    JButton nextButton = new JButton(">");
    nextButton.addActionListener(e -> {
      LocalDate date = monthTable.getCurrentDate().plusMonths(1);
      updateMonthView(monthTable, monthLabel, date);
    });

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    topPanel.setOpaque(false);
    topPanel.add(monthLabel);
    topPanel.add(prevButton, BorderLayout.WEST);
    topPanel.add(nextButton, BorderLayout.EAST);

    add(topPanel, BorderLayout.NORTH);
    add(new MonthScrollPane(monthTable));
    setBorder(BorderFactory.createEmptyBorder(5, 25, 15, 25));
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void updateUI() {
    super.updateUI();
    setBackground(UIManager.getColor("Table.background"));
  }

  private static void updateMonthView(CalendarTable table, JLabel label, LocalDate date) {
    table.setCurrentDate(date);
    Locale locale = Locale.getDefault();
    DateTimeFormatter formatter = CalendarUtils.getLocalizedYearMonthFormatter(locale);
    String formattedText = date.format(formatter);
    label.setText(CalendarUtils.getLocalizedYearMonthText(formattedText));
    table.setModel(new CalendarViewTableModel(date));
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

class MonthScrollPane extends JScrollPane {
  protected MonthScrollPane(Component view) {
    super(view);
  }

  @Override public void updateUI() {
    super.updateUI();
    setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
    setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
    setBorder(BorderFactory.createEmptyBorder());
    setViewportBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
    // getViewport().setOpaque(false);
  }

  @Override public boolean isOpaque() {
    return false;
  }
}

class CalendarTable extends JTable {
  private LocalDate currentDate;
  private int lastHeight = -1;

  @Override public void updateUI() {
    super.updateUI();
    setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setCellSelectionEnabled(true);
    setFillsViewportHeight(true);
    setShowVerticalLines(false);
    setShowHorizontalLines(false);
    setIntercellSpacing(new Dimension(0, 0));
    setBackground(UIManager.getColor("Table.background"));
    JTableHeader header = getTableHeader();
    header.setResizingAllowed(false);
    header.setReorderingAllowed(false);
    // header.setBackground(new Color(0x0, true));
    header.setOpaque(false);
    updateWeekHeaderRenderer();
  }

  @Override public void setModel(TableModel dataModel) {
    super.setModel(dataModel);
    lastHeight = -1;
    EventQueue.invokeLater(this::updateWeekHeaderRenderer);
  }

  public void setCurrentDate(LocalDate date) {
    currentDate = date;
  }

  public LocalDate getCurrentDate() {
    return currentDate;
  }

  private void updateWeekHeaderRenderer() {
    TableColumnModel columnModel = getColumnModel();
    TableCellRenderer renderer = new WeekHeaderRenderer();
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      columnModel.getColumn(i).setHeaderRenderer(renderer);
    }
    getTableHeader().repaint();
  }

  @Override public boolean getScrollableTracksViewportHeight() {
    return getParent() instanceof JViewport;
  }

  // Recompute row heights on every layout pass (e.g. window resize) so the
  // table keeps exactly filling its enclosing viewport.
  @Override public void doLayout() {
    super.doLayout();
    if (getParent() instanceof JViewport) {
      adjustRowHeights((JViewport) getParent());
    }
  }

  // Distribute the viewport height evenly across all rows so the calendar
  // grid always fills the JScrollPane exactly, with no leftover gap or
  // vertical scrollbar. Any remainder pixels are handed out one at a time
  // to the first rows so every row differs by at most one pixel.
  private void adjustRowHeights(JViewport viewport) {
    int rowCount = getModel().getRowCount();
    if (rowCount == 0) {
      return;
    }
    int height = viewport.getExtentSize().height;
    int baseRowHeight = height / rowCount;
    if (height != lastHeight && baseRowHeight > 0) {
      int remainder = height % rowCount;
      // Distribute the remainder one pixel at a time to the first rows
      for (int i = 0; i < rowCount; i++) {
        int adjustedHeight = baseRowHeight + (i < remainder ? 1 : 0);
        setRowHeight(i, Math.max(1, adjustedHeight));
      }
    }
    lastHeight = height;
  }
}

enum Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

class CalendarTableRenderer extends DefaultTableCellRenderer {
  private final Set<Corner> roundedCorners = EnumSet.noneOf(Corner.class);

  // Cache the cell coordinates of the last render call so that
  // paintComponent(...) can rebuild the same rounded-corner path.
  private int renderedRow;
  private int renderedColumn;
  private int lastRow;
  private int lastColumn;

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
    Component renderer = super.getTableCellRendererComponent(
        table, value, selected, focused, row, column);
    renderer.setBackground(table.getBackground());
    this.renderedRow = row;
    this.renderedColumn = column;
    updateCorners(table, row, column);
    if (value instanceof LocalDate
        && renderer instanceof JLabel
        && table instanceof CalendarTable) {
      LocalDate date = (LocalDate) value;
      JLabel label = (JLabel) renderer;
      label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      label.setText(Integer.toString(date.getDayOfMonth()));
      label.setForeground(table.getForeground());
      label.setVerticalAlignment(TOP);
      label.setHorizontalAlignment(CENTER);
      label.setVerticalTextPosition(TOP);
      label.setHorizontalTextPosition(CENTER);
      LocalDate currentDate = ((CalendarTable) table).getCurrentDate();
      if (YearMonth.from(date).equals(YearMonth.from(currentDate))) {
        label.setFont(label.getFont().deriveFont(Font.BOLD));
      } else {
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
      }
      if (date.equals(LocalDate.now(ZoneId.systemDefault()))) {
        label.setIcon(new TodayIndicatorIcon(label.getForeground()));
      } else {
        label.setIcon(null);
      }
    }
    return renderer;
  }

  // Determine which of the four outer corners of the whole table (not the
  // JTableHeader) the given cell occupies, if any.
  private void updateCorners(JTable table, int row, int column) {
    roundedCorners.clear();
    TableModel model = table.getModel();
    lastRow = model.getRowCount() - 1;
    lastColumn = model.getColumnCount() - 1;
    if (row == 0 && column == 0) {
      roundedCorners.add(Corner.TOP_LEFT);
    }
    if (row == 0 && column == lastColumn) {
      roundedCorners.add(Corner.TOP_RIGHT);
    }
    if (row == lastRow && column == 0) {
      roundedCorners.add(Corner.BOTTOM_LEFT);
    }
    if (row == lastRow && column == lastColumn) {
      roundedCorners.add(Corner.BOTTOM_RIGHT);
    }
  }

  @Override public boolean isOpaque() {
    return false;
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Rectangle bounds = getBounds();
    bounds.setLocation(0, 0);
    g2.setPaint(getBackground());
    g2.fill(bounds);
    g2.setPaint(UIManager.getColor("Table.gridColor"));
    Shape shape = buildRoundedRectPath(bounds, 16d, 16d, renderedRow, renderedColumn);
    g2.draw(shape);
    g2.dispose();
    super.paintComponent(g);
  }

  // Build a rectangle outline whose corners are rounded only where the
  // cell touches one of the table's four outer corners (see updateCorners).
  private Shape buildRoundedRectPath(
      Rectangle bounds, double arcWidth, double arcHeight, int row, int column) {
    double halfArcH = arcHeight * .5;
    double halfArcW = arcWidth * .5;
    // Kappa is the constant ratio used to place cubic Bezier control points
    // so that curveTo(...) approximates a quarter-circle arc.
    double kappa = 4d * (Math.sqrt(2d) - 1d) / 3d; // ≒ 0.55228
    double ctrlOffsetW = halfArcW * kappa;
    double ctrlOffsetH = halfArcH * kappa;
    double x = bounds.getX();
    double y = bounds.getY();
    // Trim the last column/row by a pixel so the outline stays inside the
    // viewport and does not get clipped or doubled up against its edge.
    double w = bounds.getWidth() - (column == lastColumn ? 2d : 0d);
    double h = bounds.getHeight() - (row == lastRow ? 2d : 0d);
    Path2D.Double path = new Path2D.Double();
    if (roundedCorners.contains(Corner.TOP_LEFT)) {
      path.moveTo(x, y + halfArcH);
      path.curveTo(x, y + halfArcH - ctrlOffsetH,
          x + halfArcW - ctrlOffsetW, y,
          x + halfArcW, y);
    } else {
      path.moveTo(x, y);
    }
    if (roundedCorners.contains(Corner.TOP_RIGHT)) {
      path.lineTo(x + w - halfArcW, y);
      path.curveTo(x + w - halfArcW + ctrlOffsetW, y,
          x + w, y + halfArcH - ctrlOffsetH,
          x + w, y + halfArcH);
    } else {
      path.lineTo(x + w, y);
    }
    if (roundedCorners.contains(Corner.BOTTOM_RIGHT)) {
      path.lineTo(x + w, y + h - halfArcH);
      path.curveTo(x + w, y + h - halfArcH + ctrlOffsetH,
          x + w - halfArcW + ctrlOffsetW, y + h,
          x + w - halfArcW, y + h);
    } else {
      path.lineTo(x + w, y + h);
    }
    if (roundedCorners.contains(Corner.BOTTOM_LEFT)) {
      path.lineTo(x + halfArcW, y + h);
      path.curveTo(x + halfArcW - ctrlOffsetW, y + h,
          x, y + h - halfArcH + ctrlOffsetH,
          x, y + h - halfArcH);
    } else {
      path.lineTo(x, y + h);
    }
    path.closePath();
    return path;
  }
}

class WeekHeaderRenderer extends DefaultTableCellRenderer {
  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component renderer = super.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (renderer instanceof JLabel) {
      JLabel label = (JLabel) renderer;
      label.setHorizontalAlignment(CENTER);
      label.setBackground(table.getBackground());
      label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
      // label.setOpaque(false);
    }
    return renderer;
  }
}

class CalendarViewTableModel extends DefaultTableModel {
  private final LocalDate startDate;
  private final Locale locale = Locale.getDefault();
  private final WeekFields weekFields = WeekFields.of(locale);

  protected CalendarViewTableModel(LocalDate date) {
    super();
    // The grid always starts on the first day of the week (locale-dependent)
    // that contains the 1st of the month, so leading cells from the
    // previous month are shown instead of left blank.
    LocalDate firstDayOfMonth = YearMonth.from(date).atDay(1);
    int dayOffset = firstDayOfMonth.get(weekFields.dayOfWeek()) - 1;
    startDate = firstDayOfMonth.minusDays(dayOffset);
  }

  @Override public Class<?> getColumnClass(int column) {
    return LocalDate.class;
  }

  @Override public String getColumnName(int column) {
    return weekFields.getFirstDayOfWeek().plus(column)
        .getDisplayName(TextStyle.SHORT_STANDALONE, locale);
  }

  @Override public int getRowCount() {
    return 6;
  }

  @Override public int getColumnCount() {
    return DayOfWeek.values().length;
  }

  @Override public Object getValueAt(int row, int column) {
    return startDate.plusDays((long) row * getColumnCount() + column);
  }

  @Override public boolean isCellEditable(int row, int column) {
    return false;
  }
}

class TodayIndicatorIcon implements Icon {
  private final Color color;

  protected TodayIndicatorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(color);
    int arcRadius = 2;
    int arcDiameter = arcRadius * 2;
    int ox = x + (getIconWidth() - arcDiameter) / 2;
    int oy = y + (getIconHeight() - arcDiameter) / 2;
    g2.fillOval(ox, oy, arcDiameter, arcDiameter);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 8;
  }

  @Override public int getIconHeight() {
    return 8;
  }
}

final class CalendarUtils {
  private static final Pattern YEAR_PATTERN = Pattern.compile("(y+)");
  private static final Pattern MONTH_PATTERN = Pattern.compile("(M+)");

  private CalendarUtils() {
    /* Utility class */
  }

  public static String getLocalizedDatePattern(Locale locale) {
    return DateTimeFormatterBuilder.getLocalizedDateTimePattern(
        FormatStyle.LONG, null, Chronology.ofLocale(locale), locale);
  }

  // Build a "year month" only pattern (e.g. "yyyy M" or "M yyyy") by pulling
  // just the year and month runs out of the locale's long date pattern and
  // re-joining them in the locale's natural order.
  public static DateTimeFormatter getLocalizedYearMonthFormatter(Locale locale) {
    String localizedPattern = getLocalizedDatePattern(locale);
    String year = extractPatternPart(localizedPattern, YEAR_PATTERN);
    String month = extractPatternPart(localizedPattern, MONTH_PATTERN);
    String pattern = isYearFirst(localizedPattern)
        ? year + " " + month : month + " " + year;
    return DateTimeFormatter.ofPattern(pattern, locale);
  }

  // Locales whose year/month fields are both purely numeric (e.g. "2026 4")
  // read poorly without a separator, so insert " / " between them; locales
  // with a spelled-out month (e.g. "April 2026") are left untouched.
  public static String getLocalizedYearMonthText(String formatted) {
    String[] parts = formatted.trim().split("\\s+");
    boolean isAllNumeric = parts.length == 2
        && parts[0].chars().allMatch(Character::isDigit)
        && parts[1].chars().allMatch(Character::isDigit);
    return isAllNumeric ? parts[0] + " / " + parts[1] : formatted;
  }

  private static String extractPatternPart(String source, Pattern pattern) {
    Matcher matcher = pattern.matcher(source);
    return matcher.find() ? matcher.group(1) : "";
  }

  public static boolean isYearFirst(Locale locale) {
    return isYearFirst(getLocalizedDatePattern(locale));
  }

  private static boolean isYearFirst(String localizedPattern) {
    int yearIndex = localizedPattern.indexOf('y');
    int monthIndex = localizedPattern.indexOf('M');
    return yearIndex != -1 && monthIndex != -1 && yearIndex < monthIndex;
  }
}
