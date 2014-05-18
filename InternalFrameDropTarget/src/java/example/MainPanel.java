package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.activation.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final TransferHandler handler = new TableRowTransferHandler();
    //private final TransferHandler handler2 = new TableColumnTransferHandler();
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"AAA", 12, true}, {"aaa", 1, false},
        {"BBB", 13, true}, {"bbb", 2, false},
        {"CCC", 15, true}, {"ccc", 3, false},
        {"DDD", 17, true}, {"ddd", 4, false},
        {"EEE", 18, true}, {"eee", 5, false},
        {"FFF", 19, true}, {"fff", 6, false},
        {"GGG", 92, true}, {"ggg", 0, false}
    };
    private JTable makeDnDTable() {
        JTable table = new JTable(new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                switch (column) {
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
        });
        table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setTransferHandler(handler);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setDragEnabled(true);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setReorderingAllowed(false);
        //table.getTableHeader().setTransferHandler(handler2);
        //Handler h = new Handler();
        //table.getTableHeader().addMouseListener(h);
        //table.getTableHeader().addMouseMotionListener(h);

        //Disable row Cut, Copy, Paste
        ActionMap map = table.getActionMap();
        AbstractAction dummy = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                /* Dummy action */
            }
        };
        map.put(TransferHandler.getCutAction().getValue(Action.NAME),   dummy);
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),  dummy);
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy);
        return table;
    }

    public MainPanel() {
        super(new BorderLayout());

        JDesktopPane p = new JDesktopPane();

        JInternalFrame f1 = new JInternalFrame("aaaaaaaaa", true, true, true, true);
        f1.add(new JScrollPane(makeDnDTable()));
        f1.setOpaque(false);
        p.add(f1, 1, 1);
        f1.setBounds(0, 0, 240, 160);
        f1.setVisible(true);
        JInternalFrame f2 = new JInternalFrame("bbbbbbbb", true, true, true, true);
        f2.add(new JScrollPane(makeDnDTable()));
        p.add(f2, 1, 0);
        f2.setBounds(50, 50, 240, 160);
        f2.setVisible(true);
        f2.setOpaque(false);

        add(p);
        setPreferredSize(new Dimension(320, 240));
    }

//     private int index = -1;
//     private class Handler extends MouseAdapter { //, BeforeDrag
//         private Point startPt;
//         private final int gestureMotionThreshold = DragSource.getDragThreshold();
//         // MouseListener
//         @Override public void mousePressed(MouseEvent e) {
//             JTableHeader src = (JTableHeader) e.getComponent();
//             startPt = e.getPoint(); //e.getDragOrigin();
//             //System.out.println(startPt);
//         }
//         @Override public void mouseDragged(MouseEvent e)  {
//             Point tabPt = e.getPoint(); //e.getDragOrigin();
//             if (startPt != null && Math.sqrt(Math.pow(tabPt.x - startPt.x, 2) + Math.pow(tabPt.y - startPt.y, 2)) > gestureMotionThreshold) {
//                 JTableHeader src = (JTableHeader) e.getComponent();
//                 System.out.println("aaaaaaaaaaaaaaaaaaaaaaaa" + src);
//                 TransferHandler th = src.getTransferHandler();
//                 index = src.columnAtPoint(tabPt);
//                 th.exportAsDrag(src, e, TransferHandler.MOVE);
//                 //lineRect.setRect(0, 0, 0, 0);
//                 //src.getRootPane().getGlassPane().setVisible(true);
//                 //src.setDropLocation(new DropLocation(tabPt, -1), null, true);
//                 startPt = null;
//             }
//         }
//     }
//     class TableColumnTransferHandler extends TransferHandler {
//         private final DataFlavor localObjectFlavor = new ActivationDataFlavor(
//             JTableHeader.class, DataFlavor.javaJVMLocalObjectMimeType,
//             "Integer Column Model Index");
//
//         @Override protected Transferable createTransferable(JComponent c) {
//             System.out.println("createTransferable");
//             JTableHeader header = (JTableHeader) c;
//             //int index = table.getSelectedColumn();
//             //TableColumn column = header.getDraggedColumn();
//             return new DataHandler(header, localObjectFlavor.getMimeType());
//         }
//         @Override public boolean canImport(TransferHandler.TransferSupport info) {
//             //System.out.println("canImport");
//             return info.isDataFlavorSupported(localObjectFlavor);
//         }
//         @Override public int getSourceActions(JComponent c) {
//             System.out.println("getSourceActions");
//             return TransferHandler.MOVE;
//         }
//         @Override public boolean importData(TransferHandler.TransferSupport info) {
//             System.out.println("importData");
//             JTableHeader target = (JTableHeader) info.getComponent();
//             //JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
//             try {
//                 JTableHeader source = (JTableHeader) info.getTransferable().getTransferData(localObjectFlavor);
//                 System.out.println("source: " + source);
//                 if (!source.equals(target)) {
//                     System.out.println("-------------------------------");
//                     TableColumn column = source.getColumnModel().getColumn(index);
//                     source.getColumnModel().removeColumn(column);
//                     target.getColumnModel().addColumn(column);
//                 }
//             } catch (Exception e) {
//                 e.printStackTrace();
//             }
//             return false;
//         }
//     }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
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

