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
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    TreeTransferHandler handler = new TreeTransferHandler();
    add(new JScrollPane(makeTree(handler)));
    add(new JScrollPane(makeTree(handler)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTree makeTree(TransferHandler handler) {
    JTree tree = new JTree();
    tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    tree.setRootVisible(false);
    tree.setDragEnabled(true);
    tree.setTransferHandler(handler);
    tree.setDropMode(DropMode.INSERT);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    // Disable node Cut action
    Action empty = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        /* do nothing */
      }
    };
    tree.getActionMap().put(TransferHandler.getCutAction().getValue(Action.NAME), empty);

    expandTree(tree);
    return tree;
  }

  private static void expandTree(JTree tree) {
    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
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
  // protected static final DataFlavor FLAVOR = new ActivationDataFlavor(
  //     DefaultMutableTreeNode[].class,
  //     DataFlavor.javaJVMLocalObjectMimeType,
  //     "Array of TreeNode");
  private static final String NAME = "List of DefaultMutableTreeNode";
  protected static final DataFlavor FLAVOR = new DataFlavor(List.class, NAME);
  private JTree source;

  @Override protected Transferable createTransferable(JComponent c) {
    source = (JTree) c;
    String msg = "SelectionPaths is null";
    TreePath[] paths = Objects.requireNonNull(source.getSelectionPaths(), msg);
    List<Object> nodes = Arrays.stream(paths)
        .map(TreePath::getLastPathComponent)
        .collect(Collectors.toList()); // Java 16: .toList();
    // List<Object> nodes = new ArrayList<>(paths.length);
    // for (TreePath path : paths) {
    //   nodes.add(path.getLastPathComponent());
    // }
    // return new DataHandler(nodes, FLAVOR.getMimeType());
    return new Transferable() {
      @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {FLAVOR};
      }

      @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Objects.equals(FLAVOR, flavor);
      }

      @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
          return nodes;
        } else {
          throw new UnsupportedFlavorException(flavor);
        }
      }
    };
  }

  @Override public int getSourceActions(JComponent c) {
    return MOVE;
  }

  @Override public boolean canImport(TransferSupport support) {
    boolean equals = Objects.equals(source, support.getComponent());
    return !equals && support.isDrop() && support.isDataFlavorSupported(FLAVOR);
  }

  @Override public boolean importData(TransferSupport support) {
    List<?> nodes = getTransferData(support.getTransferable());
    Component c = support.getComponent();
    DropLocation dl = support.getDropLocation();
    if (c instanceof JTree && dl instanceof JTree.DropLocation) {
      insertNode((JTree) c, (JTree.DropLocation) dl, nodes);
    }
    return !nodes.isEmpty();
  }

  private static void insertNode(JTree tree, JTree.DropLocation dl, List<?> nodes) {
    int childIndex = dl.getChildIndex();
    TreePath path = dl.getPath();
    DefaultMutableTreeNode tgt = (DefaultMutableTreeNode) path.getLastPathComponent();
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    AtomicInteger idx = new AtomicInteger(childIndex < 0 ? tgt.getChildCount() : childIndex);
    nodes.stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .forEach(n -> {
          DefaultMutableTreeNode clone = new DefaultMutableTreeNode(n.getUserObject());
          model.insertNodeInto(deepCopy(n, clone), tgt, idx.getAndIncrement());
        });
  }

  private static List<?> getTransferData(Transferable transferable) {
    List<?> nodes;
    try {
      nodes = (List<?>) transferable.getTransferData(FLAVOR);
    } catch (UnsupportedFlavorException | IOException ex) {
      nodes = Collections.emptyList();
    }
    return nodes;
  }

  private static MutableTreeNode deepCopy(TreeNode src, DefaultMutableTreeNode tgt) {
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
    // for (int i = 0; i < src.getChildCount(); i++) {
    //   DefaultMutableTreeNode node = (DefaultMutableTreeNode) src.getChildAt(i);
    //   DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
    //   // DefaultMutableTreeNode clone = (DefaultMutableTreeNode) node.clone();
    //   tgt.add(clone);
    //   if (!node.isLeaf()) {
    //     deepCopyTree(node, clone);
    //   }
    // }
    return tgt;
  }

  @Override protected void exportDone(JComponent src, Transferable data, int action) {
    if (action == MOVE && src instanceof JTree) {
      JTree tree = (JTree) src;
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      TreePath[] selectionPaths = tree.getSelectionPaths();
      if (selectionPaths != null) {
        for (TreePath path : selectionPaths) {
          model.removeNodeFromParent((MutableTreeNode) path.getLastPathComponent());
        }
      }
    }
  }
}
