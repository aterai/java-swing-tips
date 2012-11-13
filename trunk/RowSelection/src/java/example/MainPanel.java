package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final JPanel infoPanel = new JPanel();
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
    private final JTable table = new JTable(model);
    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
        //table.setRowSorter(new TableRowSorter<TableModel>(model));

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting()) return;
                int sc = table.getSelectedRowCount();
                changeInfoPanel((sc==1)?getInfo():" ");
            }
        });
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        add(new JScrollPane(table));
        add(infoPanel, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private String getInfo() {
        int index = table.convertRowIndexToModel(table.getSelectedRow());
        String  str = (String)model.getValueAt(index, 0);
        Integer idx = (Integer)model.getValueAt(index, 1);
        Boolean flg = (Boolean)model.getValueAt(index, 2);
        return String.format("%s, %d, %s", str, idx, flg);
    }
    private void changeInfoPanel(String str) {
        JLabel label = new JLabel(str);
        infoPanel.setVisible(false);
        infoPanel.removeAll();
        infoPanel.add(label);
        infoPanel.setVisible(true);
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
