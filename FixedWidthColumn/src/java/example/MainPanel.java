package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);

    private MainPanel() {
        super(new BorderLayout());
        JTableHeader tableHeader = table.getTableHeader();
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        tableHeader.setReorderingAllowed(false);

        // // User can't resize columns by dragging between headers.
        // tableHeader.setResizingAllowed(false);

        // // Disable resizing of the column width(JTable.AUTO_RESIZE_OFF).
        // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // col.setPreferredWidth(50);
        // col.setResizable(false);

        // // Disable resizing of the column width.
        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);

        // // Deletes the column from the tableColumns array.
        // table.removeColumn(col);

        // // // XXX: focus traversal
        // col = table.getColumnModel().getColumn(1);
        // col.setMinWidth(0);
        // col.setMaxWidth(0);
        // // <blockquote cite="https://community.oracle.com/thread/1484284"
        // //            title="JTable skiping the cells disableds">
        // InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        // KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        // Action oldTabAction = table.getActionMap().get(im.get(tab));
        // Action tabAction = new AbstractAction() {
        //     @Override public void actionPerformed(ActionEvent e) {
        //         oldTabAction.actionPerformed(e);
        //         JTable table = (JTable) e.getSource();
        //         int rowCount = table.getRowCount();
        //         int columnCount = table.getColumnCount();
        //         int row = table.getSelectedRow();
        //         int column = table.getSelectedColumn();
        //
        //         TableColumn col = table.getColumnModel().getColumn(column);
        //         while (col.getWidth() == 0) {
        //             column += 1;
        //             if (column == columnCount) {
        //                 column = 0;
        //                 row +=1;
        //             }
        //             if (row == rowCount) {
        //                 row = 0;
        //             }
        //             if (row == table.getSelectedRow() && column == table.getSelectedColumn()) {
        //                 break;
        //             }
        //             col = table.getColumnModel().getColumn(column);
        //         }
        //         table.changeSelection(row, column, false, false);
        //     }
        // };
        // table.getActionMap().put(im.get(tab), tabAction);
        // // </blockquote>

        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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
