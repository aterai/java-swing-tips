package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private static String[] columnNames = {"String", "Integer", "Boolean"};
    private static Object[][] data = {
        {"AAA", 0, true}, {"BBB", 1, false},
        {"CCC", 2, true}, {"DDD", 3, true},
        {"EEE", 4, true}, {"FFF", 5, false},
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        final JTable table = new JTable(model);
        final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model) {
            @Override public boolean isSortable(int column) {
                return false;
            }
        };
        sorter.setRowFilter(new RowFilter<TableModel,Integer>() {
            @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                return entry.getIdentifier() % 2 == 0;
            }
        });
        add(new JCheckBox(new AbstractAction("filter: idx%2==0") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox c = (JCheckBox)e.getSource();
                table.setRowSorter(c.isSelected()?sorter:null);
            }
        }), BorderLayout.NORTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 200));
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
