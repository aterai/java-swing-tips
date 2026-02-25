// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
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
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.LayerUI;
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

    JButton prev = new JButton("<");
    prev.addActionListener(e -> {
      LocalDate d = monthTable.getCurrentLocalDate().minusMonths(1);
      updateMonthView(monthTable, monthLabel, d);
    });
    JButton next = new JButton(">");
    next.addActionListener(e -> {
      LocalDate d = monthTable.getCurrentLocalDate().plusMonths(1);
      updateMonthView(monthTable, monthLabel, d);
    });
    JPanel p = new JPanel(new BorderLayout());
    p.add(monthLabel);
    p.add(prev, BorderLayout.WEST);
    p.add(next, BorderLayout.EAST);

    add(p, BorderLayout.NORTH);
    add(new MonthScrollPane(monthTable));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void updateMonthView(MonthTable table, JLabel label, LocalDate date) {
    table.setCurrentLocalDate(date);
    Locale locale = Locale.getDefault();
    DateTimeFormatter fmt = CalendarUtils.getLocalizedYearMonthFormatter(locale);
    String txt = date.format(fmt.withLocale(locale));
    label.setText(CalendarUtils.getLocalizedYearMonthText(txt));
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
  }

  // @Override public void doLayout() {
  //   JTable tbl = (JTable) getViewport().getView();
  //   int rowCount = tbl.getModel().getRowCount() + 1;
  //   JTableHeader header = tbl.getTableHeader();
  //   Dimension d = header.getPreferredSize();
  //   Rectangle r = SwingUtilities.calculateInnerArea(this, null);
  //   d.height = Math.max(r.height / rowCount, 24);
  //   header.setPreferredSize(d);
  //   super.doLayout();
  // }
}

class MonthTable extends JTable {
  private LocalDate currentLocalDate;

  @Override public void updateUI() {
    super.updateUI();
    setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setCellSelectionEnabled(true);
    setFillsViewportHeight(true);
    JTableHeader header = getTableHeader();
    header.setResizingAllowed(false);
    header.setReorderingAllowed(false);
    updateWeekHeaderRenderer();
  }

  @Override public void setModel(TableModel dataModel) {
    super.setModel(dataModel);
    EventQueue.invokeLater(this::updateWeekHeaderRenderer);
  }

  public void setCurrentLocalDate(LocalDate date) {
    currentLocalDate = date;
  }

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }

  private void updateWeekHeaderRenderer() {
    TableColumnModel cm = getColumnModel();
    TableCellRenderer r = new WeekHeaderRenderer();
    for (int i = 0; i < cm.getColumnCount(); i++) {
      cm.getColumn(i).setHeaderRenderer(r);
    }
    getTableHeader().repaint();
  }

  @Override protected JTableHeader createDefaultTableHeader() {
    return new JTableHeader(columnModel) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        JTable tbl = getTable();
        Container c = SwingUtilities.getAncestorOfClass(JScrollPane.class, tbl);
        if (c instanceof JScrollPane) {
          int rowCount = tbl.getModel().getRowCount() + 1;
          Rectangle r = SwingUtilities.calculateInnerArea((JScrollPane) c, null);
          d.height = Math.max(r.height / rowCount, 24);
        }
        return d;
      }
    };
  }

  @Override public void doLayout() {
    super.doLayout();
    Class<JViewport> clz = JViewport.class;
    Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, this))
        .filter(clz::isInstance).map(clz::cast)
        .ifPresent(this::updateRowsHeight);
  }

  private void updateRowsHeight(JViewport viewport) {
    int height = viewport.getExtentSize().height;
    int rowCount = getModel().getRowCount();
    int defaultRowHeight = height / rowCount;
    int remainder = height % rowCount;
    for (int i = 0; i < rowCount; i++) {
      int a = defaultRowHeight + Math.min(1, Math.max(0, remainder));
      setRowHeight(i, Math.max(1, a));
      remainder -= 1;
    }
  }
}

class CalendarTableRenderer extends DefaultTableCellRenderer {
  private static final Color SUNDAY_FGC = new Color(0xB0_12_1A);
  private static final Color SATURDAY_FGC = new Color(0x1A_12_B0);
  private final JPanel panel = new JPanel(new BorderLayout());
  private LocalDate currentLocalDate;

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, selected, focused, row, column);
    if (value instanceof LocalDate && c instanceof JLabel && table instanceof MonthTable) {
      LocalDate d = (LocalDate) value;
      JLabel l = (JLabel) c;
      l.setText(Integer.toString(d.getDayOfMonth()));
      l.setVerticalAlignment(TOP);
      l.setHorizontalAlignment(LEFT);
      panel.setBackground(l.getBackground());
      currentLocalDate = ((MonthTable) table).getCurrentLocalDate();
      updateWeekColor(d, table, c, selected);
      LocalDate nextWeekDay = d.plusDays(7);
      boolean isLastRow = row == table.getModel().getRowCount() - 1;
      if (isLastRow && isDiagonallySplitCell(nextWeekDay, currentLocalDate)) {
        JLabel sub = new JLabel(Integer.toString(nextWeekDay.getDayOfMonth()));
        sub.setFont(l.getFont());
        sub.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        sub.setOpaque(false);
        sub.setVerticalAlignment(BOTTOM);
        sub.setHorizontalAlignment(RIGHT);
        updateWeekColor(d, table, sub, selected);

        panel.removeAll();
        panel.add(sub, BorderLayout.SOUTH);
        panel.add(c, BorderLayout.NORTH);
        panel.setBorder(l.getBorder());
        l.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        c = new JLayer<>(panel, new DiagonallySplitCellLayerUI());
      }
    }
    return c;
  }

  private static boolean isDiagonallySplitCell(LocalDate nextWeekDay, LocalDate cur) {
    return YearMonth.from(nextWeekDay).equals(YearMonth.from(cur));
  }

  private void updateWeekColor(LocalDate d, JTable table, Component c, boolean selected) {
    if (selected) {
      c.setForeground(table.getSelectionForeground());
    } else {
      DayOfWeek dayOfWeek = d.getDayOfWeek();
      if (dayOfWeek == DayOfWeek.SUNDAY) {
        c.setForeground(SUNDAY_FGC);
      } else if (dayOfWeek == DayOfWeek.SATURDAY) {
        c.setForeground(SATURDAY_FGC);
      } else if (YearMonth.from(d).equals(YearMonth.from(currentLocalDate))) {
        c.setForeground(table.getForeground());
      } else {
        c.setForeground(Color.GRAY);
      }
    }
  }
}

