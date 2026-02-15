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
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    LocalDate date = LocalDate.now(ZoneId.systemDefault());
    CalendarTable calendarTable = new CalendarTable();
    calendarTable.setCurrentLocalDate(date);
    calendarTable.setModel(new CalendarViewTableModel(date));
    YearMonth currentMonth = YearMonth.from(date);
    List<EventPeriod> events = makeSampleEvents(currentMonth);
    JLayer<JTable> layer = new JLayer<>(calendarTable, new EventBarLayerUI(events));
    JScrollPane scroll = new JScrollPane(layer) {
      @Override public void updateUI() {
        super.updateUI();
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
      }
    };
    JScrollPane comp = new JScrollPane(makeLegendPanel(currentMonth, events));
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, comp);
    split.setResizeWeight(.8);
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private List<EventPeriod> makeSampleEvents(YearMonth ym) {
    List<EventPeriod> events = new ArrayList<>();
    // Event 1: 3-day meeting
    events.add(new EventPeriod("Project Meeting",
        LocalDate.of(ym.getYear(), ym.getMonth(), 5),
        LocalDate.of(ym.getYear(), ym.getMonth(), 7),
        new Color(100, 150, 255, 180)));

    // Event 2: 1-week training (overlaps with Event 1)
    events.add(new EventPeriod("New Employee Training",
        LocalDate.of(ym.getYear(), ym.getMonth(), 6),
        LocalDate.of(ym.getYear(), ym.getMonth(), 12),
        new Color(255, 180, 100, 180)));

    // Event 3: 2-day event
    events.add(new EventPeriod("Exhibition",
        LocalDate.of(ym.getYear(), ym.getMonth(), 20),
        LocalDate.of(ym.getYear(), ym.getMonth(), 21),
        new Color(150, 255, 150, 180)));

    // Event 4: Long-term task until month-end
    events.add(new EventPeriod("Year-End Processing",
        LocalDate.of(ym.getYear(), ym.getMonth(), 18),
        LocalDate.of(ym.getYear(), ym.getMonth(), ym.lengthOfMonth()),
        new Color(255, 150, 200, 180)));

    // Event 5: Another task overlapping with Event 4
    events.add(new EventPeriod("System Maintenance",
        LocalDate.of(ym.getYear(), ym.getMonth(), 22),
        LocalDate.of(ym.getYear(), ym.getMonth(), 26),
        new Color(200, 150, 255, 180)));
    return events;
  }

  private JPanel makeLegendPanel(YearMonth currentMonth, List<EventPeriod> events) {
    Locale locale = Locale.getDefault();
    DateTimeFormatter fmt = CalendarUtils.getLocalizedYearMonthFormatter(locale);
    String txt = currentMonth.format(fmt.withLocale(locale));
    String title = CalendarUtils.getLocalizedYearMonthText(txt);
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setBorder(BorderFactory.createTitledBorder(title));
    events.forEach(ev -> {
      JLabel label = new JLabel(String.format("%s (%d/%d-%d/%d)",
          ev.getName(),
          ev.getStartDate().getMonthValue(), ev.getStartDate().getDayOfMonth(),
          ev.getEndDate().getMonthValue(), ev.getEndDate().getDayOfMonth()));
      label.setOpaque(true);
      label.setBackground(ev.getColor());
      label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
      panel.add(label);
    });
    return panel;
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

class CalendarTable extends JTable {
  private LocalDate currentLocalDate;

  @Override public void updateUI() {
    super.updateUI();
    setDefaultRenderer(LocalDate.class, new CalendarCellRenderer());
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setCellSelectionEnabled(true);
    setFillsViewportHeight(true);
    setRowHeight(64);
    JTableHeader header = getTableHeader();
    header.setResizingAllowed(false);
    header.setReorderingAllowed(false);
    TableCellRenderer headerRenderer = header.getDefaultRenderer();
    if (headerRenderer instanceof JLabel) {
      ((JLabel) headerRenderer).setHorizontalAlignment(SwingConstants.CENTER);
    }
  }

  public void setCurrentLocalDate(LocalDate date) {
    currentLocalDate = date;
  }

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }
}

