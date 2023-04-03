// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private final JTextField field = new JTextField("foo");
  private final JTree tree = new JTree();

  private MainPanel() {
    super(new BorderLayout(5, 5));
    field.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        fireDocumentChangeEvent();
      }

      @Override public void removeUpdate(DocumentEvent e) {
        fireDocumentChangeEvent();
      }

      @Override public void changedUpdate(DocumentEvent e) {
        /* not needed */
      }
    });
    JPanel north = new JPanel(new BorderLayout());
    north.add(field);
    north.setBorder(BorderFactory.createTitledBorder("Tree filter"));

    tree.setRowHeight(-1);
    TreeModel model = tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    // Java 9: Collections.list(root.breadthFirstEnumeration()).stream()
    Collections.list((Enumeration<?>) root.breadthFirstEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .forEach(this::setUserObject);

    model.addTreeModelListener(new FilterableStatusUpdateListener());

    tree.setCellRenderer(new FilterTreeCellRenderer());
    fireDocumentChangeEvent();

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(north, BorderLayout.NORTH);
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  private void setUserObject(DefaultMutableTreeNode node) {
    String str = Objects.toString(node.getUserObject(), "");
    node.setUserObject(new FilterableNode(str));
  }

  public void fireDocumentChangeEvent() {
    String q = field.getText();
    TreePath rtp = tree.getPathForRow(0);
    if (q.isEmpty()) {
      TreeUtils.resetAll(rtp, true);
      ((DefaultTreeModel) tree.getModel()).reload();
      // TreeUtils.visitAll(tree, rtp, true);
    } else {
      TreeUtils.visitAll(tree, rtp, false);
      TreeUtils.searchTree(tree, rtp, q);
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

class FilterableNode {
  public final String label;
  protected boolean status;

  protected FilterableNode(String label) {
    this.label = label;
    status = false;
  }

  protected FilterableNode(String label, boolean status) {
    this.label = label;
    this.status = status;
  }

  @Override public String toString() {
    return label;
  }
}

class FilterableStatusUpdateListener implements TreeModelListener {
  private final AtomicBoolean adjusting = new AtomicBoolean();

  @Override public void treeNodesChanged(TreeModelEvent e) {
    if (adjusting.get()) {
      return;
    }
    adjusting.set(true);
    Object[] children = e.getChildren();
    DefaultTreeModel model = (DefaultTreeModel) e.getSource();

    DefaultMutableTreeNode node;
    FilterableNode c;
    if (Objects.nonNull(children) && children.length == 1) {
      node = (DefaultMutableTreeNode) children[0];
      c = (FilterableNode) node.getUserObject();
      TreePath parent = e.getTreePath();
      DefaultMutableTreeNode n = (DefaultMutableTreeNode) parent.getLastPathComponent();
      while (Objects.nonNull(n)) {
        updateParentUserObject(n);
        DefaultMutableTreeNode tmp = (DefaultMutableTreeNode) n.getParent();
        if (Objects.nonNull(tmp)) {
          n = tmp;
        } else {
          break;
        }
      }
      model.nodeChanged(n);
    } else {
      node = (DefaultMutableTreeNode) model.getRoot();
      c = (FilterableNode) node.getUserObject();
    }
    updateAllChildrenUserObject(node, c.status);
    model.nodeChanged(node);
    adjusting.set(false);
  }

  private void updateParentUserObject(DefaultMutableTreeNode parent) {
    FilterableNode uo = (FilterableNode) parent.getUserObject();
    // Java 9: Collections.list(node.children()).stream()
    uo.status = Collections.list((Enumeration<?>) parent.children()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .map(DefaultMutableTreeNode::getUserObject)
        .filter(FilterableNode.class::isInstance)
        .map(FilterableNode.class::cast)
        .anyMatch(c -> c.status);
    // // Java 9: Enumeration<TreeNode> children = parent.children();
    // Enumeration<?> children = parent.children();
    // while (children.hasMoreElements()) {
    //   DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
    //   FilterableNode check = (FilterableNode) node.getUserObject();
    //   if (check.status) {
    //     uo.status = true;
    //     return;
    //   }
    // }
    // uo.status = false;
  }

  private void updateAllChildrenUserObject(DefaultMutableTreeNode root, boolean match) {
    Collections.list((Enumeration<?>) root.breadthFirstEnumeration()).stream()
        .filter(n -> !Objects.equals(n, root))
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .map(DefaultMutableTreeNode::getUserObject)
        .filter(FilterableNode.class::isInstance)
        .map(FilterableNode.class::cast)
        .forEach(n -> n.status = match);
    // Enumeration<?> breadth = root.breadthFirstEnumeration();
    // while (breadth.hasMoreElements()) {
    //   DefaultMutableTreeNode node = (DefaultMutableTreeNode) breadth.nextElement();
    //   if (Objects.equals(root, node)) {
    //     continue;
    //   }
    //   FilterableNode uo = (FilterableNode) node.getUserObject();
    //   uo.status = match;
    // }
  }

  @Override public void treeNodesInserted(TreeModelEvent e) {
    /* not needed */
  }

  @Override public void treeNodesRemoved(TreeModelEvent e) {
    /* not needed */
  }

  @Override public void treeStructureChanged(TreeModelEvent e) {
    /* not needed */
  }
}

class FilterTreeCellRenderer extends DefaultTreeCellRenderer {
  private final JLabel emptyLabel = new JLabel();

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c = super.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    FilterableNode uo = (FilterableNode) node.getUserObject();
    return uo.status ? c : emptyLabel;
  }
}

final class TreeUtils {
  private TreeUtils() {
    /* Singleton */
  }

  public static void searchTree(JTree tree, TreePath path, String q) {
    Object o = path.getLastPathComponent();
    if (o instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
      FilterableNode uo = (FilterableNode) node.getUserObject();
      uo.status = node.toString().startsWith(q);
      ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
      if (uo.status) {
        tree.expandPath(node.isLeaf() ? path.getParentPath() : path);
      }
      if (!uo.status && !node.isLeaf()) {
        // Java 9: Collections.list(node.children()).stream()
        Collections.list((Enumeration<?>) node.children()).stream()
            .filter(TreeNode.class::isInstance).map(TreeNode.class::cast)
            .forEach(n -> searchTree(tree, path.pathByAddingChild(n), q));
      }
    }
  }

  public static void resetAll(TreePath parent, boolean match) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
    FilterableNode uo = (FilterableNode) node.getUserObject();
    uo.status = match;
    if (!node.isLeaf()) {
      // Java 9: Collections.list(node.children())
      Collections.list((Enumeration<?>) node.children())
          .forEach(n -> resetAll(parent.pathByAddingChild(n), match));
    }
  }

  public static void visitAll(JTree tree, TreePath parent, boolean expand) {
    TreeNode node = (TreeNode) parent.getLastPathComponent();
    if (!node.isLeaf()) {
      // Java 9: Collections.list(node.children())
      Collections.list((Enumeration<?>) node.children())
          .forEach(n -> visitAll(tree, parent.pathByAddingChild(n), expand));
    }
    if (expand) {
      tree.expandPath(parent);
    } else {
      tree.collapsePath(parent);
    }
  }
}
