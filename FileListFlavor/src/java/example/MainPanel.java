package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final TestModel model = new TestModel();
    private final JTable table;
    public MainPanel() {
        super(new BorderLayout());
        table = new JTable(model);
        DropTargetListener dtl = new DropTargetAdapter() {
            @Override public void dragOver(DropTargetDragEvent dtde) {
                if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                    return;
                }
                dtde.rejectDrag();
            }
            @Override public void drop(DropTargetDropEvent dtde) {
                try{
                    if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        Transferable transferable = dtde.getTransferable();
                        java.util.List list = (java.util.List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        for(Object o: list) {
                            if(o instanceof File) {
                                File file = (File) o;
                                model.addTest(new Test(file.getName(), file.getAbsolutePath()));
                            }
                        }
                        dtde.dropComplete(true);
                        return;
                    }
                }catch(UnsupportedFlavorException ufe) {
                    ufe.printStackTrace();
                }catch(IOException ioe) {
                    ioe.printStackTrace();
                }
                dtde.rejectDrop();
            }
        };

        new DropTarget(table, DnDConstants.ACTION_COPY, dtl, true);
        //new DropTarget(scroll.getViewport(), DnDConstants.ACTION_COPY, dtl, true);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu());
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 200));
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
        int[] selection = table.getSelectedRows();
        if(selection==null || selection.length<=0) return;
        for(int i=selection.length-1;i>=0;i--) {
            model.removeRow(table.convertRowIndexToModel(selection[i]));
        }
    }
    private class TablePopupMenu extends JPopupMenu {
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            //add(new TestCreateAction("add", null));
            //add(new ClearAction("clearSelection", null));
            //addSeparator();
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
