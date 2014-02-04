package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final TestModel model = new TestModel();
    private final JTable table = new JTable(model);
    public MainPanel() {
        super(new BorderLayout());

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new SortButtonRenderer());
        header.addMouseListener(new HeaderMouseListener());
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        //header.setReorderingAllowed(false);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(80);
        col.setMaxWidth(80);

        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", "ee"));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", "ff"));
        model.addTest(new Test("Name 0", "Test aa"));

        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu(table));
        add(new JScrollPane(table));
        add(makeToolBar(), BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private JToolBar makeToolBar() {
        JToolBar tb = new JToolBar("Sort by my order");
        tb.setFloatable(true);
        tb.add(initButton(new UpAction("\u25B2", table)));
        tb.add(initButton(new DownAction("\u25BC", table)));
        tb.add(Box.createHorizontalGlue());
        tb.add(initButton(new InitAction("OK", table)));
        return tb;
    }

    private static JButton initButton(Action action) {
        JButton b = new JButton(action);
        b.setFocusable(false);
        return b;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
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

class TablePopupMenu extends JPopupMenu {
    private final Action createAction;
    private final Action deleteAction;
    private final Action upAction;
    private final Action downAction;
    private final JTable table;
    public TablePopupMenu(JTable table) {
        super();
        this.table = table;

        createAction = new TestCreateAction("add", table);
        deleteAction = new DeleteAction("delete", table);
        upAction     = new UpAction("up", table);
        downAction   = new DownAction("down", table);

        add(createAction);
        addSeparator();
        add(deleteAction);
        addSeparator();
        add(upAction);
        add(downAction);
    }
    @Override public void show(Component c, int x, int y) {
        int row     = table.rowAtPoint(new Point(x, y));
        int count   = table.getSelectedRowCount();
        int[] l     = table.getSelectedRows();
        boolean flg = true;
        for(int i=0;i<l.length;i++) {
            if(l[i]==row) {
                flg = false;
                break;
            }
        }
        if(row>0 && flg) {
            table.setRowSelectionInterval(row, row);
        }

        createAction.setEnabled(count<=1);
        deleteAction.setEnabled(row>=0);
        upAction.setEnabled(count>0);
        downAction.setEnabled(count>0);

        super.show(c, x, y);
    }
}

class TestCreateAction extends AbstractAction {
    private final JTable table;
    public TestCreateAction(String str, JTable table) {
        super(str);
        this.table = table;
    }
    @Override public void actionPerformed(ActionEvent e) {
        if(table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        TestModel model = (TestModel)table.getModel();
        model.addTest(new Test("New row", ""));
        Rectangle r = table.getCellRect(model.getRowCount()-1, 0, true);
        table.scrollRectToVisible(r);
    }
}

class DeleteAction extends AbstractAction {
    private final JTable table;
    public DeleteAction(String str, JTable table) {
        super(str);
        this.table = table;
    }
    @Override public void actionPerformed(ActionEvent e) {
        if(table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        int[] selection = table.getSelectedRows();
        if(selection.length == 0) {
            return;
        }
        TestModel model = (TestModel)table.getModel();
        for(int i=selection.length-1;i>=0;i--) {
            //Test ixsc = model.getTest(selection[i]);
            model.removeRow(selection[i]);
        }
    }
}

class UpAction extends AbstractAction {
    private final JTable table;
    public UpAction(String str, JTable table) {
        super(str);
        this.table = table;
    }
    @Override public void actionPerformed(ActionEvent e) {
        if(table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        int[] pos = table.getSelectedRows();
        if(pos.length == 0) {
            return;
        }
        TestModel model = (TestModel)table.getModel();
        if((e.getModifiers() & ActionEvent.SHIFT_MASK) == 0) {
            if(pos[0]==0) {
                return;
            }
            model.moveRow(pos[0], pos[pos.length-1], pos[0]-1);
            table.setRowSelectionInterval(pos[0]-1, pos[pos.length-1]-1);
        }else{
            model.moveRow(pos[0], pos[pos.length-1], 0);
            table.setRowSelectionInterval(0, pos.length-1);
        }
        Rectangle r = table.getCellRect(model.getRowCount()-1, 0, true);
        table.scrollRectToVisible(r);
    }
}

class DownAction extends AbstractAction {
    private final JTable table;
    public DownAction(String str, JTable table) {
        super(str);
        this.table = table;
    }
    @Override public void actionPerformed(ActionEvent e) {
        if(table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        int[] pos = table.getSelectedRows();
        if(pos.length == 0) {
            return;
        }
        TestModel model = (TestModel)table.getModel();
        if((e.getModifiers() & ActionEvent.SHIFT_MASK) == 0) {
            if(pos[pos.length-1]==model.getRowCount()-1) {
                return;
            }
            model.moveRow(pos[0], pos[pos.length-1], pos[0]+1);
            table.setRowSelectionInterval(pos[0]+1, pos[pos.length-1]+1);
        }else{
            model.moveRow(pos[0], pos[pos.length-1], model.getRowCount()-pos.length);
            table.setRowSelectionInterval(model.getRowCount()-pos.length, model.getRowCount()-1);
        }
        Rectangle r = table.getCellRect(model.getRowCount()-1, 0, true);
        table.scrollRectToVisible(r);
    }
}

class InitAction extends AbstractAction {
    private final JTable table;
    public InitAction(String str, JTable table) {
        super(str);
        this.table = table;
    }
    @Override public void actionPerformed(ActionEvent e) {
        if(table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        int row = table.getRowCount();
        if(row<=0) {
            return;
        }
        TestModel model = (TestModel)table.getModel();
        TestModel nmodel = new TestModel();
        Vector dv = model.getDataVector();
        for(int i=0;i<row;i++) {
            //Test test = model.getTest(i);
            Vector v = (Vector)dv.elementAt(i);
            //new Test((String)v.elementAt(1), (String)v.elementAt(2));
            nmodel.addTest(new Test((String)v.elementAt(1), (String)v.elementAt(2)));
        }
        JTableHeader h = table.getTableHeader();
        TableCellRenderer tcr = h.getDefaultRenderer();
        if(tcr instanceof SortButtonRenderer) {
            SortButtonRenderer sbr = (SortButtonRenderer)tcr;
            sbr.setPressedColumn(-1);
            sbr.setSelectedColumn(-1);
        }
        table.setAutoCreateColumnsFromModel(false);
        table.setModel(nmodel);
        table.clearSelection();
    }
}

class TestModel extends SortableTableModel {
    private final ColumnContext[] columnArray = {
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
    @Override public boolean isCellEditable(int row, int col) {
        return columnArray[col].isEditable;
    }
    @Override public Class<?> getColumnClass(int modelIndex) {
        return columnArray[modelIndex].columnClass;
    }
    @Override public int getColumnCount() {
        return columnArray.length;
    }
    @Override public String getColumnName(int modelIndex) {
        return columnArray[modelIndex].columnName;
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
