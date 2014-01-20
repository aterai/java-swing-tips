package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private static final int USER_SPECIFIED_NUMBER_OF_ROWS = 5;
    private final JCheckBox check = new JCheckBox("Custom Sorting");
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"AA",  1, true},{"BB",  2, false}, {"cc",  3, true},{"dd",  4, false},{"ee",  5, false},
        {"FF", -1, true},{"GG", -2, false}, {"HH", -3, true},{"II", -4, false},{"JJ", -5, false},
        {"KK", 11, true},{"LL", 22, false}, {"MM", 33, true},{"NN", 44, false},{"OO", 55, false},
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);
    public MainPanel() {
        super(new BorderLayout());
        table.setFillsViewportHeight(true);
        //XXX: sorter.setSortsOnUpdates(true);

        final RowFilter<TableModel, Integer> filter = new RowFilter<TableModel, Integer>() {
            @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                int vidx = table.convertRowIndexToView(entry.getIdentifier());
                return vidx<USER_SPECIFIED_NUMBER_OF_ROWS;
            }
        };
        final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model) {
            @Override public void toggleSortOrder(int column) {
                super.toggleSortOrder(column);
                if(check.isSelected()) {
                    model.fireTableDataChanged();
                    sort(); //allRowsChanged();
                }
//                 if(check.isSelected()) {
//                     RowFilter<? super TableModel, ? super Integer> f = getRowFilter();
//                     setRowFilter(null);
//                     super.toggleSortOrder(column);
//                     setRowFilter(f);
//                 }else{
//                     super.toggleSortOrder(column);
//                 }
            }
        };

        table.setRowSorter(sorter);
        sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(1, SortOrder.DESCENDING)));

        Box box = Box.createHorizontalBox();
        box.add(check);
        box.add(Box.createHorizontalStrut(5));
        box.add(new JCheckBox(new AbstractAction("viewRowIndex < "+USER_SPECIFIED_NUMBER_OF_ROWS) {
            @Override public void actionPerformed(ActionEvent e) {
                sorter.setRowFilter(((JCheckBox)e.getSource()).isSelected()?filter:null);
            }
        }));
        add(box, BorderLayout.NORTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
