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
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public final class MainPanel extends JPanel {
  public final LocalDate realLocalDate = LocalDate.now(ZoneId.systemDefault());
  @SuppressWarnings("PMD.UseConcurrentHashMap")
  private final Map<DayOfWeek, Color> holidayColorMap = new EnumMap<>(DayOfWeek.class);
  private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
  private final JTable monthTable = new JTable();
  private LocalDate currentLocalDate;

  private MainPanel() {
    super(new BorderLayout());
    monthTable.setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());
    monthTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    monthTable.setCellSelectionEnabled(true);
    monthTable.setRowHeight(18);
    monthTable.setFillsViewportHeight(true);

    holidayColorMap.put(DayOfWeek.SUNDAY, new Color(0xFF_DC_DC));
    holidayColorMap.put(DayOfWeek.SATURDAY, new Color(0xDC_DC_FF));

    JTableHeader header = monthTable.getTableHeader();
    header.setResizingAllowed(false);
    header.setReorderingAllowed(false);
    ((JLabel) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

    updateMonthView(realLocalDate);

    JButton prev = new JButton("<");
    prev.addActionListener(e -> updateMonthView(getCurrentLocalDate().minusMonths(1)));

    JButton next = new JButton(">");
    next.addActionListener(e -> updateMonthView(getCurrentLocalDate().plusMonths(1)));

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
    add(new JScrollPane(monthTable));
    add(log, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }

  public void updateMonthView(LocalDate localDate) {
    currentLocalDate = localDate;
    Locale locale = Locale.getDefault();
    DateTimeFormatter fmt = CalendarUtils.getLocalizedYearMonthFormatter(locale);
    String txt = localDate.format(fmt.withLocale(locale));
    monthLabel.setText(CalendarUtils.getLocalizedYearMonthText(txt));
    monthTable.setModel(new CalendarViewTableModel(localDate));
  }

  private final class CalendarTableRenderer extends DefaultTableCellRenderer {
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      Component c = super.getTableCellRendererComponent(
          table, value, selected, focused, row, column);
      if (c instanceof JLabel && value instanceof LocalDate) {
        JLabel l = (JLabel) c;
        l.setHorizontalAlignment(CENTER);
        LocalDate d = (LocalDate) value;
        l.setText(Integer.toString(d.getDayOfMonth()));
        if (YearMonth.from(d).equals(YearMonth.from(getCurrentLocalDate()))) {
          l.setForeground(table.getForeground());
        } else {
          l.setForeground(Color.GRAY);
        }
        if (d.isEqual(realLocalDate)) {
          l.setBackground(new Color(0xDC_FF_DC));
        } else {
          l.setBackground(getDayOfWeekColor(table, d.getDayOfWeek()));
        }
      }
      return c;
    }

    private Color getDayOfWeekColor(JTable table, DayOfWeek dow) {
      return Optional.ofNullable(holidayColorMap.get(dow)).orElse(table.getBackground());
    }
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
