package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private static final Color evenColor = new Color(250, 250, 250);
    private final TestModel model = new TestModel();
    private final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
    private final JTable table;
    private final Executor executor = Executors.newCachedThreadPool();
    //TEST: private final Executor executor = Executors.newFixedThreadPool(2);
    public MainPanel() {
        super(new BorderLayout());
        table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if(isRowSelected(row)) {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                }else{
                    c.setForeground(getForeground());
                    c.setBackground((row%2==0)?evenColor:getBackground());
                }
                return c;
            }
        };
        table.setRowSorter(sorter);
        model.addTest(new Test("Name 1", 100), null);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        table.setComponentPopupMenu(new TablePopupMenu());
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension());
        table.setShowGrid(false);
        //table.setShowHorizontalLines(false);
        //table.setShowVerticalLines(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMaxWidth(60);
        column.setMinWidth(60);
        column.setResizable(false);
        column = table.getColumnModel().getColumn(2);
        column.setCellRenderer(new ProgressRenderer());

        add(new JButton(new TestCreateAction("add", null)), BorderLayout.SOUTH);
        add(scrollPane);
        setPreferredSize(new Dimension(320, 240));
    }

    class TestCreateAction extends AbstractAction{
        public TestCreateAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            testCreateActionPerformed(evt);
        }
    }
    private void testCreateActionPerformed(ActionEvent e) {
        final int key = model.getRowCount();
        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>() {
            private int sleepDummy = new Random().nextInt(100) + 1;
            private int lengthOfTask = 120;
            @Override protected Integer doInBackground() {
                int current = 0;
                while(current<lengthOfTask && !isCancelled()) {
                    if(!table.isDisplayable()) {
                        return -1;
                    }
                    current++;
                    try{
                        Thread.sleep(sleepDummy);
                    }catch(InterruptedException ie) {
                        //cancel(true);
                        break;
                    }
                    publish(100 * current / lengthOfTask);
                }
                return sleepDummy*lengthOfTask;
            }
            @Override protected void process(java.util.List<Integer> c) {
                model.setValueAt(c.get(c.size()-1), key, 2);
                //for(Integer value : chunks) {
                //    model.setValueAt(value, key, 2);
                //}
                //model.fireTableCellUpdated(key, 2);
                //table.repaint();
            }
            @Override protected void done() {
                String text;
                int i = -1;
                if(isCancelled()) {
                    text = "Cancelled";
                }else{
                    try{
                        i = get();
                        text = (i>=0)?"Done":"Disposed";
                    }catch(Exception ignore) {
                        ignore.printStackTrace();
                        text = ignore.getMessage();
                    }
                }
                System.out.println(key +":"+text+"("+i+"ms)");
            }
        };
        model.addTest(new Test("example", 0), worker);
        executor.execute(worker);
        //worker.execute();
    }
    class CancelAction extends AbstractAction{
        public CancelAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            cancelActionPerformed(evt);
        }
    }
    public synchronized void cancelActionPerformed(ActionEvent evt) {
        int[] selection = table.getSelectedRows();
        if(selection==null || selection.length<=0) return;
        for(int i=0;i<selection.length;i++) {
            int midx = table.convertRowIndexToModel(selection[i]);
            SwingWorker worker = model.getSwingWorker(midx);
            if(worker!=null && !worker.isDone()) {
                worker.cancel(true);
            }
            worker = null;
        }
        table.repaint();
    }

    class DeleteAction extends AbstractAction{
        public DeleteAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            deleteActionPerformed(evt);
        }
    }
    private final TreeSet<Integer> deleteRowSet = new TreeSet<Integer>();
    public synchronized void deleteActionPerformed(ActionEvent evt) {
        int[] selection = table.getSelectedRows();
        if(selection==null || selection.length<=0) return;
        for(int i=0;i<selection.length;i++) {
            int midx = table.convertRowIndexToModel(selection[i]);
            deleteRowSet.add(midx);
            SwingWorker worker = model.getSwingWorker(midx);
            if(worker!=null && !worker.isDone()) {
                worker.cancel(true);
                //((ThreadPoolExecutor)executor).remove(worker);
            }
            worker = null;
        }
        final RowFilter<TableModel,Integer> filter = new RowFilter<TableModel,Integer>() {
            @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                return !deleteRowSet.contains(entry.getIdentifier());
            }
        };
        sorter.setRowFilter(filter);
        table.repaint();
    }

    private class TablePopupMenu extends JPopupMenu {
        private final Action cancelAction = new CancelAction("cancel", null);
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(new TestCreateAction("add", null));
            //add(new ClearAction("clearSelection", null));
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            int[] l = table.getSelectedRows();
            boolean flag = (l!=null && l.length>0);
            cancelAction.setEnabled(flag);
            deleteAction.setEnabled(flag);
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
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