class CalendarCellRenderer extends DefaultTableCellRenderer {
  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, false, false, row, column);
    if (c instanceof JLabel && table instanceof CalendarTable && value instanceof LocalDate) {
      JLabel dayLabel = (JLabel) c;
      dayLabel.setHorizontalAlignment(CENTER);
      dayLabel.setVerticalAlignment(TOP);
      CalendarTable calendarTable = (CalendarTable) table;
      LocalDate date = (LocalDate) value;
      boolean isToday = date.equals(LocalDate.now(ZoneId.systemDefault()));
      int day = date.getDayOfMonth();
      String txt = isToday ? getCircledNumber(day) : Integer.toString(day);
      dayLabel.setText(txt);
      LocalDate currentDate = calendarTable.getCurrentLocalDate();
      dayLabel.setForeground(getDayOfWeekColor(date, currentDate, isToday));
    }
    return c;
  }

  private Color getDayOfWeekColor(LocalDate date, LocalDate currentDate, boolean isToday) {
    Color color;
    boolean isCurrentMonth = date.getMonth() == currentDate.getMonth();
    if (isCurrentMonth) {
      DayOfWeek dow = date.getDayOfWeek();
      if (isToday) {
        color = new Color(255, 100, 0);
      } else if (dow == DayOfWeek.SUNDAY) {
        color = new Color(255, 100, 100);
      } else if (dow == DayOfWeek.SATURDAY) {
        color = new Color(100, 100, 255);
      } else {
        color = Color.BLACK;
      }
    } else {
      color = Color.LIGHT_GRAY;
    }
    return color;
  }

  /**
   * Convert numbers to circled numbers.
   */
  private String getCircledNumber(int number) {
    String txt;
    if (number >= 1 && number <= 20) {
      // 1-20 (U+2460 - U+2473)
      txt = String.valueOf((char) (0x2460 + number - 1));
    } else if (number >= 21 && number <= 31) {
      // 21-31 (U+3251 - U+325B)
      txt = String.valueOf((char) (0x3251 + number - 21));
    } else {
      txt = String.valueOf(number);
    }
    return txt;
  }
}

class CalendarViewTableModel extends DefaultTableModel {
  public static final int WEEKS = 6;
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

@SuppressWarnings("PMD.DataClass")
final class EventPeriod {
  private final String name;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final Color color;
  private int track; // Track Number (for duplicate avoidance)

  /* default */ EventPeriod(String name, LocalDate startDate, LocalDate endDate, Color color) {
    this.name = name;
    this.startDate = startDate;
    this.endDate = endDate;
    this.color = color;
  }

  public String getName() {
    return name;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public Color getColor() {
    return color;
  }

  public int getTrack() {
    return track;
  }

  public void setTrack(int trackNum) {
    this.track = trackNum;
  }
}

class EventBarLayerUI extends LayerUI<JTable> {
  private static final int BAR_HEIGHT = 10;
  private static final int BAR_MARGIN = 2;
  private final List<EventPeriod> events;

  protected EventBarLayerUI(List<EventPeriod> events) {
    super();
    this.events = events;
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    JTable table = (JTable) ((JLayer<?>) c).getView();

    // Assign tracks (lanes) to events
    assignTracksToEvents();

    // Draw a color bar for each event
    for (EventPeriod ev : events) {
      drawEventBars(g2, table, ev);
    }
    g2.dispose();
  }

  /**
   * Assign track numbers to overlapping events.
   */
  private void assignTracksToEvents() {
    int[] tracks = new int[events.size()];
    boolean[] usedTracks = new boolean[events.size()];
    for (int i = 0; i < events.size(); i++) {
      EventPeriod event = events.get(i);
      Arrays.fill(usedTracks, false);
      // Check for overlap with already processed events
      for (int j = 0; j < i; j++) {
        EventPeriod other = events.get(j);
        if (isOverlapping(event, other)) {
          usedTracks[tracks[j]] = true;
        }
      }
      // Assign the smallest available track number
      int track = 0;
      while (track < usedTracks.length && usedTracks[track]) {
        track++;
      }
      tracks[i] = track;
      event.setTrack(track);
    }
  }

