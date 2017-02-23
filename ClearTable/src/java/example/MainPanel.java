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
            // ArrayIndexOutOfBoundsException: 0 >= 0
            // Bug ID: JDK-6967479 JTable sorter fires even if the model is empty
            // http://bugs.java.com/view_bug.do?bug_id=6967479
            //return getValueAt(0, column).getClass();
            switch (column) {
              case 0:
                return String.class;
              case 1:
                return Number.class;
              case 2:
                return Boolean.class;
              default:
                return super.getColumnClass(column);
            }
        }
    };
    private final JTable table = new JTable(model);
    private final transient RowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
    private static final boolean DEBUG = false;

    public MainPanel() {
        super(new BorderLayout());

        if (DEBUG) {
            //table.setRowSorter(new TableRowSorter<>(model));
            table.setRowSorter(sorter);
        } else {
            table.setAutoCreateRowSorter(true);
        }
        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu());

        add(new JButton(new AbstractAction("remove all rows") {
            @Override public void actionPerformed(ActionEvent e) {
                //model.clear();
                if (DEBUG) {
                    // ArrayIndexOutOfBoundsException: 0 >= 0
                    // Bug ID: JDK-6967479 JTable sorter fires even if the model is empty
                    // http://bugs.java.com/view_bug.do?bug_id=6967479
                    table.setRowSorter(null);
                    table.getTableHeader().repaint();
                }
                model.setRowCount(0);
                //table.setAutoCreateColumnsFromModel(false);
                //table.setModel(new DefaultTableModel());
            }
        }), BorderLayout.SOUTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }

    private class TablePopupMenu extends JPopupMenu {
        private final Action addAction = new AbstractAction("add") {
            @Override public void actionPerformed(ActionEvent e) {
                if (DEBUG && model.getRowCount() == 0) {
                    //table.setRowSorter(new TableRowSorter<>(model));
                    table.setRowSorter(sorter);
                    model.fireTableDataChanged();
                }
                model.addRow(new Object[] {"", model.getRowCount(), false});
                Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
                table.scrollRectToVisible(r);
            }
        };
        private final Action deleteAction = new AbstractAction("delete") {
            @Override public void actionPerformed(ActionEvent e) {
                int[] selection = table.getSelectedRows();
                for (int i = selection.length - 1; i >= 0; i--) {
                    model.removeRow(table.convertRowIndexToModel(selection[i]));
                }
                if (DEBUG && model.getRowCount() == 0) {
                    table.setRowSorter(null);
                    table.getTableHeader().repaint();
                }
            }
        };
        protected TablePopupMenu() {
            super();
            add(addAction);
            //add(new ClearAction("clearSelection"));
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            deleteAction.setEnabled(table.getSelectedRowCount() > 0);
            super.show(c, x, y);
        }
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
