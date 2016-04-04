package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final int BUTTON_COLUMN = 3;
    private final TestModel model = new TestModel();
//     private final JTable table = new JTable(model) {
//         @Override public int rowAtPoint(Point pt) {
//             // Bug ID: 6291631 JTable: rowAtPoint returns 0 for negative y
//             // http://bugs.java.com/view_bug.do?bug_id=6291631
//             return pt.y < 0 ? -1 : super.rowAtPoint(pt);
//         }
//     };
    private final JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());
        TableRowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortable(BUTTON_COLUMN, false);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        model.addTest(new Test("Name 1", "Comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", "ee"));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", "ff"));
        model.addTest(new Test("Name 0", "Test aa"));
        model.addTest(new Test("Name 0", "gg"));

//         table.addMouseListener(new MouseAdapter() {
//             private int targetRow = -1;
//             @Override public void mousePressed(MouseEvent e) {
//                 Point pt = e.getPoint();
//                 int mcol = table.convertColumnIndexToModel(table.columnAtPoint(pt));
//                 int vrow = table.rowAtPoint(e.getPoint());
//                 int mrow = vrow >= 0 ? table.convertRowIndexToModel(vrow) : -1;
//                 if (mrow >= 0 && mcol == BUTTON_COLUMN) {
//                     targetRow = mrow;
//                 }
//             }
//             @Override public void mouseReleased(MouseEvent e) {
//                 Point pt = e.getPoint();
//                 int mcol = table.convertColumnIndexToModel(table.columnAtPoint(pt));
//                 int vrow = table.rowAtPoint(e.getPoint());
//                 int mrow = vrow >= 0 ? table.convertRowIndexToModel(vrow) : -1;
//                 if (targetRow == mrow && mcol == BUTTON_COLUMN) {
//                     model.removeRow(mrow);
//                 }
//                 targetRow = -1;
//             }
//         });
//         ButtonColumn buttonColumn = new ButtonColumn();

        //ButtonColumn buttonColumn = new ButtonColumn(table);
        TableColumn column = table.getColumnModel().getColumn(BUTTON_COLUMN);
        column.setCellRenderer(new DeleteButtonRenderer());
        column.setCellEditor(new DeleteButtonEditor());
        column.setMinWidth(20);
        column.setMaxWidth(20);
        column.setResizable(false);

        add(new JButton(new AbstractAction("add") {
            @Override public void actionPerformed(ActionEvent e) {
                model.addTest(new Test("Test", "aaaaaaaaaaa"));
            }
        }), BorderLayout.SOUTH);
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

// https://community.oracle.com/thread/1357722 JButton inside JTable Cell
class DeleteButton extends JButton {
    @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder());
        setFocusable(false);
        setRolloverEnabled(false);
        setText("X");
    }
}

class DeleteButtonRenderer extends DeleteButton implements TableCellRenderer {
    @Override public void updateUI() {
        super.updateUI();
        setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
}

class DeleteButtonEditor extends DeleteButton implements TableCellEditor {
    private transient ActionListener listener;
    @Override public void updateUI() {
        removeActionListener(listener);
        super.updateUI();
        listener = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                Object o = SwingUtilities.getAncestorOfClass(JTable.class, DeleteButtonEditor.this);
                if (o instanceof JTable) {
                    JTable table = (JTable) o;
                    int row = table.convertRowIndexToModel(table.getEditingRow());
                    fireEditingStopped();
                    ((DefaultTableModel) table.getModel()).removeRow(row);
                }
            }
        };
        addActionListener(listener);
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return this;
    }
    @Override public Object getCellEditorValue() {
        return "";
    }

    //Copied from AbstractCellEditor
    //protected EventListenerList listenerList = new EventListenerList();
    //protected transient ChangeEvent changeEvent;

    @Override public boolean isCellEditable(EventObject e) {
        return true;
    }
    @Override public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
    @Override public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }
    @Override public void cancelCellEditing() {
        fireEditingCanceled();
    }
    @Override public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }
    @Override public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }
    public CellEditorListener[] getCellEditorListeners() {
        return listenerList.getListeners(CellEditorListener.class);
    }
    protected void fireEditingStopped() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
            }
        }
    }
    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
            }
        }
    }
}

class TestModel extends DefaultTableModel {
    private static final ColumnContext[] COLUMN_ARRAY = {
        new ColumnContext("No.",     Integer.class, false),
        new ColumnContext("Name",    String.class,  true),
        new ColumnContext("Comment", String.class,  true),
        new ColumnContext("",        String.class,  true)
    };
    private int number;
    public void addTest(Test t) {
        Object[] obj = {number, t.getName(), t.getComment(), ""};
        super.addRow(obj);
        number++;
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
