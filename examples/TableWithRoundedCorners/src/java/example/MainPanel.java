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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
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
    MonthTable monthTable = new MonthTable();
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

  private static void updateMonthView(MonthTable table, JLabel label, LocalDate date) {
    table.setCurrentDate(date);
    Locale locale = Locale.getDefault();
    DateTimeFormatter formatter = CalendarUtils.getLocalizedYearMonthFormatter(locale);
    String formattedText = date.format(formatter.withLocale(locale));
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

class MonthTable extends JTable {
  private LocalDate currentDate;
  private int prevHeight = -1;

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
    prevHeight = -1;
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

  @Override public void doLayout() {
    super.doLayout();
    Class<JViewport> viewportClass = JViewport.class;
    Optional.ofNullable(SwingUtilities.getAncestorOfClass(viewportClass, this))
        .filter(viewportClass::isInstance)
        .map(viewportClass::cast)
        .ifPresent(this::adjustRowHeights);
  }

  private void adjustRowHeights(JViewport viewport) {
    int height = viewport.getExtentSize().height;
    int rowCount = getModel().getRowCount();
    int baseRowHeight = height / rowCount;
    if (height != prevHeight && baseRowHeight > 0) {
      int remainder = height % rowCount;
      for (int i = 0; i < rowCount; i++) {
        int adjustedHeight = baseRowHeight + Math.min(Math.max(remainder, 0), 1);
        // Java 21: int adjustedHeight = baseRowHeight + Math.clamp(remainder, 0, 1);
        setRowHeight(i, Math.max(1, adjustedHeight));
        remainder -= 1;
      }
    }
    prevHeight = height;
  }
}

enum Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

class CalendarTableRenderer extends DefaultTableCellRenderer {
  private final Set<Corner> roundedCorners = EnumSet.noneOf(Corner.class);
  private final LocalDate realDate = LocalDate.now(ZoneId.systemDefault());
  private int row;
  private int column;

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
    Component renderer = super.getTableCellRendererComponent(
        table, value, selected, focused, row, column);
    renderer.setBackground(table.getBackground());
    this.row = row;
    this.column = column;
    updateCorners(table, row, column);
    if (value instanceof LocalDate && renderer instanceof JLabel && table instanceof MonthTable) {
      LocalDate date = (LocalDate) value;
      JLabel label = (JLabel) renderer;
      label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      label.setText(Integer.toString(date.getDayOfMonth()));
      label.setForeground(table.getForeground());
      label.setVerticalAlignment(TOP);
      label.setHorizontalAlignment(CENTER);
      label.setVerticalTextPosition(TOP);
      label.setHorizontalTextPosition(CENTER);
      LocalDate currentDate = ((MonthTable) table).getCurrentDate();
      if (YearMonth.from(date).equals(YearMonth.from(currentDate))) {
        label.setFont(label.getFont().deriveFont(Font.BOLD));
      } else {
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
      }
      if (date.equals(realDate)) {
        label.setIcon(new IndicatorIcon(label.getForeground()));
      } else {
        label.setIcon(null);
      }
    }
    return renderer;
  }

  private void updateCorners(JTable table, int row, int col) {
    roundedCorners.clear();
    TableModel model = table.getModel();
    int lastRow = model.getRowCount() - 1;
    int lastCol = model.getColumnCount() - 1;
    if (row == 0 && col == 0) {
      roundedCorners.add(Corner.TOP_LEFT);
    }
    if (row == 0 && col == lastCol) {
      roundedCorners.add(Corner.TOP_RIGHT);
    }
    if (row == lastRow && col == 0) {
      roundedCorners.add(Corner.BOTTOM_LEFT);
    }
    if (row == lastRow && col == lastCol) {
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
    Shape shape = buildRoundedRectPath(bounds, 16d, 16d, row, column);
    g2.draw(shape);
    g2.dispose();
    super.paintComponent(g);
  }

  private Shape buildRoundedRectPath(
      Rectangle bounds, double arcWidth, double arcHeight, int row, int col) {
    double x = bounds.getX();
    double y = bounds.getY();
    double w = bounds.getWidth() - (col == 6 ? 2d : 0d);
    double h = bounds.getHeight() - (row == 5 ? 2d : 0d);
    double halfArcH = arcHeight * .5;
    double halfArcW = arcWidth * .5;
    double kappa = 4d * (Math.sqrt(2d) - 1d) / 3d; // ≒ 0.55228
    double ctrlOffsetW = halfArcW * kappa;
    double ctrlOffsetH = halfArcH * kappa;
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
  private final WeekFields weekFields = WeekFields.of(Locale.getDefault());

  protected CalendarViewTableModel(LocalDate date) {
    super();
    LocalDate firstDayOfMonth = YearMonth.from(date).atDay(1);
    int dayOffset = firstDayOfMonth.get(weekFields.dayOfWeek()) - 1;
    startDate = firstDayOfMonth.minusDays(dayOffset);
  }

  @Override public Class<?> getColumnClass(int column) {
    return LocalDate.class;
  }

  @Override public String getColumnName(int column) {
    return weekFields.getFirstDayOfWeek().plus(column)
        .getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault());
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

class IndicatorIcon implements Icon {
  private final Color color;

  protected IndicatorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(color);
    // g2.fillRoundRect(x, y, width, height, arcDiameter, arcDiameter);
    int arcRadius = 2;
    int arcDiameter = arcRadius * 2;
    Rectangle r = SwingUtilities.calculateInnerArea((JComponent) c, null);
    int ox = (int) r.getCenterX() - arcRadius;
    int oy = c.getFont().getSize() + arcDiameter;
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
  private CalendarUtils() {
    /* Utility class */
  }

  public static String getLocalizedPattern(Locale locale) {
    return DateTimeFormatterBuilder.getLocalizedDateTimePattern(
        FormatStyle.LONG, null, Chronology.ofLocale(locale), locale);
  }

  public static DateTimeFormatter getLocalizedYearMonthFormatter(Locale locale) {
    String localizedPattern = getLocalizedPattern(locale);
    String year = extractPattern(localizedPattern, Pattern.compile("(y+)"));
    String month = extractPattern(localizedPattern, Pattern.compile("(M+)"));
    String pattern = isYearFirst(locale) ? year + " " + month : month + " " + year;
    return DateTimeFormatter.ofPattern(pattern);
  }

  public static String getLocalizedYearMonthText(String formatted) {
    String[] parts = formatted.split(" ");
    boolean isAllNumeric = Arrays.stream(parts)
        .flatMapToInt(String::chars)
        .allMatch(Character::isDigit);
    return isAllNumeric ? parts[0] + " / " + parts[1] : formatted;
  }

  public static String extractPattern(String source, Pattern pattern) {
    Matcher matcher = pattern.matcher(source);
    return matcher.find() ? matcher.group(1) : "";
  }

  public static boolean isYearFirst(Locale locale) {
    String localizedPattern = getLocalizedPattern(locale);
    int yearIndex = localizedPattern.indexOf('y');
    int monthIndex = localizedPattern.indexOf('M');
    return yearIndex != -1 && monthIndex != -1 && yearIndex < monthIndex;
  }
}
