// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    JLabel yearMonthLabel = new JLabel("", SwingConstants.CENTER);
    MonthList monthList = new MonthList();
    installActions(monthList, yearMonthLabel);

    JButton prev = new JButton("<");
    prev.addActionListener(e -> {
      LocalDate date = monthList.getCurrentLocalDate().minusMonths(1);
      updateMonthView(monthList, yearMonthLabel, date);
    });

    JButton next = new JButton(">");
    next.addActionListener(e -> {
      LocalDate date = monthList.getCurrentLocalDate().plusMonths(1);
      updateMonthView(monthList, yearMonthLabel, date);
    });

    JPanel yearMonthPanel = new JPanel(new BorderLayout());
    yearMonthPanel.add(yearMonthLabel);
    yearMonthPanel.add(prev, BorderLayout.WEST);
    yearMonthPanel.add(next, BorderLayout.EAST);
    Box box = Box.createVerticalBox();
    box.add(yearMonthPanel);
    box.add(Box.createVerticalStrut(2));

    updateMonthView(monthList, yearMonthLabel, monthList.getRealLocalDate());

    JScrollPane scroll = new JScrollPane(monthList) {
      @Override public void updateUI() {
        super.updateUI();
        setColumnHeaderView(new WeekHeaderList());
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        EventQueue.invokeLater(() -> {
          Container p = SwingUtilities.getUnwrappedParent(this);
          p.revalidate();
          p.repaint();
        });
      }
    };
    box.add(scroll);

    JLabel label = new JLabel(" ", SwingConstants.CENTER);
    box.add(label);
    monthList.getSelectionModel().addListSelectionListener(e -> {
      ListSelectionModel lsm = (ListSelectionModel) e.getSource();
      label.setText(lsm.isSelectionEmpty() ? " " : between(monthList, lsm));
    });

    add(box);
    setPreferredSize(new Dimension(320, 240));
  }

  private String between(MonthList monthList, ListSelectionModel lsm) {
    ListModel<LocalDate> model = monthList.getModel();
    LocalDate from = model.getElementAt(lsm.getMinSelectionIndex());
    LocalDate to = model.getElementAt(lsm.getMaxSelectionIndex());
    return Period.between(from, to).toString();
  }

  private void installActions(MonthList monthList, JLabel label) {
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
          LocalDate date = monthList.getCurrentLocalDate().minusMonths(1);
          updateMonthView(monthList, label, date);
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
          LocalDate date = monthList.getCurrentLocalDate().plusMonths(1);
          updateMonthView(monthList, label, date);
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
          LocalDate date = monthList.getCurrentLocalDate().minusMonths(1);
          updateMonthView(monthList, label, date);
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
          LocalDate date = monthList.getCurrentLocalDate().plusMonths(1);
          updateMonthView(monthList, label, date);
          monthList.setSelectedValue(d, false);
        } else {
          selectNextRow.actionPerformed(e);
        }
      }
    });
  }

  private void updateMonthView(MonthList list, JLabel label, LocalDate localDate) {
    list.setCurrentLocalDate(localDate);
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy / MM");
    label.setText(localDate.format(fmt.withLocale(Locale.getDefault())));
    list.setModel(new CalendarViewListModel(localDate));
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

class MonthList extends JList<LocalDate> {
  private final LocalDate realLocalDate = LocalDate.now(ZoneId.systemDefault());
  private LocalDate currentLocalDate;
  private transient MouseAdapter handler;

  protected MonthList() {
    super();
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    setCellRenderer(null);
    super.updateUI();
    setLayoutOrientation(HORIZONTAL_WRAP);
    setVisibleRowCount(CalendarViewListModel.ROW_COUNT); // ensure 6 rows in the list
    setFixedCellWidth(WeekHeaderList.CELL_SIZE.width);
    setFixedCellHeight(WeekHeaderList.CELL_SIZE.height);
    setCellRenderer(new CalendarListRenderer());
    getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    handler = new SingleIntervalMouseSelectionListener();
    addMouseListener(handler);
    addMouseMotionListener(handler);
  }

  public LocalDate getRealLocalDate() {
    return realLocalDate;
  }

  public void setCurrentLocalDate(LocalDate date) {
    currentLocalDate = date;
  }

  public LocalDate getCurrentLocalDate() {
    return currentLocalDate;
  }

  private final class CalendarListRenderer implements ListCellRenderer<LocalDate> {
    private final ListCellRenderer<? super LocalDate> renderer = new DefaultListCellRenderer();

    @Override public Component getListCellRendererComponent(JList<? extends LocalDate> list, LocalDate value, int index, boolean isSelected, boolean cellHasFocus) {
      Component c = renderer.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      if (c instanceof JLabel) {
        JLabel l = (JLabel) c;
        l.setOpaque(true);
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
      c.setForeground(isSelected ? c.getForeground() : fgc);
      return c;
    }
  }
}

class WeekHeaderList extends JList<DayOfWeek> {
  public static final Dimension CELL_SIZE = new Dimension(40, 26);

  protected WeekHeaderList() {
    super(makeDayOfWeekListModel());
  }

  @Override public void updateUI() {
    setCellRenderer(null);
    super.updateUI();
    Color bgc = UIManager.getColor("TableHeader.background").darker();
    ListCellRenderer<? super DayOfWeek> r = getCellRenderer();
    setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c = r.getListCellRendererComponent(list, value, index, false, false);
      c.setBackground(bgc);
      if (c instanceof JLabel) {
        JLabel l = (JLabel) c;
        l.setHorizontalAlignment(SwingConstants.CENTER);
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

  private static ListModel<DayOfWeek> makeDayOfWeekListModel() {
    DefaultListModel<DayOfWeek> weekModel = new DefaultListModel<>();
    DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
    for (int i = 0; i < DayOfWeek.values().length; i++) {
      weekModel.add(i, firstDayOfWeek.plus(i));
    }
    return weekModel;
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

class SingleIntervalMouseSelectionListener extends MouseAdapter {
  private int start = -1;

  @Override public void mousePressed(MouseEvent e) {
    JList<?> l = (JList<?>) e.getComponent();
    start = l.locationToIndex(e.getPoint());
    l.getSelectionModel().addSelectionInterval(start, start);
  }

  @Override public void mouseDragged(MouseEvent e) {
    JList<?> l = (JList<?>) e.getComponent();
    int end = l.locationToIndex(e.getPoint());
    l.getSelectionModel().addSelectionInterval(start, end);
  }
}
