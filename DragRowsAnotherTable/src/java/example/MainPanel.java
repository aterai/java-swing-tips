package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Vector;
import javax.activation.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    TransferHandler handler = new TableRowTransferHandler();
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
    private JTable makeDnDTable() {
        JTable t = new JTable(new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        });
        t.getSelectionModel().setSelectionMode(
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        t.setTransferHandler(handler);
        t.setDropMode(DropMode.INSERT_ROWS);
        t.setDragEnabled(true);
        t.setFillsViewportHeight(true);
        return t;
    }
    public MainPanel() {
        super(new BorderLayout());
        JPanel p = new JPanel(new GridLayout(2,1));
        p.add(new JScrollPane(makeDnDTable()));
        p.add(new JScrollPane(makeDnDTable()));
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
class TableRowTransferHandler extends TransferHandler {
    private int[] rows    = null;
    private int addIndex  = -1;
    private int addCount  = 0;
    private final DataFlavor localObjectFlavor;
    private Object[] transferedObjects = null;
    private JComponent source = null;
    public TableRowTransferHandler() {
        localObjectFlavor = new ActivationDataFlavor(Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
    }
    @Override protected Transferable createTransferable(JComponent c) {
        source = c;
        JTable table = (JTable) c;
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        ArrayList<Object> list = new ArrayList<Object>();
        for(int i: rows = table.getSelectedRows())
          list.add(model.getDataVector().elementAt(i));
        transferedObjects = list.toArray();
        return new DataHandler(transferedObjects,localObjectFlavor.getMimeType());
    }
    @Override public boolean canImport(TransferHandler.TransferSupport info) {
        JTable t = (JTable)info.getComponent();
        boolean b = info.isDrop()&&info.isDataFlavorSupported(localObjectFlavor);
        //XXX bug?
        t.setCursor(b?DragSource.DefaultMoveDrop:DragSource.DefaultMoveNoDrop);
        return b;
    }
    @Override public int getSourceActions(JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }
    @Override public boolean importData(TransferHandler.TransferSupport info) {
        JTable target = (JTable)info.getComponent();
        JTable.DropLocation dl  = (JTable.DropLocation)info.getDropLocation();
        DefaultTableModel model = (DefaultTableModel)target.getModel();
        int index = dl.getRow();
        int max = model.getRowCount();
        if(index<0 || index>max) index = max;
        addIndex = index;
        target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        try{
            Object[] values = (Object[])info.getTransferable().getTransferData(localObjectFlavor);
            if(source==target) addCount = values.length;
            for(int i=0;i<values.length;i++) {
                int idx = index++;
                model.insertRow(idx, (Vector)values[i]);
                target.getSelectionModel().addSelectionInterval(idx, idx);
            }
            return true;
        }catch(UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
        }catch(java.io.IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }
    @Override protected void exportDone(JComponent c, Transferable t, int act) {
        cleanup(c, act == TransferHandler.MOVE);
    }
    private void cleanup(JComponent src, boolean remove) {
        if(remove && rows != null) {
            JTable table = (JTable)src;
            src.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            DefaultTableModel model = (DefaultTableModel)table.getModel();
            if(addCount > 0) {
                for(int i=0;i<rows.length;i++) {
                    if(rows[i]>=addIndex) {
                        rows[i] += addCount;
                    }
                }
            }
            for(int i=rows.length-1;i>=0;i--) model.removeRow(rows[i]);
        }
        rows     = null;
        addCount = 0;
        addIndex = -1;
    }
}
