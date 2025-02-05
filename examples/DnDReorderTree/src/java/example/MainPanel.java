// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new JTree();
    tree.setDragEnabled(true);
    tree.setDropMode(DropMode.ON_OR_INSERT);
    tree.setTransferHandler(new TreeTransferHandler());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
    // Disable node Cut action
    Action empty = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        /* do nothing */
      }
    };
    tree.getActionMap().put(TransferHandler.getCutAction().getValue(Action.NAME), empty);
    expandTree(tree);
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void expandTree(JTree tree) {
    int row = 0;
    while (row < tree.getRowCount()) {
      tree.expandRow(row);
      row++;
    }
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

class TreeTransferHandler extends TransferHandler {
  private final DataFlavor nodesFlavor = new DataFlavor(List.class, "List of TreeNode");

  @Override public int getSourceActions(JComponent c) {
    return c instanceof JTree && TreeUtils.canStartDrag((JTree) c) ? COPY_OR_MOVE : NONE;
  }

  @Override protected Transferable createTransferable(JComponent c) {
    Transferable transferable = null;
    if (c instanceof JTree && ((JTree) c).getSelectionPaths() != null) {
      List<MutableTreeNode> copies = new ArrayList<>();
      Arrays.stream(((JTree) c).getSelectionPaths()).forEach(path -> {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
        copies.add(TreeUtils.deepCopy(node, clone));
      });
      transferable = new Transferable() {
        @Override public DataFlavor[] getTransferDataFlavors() {
          return new DataFlavor[] {nodesFlavor};
        }

        @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
          return Objects.equals(nodesFlavor, flavor);
        }

        @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
          if (isDataFlavorSupported(flavor)) {
            return copies;
          } else {
            throw new UnsupportedFlavorException(flavor);
          }
        }
      };
    }
    return transferable;
  }

  @Override public boolean canImport(TransferSupport support) {
    DropLocation dl = support.getDropLocation();
    Component c = support.getComponent();
    return support.isDrop()
        && support.isDataFlavorSupported(nodesFlavor)
        && c instanceof JTree
        && dl instanceof JTree.DropLocation
        && TreeUtils.canImportDropLocation((JTree) c, (JTree.DropLocation) dl);
  }

  @Override public boolean importData(TransferSupport support) {
    Component c = support.getComponent();
    DropLocation dl = support.getDropLocation();
    Transferable transferable = support.getTransferable();
    return canImport(support)
        && c instanceof JTree
        && dl instanceof JTree.DropLocation
        && insertNode((JTree) c, (JTree.DropLocation) dl, transferable);
  }

  private boolean insertNode(JTree tree, JTree.DropLocation dl, Transferable transferable) {
    TreePath path = dl.getPath();
    Object p = path.getLastPathComponent();
    TreeModel m = tree.getModel();
    List<?> nodes = getTransferData(transferable);
    if (p instanceof MutableTreeNode && m instanceof DefaultTreeModel) {
      MutableTreeNode parent = (MutableTreeNode) p;
      DefaultTreeModel model = (DefaultTreeModel) m;
      int childIndex = dl.getChildIndex();
      AtomicInteger index = new AtomicInteger(getDropIndex(parent, childIndex));
      nodes.stream()
          .filter(MutableTreeNode.class::isInstance)
          .map(MutableTreeNode.class::cast)
          .forEach(n -> model.insertNodeInto(n, parent, index.getAndIncrement()));
    }
    return !nodes.isEmpty();
  }

  private static int getDropIndex(MutableTreeNode parent, int childIndex) {
    // Configure for drop mode.
    int index = childIndex; // DropMode.INSERT
    if (childIndex == -1) { // DropMode.ON
      index = parent.getChildCount();
    }
    return index;
  }

  private List<?> getTransferData(Transferable t) {
    List<?> nodes;
    try {
      nodes = (List<?>) t.getTransferData(nodesFlavor);
    } catch (UnsupportedFlavorException | IOException ex) {
      nodes = Collections.emptyList();
    }
    return nodes;
  }

  @Override protected void exportDone(JComponent src, Transferable data, int action) {
    if (src instanceof JTree && (action & MOVE) == MOVE) {
      cleanup((JTree) src);
    }
  }

  private void cleanup(JTree tree) {
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    TreePath[] selectionPaths = tree.getSelectionPaths();
    if (selectionPaths != null) {
      for (TreePath path : selectionPaths) {
        model.removeNodeFromParent((MutableTreeNode) path.getLastPathComponent());
      }
    }
  }
}

final class TreeUtils {
  private TreeUtils() {
    /* Singleton */
  }

  public static boolean canStartDrag(JTree tree) {
    TreePath[] paths = tree.getSelectionPaths();
    return paths != null && canStartDragPaths(paths);
  }

  public static boolean canStartDragPaths(TreePath... paths) {
    return Arrays.stream(paths)
        .map(TreePath::getLastPathComponent)
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .map(DefaultMutableTreeNode::getLevel)
        .distinct()
        .filter(level -> level != 0) // Level 0 is excluded because it is the root node
        .count() == 1; // All nodes are at the same level
  }

  public static boolean canImportDropLocation(JTree tree, JTree.DropLocation dl) {
    // Do not allow drop to descendant and drag-source selections
    // int dropRow = tree.getRowForPath(dl.getPath());
    Point pt = dl.getDropPoint();
    int dropRow = tree.getRowForLocation(pt.x, pt.y);
    int[] selRows = tree.getSelectionRows();
    return selRows != null && IntStream
        .of(selRows)
        .noneMatch(r -> r == dropRow || isDescendant(tree, r, dropRow));
  }

  private static boolean isDescendant(JTree tree, int selRow, int dropRow) {
    Object node = tree.getPathForRow(selRow).getLastPathComponent();
    return node instanceof DefaultMutableTreeNode
        && isDescendant2(tree, dropRow, (DefaultMutableTreeNode) node);
  }

  private static boolean isDescendant2(JTree tree, int dropRow, DefaultMutableTreeNode node) {
    Enumeration<?> e = node.depthFirstEnumeration();
    return Collections.list(e)
        .stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .map(DefaultMutableTreeNode::getPath)
        .map(TreePath::new)
        .map(tree::getRowForPath)
        .anyMatch(row -> row == dropRow);
  }

  public static DefaultMutableTreeNode deepCopy(MutableTreeNode src, DefaultMutableTreeNode tgt) {
    // Java 9: Collections.list(src.children()).stream()
    Collections.list((Enumeration<?>) src.children()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .forEach(node -> {
          DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
          tgt.add(clone);
          if (!node.isLeaf()) {
            deepCopy(node, clone);
          }
        });
    return tgt;
  }
}
