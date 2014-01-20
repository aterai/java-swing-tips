package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.geom.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(makeList()));
        setPreferredSize(new Dimension(320, 200));
    }
    @SuppressWarnings("unchecked")
    private static JList makeList() {
        DefaultListModel model = new DefaultListModel();
        model.addElement("1111");
        model.addElement("22222222");
        model.addElement("333333333333");
        model.addElement("<<<<<<---->>>>>>");
        model.addElement("AAAAAAAAAAAAAA");
        model.addElement("****");

        DnDList list = new DnDList();
        list.setModel(model);
        list.setCellRenderer(new DefaultListCellRenderer() {
            private final Color ec = new Color(240, 240, 240);
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                if(isSelected) {
                    setForeground(list.getSelectionForeground());
                    setBackground(list.getSelectionBackground());
                }else{
                    setForeground(list.getForeground());
                    setBackground(index%2==0 ? ec : list.getBackground());
                }
                return this;
            }
        });
        return list;
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

class DnDList extends JList implements DragGestureListener, DragSourceListener, Transferable {
    private static final String NAME = "test";
    private static final Color lineColor = new Color(100,100,255);
    private final Rectangle2D targetLine = new Rectangle2D.Float();
    private int draggedIndex = -1;
    private int targetIndex  = -1;
    public DnDList() {
        super();
        //DropTarget dropTarget =
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
        //DragSource dragSource = new DragSource();
        new DragSource().createDefaultDragGestureRecognizer((Component)this, DnDConstants.ACTION_COPY_OR_MOVE, (DragGestureListener)this);
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(targetIndex>=0) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setPaint(lineColor);
            g2.fill(targetLine);
        }
    }
    private void initTargetLine(Point p) {
        Rectangle2D testArea = new Rectangle2D.Float();
        int cellHeight = (int) getCellBounds(0,0).getHeight();
        int lineWidht  = (int) getCellBounds(0,0).getWidth();
        int lineHeight = 2;
        int modelSize  = getModel().getSize();
        targetIndex = -1;
        for(int i=0;i<modelSize;i++) {
            testArea.setRect(0, cellHeight*i-cellHeight/2, lineWidht, cellHeight);
            if(testArea.contains(p)) {
                targetIndex = i;
                targetLine.setRect(0, i*cellHeight, lineWidht, lineHeight);
                break;
            }
        }
        if(targetIndex<0) {
            targetIndex = modelSize;
            targetLine.setRect(0, targetIndex*cellHeight-lineHeight, lineWidht, lineHeight);
        }
    }

    // Interface: DragGestureListener
    @Override public void dragGestureRecognized(DragGestureEvent e) {
        if(getSelectedIndices().length>1) { return; }
        draggedIndex = locationToIndex(e.getDragOrigin());
        if(draggedIndex<0) { return; }
        try{
            e.startDrag(DragSource.DefaultMoveDrop, (Transferable)this, (DragSourceListener)this);
        }catch(InvalidDnDOperationException idoe) { idoe.printStackTrace(); }
    }

    // Interface: DragSourceListener
    @Override public void dragEnter(DragSourceDragEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
    }
    @Override public void dragExit(DragSourceEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }
    @Override public void dragOver(DragSourceDragEvent e) {}
    @Override public void dropActionChanged(DragSourceDragEvent e) {}
    @Override public void dragDropEnd(DragSourceDropEvent e) {}

    // Interface: Transferable
    private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
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
        // DropTargetListener interface
        @Override public void dragExit(DropTargetEvent e) {
            targetIndex = -1;
            repaint();
        }
        @Override public void dragEnter(DropTargetDragEvent e) {
            if(isDragAcceptable(e)) {
                e.acceptDrag(e.getDropAction());
            }else{
                e.rejectDrag();
            }
        }
        @Override public void dragOver(final DropTargetDragEvent e) {
            if(isDragAcceptable(e)) {
                e.acceptDrag(e.getDropAction());
            }else{
                e.rejectDrag();
                return;
            }
            initTargetLine(e.getLocation());
            repaint();
        }
        @Override public void dropActionChanged(DropTargetDragEvent e) {
            // if(isDragAcceptable(e)) { e.acceptDrag(e.getDropAction()); }
            // else e.rejectDrag();
        }
        @SuppressWarnings("unchecked")
        @Override public void drop(DropTargetDropEvent e) {
            DefaultListModel model = (DefaultListModel)getModel();
//             Transferable t = e.getTransferable();
//             DataFlavor[] f = t.getTransferDataFlavors();
//             try{
//                 Component comp = (Component) t.getTransferData(f[0]);
//             }catch(UnsupportedFlavorException ex) {
//                     e.dropComplete(false);
//             }catch(IOException ie) {
//                 e.dropComplete(false);
//             }
            if(isDropAcceptable(e)) {
                Object str = model.getElementAt(draggedIndex);
                if(targetIndex==draggedIndex) {
                    setSelectedIndex(targetIndex);
                }else if(targetIndex<draggedIndex) {
                    model.removeElementAt(draggedIndex);
                    model.insertElementAt(str, targetIndex);
                    setSelectedIndex(targetIndex);
                }else{
                    model.insertElementAt(str, targetIndex);
                    model.removeElementAt(draggedIndex);
                    setSelectedIndex(targetIndex-1);
                }
                e.dropComplete(true);
            }else{
                e.dropComplete(false);
            }
            e.dropComplete(false);
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