// class TableDropTargetAdapter extends DropTargetAdapter {
//     @Override public void drop(...
//     @Override public void dragEnter(DropTargetDragEvent dtde) {
//         Component c = dtde.getDropTargetContext().getComponent();
//         Container cn = SwingUtilities.getAncestorOfClass(JInternalFrame.class, c);
//         if (cn != null) {
//             JInternalFrame f = (JInternalFrame) cn;
//             f.moveToFront();
//             f.getParent().repaint();
//         }
//     }
// }

//Demo - BasicDnD (Drag and Drop and Data Transfer)
//http://docs.oracle.com/javase/tutorial/uiswing/dnd/basicdemo.html
class TableRowTransferHandler extends TransferHandler {
    private final DataFlavor localObjectFlavor;
    private int[] indices;
    private int addIndex = -1; //Location where items were added
    private int addCount; //Number of items added.
    private JComponent source;

    public TableRowTransferHandler() {
        super();
        localObjectFlavor = new ActivationDataFlavor(Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
    }
    @Override protected Transferable createTransferable(JComponent c) {
        source = c;
        JTable table = (JTable) c;
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        List<Object> list = new ArrayList<>();
        indices = table.getSelectedRows();
        for (int i : indices) {
            list.add(model.getDataVector().elementAt(i));
        }
        Object[] transferedObjects = list.toArray();
        return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    }
    private JInternalFrame getInternalFrame(JComponent c) {
        Container cn = SwingUtilities.getAncestorOfClass(JInternalFrame.class, c);
        if (cn != null) {
            return (JInternalFrame) cn;
        }
        return null;
    }
    private boolean isDropableTableIntersection(TransferSupport info) {
        Component c = info.getComponent();
        if (!(c instanceof JTable)) {
            return false;
        }
        JTable target = (JTable) c;
        if (!target.equals(source)) {
            JDesktopPane dp = null;
            Container cn = SwingUtilities.getAncestorOfClass(JDesktopPane.class, target);
            if (cn != null) {
                dp = (JDesktopPane) cn;
            }

            JInternalFrame sf = getInternalFrame(source);
            JInternalFrame tf = getInternalFrame(target);
            if (sf == null || tf == null || dp.getIndexOf(tf) < dp.getIndexOf(sf)) {
                return false;
            }

            Point pt = SwingUtilities.convertPoint(target, info.getDropLocation().getDropPoint(), dp);
            Rectangle rect = sf.getBounds().intersection(tf.getBounds());
            if (rect.contains(pt)) {
                return false;
            }
            //tf.moveToFront();
            //tf.getParent().repaint();
        }
        return true;
    }
    @Override public boolean canImport(TransferSupport info) {
        boolean isDropable = info.isDrop() && info.isDataFlavorSupported(localObjectFlavor) && isDropableTableIntersection(info);
        info.getComponent().setCursor(isDropable ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
        return isDropable;
    }
    @Override public int getSourceActions(JComponent c) {
        return MOVE;
    }
    @Override public boolean importData(TransferSupport info) {
        if (!canImport(info)) {
            return false;
        }
        TransferHandler.DropLocation tdl = info.getDropLocation();
        if (!(tdl instanceof JTable.DropLocation)) {
            return false;
        }
        JTable.DropLocation dl = (JTable.DropLocation) tdl;
        JTable target = (JTable) info.getComponent();
        DefaultTableModel model = (DefaultTableModel) target.getModel();
        int index = dl.getRow();
        //boolean insert = dl.isInsert();
        int max = model.getRowCount();
        if (index < 0 || index > max) {
            index = max;
        }
        addIndex = index;
        target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        try {
            Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
            if (Objects.equals(source, target)) {
                addCount = values.length;
            }
            for (int i = 0; i < values.length; i++) {
                int idx = index++;
                model.insertRow(idx, (Vector) values[i]);
                target.getSelectionModel().addSelectionInterval(idx, idx);
            }
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    @Override protected void exportDone(JComponent c, Transferable data, int action) {
        cleanup(c, action == MOVE);
    }
    private void cleanup(JComponent c, boolean remove) {
        if (remove && indices != null) {
            c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            DefaultTableModel model = (DefaultTableModel) ((JTable) c).getModel();
            if (addCount > 0) {
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] >= addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            for (int i = indices.length - 1; i >= 0; i--) {
                model.removeRow(indices[i]);
            }
        }
        indices  = null;
        addCount = 0;
        addIndex = -1;
    }
}
