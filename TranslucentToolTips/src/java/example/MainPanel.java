// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public static final Dimension CELLSZ = new Dimension(10, 10);
  public final LocalDate currentLocalDate = LocalDate.now(ZoneId.systemDefault());
  public final JList<Contribution> weekList = new JList<Contribution>(new CalendarViewListModel(currentLocalDate)) {
    private transient JToolTip tip;

    @Override public void updateUI() {
      setCellRenderer(null);
      super.updateUI();
      setLayoutOrientation(JList.VERTICAL_WRAP);
      setVisibleRowCount(DayOfWeek.values().length); // ensure 7 rows in the list
      setFixedCellWidth(CELLSZ.width);
      setFixedCellHeight(CELLSZ.height);
      setCellRenderer(new ContributionListRenderer());
      getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    @Override public String getToolTipText(MouseEvent e) {
      Point p = e.getPoint();
      int idx = locationToIndex(p);
      Rectangle rect = getCellBounds(idx, idx);
      if (idx < 0 || !rect.contains(p.x, p.y)) {
        return null;
      }
      Contribution value = getModel().getElementAt(idx);
      String actTxt = value.activity == 0 ? "No" : Objects.toString(value.activity);
      return "<html>" + actTxt + " contribution <span style='color:#C8C8C8'> on " + value.date.toString();
    }

    @Override public Point getToolTipLocation(MouseEvent e) {
      Point p = e.getPoint();
      int i = locationToIndex(p);
      Rectangle rect = getCellBounds(i, i);

      String toolTipText = getToolTipText(e);
      if (Objects.nonNull(toolTipText)) {
        JToolTip tip = createToolTip();
        tip.setTipText(toolTipText);
        Dimension d = tip.getPreferredSize();
        int gap = 2;
        return new Point((int) (rect.getCenterX() - d.getWidth() / 2d), rect.y - d.height - gap);
      }
      return null;
    }

    @Override public JToolTip createToolTip() {
      if (tip == null) {
        tip = new BalloonToolTip();
        tip.setComponent(this);
      }
      return tip;
    }
  };
  public final Color color = new Color(0x32_C8_32);
  public final List<Icon> activityIcons = Arrays.asList(
      new ContributionIcon(new Color(0xC8_C8_C8)),
      new ContributionIcon(color.brighter()),
      new ContributionIcon(color),
      new ContributionIcon(color.darker()),
      new ContributionIcon(color.darker().darker()));

  private MainPanel() {
    super(new BorderLayout());
    Font font = weekList.getFont().deriveFont(CELLSZ.height - 1f);

    Box box = Box.createHorizontalBox();
    box.add(makeLabel("Less", font));
    box.add(Box.createHorizontalStrut(2));
    activityIcons.forEach(icon -> {
      box.add(new JLabel(icon));
      box.add(Box.createHorizontalStrut(2));
    });
    box.add(makeLabel("More", font));

    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createEmptyBorder(10, 2, 10, 2));
    p.setBackground(Color.WHITE);

    GridBagConstraints c = new GridBagConstraints();
    p.add(makeWeekCalendar(weekList, font), c);

    c.insets = new Insets(10, 0, 2, 0);
    c.gridy = 1;
    c.anchor = GridBagConstraints.LINE_END;
    p.add(box, c);

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea()));
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeWeekCalendar(JList<Contribution> weekList, Font font) {
    Locale l = Locale.getDefault();
    WeekFields weekFields = WeekFields.of(l);

    DefaultListModel<String> weekModel = new DefaultListModel<>();
    DayOfWeek firstDayOfWeek = weekFields.getFirstDayOfWeek();
    for (int i = 0; i < DayOfWeek.values().length; i++) {
      boolean isEven = i % 2 == 0;
      if (isEven) {
        weekModel.add(i, "");
      } else {
        weekModel.add(i, firstDayOfWeek.plus(i).getDisplayName(TextStyle.SHORT_STANDALONE, l));
      }
    }
    JList<String> rowHeader = new JList<>(weekModel);
    rowHeader.setEnabled(false);
    rowHeader.setFont(font);
    rowHeader.setLayoutOrientation(JList.VERTICAL_WRAP);
    rowHeader.setVisibleRowCount(DayOfWeek.values().length);
    rowHeader.setFixedCellHeight(CELLSZ.height);

    JPanel colHeader = new JPanel(new GridBagLayout());
    colHeader.setBackground(Color.WHITE);
    GridBagConstraints c = new GridBagConstraints();
    for (c.gridx = 0; c.gridx < CalendarViewListModel.WEEK_VIEW; c.gridx++) {
      colHeader.add(Box.createHorizontalStrut(CELLSZ.width), c); // grid guides
    }
    c.anchor = GridBagConstraints.LINE_START;
    c.gridy = 1;
    c.gridwidth = 3; // use 3 columns to display the name of the month
    for (c.gridx = 0; c.gridx < CalendarViewListModel.WEEK_VIEW - c.gridwidth + 1; c.gridx++) {
      LocalDate date = weekList.getModel().getElementAt(c.gridx * DayOfWeek.values().length).date;
      boolean isSimplyFirstWeekOfMonth = date.getMonth() != date.minusWeeks(1).getMonth();
      if (isSimplyFirstWeekOfMonth) {
        colHeader.add(makeLabel(date.getMonth().getDisplayName(TextStyle.SHORT, l), font), c);
      }
    }

    JScrollPane scroll = new JScrollPane(weekList);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setColumnHeaderView(colHeader);
    scroll.setRowHeaderView(rowHeader);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBackground(Color.WHITE);

    return scroll;
  }

  private class ContributionListRenderer implements ListCellRenderer<Contribution> {
    private final ListCellRenderer<? super Contribution> renderer = new DefaultListCellRenderer();

    @Override public Component getListCellRendererComponent(JList<? extends Contribution> list, Contribution value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel l = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value.date.isAfter(currentLocalDate)) {
        l.setIcon(new ContributionIcon(Color.WHITE));
      } else {
        l.setIcon(activityIcons.get(value.activity));
      }
      return l;
    }
  }

  private static JLabel makeLabel(String title, Font font) {
    JLabel label = new JLabel(title);
    label.setFont(font);
    label.setEnabled(false);
    return label;
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

class Contribution {
  public final LocalDate date;
  public final int activity;

  protected Contribution(LocalDate date, int activity) {
    this.date = date;
    this.activity = activity;
  }
}

class CalendarViewListModel extends AbstractListModel<Contribution> {
  public static final int WEEK_VIEW = 27;
  private final LocalDate startDate;
  private final int displayDays;
  private final Map<LocalDate, Integer> contributionActivity;

  protected CalendarViewListModel(LocalDate date) {
    super();
    int dow = date.get(WeekFields.of(Locale.getDefault()).dayOfWeek());
    this.startDate = date.minusWeeks(WEEK_VIEW - 1).minusDays(dow - 1);
    this.displayDays = DayOfWeek.values().length * (WEEK_VIEW - 1) + dow;
    this.contributionActivity = new ConcurrentHashMap<>(displayDays);
    Random rnd = new Random();
    IntStream.range(0, displayDays).forEach(i -> contributionActivity.put(startDate.plusDays(i), rnd.nextInt(5)));
  }

  @Override public int getSize() {
    return displayDays;
  }

  @Override public Contribution getElementAt(int index) {
    LocalDate date = startDate.plusDays(index);
    return new Contribution(date, contributionActivity.get(date));
  }
}

class ContributionIcon implements Icon {
  private final Color color;

  protected ContributionIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return MainPanel.CELLSZ.width - 2;
  }

  @Override public int getIconHeight() {
    return MainPanel.CELLSZ.height - 2;
  }
}

