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
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
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
    add(new JScrollPane(createTree(handler)));
    add(new JScrollPane(createTree(handler)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTree createTree(TransferHandler handler) {
    JTree tree = new JTree();
    tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    tree.setRootVisible(false);
    tree.setDragEnabled(true);
    tree.setTransferHandler(handler);
    tree.setDropMode(DropMode.INSERT);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    // Disable node Cut action
    Action doNothing = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        /* do nothing */
      }
    };
    tree.getActionMap().put(TransferHandler.getCutAction().getValue(Action.NAME), doNothing);

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
    TreePath[] paths = source.getSelectionPaths();
    // An empty list is transferred when nothing is selected, and importData(...) rejects it
    List<Object> nodes = paths == null ? Collections.emptyList() : Arrays.stream(paths)
        .map(TreePath::getLastPathComponent)
        .collect(Collectors.toList()); // Java 16: .toList();
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
    boolean isSource = Objects.equals(source, support.getComponent());
    return !isSource && support.isDrop() && support.isDataFlavorSupported(FLAVOR);
  }

  @Override public boolean importData(TransferSupport support) {
    Component c = support.getComponent();
    DropLocation dl = support.getDropLocation();
    boolean imported = false;
    if (c instanceof JTree && dl instanceof JTree.DropLocation) {
      List<?> nodes = getTransferData(support.getTransferable());
      imported = insertNodes((JTree) c, (JTree.DropLocation) dl, nodes);
    }
    // Returning true lets exportDone(...) remove the nodes from the source tree,
    // so report success only when the nodes were actually inserted
    return imported;
  }

  private static boolean insertNodes(JTree tree, JTree.DropLocation dl, List<?> nodes) {
    int childIndex = dl.getChildIndex();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dl.getPath().getLastPathComponent();
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    int index = childIndex < 0 ? parent.getChildCount() : childIndex;
    boolean inserted = false;
    for (Object o : nodes) {
      if (o instanceof DefaultMutableTreeNode) {
        // Insert a copy without a parent: inserting the original node would detach it
        // from the source model, and exportDone(...) could no longer remove it
        model.insertNodeInto(deepCopy((DefaultMutableTreeNode) o), parent, index);
        index++;
        inserted = true;
      }
    }
    return inserted;
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

  private static DefaultMutableTreeNode deepCopy(DefaultMutableTreeNode src) {
    DefaultMutableTreeNode copy = new DefaultMutableTreeNode(src.getUserObject());
    for (int i = 0; i < src.getChildCount(); i++) {
      TreeNode child = src.getChildAt(i);
      if (child instanceof DefaultMutableTreeNode) {
        copy.add(deepCopy((DefaultMutableTreeNode) child));
      }
    }
    return copy;
  }

  @Override protected void exportDone(JComponent src, Transferable data, int action) {
    if (action == MOVE && src instanceof JTree) {
      // Remove the transferred nodes rather than the current selection,
      // which may have changed since the drag started
      DefaultTreeModel model = (DefaultTreeModel) ((JTree) src).getModel();
      getTransferData(data).stream()
          .filter(MutableTreeNode.class::isInstance)
          .map(MutableTreeNode.class::cast)
          .filter(node -> node.getParent() != null)
          .forEach(model::removeNodeFromParent);
    }
  }
}
