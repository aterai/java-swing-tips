// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new DnDTree();
    tree.setModel(makeModel());
    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
    }
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultTreeModel makeModel() {
    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
    set1.add(new DefaultMutableTreeNode("111111111"));
    set1.add(new DefaultMutableTreeNode("22222222222"));
    set1.add(new DefaultMutableTreeNode("33333"));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
    set2.add(new DefaultMutableTreeNode("asdfasdfas"));
    set2.add(new DefaultMutableTreeNode("asdf"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
    set3.add(new DefaultMutableTreeNode("asdfasdfasdf"));
    set3.add(new DefaultMutableTreeNode("5555555555"));
    set3.add(new DefaultMutableTreeNode("66666666666666"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    root.add(set1);
    root.add(set2);
    set2.add(set3);
    return new DefaultTreeModel(root);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// Java Swing Hacks - HACK #26: DnD JTree
// https://www.oreilly.co.jp/books/4873112788/
class DnDTree extends JTree {
  private transient DragGestureRecognizer dragGestureRecognizer;
  private transient DropTarget dropTarget;
  protected TreeNode dropTargetNode;
  protected TreeNode draggedNode;

  @Override public void updateUI() {
    setCellRenderer(null);
    super.updateUI();
    setCellRenderer(new DnDTreeCellRenderer());
    if (Objects.isNull(dragGestureRecognizer) || Objects.isNull(dropTarget)) {
      dragGestureRecognizer = DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
        this, DnDConstants.ACTION_MOVE, new NodeDragGestureListener());
      dropTarget = new DropTarget(this, new NodeDropTargetListener());
    }
  }

  private class NodeDragGestureListener implements DragGestureListener {
    @Override public void dragGestureRecognized(DragGestureEvent dge) {
      // System.out.println("dragGestureRecognized");
      Point pt = dge.getDragOrigin();
      TreePath path = getPathForLocation(pt.x, pt.y);
      if (Objects.isNull(path) || Objects.isNull(path.getParentPath())) {
        return;
      }
      // System.out.println("start " + path.toString());
      draggedNode = (TreeNode) path.getLastPathComponent();
      Transferable trans = new TreeNodeTransferable(draggedNode);
      DragSource.getDefaultDragSource().startDrag(dge, Cursor.getDefaultCursor(), trans, new NodeDragSourceListener());
    }
  }

  private class NodeDropTargetListener implements DropTargetListener {
    @Override public void dropActionChanged(DropTargetDragEvent dtde) {
      /* not needed */
    }

    @Override public void dragEnter(DropTargetDragEvent dtde) {
      /* not needed */
    }

    @Override public void dragExit(DropTargetEvent dte) {
      /* not needed */
    }

    @Override public void dragOver(DropTargetDragEvent dtde) {
      DataFlavor[] f = dtde.getCurrentDataFlavors();
      boolean isDataFlavorSupported = f[0].getHumanPresentableName().equals(TreeNodeTransferable.NAME);
      if (!isDataFlavorSupported) {
        // This DataFlavor is not supported(e.g. files from the desktop)
        rejectDrag(dtde);
        return;
      }

      // figure out which cell it's over, no drag to self
      Point pt = dtde.getLocation();
      TreePath path = getPathForLocation(pt.x, pt.y);
      if (Objects.isNull(path)) {
        // Dropped into the non-node locations(e.g. margin area of JTree)
        rejectDrag(dtde);
        return;
      }
      // Object draggingObject;
      // if (!isWebStart()) {
      //   try {
      //     draggingObject = dtde.getTransferable().getTransferData(FLAVOR);
      //   } catch (Exception ex) {
      //     rejectDrag(dtde);
      //     return;
      //   }
      // } else {
      //   draggingObject = getSelectionPath().getLastPathComponent();
      // }
      // MutableTreeNode draggingNode = (MutableTreeNode) draggingObject;

      MutableTreeNode draggingNode = (MutableTreeNode) getSelectionPath().getLastPathComponent();
      DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();

      TreeNode parent = targetNode.getParent();
      if (parent instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode ancestor = (DefaultMutableTreeNode) parent;
        if (Arrays.asList(ancestor.getPath()).contains(draggingNode)) {
          // Trying to drop a parent node to a child node
          rejectDrag(dtde);
          return;
        }
      }
      dropTargetNode = targetNode; // (TreeNode) path.getLastPathComponent();
      dtde.acceptDrag(dtde.getDropAction());
      repaint();
    }

    @Override public void drop(DropTargetDropEvent dtde) {
      // System.out.println("drop");
      // if (!isWebStart()) {
      //   try {
      //     draggingObject = dtde.getTransferable().getTransferData(FLAVOR);
      //   } catch (Exception ex) {
      //     rejectDrag(dtde);
      //     return;
      //   }
      // } else {
      //   draggingObject = getSelectionPath().getLastPathComponent();
      // }
      Object draggingObject = getSelectionPath().getLastPathComponent();
      Point pt = dtde.getLocation();
      TreePath path = getPathForLocation(pt.x, pt.y);
      if (Objects.isNull(path) || !(draggingObject instanceof MutableTreeNode)) {
        dtde.dropComplete(false);
        return;
      }
      // System.out.println("drop path is " + path);
      MutableTreeNode draggingNode = (MutableTreeNode) draggingObject;
      DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();
      if (targetNode.equals(draggingNode)) {
        // Cannot move the node to the node itself
        dtde.dropComplete(false);
        return;
      }
      dtde.acceptDrop(DnDConstants.ACTION_MOVE);

      DefaultTreeModel model = (DefaultTreeModel) getModel();
      model.removeNodeFromParent(draggingNode);

      TreeNode parent = targetNode.getParent();
      if (parent instanceof MutableTreeNode && targetNode.isLeaf()) {
        model.insertNodeInto(draggingNode, (MutableTreeNode) parent, parent.getIndex(targetNode));
      } else {
        model.insertNodeInto(draggingNode, targetNode, targetNode.getChildCount());
      }
      dtde.dropComplete(true);

      dropTargetNode = null;
      draggedNode = null;
      repaint();
    }

    private void rejectDrag(DropTargetDragEvent dtde) {
      dtde.rejectDrag();
      dropTargetNode = null; // dropTargetNode as null,
      repaint();             // and repaint the JTree(turn off the Rectangle2D and Line2D)
    }
  }

  private class DnDTreeCellRenderer extends DefaultTreeCellRenderer {
    private boolean isTargetNode;
    private boolean isTargetNodeLeaf;

    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      if (value instanceof TreeNode) {
        isTargetNode = value.equals(dropTargetNode);
        isTargetNodeLeaf = isTargetNode && ((TreeNode) value).isLeaf();
      }
      return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

    @Override protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (isTargetNode) {
        g.setColor(Color.BLACK);
        if (isTargetNodeLeaf) {
          g.drawLine(0, 0, getSize().width, 0);
        } else {
          g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
        }
      }
    }
  }
}

class TreeNodeTransferable implements Transferable {
  public static final String NAME = "TREE-TEST";
  private static final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
  // private static final DataFlavor[] supportedFlavors = {FLAVOR};
  private final Object object;

  protected TreeNodeTransferable(Object o) {
    object = o;
  }

  @Override public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
    if (isDataFlavorSupported(df)) {
      return object;
    } else {
      throw new UnsupportedFlavorException(df);
    }
  }

  @Override public boolean isDataFlavorSupported(DataFlavor df) {
    return df.getHumanPresentableName().equals(NAME);
    // return (df.equals(FLAVOR));
  }

  @Override public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {FLAVOR};
  }
}

class NodeDragSourceListener implements DragSourceListener {
  @Override public void dragDropEnd(DragSourceDropEvent dsde) {
    // dropTargetNode = null;
    // draggedNode = null;
    // repaint();
  }

  @Override public void dragEnter(DragSourceDragEvent dsde) {
    dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
  }

  @Override public void dragExit(DragSourceEvent dse) {
    dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
  }

  @Override public void dragOver(DragSourceDragEvent dsde) {
    /* not needed */
  }

  @Override public void dropActionChanged(DragSourceDragEvent dsde) {
    /* not needed */
  }
}
