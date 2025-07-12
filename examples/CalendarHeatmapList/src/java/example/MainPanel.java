// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Color color = new Color(0x32_C8_32);
    List<Icon> activityIcons = Arrays.asList(
        new ContributionIcon(new Color(0xC8_C8_C8)),
        new ContributionIcon(color.brighter()),
        new ContributionIcon(color),
        new ContributionIcon(color.darker()),
        new ContributionIcon(color.darker().darker()));

    LocalDate date = LocalDate.now(ZoneId.systemDefault());
    JList<Contribution> weekList = new ContributionCalendarList(date, activityIcons);
    int height = ContributionIcon.CELL_SIZE.height;
    Font font = weekList.getFont().deriveFont(height - 1f);

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 0, 2, 0));
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

    // c.insets = new Insets(10, 0, 2, 0);
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
    rowHeader.setFixedCellHeight(ContributionIcon.CELL_SIZE.height);

    JPanel colHeader = new JPanel(new GridBagLayout());
    colHeader.setBackground(Color.WHITE);
    GridBagConstraints c = new GridBagConstraints();
    for (c.gridx = 0; c.gridx < CalendarViewListModel.WEEK_VIEW; c.gridx++) {
      // grid guides
      colHeader.add(Box.createHorizontalStrut(ContributionIcon.CELL_SIZE.width), c);
    }
    c.anchor = GridBagConstraints.LINE_START;
    c.gridy = 1;
    c.gridwidth = 3; // use 3 columns to display the name of the month
    for (c.gridx = 0; c.gridx < CalendarViewListModel.WEEK_VIEW - c.gridwidth + 1; c.gridx++) {
      int index = c.gridx * DayOfWeek.values().length;
      Contribution contribution = weekList.getModel().getElementAt(index);
      LocalDate date = contribution.getDate();
      boolean isFirst = date.getMonth() != date.minusWeeks(1L).getMonth();
      if (isFirst) {
        colHeader.add(makeLabel(date.getMonth().getDisplayName(TextStyle.SHORT, l), font), c);
      }
    }
    return makeScrollPane(weekList, colHeader);
  }

  private static JScrollPane makeScrollPane(JList<Contribution> weekList, JPanel colHeader) {
    return new JScrollPane(weekList) {
      @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder());
        setColumnHeaderView(colHeader);
        setRowHeaderView(rowHeader);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        setBackground(Color.WHITE);
      }
    };
  }

  // private class ContributionListRenderer implements ListCellRenderer<Contribution> {
  //   private final ListCellRenderer<? super Contribution> r = new DefaultListCellRenderer();
  //
  //   @Override public Component getListCellRendererComponent(JList<? extends Contribution> list, Contribution value, int index, boolean isSelected, boolean cellHasFocus) {
  //     // Contribution v = Optional.ofNullable(value).orElseGet(list::getPrototypeCellValue);
  //     Component c = r.getListCellRendererComponent(
  //         list, value, index, isSelected, cellHasFocus);
  //     if (c instanceof JLabel) {
  //       JLabel l = (JLabel) c;
  //       if (value.date.isAfter(currentLocalDate)) {
  //         l.setIcon(new ContributionIcon(Color.WHITE));
  //       } else {
  //         l.setIcon(activityIcons.get(value.activity));
  //       }
  //     }
  //     return c;
  //   }
  // }

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

class Contribution {
  private final LocalDate date;
  private final int activity;

  protected Contribution(LocalDate date, int activity) {
    this.date = date;
    this.activity = activity;
  }

  public LocalDate getDate() {
    return date;
  }

  public int getActivity() {
    return activity;
  }
}

class CalendarViewListModel extends AbstractListModel<Contribution> {
  public static final int WEEK_VIEW = 27;
  private final LocalDate startDate;
  private final int displayDays;
  private final Map<LocalDate, Integer> contribution;

  protected CalendarViewListModel(LocalDate date) {
    super();
    int dow = date.get(WeekFields.of(Locale.getDefault()).dayOfWeek());
    this.startDate = date.minusWeeks(WEEK_VIEW - 1L).minusDays(dow - 1L);
    this.displayDays = DayOfWeek.values().length * (WEEK_VIEW - 1) + dow;
    this.contribution = new ConcurrentHashMap<>(displayDays);
    Random rnd = new Random();
    IntStream.range(0, displayDays).forEach(i -> {
      int iv = rnd.nextInt(5);
      contribution.put(startDate.plusDays(i), iv);
    });
  }

  @Override public int getSize() {
    return displayDays;
  }

  @Override public Contribution getElementAt(int index) {
    LocalDate date = startDate.plusDays(index);
    return new Contribution(date, contribution.get(date));
  }
}

class ContributionIcon implements Icon {
  public static final Dimension CELL_SIZE = new Dimension(10, 10);
  private final Color color;

  protected ContributionIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    // JList#setLayoutOrientation(VERTICAL_WRAP) + SynthLookAndFeel(Nimbus, GTK) bug???
    // g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return CELL_SIZE.width - 2;
  }

  @Override public int getIconHeight() {
    return CELL_SIZE.height - 2;
  }
}

class ContributionCalendarList extends JList<Contribution> {
  private final List<Icon> activityIcons;
  private final LocalDate currentLocalDate;

  protected ContributionCalendarList(LocalDate date, List<Icon> activityIcons) {
    super(new CalendarViewListModel(date));
    this.currentLocalDate = date;
    this.activityIcons = activityIcons;
  }

  @Override public void updateUI() {
    setCellRenderer(null);
    super.updateUI();
    setLayoutOrientation(VERTICAL_WRAP);
    setVisibleRowCount(DayOfWeek.values().length); // ensure 7 rows in the list
    setFixedCellWidth(ContributionIcon.CELL_SIZE.width);
    setFixedCellHeight(ContributionIcon.CELL_SIZE.height);
    ListCellRenderer<? super Contribution> renderer = getCellRenderer();
    setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c = renderer.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      if (c instanceof JLabel) {
        ((JLabel) c).setIcon(getActivityIcon(value));
      }
      return c;
    });
    getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
  }

  @Override public String getToolTipText(MouseEvent e) {
    Point p = e.getPoint();
    int i = locationToIndex(p);
    Rectangle r = getCellBounds(i, i);
    return r != null && r.contains(p) ? getActivityText(i) : null;
  }

  private Icon getActivityIcon(Contribution value) {
    return value.getDate().isAfter(currentLocalDate)
        ? new ContributionIcon(Color.WHITE)
        : activityIcons.get(value.getActivity());
  }

  private String getActivityText(int idx) {
    Contribution c = getModel().getElementAt(idx);
    int activity = c.getActivity();
    String actTxt = activity == 0 ? "No" : Integer.toString(activity);
    return String.format("%s contribution on %s", actTxt, c.getDate());
  }
}
