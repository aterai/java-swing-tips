package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);
    private final transient RowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);

    private static final Icon EMPTY_ICON = new EmptyIcon();
    private final Icon customAscendingSortIcon  = new ImageIcon(getClass().getResource("ascending.png"));
    private final Icon customDescendingSortIcon = new ImageIcon(getClass().getResource("descending.png"));

    private final JButton clearButton = new JButton("clear SortKeys");

    private Box makeRadioPane() {
        JRadioButton r0 = new JRadioButton("Default");
        JRadioButton r1 = new JRadioButton("Empty");
        JRadioButton r2 = new JRadioButton("Cumstom");
        ActionListener al = e -> {
            JRadioButton r = (JRadioButton) e.getSource();
            Icon ascending  = null;
            Icon descending = null;
            if (r.equals(r0)) {
                ascending  = UIManager.getIcon("Table.ascendingSortIcon");
                descending = UIManager.getIcon("Table.descendingSortIcon");
            } else if (r.equals(r1)) {
                ascending  = new IconUIResource(EMPTY_ICON);
                descending = new IconUIResource(EMPTY_ICON);
            } else {
                ascending  = new IconUIResource(customAscendingSortIcon);
                descending = new IconUIResource(customDescendingSortIcon);
            }
            UIManager.put("Table.ascendingSortIcon",  ascending);
            UIManager.put("Table.descendingSortIcon", descending);
            table.getTableHeader().repaint();
        };
        Box box1 = Box.createHorizontalBox();
        box1.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        ButtonGroup bg = new ButtonGroup();
        box1.add(new JLabel("Table Sort Icon: "));
        for (JRadioButton rb: Arrays.asList(r0, r1, r2)) {
            box1.add(rb);
            box1.add(Box.createHorizontalStrut(5));
            bg.add(rb);
            rb.addActionListener(al);
        }
        box1.add(Box.createHorizontalGlue());
        r0.setSelected(true);
        return box1;
    }

    public MainPanel() {
        super(new BorderLayout());
        table.setRowSorter(sorter);
        clearButton.addActionListener(e -> sorter.setSortKeys(null));

        add(makeRadioPane(), BorderLayout.NORTH);
        add(clearButton), BorderLayout.SOUTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
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

class EmptyIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* Empty icon */ }
    @Override public int getIconWidth() {
        return 0;
    }
    @Override public int getIconHeight() {
        return 0;
    }
}
