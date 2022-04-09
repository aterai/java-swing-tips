// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public final class MainPanel extends JPanel {
  private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
  private final JTable monthTable = new JTable() {
    private void updateRowsHeight(JViewport vport) {
      int height = vport.getExtentSize().height;
      int rowCount = getModel().getRowCount();
      int defaultRowHeight = height / rowCount;
      int remainder = height % rowCount;
      for (int i = 0; i < rowCount; i++) {
        int a = Math.min(1, Math.max(0, remainder--));
        setRowHeight(i, defaultRowHeight + a);
      }
    }

    @Override public void doLayout() {
      super.doLayout();
      Class<JViewport> clz = JViewport.class;
      Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, this))
          .filter(clz::isInstance).map(clz::cast)
          .ifPresent(this::updateRowsHeight);
    }
  };
  private LocalDate currentLocalDate;

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }

  private MainPanel() {
    super(new BorderLayout());

    monthTable.setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());
    monthTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    monthTable.setCellSelectionEnabled(true);
    monthTable.setFillsViewportHeight(true);

    JTableHeader header = monthTable.getTableHeader();
    header.setResizingAllowed(false);
    header.setReorderingAllowed(false);
    ((JLabel) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

    updateMonthView(LocalDate.of(2020, 8, 1));

    JButton prev = new JButton("<");
    prev.addActionListener(e -> updateMonthView(getCurrentLocalDate().minusMonths(1)));

    JButton next = new JButton(">");
    next.addActionListener(e -> updateMonthView(getCurrentLocalDate().plusMonths(1)));

    JPanel p = new JPanel(new BorderLayout());
    p.add(monthLabel);
    p.add(prev, BorderLayout.WEST);
    p.add(next, BorderLayout.EAST);

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(monthTable));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public void updateMonthView(LocalDate localDate) {
    currentLocalDate = localDate;
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy / MM");
    monthLabel.setText(localDate.format(fmt.withLocale(Locale.getDefault())));
    monthTable.setModel(new CalendarViewTableModel(localDate));
    // EventQueue.invokeLater(monthTable::doLayout);
  }

  private class CalendarTableRenderer extends DefaultTableCellRenderer {
    private final JPanel panel = new JPanel(new BorderLayout());

    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      Component c = super.getTableCellRendererComponent(
          table, value, selected, focused, row, column);
      if (value instanceof LocalDate && c instanceof JLabel) {
        LocalDate d = (LocalDate) value;
        JLabel l = (JLabel) c;
        l.setText(Objects.toString(d.getDayOfMonth()));
        l.setVerticalAlignment(SwingConstants.TOP);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        updateCellWeekColor(d, c, c);

        LocalDate nextWeekDay = d.plusDays(7);
        boolean isLastRow = row == table.getModel().getRowCount() - 1;
        if (isLastRow && isDiagonallySplitCell(nextWeekDay)) {
          JLabel sub = new JLabel(Objects.toString(nextWeekDay.getDayOfMonth()));
          sub.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
          sub.setOpaque(false);
          sub.setVerticalAlignment(SwingConstants.BOTTOM);
          sub.setHorizontalAlignment(SwingConstants.RIGHT);

          panel.removeAll();
          panel.add(sub, BorderLayout.SOUTH);
          panel.add(c, BorderLayout.NORTH);
          panel.setBorder(l.getBorder());
          l.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

          updateCellWeekColor(d, sub, panel);
          return new JLayer<>(panel, new DiagonallySplitCellLayerUI());
        }
      }
      return c;
    }

    private boolean isDiagonallySplitCell(LocalDate nextWeekDay) {
      return YearMonth.from(nextWeekDay).equals(YearMonth.from(getCurrentLocalDate()));
    }

    private void updateCellWeekColor(LocalDate d, Component fgc, Component bgc) {
      if (YearMonth.from(d).equals(YearMonth.from(getCurrentLocalDate()))) {
        fgc.setForeground(Color.BLACK);
      } else {
        fgc.setForeground(Color.GRAY);
      }
      bgc.setBackground(getDayOfWeekColor(d.getDayOfWeek()));
    }

    private Color getDayOfWeekColor(DayOfWeek dow) {
      switch (dow) {
        case SUNDAY: return new Color(0xFF_DC_DC);
        case SATURDAY: return new Color(0xDC_DC_FF);
        default: return Color.WHITE;
      }
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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
