package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final FileModel model = new FileModel();
    private final JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);

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
                        List list = (List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        for(Object o: list) {
                            if(o instanceof File) {
                                File file = (File)o;
                                addImageFile(file);
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

        JScrollPane scroll = new JScrollPane(table);
        scroll.setComponentPopupMenu(new TablePopupMenu());
        table.setInheritsPopupMenu(true);

        new DropTarget(table, DnDConstants.ACTION_COPY, dtl, true);
        new DropTarget(scroll.getViewport(), DnDConstants.ACTION_COPY, dtl, true);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        try{
            addImageFile(new File(getClass().getResource("test.png").toURI()));
        }catch(java.net.URISyntaxException e) {
            e.printStackTrace();
        }
        add(scroll);
        setPreferredSize(new Dimension(320, 240));
    }

    private MediaTracker tracker;
    private static final int id = 0;
    private void addImageFile(File file) {
        String path = file.getAbsolutePath();
        Image img = Toolkit.getDefaultToolkit().createImage(path);
        if(tracker==null) {
            tracker = new MediaTracker((Container)this);
        }
        tracker.addImage(img, id);
        try{
            tracker.waitForID(id);
        }catch(InterruptedException e) {
            e.printStackTrace();
        }finally{
            if(!tracker.isErrorID(id)) {
                model.addTest(new Test(file.getName(), path,
                                       img.getWidth(this), img.getHeight(this)));
            }
            tracker.removeImage(img);
        }
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
        private final Action deleteAction = new DeleteAction("Remove from list", null);
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
class FileModel extends DefaultTableModel {
    private static final ColumnContext[] columnArray = {
        new ColumnContext("No.",       Integer.class, false),
        new ColumnContext("Name",      String.class,  false),
        new ColumnContext("Full Path", String.class,  false),
        new ColumnContext("Width",     Integer.class, false),
        new ColumnContext("Height",    Integer.class, false)
    };
    private int number = 0;
    public void addTest(Test t) {
        Object[] obj = {
            number, t.getName(), t.getComment(),
            t.getWidth(), t.getHeight()
        };
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
class Test{
    private String name, comment;
    private int width, height;
    public Test(String name, String comment, int width, int height) {
        this.name    = name;
        this.comment = comment;
        this.width   = width;
        this.height  = height;
    }
    public void setName(String str) {
        name = str;
    }
    public void setComment(String str) {
        comment = str;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public String getName() {
        return name;
    }
    public String getComment() {
        return comment;
    }
    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
}
