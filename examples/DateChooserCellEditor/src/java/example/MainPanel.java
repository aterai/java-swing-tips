// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.EnumMap;
import java.util.EventObject;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private static final int TARGET_COLUMN = 0;

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setCellSelectionEnabled(true);
        setDefaultEditor(Date.class, new DateEditor());
      }
    };
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"A", "B"};
    @SuppressWarnings({"JavaUtilDate", "PMD.ReplaceJavaUtilDate"})
    Object[][] data = {
        {new Date(), ""},
        {new Date(), ""},
        {new Date(), ""},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int column) {
        return column == TARGET_COLUMN;
      }
    };
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

class DateEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
  private static final String EDIT = "edit";
  private final JButton button = new JButton();
  private final DateFormat formatter = DateFormat.getDateInstance();
  private final CalenderPanel dateChooser = new CalenderPanel();
  private JPopupMenu popup;
  private JTable table;

  protected DateEditor() {
    super();
    button.setActionCommand(EDIT);
    button.addActionListener(this);
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    button.setHorizontalAlignment(SwingConstants.LEFT);
    button.setHorizontalTextPosition(SwingConstants.RIGHT);
  }

  @Override public void actionPerformed(ActionEvent e) {
    if (EDIT.equals(e.getActionCommand()) && table != null) {
      int row = table.getSelectedRow();
      int col = table.getSelectedColumn();
      Rectangle rect = table.getCellRect(row, col, true);
      Point p = new Point(rect.x, (int) rect.getMaxY());
      if (popup == null) {
        popup = new JPopupMenu();
        popup.add(dateChooser);
        popup.pack();
      }
      popup.show(table, p.x, p.y);
      dateChooser.requestFocusInWindow();
    }
  }

  @Override public boolean isCellEditable(EventObject e) {
    return e instanceof MouseEvent && ((MouseEvent) e).getClickCount() >= 2;
  }

  @Override public Object getCellEditorValue() {
    LocalDate d = dateChooser.getLocalDate();
    return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }

  @SuppressWarnings("PMD.ReplaceJavaUtilDate")
  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    if (value instanceof Date) {
      Date date = (Date) value;
      button.setText(formatter.format(date));
      button.setOpaque(true);
      // button.setForeground(table.getSelectionForeground());
      Color fgc = table.getSelectionForeground();
      button.setForeground(new Color(fgc.getRGB()));
      button.setBackground(table.getSelectionBackground());
      ZonedDateTime dateTime = date.toInstant().atZone(ZoneId.systemDefault());
      dateChooser.setLocalDate(dateTime.toLocalDate());
      this.table = table;
    }
    return button;
  }

  private final class CalenderPanel extends JPanel {
    public final LocalDate realLocalDate = LocalDate.now(ZoneId.systemDefault());
    private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    private final MonthTable monthTable = new MonthTable();
    private LocalDate currentLocalDate;

    private CalenderPanel() {
      super(new BorderLayout());
      CalendarTableRenderer r = new CalendarTableRenderer(this, monthTable.highlighter);
      monthTable.setDefaultRenderer(LocalDate.class, r);
      monthTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      monthTable.setCellSelectionEnabled(true);
      monthTable.setRowHeight(16);
      monthTable.setFillsViewportHeight(true);

      JTableHeader header = monthTable.getTableHeader();
      header.setResizingAllowed(false);
      header.setReorderingAllowed(false);
      ((JLabel) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

      MouseAdapter mouseHandler = new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
          int row = monthTable.getSelectedRow();
          int column = monthTable.getSelectedColumn();
          currentLocalDate = (LocalDate) monthTable.getValueAt(row, column);
          popup.setVisible(false);
          fireEditingStopped();
        }
      };
      monthTable.addMouseListener(mouseHandler);
      setLocalDate(realLocalDate);
      JButton prev = new JButton("<");
      prev.addActionListener(e -> setLocalDate(getLocalDate().minusMonths(1)));
      JButton next = new JButton(">");
      next.addActionListener(e -> setLocalDate(getLocalDate().plusMonths(1)));
      JPanel p = new JPanel(new BorderLayout());
      p.add(monthLabel);
      p.add(prev, BorderLayout.WEST);
      p.add(next, BorderLayout.EAST);
      add(p, BorderLayout.NORTH);
      add(new JScrollPane(monthTable));
    }

    @Override public Dimension getPreferredSize() {
      return new Dimension(220, 143);
    }

    public LocalDate getLocalDate() {
      return currentLocalDate;
    }

    public void setLocalDate(LocalDate date) {
      currentLocalDate = date;
      DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy / MM");
      monthLabel.setText(date.format(fmt.withLocale(Locale.getDefault())));
      monthTable.setModel(new CalendarViewTableModel(date));
    }
  }

  private static final class MonthTable extends JTable {
    public transient HighlightListener highlighter;
    private int prevHeight = -1;
    private int prevCount = -1;

    @Override public void updateUI() {
      removeMouseListener(highlighter);
      removeMouseMotionListener(highlighter);
      super.updateUI();
      setRowSelectionAllowed(false);
      highlighter = new HighlightListener();
      addMouseListener(highlighter);
      addMouseMotionListener(highlighter);
    }

    @Override public void doLayout() {
      super.doLayout();
      Class<JViewport> clz = JViewport.class;
      Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, this))
          .filter(clz::isInstance)
          .map(clz::cast)
          .ifPresent(this::updateRowsHeight);
    }

    private void updateRowsHeight(JViewport viewport) {
      int height = viewport.getExtentSize().height;
      int rowCount = getModel().getRowCount();
      int defaultRowHeight = height / rowCount;
      if ((height != prevHeight || rowCount != prevCount) && defaultRowHeight > 0) {
        int remainder = height % rowCount;
        for (int i = 0; i < rowCount; i++) {
          int a = Math.min(1, Math.max(0, remainder--));
          setRowHeight(i, defaultRowHeight + a);
        }
      }
      prevHeight = height;
      prevCount = rowCount;
    }
  }

  private static class CalendarTableRenderer extends DefaultTableCellRenderer {
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private final Map<DayOfWeek, Color> holidayColorMap = new EnumMap<>(DayOfWeek.class);
    private final LocalDate realLocalDate;
    private final CalenderPanel calender;
    private final transient HighlightListener highlighter;

    protected CalendarTableRenderer(CalenderPanel calender, HighlightListener highlighter) {
      super();
      this.calender = calender;
      this.realLocalDate = calender.realLocalDate;
      this.highlighter = highlighter;
      holidayColorMap.put(DayOfWeek.SUNDAY, new Color(0xFF_DC_DC));
      holidayColorMap.put(DayOfWeek.SATURDAY, new Color(0xDC_DC_FF));
    }

    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      Component c = super.getTableCellRendererComponent(
          table, value, selected, focused, row, column);
      if (c instanceof JLabel && value instanceof LocalDate) {
        JLabel l = (JLabel) c;
        l.setHorizontalAlignment(CENTER);
        LocalDate d = (LocalDate) value;
        l.setText(Integer.toString(d.getDayOfMonth()));
        if (YearMonth.from(d).equals(YearMonth.from(calender.getLocalDate()))) {
          l.setForeground(table.getForeground());
        } else {
          l.setForeground(Color.GRAY);
        }
        if (d.isEqual(realLocalDate)) {
          l.setBackground(new Color(0xDC_FF_DC));
        } else {
          l.setBackground(getDayOfWeekColor(table, d.getDayOfWeek()));
        }
        highlighter.getCellHighlightColor(row, column).ifPresent(c::setBackground);
      }
      return c;
    }

    private Color getDayOfWeekColor(JTable table, DayOfWeek dow) {
      return Optional.ofNullable(holidayColorMap.get(dow)).orElse(table.getBackground());
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

class HighlightListener extends MouseAdapter {
  private int viewRowIndex = -1;
  private int viewColumnIndex = -1;

  public Optional<Color> getCellHighlightColor(int row, int column) {
    boolean ri = this.viewRowIndex == row;
    boolean ci = this.viewColumnIndex == column;
    return ri && ci ? Optional.of(Color.LIGHT_GRAY) : Optional.empty();
  }

  private void setHighlightTableCell(MouseEvent e) {
    Point pt = e.getPoint();
    Component c = e.getComponent();
    if (c instanceof JTable) {
      JTable table = (JTable) c;
      viewRowIndex = table.rowAtPoint(pt);
      viewColumnIndex = table.columnAtPoint(pt);
      if (viewRowIndex < 0 || viewColumnIndex < 0) {
        viewRowIndex = -1;
        viewColumnIndex = -1;
      }
      table.repaint();
    }
  }

  @Override public void mouseMoved(MouseEvent e) {
    setHighlightTableCell(e);
  }

  @Override public void mouseDragged(MouseEvent e) {
    setHighlightTableCell(e);
  }

  @Override public void mouseExited(MouseEvent e) {
    viewRowIndex = -1;
    viewColumnIndex = -1;
    e.getComponent().repaint();
  }
}
