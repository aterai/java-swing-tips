// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(2, 2));
    JTree tree = new JTree();
    TreeSelectionModel sm = tree.getSelectionModel();
    sm.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    JPopupMenu popup = new TreePopupMenu();
    tree.setComponentPopupMenu(popup);
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
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

final class TreePopupMenu extends JPopupMenu {
  private final JTextField field = new JTextField(24) {
    private transient AncestorListener listener;
    @Override public void updateUI() {
      removeAncestorListener(listener);
      super.updateUI();
      listener = new FocusAncestorListener();
      addAncestorListener(listener);
    }
  };
  private TreePath path;

  /* default */ TreePopupMenu() {
    super();
    add("getCommonParent").addActionListener(e -> showCommonParent());
    addSeparator();
    add("add").addActionListener(e -> addNode());
    add("add & reload").addActionListener(e -> addAndReload());
    add("edit").addActionListener(e -> edit());
    addSeparator();
    add("remove").addActionListener(e -> remove());
  }

  private void showCommonParent() {
    JTree tree = (JTree) getInvoker();
    Optional.ofNullable(tree.getSelectionPaths())
        .filter(paths -> paths.length > 1)
        .map(TreeUtils::findCommonParent)
        .ifPresent(p -> {
          Object node = p.getLastPathComponent();
          String title = "common parent";
          JOptionPane.showMessageDialog(
              tree, node, title, JOptionPane.INFORMATION_MESSAGE);
        });
  }

  private void addNode() {
    JTree tree = (JTree) getInvoker();
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    MutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
    DefaultMutableTreeNode child = new DefaultMutableTreeNode("New node");
    model.insertNodeInto(child, parent, parent.getChildCount());
    tree.scrollPathToVisible(new TreePath(child.getPath()));
  }

  private void addAndReload() {
    JTree tree = (JTree) getInvoker();
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
    DefaultMutableTreeNode child = new DefaultMutableTreeNode("New node");
    parent.add(child);
    model.reload(parent); // = model.nodeStructureChanged(parent);
    tree.scrollPathToVisible(new TreePath(child.getPath()));
  }

  private void edit() {
    Object node = path.getLastPathComponent();
    if (node instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) node;
      field.setText(leaf.getUserObject().toString());
      JTree tree = (JTree) getInvoker();
      int ret = JOptionPane.showConfirmDialog(
          tree, field, "edit", JOptionPane.YES_NO_OPTION);
      if (ret == JOptionPane.OK_OPTION) {
        tree.getModel().valueForPathChanged(path, field.getText());
      }
    }
  }

  private void remove() {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
    if (!node.isRoot()) {
      JTree tree = (JTree) getInvoker();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      model.removeNodeFromParent(node);
    }
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTree) {
      JTree tree = (JTree) c;
      path = tree.getPathForLocation(x, y);
      super.show(c, x, y);
    }
  }
}

class FocusAncestorListener implements AncestorListener {
  @Override public void ancestorAdded(AncestorEvent e) {
    e.getComponent().requestFocusInWindow();
  }

  @Override public void ancestorMoved(AncestorEvent e) {
    /* not needed */
  }

  @Override public void ancestorRemoved(AncestorEvent e) {
    /* not needed */
  }
}

final class TreeUtils {
  private TreeUtils() {
    /* Singleton */
  }

  public static TreePath findCommonParent(TreePath... paths) {
    return Stream.of(paths)
        .map(TreePath::getPath)
        .reduce(TreeUtils::getCommonPath)
        .map(TreePath::new)
        .orElse(null);
  }

  @SuppressWarnings({"PMD.UseVarargs", "PMD.OnlyOneReturn", "ReturnCount"})
  public static <T> T[] getCommonPath(T[] node1, T[] node2) {
    int min = Math.min(node1.length, node2.length);
    for (int len = min; len > 0; len--) {
      T[] a1 = Arrays.copyOf(node1, len);
      T[] a2 = Arrays.copyOf(node2, len);
      if (Arrays.deepEquals(a1, a2)) {
        return a1;
      }
    }
    return Arrays.copyOf(node1, 1);
  }

  // Optional<TreeNode[]> findCommon(DefaultTreeModel model, List<TreeNode> nodes) {
  //   return nodes
  //       .stream()
  //       .map(model::getPathToRoot)
  //       .reduce(TreeUtils::getCommonPath);
  // }
}
