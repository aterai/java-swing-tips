// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
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
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  @SuppressWarnings("PMD.UseConcurrentHashMap")
  private final Map<DayOfWeek, Color> holidayColorMap = new EnumMap<>(DayOfWeek.class);
  private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
  private final JTable monthTable = new CalendarTable();
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
    monthLabel.setText(localDate.format(DateTimeFormatter.ofPattern("yyyy / MM")));
    monthTable.setModel(new CalendarViewTableModel(localDate));
  }

  private final class CalendarTable extends JTable {
    private final Point spotlightCenter = new Point(-1000, -1000);
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
      // The spotlight is painted before the cells, so the table itself
      // must not fill its background
      setOpaque(false);
      setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());
      JTableHeader header = getTableHeader();
      header.setResizingAllowed(false);
      header.setReorderingAllowed(false);
      listener = new SpotlightListener();
      addMouseListener(listener);
      addMouseMotionListener(listener);
    }

    @Override public void createDefaultColumnsFromModel() {
      super.createDefaultColumnsFromModel();
      // setModel(...) recreates all the TableColumns, so the header renderer
      // must be set again each time the month view is updated
      TableCellRenderer r = new CenterAlignmentHeaderRenderer();
      TableColumnModel cm = getColumnModel();
      for (int i = 0; i < cm.getColumnCount(); i++) {
        cm.getColumn(i).setHeaderRenderer(r);
      }
    }

    @Override public boolean getScrollableTracksViewportHeight() {
      // Always follow the viewport height so that doLayout (and thus
      // adjustRowHeights) runs again when the viewport shrinks, e.g. when the
      // menu bar is added after the frame is packed
      return getParent() instanceof JViewport;
    }

    @Override public void doLayout() {
      super.doLayout();
      Class<JViewport> clz = JViewport.class;
      Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, this))
          .filter(clz::isInstance).map(clz::cast)
          .ifPresent(this::adjustRowHeights);
    }

    @Override protected void paintComponent(Graphics g) {
      Rectangle r = getSpotlightBounds();
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      float radius = r.width / 2f;
      float[] dist = {0f, .5f, 1f};
      Color[] colors = {Color.GRAY, Color.LIGHT_GRAY, Color.WHITE};
      g2.setPaint(new RadialGradientPaint(spotlightCenter, radius, dist, colors));
      g2.fill(new Ellipse2D.Float(r.x, r.y, r.width, r.height));
      g2.dispose();
      // The opaque cells are painted over the gradient, so it remains
      // visible only in the intercell spacing
      super.paintComponent(g);
    }

    private Rectangle getSpotlightBounds() {
      Rectangle cr = getCellRect(0, 0, true);
      int radius = Math.max(cr.width, cr.height) * 2;
      int size = radius + radius;
      return new Rectangle(spotlightCenter.x - radius, spotlightCenter.y - radius, size, size);
    }

    private void adjustRowHeights(JViewport viewport) {
      int height = viewport.getExtentSize().height;
      int rowCount = getModel().getRowCount();
      int baseRowHeight = height / rowCount;
      int remainder = height % rowCount;
      // Distribute the remainder one pixel at a time to the first rows
      for (int i = 0; i < rowCount; i++) {
        int adjustedHeight = baseRowHeight + (i < remainder ? 1 : 0);
        setRowHeight(i, Math.max(1, adjustedHeight));
      }
    }

    private final class SpotlightListener extends MouseAdapter {
      @Override public void mouseExited(MouseEvent e) {
        Rectangle r = getSpotlightBounds();
        spotlightCenter.setLocation(-1000, -1000);
        repaint(r);
      }

      @Override public void mouseEntered(MouseEvent e) {
        moveSpotlight(e.getPoint());
      }

      @Override public void mouseDragged(MouseEvent e) {
        moveSpotlight(e.getPoint());
      }

      @Override public void mouseMoved(MouseEvent e) {
        moveSpotlight(e.getPoint());
      }

      private void moveSpotlight(Point pt) {
        // Repaint both the old and the new spotlight areas so that no
        // highlighted borders are left behind when the mouse moves quickly
        Rectangle r = getSpotlightBounds();
        spotlightCenter.setLocation(pt);
        repaint(r.union(getSpotlightBounds()));
      }
    }
  }

  private final class CalendarTableRenderer extends DefaultTableCellRenderer {
    private final JLabel sub = new JLabel();
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLayer<JPanel> layer = new JLayer<>(panel, new DiagonallySplitCellLayerUI());

    private CalendarTableRenderer() {
      super();
      sub.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
      sub.setOpaque(false);
      sub.setVerticalAlignment(BOTTOM);
      sub.setHorizontalAlignment(RIGHT);
      // Make the container opaque so that the spotlight gradient shows
      // through only the intercell spacing, like the other cells
      panel.setOpaque(true);
    }

    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      Component c = super.getTableCellRendererComponent(
          table, value, false, false, row, column);
      if (value instanceof LocalDate && c instanceof JLabel) {
        LocalDate d = (LocalDate) value;
        JLabel l = (JLabel) c;
        l.setText(Integer.toString(d.getDayOfMonth()));
        l.setVerticalAlignment(TOP);
        l.setHorizontalAlignment(CENTER);
        updateForeground(d, l);
        TableModel model = table.getModel();
        LocalDate nextWeekDay = d.plusDays(model.getColumnCount()); // plus 7 days
        boolean isLastRow = row == model.getRowCount() - 1;
        if (isLastRow && isCurrentMonth(nextWeekDay)) {
          l.setHorizontalAlignment(LEFT);
          sub.setText(Integer.toString(nextWeekDay.getDayOfMonth()));
          sub.setFont(l.getFont());
          updateForeground(nextWeekDay, sub);
          // The label may have been re-parented to the CellRendererPane
          panel.removeAll();
          panel.add(l, BorderLayout.NORTH);
          panel.add(sub, BorderLayout.SOUTH);
          panel.setBackground(l.getBackground());
          // Used as the color of the diagonal line by DiagonallySplitCellLayerUI
          panel.setForeground(l.getForeground());
          c = layer;
        }
      }
      return c;
    }

    private boolean isCurrentMonth(LocalDate d) {
      return YearMonth.from(d).equals(YearMonth.from(getCurrentLocalDate()));
    }

    private void updateForeground(LocalDate d, Component c) {
      c.setForeground(isCurrentMonth(d) ? getDayOfWeekColor(d.getDayOfWeek()) : Color.GRAY);
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
    return DayOfWeek.values().length; // 7
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
