package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        String[] columnNames = {"String", "Integer", "Boolean"};
        Object[][] data = {
            {"aaa", 12, true}, {"bbb", 5, false},
            {"CCC", 92, true}, {"DDD", 0, false}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        Font font = new Font("Sans-serif", Font.PLAIN, 32);
        table.getColumnModel().getColumn(0).setHeaderRenderer(new HeaderRenderer(font));

        //all column
        //table.getTableHeader().setFont(font);

        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    private static class HeaderRenderer implements TableCellRenderer {
        private final Font font;
        public HeaderRenderer(Font font) {
            this.font = font;
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean isS,
                                                                 boolean hasF, int row, int col) {
            TableCellRenderer r = t.getTableHeader().getDefaultRenderer();
            JLabel l = (JLabel)r.getTableCellRendererComponent(t, val, isS, hasF, row, col);
            l.setFont(font);
            return l;
        }
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
