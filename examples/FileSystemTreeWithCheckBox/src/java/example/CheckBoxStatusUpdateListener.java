// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public final class CheckBoxStatusUpdateListener implements TreeModelListener {
  private final AtomicBoolean adjusting = new AtomicBoolean();

  @Override public void treeNodesChanged(TreeModelEvent e) {
    // https://docs.oracle.com/javase/8/docs/api/javax/swing/event/TreeModelListener.html#treeNodesChanged-javax.swing.event.TreeModelEvent-
    // To indicate the root has changed, childIndices and children will be null.
    // The root of this sample has no CheckBoxNode, so there is nothing to update.
    Object[] children = e.getChildren();
    if (adjusting.get() || Objects.isNull(children)) {
      return;
    }
    adjusting.set(true);
    try {
      DefaultTreeModel model = (DefaultTreeModel) e.getSource();
      // Update the status of all descendant nodes to be the same as the changed node status
      for (Object child : children) {
        if (child instanceof DefaultMutableTreeNode) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) child;
          Object userObject = node.getUserObject();
          if (userObject instanceof CheckBoxNode) {
            updateAllChildrenUserObject(node, ((CheckBoxNode) userObject).getStatus());
            model.nodeChanged(node);
          }
        }
      }
      // Update the status of the ancestor nodes from the nearest parent up to the root
      for (TreePath path = e.getTreePath(); Objects.nonNull(path); path = path.getParentPath()) {
        Object o = path.getLastPathComponent();
        if (o instanceof DefaultMutableTreeNode) {
          DefaultMutableTreeNode parent = (DefaultMutableTreeNode) o;
          updateParentUserObject(parent);
          model.nodeChanged(parent);
        }
      }
    } finally {
      // Always reset the flag, otherwise this listener stops working after an exception
      adjusting.set(false);
    }
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
    Object o = parent.getUserObject();
    if (o instanceof CheckBoxNode) {
      // Java 9: Collections.list(parent.children()).stream()
      Set<Status> set = Collections.list((Enumeration<?>) parent.children()).stream()
          .filter(DefaultMutableTreeNode.class::isInstance)
          .map(DefaultMutableTreeNode.class::cast)
          .map(DefaultMutableTreeNode::getUserObject)
          .filter(CheckBoxNode.class::isInstance)
          .map(CheckBoxNode.class::cast)
          .map(CheckBoxNode::getStatus)
          .collect(Collectors.toCollection(() -> EnumSet.noneOf(Status.class)));
      // SELECTED or DESELECTED only if all children have that same status
      Status status = set.size() == 1 ? set.iterator().next() : Status.INDETERMINATE;
      File file = ((CheckBoxNode) o).getFile();
      parent.setUserObject(new CheckBoxNode(file, status));
    }
  }

  private void updateAllChildrenUserObject(DefaultMutableTreeNode parent, Status status) {
    // Java 9: Collections.list(parent.breadthFirstEnumeration()).stream()
    Collections.list((Enumeration<?>) parent.breadthFirstEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(node -> !Objects.equals(parent, node))
        .forEach(node -> {
          Object o = node.getUserObject();
          if (o instanceof CheckBoxNode) {
            node.setUserObject(new CheckBoxNode(((CheckBoxNode) o).getFile(), status));
          }
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