class BalloonToolTip extends JToolTip {
  private static final int TRI_HEIGHT = 4;
  private HierarchyListener listener;

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    listener = e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
        Optional.ofNullable(SwingUtilities.getRoot(c))
           .filter(JWindow.class::isInstance).map(JWindow.class::cast)
           .ifPresent(w -> w.setBackground(new Color(0x0, true)));
      }
    };
    addHierarchyListener(listener);
    setOpaque(false);
    setForeground(Color.WHITE);
    setBackground(new Color(0xC8_00_00_00, true));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5 + TRI_HEIGHT, 5));
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.height = 32;
    return d;
  }

  @Override protected void paintComponent(Graphics g) {
    Shape s = makeBalloonShape();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(getBackground());
    g2.fill(s);
    g2.dispose();
    super.paintComponent(g);
  }

  private Shape makeBalloonShape() {
    int w = getWidth() - 1;
    int h = getHeight() - TRI_HEIGHT - 1;
    int r = 10;
    int cx = getWidth() / 2;
    Polygon triangle = new Polygon();
    triangle.addPoint(cx - TRI_HEIGHT, h);
    triangle.addPoint(cx, h + TRI_HEIGHT);
    triangle.addPoint(cx + TRI_HEIGHT, h);
    Area area = new Area(new RoundRectangle2D.Float(0, 0, w, h, r, r));
    area.add(new Area(triangle));
    return area;
  }
}
