package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"AAA", "BBB", "CCC", "DDD"};
    private final Object[][] data = {
        {"aaa", "1", "true", "cc"}, {"aaa", "1", "false", "dd"},
        {"aaa", "2", "true", "ee"}, {"ddd", "3", "false", "ff"}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return String.class;
        }
    };
    private final JTable table = new JTable(model) {
        @Override public void updateUI() {
            super.updateUI();
            MultisortHeaderRenderer r = new MultisortHeaderRenderer();
            TableColumnModel cm = getColumnModel();
            for (int i = 0; i < cm.getColumnCount(); i++) {
                cm.getColumn(i).setHeaderRenderer(r);
            }
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
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

class MultisortHeaderRenderer implements TableCellRenderer {
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String str = Objects.toString(value, "");
        if (table.getRowSorter() != null) {
            List<?> keys = table.getRowSorter().getSortKeys();
            int max = keys.size();
            for (int i = 0; i < max; i++) {
                TableRowSorter.SortKey sortKey = (TableRowSorter.SortKey) keys.get(i);
                if (column == sortKey.getColumn()) {
                    //BLACK TRIANGLE
                    //String k = sortKey.getSortOrder() == SortOrder.ASCENDING ? "\u25B2 " : "\u25BC ";
                    //BLACK SMALL TRIANGLE
                    String k = sortKey.getSortOrder() == SortOrder.ASCENDING ? "\u25B4 " : "\u25BE ";
                    str = String.format("<html>%s<small color='gray'>%s%d", str, k, i + 1);
                }
            }
        }
        TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
        return r.getTableCellRendererComponent(table, str, isSelected, hasFocus, row, column);
    }
}
