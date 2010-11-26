package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private static final int START_HEIGHT = 4;
    private static final int END_HEIGHT = 24;
    private static final int DELAY = 10;
    private static final Color evenColor = new Color(250, 250, 250);
    private final TestModel model = new TestModel();
    private final JTable table = new JTable(model) {
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
    public MainPanel() {
        super(new BorderLayout());
        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));

        //table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(START_HEIGHT);
        for(int i=0;i<model.getRowCount();i++) table.setRowHeight(i, END_HEIGHT);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setComponentPopupMenu(new TablePopupMenu());
        table.setInheritsPopupMenu(true);
        add(scroll);
        add(new JButton(new TestCreateAction("add", null)), BorderLayout.SOUTH);
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
        model.addTest(new Test("New name", ""));
        (new javax.swing.Timer(DELAY, new ActionListener() {
            int i = table.convertRowIndexToView(model.getRowCount()-1);
            int h = START_HEIGHT;
            @Override public void actionPerformed(ActionEvent e) {
                if(h<END_HEIGHT) {
                    table.setRowHeight(i, h++);
                }else{
                    ((javax.swing.Timer)e.getSource()).stop();
                }
            }
        })).start();
    }

    class DeleteAction extends AbstractAction{
        public DeleteAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            deleteActionPerformed(evt);
        }
    }
    public void deleteActionPerformed(ActionEvent evt) {
        final int[] selection = table.getSelectedRows();
        if(selection==null || selection.length<=0) return;
        (new javax.swing.Timer(DELAY, new ActionListener() {
            int h = END_HEIGHT;
            @Override public void actionPerformed(ActionEvent e) {
                h--;
                if(h>START_HEIGHT) {
                    for(int i=selection.length-1;i>=0;i--) {
                        table.setRowHeight(selection[i], h);
                    }
                }else{
                    ((javax.swing.Timer)e.getSource()).stop();
                    for(int i=selection.length-1;i>=0;i--) {
                        model.removeRow(table.convertRowIndexToModel(selection[i]));
                    }
                }
            }
        })).start();
    }

//     public void xxx_deleteActionPerformed(ActionEvent evt) {
//         final int[] selection = table.getSelectedRows();
//         if(selection==null || selection.length<=0) return;
//         (new SwingWorker<Void, Integer>() {
//             @Override public Void doInBackground() {
//                 int current = END_HEIGHT;
//                 while(current>START_HEIGHT && !isCancelled()) {
//                     try{
//                         Thread.sleep(DELAY);
//                     }catch(InterruptedException ie) {
//                         return null;
//                     }
//                     publish(current--);
//                 }
//                 return null;
//             }
//             @Override protected void process(java.util.List<Integer> chunks) {
//                 for(Integer height: chunks) {
//                     for(int i=selection.length-1;i>=0;i--) {
//                         table.setRowHeight(selection[i], height);
//                     }
//                 }
//             }
//             @Override public void done() {
//                 for(int i=selection.length-1;i>=0;i--) {
//                     model.removeRow(table.convertRowIndexToModel(selection[i]));
//                 }
//             }
//         }).execute();
//     }

    private class TablePopupMenu extends JPopupMenu {
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(new TestCreateAction("add", null));
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            int[] l = table.getSelectedRows();
            deleteAction.setEnabled(l!=null && l.length>0);
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
