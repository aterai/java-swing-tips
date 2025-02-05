// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Path2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public final class MainPanel extends JPanel {
  @SuppressWarnings("PMD.UseConcurrentHashMap")
  private final Map<DayOfWeek, Color> holidayColorMap = new EnumMap<>(DayOfWeek.class);
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

    @Override public void updateUI() {
      super.updateUI();
      setFillsViewportHeight(true);
      setBackground(Color.WHITE);
      setShowVerticalLines(false);
      setShowHorizontalLines(true);
      setIntercellSpacing(new Dimension(0, 1));
      setFont(getFont().deriveFont(Font.BOLD));
      setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());
    }

    @Override public void doLayout() {
      super.doLayout();
      Class<JViewport> clz = JViewport.class;
      Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, this))
          .filter(clz::isInstance).map(clz::cast)
          .ifPresent(this::updateRowsHeight);
    }
  };
  private final List<Color> monthThemeColor = Arrays.asList(
      new Color(0xD5_0B_17), new Color(0x02_6C_B6), new Color(0xED_87_AD),
      new Color(0xCE_30_6A), new Color(0x48_B0_37), new Color(0xA4_62_A2),
      new Color(0x00_BD_E7), new Color(0xEB_5E_31), new Color(0xC8_01_82),
      new Color(0x8F_19_19), new Color(0x6A_31_8F), new Color(0x00_7A_70));
  private LocalDate currentLocalDate;

  private MainPanel() {
    super(new BorderLayout());
    holidayColorMap.put(DayOfWeek.SUNDAY, new Color(0xD9_0B_0D));
    holidayColorMap.put(DayOfWeek.SATURDAY, new Color(0x10_4A_90));

    monthLabel.setOpaque(false);
    monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD));

    JTableHeader header = monthTable.getTableHeader();
    header.setForeground(Color.WHITE);
    header.setOpaque(false);
    header.setDefaultRenderer(new RoundedHeaderRenderer());
    header.setResizingAllowed(false);
    header.setReorderingAllowed(false);

    updateMonthView(LocalDate.of(2021, 6, 21));

    JButton prev = new JButton("<");
    prev.addActionListener(e -> updateMonthView(getCurrentLocalDate().minusMonths(1)));

    JButton next = new JButton(">");
    next.addActionListener(e -> updateMonthView(getCurrentLocalDate().plusMonths(1)));

    JPanel p = new JPanel(new BorderLayout());
    p.setOpaque(false);
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(monthLabel);
    p.add(prev, BorderLayout.WEST);
    p.add(next, BorderLayout.EAST);

    JScrollPane scroll = new JScrollPane(monthTable);
    scroll.setColumnHeader(new JViewport() {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = 24;
        return d;
      }
    });
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setViewportBorder(BorderFactory.createEmptyBorder());
    scroll.getViewport().setBackground(Color.WHITE);

    add(p, BorderLayout.NORTH);
    add(scroll);
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }

  public void updateMonthView(LocalDate localDate) {
    currentLocalDate = localDate;
    Color color = monthThemeColor.get(localDate.getMonthValue() - 1);
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy / MM");
    monthLabel.setText(localDate.format(fmt.withLocale(Locale.getDefault())));
    monthLabel.setForeground(color);
    monthTable.setModel(new CalendarViewTableModel(localDate));
    monthTable.getTableHeader().setBackground(color);
  }

  private final class CalendarTableRenderer extends DefaultTableCellRenderer {
    private final JPanel panel = new JPanel(new BorderLayout());

    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      Component c = super.getTableCellRendererComponent(table, value, false, false, row, column);
      if (value instanceof LocalDate && c instanceof JLabel) {
        LocalDate d = (LocalDate) value;
        JLabel l = (JLabel) c;
        l.setText(Integer.toString(d.getDayOfMonth()));
        l.setVerticalAlignment(TOP);
        l.setHorizontalAlignment(CENTER);
        updateCellWeekColor(d, c);

        LocalDate nextWeekDay = d.plusDays(7);
        boolean isLastRow = row == table.getModel().getRowCount() - 1;
        if (isLastRow && isDiagonallySplitCell(nextWeekDay)) {
          JLabel sub = new JLabel(Integer.toString(nextWeekDay.getDayOfMonth()));
          sub.setFont(table.getFont());
          sub.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
          sub.setOpaque(false);
          sub.setVerticalAlignment(BOTTOM);
          sub.setHorizontalAlignment(RIGHT);

          panel.removeAll();
          panel.setOpaque(false);
          panel.setForeground(getDayOfWeekColor(d.getDayOfWeek()));
          panel.add(sub, BorderLayout.SOUTH);
          panel.add(c, BorderLayout.NORTH);
          panel.setBorder(l.getBorder());
          l.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
          l.setHorizontalAlignment(LEFT);

          updateCellWeekColor(d, sub);
          c = new JLayer<>(panel, new DiagonallySplitCellLayerUI());
        }
      }
      return c;
    }

    private boolean isDiagonallySplitCell(LocalDate nextWeekDay) {
      return YearMonth.from(nextWeekDay).equals(YearMonth.from(getCurrentLocalDate()));
    }

    private void updateCellWeekColor(LocalDate d, Component fgc) {
      if (YearMonth.from(d).equals(YearMonth.from(getCurrentLocalDate()))) {
        fgc.setForeground(getDayOfWeekColor(d.getDayOfWeek()));
      } else {
        fgc.setForeground(Color.GRAY);
      }
    }

    private Color getDayOfWeekColor(DayOfWeek dow) {
      return Optional.ofNullable(holidayColorMap.get(dow)).orElse(Color.BLACK);
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

class RoundedHeaderRenderer extends DefaultTableCellRenderer {
  private final JLabel firstLabel = new JLabel() {
    @Override public void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(getBackground());
      double r = 8d;
      double rr = r * 4d * (Math.sqrt(2d) - 1d) / 3d; // = r * .5522;
      double x = 0d;
      double y = 0d;
      double w = getWidth();
      double h = getHeight();
      Path2D p = new Path2D.Double();
      p.moveTo(x, y + r);
      p.curveTo(x, y + r - rr, x + r - rr, y, x + r, y);
      p.lineTo(x + w, y);
      p.lineTo(x + w, y + h);
      p.lineTo(x + r, y + h);
      p.curveTo(x + r - rr, y + h, x, y + h - r + rr, x, y + h - r);
      p.closePath();
      g2.fill(p);
      g2.dispose();
      super.paintComponent(g);
    }
  };
  private final JLabel lastLabel = new JLabel() {
    @Override public void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(getBackground());
      double r = 8d;
      double rr = r * 4d * (Math.sqrt(2d) - 1d) / 3d; // = r * .5522;
      double x = 0d;
      double y = 0d;
      double w = getWidth();
      double h = getHeight();
      Path2D p = new Path2D.Double();
      p.moveTo(x, y);
      p.lineTo(x + w - r, y);
      p.curveTo(x + w - r + rr, y, x + w, y + r - rr, x + w, y + r);
      p.lineTo(x + w, y + h - r);
      p.curveTo(x + w, y + h - r + rr, x + w - r + rr, y + h, x + w - r, y + h);
      p.lineTo(x, y + h);
      p.closePath();
      g2.fill(p);
      g2.dispose();
      super.paintComponent(g);
    }
  };

  protected RoundedHeaderRenderer() {
    super();
    firstLabel.setOpaque(false);
    lastLabel.setOpaque(false);
    firstLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    lastLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (column == 0) {
      c = firstLabel;
    } else if (column == table.getColumnCount() - 1) {
      c = lastLabel;
    }
    c.setFont(table.getFont());
    c.setForeground(table.getTableHeader().getForeground());
    c.setBackground(table.getTableHeader().getBackground());
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      l.setText(value.toString());
      l.setHorizontalAlignment(CENTER);
    }
    return c;
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
      // g2.setPaint(UIManager.getColor("Table.gridColor"));
      g2.setPaint(((JLayer<?>) c).getView().getForeground());
      g2.drawLine(c.getWidth() - 4, 4, 4, c.getHeight() - 4);
      g2.dispose();
    }
  }
}
