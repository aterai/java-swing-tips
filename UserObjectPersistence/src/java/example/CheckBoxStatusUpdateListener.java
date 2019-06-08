// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public final class CheckBoxStatusUpdateListener implements TreeModelListener {
  private boolean adjusting;

  @Override public void treeNodesChanged(TreeModelEvent e) {
    if (adjusting) {
      return;
    }
    adjusting = true;
    Object[] children = e.getChildren();
    DefaultTreeModel model = (DefaultTreeModel) e.getSource();

    DefaultMutableTreeNode node;
    CheckBoxNode c; // = (CheckBoxNode) node.getUserObject();
    boolean isNotRootAndOnlyOneNodeChanged = Objects.nonNull(children) && children.length == 1;
    if (isNotRootAndOnlyOneNodeChanged) {
      node = (DefaultMutableTreeNode) children[0];
      c = (CheckBoxNode) node.getUserObject();
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
    } else { // if (Objects.isNull(children)) {
      // https://docs.oracle.com/javase/8/docs/api/javax/swing/event/TreeModelListener.html#treeNodesChanged-javax.swing.event.TreeModelEvent-
      // To indicate the root has changed, childIndices and children will be null.
      node = (DefaultMutableTreeNode) model.getRoot();
      c = (CheckBoxNode) node.getUserObject();
    }
    updateAllChildrenUserObject(node, c.getStatus());
    model.nodeChanged(node);
    adjusting = false;
  }

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
      String label = ((CheckBoxNode) o).getLabel();
      if (list.stream().allMatch(s -> Objects.equals(s, Status.DESELECTED))) {
        parent.setUserObject(new CheckBoxNode(label, Status.DESELECTED));
      } else if (list.stream().allMatch(s -> Objects.equals(s, Status.SELECTED))) {
        parent.setUserObject(new CheckBoxNode(label, Status.SELECTED));
      } else {
        parent.setUserObject(new CheckBoxNode(label, Status.INDETERMINATE));
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
          node.setUserObject(new CheckBoxNode(check.getLabel(), status));
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

class CheckBoxNodeRenderer implements TreeCellRenderer {
  private final JPanel panel = new JPanel(new BorderLayout());
  private final TriStateCheckBox checkBox = new TriStateCheckBox();
  private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    l.setFont(tree.getFont());
    if (value instanceof DefaultMutableTreeNode) {
      panel.setFocusable(false);
      panel.setRequestFocusEnabled(false);
      panel.setOpaque(false);
      checkBox.setEnabled(tree.isEnabled());
      checkBox.setFont(tree.getFont());
      checkBox.setFocusable(false);
      checkBox.setOpaque(false);
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof CheckBoxNode) {
        CheckBoxNode node = (CheckBoxNode) userObject;
        if (node.getStatus() == Status.INDETERMINATE) {
          checkBox.setIcon(new IndeterminateIcon());
        } else {
          checkBox.setIcon(null);
        }
        l.setText(node.getLabel());
        checkBox.setSelected(node.getStatus() == Status.SELECTED);
      }
      panel.add(checkBox, BorderLayout.WEST);
      panel.add(l);
      return panel;
    }
    return l;
  }
}

class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {
  private final JPanel panel = new JPanel(new BorderLayout());
  private final TriStateCheckBox checkBox = new TriStateCheckBox() {
    private transient ActionListener handler;
    @Override public void updateUI() {
      removeActionListener(handler);
      super.updateUI();
      setOpaque(false);
      setFocusable(false);
      handler = e -> stopCellEditing();
      addActionListener(handler);
    }
  };
  private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
  private String str;

  @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
    JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
    l.setFont(tree.getFont());
    if (value instanceof DefaultMutableTreeNode) {
      panel.setFocusable(false);
      panel.setRequestFocusEnabled(false);
      panel.setOpaque(false);
      checkBox.setEnabled(tree.isEnabled());
      checkBox.setFont(tree.getFont());
      // checkBox.setFocusable(false);
      // checkBox.setOpaque(false);
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof CheckBoxNode) {
        CheckBoxNode node = (CheckBoxNode) userObject;
        if (node.getStatus() == Status.INDETERMINATE) {
          checkBox.setIcon(new IndeterminateIcon());
        } else {
          checkBox.setIcon(null);
        }
        l.setText(node.getLabel());
        checkBox.setSelected(node.getStatus() == Status.SELECTED);
        str = node.getLabel();
      }
      panel.add(checkBox, BorderLayout.WEST);
      panel.add(l);
      return panel;
    }
    return l;
  }

  @Override public Object getCellEditorValue() {
    return new CheckBoxNode(str, checkBox.isSelected() ? Status.SELECTED : Status.DESELECTED);
  }

  @Override public boolean isCellEditable(EventObject e) {
    if (e instanceof MouseEvent && e.getSource() instanceof JTree) {
      Point p = ((MouseEvent) e).getPoint();
      JTree tree = (JTree) e.getSource();
      TreePath path = tree.getPathForLocation(p.x, p.y);
      return Optional.ofNullable(tree.getPathBounds(path)).map(r -> {
        r.width = checkBox.getPreferredSize().width;
        return r.contains(p);
      }).orElse(false);
      // MouseEvent me = (MouseEvent) e;
      // JTree tree = (JTree) e.getSource();
      // TreePath path = tree.getPathForLocation(me.getX(), me.getY());
      // Rectangle r = tree.getPathBounds(path);
      // if (Objects.isNull(r)) {
      //   return false;
      // }
      // Dimension d = checkBox.getPreferredSize();
      // r.setSize(new Dimension(d.width, r.height));
      // if (r.contains(me.getPoint())) {
      //   return true;
      // }
    }
    return false;
  }
  // // AbstractCellEditor
  // @Override public boolean shouldSelectCell(EventObject anEvent) {
  //   return true;
  // }
  // @Override public boolean stopCellEditing() {
  //   fireEditingStopped();
  //   return true;
  // }
  // @Override public void cancelCellEditing() {
  //   fireEditingCanceled();
  // }
}
