// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public final class MainPanel extends JPanel {
  private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy / MM");
  private final JLabel dateLabel = new JLabel("", SwingConstants.CENTER);
  private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
  private final CalendarTable monthTable = new CalendarTable();
  private final JScrollPane scroll = new JScrollPane(monthTable);

  private MainPanel() {
    super(new BorderLayout());
    // The renderer, selection mode, row height and header settings
    // are configured in CalendarTable#updateUI()
    ListSelectionListener selectionListener = e -> {
      int row = monthTable.getSelectedRow();
      int column = monthTable.getSelectedColumn();
      // Ignore the intermediate state where only one of the row or column is selected
      if (!e.getValueIsAdjusting() && row >= 0 && column >= 0) {
        LocalDate date = (LocalDate) monthTable.getValueAt(row, column);
        dateLabel.setText(date.toString());
      }
    };
    monthTable.getSelectionModel().addListSelectionListener(selectionListener);
    monthTable.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);

    Locale defaultLocale = Locale.getDefault();
    LocalDate today = monthTable.getToday();
    LocalDate date = getTopLeftCellDayOfMonth(today, defaultLocale);
    monthTable.setModel(new CalendarViewTableModel(date, defaultLocale));

    JScrollBar verticalScrollBar = new JScrollBar(Adjustable.VERTICAL) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = 0;
        return d;
      }
    };
    verticalScrollBar.setUnitIncrement(monthTable.getRowHeight());
    scroll.setVerticalScrollBar(verticalScrollBar);
    verticalScrollBar.getModel().addChangeListener(e -> verticalScrollChanged());

    updateMonthView(today);

    add(createButtonBox(), BorderLayout.NORTH);
    add(scroll);
    add(dateLabel, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  private JPanel createButtonBox() {
    JButton prev = new JButton("<");
    prev.addActionListener(e -> {
      LocalDate date = monthTable.getCurrentLocalDate();
      updateMonthView(date.minusMonths(1));
    });
    JButton next = new JButton(">");
    next.addActionListener(e -> {
      LocalDate date = monthTable.getCurrentLocalDate();
      updateMonthView(date.plusMonths(1));
    });
    JPanel p = new JPanel(new BorderLayout());
    p.add(monthLabel);
    p.add(prev, BorderLayout.WEST);
    p.add(next, BorderLayout.EAST);
    return p;
  }

  private void verticalScrollChanged() {
    EventQueue.invokeLater(this::updateCurrentMonth);
  }

  private void updateCurrentMonth() {
    JViewport viewport = scroll.getViewport();
    Point pt = SwingUtilities.convertPoint(viewport, 0, 0, monthTable);
    int row = monthTable.rowAtPoint(pt);
    if (row >= 0) {
      // The top-right cell (the last day of the top week) always belongs to
      // the month whose first week is displayed at the top
      int column = monthTable.getColumnCount() - 1;
      LocalDate date = (LocalDate) monthTable.getValueAt(row, column);
      monthTable.setCurrentLocalDate(date);
      updateMonthLabel(date);
      viewport.repaint();
    }
  }

  private void updateMonthView(LocalDate date) {
    monthTable.setCurrentLocalDate(date);
    updateMonthLabel(date);
    // The center row of the model is the first week of this month (today)
    int centerRow = monthTable.getRowCount() / 2;
    Locale locale = monthTable.getLocale();
    LocalDate baseDate = getTopLeftCellDayOfMonth(monthTable.getToday(), locale);
    LocalDate targetDate = getTopLeftCellDayOfMonth(date, locale);
    int weeks = (int) ChronoUnit.WEEKS.between(baseDate, targetDate);
    Rectangle r = monthTable.getCellRect(centerRow + weeks, 0, false);
    r.height = scroll.getViewport().getViewRect().height;
    monthTable.scrollRectToVisible(r);
    scroll.repaint();
  }

  private void updateMonthLabel(LocalDate date) {
    Locale locale = monthLabel.getLocale();
    monthLabel.setText(date.format(MONTH_FORMATTER.withLocale(locale)));
  }

  private static LocalDate getTopLeftCellDayOfMonth(LocalDate date, Locale locale) {
    WeekFields weekFields = WeekFields.of(locale);
    LocalDate firstDayOfMonth = date.withDayOfMonth(1);
    int offset = firstDayOfMonth.get(weekFields.dayOfWeek()) - 1;
    return firstDayOfMonth.minusDays(offset);
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
  private final LocalDate today = LocalDate.now(ZoneId.systemDefault());
  private LocalDate currentLocalDate;

  @Override public void updateUI() {
    super.updateUI();
    setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setCellSelectionEnabled(true);
    setFillsViewportHeight(true);
    setRowHeight(32);
    JTableHeader header = getTableHeader();
    header.setResizingAllowed(false);
    header.setReorderingAllowed(false);
    TableCellRenderer renderer = header.getDefaultRenderer();
    if (renderer instanceof JLabel) {
      ((JLabel) renderer).setHorizontalAlignment(SwingConstants.CENTER);
    }
  }

  public LocalDate getToday() {
    return today;
  }

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }

  public void setCurrentLocalDate(LocalDate date) {
    currentLocalDate = date;
  }
}

class CalendarTableRenderer extends DefaultTableCellRenderer {
  private static final Color TODAY_BGC = new Color(0xDC_FF_DC);
  private static final Color SUNDAY_BGC = new Color(0xFF_DC_DC);
  private static final Color SATURDAY_BGC = new Color(0xDC_DC_FF);

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, selected, focused, row, column);
    if (c instanceof JLabel && value instanceof LocalDate && table instanceof CalendarTable) {
      JLabel l = (JLabel) c;
      l.setHorizontalAlignment(CENTER);
      LocalDate d = (LocalDate) value;
      l.setText(Integer.toString(d.getDayOfMonth()));
      CalendarTable calendar = (CalendarTable) table;
      LocalDate current = calendar.getCurrentLocalDate();
      if (YearMonth.from(d).equals(YearMonth.from(current))) {
        l.setForeground(table.getForeground());
      } else {
        l.setForeground(Color.GRAY);
      }
      DayOfWeek dow = d.getDayOfWeek();
      if (d.isEqual(calendar.getToday())) {
        l.setBackground(TODAY_BGC);
      } else if (dow == DayOfWeek.SUNDAY) {
        l.setBackground(SUNDAY_BGC);
      } else if (dow == DayOfWeek.SATURDAY) {
        l.setBackground(SATURDAY_BGC);
      } else {
        l.setBackground(table.getBackground());
      }
    }
    return c;
  }
}

class CalendarViewTableModel extends AbstractTableModel {
  private static final int WEEK_COUNT = 1000;
  private final LocalDate startDate;
  private final Locale locale;
  private final WeekFields weekFields;

  /* default */ CalendarViewTableModel(LocalDate date, Locale locale) {
    super();
    this.locale = locale;
    weekFields = WeekFields.of(locale);
    startDate = date.minusWeeks(WEEK_COUNT / 2);
  }

  @Override public Class<?> getColumnClass(int column) {
    return LocalDate.class;
  }

  @Override public String getColumnName(int column) {
    return weekFields.getFirstDayOfWeek().plus(column)
        .getDisplayName(TextStyle.SHORT_STANDALONE, locale);
  }

  @Override public int getRowCount() {
    return WEEK_COUNT;
  }

  @Override public int getColumnCount() {
    return DayOfWeek.values().length; // 7 days a week
  }

  @Override public Object getValueAt(int row, int column) {
    return startDate.plusDays((long) row * getColumnCount() + column);
  }

  @Override public boolean isCellEditable(int row, int column) {
    return false;
  }
}
