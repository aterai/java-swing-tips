// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class CheckBoxStatusUpdateListener implements TreeModelListener {
  private boolean adjusting;

  @Override public void treeNodesChanged(TreeModelEvent e) {
    if (adjusting) {
      return;
    }
    adjusting = true;

    DefaultTreeModel model = (DefaultTreeModel) e.getSource();
    // https://docs.oracle.com/javase/8/docs/api/javax/swing/event/TreeModelListener.html#treeNodesChanged-javax.swing.event.TreeModelEvent-
    // To indicate the root has changed, childIndices and children will be null.
    Object[] children = e.getChildren();
    boolean isRoot = Objects.isNull(children);

    // If the parent node exists, update its status
    if (!isRoot) {
      TreePath parent = e.getTreePath();
      DefaultMutableTreeNode n = (DefaultMutableTreeNode) parent.getLastPathComponent();
      while (Objects.nonNull(n)) {
        updateParentUserObject(n);
        TreeNode tmp = n.getParent();
        if (tmp instanceof DefaultMutableTreeNode) {
          n = (DefaultMutableTreeNode) tmp;
        } else {
          break;
        }
      }
      model.nodeChanged(n);
    }

    // Update the status of all child nodes to be the same as the current node status
    boolean isOnlyOneNodeSelected = Objects.nonNull(children) && children.length == 1;
    Object current = isOnlyOneNodeSelected ? children[0] : model.getRoot();
    if (current instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) current;
      CheckBoxNode c = (CheckBoxNode) node.getUserObject();
      updateAllChildrenUserObject(node, c.getStatus());
      model.nodeChanged(node);
    }

    adjusting = false;
  }

  // private void updateParentUserObject(DefaultMutableTreeNode parent) {
  //   int selectedCount = 0;
  //   // Java 9: Enumeration<TreeNode> children = parent.children();
  //   Enumeration<?> children = parent.children();
  //   while (children.hasMoreElements()) {
  //     DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
  //     CheckBoxNode check = (CheckBoxNode) node.getUserObject();
  //     if (check.getStatus() == Status.INDETERMINATE) {
  //       selectedCount = -1;
  //       break;
  //     }
  //     if (check.getStatus() == Status.SELECTED) {
  //       selectedCount++;
  //     }
  //   }
  //   Object o = parent.getUserObject();
  //   if (o instanceof CheckBoxNode) {
  //     File file = ((CheckBoxNode) o).getFile();
  //     if (selectedCount == 0) {
  //       parent.setUserObject(new CheckBoxNode(file, Status.DESELECTED));
  //     } else if (selectedCount == parent.getChildCount()) {
  //       parent.setUserObject(new CheckBoxNode(file, Status.SELECTED));
  //     } else {
  //       parent.setUserObject(new CheckBoxNode(file));
  //     }
  //   }
  // }

  private void updateParentUserObject(DefaultMutableTreeNode parent) {
    // Java 9: Collections.list(parent.children()).stream()
    List<Status> list = Collections.list((Enumeration<?>) parent.children()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .map(DefaultMutableTreeNode::getUserObject)
        .filter(CheckBoxNode.class::isInstance)
        .map(CheckBoxNode.class::cast)
        .map(CheckBoxNode::getStatus)
        .collect(Collectors.toList());

    Object o = parent.getUserObject();
    if (o instanceof CheckBoxNode) {
      File file = ((CheckBoxNode) o).getFile();
      if (list.stream().allMatch(s -> s == Status.DESELECTED)) {
        parent.setUserObject(new CheckBoxNode(file, Status.DESELECTED));
      } else if (list.stream().allMatch(s -> s == Status.SELECTED)) {
        parent.setUserObject(new CheckBoxNode(file, Status.SELECTED));
      } else {
        parent.setUserObject(new CheckBoxNode(file, Status.INDETERMINATE));
      }
    }
  }

  private void updateAllChildrenUserObject(DefaultMutableTreeNode parent, Status status) {
    // Java 9: Collections.list(parent.breadthFirstEnumeration()).stream()
    Collections.list((Enumeration<?>) parent.breadthFirstEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(node -> !Objects.equals(parent, node))
        .forEach(node -> {
          CheckBoxNode check = (CheckBoxNode) node.getUserObject();
          node.setUserObject(new CheckBoxNode(check.getFile(), status));
        });
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
