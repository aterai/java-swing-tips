package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.io.File;
import java.util.*;
import javax.swing.event.*;
import javax.swing.tree.*;

class CheckBoxStatusUpdateListener implements TreeModelListener {
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
        boolean isOnlyOneNodeSelected = Objects.nonNull(children) && children.length == 1;
        if (isOnlyOneNodeSelected) {
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
        } else {
            node = (DefaultMutableTreeNode) model.getRoot();
            c = (CheckBoxNode) node.getUserObject();
        }
        updateAllChildrenUserObject(node, c.status);
        model.nodeChanged(node);
        adjusting = false;
    }
    private void updateParentUserObject(DefaultMutableTreeNode parent) {
        int selectedCount = 0;
        // Java 9: Enumeration<TreeNode> children = parent.children();
        Enumeration<?> children = parent.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
            CheckBoxNode check = (CheckBoxNode) node.getUserObject();
            if (check.status == Status.INDETERMINATE) {
                selectedCount = -1;
                break;
            }
            if (check.status == Status.SELECTED) {
                selectedCount++;
            }
        }
        Object o = parent.getUserObject();
        if (o instanceof CheckBoxNode) {
            File file = ((CheckBoxNode) o).file;
            if (selectedCount == 0) {
                parent.setUserObject(new CheckBoxNode(file, Status.DESELECTED));
            } else if (selectedCount == parent.getChildCount()) {
                parent.setUserObject(new CheckBoxNode(file, Status.SELECTED));
            } else {
                parent.setUserObject(new CheckBoxNode(file));
            }
        }
    }
    private void updateAllChildrenUserObject(DefaultMutableTreeNode root, Status status) {
        // Java 9: Enumeration<TreeNode> breadth = root.breadthFirstEnumeration();
        Enumeration<?> breadth = root.breadthFirstEnumeration();
        while (breadth.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) breadth.nextElement();
            if (Objects.equals(root, node)) {
                continue;
            }
            CheckBoxNode check = (CheckBoxNode) node.getUserObject();
            node.setUserObject(new CheckBoxNode(check.file, status));
        }
    }
    @Override public void treeNodesInserted(TreeModelEvent e) { /* not needed */ }
    @Override public void treeNodesRemoved(TreeModelEvent e) { /* not needed */ }
    @Override public void treeStructureChanged(TreeModelEvent e) { /* not needed */ }
}
