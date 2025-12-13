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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

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

    JTextArea log = new JTextArea();
    Arrays.asList(Locale.JAPAN, Locale.US, Locale.FRANCE).forEach(loc -> {
      boolean isYearFirst = CalendarUtils.isYearFirst(loc);
      String pattern = isYearFirst ? "yyyy/MMM" : "MMM/yyyy";
      YearMonth currentYm = YearMonth.now(ZoneId.systemDefault());
      DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern, loc);
      String str = currentYm.format(fmt);
      String lang = loc.toLanguageTag();
      log.append(String.format("%s, %s, isYearFirst? %b%n", lang, str, isYearFirst));
    });

    add(p, BorderLayout.NORTH);
    add(new MonthScrollPane(monthTable));
    add(log, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
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
}

class MonthTable extends JTable {
  private LocalDate currentLocalDate;

  @Override public void updateUI() {
    super.updateUI();
    setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setCellSelectionEnabled(true);
    setFillsViewportHeight(true);
    setRowHeight(18);
    JTableHeader header = getTableHeader();
    header.setResizingAllowed(false);
    header.setReorderingAllowed(false);
    TableCellRenderer r = header.getDefaultRenderer();
    if (r instanceof JLabel) {
      ((JLabel) r).setHorizontalAlignment(SwingConstants.CENTER);
    }
  }

  public void setCurrentLocalDate(LocalDate date) {
    currentLocalDate = date;
  }

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }
}

class CalendarTableRenderer extends DefaultTableCellRenderer {
  private static final Color TODAY_BGC = new Color(0xDC_FF_DC);
  private static final Color SUNDAY_BGC = new Color(0xFF_DC_DC);
  private static final Color SATURDAY_BGC = new Color(0xDC_DC_FF);
  private final LocalDate realLocalDate = LocalDate.now(ZoneId.systemDefault());

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, selected, focused, row, column);
    if (value instanceof LocalDate && table instanceof MonthTable) {
      LocalDate d = (LocalDate) value;
      if (c instanceof JLabel) {
        ((JLabel) c).setHorizontalAlignment(CENTER);
        ((JLabel) c).setText(Integer.toString(d.getDayOfMonth()));
      }
      LocalDate currentLocalDate = ((MonthTable) table).getCurrentLocalDate();
      if (selected) {
        c.setForeground(table.getSelectionForeground());
      } else if (YearMonth.from(d).equals(YearMonth.from(currentLocalDate))) {
        c.setForeground(table.getForeground());
      } else {
        c.setForeground(Color.GRAY);
      }
      if (d.isEqual(realLocalDate)) {
        c.setBackground(TODAY_BGC);
      } else {
        c.setBackground(getDayOfWeekColor(d.getDayOfWeek(), table, selected));
      }
    }
    return c;
  }

  private static Color getDayOfWeekColor(DayOfWeek dow, JTable table, boolean selected) {
    Color color;
    if (selected) {
      color = table.getSelectionBackground();
    } else if (dow == DayOfWeek.SUNDAY) {
      color = SUNDAY_BGC;
    } else if (dow == DayOfWeek.SATURDAY) {
      color = SATURDAY_BGC;
    } else {
      color = table.getBackground();
    }
    return color;
  }
}

class CalendarViewTableModel extends DefaultTableModel {
  private final LocalDate startDate;
  private final WeekFields weekFields = WeekFields.of(Locale.getDefault());

  protected CalendarViewTableModel(LocalDate date) {
    super();
    // LocalDate firstDayOfMonth = date.with(TemporalAdjusters.firstDayOfMonth());
    LocalDate firstDayOfMonth = YearMonth.from(date).atDay(1);
    // int v = firstDayOfMonth.get(WeekFields.SUNDAY_START.dayOfWeek()) - 1;
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
    return 6;
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

  // public static boolean isYearFirst(Locale locale) {
  //   LocalDate sampleLocalDate = LocalDate.of(1111, Month.FEBRUARY, 1);
  //   DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
  //   String formattedStr = sampleLocalDate.format(formatter.withLocale(locale));
  //   String yearStr = String.valueOf(sampleLocalDate.getYear());
  //   String monthStr = sampleLocalDate.getMonth().getDisplayName(TextStyle.FULL, locale);
  //   int yearIndex = formattedStr.indexOf(yearStr);
  //   int monthIndex = formattedStr.indexOf(monthStr);
  //   return yearIndex != -1 && monthIndex != -1 && yearIndex < monthIndex;
  // }

  public static boolean isYearFirst(Locale locale) {
    String localizedPattern = getLocalizedPattern(locale);
    int yearIndex = localizedPattern.indexOf('y');
    int monthIndex = localizedPattern.indexOf('M');
    return yearIndex != -1 && monthIndex != -1 && yearIndex < monthIndex;
  }
}
