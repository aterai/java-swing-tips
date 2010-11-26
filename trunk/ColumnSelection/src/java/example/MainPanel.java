package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox("Header click: Select all cells in a column", true);
    private final TestModel model = new TestModel();
    private final JTable table;
    public MainPanel() {
        super(new BorderLayout());
        model.addTest(new Test("Name 1", "comment"));
        model.addTest(new Test("Name 2", "test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "test cc"));
        model.addTest(new Test("Name b", "test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "test aa"));
        model.addTest(new Test("Name 0", ""));

        table = new JTable(model) {
            private final Color evenColor = new Color(250, 250, 250);
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if(isCellSelected(row, column)) {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                }else{
                    c.setForeground(getForeground());
                    c.setBackground((row%2==0)?evenColor:getBackground());
                }
                return c;
            }
        };
        table.setCellSelectionEnabled(true);

        final JTableHeader header = table.getTableHeader();
        header.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if(!check.isSelected()) return;
                if(table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
                int col = header.columnAtPoint(e.getPoint());
                table.changeSelection(0, col, false, false);
                table.changeSelection(table.getRowCount()-1, col, false, true);
            }
        });

//         table.getTableHeader().addMouseListener(new MouseAdapter() {
//             public void mousePressed(MouseEvent e) {
//                 JTable table = ((JTableHeader)e.getSource()).getTable();
//                 if(table.isEditing()) {
//                     table.getCellEditor().stopCellEditing();
//                 }
//                 if(check.isSelected()) {
//                     //table.getSelectionModel().clearSelection();
//                     //table.getSelectionModel().setAnchorSelectionIndex(-1);
//                     //table.getSelectionModel().setLeadSelectionIndex(-1);
//                     table.getColumnModel().getSelectionModel().setAnchorSelectionIndex(-1);
//                     table.getColumnModel().getSelectionModel().setLeadSelectionIndex(-1);
//                 }
//             }
//         });

        add(check, BorderLayout.NORTH);
        add(new JScrollPane(table));
        add(new JButton(new AbstractAction("clear selection") {
            @Override public void actionPerformed(ActionEvent e) {
                table.clearSelection();
            }
        }), BorderLayout.SOUTH);
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
