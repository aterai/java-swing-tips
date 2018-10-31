package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public final class MainPanel extends JPanel {
    private final RowDataModel model = new RowDataModel();
    private final JTable table = new JTable(model);
    public MainPanel() {
        super(new BorderLayout());

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new SortButtonRenderer());
        header.addMouseListener(new HeaderMouseListener());
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        // header.setReorderingAllowed(false);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(80);
        col.setMaxWidth(80);

        model.addRowData(new RowData("Name 1", "comment..."));
        model.addRowData(new RowData("Name 2", "Test"));
        model.addRowData(new RowData("Name d", "ee"));
        model.addRowData(new RowData("Name c", "Test cc"));
        model.addRowData(new RowData("Name b", "Test bb"));
        model.addRowData(new RowData("Name a", "ff"));
        model.addRowData(new RowData("Name 0", "Test aa"));

        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu(table));
        add(new JScrollPane(table));
        add(makeToolBar(), BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private JToolBar makeToolBar() {
        JToolBar tb = new JToolBar("Sort by my order");
        tb.setFloatable(true);
        tb.add(makeToolButton(new UpAction("▲", table)));
        tb.add(makeToolButton(new DownAction("▼", table)));
        tb.add(Box.createHorizontalGlue());
        tb.add(makeToolButton(new InitAction("OK", table)));
        return tb;
    }

    private static JButton makeToolButton(Action action) {
        JButton b = new JButton(action);
        b.setFocusable(false);
        return b;
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

class TablePopupMenu extends JPopupMenu {
    private final Action createAction;
    private final Action deleteAction;
    private final Action upAction;
    private final Action downAction;
    private final JTable table;
    protected TablePopupMenu(JTable table) {
        super();
        this.table = table;

        createAction = new RowDataCreateAction("add", table);
        deleteAction = new DeleteAction("delete", table);
        upAction = new UpAction("up", table);
        downAction = new DownAction("down", table);

        add(createAction);
        addSeparator();
        add(deleteAction);
        addSeparator();
        add(upAction);
        add(downAction);
    }
    @Override public void show(Component c, int x, int y) {
        int row = table.rowAtPoint(new Point(x, y));
        int count = table.getSelectedRowCount();
        // int[] l = table.getSelectedRows();
        // boolean flg = true;
        // for (int i = 0; i < l.length; i++) {
        //     if (l[i] == row) {
        //         flg = false;
        //         break;
        //     }
        // }
        // if (row > 0 && flg) {
        //     table.setRowSelectionInterval(row, row);
        // }
        boolean flg = Arrays.stream(table.getSelectedRows()).anyMatch(i -> i == row);
        if (row > 0 && !flg) {
            table.setRowSelectionInterval(row, row);
        }

        createAction.setEnabled(count <= 1);
        deleteAction.setEnabled(row >= 0);
        upAction.setEnabled(count > 0);
        downAction.setEnabled(count > 0);

        super.show(c, x, y);
    }
}

class RowDataCreateAction extends AbstractAction {
    private final JTable table;
    protected RowDataCreateAction(String str, JTable table) {
        super(str);
        this.table = table;
    }
    @Override public void actionPerformed(ActionEvent e) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        RowDataModel model = (RowDataModel) table.getModel();
        model.addRowData(new RowData("New row", ""));
        Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
        table.scrollRectToVisible(r);
    }
}

class DeleteAction extends AbstractAction {
    private final JTable table;
    protected DeleteAction(String str, JTable table) {
        super(str);
        this.table = table;
    }
    @Override public void actionPerformed(ActionEvent e) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        int[] selection = table.getSelectedRows();
        RowDataModel model = (RowDataModel) table.getModel();
        for (int i = selection.length - 1; i >= 0; i--) {
            // RowData ixsc = model.getRowData(selection[i]);
            model.removeRow(selection[i]);
        }
    }
}

class UpAction extends AbstractAction {
    private final JTable table;
    protected UpAction(String str, JTable table) {
        super(str);
        this.table = table;
    }
    @Override public void actionPerformed(ActionEvent e) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        int[] pos = table.getSelectedRows();
        if (pos.length == 0) {
            return;
        }
        RowDataModel model = (RowDataModel) table.getModel();
        boolean isShiftDown = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
        System.out.println(isShiftDown);
        if (isShiftDown) { // Jump to the top
            model.moveRow(pos[0], pos[pos.length - 1], 0);
            table.setRowSelectionInterval(0, pos.length - 1);
        } else {
            if (pos[0] == 0) {
                return;
            }
            model.moveRow(pos[0], pos[pos.length - 1], pos[0] - 1);
            table.setRowSelectionInterval(pos[0] - 1, pos[pos.length - 1] - 1);
        }
        Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
        table.scrollRectToVisible(r);
    }
}

class DownAction extends AbstractAction {
    private final JTable table;
    protected DownAction(String str, JTable table) {
        super(str);
        this.table = table;
    }
    @Override public void actionPerformed(ActionEvent e) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        int[] pos = table.getSelectedRows();
        if (pos.length == 0) {
            return;
        }
        RowDataModel model = (RowDataModel) table.getModel();
        boolean isShiftDown = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
        if (isShiftDown) { // Jump to the end
            model.moveRow(pos[0], pos[pos.length - 1], model.getRowCount() - pos.length);
            table.setRowSelectionInterval(model.getRowCount() - pos.length, model.getRowCount() - 1);
        } else {
            if (pos[pos.length - 1] == model.getRowCount() - 1) {
                return;
            }
            model.moveRow(pos[0], pos[pos.length - 1], pos[0] + 1);
            table.setRowSelectionInterval(pos[0] + 1, pos[pos.length - 1] + 1);
        }
        Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
        table.scrollRectToVisible(r);
    }
}

class InitAction extends AbstractAction {
    private final JTable table;
    protected InitAction(String str, JTable table) {
        super(str);
        this.table = table;
    }
    @SuppressWarnings("PMD.ReplaceVectorWithList")
    @Override public void actionPerformed(ActionEvent e) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        int row = table.getRowCount();
        if (row <= 0) {
            return;
        }
        RowDataModel model = (RowDataModel) table.getModel();
        RowDataModel nmodel = new RowDataModel();
        Vector<?> dv = model.getDataVector();
        for (int i = 0; i < row; i++) {
            // RowData test = model.getRowData(i);
            Vector<?> v = (Vector<?>) dv.get(i);
            // new RowData((String) v.get(1), (String) v.get(2));
            nmodel.addRowData(new RowData(Objects.toString(v.get(1)), Objects.toString(v.get(2))));
        }
        JTableHeader h = table.getTableHeader();
        TableCellRenderer tcr = h.getDefaultRenderer();
        if (tcr instanceof SortButtonRenderer) {
            SortButtonRenderer sbr = (SortButtonRenderer) tcr;
            sbr.setPressedColumn(-1);
            sbr.setSelectedColumn(-1);
        }
        table.setAutoCreateColumnsFromModel(false);
        table.setModel(nmodel);
        table.clearSelection();
    }
}

class RowDataModel extends SortableTableModel {
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
