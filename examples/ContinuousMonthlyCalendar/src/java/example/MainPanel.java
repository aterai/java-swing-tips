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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private final JLabel dateLabel = new JLabel("", SwingConstants.CENTER);
  private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
  private final MonthTable monthTable = new MonthTable();
  private final JScrollPane scroll = new JScrollPane(monthTable);

  private MainPanel() {
    super(new BorderLayout());
    monthTable.setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());
    monthTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    monthTable.setCellSelectionEnabled(true);
    monthTable.setRowHeight(32);
    monthTable.setFillsViewportHeight(true);

    JTableHeader header = monthTable.getTableHeader();
    header.setResizingAllowed(false);
    header.setReorderingAllowed(false);
    ((JLabel) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

    ListSelectionListener selectionListener = e -> {
      if (!e.getValueIsAdjusting()) {
        int row = monthTable.getSelectedRow();
        int column = monthTable.getSelectedColumn();
        LocalDate ld = (LocalDate) monthTable.getValueAt(row, column);
        dateLabel.setText(ld.toString());
      }
    };
    monthTable.getSelectionModel().addListSelectionListener(selectionListener);
    monthTable.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);

    Locale defaultLocale = Locale.getDefault();
    LocalDate realLocalDate = monthTable.getRealLocalDate();
    LocalDate date = getTopLeftCellDayOfMonth(realLocalDate, defaultLocale);
    CalendarViewTableModel model = new CalendarViewTableModel(date, defaultLocale);
    monthTable.setModel(model);

    JScrollBar verticalScrollBar = new JScrollBar(Adjustable.VERTICAL) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = 0;
        return d;
      }
    };
    verticalScrollBar.setUnitIncrement(monthTable.getRowHeight());
    // verticalScrollBar.setBlockIncrement(monthTable.getRowHeight());
    scroll.setVerticalScrollBar(verticalScrollBar);
    verticalScrollBar.getModel().addChangeListener(e -> verticalScrollChanged());

    updateMonthView(realLocalDate);

    add(makeButtonBox(), BorderLayout.NORTH);
    add(scroll);
    add(dateLabel, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  private JPanel makeButtonBox() {
    JButton prev = new JButton("<");
    prev.addActionListener(e -> {
      LocalDate d = monthTable.getCurrentLocalDate();
      updateMonthView(d.minusMonths(1));
    });
    JButton next = new JButton(">");
    next.addActionListener(e -> {
      LocalDate d = monthTable.getCurrentLocalDate();
      updateMonthView(d.plusMonths(1));
    });
    JPanel p = new JPanel(new BorderLayout());
    p.add(monthLabel);
    p.add(prev, BorderLayout.WEST);
    p.add(next, BorderLayout.EAST);
    return p;
  }

  private void verticalScrollChanged() {
    EventQueue.invokeLater(() -> {
      JViewport viewport = scroll.getViewport();
      Point pt = SwingUtilities.convertPoint(viewport, 0, 0, monthTable);
      int row = monthTable.rowAtPoint(pt);
      int col = 6; // monthTable.columnAtPoint(pt);
      LocalDate localDate = (LocalDate) monthTable.getValueAt(row, col);
      monthTable.setCurrentLocalDate(localDate);
      updateMonthLabel(localDate);
      viewport.repaint();
    });
  }

  public void updateMonthView(LocalDate date) {
    monthTable.setCurrentLocalDate(date);
    updateMonthLabel(date);
    TableModel model = monthTable.getModel();
    int v = model.getRowCount() / 2;
    LocalDate realLocalDate = monthTable.getRealLocalDate();
    Locale locale = monthTable.getLocale();
    LocalDate startDate1 = getTopLeftCellDayOfMonth(realLocalDate, locale);
    LocalDate startDate2 = getTopLeftCellDayOfMonth(date, locale);
    int between = (int) ChronoUnit.WEEKS.between(startDate1, startDate2);
    // monthTable.revalidate();
    Rectangle r = monthTable.getCellRect(v + between, 0, false);
    r.height = scroll.getViewport().getViewRect().height;
    monthTable.scrollRectToVisible(r);
    scroll.repaint();
  }

  private void updateMonthLabel(LocalDate localDate) {
    Locale locale = monthLabel.getLocale();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy / MM");
    monthLabel.setText(localDate.format(fmt.withLocale(locale)));
  }

  private static LocalDate getTopLeftCellDayOfMonth(LocalDate date, Locale loc) {
    WeekFields weekFields = WeekFields.of(loc);
    LocalDate firstDayOfMonth = YearMonth.from(date).atDay(1);
    int v = firstDayOfMonth.get(weekFields.dayOfWeek()) - 1;
    return firstDayOfMonth.minusDays(v);
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

class MonthTable extends JTable {
  private final LocalDate realLocalDate = LocalDate.now(ZoneId.systemDefault());
  private LocalDate currentLocalDate;

  @Override public void updateUI() {
    super.updateUI();
    setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setCellSelectionEnabled(true);
    setFillsViewportHeight(true);
    setRowHeight(20);
    JTableHeader header = getTableHeader();
    header.setResizingAllowed(false);
    header.setReorderingAllowed(false);
    ((JLabel) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
  }

  public LocalDate getRealLocalDate() {
    return realLocalDate;
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
    if (c instanceof JLabel && value instanceof LocalDate && table instanceof MonthTable) {
      JLabel l = (JLabel) c;
      l.setHorizontalAlignment(CENTER);
      LocalDate d = (LocalDate) value;
      l.setText(Integer.toString(d.getDayOfMonth()));
      LocalDate cur = ((MonthTable) table).getCurrentLocalDate();
      if (YearMonth.from(d).equals(YearMonth.from(cur))) {
        l.setForeground(table.getForeground());
      } else {
        l.setForeground(Color.GRAY);
      }
      DayOfWeek dow = d.getDayOfWeek();
      if (d.isEqual(((MonthTable) table).getRealLocalDate())) {
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

class CalendarViewTableModel extends DefaultTableModel {
  private static final int WEEK_COUNT = 1000;
  private final LocalDate startDate;
  private final Locale locale;
  private final WeekFields weekFields;

  protected CalendarViewTableModel(LocalDate date, Locale locale) {
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
    return WEEK_COUNT; // week
  }

  @Override public int getColumnCount() {
    return 7; // day
  }

  @Override public Object getValueAt(int row, int column) {
    return startDate.plusDays((long) row * getColumnCount() + column);
  }

  @Override public boolean isCellEditable(int row, int column) {
    return false;
  }
}
