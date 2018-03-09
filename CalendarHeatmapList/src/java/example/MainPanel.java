package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    public final Dimension size = new Dimension(10, 10);
    public final LocalDate currentLocalDate = LocalDate.now();
    public final JList<Contribution> weekList = new JList<Contribution>(new CalendarViewListModel(currentLocalDate)) {
        @Override public void updateUI() {
            setCellRenderer(null);
            super.updateUI();
            setLayoutOrientation(JList.VERTICAL_WRAP);
            setVisibleRowCount(DayOfWeek.values().length); // ensure 7 rows in the list
            setFixedCellWidth(size.width);
            setFixedCellHeight(size.height);
            setCellRenderer(new ContributionListRenderer());
            getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }
    };
    public final Color color = new Color(50, 200, 50);
    public final List<Icon> activityIcons = Arrays.asList(
        new ColorIcon(new Color(200, 200, 200)),
        new ColorIcon(color.brighter()),
        new ColorIcon(color),
        new ColorIcon(color.darker()),
        new ColorIcon(color.darker().darker()));

    private MainPanel() {
        super(new BorderLayout());
        Font font = weekList.getFont().deriveFont(size.height - 1f);

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
        rowHeader.setFixedCellHeight(size.height);

        JPanel colHeader = new JPanel(new GridBagLayout());
        colHeader.setBackground(Color.WHITE);
        GridBagConstraints cc = new GridBagConstraints();
        for (cc.gridx = 0; cc.gridx < CalendarViewListModel.WEEK_VIEW; cc.gridx++) {
            colHeader.add(Box.createHorizontalStrut(size.width), cc); // grid guides
        }
        cc.anchor = GridBagConstraints.LINE_START;
        cc.gridy = 1;
        cc.gridwidth = 4; // use 4 columns to display the name of the month
        for (cc.gridx = 0; cc.gridx < CalendarViewListModel.WEEK_VIEW - cc.gridwidth + 1; cc.gridx++) {
            LocalDate date = weekList.getModel().getElementAt(cc.gridx * DayOfWeek.values().length).date;
            // int weekNumberOfMonth = date.get(weekFields.weekOfMonth());
            // System.out.println(weekNumberOfMonth);
            boolean isSimplyFirstWeekOfMonth = date.getMonth() != date.minusWeeks(1).getMonth(); // ignore WeekFields#getMinimalDaysInFirstWeek()
            if (isSimplyFirstWeekOfMonth) {
                colHeader.add(makeLabel(date.getMonth().getDisplayName(TextStyle.SHORT, l), font), cc);
            }
        }

        JScrollPane scroll = new JScrollPane(weekList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setColumnHeaderView(colHeader);
        scroll.setRowHeaderView(rowHeader);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBackground(Color.WHITE);

        Box box = Box.createHorizontalBox();
        box.add(makeLabel("Less", font));
        box.add(Box.createHorizontalStrut(2));
        activityIcons.forEach(icon -> {
            box.add(new JLabel(icon));
            box.add(Box.createHorizontalStrut(2));
        });
        box.add(makeLabel("More", font));

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 2, 10, 10));
        p.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        p.add(scroll, c);

        c.insets = new Insets(10, 0, 2, 0);
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_END;
        p.add(box, c);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea()));
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setPreferredSize(new Dimension(320, 240));
    }
    private class ContributionListRenderer implements ListCellRenderer<Contribution> {
        private final ListCellRenderer<? super Contribution> renderer = new DefaultListCellRenderer();
        @Override public Component getListCellRendererComponent(JList<? extends Contribution> list, Contribution value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) renderer.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
            if (value.date.isAfter(currentLocalDate)) {
                l.setIcon(new ColorIcon(Color.WHITE));
                l.setToolTipText(null);
            } else {
                l.setIcon(activityIcons.get(value.activity));
                String actTxt = value.activity == 0 ? "No" : Objects.toString(value.activity);
                l.setToolTipText(actTxt + " contribution on " + value.date.toString());
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
    private final Map<LocalDate, Integer> contributionActivity = new ConcurrentHashMap<>(getSize());
    protected CalendarViewListModel(LocalDate date) {
        super();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int dow = date.get(weekFields.dayOfWeek()) - 1;
        // int wby = date.get(weekFields.weekOfWeekBasedYear());
        startDate = date.minusWeeks(WEEK_VIEW - 1).minusDays(dow);
        Random rnd = new Random();
        int size = DayOfWeek.values().length * WEEK_VIEW;
        IntStream.range(0, size).forEach(i -> contributionActivity.put(startDate.plusDays(i), rnd.nextInt(5)));
    }
    @Override public int getSize() {
        return DayOfWeek.values().length * WEEK_VIEW;
    }
    @Override public Contribution getElementAt(int index) {
        LocalDate date = startDate.plusDays(index);
        return new Contribution(date, contributionActivity.get(date));
    }
}

class ColorIcon implements Icon {
    private final Color color;
    protected ColorIcon(Color color) {
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
        return 8;
    }
    @Override public int getIconHeight() {
        return 8;
    }
}
