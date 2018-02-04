package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(makeList()));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JList<String> makeList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("1111");
        model.addElement("22222222");
        model.addElement("333333333333");
        model.addElement("<<<<<<---->>>>>>");
        model.addElement("AAAAAAAAAAAAAA");
        model.addElement("****");

        JList<String> list = new DnDList<>();
        list.setModel(model);
        list.setCellRenderer(new DefaultListCellRenderer() {
            private final Color ec = new Color(240, 240, 240);
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setForeground(list.getSelectionForeground());
                    setBackground(list.getSelectionBackground());
                } else {
                    setForeground(list.getForeground());
                    setBackground(index % 2 == 0 ? ec : list.getBackground());
                }
                return this;
            }
        });
        return list;
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

class DnDList<E> extends JList<E> implements DragGestureListener, Transferable {
    private static final Color LINE_COLOR = new Color(100, 100, 255);
    private static final String NAME = "test";
    private static final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
    private final Rectangle targetLine = new Rectangle();
    protected int draggedIndex = -1;
    protected int targetIndex = -1;
    protected DnDList() {
        super();
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer((Component) this, DnDConstants.ACTION_COPY_OR_MOVE, (DragGestureListener) this);
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (targetIndex >= 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(LINE_COLOR);
            g2.fill(targetLine);
            g2.dispose();
        }
    }
    protected void initTargetLine(Point p) {
        Rectangle rect = getCellBounds(0, 0);
        int cellHeight = rect.height;
        int lineHeight = 2;
        int modelSize = getModel().getSize();
        targetIndex = -1;
        targetLine.setSize(rect.width, lineHeight);
        for (int i = 0; i < modelSize; i++) {
            rect.setLocation(0, cellHeight * i - cellHeight / 2);
            if (rect.contains(p)) {
                targetIndex = i;
                targetLine.setLocation(0, i * cellHeight);
                break;
            }
        }
        if (targetIndex < 0) {
            targetIndex = modelSize;
            targetLine.setLocation(0, targetIndex * cellHeight - lineHeight);
        }
    }

    // Interface: DragGestureListener
    @Override public void dragGestureRecognized(DragGestureEvent e) {
        boolean isMoreThanOneItemSelected = getSelectedIndices().length > 1;
        if (isMoreThanOneItemSelected) {
            return;
        }
        draggedIndex = locationToIndex(e.getDragOrigin());
        if (draggedIndex < 0) {
            return;
        }
        try {
            e.startDrag(DragSource.DefaultMoveDrop, (Transferable) this, new ListDragSourceListener());
        } catch (InvalidDnDOperationException ex) {
            ex.printStackTrace();
        }
    }

    // Interface: Transferable
    @Override public Object getTransferData(DataFlavor flavor) {
        return this;
    }
    @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {FLAVOR};
    }
    @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.getHumanPresentableName().equals(NAME);
    }

    private class CDropTargetListener implements DropTargetListener {
        // DropTargetListener interface
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
        @Override public void dragOver(DropTargetDragEvent e) {
            if (isDragAcceptable(e)) {
                e.acceptDrag(e.getDropAction());
            } else {
                e.rejectDrag();
                return;
            }
            initTargetLine(e.getLocation());
            repaint();
        }
        @Override public void dropActionChanged(DropTargetDragEvent e) {
            // if (isDragAcceptable(e)) {
            //     e.acceptDrag(e.getDropAction());
            // } else {
            //     e.rejectDrag();
            // }
        }
        @Override public void drop(DropTargetDropEvent e) {
            DefaultListModel<E> model = (DefaultListModel<E>) getModel();
//             Transferable t = e.getTransferable();
//             DataFlavor[] f = t.getTransferDataFlavors();
//             try {
//                 Component comp = (Component) t.getTransferData(f[0]);
//             } catch (UnsupportedFlavorException | IOException ex) {
//                 e.dropComplete(false);
//             }
            if (isDropAcceptable(e)) {
                E str = model.get(draggedIndex);
                if (targetIndex == draggedIndex) {
                    setSelectedIndex(targetIndex);
                } else if (targetIndex < draggedIndex) {
                    model.remove(draggedIndex);
                    model.add(targetIndex, str);
                    setSelectedIndex(targetIndex);
                } else {
                    model.add(targetIndex, str);
                    model.remove(draggedIndex);
                    setSelectedIndex(targetIndex - 1);
                }
                e.dropComplete(true);
            } else {
                e.dropComplete(false);
            }
            e.dropComplete(false);
            targetIndex = -1;
            repaint();
        }
        private boolean isDragAcceptable(DropTargetDragEvent e) {
            return isDataFlavorSupported(e.getCurrentDataFlavors()[0]);
        }
        private boolean isDropAcceptable(DropTargetDropEvent e) {
            return isDataFlavorSupported(e.getTransferable().getTransferDataFlavors()[0]);
        }
    }
}

class ListDragSourceListener implements DragSourceListener {
    @Override public void dragEnter(DragSourceDragEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
    }
    @Override public void dragExit(DragSourceEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }
    @Override public void dragOver(DragSourceDragEvent e) { /* not needed */ }
    @Override public void dropActionChanged(DragSourceDragEvent e) { /* not needed */ }
    @Override public void dragDropEnd(DragSourceDropEvent e) { /* not needed */ }
}