class WeekHeaderRenderer extends DefaultTableCellRenderer {
  public static final Color SUNDAY_BGC = new Color(0xB0_12_1A);
  public static final Color SATURDAY_BGC = new Color(0x1A_12_B0);
  private final WeekFields weekFields = WeekFields.of(Locale.getDefault());

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    DayOfWeek week = weekFields.getFirstDayOfWeek().plus(column);
    if (week == DayOfWeek.SUNDAY) {
      c.setForeground(table.getSelectionForeground());
      c.setBackground(SUNDAY_BGC);
    } else if (week == DayOfWeek.SATURDAY) {
      c.setForeground(table.getSelectionForeground());
      c.setBackground(SATURDAY_BGC);
    } else {
      c.setForeground(table.getForeground());
      c.setBackground(table.getTableHeader().getBackground());
    }
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      l.setHorizontalAlignment(CENTER);
      Color gridColor = UIManager.getColor("Table.gridColor");
      Border border = BorderFactory.createMatteBorder(0, 0, 1, 1, gridColor);
      Border b = BorderFactory.createCompoundBorder(border, l.getBorder());
      l.setBorder(b);
    }
    return c;
  }
}

class CalendarViewTableModel extends DefaultTableModel {
  private final LocalDate startDate;
  private final WeekFields weekFields = WeekFields.of(Locale.getDefault());

  protected CalendarViewTableModel(LocalDate date) {
    super();
    LocalDate firstDayOfMonth = YearMonth.from(date).atDay(1);
    int v = firstDayOfMonth.get(weekFields.dayOfWeek()) - 1;
    startDate = firstDayOfMonth.minusDays(v);
  }

  @Override public Class<?> getColumnClass(int column) {
    return LocalDate.class;
  }

  @Override public String getColumnName(int column) {
    return weekFields.getFirstDayOfWeek().plus(column)
        .getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault());
  }

  @Override public int getRowCount() {
    return 5;
  }

  @Override public int getColumnCount() {
    return 7;
  }

  @Override public Object getValueAt(int row, int column) {
    return startDate.plusDays((long) row * getColumnCount() + column);
  }

  @Override public boolean isCellEditable(int row, int column) {
    return false;
  }
}

class DiagonallySplitCellLayerUI extends LayerUI<JPanel> {
  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(UIManager.getColor("Table.gridColor"));
      g2.drawLine(c.getWidth(), 0, 0, c.getHeight());
      g2.dispose();
    }
  }
}

final class CalendarUtils {
  private CalendarUtils() {
    /* Singleton */
  }

  public static String getLocalizedPattern(Locale locale) {
    // DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG, locale);
    // return ((SimpleDateFormat) formatter).toLocalizedPattern();
    return DateTimeFormatterBuilder.getLocalizedDateTimePattern(
        FormatStyle.LONG, null, Chronology.ofLocale(locale), locale);
  }

  public static DateTimeFormatter getLocalizedYearMonthFormatter(Locale locale) {
    String localizedPattern = getLocalizedPattern(locale);
    String year = find(localizedPattern, Pattern.compile("(y+)"));
    String month = find(localizedPattern, Pattern.compile("(M+)"));
    String pattern = isYearFirst(locale) ? year + " " + month : month + " " + year;
    return DateTimeFormatter.ofPattern(pattern);
  }

  public static String getLocalizedYearMonthText(String str) {
    String[] list = str.split(" ");
    String txt;
    boolean isNumeric = Arrays.stream(list)
        .flatMapToInt(String::chars)
        .allMatch(Character::isDigit);
    if (isNumeric) {
      txt = list[0] + " / " + list[1];
    } else {
      txt = str;
    }
    return txt;
  }

  public static String find(String str, Pattern ptn) {
    Matcher matcher = ptn.matcher(str);
    return matcher.find() ? matcher.group(1) : "";
  }

  public static boolean isYearFirst(Locale locale) {
    String localizedPattern = getLocalizedPattern(locale);
    int yearIndex = localizedPattern.indexOf('y');
    int monthIndex = localizedPattern.indexOf('M');
    return yearIndex != -1 && monthIndex != -1 && yearIndex < monthIndex;
  }
}
