package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {null,  15, true}, {"", null, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    protected final TableSorter sorter = new TableSorter(model);
    protected final JTable table = new JTable(sorter) {
        @Override public void updateUI() {
            sorter.setTableHeader(null);
            super.updateUI();
            EventQueue.invokeLater(() -> {
                JTableHeader h = table.getTableHeader();
                sorter.setTableHeader(h);
                h.repaint();
            });
        }
    };
    public MainPanel() {
        super(new BorderLayout());
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
