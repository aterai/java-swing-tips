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
    private static final int MODEL_COLUMN_INDEX = 0;
    private final Object[] columnNames = {Status.INDETERMINATE, "Integer", "String"};
    private final Object[][] data = {
        {true, 1, "BBB"}, {false, 12, "AAA"},
        {true, 2, "DDD"}, {false,  5, "CCC"},
        {true, 3, "EEE"}, {false,  6, "GGG"},
        {true, 4, "FFF"}, {false,  7, "HHH"}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model) {
        @Override public void updateUI() {
            super.updateUI();
            //XXX: Nimbus
            TableCellRenderer r = getDefaultRenderer(Boolean.class);
            if (r instanceof JComponent) {
                ((JComponent) r).updateUI();
            }
        }
        @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
            Component c = super.prepareEditor(editor, row, column);
            if (c instanceof JCheckBox) {
                JCheckBox b = (JCheckBox) c;
                b.setBackground(getSelectionBackground());
                b.setBorderPainted(true);
            }
            return c;
        }
    };

    public MainPanel() {
        super(new BorderLayout());

        TableCellRenderer renderer = new HeaderRenderer(table.getTableHeader(), MODEL_COLUMN_INDEX);
        TableColumn column = table.getColumnModel().getColumn(MODEL_COLUMN_INDEX);
        column.setHeaderRenderer(renderer);
        column.setHeaderValue(Status.INDETERMINATE);
        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu());
        model.addTableModelListener(new HeaderCheckBoxHandler(table, MODEL_COLUMN_INDEX));

        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }

    class AddRowAction extends AbstractAction {
        private final boolean isSelected;
        public AddRowAction(String label, boolean isSelected) {
            super(label);
            this.isSelected = isSelected;
        }
        @Override public void actionPerformed(ActionEvent e) {
            model.addRow(new Object[] {isSelected, 0, ""});
            table.scrollRectToVisible(table.getCellRect(model.getRowCount() - 1, 0, true));
        }
    }

    class DeleteAction extends AbstractAction {
        public DeleteAction(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            int[] selection = table.getSelectedRows();
            if (selection.length == 0) {
                return;
            }
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    }

    private class TablePopupMenu extends JPopupMenu {
        private final Action deleteAction = new DeleteAction("delete");
        public TablePopupMenu() {
            super();
            add(new AddRowAction("add(true)", true));
            add(new AddRowAction("add(false)", false));
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            int[] l = table.getSelectedRows();
            deleteAction.setEnabled(l.length > 0);
            super.show(c, x, y);
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

class HeaderRenderer extends JCheckBox implements TableCellRenderer {
    private final JLabel label = new JLabel("Check All");
    private final int targetColumnIndex;
    public HeaderRenderer(JTableHeader header, int index) {
        super((String) null);
        this.targetColumnIndex = index;
        setOpaque(false);
        setFont(header.getFont());
        header.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JTableHeader header = (JTableHeader) e.getComponent();
                JTable table = header.getTable();
                TableColumnModel columnModel = table.getColumnModel();
                int vci = columnModel.getColumnIndexAtX(e.getX());
                int mci = table.convertColumnIndexToModel(vci);
                if (mci == targetColumnIndex) {
                    TableColumn column = columnModel.getColumn(vci);
                    Object v = column.getHeaderValue();
                    boolean b = Status.DESELECTED.equals(v);
                    TableModel m = table.getModel();
                    for (int i = 0; i < m.getRowCount(); i++) {
                        m.setValueAt(b, i, mci);
                    }
                    column.setHeaderValue(b ? Status.SELECTED : Status.DESELECTED);
                }
            }
        });
    }
    @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
        TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
        JLabel l = (JLabel) r.getTableCellRendererComponent(tbl, val, isS, hasF, row, col);
        if (targetColumnIndex == tbl.convertColumnIndexToModel(col)) {
            if (val instanceof Status) {
                switch((Status) val) {
                  case SELECTED:      setSelected(true);  setEnabled(true);  break;
                  case DESELECTED:    setSelected(false); setEnabled(true);  break;
                  case INDETERMINATE: setSelected(true);  setEnabled(false); break;
                  default:            throw new AssertionError("Unknown Status");
                }
            } else {
                setSelected(true); setEnabled(false);
            }
            label.setIcon(new ComponentIcon(this));
            l.setIcon(new ComponentIcon(label));
            l.setText(null);
        }
        return l;
    }
}

class HeaderCheckBoxHandler implements TableModelListener {
    private final JTable table;
    private final int targetColumnIndex;
    public HeaderCheckBoxHandler(JTable table, int index) {
        this.table = table;
        this.targetColumnIndex = index;
    }
    @Override public void tableChanged(TableModelEvent e) {
        int vci = table.convertColumnIndexToView(targetColumnIndex);
        TableColumn column = table.getColumnModel().getColumn(vci);
        Object status = column.getHeaderValue();
        TableModel m = table.getModel();
        if (e.getType() == TableModelEvent.DELETE) {
            //System.out.println("DELETE");
            //System.out.println(status + ":   " + Status.INDETERMINATE.equals(status));
            if (m.getRowCount() == 0) {
                column.setHeaderValue(Status.DESELECTED);
            } else if (Status.INDETERMINATE.equals(status)) {
                boolean selected = true;
                boolean deselected = true;
                for (int i = 0; i < m.getRowCount(); i++) {
                    Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
                    selected &= b;
                    deselected &= !b;
                }
                //System.out.println(selected);
                //System.out.println(deselected);
                if (deselected) {
                    column.setHeaderValue(Status.DESELECTED);
                } else if (selected) {
                    column.setHeaderValue(Status.SELECTED);
                } else {
                    return;
                }
            }
        } else if (e.getType() == TableModelEvent.INSERT && !Status.INDETERMINATE.equals(status)) {
            //System.out.println("INSERT");
            boolean selected = Status.DESELECTED.equals(status);
            boolean deselected = Status.SELECTED.equals(status);
            for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
                Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
                selected &= b;
                deselected &= !b;
            }
            if (selected && m.getRowCount() == 1) {
                column.setHeaderValue(Status.SELECTED);
            } else if (selected || deselected) {
                column.setHeaderValue(Status.INDETERMINATE);
            }
        } else if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == targetColumnIndex) {
            //System.out.println("UPDATE");
            if (Status.INDETERMINATE.equals(status)) {
                boolean selected = true;
                boolean deselected = true;
                for (int i = 0; i < m.getRowCount(); i++) {
                    Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
                    selected &= b;
                    deselected &= !b;
                    if (selected == deselected) {
                        return;
                    }
                }
                if (deselected) {
                    column.setHeaderValue(Status.DESELECTED);
                } else if (selected) {
                    column.setHeaderValue(Status.SELECTED);
                } else {
                    return;
                }
            } else {
                column.setHeaderValue(Status.INDETERMINATE);
            }
        }
        JTableHeader h = table.getTableHeader();
        h.repaint(h.getHeaderRect(vci));
    }
}

class ComponentIcon implements Icon {
    private final JComponent cmp;
    public ComponentIcon(JComponent cmp) {
        this.cmp = cmp;
    }
    @Override public int getIconWidth() {
        return cmp.getPreferredSize().width;
    }
    @Override public int getIconHeight() {
        return cmp.getPreferredSize().height;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        SwingUtilities.paintComponent(g, cmp, c.getParent(), x, y, getIconWidth(), getIconHeight());
    }
}

enum Status { SELECTED, DESELECTED, INDETERMINATE }
