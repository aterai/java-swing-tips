// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public final class MainPanel extends JPanel {
  // public static final String[] CIRCLED_NUMBER = {
  //     "①", "②", "③", "④", "⑤", "⑥", "⑦",
  //     "⑧", "⑨", "⑩", "⑪", "⑫", "⑬", "⑭",
  //     "⑮", "⑯", "⑰", "⑱", "⑲", "⑳", "㉑",
  //     "㉒", "㉓", "㉔", "㉕", "㉖", "㉗", "㉘",
  //     "㉙", "㉚", "㉛"
  // };
  // public static final String CIRCLED_IDEOGRAPH_CONGRATULATION = "㊗"; // "\u3297";
  public final LocalDate realLocalDate = LocalDate.now(ZoneId.systemDefault());
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

    updateMonthView(realLocalDate);

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
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  public void updateMonthView(LocalDate localDate) {
    currentLocalDate = localDate;
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy / MM");
    monthLabel.setText(localDate.format(fmt.withLocale(Locale.getDefault())));
    monthTable.setModel(new CalendarViewTableModel(localDate));
  }

  private class CalendarTableRenderer implements TableCellRenderer {
    private final JPanel renderer = new JPanel(new FlowLayout(FlowLayout.LEADING, 1, 1));
    private final JLabel label = new EnclosedLabel();
    // private final JLabel holiday = new EnclosedLabel();

    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      renderer.setOpaque(true);
      renderer.removeAll();
      renderer.add(label);
      label.setOpaque(false);

      if (value instanceof LocalDate) {
        LocalDate d = (LocalDate) value;
        updateEnclosedLabel(label, d);
        // if (isJapaneseNationalHoliday(d)) {
        //   holiday.setText(CIRCLED_IDEOGRAPH_CONGRATULATION);
        //   holiday.setForeground(Color.WHITE);
        //   holiday.setBackground(Color.BLACK);
        //   renderer.add(holiday);
        // }
        if (selected) {
          renderer.setBackground(table.getSelectionBackground());
        } else if (d.isEqual(realLocalDate)) {
          renderer.setBackground(new Color(0xDC_FF_DC));
        } else {
          renderer.setBackground(getDayOfWeekColor(d.getDayOfWeek()));
        }
      }
      return renderer;
    }

    private void updateEnclosedLabel(JLabel lbl, LocalDate d) {
      lbl.setText("<html><b>" + Objects.toString(d.getDayOfMonth()));
      // label.setText(getDayOfWeekText(d));
      boolean isThisMonth = YearMonth.from(d).equals(YearMonth.from(getCurrentLocalDate()));
      if (isThisMonth && (d.getDayOfWeek() == DayOfWeek.SUNDAY || isJapaneseNationalHoliday(d))) {
        lbl.setForeground(Color.WHITE);
        lbl.setBackground(Color.BLACK);
      } else if (isThisMonth && d.getDayOfWeek() == DayOfWeek.SATURDAY) {
        lbl.setForeground(Color.WHITE);
        lbl.setBackground(Color.BLUE);
      } else if (isThisMonth) {
        lbl.setBackground(Color.WHITE);
        lbl.setForeground(Color.BLACK);
      } else {
        lbl.setBackground(Color.WHITE);
        lbl.setForeground(Color.GRAY);
        lbl.setText(Objects.toString(d.getDayOfMonth()));
      }
    }

    private Color getDayOfWeekColor(DayOfWeek dow) {
      switch (dow) {
        case SUNDAY: return new Color(0xFF_DC_DC);
        case SATURDAY: return new Color(0xDC_DC_FF);
        default: return Color.WHITE;
      }
    }

    protected boolean isJapaneseNationalHoliday(LocalDate d) {
      return LocalDate.of(2020, 7, 23).equals(d)
          || LocalDate.of(2020, 7, 24).equals(d);
    }
  }

  // public static String getDayOfWeekText(LocalDate d) {
  //   switch (d.getDayOfWeek()) {
  //     case SUNDAY:
  //     case SATURDAY: return CIRCLED_NUMBER[d.getDayOfMonth() - 1];
  //     default: return Objects.toString(d.getDayOfMonth());
  //   }
  // }

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

class EnclosedLabel extends JLabel {
  protected EnclosedLabel() {
    super("", SwingConstants.CENTER);
  }

  @Override public void updateUI() {
    super.updateUI();
    setBorder(BorderFactory.createEmptyBorder(2, 0, 3, 1));
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.width = 18;
    return d;
  }

  @Override protected void paintComponent(Graphics g) {
    if (!Objects.equals(getBackground(), Color.WHITE)) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(getBackground());
      g2.fill(getShape());
      g2.dispose();
    }
    super.paintComponent(g);
  }

  protected Shape getShape() {
    Dimension d = getSize();
    if (Objects.equals(getBackground(), Color.BLUE)) {
      return new Ellipse2D.Double(0d, 0d, d.width - 1d, d.height - 1d);
    } else {
      return new RoundRectangle2D.Double(0d, 0d, d.width - 1d, d.height - 1d, 5d, 5d);
    }
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
