package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"Integer", "String", "Boolean"};
    private final Object[][] data = {
        {0, "", true}, {1, "", false},
        {2, "", true}, {3, "", false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            switch (column) {
              case 0:
                return Integer.class;
              case 1:
                return String.class;
              case 2:
                return Boolean.class;
              default:
                return super.getColumnClass(column);
            }
        }
    };
    private final JTable table = new JTable(model) {
        @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
            Component c = super.prepareRenderer(tcr, row, column);
            if (isRowSelected(row)) {
                c.setForeground(getSelectionForeground());
                c.setBackground(getSelectionBackground());
            } else if (convertRowIndexToModel(row) == getRowCount() - 1) {
                c.setForeground(Color.WHITE);
                c.setBackground(Color.RED);
            } else {
                c.setForeground(getForeground());
                c.setBackground(getBackground());
            }
            return c;
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu());

        add(new JScrollPane(table));
        add(new JCheckBox(new AbstractAction("DefaultRowSorter#setSortsOnUpdates") {
            @Override public void actionPerformed(ActionEvent e) {
                RowSorter<? extends TableModel> rs = table.getRowSorter();
                if (rs instanceof DefaultRowSorter) {
                    ((DefaultRowSorter<? extends TableModel, ?>) rs).setSortsOnUpdates(((JCheckBox) e.getSource()).isSelected());
                }
            }
        }), BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private class TestCreateAction extends AbstractAction {
        public TestCreateAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            int i = model.getRowCount();
            model.addRow(new Object[] {i, "", i % 2 == 0});
            Rectangle r = table.getCellRect(table.convertRowIndexToView(i - 1), 0, true);
            table.scrollRectToVisible(r);
        }
    }

    private class DeleteAction extends AbstractAction {
        public DeleteAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            int[] selection = table.getSelectedRows();
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    }

    private class TablePopupMenu extends JPopupMenu {
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(new TestCreateAction("add", null));
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
