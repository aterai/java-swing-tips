package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"aaa", "bbb"};
    private final Object[][] data = {
        {Integer.MIN_VALUE, Integer.MIN_VALUE},
        {1, 1}, {1, 2}, {1, -1}, {1, 3}, {1, 0},
        {1, 5}, {1, 4}, {1, -5}, {1, 0}, {1, 6},
        {Integer.MAX_VALUE, Integer.MAX_VALUE}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return Integer.class; //getValueAt(0, column).getClass();
        }
        @Override public boolean isCellEditable(int row, int column) {
            return row>0 && row!=model.getRowCount()-1;
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(makeTable()));
        setPreferredSize(new Dimension(320, 240));
    }
    private JTable makeTable() {
        final JTable table = new JTable(model);
        final RowFilter<TableModel,Integer> filter = new RowFilter<TableModel,Integer>() {
            @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                return 0 != table.convertRowIndexToView(entry.getIdentifier());
            }
        };
        final TableRowSorter<TableModel> s = new TableRowSorter<TableModel>(model) {
            @Override public void toggleSortOrder(int column) {
                RowFilter<? super TableModel, ? super Integer> f = getRowFilter();
                setRowFilter(null);
                super.toggleSortOrder(column);
                setRowFilter(f);
            }
        };
        s.setRowFilter(filter);
        //s.setSortsOnUpdates(true);
        s.toggleSortOrder(1);
        table.setRowSorter(s);

        model.addTableModelListener(new TableModelListener() {
            @Override public void tableChanged(TableModelEvent e) {
                if(e.getType()==TableModelEvent.UPDATE) {
                    table.repaint();
                }
            }
        });
        TableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l;
                if(row==model.getRowCount()-2) {
                    int i = getSum(table.convertColumnIndexToModel(column));
                    l = (JLabel)super.getTableCellRendererComponent(table, i, isSelected, hasFocus, row, column);
                    l.setBackground(Color.ORANGE);
                }else{
                    l = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    l.setBackground(Color.WHITE);
                }
                l.setForeground(Color.BLACK);
                return l;
            }
        };
        TableColumnModel cm = table.getColumnModel();
        for(int i=0;i<cm.getColumnCount();i++) {
            cm.getColumn(i).setCellRenderer(renderer);
        }
        return table;
    }
    private int getSum(int column) {
        int counter = 0;
        for(int i=1;i<model.getRowCount()-1;i++) {
            counter += (Integer)model.getValueAt(i,column);
        }
        return counter;
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
