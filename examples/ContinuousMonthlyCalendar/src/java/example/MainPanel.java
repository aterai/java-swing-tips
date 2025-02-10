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
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  public final LocalDate realLocalDate = LocalDate.now(ZoneId.systemDefault());
  private final Locale locale1 = Locale.getDefault();
  private final JLabel dateLabel = new JLabel(realLocalDate.toString(), SwingConstants.CENTER);
  private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
  private final JTable monthTable = new JTable();
  private final JScrollPane scroll = new JScrollPane(monthTable);
  private LocalDate currentLocalDate;

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
    LocalDate date = getTopLeftCellDayOfMonth(realLocalDate, locale1);
    CalendarViewTableModel model = new CalendarViewTableModel(date, locale1);
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

    updateMonthView(realLocalDate, locale1);

    JButton prev = new JButton("<");
    prev.addActionListener(e -> updateMonthView(currentLocalDate.minusMonths(1), locale1));

    JButton next = new JButton(">");
    next.addActionListener(e -> updateMonthView(currentLocalDate.plusMonths(1), locale1));

    JPanel p = new JPanel(new BorderLayout());
    p.add(monthLabel);
    p.add(prev, BorderLayout.WEST);
    p.add(next, BorderLayout.EAST);

    add(p, BorderLayout.NORTH);
    add(scroll);
    add(dateLabel, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }

  private void verticalScrollChanged() {
    EventQueue.invokeLater(() -> {
      JViewport viewport = scroll.getViewport();
      Point pt = SwingUtilities.convertPoint(viewport, 0, 0, monthTable);
      int row = monthTable.rowAtPoint(pt);
      int col = 6; // monthTable.columnAtPoint(pt);
      LocalDate localDate = (LocalDate) monthTable.getValueAt(row, col);
      currentLocalDate = localDate;
      updateMonthLabel(localDate, locale1);
      viewport.repaint();
    });
  }

  public void updateMonthView(LocalDate localDate, Locale locale) {
    currentLocalDate = localDate;
    updateMonthLabel(localDate, locale);
    TableModel model = monthTable.getModel();
    int v = model.getRowCount() / 2;
    LocalDate startDate1 = getTopLeftCellDayOfMonth(realLocalDate, locale);
    LocalDate startDate2 = getTopLeftCellDayOfMonth(localDate, locale);
    int between = (int) ChronoUnit.WEEKS.between(startDate1, startDate2);
    // monthTable.revalidate();
    Rectangle r = monthTable.getCellRect(v + between, 0, false);
    r.height = scroll.getViewport().getViewRect().height;
    monthTable.scrollRectToVisible(r);
    scroll.repaint();
  }

  private void updateMonthLabel(LocalDate localDate, Locale locale) {
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy / MM");
    monthLabel.setText(localDate.format(fmt.withLocale(locale)));
  }

  private static LocalDate getTopLeftCellDayOfMonth(LocalDate date, Locale locale) {
    WeekFields weekFields = WeekFields.of(locale);
    LocalDate firstDayOfMonth = YearMonth.from(date).atDay(1);
    int v = firstDayOfMonth.get(weekFields.dayOfWeek()) - 1;
    return firstDayOfMonth.minusDays(v);
  }

  private final class CalendarTableRenderer extends DefaultTableCellRenderer {
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      Component c = super.getTableCellRendererComponent(
          table, value, selected, focused, row, column);
      if (c instanceof JLabel && value instanceof LocalDate) {
        LocalDate d = (LocalDate) value;
        JLabel l = (JLabel) c;
        l.setHorizontalAlignment(CENTER);
        l.setText(Integer.toString(d.getDayOfMonth()));
        if (YearMonth.from(d).equals(YearMonth.from(getCurrentLocalDate()))) {
          l.setForeground(Color.BLACK);
        } else {
          l.setForeground(Color.GRAY);
        }
        if (d.isEqual(realLocalDate)) {
          l.setBackground(new Color(0xDC_FF_DC));
        } else {
          l.setBackground(getDayOfWeekColor(d.getDayOfWeek()));
        }
      }
      return c;
    }

    private Color getDayOfWeekColor(DayOfWeek dow) {
      Color color;
      if (dow == DayOfWeek.SUNDAY) {
        color = new Color(0xFF_DC_DC);
      } else if (dow == DayOfWeek.SATURDAY) {
        color = new Color(0xDC_DC_FF);
      } else {
        color = Color.WHITE;
      }
      return color;
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
      ex.printStackTrace();
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
