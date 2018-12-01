package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
    private final JCheckBox check1 = new JCheckBox("!comment.isEmpty()");
    private final JCheckBox check2 = new JCheckBox("idx % 2 == 0");
    private final RowDataModel model = new RowDataModel();
    private final transient TableRowSorter<? extends RowDataModel> sorter = new TableRowSorter<>(model);
    private final JTable table = new JTable(model) {
        private final Color evenColor = new Color(240, 255, 250);
        @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
            Component c = super.prepareRenderer(tcr, row, column);
            if (isRowSelected(row)) {
                c.setForeground(getSelectionForeground());
                c.setBackground(getSelectionBackground());
            } else {
                c.setForeground(getForeground());
                c.setBackground(row % 2 == 0 ? evenColor : getBackground());
            }
            return c;
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        table.setRowSorter(sorter);
        model.addRowData(new RowData("Name 1", "comment..."));
        model.addRowData(new RowData("Name 2", "Test"));
        model.addRowData(new RowData("Name d", ""));
        model.addRowData(new RowData("Name c", "Test cc"));
        model.addRowData(new RowData("Name b", "Test bb"));
        model.addRowData(new RowData("Name a", "ff"));
        model.addRowData(new RowData("Name 0", "Test aa"));
        // table.setRowSorter(sorter); <- IndexOutOfBoundsException: Invalid range (add, delete, etc.)

        JScrollPane scrollPane = new JScrollPane(table);
        // scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        table.setComponentPopupMenu(new TablePopupMenu());
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension());
        table.setShowGrid(false);
        // table.setShowHorizontalLines(false);
        // table.setShowVerticalLines(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        Set<RowFilter<? super RowDataModel, ? super Integer>> filters = new HashSet<>(2);
        RowFilter<RowDataModel, Integer> filter1 = new RowFilter<RowDataModel, Integer>() {
            @Override public boolean include(Entry<? extends RowDataModel, ? extends Integer> entry) {
                RowDataModel m = entry.getModel();
                RowData rd = m.getRowData(entry.getIdentifier());
                return !rd.getComment().trim().isEmpty();
            }
        };
        RowFilter<RowDataModel, Integer> filter2 = new RowFilter<RowDataModel, Integer>() {
            @Override public boolean include(Entry<? extends RowDataModel, ? extends Integer> entry) {
                return entry.getIdentifier() % 2 == 0;
            }
        };
        // sorter.setRowFilter(RowFilter.andFilter(filters));
        // sorter.setRowFilter(filter1);
        check1.addActionListener(e -> {
            JCheckBox cb = (JCheckBox) e.getSource();
            if (cb.isSelected()) {
                filters.add(filter1);
            } else {
                filters.remove(filter1);
            }
            sorter.setRowFilter(RowFilter.andFilter(filters));
        });
        check2.addActionListener(e -> {
            JCheckBox cb = (JCheckBox) e.getSource();
            if (cb.isSelected()) {
                filters.add(filter2);
            } else {
                filters.remove(filter2);
            }
            sorter.setRowFilter(RowFilter.andFilter(filters));
        });
        Box box = Box.createHorizontalBox();
        box.add(check1);
        box.add(check2);
        add(box, BorderLayout.NORTH);
        add(scrollPane);
        setPreferredSize(new Dimension(320, 240));
    }
    protected boolean canAddRow() {
        return !check1.isSelected() && !check2.isSelected();
        // return filters.isEmpty();
    }
    private class TablePopupMenu extends JPopupMenu {
        private final Action addAction = new AbstractAction("add") {
            @Override public void actionPerformed(ActionEvent e) {
                JTable tbl = (JTable) getInvoker();
                RowDataModel m = (RowDataModel) tbl.getModel();
                m.addRowData(new RowData("example", ""));
            }
        };
        private final Action deleteAction = new AbstractAction("delete") {
            @Override public void actionPerformed(ActionEvent e) {
                JTable tbl = (JTable) getInvoker();
                DefaultTableModel m = (DefaultTableModel) tbl.getModel();
                int[] selection = tbl.getSelectedRows();
                for (int i = selection.length - 1; i >= 0; i--) {
                    m.removeRow(tbl.convertRowIndexToModel(selection[i]));
                }
            }
        };
        protected TablePopupMenu() {
            super();
            add(addAction);
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            if (c instanceof JTable) {
                addAction.setEnabled(canAddRow());
                deleteAction.setEnabled(((JTable) c).getSelectedRowCount() > 0);
                super.show(c, x, y);
            }
        }
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

class RowDataModel extends DefaultTableModel {
    private static final ColumnContext[] COLUMN_ARRAY = {
        new ColumnContext("No.", Integer.class, false),
        new ColumnContext("Name", String.class, true),
        new ColumnContext("Comment", String.class, true)
    };
    private int number;
    public void addRowData(RowData t) {
        Object[] obj = {number, t.getName(), t.getComment()};
        super.addRow(obj);
        number++;
    }
    public RowData getRowData(int identifier) {
        return new RowData(Objects.toString(getValueAt(identifier, 1)), Objects.toString(getValueAt(identifier, 2)));
    }
    @Override public boolean isCellEditable(int row, int col) {
        return COLUMN_ARRAY[col].isEditable;
    }
    @Override public Class<?> getColumnClass(int column) {
        return COLUMN_ARRAY[column].columnClass;
    }
    @Override public int getColumnCount() {
        return COLUMN_ARRAY.length;
    }
    @Override public String getColumnName(int column) {
        return COLUMN_ARRAY[column].columnName;
    }
    private static class ColumnContext {
        public final String columnName;
        public final Class<?> columnClass;
        public final boolean isEditable;
        protected ColumnContext(String columnName, Class<?> columnClass, boolean isEditable) {
            this.columnName = columnName;
            this.columnClass = columnClass;
            this.isEditable = isEditable;
        }
    }
}

class RowData {
    private String name;
    private String comment;
    protected RowData(String name, String comment) {
        this.name = name;
        this.comment = comment;
    }
    public void setName(String str) {
        name = str;
    }
    public void setComment(String str) {
        comment = str;
    }
    public String getName() {
        return name;
    }
    public String getComment() {
        return comment;
    }
}
