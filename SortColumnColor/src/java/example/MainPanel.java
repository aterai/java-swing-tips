package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String[] columnNames = {"String", "Integer", "Boolean"};
        Object[][] data = {
            {"aaa", 12, true}, {"bbb", 5, false},
            {"CCC", 92, true}, {"DDD", 0, false}
        };
        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model) {
            protected final Color evenColor = new Color(250, 230, 230);
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if (isRowSelected(row)) {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                } else {
                    c.setForeground(getForeground());
                    c.setBackground(isSortingColumn(column) ? evenColor : getBackground());
                }
                return c;
            }
            protected boolean isSortingColumn(int column) {
                RowSorter<? extends TableModel> sorter = getRowSorter();
                if (Objects.nonNull(sorter)) {
                    List<? extends RowSorter.SortKey> keys = sorter.getSortKeys();
                    return !keys.isEmpty() && column == convertColumnIndexToView(keys.get(0).getColumn());
                }
                return false;
            }
        };
        table.setAutoCreateRowSorter(true);

        JButton button = new JButton("clear SortKeys");
        button.addActionListener(e -> table.getRowSorter().setSortKeys(null));

        add(button, BorderLayout.SOUTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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
