package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import javax.activation.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        String[] columnNames = {"String", "Integer", "Boolean"};
        Object[][] data = {
            {"AAA", 12, true}, {"aaa", 1, false},
            {"BBB", 13, true}, {"bbb", 2, false},
            {"CCC", 15, true}, {"ccc", 3, false},
            {"DDD", 17, true}, {"ddd", 4, false},
            {"EEE", 18, true}, {"eee", 5, false},
            {"FFF", 19, true}, {"fff", 6, false},
            {"GGG", 92, true}, {"ggg", 0, false}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setTransferHandler(new TableRowTransferHandler());
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setDragEnabled(true);

        //Disable row Cut, Copy, Paste
        ActionMap map = table.getActionMap();
        AbstractAction dummy = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {}
        };
        map.put(TransferHandler.getCutAction().getValue(Action.NAME),   dummy);
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),  dummy);
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy);

        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(table));
        p.setBorder(BorderFactory.createTitledBorder("Drag & Drop JTable"));
        add(p);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
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

//Demo - BasicDnD (Drag and Drop and Data Transfer)>http://docs.oracle.com/javase/tutorial/uiswing/dnd/basicdemo.html
class TableRowTransferHandler extends TransferHandler {
    private final DataFlavor localObjectFlavor;
    private Object[] transferedObjects = null;
    public TableRowTransferHandler() {
        localObjectFlavor = new ActivationDataFlavor(Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
    }
    @Override protected Transferable createTransferable(JComponent c) {
        JTable table = (JTable) c;
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        ArrayList<Object> list = new ArrayList<Object>();
        for(int i: indices = table.getSelectedRows()) {
            list.add(model.getDataVector().elementAt(i));
        }
        transferedObjects = list.toArray();
        return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    }
    @Override public boolean canImport(TransferSupport info) {
        JTable table = (JTable)info.getComponent();
        boolean isDropable = info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
        table.setCursor(isDropable?DragSource.DefaultMoveDrop:DragSource.DefaultMoveNoDrop);
        return isDropable;
    }
    @Override public int getSourceActions(JComponent c) {
        return MOVE; //TransferHandler.COPY_OR_MOVE;
    }
    @Override public boolean importData(TransferSupport info) {
        if(!canImport(info)) {
            return false;
        }
        JTable target = (JTable)info.getComponent();
        JTable.DropLocation dl = (JTable.DropLocation)info.getDropLocation();
        DefaultTableModel model = (DefaultTableModel)target.getModel();
        int index = dl.getRow();
        //boolean insert = dl.isInsert();
        int max = model.getRowCount();
        if(index<0 || index>max) {
            index = max;
        }
        addIndex = index;

        try{
            Object[] values = (Object[])info.getTransferable().getTransferData(localObjectFlavor);
            addCount = values.length;
            for(int i=0;i<values.length;i++) {
                int idx = index++;
                model.insertRow(idx, (Vector)values[i]);
                target.getSelectionModel().addSelectionInterval(idx, idx);
            }
            return true;
        }catch(UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }
    @Override protected void exportDone(JComponent c, Transferable data, int action) {
        cleanup(c, action == MOVE);
    }
    private void cleanup(JComponent c, boolean remove) {
        if(remove && indices != null) {
            JTable source = (JTable)c;
            source.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            DefaultTableModel model  = (DefaultTableModel)source.getModel();
            if(addCount > 0) {
                for(int i=0;i<indices.length;i++) {
                    if(indices[i]>=addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            for(int i=indices.length-1;i>=0;i--) {
                model.removeRow(indices[i]);
            }
        }
        indices  = null;
        addCount = 0;
        addIndex = -1;
    }
    private int[] indices = null;
    private int addIndex  = -1; //Location where items were added
    private int addCount  = 0;  //Number of items added.
}
