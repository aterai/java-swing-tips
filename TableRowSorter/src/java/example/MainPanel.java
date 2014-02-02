package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private static final Color EVEN_COLOR = new Color(250, 250, 250);
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model) {
        @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
            Component c = super.prepareRenderer(tcr, row, column);
            if(isRowSelected(row)) {
                c.setForeground(getSelectionForeground());
                c.setBackground(getSelectionBackground());
            }else{
                c.setForeground(getForeground());
                c.setBackground((row%2==0)?EVEN_COLOR:getBackground());
            }
            return c;
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        //table.setAutoCreateRowSorter(true);
        //TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        table.setRowSorter(new TableRowSorter<TableModel>(model));
        //sorter.setSortKeys(java.util.Arrays.asList(new RowSorter.SortKey(0, SortOrder.DESCENDING)));

        TableCellRenderer renderer = new TableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int row, int col) {
                TableCellRenderer r = t.getTableHeader().getDefaultRenderer();
                JLabel l = (JLabel)r.getTableCellRendererComponent(t, v, isS, hasF, row, col);
                l.setForeground(((DefaultRowSorter)table.getRowSorter()).isSortable(t.convertColumnIndexToModel(col))?Color.BLACK:Color.GRAY);
                return l;
            }
        };
        TableColumnModel columns = table.getColumnModel();
        for(int i=0;i<columns.getColumnCount();i++) {
            TableColumn c = columns.getColumn(i);
            c.setHeaderRenderer(renderer);
            if(i==0) {
                c.setMinWidth(60);
                c.setMaxWidth(60);
                c.setResizable(false);
            }
        }
        add(new JCheckBox(new AbstractAction("Sortable(1, false)") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox)e.getSource();
                ((DefaultRowSorter<?,?>)table.getRowSorter()).setSortable(1, !cb.isSelected());
                table.getTableHeader().repaint();
            }
        }), BorderLayout.NORTH);
        add(new JButton(new AbstractAction("clear SortKeys") {
            @Override public void actionPerformed(ActionEvent e) {
                table.getRowSorter().setSortKeys(null);
            }
        }), BorderLayout.SOUTH);
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
