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
    private static final Color EVEN_COLOR = new Color(240, 255, 250);
    private final JCheckBox check1;
    private final JCheckBox check2;
    private final TestModel model = new TestModel();
    private final transient TableRowSorter<? extends TestModel> sorter = new TableRowSorter<>(model);
    private final JTable table = new JTable(model) {
        @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
            Component c = super.prepareRenderer(tcr, row, column);
            if (isRowSelected(row)) {
                c.setForeground(getSelectionForeground());
                c.setBackground(getSelectionBackground());
            } else {
                c.setForeground(getForeground());
                c.setBackground((row % 2 == 0) ? EVEN_COLOR : table.getBackground());
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

        final Set<RowFilter<? super TestModel, ? super Integer>> filters = new HashSet<>(2);
        final RowFilter<TestModel, Integer> filter1 = new RowFilter<TestModel, Integer>() {
            @Override public boolean include(Entry<? extends TestModel, ? extends Integer> entry) {
                TestModel model = entry.getModel();
                Test t = model.getTest(entry.getIdentifier());
                return !t.getComment().trim().isEmpty();
            }
        };
        final RowFilter<TestModel, Integer> filter2 = new RowFilter<TestModel, Integer>() {
            @Override public boolean include(Entry<? extends TestModel, ? extends Integer> entry) {
                return entry.getIdentifier() % 2 == 0;
            }
        };
        //sorter.setRowFilter(RowFilter.andFilter(filters));
        //sorter.setRowFilter(filter1);
        check1 = new JCheckBox(new AbstractAction("!comment.isEmpty()") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                if (cb.isSelected()) {
                    filters.add(filter1);
                } else {
                    filters.remove(filter1);
                }
                sorter.setRowFilter(RowFilter.andFilter(filters));
            }
        });
        check2 = new JCheckBox(new AbstractAction("idx % 2 == 0") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                if (cb.isSelected()) {
                    filters.add(filter2);
                } else {
                    filters.remove(filter2);
                }
                sorter.setRowFilter(RowFilter.andFilter(filters));
            }
        });
        Box box = Box.createHorizontalBox();
        box.add(check1);
        box.add(check2);
        add(box, BorderLayout.NORTH);
        add(scrollPane);
        setPreferredSize(new Dimension(320, 240));
    }
    private class TablePopupMenu extends JPopupMenu {
        private final Action addAction;
        private final Action deleteAction;
        public TablePopupMenu() {
            super();
            addAction = new TestCreateAction("add", table);
            deleteAction = new DeleteAction("delete", table);
            add(addAction);
            //add(new ClearAction("clearSelection", null));
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            addAction.setEnabled(!check1.isSelected() && !check2.isSelected());
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
    @Override public Class<?> getColumnClass(int modelIndex) {
        return COLUMN_ARRAY[modelIndex].columnClass;
    }
    @Override public int getColumnCount() {
        return COLUMN_ARRAY.length;
    }
    @Override public String getColumnName(int modelIndex) {
        return COLUMN_ARRAY[modelIndex].columnName;
    }
    private static class ColumnContext {
        public final String  columnName;
        public final Class   columnClass;
        public final boolean isEditable;
        public ColumnContext(String columnName, Class columnClass, boolean isEditable) {
            this.columnName = columnName;
            this.columnClass = columnClass;
            this.isEditable = isEditable;
        }
    }
}

class Test {
    private String name, comment;
    public Test(String name, String comment) {
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

class TestCreateAction extends AbstractAction {
    private final JTable table;
    public TestCreateAction(String label, JTable table) {
        super(label);
        this.table = table;
    }
    @Override public void actionPerformed(ActionEvent e) {
        TestModel model = (TestModel) table.getModel();
        model.addTest(new Test("example", ""));
    }
}

class DeleteAction extends AbstractAction {
    private final JTable table;
    public DeleteAction(String label, JTable table) {
        super(label);
        this.table = table;
    }
    @Override public void actionPerformed(ActionEvent e) {
        int[] selection = table.getSelectedRows();
        if (selection.length == 0) {
            return;
        }
        for (int i = selection.length - 1; i >= 0; i--) {
            TestModel model = (TestModel) table.getModel();
            model.removeRow(table.convertRowIndexToModel(selection[i]));
        }
    }
}
