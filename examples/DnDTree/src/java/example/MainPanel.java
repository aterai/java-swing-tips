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
import java.util.Objects;
import java.util.logging.Logger;
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
    tree.setModel(createModel());
    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
    }
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultTreeModel createModel() {
    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
    set1.add(new DefaultMutableTreeNode("111111111"));
    set1.add(new DefaultMutableTreeNode("22222222222"));
    set1.add(new DefaultMutableTreeNode("33333"));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
    set2.add(new DefaultMutableTreeNode("asd fas df as"));
    set2.add(new DefaultMutableTreeNode("asd f"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
    set3.add(new DefaultMutableTreeNode("asd fas dfa sdf"));
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
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      return;
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
final class DnDTree extends JTree {
  private transient TreePath draggedPath;
  private transient TreeNode dropTargetNode;

  /* default */ DnDTree() {
    super();
    // Register the gesture recognizer and the DropTarget only once here, not in updateUI()
    DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
        this, DnDConstants.ACTION_MOVE, new NodeDragGestureListener());
    // This DropTarget replaces the one installed by the TransferHandler.
    // It is not a UIResource, so it is kept even if updateUI() is called.
    setDropTarget(new DropTarget(this, new NodeDropTargetListener()));
  }

  @Override public void updateUI() {
    setCellRenderer(null);
    super.updateUI();
    setCellRenderer(new DnDTreeCellRenderer());
  }

  private void setDropTargetNode(TreeNode node) {
    // Repaint only when the drop target node changes, not on every dragOver
    if (!Objects.equals(dropTargetNode, node)) {
      dropTargetNode = node;
      repaint();
    }
  }

  private boolean canDrop(TreePath path) {
    // TreePath#isDescendant(...) is also true for the same path,
    // so this prevents dropping a node onto itself or onto one of its descendants
    return Objects.nonNull(draggedPath) && Objects.nonNull(path)
        && path.getLastPathComponent() instanceof MutableTreeNode
        && !draggedPath.isDescendant(path);
  }

  private void moveNode(MutableTreeNode node, MutableTreeNode target) {
    DefaultTreeModel model = (DefaultTreeModel) getModel();
    model.removeNodeFromParent(node);
    TreeNode parent = target.getParent();
    if (parent instanceof MutableTreeNode && target.isLeaf()) {
      // Insert the node before the leaf target node
      model.insertNodeInto(node, (MutableTreeNode) parent, parent.getIndex(target));
    } else {
      // Append the node as the last child of the target node
      model.insertNodeInto(node, target, target.getChildCount());
    }
  }

  private final class NodeDragGestureListener implements DragGestureListener {
    @Override public void dragGestureRecognized(DragGestureEvent e) {
      Point pt = e.getDragOrigin();
      TreePath path = getPathForLocation(pt.x, pt.y);
      // The root node cannot be dragged
      boolean isNotRoot = Objects.nonNull(path) && Objects.nonNull(path.getParentPath());
      if (isNotRoot && path.getLastPathComponent() instanceof MutableTreeNode) {
        draggedPath = path;
        Transferable t = new TreeNodeTransferable(path.getLastPathComponent());
        e.startDrag(Cursor.getDefaultCursor(), t, new NodeDragSourceListener());
      }
    }
  }

  private final class NodeDropTargetListener implements DropTargetListener {
    @Override public void dropActionChanged(DropTargetDragEvent e) {
      /* not needed */
    }

    @Override public void dragEnter(DropTargetDragEvent e) {
      /* not needed */
    }

    @Override public void dragExit(DropTargetEvent e) {
      // Erase the drop location indicator when the cursor leaves the JTree
      setDropTargetNode(null);
    }

    @Override public void dragOver(DropTargetDragEvent e) {
      Point pt = e.getLocation();
      TreePath path = getPathForLocation(pt.x, pt.y);
      // Reject unsupported DataFlavors (e.g. files from the desktop),
      // non-node locations (e.g. margin area of JTree),
      // and dropping a parent node onto its child node
      if (e.isDataFlavorSupported(TreeNodeTransferable.FLAVOR) && canDrop(path)) {
        setDropTargetNode((TreeNode) path.getLastPathComponent());
        e.acceptDrag(e.getDropAction());
      } else {
        e.rejectDrag();
        setDropTargetNode(null);
      }
    }

    @Override public void drop(DropTargetDropEvent e) {
      Point pt = e.getLocation();
      TreePath path = getPathForLocation(pt.x, pt.y);
      if (e.isDataFlavorSupported(TreeNodeTransferable.FLAVOR) && canDrop(path)) {
        e.acceptDrop(DnDConstants.ACTION_MOVE);
        MutableTreeNode node = (MutableTreeNode) draggedPath.getLastPathComponent();
        moveNode(node, (MutableTreeNode) path.getLastPathComponent());
        e.dropComplete(true);
      } else {
        e.rejectDrop();
      }
      setDropTargetNode(null);
    }
  }

  private final class NodeDragSourceListener implements DragSourceListener {
    @SuppressWarnings("PMD.NullAssignment")
    @Override public void dragDropEnd(DragSourceDropEvent e) {
      draggedPath = null;
      setDropTargetNode(null);
    }

    @Override public void dragEnter(DragSourceDragEvent e) {
      e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
    }

    @Override public void dragExit(DragSourceEvent e) {
      e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }

    @Override public void dragOver(DragSourceDragEvent e) {
      /* not needed */
    }

    @Override public void dropActionChanged(DragSourceDragEvent e) {
      /* not needed */
    }
  }

  private final class DnDTreeCellRenderer extends DefaultTreeCellRenderer {
    private boolean isTargetNode;
    private boolean isTargetNodeLeaf;

    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      isTargetNode = Objects.equals(value, dropTargetNode);
      isTargetNodeLeaf = isTargetNode && leaf;
      return super.getTreeCellRendererComponent(
          tree, value, selected, expanded, leaf, row, hasFocus);
    }

    @Override protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (isTargetNode) {
        g.setColor(Color.BLACK);
        if (isTargetNodeLeaf) {
          // The dragged node will be inserted before this leaf node
          g.drawLine(0, 0, getWidth(), 0);
        } else {
          // The dragged node will be appended as a child of this node
          g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
      }
    }
  }
}

class TreeNodeTransferable implements Transferable {
  private static final String NAME = "TREE-TEST";
  private static final String MIME_TYPE = DataFlavor.javaJVMLocalObjectMimeType;
  public static final DataFlavor FLAVOR = new DataFlavor(MIME_TYPE, NAME);
  private final Object object;

  protected TreeNodeTransferable(Object o) {
    object = o;
  }

  @Override public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException {
    if (isDataFlavorSupported(df)) {
      return object;
    } else {
      throw new UnsupportedFlavorException(df);
    }
  }

  @Override public boolean isDataFlavorSupported(DataFlavor df) {
    return FLAVOR.equals(df);
  }

  @Override public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {FLAVOR};
  }
}
