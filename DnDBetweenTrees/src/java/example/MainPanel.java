package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import javax.activation.*;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(1, 2));
        TreeTransferHandler handler = new TreeTransferHandler();
        add(new JScrollPane(makeTree(handler)));
        add(new JScrollPane(makeTree(handler)));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JTree makeTree(TransferHandler hanlder) {
        JTree tree = new JTree();
        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tree.setRootVisible(false);
        tree.setDragEnabled(true);
        tree.setTransferHandler(hanlder);
        tree.setDropMode(DropMode.INSERT);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        expandTree(tree);
        return tree;
    }
    private static void expandTree(JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
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

class TreeTransferHandler extends TransferHandler {
    private static final DataFlavor FLAVOR = new ActivationDataFlavor(DefaultMutableTreeNode[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of DefaultMutableTreeNode");
    private JTree source;

    @Override protected Transferable createTransferable(JComponent c) {
        source = (JTree) c;
        TreePath[] paths = source.getSelectionPaths();
        DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[paths.length];
        for (int i = 0; i < paths.length; i++) {
            nodes[i] = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
        }
        return new DataHandler(nodes, FLAVOR.getMimeType());
    }

    @Override public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDrop()) {
            return false;
        }
        if (!support.isDataFlavorSupported(FLAVOR)) {
            return false;
        }
        JTree tree = (JTree) support.getComponent();
        return !tree.equals(source);
    }

    @Override public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        DefaultMutableTreeNode[] nodes = null;
        try {
            Transferable t = support.getTransferable();
            nodes = (DefaultMutableTreeNode[]) t.getTransferData(FLAVOR);
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }
        TransferHandler.DropLocation tdl = support.getDropLocation();
        if (tdl instanceof JTree.DropLocation) {
            JTree.DropLocation dl = (JTree.DropLocation) tdl;
            int childIndex = dl.getChildIndex();
            TreePath dest = dl.getPath();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();
            JTree tree = (JTree) support.getComponent();
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            int idx = childIndex < 0 ? parent.getChildCount() : childIndex;
            //DefaultTreeModel sm = (DefaultTreeModel) source.getModel();
            for (DefaultMutableTreeNode node: nodes) {
                //sm.removeNodeFromParent(node);
                //model.insertNodeInto(node, parent, idx++);
                DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
                model.insertNodeInto(deepCopyTreeNode(node, clone), parent, idx++);
            }
            return true;
        }
        return false;
    }
    private static DefaultMutableTreeNode deepCopyTreeNode(DefaultMutableTreeNode src, DefaultMutableTreeNode tgt) {
        for (int i = 0; i < src.getChildCount(); i++) {
            DefaultMutableTreeNode node  = (DefaultMutableTreeNode) src.getChildAt(i);
            DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
            tgt.add(clone);
            if (!node.isLeaf()) {
                deepCopyTreeNode(node, clone);
            }
        }
        return tgt;
    }
    @Override protected void exportDone(JComponent source, Transferable data, int action) {
        if (action == MOVE) {
            JTree tree = (JTree) source;
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            for (TreePath path: tree.getSelectionPaths()) {
                model.removeNodeFromParent((MutableTreeNode) path.getLastPathComponent());
            }
        }
    }
}
