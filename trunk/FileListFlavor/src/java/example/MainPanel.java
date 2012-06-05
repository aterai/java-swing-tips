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
    private final FileModel model = new FileModel();
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
                                model.addFileName(new FileName(file.getName(), file.getAbsolutePath()));
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

//         table.setDropMode(DropMode.INSERT_ROWS);
//         table.setTransferHandler(new FileTransferHandler());

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
            //add(new FileNameCreateAction("add", null));
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

// //table.setDropMode(DropMode.INSERT_ROWS);
// //table.setTransferHandler(new FileTransferHandler());
// class FileTransferHandler extends TransferHandler {
//     @Override public boolean importData(JComponent component, Transferable transferable) {
//         try{
//             if(canImport(component, transferable.getTransferDataFlavors())) {
//                 //DefaultTableModel model = (DefaultTableModel)((JTable)component).getModel();
//                 FileModel model = (FileModel)((JTable)component).getModel();
//                 for(Object o: (java.util.List)transferable.getTransferData(DataFlavor.javaFileListFlavor)) {
//                     if(o instanceof File) {
//                         File file = (File)o;
//                         //model.addRow(new Object[] {file, file.length(), file.getAbsolutePath()});
//                         model.addFileName(new FileName(file.getName(), file.getAbsolutePath()));
//                     }
//                 }
//                 return true;
//             }
//         }catch(Exception ex) {
//             ex.printStackTrace();
//         }
//         return false;
//     }
//     @Override public boolean canImport(JComponent component, DataFlavor[] flavors) {
//         for(DataFlavor f: flavors) {
//             if(DataFlavor.javaFileListFlavor.equals(f)) {
//                 return true;
//             }
//         }
//         return false;
//     }
//     @Override public int getSourceActions(JComponent component) {
//         return COPY;
//     }
// }

class FileModel extends DefaultTableModel {
    private static final ColumnContext[] columnArray = {
        new ColumnContext("No.",       Integer.class, false),
        new ColumnContext("Name",      String.class,  true),
        new ColumnContext("Full Path", String.class,  true)
    };
    private int number = 0;
    public void addFileName(FileName t) {
        Object[] obj = {number, t.getName(), t.getAbsolutePath()};
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
class FileName{
    private String name, absolutePath;
    public FileName(String name, String absolutePath) {
        this.name = name;
        this.absolutePath = absolutePath;
    }
    public void setName(String str) {
        name = str;
    }
    public void setAbsolutePath(String str) {
        absolutePath = str;
    }
    public String getName() {
        return name;
    }
    public String getAbsolutePath() {
        return absolutePath;
    }
}
