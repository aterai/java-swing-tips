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
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    LocalDate now = LocalDate.now(ZoneId.systemDefault());
    JLabel dateLabel = new JLabel(now.toString(), SwingConstants.CENTER);
    JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    MonthTable monthTable = new MonthTable();
    updateMonthView(monthTable, monthLabel, now);

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
    add(new JScrollPane(monthTable));
    add(dateLabel, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void updateMonthView(MonthTable table, JLabel label, LocalDate date) {
    table.setCurrentLocalDate(date);
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy / MM");
    label.setText(date.format(fmt.withLocale(Locale.getDefault())));
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

class MonthTable extends JTable {
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
      if (d.isEqual(cur)) {
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
