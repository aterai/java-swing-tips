package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final int IMAGE_ID = 0;
    private final FileModel model = new FileModel();
    private final JTable table = new JTable(model);
    private transient MediaTracker tracker;

    private class ImageDropTargetListener extends DropTargetAdapter {
        @Override public void dragOver(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
                return;
            }
            dtde.rejectDrag();
        }
        @Override public void drop(DropTargetDropEvent dtde) {
            try {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dtde.getTransferable();
                    ((List<?>) transferable.getTransferData(DataFlavor.javaFileListFlavor))
                      .stream().filter(File.class::isInstance).map(File.class::cast)
                      .forEach(MainPanel.this::addImageFile);
                    dtde.dropComplete(true);
                    return;
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
            dtde.rejectDrop();
        }
    }

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setComponentPopupMenu(new TablePopupMenu());
        table.setInheritsPopupMenu(true);

        DropTargetListener dtl = new ImageDropTargetListener();
        new DropTarget(table, DnDConstants.ACTION_COPY, dtl, true);
        new DropTarget(scroll.getViewport(), DnDConstants.ACTION_COPY, dtl, true);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        try {
            addImageFile(new File(getClass().getResource("test.png").toURI()));
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        add(scroll);
        setPreferredSize(new Dimension(320, 240));
    }

    protected void addImageFile(File file) {
        String path = file.getAbsolutePath();
        Image img = Toolkit.getDefaultToolkit().createImage(path);
        tracker = Optional.ofNullable(tracker).orElseGet(() -> new MediaTracker((Container) this));
        tracker.addImage(img, IMAGE_ID);
        try {
            tracker.waitForID(IMAGE_ID);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            if (!tracker.isErrorID(IMAGE_ID)) {
                model.addTest(new Test(file.getName(), path, img.getWidth(this), img.getHeight(this)));
            }
            tracker.removeImage(img);
        }
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

class FileModel extends DefaultTableModel {
    private static final List<ColumnContext> COLUMN_LIST = Arrays.asList(
        new ColumnContext("No.",       Integer.class, false),
        new ColumnContext("Name",      String.class,  false),
        new ColumnContext("Full Path", String.class,  false),
        new ColumnContext("Width",     Integer.class, false),
        new ColumnContext("Height",    Integer.class, false)
    );
    private int number;
    public void addTest(Test t) {
        Object[] obj = {
            number, t.getName(), t.getComment(), t.getWidth(), t.getHeight()
        };
        super.addRow(obj);
        number++;
    }
    @Override public boolean isCellEditable(int row, int col) {
        return COLUMN_LIST.get(col).isEditable;
    }
    @Override public Class<?> getColumnClass(int column) {
        return COLUMN_LIST.get(column).columnClass;
    }
    @Override public int getColumnCount() {
        return COLUMN_LIST.size();
    }
    @Override public String getColumnName(int column) {
        return COLUMN_LIST.get(column).columnName;
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
    private int width;
    private int height;
    protected Test(String name, String comment, int width, int height) {
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

class TablePopupMenu extends JPopupMenu {
    private final Action deleteAction = new AbstractAction("Remove from list") {
        @Override public void actionPerformed(ActionEvent e) {
            JTable table = (JTable) getInvoker();
            int[] selection = table.getSelectedRows();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    };
    protected TablePopupMenu() {
        super();
        add(deleteAction);
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTable) {
            deleteAction.setEnabled(((JTable) c).getSelectedRowCount() > 0);
            super.show(c, x, y);
        }
    }
}