  /**
   * Check if two event periods overlap.
   */
  private boolean isOverlapping(EventPeriod e1, EventPeriod e2) {
    boolean b1 = e1.getEndDate().isBefore(e2.getStartDate());
    boolean b2 = e2.getEndDate().isBefore(e1.getStartDate());
    return !(b1 || b2);
  }

  private void drawEventBars(Graphics2D g2, JTable table, EventPeriod event) {
    LocalDate calendarStartDate = (LocalDate) table.getModel().getValueAt(0, 0);
    int daysInTable = DayOfWeek.values().length * CalendarViewTableModel.WEEKS;
    LocalDate current = event.getStartDate();
    while (!current.isAfter(event.getEndDate())) {
      long sinceStart = ChronoUnit.DAYS.between(calendarStartDate, current);
      if (sinceStart >= 0 && sinceStart < daysInTable) {
        int consecutiveDays = getConsecutiveDaysAndPaintBar(g2, table, event, current);
        current = current.plusDays(consecutiveDays);
      } else {
        current = current.plusDays(1);
      }
    }
  }

  private static void drawEventBar(Graphics2D g2, EventPeriod event, Rectangle barRect) {
    Color clr = event.getColor();
    g2.setColor(clr);
    g2.fillRoundRect(barRect.x, barRect.y, barRect.width, barRect.height, 5, 5);
    g2.setColor(clr.darker());
    g2.drawRoundRect(barRect.x, barRect.y, barRect.width, barRect.height, 5, 5);
    boolean b = barRect.width > 60;
    if (b) {
      drawBarTitle(g2, event, barRect);
    }
  }

  private static int getConsecutiveDaysAndPaintBar(
      Graphics2D g2, JTable tbl, EventPeriod ev, LocalDate cur) {
    LocalDate calendarStartDate = (LocalDate) tbl.getModel().getValueAt(0, 0);
    long sinceStart = ChronoUnit.DAYS.between(calendarStartDate, cur);
    int trackOffset = ev.getTrack() * (BAR_HEIGHT + BAR_MARGIN);
    int headerHeight = tbl.getTableHeader().getHeight();
    long daysInWeek = DayOfWeek.values().length;
    int weekRow = (int) (sinceStart / daysInWeek);
    int dayCol = (int) (sinceStart % daysInWeek);

    int consecutiveDays = 1;
    LocalDate nextDay = cur.plusDays(1);
    boolean notEndOfWeek = dayCol != daysInWeek - 1;
    while (!nextDay.isAfter(ev.getEndDate()) && notEndOfWeek) {
      consecutiveDays++;
      nextDay = nextDay.plusDays(1);
      if (dayCol + consecutiveDays >= daysInWeek) {
        break;
      }
    }

    Rectangle firstRect = tbl.getCellRect(weekRow, dayCol, false);
    Rectangle lastRect = tbl.getCellRect(weekRow, dayCol + consecutiveDays - 1, false);

    int barX = firstRect.x + 5;
    int barY = firstRect.y + trackOffset + headerHeight;
    int barWidth = lastRect.x + lastRect.width - firstRect.x - 10;
    drawEventBar(g2, ev, new Rectangle(barX, barY, barWidth, BAR_HEIGHT));
    return consecutiveDays;
  }

  private static void drawBarTitle(Graphics2D g2, EventPeriod event, Rectangle rect) {
    g2.setColor(Color.BLACK);
    g2.setFont(g2.getFont().deriveFont(9f));
    FontMetrics fm = g2.getFontMetrics();
    String eventName = event.getName();
    int textWidth = fm.stringWidth(eventName);
    if (textWidth > rect.width - 6) {
      eventName = eventName.substring(0, Math.min(eventName.length(), 5)) + "...";
    }
    int textX = rect.x + 3;
    int textY = rect.y + rect.height / 2 + fm.getAscent() / 2 - 1;
    g2.drawString(eventName, textX, textY);
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
