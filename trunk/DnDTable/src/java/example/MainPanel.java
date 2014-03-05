package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final Color EVEN_COLOR = new Color(250, 250, 250);
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false},
        {"eee",  1, true}, {"GGG", 3, false}, {"hhh", 72, true}, {"fff", 4, false},
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final DnDTable table = new DnDTable(model) {
        @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
            Component c = super.prepareRenderer(tcr, row, column);
            if (isRowSelected(row)) {
                c.setForeground(getSelectionForeground());
                c.setBackground(getSelectionBackground());
            } else {
                c.setForeground(getForeground());
                c.setBackground((row % 2 == 0) ? EVEN_COLOR : table.getBackground());
            }
            return c;
        }
    };
    public MainPanel() {
        super(new BorderLayout());

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu());
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }

    class TestCreateAction extends AbstractAction {
        public TestCreateAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            model.addRow(new Object[] {"New row", Integer.valueOf(0), Boolean.FALSE});
            Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
            table.scrollRectToVisible(r);
        }
    }

    class DeleteAction extends AbstractAction {
        public DeleteAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            int[] selection = table.getSelectedRows();
            if (selection.length == 0) {
                return;
            }
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    }

    private class TablePopupMenu extends JPopupMenu {
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
            deleteAction.setEnabled(l.length > 0);
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

class DnDTable extends JTable implements DragGestureListener, Transferable {
    private static final String NAME = "test";
    private static final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
    private static final Color LINE_COLOR = new Color(255, 100, 100);
    private final Rectangle2D targetLine = new Rectangle2D.Float();
    private int draggedIndex = -1;
    private int targetIndex  = -1;

    public DnDTable(TableModel model) {
        super(model);
        //DropTarget dropTarget =
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
        //DragSource dragSource = new DragSource();
        new DragSource().createDefaultDragGestureRecognizer((Component) this, DnDConstants.ACTION_COPY_OR_MOVE, (DragGestureListener) this);
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (targetIndex >= 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(LINE_COLOR);
            g2.fill(targetLine);
            g2.dispose();
        }
    }
    private void initTargetLine(Point p) {
        Rectangle2D testArea = new Rectangle2D.Float();
        int cellHeight = getRowHeight();
        int lineWidht  = getWidth();
        int lineHeight = 2;
        int modelSize  = getRowCount();
        targetIndex = -1;
        for (int i = 0; i < modelSize; i++) {
            testArea.setRect(0, cellHeight * i - cellHeight / 2, lineWidht, cellHeight);
            if (testArea.contains(p)) {
                targetIndex = i;
                targetLine.setRect(0, i * cellHeight, lineWidht, lineHeight);
                break;
            }
        }
        if (targetIndex < 0) {
            targetIndex = modelSize;
            targetLine.setRect(0, targetIndex * cellHeight - lineHeight, lineWidht, lineHeight);
        }
    }

    // Interface: DragGestureListener
    @Override public void dragGestureRecognized(DragGestureEvent e) {
        if (getSelectedRowCount() > 1) {
            return;
        }
        draggedIndex = rowAtPoint(e.getDragOrigin());
        if (draggedIndex < 0) {
            return;
        }
        try {
            e.startDrag(DragSource.DefaultMoveDrop, (Transferable) this, new TableDragSourceListener());
        } catch (InvalidDnDOperationException idoe) {
            idoe.printStackTrace();
        }
    }

    // Interface: Transferable
    @Override public Object getTransferData(DataFlavor flavor) {
        return this;
    }
    @Override public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] f = new DataFlavor[1];
        f[0] = this.FLAVOR;
        return f;
    }
    @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.getHumanPresentableName().equals(NAME);
    }

    class CDropTargetListener implements DropTargetListener {
        @Override public void dragExit(DropTargetEvent e) {
            targetIndex = -1;
            repaint();
        }
        @Override public void dragEnter(DropTargetDragEvent e) {
            if (isDragAcceptable(e)) {
                e.acceptDrag(e.getDropAction());
            } else {
                e.rejectDrag();
            }
        }
        @Override public void dragOver(final DropTargetDragEvent e) {
            if (isDragAcceptable(e)) {
                e.acceptDrag(e.getDropAction());
                setCursor(DragSource.DefaultMoveDrop);
            } else {
                e.rejectDrag();
                setCursor(DragSource.DefaultMoveNoDrop);
                return;
            }
            initTargetLine(e.getLocation());
            repaint();
        }
        @Override public void dropActionChanged(DropTargetDragEvent e) {
            // if (isDragAcceptable(e)) { e.acceptDrag(e.getDropAction()); }
            // else e.rejectDrag();
        }
        @Override public void drop(DropTargetDropEvent e) {
//             Transferable t = e.getTransferable();
//             DataFlavor[] f = t.getTransferDataFlavors();
//             Component c = null;
//             try {
//                 c = (Component) t.getTransferData(f[0]);
//             } catch (UnsupportedFlavorException ex) {
//                 e.dropComplete(false);
//             } catch (IOException ie) {
//                 e.dropComplete(false);
//             }
//             if (c instanceof JTable) {
//                 JTable table = (JTable) c;
//                 DefaultTableModel model = (DefaultTableModel) table.getModel();
            DefaultTableModel model = (DefaultTableModel) getModel();
            if (isDropAcceptable(e)) {
                if (targetIndex == draggedIndex) {
                    setRowSelectionInterval(targetIndex, targetIndex);
                } else {
                    int tg = targetIndex < draggedIndex ? targetIndex : targetIndex - 1;
                    model.moveRow(draggedIndex, draggedIndex, tg);
                    setRowSelectionInterval(tg, tg);
                }
                e.dropComplete(true);
            } else {
                e.dropComplete(false);
            }
            e.dropComplete(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            targetIndex = -1;
            repaint();
        }
        private boolean isDragAcceptable(DropTargetDragEvent e) {
            DataFlavor[] f = e.getCurrentDataFlavors();
            return isDataFlavorSupported(f[0]);
        }
        private boolean isDropAcceptable(DropTargetDropEvent e) {
            Transferable t = e.getTransferable();
            DataFlavor[] f = t.getTransferDataFlavors();
            return isDataFlavorSupported(f[0]);
        }
    }
}

class TableDragSourceListener implements DragSourceListener {
    @Override public void dragEnter(DragSourceDragEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
    }
    @Override public void dragExit(DragSourceEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }
    @Override public void dragOver(DragSourceDragEvent e)          { /* not needed */ }
    @Override public void dropActionChanged(DragSourceDragEvent e) { /* not needed */ }
    @Override public void dragDropEnd(DragSourceDropEvent e) {
        //e.getDragSourceContext().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
