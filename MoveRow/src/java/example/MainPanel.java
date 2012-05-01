package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
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
        col.setMinWidth(50);
        col.setMaxWidth(50);

        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));

        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu());
        add(new JScrollPane(table));
        add(makeToolBar(), BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private static JToolBar makeToolBar() {
        JToolBar tb = new JToolBar("Sort by my order");
        tb.setFloatable(true);
        tb.add(initButton(new UpAction("\u25B2")));
        tb.add(initButton(new DownAction("\u25BC")));
        tb.add(Box.createHorizontalGlue());
        tb.add(initButton(new InitAction("OK")));
        return tb;
    }

    private static JButton initButton(Action action) {
        JButton b = new JButton(action);
        b.setFocusable(false);
        return b;
    }

    private class TablePopupMenu extends JPopupMenu {
        private final Action createAction = new TestCreateAction("add", null);
        private final Action deleteAction = new DeleteAction("delete", null);
        private final Action upAction     = new UpAction("up");
        private final Action downAction   = new DownAction("down");
        public TablePopupMenu() {
            super();
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
            if(row>0 && flg) table.setRowSelectionInterval(row, row);

            createAction.setEnabled(count<=1);
            deleteAction.setEnabled(row>=0);
            upAction.setEnabled(count>0);
            downAction.setEnabled(count>0);

            super.show(c, x, y);
        }
    }

    class TestCreateAction extends AbstractAction{
        public TestCreateAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            if(table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            testCreateActionPerformed(evt);
        }
    }
    private void testCreateActionPerformed(ActionEvent e) {
        model.addTest(new Test("New row", ""));
        Rectangle r = table.getCellRect(model.getRowCount()-1, 0, true);
        table.scrollRectToVisible(r);
    }

    class DeleteAction extends AbstractAction{
        public DeleteAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            if(table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            deleteActionPerformed(evt);
        }
    }
    private void deleteActionPerformed(ActionEvent evt) {
        int[] selection = table.getSelectedRows();
        if(selection==null || selection.length<=0) return;
        for(int i=selection.length-1;i>=0;i--) {
            //Test ixsc = model.getTest(selection[i]);
            model.removeRow(selection[i]);
        }
    }
    class UpAction extends AbstractAction{
        public UpAction(String str) {
            super(str);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            if(table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            upActionPerformed(evt);
        }
    }
    private void upActionPerformed(ActionEvent e) {
        int[] pos = table.getSelectedRows();
        if(pos==null || pos.length<=0) return;
        DefaultTableModel mdl = (DefaultTableModel) table.getModel();
        if((e.getModifiers() & ActionEvent.SHIFT_MASK)!=0) {
            mdl.moveRow(pos[0], pos[pos.length-1], 0);
            table.setRowSelectionInterval(0, pos.length-1);
        }else{
            if(pos[0]==0) return;
            mdl.moveRow(pos[0], pos[pos.length-1], pos[0]-1);
            table.setRowSelectionInterval(pos[0]-1, pos[pos.length-1]-1);
        }
        scrollSelectedRow();
    }
    class DownAction extends AbstractAction{
        public DownAction(String str) {
            super(str);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            if(table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            downActionPerformed(evt);
        }
    }
    private void downActionPerformed(ActionEvent e) {
        int[] pos = table.getSelectedRows();
        if(pos==null || pos.length<=0) return;
        DefaultTableModel mdl = (DefaultTableModel) table.getModel();
        if((e.getModifiers() & ActionEvent.SHIFT_MASK)!=0) {
            mdl.moveRow(pos[0], pos[pos.length-1], mdl.getRowCount()-pos.length);
            table.setRowSelectionInterval(mdl.getRowCount()-pos.length, mdl.getRowCount()-1);
        }else{
            if(pos[pos.length-1]==mdl.getRowCount()-1) return;
            mdl.moveRow(pos[0], pos[pos.length-1], pos[0]+1);
            table.setRowSelectionInterval(pos[0]+1, pos[pos.length-1]+1);
        }
        scrollSelectedRow();
    }
    private void scrollSelectedRow() {
        Rectangle rect = table.getCellRect(table.getSelectedRow(), 0, false);
        if(rect!=null) {
            table.scrollRectToVisible(rect);
        }
    }
    class InitAction extends AbstractAction{
        public InitAction(String str) {
            super(str);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            if(table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            initActionPerformed(evt);
        }
    }
    private void initActionPerformed(ActionEvent e) {
        int row = table.getRowCount();
        if(row<=0) return;
        TestModel model = (TestModel)table.getModel();
        TestModel nmodel = new TestModel();
        Vector dv = model.getDataVector();
        for(int i=0;i<row;i++) {
            //Test test = model.getTest(i);
            Vector v = (Vector)dv.elementAt(i);
            //new Test((String)v.elementAt(1), (String)v.elementAt(2));
            nmodel.addTest(new Test((String)v.elementAt(1), (String)v.elementAt(2)));
        }
        table.setModel(nmodel);
        TableColumn col = table.getColumnModel().getColumn(0);
        col.setPreferredWidth(50);
        col.setMinWidth(50);
        col.setMaxWidth(50);
        table.clearSelection();
        revalidate();
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
