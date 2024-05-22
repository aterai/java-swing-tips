// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  @SuppressWarnings("PMD.UseConcurrentHashMap")
  private final Map<DayOfWeek, Color> holidayColorMap = new EnumMap<>(DayOfWeek.class);
  private final LocalDate realLocalDate = LocalDate.now(ZoneId.systemDefault());
  private final JComboBox<String> combo = new JComboBox<>();
  private final JTable monthTable = new JTable();
  private LocalDate currentLocalDate;

  private MainPanel() {
    super(new BorderLayout(2, 2));
    holidayColorMap.put(DayOfWeek.SUNDAY, new Color(0xFF_DC_DC));
    holidayColorMap.put(DayOfWeek.SATURDAY, new Color(0xDC_DC_FF));

    TableModel model = new CalendarViewTableModel(YearMonth.from(realLocalDate));
    monthTable.setModel(model);
    TableRowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
    monthTable.setRowSorter(sorter);
    monthTable.setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());

    JTableHeader header = monthTable.getTableHeader();
    ((JLabel) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

    JButton prev = new JButton("<");
    prev.addActionListener(e -> updateMonthView(getCurrentLocalDate().minusMonths(1)));

    JButton next = new JButton(">");
    next.addActionListener(e -> updateMonthView(getCurrentLocalDate().plusMonths(1)));

    String[] cm = {
        "1 month",
        "within 3 days before",
        "within 1 week before",
        "1 week before and after",
        "within 1 week after"
    };
    combo.setModel(new DefaultComboBoxModel<>(cm));
    combo.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        sorter.setRowFilter(makeRowFilter(Objects.toString(e.getItem())));
      }
    });

    updateMonthView(realLocalDate);

    JPanel p = new JPanel(new BorderLayout());
    p.add(prev, BorderLayout.WEST);
    p.add(next, BorderLayout.EAST);

    add(combo, BorderLayout.NORTH);
    add(new JScrollPane(monthTable));
    add(p, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }

  public void updateMonthView(LocalDate date) {
    currentLocalDate = date;
    monthTable.setModel(new CalendarViewTableModel(YearMonth.from(date)));
    TableColumn col = monthTable.getColumnModel().getColumn(0);
    col.setMaxWidth(100);
    col.setResizable(false);
    combo.setSelectedIndex(0);
  }

  @SuppressWarnings("PMD.OnlyOneReturn")
  public RowFilter<TableModel, Integer> makeRowFilter(String selected) {
    switch (selected) {
      case "within 3 days before":
        return new LocalDateFilter(realLocalDate.minusDays(3).plusDays(1), realLocalDate, 0);
      case "within 1 week before":
        return new LocalDateFilter(realLocalDate.minusWeeks(1).plusDays(1), realLocalDate, 0);
      case "1 week before and after":
        return new LocalDateFilter(realLocalDate.minusDays(3), realLocalDate.plusDays(3), 0);
      case "within 1 week after":
        return new LocalDateFilter(realLocalDate, realLocalDate.plusWeeks(1).minusDays(1), 0);
      default:
        return null;
    }
  }

  private final class CalendarTableRenderer extends DefaultTableCellRenderer {
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      Component c = super.getTableCellRendererComponent(
          table, value, selected, focused, row, column);
      if (value instanceof LocalDate && c instanceof JLabel) {
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
      return Optional.ofNullable(holidayColorMap.get(dow)).orElse(Color.WHITE);
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
  private final YearMonth currentMonth;

  protected CalendarViewTableModel(YearMonth month) {
    super(month.lengthOfMonth(), 2);
    currentMonth = month;
  }

  @Override public Class<?> getColumnClass(int column) {
    return column == 0 ? LocalDate.class : Object.class;
  }

  @Override public String getColumnName(int column) {
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
    return column == 0 ? currentMonth.format(fmt.withLocale(Locale.getDefault())) : "";
  }

  @Override public Object getValueAt(int row, int column) {
    return column == 0 ? currentMonth.atDay(1).plusDays(row) : super.getValueAt(row, column);
  }

  @Override public boolean isCellEditable(int row, int column) {
    return column != 0;
  }
}

class LocalDateFilter extends RowFilter<TableModel, Integer> {
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final int column;

  protected LocalDateFilter(LocalDate startDate, LocalDate endDate, int column) {
    super();
    this.startDate = startDate;
    this.endDate = endDate;
    // checkIndices(columns);
    this.column = column;
  }

  @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
    Object v = entry.getModel().getValueAt(entry.getIdentifier(), column);
    return v instanceof LocalDate && between((LocalDate) v);
  }

  private boolean between(LocalDate date) {
    return !(startDate.isAfter(date) || endDate.isBefore(date));
  }

  // // @see RowFilter.GeneralFilter
  // @Override public boolean include(Entry<? extends TableModel,? extends Integer> value){
  //   int count = value.getValueCount();
  //   if (columns.length > 0) {
  //     for (int i = columns.length - 1; i >= 0; i--) {
  //       int index = columns[i];
  //       if (index < count) {
  //         if (include(value, index)) {
  //           return true;
  //         }
  //       }
  //     }
  //   } else {
  //     while (--count >= 0) {
  //       if (include(value, count)) {
  //         return true;
  //       }
  //     }
  //   }
  //   return false;
  // }

  // protected boolean include(Entry<? extends TableModel, ? extends Integer> entry, int index) {
  //   Object v = entry.getValue(index);
  //   if (v instanceof LocalDate) {
  //     LocalDate date = (LocalDate) v;
  //     return !(startDate.isAfter(date) || endDate.isBefore(date));
  //   }
  //   return false;
  // }

  // private static void checkIndices(int[] columns) {
  //   for (int i = columns.length - 1; i >= 0; i--) {
  //     if (columns[i] < 0) {
  //       throw new IllegalArgumentException("Index must be >= 0");
  //     }
  //   }
  // }
}
