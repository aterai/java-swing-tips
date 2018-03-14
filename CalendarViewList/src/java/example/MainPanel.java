package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    public final Dimension size = new Dimension(40, 26);
    public final JLabel yearMonthLabel = new JLabel("", SwingConstants.CENTER);
    public final JList<LocalDate> monthList = new JList<LocalDate>() {
        @Override public void updateUI() {
            setCellRenderer(null);
            super.updateUI();
            setLayoutOrientation(JList.HORIZONTAL_WRAP);
            setVisibleRowCount(CalendarViewListModel.ROW_COUNT); // ensure 6 rows in the list
            setFixedCellWidth(size.width);
            setFixedCellHeight(size.height);
            setCellRenderer(new CalendarListRenderer());
            getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        }
    };
    public final LocalDate realLocalDate = LocalDate.now();
    public LocalDate currentLocalDate;

    private MainPanel() {
        super();
        installActions();

        Locale l = Locale.getDefault();
        DefaultListModel<DayOfWeek> weekModel = new DefaultListModel<>();
        DayOfWeek firstDayOfWeek = WeekFields.of(l).getFirstDayOfWeek();
        for (int i = 0; i < DayOfWeek.values().length; i++) {
            weekModel.add(i, firstDayOfWeek.plus(i));
        }
        JList<DayOfWeek> header = new JList<>(weekModel);
        header.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        header.setVisibleRowCount(0);
        header.setFixedCellWidth(size.width);
        header.setFixedCellHeight(size.height);
        header.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, false, false);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (value instanceof DayOfWeek) {
                    DayOfWeek dow = (DayOfWeek) value;
                    // String s = dow.getDisplayName(TextStyle.SHORT_STANDALONE, l);
                    // setText(s.substring(0, Math.min(2, s.length())));
                    setText(dow.getDisplayName(TextStyle.SHORT_STANDALONE, l));
                    setBackground(new Color(220, 220, 220));
                }
                return this;
            }
        });
        updateMonthView(realLocalDate);

        JButton prev = new JButton("<");
        prev.addActionListener(e -> updateMonthView(currentLocalDate.minusMonths(1)));

        JButton next = new JButton(">");
        next.addActionListener(e -> updateMonthView(currentLocalDate.plusMonths(1)));

        JPanel yearMonthPanel = new JPanel(new BorderLayout());
        yearMonthPanel.add(yearMonthLabel);
        yearMonthPanel.add(prev, BorderLayout.WEST);
        yearMonthPanel.add(next, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(monthList);
        scroll.setColumnHeaderView(header);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

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
    private void installActions() {
        InputMap im = monthList.getInputMap(JComponent.WHEN_FOCUSED);
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
                    updateMonthView(currentLocalDate.minusMonths(1));
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
                    LocalDate d = monthList.getModel().getElementAt(monthList.getModel().getSize() - 1).plusDays(1);
                    updateMonthView(currentLocalDate.plusMonths(1));
                    monthList.setSelectedValue(d, false);
                }
            }
        });
        Action selectPreviousRow = am.get("selectPreviousRow");
        am.put("selectPreviousRow", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int index = monthList.getLeadSelectionIndex();
                int dowvl = DayOfWeek.values().length; // 7
                if (index < dowvl) {
                    LocalDate d = monthList.getModel().getElementAt(index).minusDays(dowvl);
                    updateMonthView(currentLocalDate.minusMonths(1));
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
                int dowvl = DayOfWeek.values().length; // 7
                if (index > monthList.getModel().getSize() - dowvl) {
                    LocalDate d = monthList.getModel().getElementAt(index).plusDays(dowvl);
                    updateMonthView(currentLocalDate.plusMonths(1));
                    monthList.setSelectedValue(d, false);
                } else {
                    selectNextRow.actionPerformed(e);
                }
            }
        });
    }
    public void updateMonthView(LocalDate localDate) {
        currentLocalDate = localDate;
        yearMonthLabel.setText(localDate.format(DateTimeFormatter.ofPattern("YYYY / MMMM").withLocale(Locale.getDefault())));
        monthList.setModel(new CalendarViewListModel(localDate));
    }
    private class CalendarListRenderer implements ListCellRenderer<LocalDate> {
        private final ListCellRenderer<? super LocalDate> renderer = new DefaultListCellRenderer();
        @Override public Component getListCellRendererComponent(JList<? extends LocalDate> list, LocalDate value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            l.setOpaque(true);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setText(Objects.toString(value.getDayOfMonth()));
            Color fgc = l.getForeground();
            if (YearMonth.from(value).equals(YearMonth.from(currentLocalDate))) {
                DayOfWeek dow = value.getDayOfWeek();
                if (value.isEqual(realLocalDate)) {
                    fgc = new Color(100, 255, 100);
                } else if (dow == DayOfWeek.SUNDAY) {
                    fgc = new Color(255, 100, 100);
                } else if (dow == DayOfWeek.SATURDAY) {
                    fgc = new Color(100, 100, 255);
                }
            } else {
                fgc = Color.GRAY;
            }
            l.setForeground(isSelected ? l.getForeground() : fgc);
            return l;
        }
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
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
    private final WeekFields weekFields = WeekFields.of(Locale.getDefault());
    protected CalendarViewListModel(LocalDate date) {
        super();
        LocalDate firstDayOfMonth = YearMonth.from(date).atDay(1);
        int dowv = firstDayOfMonth.get(weekFields.dayOfWeek()) - 1;
        startDate = firstDayOfMonth.minusDays(dowv);
    }
    @Override public int getSize() {
        return DayOfWeek.values().length * ROW_COUNT;
    }
    @Override public LocalDate getElementAt(int index) {
        return startDate.plusDays(index);
    }
}
