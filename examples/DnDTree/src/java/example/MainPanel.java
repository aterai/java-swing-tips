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
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
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
      ex.printStackTrace();
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
class DnDTree extends JTree {
  protected transient TreeNode dropTargetNode;
  protected transient TreeNode draggedNode;
  private transient DropTarget treeDropTarget;
  private final transient DragSourceListener listener = new NodeDragSourceListener();

  @Override public void updateUI() {
    setCellRenderer(null);
    super.updateUI();
    setCellRenderer(new DnDTreeCellRenderer());
    DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
        this, DnDConstants.ACTION_MOVE, new NodeDragGestureListener());
    if (Objects.isNull(treeDropTarget)) {
      treeDropTarget = new DropTarget(this, new NodeDropTargetListener());
    }
  }

  private final class NodeDragGestureListener implements DragGestureListener {
    @Override public void dragGestureRecognized(DragGestureEvent e) {
      // System.out.println("dragGestureRecognized");
      Point pt = e.getDragOrigin();
      TreePath path = getPathForLocation(pt.x, pt.y);
      if (Objects.isNull(path) || Objects.isNull(path.getParentPath())) {
        return;
      }
      // System.out.println("start " + path.toString());
      draggedNode = (TreeNode) path.getLastPathComponent();
      Transferable transferable = new TreeNodeTransferable(draggedNode);
      Cursor cursor = Cursor.getDefaultCursor();
      DragSource.getDefaultDragSource().startDrag(e, cursor, transferable, listener);
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
      /* not needed */
    }

    @SuppressWarnings("PMD.OnlyOneReturn")
    @Override public void dragOver(DropTargetDragEvent e) {
      DataFlavor[] dataFlavors = e.getCurrentDataFlavors();
      String name = dataFlavors[0].getHumanPresentableName();
      boolean isSupported = TreeNodeTransferable.NAME.equals(name);
      if (!isSupported) {
        // This DataFlavor is not supported(e.g. files from the desktop)
        rejectDrag(e);
        return;
      }

      // figure out which cell it's over, no drag to self
      Point pt = e.getLocation();
      TreePath path = getPathForLocation(pt.x, pt.y);
      if (Objects.isNull(path)) {
        // Dropped into the non-node locations(e.g. margin area of JTree)
        rejectDrag(e);
        return;
      }
      // Object draggingObject;
      // if (!isWebStart()) {
      //   try {
      //     draggingObject = e.getTransferable().getTransferData(FLAVOR);
      //   } catch (Exception ex) {
      //     rejectDrag(e);
      //     return;
      //   }
      // } else {
      //   draggingObject = getSelectionPath().getLastPathComponent();
      // }
      // MutableTreeNode draggingNode = (MutableTreeNode) draggingObject;
      Object draggingNode = Optional.ofNullable(getSelectionPath())
          .map(TreePath::getLastPathComponent).orElse(null);
      TreeNode target = (TreeNode) path.getLastPathComponent();
      TreeNode parent = target.getParent();
      if (parent instanceof DefaultMutableTreeNode && draggingNode instanceof TreeNode) {
        DefaultMutableTreeNode ancestor = (DefaultMutableTreeNode) parent;
        if (Arrays.asList(ancestor.getPath()).contains(draggingNode)) {
          // Trying to drop a parent node to a child node
          rejectDrag(e);
          return;
        }
      }
      dropTargetNode = target; // (TreeNode) path.getLastPathComponent();
      e.acceptDrag(e.getDropAction());
      repaint();
    }

    @SuppressWarnings("PMD.NullAssignment")
    @Override public void drop(DropTargetDropEvent e) {
      // System.out.println("drop");
      // if (!isWebStart()) {
      //   try {
      //     draggingObject = e.getTransferable().getTransferData(FLAVOR);
      //   } catch (Exception ex) {
      //     rejectDrag(e);
      //     return;
      //   }
      // } else {
      //   draggingObject = getSelectionPath().getLastPathComponent();
      // }
      Object draggingObject = Optional.ofNullable(getSelectionPath())
          .map(TreePath::getLastPathComponent).orElse(null);
      Point pt = e.getLocation();
      TreePath path = getPathForLocation(pt.x, pt.y);
      if (Objects.isNull(path) || !(draggingObject instanceof MutableTreeNode)) {
        e.dropComplete(false);
        return;
      }
      // System.out.println("drop path is " + path);
      MutableTreeNode draggingNode = (MutableTreeNode) draggingObject;
      MutableTreeNode target = (MutableTreeNode) path.getLastPathComponent();
      if (target.equals(draggingNode)) {
        // Cannot move the node to the node itself
        e.dropComplete(false);
      } else {
        e.acceptDrop(DnDConstants.ACTION_MOVE);

        DefaultTreeModel model = (DefaultTreeModel) getModel();
        model.removeNodeFromParent(draggingNode);

        TreeNode parent = target.getParent();
        if (parent instanceof MutableTreeNode && target.isLeaf()) {
          model.insertNodeInto(draggingNode, (MutableTreeNode) parent, parent.getIndex(target));
        } else {
          model.insertNodeInto(draggingNode, target, target.getChildCount());
        }
        e.dropComplete(true);

        dropTargetNode = null;
        draggedNode = null;
        repaint();
      }
    }

    @SuppressWarnings("PMD.NullAssignment")
    private void rejectDrag(DropTargetDragEvent e) {
      e.rejectDrag();
      dropTargetNode = null; // dropTargetNode as null,
      repaint();             // and repaint the JTree(turn off the Rectangle2D and Line2D)
    }
  }

  private final class DnDTreeCellRenderer extends DefaultTreeCellRenderer {
    private boolean isTargetNode;
    private boolean isTargetNodeLeaf;

    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      if (value instanceof TreeNode) {
        isTargetNode = value.equals(dropTargetNode);
        isTargetNodeLeaf = isTargetNode && ((TreeNode) value).isLeaf();
      }
      return super.getTreeCellRendererComponent(
          tree, value, selected, expanded, leaf, row, hasFocus);
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
  private static final String MIME_TYPE = DataFlavor.javaJVMLocalObjectMimeType;
  private static final DataFlavor FLAVOR = new DataFlavor(MIME_TYPE, NAME);
  // private static final DataFlavor[] supportedFlavors = {FLAVOR};
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
    return NAME.equals(df.getHumanPresentableName());
    // return (df.equals(FLAVOR));
  }

  @Override public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {FLAVOR};
  }
}

class NodeDragSourceListener implements DragSourceListener {
  @Override public void dragDropEnd(DragSourceDropEvent e) {
    // dropTargetNode = null;
    // draggedNode = null;
    // repaint();
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
