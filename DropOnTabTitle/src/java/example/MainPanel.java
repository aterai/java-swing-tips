package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final List<JList<String>> listArray = new ArrayList<>(3);
    private final JTabbedPane jtp = new JTabbedPane();

    public MainPanel() {
        super(new BorderLayout());
        listArray.add(makeList(0));
        listArray.add(makeList(1));
        listArray.add(makeList(2));

        jtp.addTab("00000000", new JScrollPane(listArray.get(0)));
        jtp.addTab("11111111", new JScrollPane(listArray.get(1)));
        jtp.addTab("22222222", new JScrollPane(listArray.get(2)));
        add(jtp);

        new DropTarget(jtp, DnDConstants.ACTION_MOVE, new DropTargetListener() {
            private int targetTabIndex = -1;
            @Override public void dropActionChanged(DropTargetDragEvent e) {
                //repaint();
            }
            @Override public void dragExit(DropTargetEvent e) {
                //repaint();
            }
            @Override public void dragEnter(DropTargetDragEvent e) {
                //repaint();
            }
            @Override public void dragOver(DropTargetDragEvent e) {
                if (isDropAcceptable(e)) {
                    e.acceptDrag(e.getDropAction());
                } else {
                    e.rejectDrag();
                }
                repaint();
            }
            @SuppressWarnings("unchecked")
            @Override public void drop(DropTargetDropEvent e) {
                try {
                    Transferable t = e.getTransferable();
                    DataFlavor[] f = t.getTransferDataFlavors();
                    JList<String> sourceList = (JList<String>) t.getTransferData(f[0]);
                    JList<String> targetList = listArray.get(targetTabIndex);
                    DefaultListModel<String> tm = (DefaultListModel<String>) targetList.getModel();
                    DefaultListModel<String> sm = (DefaultListModel<String>) sourceList.getModel();

                    int[] indices = sourceList.getSelectedIndices();
                    for (int j = indices.length - 1; j >= 0; j--) {
                        tm.addElement(sm.remove(indices[j]));
                    }
                    e.dropComplete(true);
                } catch (UnsupportedFlavorException | IOException ie) {
                    e.dropComplete(false);
                }
            }
            private boolean isDropAcceptable(DropTargetDragEvent e) {
                Transferable t = e.getTransferable();
                DataFlavor[] f = t.getTransferDataFlavors();
                Point pt = e.getLocation();
                targetTabIndex = -1;
                for (int i = 0; i < jtp.getTabCount(); i++) {
                    if (jtp.getBoundsAt(i).contains(pt)) {
                        targetTabIndex = i;
                        break;
                    }
                }
                return targetTabIndex >= 0 && targetTabIndex != jtp.getSelectedIndex() && t.isDataFlavorSupported(f[0]);
            }
        }, true);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JList<String> makeList(int index) {
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement(index + " - 1111");
        model.addElement(index + " - 22222222");
        model.addElement(index + " - 333333333333");
        model.addElement(index + " - asdfasdfasdfasdfasd");
        model.addElement(index + " - AAAAAAAAAAAAAA");
        model.addElement(index + " - ****");
        return new DnDList<String>(model);
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

class DnDList<E> extends JList<E> implements DragGestureListener, DragSourceListener, Transferable {
    private static final String NAME = "test";
    public DnDList() {
        this(null);
    }
    public DnDList(ListModel<E> model) {
        super(model);
        DragSource dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer((Component) this, DnDConstants.ACTION_MOVE, (DragGestureListener) this);
    }

    // Interface: DragGestureListener
    @Override public void dragGestureRecognized(DragGestureEvent e) {
        try {
            e.startDrag(DragSource.DefaultMoveDrop, (Transferable) this, (DragSourceListener) this);
        } catch (InvalidDnDOperationException idoe) {
            idoe.printStackTrace();
        }
    }

    // Interface: DragSourceListener
    @Override public void dragEnter(DragSourceDragEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
    }
    @Override public void dragExit(DragSourceEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }
    @Override public void dragOver(DragSourceDragEvent e)          { /* not needed */ }
    @Override public void dragDropEnd(DragSourceDropEvent e)       { /* not needed */ }
    @Override public void dropActionChanged(DragSourceDragEvent e) { /* not needed */ }

    // Interface: Transferable
    //private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
    //private final DataFlavor FLAVOR = new DataFlavor(Object.class, DataFlavor.javaJVMLocalObjectMimeType);
    @Override public Object getTransferData(DataFlavor flavor) {
        return this;
    }
    @Override public DataFlavor[] getTransferDataFlavors() {
        DefaultListModel m = (DefaultListModel) getModel();
        DataFlavor[] f = new DataFlavor[m.size()];
        for (int i = 0; i < m.size(); i++) {
            //f[i] = new DataFlavor(Object.class, DataFlavor.javaJVMLocalObjectMimeType);
            f[i] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
        }
        return f;
    }
    @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.getHumanPresentableName().equals(NAME);
        //return flavor.getRepresentationClass().equals(Object.class);
    }
}
