package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        DefaultTreeModel model = makeModel();
        DnDTree tree = new DnDTree();
        tree.setModel(model);
        for(int i=0;i<tree.getRowCount();i++) {
            tree.expandRow(i);
        }
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private DefaultTreeModel makeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
        DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
        DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
        set1.add(new DefaultMutableTreeNode("111111111"));
        set1.add(new DefaultMutableTreeNode("22222222222"));
        set1.add(new DefaultMutableTreeNode("33333"));
        set2.add(new DefaultMutableTreeNode("asdfasdfas"));
        set2.add(new DefaultMutableTreeNode("asdf"));
        set3.add(new DefaultMutableTreeNode("asdfasdfasdf"));
        set3.add(new DefaultMutableTreeNode("5555555555"));
        set3.add(new DefaultMutableTreeNode("66666666666666"));
        root.add(set1);
        root.add(set2);
        set2.add(set3);
        return new DefaultTreeModel(root);
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

//Java Swing Hacks - HACK #26: DnD JTree
//http://www.oreilly.co.jp/books/4873112788/
class DnDTree extends JTree {
    private TreeNode dropTargetNode = null;
    private TreeNode draggedNode = null;

    public DnDTree() {
        super();
        setCellRenderer(new DnDTreeCellRenderer());
        setModel(new DefaultTreeModel(new DefaultMutableTreeNode("default")));
        new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, new NodeDragGestureListener());
        new DropTarget(this, new NodeDropTargetListener());
    }

    private class NodeDragSourceListener implements DragSourceListener {
        @Override public void dragDropEnd(DragSourceDropEvent dsde) {
            dropTargetNode = null;
            draggedNode = null;
            repaint();
        }
        @Override public void dragEnter(DragSourceDragEvent dsde) {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
        }
        @Override public void dragExit(DragSourceEvent dse) {
            dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
        }
        @Override public void dragOver(DragSourceDragEvent dsde)          { /* not needed */ }
        @Override public void dropActionChanged(DragSourceDragEvent dsde) { /* not needed */ }
    }

    private class NodeDragGestureListener implements DragGestureListener {
        @Override public void dragGestureRecognized(DragGestureEvent dge) {
            //System.out.println("dragGestureRecognized");
            Point pt = dge.getDragOrigin();
            TreePath path = getPathForLocation(pt.x, pt.y);
            if(path==null || path.getParentPath()==null) {
                return;
            }
            //System.out.println("start "+path.toString());
            draggedNode = (TreeNode) path.getLastPathComponent();
            Transferable trans = new RJLTransferable(draggedNode);
            new DragSource().startDrag(dge, Cursor.getDefaultCursor(), trans, new NodeDragSourceListener());
        }
    }

    private class NodeDropTargetListener implements DropTargetListener {
        @Override public void dropActionChanged(DropTargetDragEvent dtde) { /* not needed */ }
        @Override public void dragEnter(DropTargetDragEvent dtde)         { /* not needed */ }
        @Override public void dragExit(DropTargetEvent dte)               { /* not needed */ }
        @Override public void dragOver(DropTargetDragEvent dtde) {
            DataFlavor[] f = dtde.getCurrentDataFlavors();
            boolean isDataFlavorSupported = f[0].getHumanPresentableName().equals(RJLTransferable.NAME);
            if(!isDataFlavorSupported) {
                //サポートされていないDataFlavorである(例えばデスクトップからファイルなど)
                rejectDrag(dtde);
                return;
            }

            // figure out which cell it's over, no drag to self
            Point pt = dtde.getLocation();
            TreePath path = getPathForLocation(pt.x, pt.y);
            if(path==null) {
                //ノード以外の場所である(例えばJTreeの余白など)
                rejectDrag(dtde);
                return;
            }
            //Object draggingObject;
            //if(!isWebStart()) {
            //    try{
            //        draggingObject = dtde.getTransferable().getTransferData(localObjectFlavor);
            //    }catch(Exception ex) {
            //        rejectDrag(dtde);
            //        return;
            //    }
            //}else{
            //    draggingObject = getSelectionPath().getLastPathComponent();
            //}
            Object draggingObject             = getSelectionPath().getLastPathComponent();
            MutableTreeNode draggingNode      = (MutableTreeNode) draggingObject;
            DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) targetNode.getParent();
            while(parentNode!=null) {
                if(draggingNode.equals(parentNode)) {
                    //親ノードを子ノードにドロップしようとしている
                    rejectDrag(dtde);
                    return;
                }
                parentNode = (DefaultMutableTreeNode)parentNode.getParent();
            }
            //dropTargetNode は、描画用(Rectangle2D、Line)のflag
            dropTargetNode = targetNode; //(TreeNode) path.getLastPathComponent();
            dtde.acceptDrag(dtde.getDropAction());
            repaint();
        }
        @Override public void drop(DropTargetDropEvent dtde) {
            //System.out.println("drop");
            //if(!isWebStart()) {
            //    try{
            //        draggingObject = dtde.getTransferable().getTransferData(localObjectFlavor);
            //    }catch(Exception ex) {
            //        rejectDrag(dtde);
            //        return;
            //    }
            //}else{
            //    draggingObject = getSelectionPath().getLastPathComponent();
            //}
            Object draggingObject  = getSelectionPath().getLastPathComponent();
            DefaultTreeModel model = (DefaultTreeModel)getModel();
            Point p = dtde.getLocation();
            TreePath path = getPathForLocation(p.x, p.y);
            if(path==null || !(draggingObject instanceof MutableTreeNode)) {
                dtde.dropComplete(false);
                return;
            }
            //System.out.println("drop path is " + path);
            MutableTreeNode draggingNode      = (MutableTreeNode) draggingObject;
            DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) targetNode.getParent();
            if(targetNode.equals(draggingNode)) {
                //自分を自分にはドロップ不可
                dtde.dropComplete(false);
                return;
            }
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            model.removeNodeFromParent(draggingNode);
            if(parentNode!=null && targetNode.isLeaf()) {
                model.insertNodeInto(draggingNode, parentNode, parentNode.getIndex(targetNode));
            }else{
                model.insertNodeInto(draggingNode, targetNode, targetNode.getChildCount());
            }
            dtde.dropComplete(true);
        }
        private void rejectDrag(DropTargetDragEvent dtde) {
            dtde.rejectDrag();
            dropTargetNode = null; // dropTargetNode(flag)をnullにして
            repaint();             // Rectangle2D、Lineを消すためJTreeを再描画
        }
    }

    private class DnDTreeCellRenderer extends DefaultTreeCellRenderer {
        private boolean isTargetNode;
        private boolean isTargetNodeLeaf;
        @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded, boolean isLeaf, int row, boolean hasFocus) {
            if(value instanceof TreeNode) {
                isTargetNode     = value.equals(dropTargetNode);
                isTargetNodeLeaf = isTargetNode && ((TreeNode)value).isLeaf();
            }
            return super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus);
        }
        @Override public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(isTargetNode) {
                g.setColor(Color.BLACK);
                if(isTargetNodeLeaf) {
                    g.drawLine(0, 0, getSize().width, 0);
                }else{
                    g.drawRect(0, 0, getSize().width-1, getSize().height-1);
                }
            }
        }
    }
}

class RJLTransferable implements Transferable {
    public static final String NAME = "TREE-TEST";
    private static final DataFlavor localObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
    //private static final DataFlavor[] supportedFlavors = { localObjectFlavor };
    private final Object object;
    public RJLTransferable(Object o) {
        object = o;
    }
    @Override public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
         if(isDataFlavorSupported(df)) {
             return object;
         }else{
             throw new UnsupportedFlavorException(df);
         }
     }
    @Override public boolean isDataFlavorSupported(DataFlavor df) {
        return df.getHumanPresentableName().equals(NAME);
        //return (df.equals(localObjectFlavor));
    }
    @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { localObjectFlavor };
    }
}
