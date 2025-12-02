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
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;
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
  // public static final String CIRCLED_IDEOGRAPH_CONGRATULATION = "㊗"; // U+3297

  private MainPanel() {
    super(new BorderLayout());
    JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    MonthTable monthTable = new MonthTable();
    LocalDate realLocalDate = LocalDate.of(2020, 7, 27);
    updateMonthView(monthTable, monthLabel, realLocalDate);

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

    JScrollPane scroll = new JScrollPane(monthTable) {
      @Override public void updateUI() {
        super.updateUI();
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
      }
    };
    add(p, BorderLayout.NORTH);
    add(scroll);
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
    JTableHeader header = getTableHeader();
    header.setResizingAllowed(false);
    header.setReorderingAllowed(false);
  }

  public void setCurrentLocalDate(LocalDate date) {
    currentLocalDate = date;
  }

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }

  @Override public void doLayout() {
    super.doLayout();
    Class<JViewport> clz = JViewport.class;
    Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, this))
        .filter(clz::isInstance).map(clz::cast)
        .ifPresent(this::updateRowsHeight);
  }

  private void updateRowsHeight(JViewport viewport) {
    int height = viewport.getExtentSize().height;
    int rowCount = getModel().getRowCount();
    int defaultRowHeight = height / rowCount;
    int remainder = height % rowCount;
    for (int i = 0; i < rowCount; i++) {
      int a = defaultRowHeight + Math.min(1, Math.max(0, remainder--));
      setRowHeight(i, Math.max(1, a));
    }
  }
}

class CalendarTableRenderer implements TableCellRenderer {
  private final JPanel renderer = new JPanel(new FlowLayout(FlowLayout.LEADING, 1, 1));
  private final EnclosedLabel label = new EnclosedLabel();
  // private final JLabel holiday = new EnclosedLabel();
  private LocalDate currentLocalDate;

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
    renderer.setOpaque(true);
    renderer.removeAll();
    renderer.add(label);
    label.setOpaque(false);
    if (value instanceof LocalDate && table instanceof MonthTable) {
      currentLocalDate = ((MonthTable) table).getCurrentLocalDate();
      LocalDate d = (LocalDate) value;
      updateEnclosedLabel(table, label, d);
      // if (isJapaneseNationalHoliday(d)) {
      //   holiday.setText(CIRCLED_IDEOGRAPH_CONGRATULATION);
      //   holiday.setForeground(Color.WHITE);
      //   holiday.setBackground(Color.BLACK);
      //   renderer.add(holiday);
      // }
      if (selected) {
        renderer.setBackground(table.getSelectionBackground());
      } else if (d.isEqual(currentLocalDate)) {
        renderer.setBackground(new Color(0xDC_FF_DC));
      } else {
        renderer.setBackground(getDayOfWeekColor(table, d.getDayOfWeek()));
      }
    }
    return renderer;
  }

  private void updateEnclosedLabel(JTable table, EnclosedLabel lbl, LocalDate d) {
    String txt = Integer.toString(d.getDayOfMonth());
    lbl.setText("<html><b>" + txt);
    // label.setText(getDayOfWeekText(d));
    boolean isThisMonth = YearMonth.from(d).equals(YearMonth.from(currentLocalDate));
    if (isThisMonth && (d.getDayOfWeek() == DayOfWeek.SUNDAY || isJapaneseNationalHoliday(d))) {
      lbl.setEnclosedShape(EnclosedShape.ROUNDED_RECTANGLE);
      lbl.setForeground(table.getBackground());
      lbl.setBackground(table.getForeground());
    } else if (isThisMonth && d.getDayOfWeek() == DayOfWeek.SATURDAY) {
      lbl.setEnclosedShape(EnclosedShape.ELLIPSE);
      lbl.setForeground(table.getBackground().darker());
      lbl.setBackground(table.getForeground().brighter());
    } else if (isThisMonth) {
      lbl.setEnclosedShape(EnclosedShape.NONE);
      lbl.setForeground(table.getForeground());
      lbl.setBackground(table.getBackground());
    } else {
      lbl.setEnclosedShape(EnclosedShape.NONE);
      lbl.setForeground(Color.GRAY);
      lbl.setBackground(table.getForeground());
      lbl.setText(txt);
    }
  }

  private Color getDayOfWeekColor(JTable table, DayOfWeek dow) {
    int code;
    if (dow == DayOfWeek.SUNDAY) {
      code = 0xFF_DC_DC;
    } else if (dow == DayOfWeek.SATURDAY) {
      code = 0xDC_DC_FF;
    } else {
      code = table.getBackground().getRGB();
    }
    return new Color(code);
  }

  public boolean isJapaneseNationalHoliday(LocalDate d) {
    return LocalDate.of(2020, 7, 23).equals(d)
        || LocalDate.of(2020, 7, 24).equals(d);
  }

  // public static String getDayOfWeekText(LocalDate d) {
  //   switch (d.getDayOfWeek()) {
  //     case SUNDAY:
  //     case SATURDAY: return CIRCLED_NUMBER[d.getDayOfMonth() - 1];
  //     default: return Objects.toString(d.getDayOfMonth());
  //   }
  // }
}

class EnclosedLabel extends JLabel {
  private EnclosedShape enclosedShape = EnclosedShape.NONE;

  protected EnclosedLabel() {
    super("", CENTER);
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
    if (enclosedShape != EnclosedShape.NONE) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(getBackground());
      g2.fill(getShape());
      g2.dispose();
    }
    super.paintComponent(g);
  }

  protected void setEnclosedShape(EnclosedShape shape) {
    this.enclosedShape = shape;
  }

  protected Shape getShape() {
    double w = getWidth() - 1d;
    double h = getHeight() - 1d;
    return enclosedShape == EnclosedShape.ELLIPSE
        ? new Ellipse2D.Double(0d, 0d, w, h)
        : new RoundRectangle2D.Double(0d, 0d, w, h, 8d, 8d);
  }
}

enum EnclosedShape {
  ROUNDED_RECTANGLE, ELLIPSE, NONE
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
