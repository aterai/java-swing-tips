package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        String[] columnNames = {"String-String/String", "Integer", "Boolean"};
        Object[][] data = {
            {"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", 12, true},
            {"BBB", 2, true}, {"EEE", 3, false},
            {"CCC", 4, true}, {"FFF", 5, false},
            {"DDD", 6, true}, {"GGG", 7, false}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if(c instanceof JComponent) {
                    JComponent l = (JComponent)c;
                    Object o = getValueAt(row, column);
                    Insets i = l.getInsets();
                    Rectangle rect = getCellRect(row, column, false);
                    rect.width -= i.left + i.right;
                    FontMetrics fm = l.getFontMetrics(l.getFont());
                    String str = o.toString();
                    int cellTextWidth = fm.stringWidth(str);
                    l.setToolTipText(cellTextWidth>rect.width?str:null);
                }
                return c;
            }
        };
        table.setAutoCreateRowSorter(true);

        TableCellRenderer r = new ToolTipHeaderRenderer();
        for(int i=0;i<table.getColumnModel().getColumnCount();i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(r);
        }
//         JTableHeader h = table.getTableHeader();
//         h.setDefaultRenderer(new ToolTipHeaderRenderer(h.getDefaultRenderer()));

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

class ToolTipHeaderRenderer implements TableCellRenderer {
    private final Icon icon = UIManager.getIcon("Table.ascendingSortIcon");
    @Override public Component getTableCellRendererComponent(JTable table,Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
        TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
        JLabel l = (JLabel)renderer.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
        Insets i = l.getInsets();
        Rectangle rect = table.getCellRect(row, column, false);
        rect.width -= i.left + i.right;
        RowSorter<? extends TableModel> sorter = table.getRowSorter();
        if(sorter!=null && !sorter.getSortKeys().isEmpty()
                        && sorter.getSortKeys().get(0).getColumn()==column) {
            rect.width -= icon.getIconWidth() + 2; //XXX
        }
        FontMetrics fm = l.getFontMetrics(l.getFont());
        String str = value.toString();
        int cellTextWidth = fm.stringWidth(str);
        l.setToolTipText(cellTextWidth>rect.width?str:null);
        return l;
    }
}
