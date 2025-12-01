// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  @SuppressWarnings("PMD.UseConcurrentHashMap")
  private final Map<DayOfWeek, Color> holidayColorMap = new EnumMap<>(DayOfWeek.class);
  private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
  private final JTable monthTable = new MonthTable();
  private LocalDate currentLocalDate;

  private MainPanel() {
    super(new BorderLayout());
    holidayColorMap.put(DayOfWeek.SUNDAY, new Color(0xD9_0B_0D));
    holidayColorMap.put(DayOfWeek.SATURDAY, new Color(0x10_4A_90));
    updateMonthView(LocalDate.now(ZoneId.systemDefault()));
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
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getViewport().setOpaque(false);
    scroll.setOpaque(false);
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    add(p, BorderLayout.NORTH);
    add(scroll);
    setOpaque(true);
    setBackground(Color.WHITE);
    setPreferredSize(new Dimension(320, 240));
  }

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }

  public void updateMonthView(LocalDate localDate) {
    currentLocalDate = localDate;
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy / MM");
    monthLabel.setText(localDate.format(fmt.withLocale(Locale.getDefault())));
    monthTable.setModel(new CalendarViewTableModel(localDate));
  }

  private final class MonthTable extends JTable {
    private final Point pt = new Point(-1000, -1000);
    private transient MouseAdapter listener;

    @Override public void updateUI() {
      removeMouseListener(listener);
      removeMouseMotionListener(listener);
      super.updateUI();
      setFillsViewportHeight(true);
      setBackground(Color.WHITE);
      setShowGrid(false);
      setIntercellSpacing(new Dimension(2, 2));
      setFont(getFont().deriveFont(Font.BOLD));
      setOpaque(false);
      setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());
      JTableHeader header = getTableHeader();
      TableCellRenderer r = new CenterAlignmentHeaderRenderer();
      TableColumnModel cm = getColumnModel();
      EventQueue.invokeLater(() -> {
        for (int i = 0; i < cm.getColumnCount(); i++) {
          cm.getColumn(i).setHeaderRenderer(r);
        }
      });
      header.setResizingAllowed(false);
      header.setReorderingAllowed(false);
      listener = new SpotlightListener();
      addMouseListener(listener);
      addMouseMotionListener(listener);
    }

    @Override public void doLayout() {
      super.doLayout();
      Class<JViewport> clz = JViewport.class;
      Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, this))
          .filter(clz::isInstance).map(clz::cast)
          .ifPresent(this::updateRowsHeight);
    }

    @Override protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setComposite(AlphaComposite.Src);
      Point2D center = new Point2D.Float(pt.x, pt.y);
      float[] dist = {0.0f, 0.5f, 1.0f};
      Color[] colors = {Color.GRAY, Color.LIGHT_GRAY, Color.WHITE};
      Rectangle cr = getCellRect(0, 0, true);
      int r = Math.max(cr.width, cr.height) * 2;
      g2.setPaint(new RadialGradientPaint(center, r, dist, colors));
      int r2 = r + r;
      g2.fill(new Ellipse2D.Float(pt.x - r, pt.y - r, r2, r2));
      g2.dispose();
      super.paintComponent(g);
    }

    private void updateRowsHeight(JViewport viewport) {
      int height = viewport.getExtentSize().height;
      int rowCount = getModel().getRowCount();
      int rowHeight = height / rowCount;
      int remainder = height % rowCount;
      for (int i = 0; i < rowCount; i++) {
        int a = rowHeight + Math.min(1, Math.max(0, remainder--));
        setRowHeight(i, Math.max(1, a));
      }
    }

    private final class SpotlightListener extends MouseAdapter {
      @Override public void mouseExited(MouseEvent e) {
        pt.setLocation(-1000, -1000);
        repaint();
      }

      @Override public void mouseEntered(MouseEvent e) {
        update(e);
      }

      @Override public void mouseDragged(MouseEvent e) {
        update(e);
      }

      @Override public void mouseMoved(MouseEvent e) {
        update(e);
      }

      private void update(MouseEvent e) {
        pt.setLocation(e.getPoint());
        Rectangle cr = getCellRect(0, 0, true);
        int r = Math.max(cr.width, cr.height) * 2;
        int r2 = r + r;
        repaint(new Rectangle(pt.x - r, pt.y - r, r2, r2));
      }
    }
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

final class CenterAlignmentHeaderRenderer implements TableCellRenderer {
  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    Component c = r.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel) {
      ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
    }
    return c;
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
