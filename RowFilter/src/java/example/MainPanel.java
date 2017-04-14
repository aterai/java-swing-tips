package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final JCheckBox check1 = new JCheckBox("!comment.isEmpty()");
    private final JCheckBox check2 = new JCheckBox("idx % 2 == 0");
    private final TestModel model = new TestModel();
    private final transient TableRowSorter<? extends TestModel> sorter = new TableRowSorter<>(model);
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
        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", "ff"));
        model.addTest(new Test("Name 0", "Test aa"));
        //table.setRowSorter(sorter); <- IndexOutOfBoundsException: Invalid range (add, delete, etc.)

        JScrollPane scrollPane = new JScrollPane(table);
        //scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        table.setComponentPopupMenu(new TablePopupMenu());
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension());
        table.setShowGrid(false);
        //table.setShowHorizontalLines(false);
        //table.setShowVerticalLines(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        Set<RowFilter<? super TestModel, ? super Integer>> filters = new HashSet<>(2);
        RowFilter<TestModel, Integer> filter1 = new RowFilter<TestModel, Integer>() {
            @Override public boolean include(Entry<? extends TestModel, ? extends Integer> entry) {
                TestModel model = entry.getModel();
                Test t = model.getTest(entry.getIdentifier());
                return !t.getComment().trim().isEmpty();
            }
        };
        RowFilter<TestModel, Integer> filter2 = new RowFilter<TestModel, Integer>() {
            @Override public boolean include(Entry<? extends TestModel, ? extends Integer> entry) {
                return entry.getIdentifier() % 2 == 0;
            }
        };
        //sorter.setRowFilter(RowFilter.andFilter(filters));
        //sorter.setRowFilter(filter1);
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
        //return filters.isEmpty();
    }
    private class TablePopupMenu extends JPopupMenu {
        private final Action addAction = new AbstractAction("add") {
            @Override public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) getInvoker();
                TestModel model = (TestModel) table.getModel();
                model.addTest(new Test("example", ""));
            }
        };
        private final Action deleteAction = new AbstractAction("delete") {
            @Override public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) getInvoker();
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                int[] selection = table.getSelectedRows();
                for (int i = selection.length - 1; i >= 0; i--) {
                    model.removeRow(table.convertRowIndexToModel(selection[i]));
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

class TestModel extends DefaultTableModel {
    private static final ColumnContext[] COLUMN_ARRAY = {
        new ColumnContext("No.",     Integer.class, false),
        new ColumnContext("Name",    String.class,  true),
        new ColumnContext("Comment", String.class,  true)
    };
    private int number;
    public void addTest(Test t) {
        Object[] obj = {number, t.getName(), t.getComment()};
        super.addRow(obj);
        number++;
    }
    public Test getTest(int identifier) {
        return new Test((String) getValueAt(identifier, 1), (String) getValueAt(identifier, 2));
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
        public final String  columnName;
        public final Class   columnClass;
        public final boolean isEditable;
        protected ColumnContext(String columnName, Class columnClass, boolean isEditable) {
            this.columnName = columnName;
            this.columnClass = columnClass;
            this.isEditable = isEditable;
        }
    }
}

class Test {
    private String name;
    private String comment;
    protected Test(String name, String comment) {
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
