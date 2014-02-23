package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
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
        @Override public String getToolTipText(MouseEvent e) {
            int row = convertRowIndexToModel(rowAtPoint(e.getPoint()));
            TableModel m = getModel();
            return String.format("<html>%s<br>%d<br>%s</html>", m.getValueAt(row, 0), (Integer) m.getValueAt(row, 1), m.getValueAt(row, 2));
        }
//         public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
//             Component c = super.prepareRenderer(tcr, row, column);
//             if (c instanceof JComponent) {
//                 int mr = convertRowIndexToModel(row);
//                 int mc = convertColumnIndexToModel(column);
//                 Object o = getModel().getValueAt(mr, mc);
//                 String s = Objects.toString(o, "");
//                 ((JComponent) c).setToolTipText(s.isEmpty() ? null : s);
//             }
//             return c;
//         }
    };

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
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
