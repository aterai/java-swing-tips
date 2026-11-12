// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public final class MainPanel extends JPanel {
  private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy / MM");
  private static final List<Color> THEME_COLORS = Arrays.asList(
      new Color(0xD5_0B_17), new Color(0x02_6C_B6), new Color(0xED_87_AD),
      new Color(0xCE_30_6A), new Color(0x48_B0_37), new Color(0xA4_62_A2),
      new Color(0x00_BD_E7), new Color(0xEB_5E_31), new Color(0xC8_01_82),
      new Color(0x8F_19_19), new Color(0x6A_31_8F), new Color(0x00_7A_70));

  private MainPanel() {
    super(new BorderLayout());
    JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    monthLabel.setOpaque(false);
    monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD));
    CalendarTable monthTable = new CalendarTable();
    updateMonthView(monthTable, monthLabel, LocalDate.of(2021, 6, 21));

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
    p.setOpaque(false);
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(monthLabel);
    p.add(prev, BorderLayout.WEST);
    p.add(next, BorderLayout.EAST);

    add(p, BorderLayout.NORTH);
    add(new MonthScrollPane(monthTable));
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void updateMonthView(CalendarTable table, JLabel label, LocalDate date) {
    table.setCurrentLocalDate(date);
    Color color = THEME_COLORS.get(date.getMonthValue() - 1);
    label.setText(date.format(MONTH_FORMATTER));
    label.setForeground(color);
    table.setModel(new CalendarViewTableModel(date));
    table.getTableHeader().setBackground(color);
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
    setViewportBorder(BorderFactory.createEmptyBorder());
    getViewport().setBackground(Color.WHITE);
  }
}

class CalendarTable extends JTable {
  private static final int HEADER_HEIGHT = 24;
  private LocalDate currentLocalDate;

  @Override public void updateUI() {
    super.updateUI();
    setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setCellSelectionEnabled(true);
    setFillsViewportHeight(true);
    setBackground(Color.WHITE);
    setShowVerticalLines(false);
    setShowHorizontalLines(true);
    setIntercellSpacing(new Dimension(0, 1));
    setFont(getFont().deriveFont(Font.BOLD));
    JTableHeader header = getTableHeader();
    header.setForeground(Color.WHITE);
    header.setOpaque(false);
    header.setDefaultRenderer(new RoundedHeaderRenderer());
    header.setResizingAllowed(false);
    header.setReorderingAllowed(false);
  }

  // Fix the header height in the header itself instead of replacing the
  // JScrollPane column header viewport, which would drop the header on updateUI()
  @Override protected JTableHeader createDefaultTableHeader() {
    return new JTableHeader(columnModel) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = HEADER_HEIGHT;
        return d;
      }
    };
  }

  public void setCurrentLocalDate(LocalDate date) {
    currentLocalDate = date;
  }

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }

  private void adjustRowHeights(JViewport viewport) {
    int rowCount = getRowCount();
    if (rowCount == 0) {
      return;
    }
    int height = viewport.getExtentSize().height;
    int baseRowHeight = height / rowCount;
    int remainder = height % rowCount;
    // Distribute the remainder one pixel at a time to the first rows
    for (int i = 0; i < rowCount; i++) {
      int adjustedHeight = Math.max(1, baseRowHeight + (i < remainder ? 1 : 0));
      // Skip unchanged rows: setRowHeight(...) always revalidates the table,
      // which would schedule another doLayout() call
      if (getRowHeight(i) != adjustedHeight) {
        setRowHeight(i, adjustedHeight);
      }
    }
  }

  @Override public boolean getScrollableTracksViewportHeight() {
    return getParent() instanceof JViewport;
  }

  @Override public void doLayout() {
    super.doLayout();
    Container parent = getParent();
    if (parent instanceof JViewport) {
      adjustRowHeights((JViewport) parent);
    }
  }
}

