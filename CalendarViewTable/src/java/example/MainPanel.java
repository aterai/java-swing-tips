package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    public LocalDate currentLocalDate;
    public final LocalDate realLocalDate = LocalDate.now();
    private final JLabel dateLabel = new JLabel(realLocalDate.toString(), SwingConstants.CENTER);
    private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    private final JTable monthTable = new JTable();

    private MainPanel() {
        super(new BorderLayout());

        monthTable.setDefaultRenderer(LocalDate.class, new CalendarTableRenderer());
        monthTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        monthTable.setCellSelectionEnabled(true);
        monthTable.setRowHeight(20);
        monthTable.setFillsViewportHeight(true);

        JTableHeader header = monthTable.getTableHeader();
        header.setResizingAllowed(false);
        header.setReorderingAllowed(false);
        ((JLabel) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        ListSelectionListener selectionListener = e -> {
            if (!e.getValueIsAdjusting()) {
                LocalDate ld = (LocalDate) monthTable.getValueAt(monthTable.getSelectedRow(), monthTable.getSelectedColumn());
                dateLabel.setText(ld.toString());
            }
        };
        monthTable.getSelectionModel().addListSelectionListener(selectionListener);
        monthTable.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);

        updateMonthView(realLocalDate);

        JButton prev = new JButton("<");
        prev.addActionListener(e -> updateMonthView(currentLocalDate.minusMonths(1)));

        JButton next = new JButton(">");
        next.addActionListener(e -> updateMonthView(currentLocalDate.plusMonths(1)));

        JPanel p = new JPanel(new BorderLayout());
        p.add(monthLabel);
        p.add(prev, BorderLayout.WEST);
        p.add(next, BorderLayout.EAST);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(monthTable));
        add(dateLabel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    public void updateMonthView(LocalDate localDate) {
        currentLocalDate = localDate;
        monthLabel.setText(localDate.format(DateTimeFormatter.ofPattern("YYYY / MMMM").withLocale(Locale.getDefault())));
        monthTable.setModel(new CalendarViewTableModel<>(localDate));
    }
    private class CalendarTableRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (value instanceof LocalDate) {
                LocalDate d = (LocalDate) value;
                setText(String.valueOf(d.getDayOfMonth()));
                if (YearMonth.from(d).equals(YearMonth.from(currentLocalDate))) {
                    setForeground(Color.BLACK);
                } else {
                    setForeground(Color.GRAY);
                }
                DayOfWeek dow = d.getDayOfWeek();
                if (d.isEqual(realLocalDate)) {
                    setBackground(new Color(220, 255, 220));
                } else if (dow == DayOfWeek.SUNDAY) {
                    setBackground(new Color(255, 220, 220));
                } else if (dow == DayOfWeek.SATURDAY) {
                    setBackground(new Color(220, 220, 255));
                } else {
                    setBackground(Color.WHITE);
                }
            }
            return this;
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

class CalendarViewTableModel<T extends LocalDate> extends DefaultTableModel {
    private final LocalDate startDate;
    private final WeekFields weekFields = WeekFields.of(Locale.getDefault());
    protected CalendarViewTableModel(T date) {
        super();
        LocalDate firstDayOfMonth = YearMonth.from(date).atDay(1); // date.with(TemporalAdjusters.firstDayOfMonth());
        // int dowv = firstDayOfMonth.get(WeekFields.SUNDAY_START.dayOfWeek()) - 1;
        int dowv = firstDayOfMonth.get(weekFields.dayOfWeek()) - 1;
        startDate = firstDayOfMonth.minusDays(dowv);
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
        return startDate.plusDays(row * getColumnCount() + column);
    }
    @Override public boolean isCellEditable(int row, int column) {
        return false;
    }
}
