// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final Dimension CELL_SIZE = new Dimension(40, 26);
  private static final Color SELECTED_COLOR = new Color(0xC8_00_78_D7, true);
  public final JLabel yearMonthLabel = new JLabel("", SwingConstants.CENTER);
  public final JList<LocalDate> monthList = new JList<LocalDate>() {
    @Override public void updateUI() {
      setCellRenderer(null);
      super.updateUI();
      setLayoutOrientation(HORIZONTAL_WRAP);
      setVisibleRowCount(CalendarViewListModel.ROW_COUNT); // ensure 6 rows in the list
      setFixedCellWidth(CELL_SIZE.width);
      setFixedCellHeight(CELL_SIZE.height);
      setCellRenderer(new CalendarListRenderer());
      setOpaque(false);
      getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      // getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      addListSelectionListener(e -> repaint());
    }

    @Override protected void paintComponent(Graphics g) {
      int[] indices = getSelectedIndices();
      if (indices.length > 0) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(SELECTED_COLOR);
        Area area = new Area();
        Arrays.stream(indices)
            .mapToObj(i -> getCellBounds(i, i))
            .forEach(r -> area.add(new Area(r)));
        for (Area a : GeomUtils.singularization(area)) {
          List<Point2D> lst = GeomUtils.convertAreaToPoint2DList(a);
          g2.fill(GeomUtils.convertRoundedPath(lst, 4d));
        }
        g2.dispose();
      }
      super.paintComponent(g);
    }
  };
  public final LocalDate realLocalDate = LocalDate.now(ZoneId.systemDefault());
  private LocalDate currentLocalDate;

  private MainPanel() {
    super();
    installActions();

    JButton prev = new JButton("<");
    prev.addActionListener(e -> updateMonthView(getCurrentLocalDate().minusMonths(1)));

    JButton next = new JButton(">");
    next.addActionListener(e -> updateMonthView(getCurrentLocalDate().plusMonths(1)));

    JPanel yearMonthPanel = new JPanel(new BorderLayout());
    yearMonthPanel.add(yearMonthLabel);
    yearMonthPanel.add(prev, BorderLayout.WEST);
    yearMonthPanel.add(next, BorderLayout.EAST);
    updateMonthView(realLocalDate);

    JScrollPane scroll = new JScrollPane(monthList);
    scroll.setColumnHeaderView(makeDayOfWeekHeader());
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JLabel label = new JLabel(" ", SwingConstants.CENTER);

    monthList.getSelectionModel().addListSelectionListener(e -> {
      ListSelectionModel lsm = (ListSelectionModel) e.getSource();
      if (lsm.isSelectionEmpty()) {
        label.setText(" ");
      } else {
        ListModel<LocalDate> model = monthList.getModel();
        LocalDate from = model.getElementAt(lsm.getMinSelectionIndex());
        LocalDate to = model.getElementAt(lsm.getMaxSelectionIndex());
        label.setText(Period.between(from, to).toString());
      }
    });

    Box box = Box.createVerticalBox();
    box.add(yearMonthPanel);
    box.add(Box.createVerticalStrut(2));
    box.add(scroll);
    box.add(label);

    add(box);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JList<DayOfWeek> makeDayOfWeekHeader() {
    DefaultListModel<DayOfWeek> weekModel = new DefaultListModel<>();
    DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
    for (int i = 0; i < DayOfWeek.values().length; i++) {
      weekModel.add(i, firstDayOfWeek.plus(i));
    }
    // String s = value.getDisplayName(TextStyle.SHORT_STANDALONE, locale);
    // l.setText(s.substring(0, Math.min(2, s.length())));
    return new JList<DayOfWeek>(weekModel) {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        ListCellRenderer<? super DayOfWeek> r = getCellRenderer();
        setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
          Component c = r.getListCellRendererComponent(list, value, index, false, false);
          c.setBackground(new Color(0xDC_DC_DC));
          if (c instanceof JLabel) {
            JLabel l = (JLabel) c;
            l.setHorizontalAlignment(SwingConstants.CENTER);
            // String s = value.getDisplayName(TextStyle.SHORT_STANDALONE, locale);
            // l.setText(s.substring(0, Math.min(2, s.length())));
            l.setText(value.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()));
          }
          return c;
        });
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setLayoutOrientation(HORIZONTAL_WRAP);
        setVisibleRowCount(0);
        setFixedCellWidth(CELL_SIZE.width);
        setFixedCellHeight(CELL_SIZE.height);
      }
    };
  }

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }

  private void installActions() {
    InputMap im = monthList.getInputMap(WHEN_FOCUSED);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "selectNextIndex");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "selectPreviousIndex");

    ActionMap am = monthList.getActionMap();
    am.put("selectPreviousIndex", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        int index = monthList.getLeadSelectionIndex();
        if (index > 0) {
          monthList.setSelectedIndex(index - 1);
        } else {
          LocalDate d = monthList.getModel().getElementAt(0).minusDays(1);
          updateMonthView(getCurrentLocalDate().minusMonths(1));
          monthList.setSelectedValue(d, false);
        }
      }
    });
    am.put("selectNextIndex", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        int index = monthList.getLeadSelectionIndex();
        if (index < monthList.getModel().getSize() - 1) {
          monthList.setSelectedIndex(index + 1);
        } else {
          int lastDayOfMonth = monthList.getModel().getSize() - 1;
          LocalDate d = monthList.getModel().getElementAt(lastDayOfMonth).plusDays(1);
          updateMonthView(getCurrentLocalDate().plusMonths(1));
          monthList.setSelectedValue(d, false);
        }
      }
    });
    Action selectPreviousRow = am.get("selectPreviousRow");
    am.put("selectPreviousRow", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        int index = monthList.getLeadSelectionIndex();
        int weekLength = DayOfWeek.values().length; // 7
        if (index < weekLength) {
          LocalDate d = monthList.getModel().getElementAt(index).minusDays(weekLength);
          updateMonthView(getCurrentLocalDate().minusMonths(1));
          monthList.setSelectedValue(d, false);
        } else {
          selectPreviousRow.actionPerformed(e);
        }
      }
    });
    Action selectNextRow = am.get("selectNextRow");
    am.put("selectNextRow", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        int index = monthList.getLeadSelectionIndex();
        int weekLength = DayOfWeek.values().length; // 7
        if (index > monthList.getModel().getSize() - weekLength) {
          LocalDate d = monthList.getModel().getElementAt(index).plusDays(weekLength);
          updateMonthView(getCurrentLocalDate().plusMonths(1));
          monthList.setSelectedValue(d, false);
        } else {
          selectNextRow.actionPerformed(e);
        }
      }
    });
  }

  public void updateMonthView(LocalDate localDate) {
    currentLocalDate = localDate;
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy / MM");
    yearMonthLabel.setText(localDate.format(fmt.withLocale(Locale.getDefault())));
    monthList.setModel(new CalendarViewListModel(localDate));
  }

  private final class CalendarListRenderer implements ListCellRenderer<LocalDate> {
    private final ListCellRenderer<? super LocalDate> renderer = new DefaultListCellRenderer();

    @Override public Component getListCellRendererComponent(JList<? extends LocalDate> list, LocalDate value, int index, boolean isSelected, boolean cellHasFocus) {
      Component c = renderer.getListCellRendererComponent(list, value, index, false, false);
      if (c instanceof JLabel) {
        JLabel l = (JLabel) c;
        l.setOpaque(false);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setText(Integer.toString(value.getDayOfMonth()));
      }
      Color fgc = c.getForeground();
      if (YearMonth.from(value).equals(YearMonth.from(getCurrentLocalDate()))) {
        DayOfWeek dow = value.getDayOfWeek();
        if (value.isEqual(realLocalDate)) {
          fgc = new Color(0x64_FF_64);
        } else if (dow == DayOfWeek.SUNDAY) {
          fgc = new Color(0xFF_64_64);
        } else if (dow == DayOfWeek.SATURDAY) {
          fgc = new Color(0x64_64_FF);
        }
      } else {
        fgc = Color.GRAY;
      }
      c.setForeground(isSelected ? Color.WHITE : fgc);
      return c;
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

class CalendarViewListModel extends AbstractListModel<LocalDate> {
  public static final int ROW_COUNT = 6;
  private final LocalDate startDate;

  protected CalendarViewListModel(LocalDate date) {
    super();
    LocalDate firstDayOfMonth = YearMonth.from(date).atDay(1);
    WeekFields weekFields = WeekFields.of(Locale.getDefault());
    int fdmDow = firstDayOfMonth.get(weekFields.dayOfWeek()) - 1;
    startDate = firstDayOfMonth.minusDays(fdmDow);
  }

  @Override public int getSize() {
    return DayOfWeek.values().length * ROW_COUNT;
  }

  @Override public LocalDate getElementAt(int index) {
    return startDate.plusDays(index);
  }
}

final class GeomUtils {
  private GeomUtils() {
    /* Singleton */
  }

  public static List<Point2D> convertAreaToPoint2DList(Area area) {
    List<Point2D> list = new ArrayList<>();
    PathIterator pi = area.getPathIterator(null);
    double[] coords = new double[6];
    while (!pi.isDone()) {
      switch (pi.currentSegment(coords)) {
        case PathIterator.SEG_MOVETO:
        case PathIterator.SEG_LINETO:
          list.add(new Point2D.Double(coords[0], coords[1]));
          break;
        default:
          break;
      }
      pi.next();
    }
    return list;
  }

  /**
   * Rounding the corners of a Rectilinear Polygon.
   */
  public static Path2D convertRoundedPath(List<Point2D> list, double arc) {
    double kappa = 4d * (Math.sqrt(2d) - 1d) / 3d; // = 0.55228...;
    double akv = arc - arc * kappa;
    int sz = list.size();
    Point2D pt0 = list.get(0);
    Path2D path = new Path2D.Double();
    path.moveTo(pt0.getX() + arc, pt0.getY());
    for (int i = 0; i < sz; i++) {
      Point2D prv = list.get((i - 1 + sz) % sz);
      Point2D cur = list.get(i);
      Point2D nxt = list.get((i + 1) % sz);
      double dx0 = Math.signum(cur.getX() - prv.getX());
      double dy0 = Math.signum(cur.getY() - prv.getY());
      double dx1 = Math.signum(nxt.getX() - cur.getX());
      double dy1 = Math.signum(nxt.getY() - cur.getY());
      path.curveTo(
          cur.getX() - dx0 * akv, cur.getY() - dy0 * akv,
          cur.getX() + dx1 * akv, cur.getY() + dy1 * akv,
          cur.getX() + dx1 * arc, cur.getY() + dy1 * arc);
      path.lineTo(nxt.getX() - dx1 * arc, nxt.getY() - dy1 * arc);
    }
    path.closePath();
    return path;
  }

  public static List<Area> singularization(Area rect) {
    List<Area> list = new ArrayList<>();
    Path2D path = new Path2D.Double();
    PathIterator pi = rect.getPathIterator(null);
    double[] coords = new double[6];
    while (!pi.isDone()) {
      switch (pi.currentSegment(coords)) {
        case PathIterator.SEG_MOVETO:
          path.moveTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_LINETO:
          path.lineTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_QUADTO:
          path.quadTo(coords[0], coords[1], coords[2], coords[3]);
          break;
        case PathIterator.SEG_CUBICTO:
          path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
          break;
        case PathIterator.SEG_CLOSE:
          path.closePath();
          list.add(new Area(path));
          path.reset();
          break;
        default:
          break;
      }
      pi.next();
    }
    return list;
  }
}