class CalendarTableRenderer extends DefaultTableCellRenderer {
  private static final Color SUNDAY_FGC = new Color(0xD9_0B_0D);
  private static final Color SATURDAY_FGC = new Color(0x10_4A_90);
  private final JPanel panel = new JPanel(new BorderLayout());

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, selected, focused, row, column);
    if (value instanceof LocalDate && c instanceof JLabel && table instanceof CalendarTable) {
      LocalDate d = (LocalDate) value;
      JLabel l = (JLabel) c;
      l.setText(Integer.toString(d.getDayOfMonth()));
      l.setVerticalAlignment(TOP);
      l.setHorizontalAlignment(LEFT);
      panel.setBackground(l.getBackground());
      YearMonth currentMonth = YearMonth.from(((CalendarTable) table).getCurrentLocalDate());
      updateWeekColor(c, d, currentMonth, table, selected);
      LocalDate nextWeekDay = d.plusDays(table.getColumnCount()); // plus 7 days
      boolean isLastRow = row == table.getRowCount() - 1;
      // The last row also shows the date of the following week if it is still in the current month
      if (isLastRow && YearMonth.from(nextWeekDay).equals(currentMonth)) {
        JLabel sub = new JLabel(Integer.toString(nextWeekDay.getDayOfMonth()));
        sub.setFont(l.getFont());
        sub.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        sub.setOpaque(false);
        sub.setVerticalAlignment(BOTTOM);
        sub.setHorizontalAlignment(RIGHT);
        updateWeekColor(sub, nextWeekDay, currentMonth, table, selected);
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

  private static void updateWeekColor(
      Component c, LocalDate d, YearMonth currentMonth, JTable table, boolean selected) {
    if (selected) {
      c.setForeground(table.getSelectionForeground());
    } else {
      DayOfWeek dayOfWeek = d.getDayOfWeek();
      if (dayOfWeek == DayOfWeek.SUNDAY) {
        c.setForeground(SUNDAY_FGC);
      } else if (dayOfWeek == DayOfWeek.SATURDAY) {
        c.setForeground(SATURDAY_FGC);
      } else if (YearMonth.from(d).equals(currentMonth)) {
        c.setForeground(table.getForeground());
      } else {
        c.setForeground(Color.GRAY);
      }
    }
  }
}

class RoundedHeaderRenderer extends DefaultTableCellRenderer {
  private final JLabel firstLabel = new RoundedCornerLabel(true);
  private final JLabel lastLabel = new RoundedCornerLabel(false);

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    JLabel l = this;
    if (column == 0) {
      l = firstLabel;
    } else if (column == table.getColumnCount() - 1) {
      l = lastLabel;
    } else {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
    JTableHeader header = table.getTableHeader();
    l.setText(Objects.toString(value, ""));
    l.setFont(table.getFont());
    l.setForeground(header.getForeground());
    l.setBackground(header.getBackground());
    l.setHorizontalAlignment(CENTER);
    return l;
  }
}

class RoundedCornerLabel extends JLabel {
  private static final double ARC = 16d;
  private final boolean leading;

  protected RoundedCornerLabel(boolean leading) {
    super();
    this.leading = leading;
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(getBackground());
    // Extend the round rectangle beyond the opposite edge so that
    // only the corners on the leading (or trailing) side are rounded
    double x = leading ? 0d : -ARC;
    double w = getWidth() + ARC;
    g2.fill(new RoundRectangle2D.Double(x, 0d, w, getHeight(), ARC, ARC));
    g2.dispose();
    super.paintComponent(g);
  }
}

class CalendarViewTableModel extends AbstractTableModel {
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
    return DayOfWeek.values().length; // 7
  }

  @Override public Object getValueAt(int row, int column) {
    return startDate.plusDays((long) row * getColumnCount() + column);
  }
}

class DiagonallySplitCellLayerUI extends LayerUI<JPanel> {
  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(((JLayer<?>) c).getView().getForeground());
      g2.drawLine(c.getWidth() - 4, 4, 4, c.getHeight() - 4);
      g2.dispose();
    }
  }
}
