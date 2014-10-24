package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String[] columnNames = {"AAA", "BBB", "CCC"};
        Object[][] data = {
            {"aaa", "eee", "fff"}, {"bbb", "lll", "kk"},
            {"CCC", "g", "hh"}, {"DDD", "iiii", "j"}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };
        JTable table = new JTable(model);
        //table.setAutoCreateColumnsFromModel(true);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setComponentPopupMenu(new TablePopupMenu(columnNames));
        //table.getTableHeader().setReorderingAllowed(false);
        //table.getTableHeader().setDraggedDistance(4);

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

class TablePopupMenu extends JPopupMenu {
    private final String[] columnNames;
    private final JTextField textField = new JTextField();
    private final JMenuItem editItem1 = new JMenuItem(new AbstractAction("Edit: setHeaderValue") {
        @Override public void actionPerformed(ActionEvent e) {
            JTableHeader header = (JTableHeader) getInvoker();
            TableColumn column = header.getColumnModel().getColumn(index);
            String name = column.getHeaderValue().toString();
            textField.setText(name);
            int result = JOptionPane.showConfirmDialog(header.getTable(), textField, getValue(NAME).toString(),
                                                       JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String str = textField.getText().trim();
                if (!str.equals(name)) {
                    column.setHeaderValue(str);
                    header.repaint(header.getHeaderRect(index));
                }
            }
        }
    });
    private final JMenuItem editItem2 = new JMenuItem(new AbstractAction("Edit: setColumnIdentifiers") {
        @Override public void actionPerformed(ActionEvent e) {
            final JTableHeader header = (JTableHeader) getInvoker();
            final JTable table = header.getTable();
            final DefaultTableModel model = (DefaultTableModel) table.getModel();
            String name = table.getColumnName(index);
            textField.setText(name);
            int result = JOptionPane.showConfirmDialog(table, textField, getValue(NAME).toString(),
                                                       JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String str = textField.getText().trim();
                if (!str.equals(name)) {
                    columnNames[table.convertColumnIndexToModel(index)] = str;
                    // Bug?: if (header.getDraggedColumn() != null): ArrayIndexOutOfBoundsException: -1
                    // @see bookmark_1: header.setDraggedColumn(null);
                    model.setColumnIdentifiers(columnNames);
                }
            }
        }
    });

    private int index = -1;
    public TablePopupMenu(String... arrays) {
        super();
        columnNames = new String[arrays.length];
        System.arraycopy(arrays, 0, columnNames, 0, arrays.length);
//         for (int i = 0; i < arrays.length; i++) {
//             columnNames[i] = arrays[i];
//         }
        textField.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent e) {
                textField.requestFocusInWindow();
            }
            @Override public void ancestorMoved(AncestorEvent e)   { /* not needed */ }
            @Override public void ancestorRemoved(AncestorEvent e) { /* not needed */ }
        });
        add(editItem1);
        add(editItem2);
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTableHeader) {
            JTableHeader header = (JTableHeader) c;
            header.setDraggedColumn(null); // bookmark_1
            //if (header.getDraggedColumn() != null) remain dirty area >>>
            header.repaint();
            header.getTable().repaint();
            //<<<
            index = header.columnAtPoint(new Point(x, y));
            super.show(c, x, y);
        }
    }
}
