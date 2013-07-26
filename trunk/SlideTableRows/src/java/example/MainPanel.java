package example;
//-*- mode:java; encoding:utf-8 -*-
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
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            //ArrayIndexOutOfBoundsException:  0 >= 0
            //Bug ID: JDK-6967479 JTable sorter fires even if the model is empty
            //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6967479
            //return getValueAt(0, column).getClass();
            switch(column) {
              case 0:
                return String.class;
              case 1:
                return Number.class;
              case 2:
                return Boolean.class;
              default:
                return super.getColumnClass(column);
            }
        }
    };
    private final JTable table = new JTable(model);
    public MainPanel() {
        super(new BorderLayout());
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
        model.addRow(new Object[] {"New name", model.getRowCount(), false});
        (new Timer(DELAY, new ActionListener() {
            int i = table.convertRowIndexToView(model.getRowCount()-1);
            int h = START_HEIGHT;
            @Override public void actionPerformed(ActionEvent e) {
                if(h<END_HEIGHT) {
                    table.setRowHeight(i, h++);
                }else{
                    ((Timer)e.getSource()).stop();
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
        (new Timer(DELAY, new ActionListener() {
            int h = END_HEIGHT;
            @Override public void actionPerformed(ActionEvent e) {
                h--;
                if(h>START_HEIGHT) {
                    for(int i=selection.length-1;i>=0;i--) {
                        table.setRowHeight(selection[i], h);
                    }
                }else{
                    ((Timer)e.getSource()).stop();
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
//             @Override protected void process(List<Integer> chunks) {
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
