package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
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
import javax.swing.tree.TreePath;

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
        updateAllChildrenUserObject(node, c.getStatus());
        model.nodeChanged(node);
        adjusting = false;
    }
    // private void updateParentUserObject(DefaultMutableTreeNode parent) {
    //     int selectedCount = 0;
    //     // Java 9: Enumeration<TreeNode> children = parent.children();
    //     Enumeration<?> children = parent.children();
    //     while (children.hasMoreElements()) {
    //         DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
    //         CheckBoxNode check = (CheckBoxNode) node.getUserObject();
    //         if (check.getStatus() == Status.INDETERMINATE) {
    //             selectedCount = -1;
    //             break;
    //         }
    //         if (check.getStatus() == Status.SELECTED) {
    //             selectedCount++;
    //         }
    //     }
    //     Object o = parent.getUserObject();
    //     if (o instanceof CheckBoxNode) {
    //         File file = ((CheckBoxNode) o).getFile();
    //         if (selectedCount == 0) {
    //             parent.setUserObject(new CheckBoxNode(file, Status.DESELECTED));
    //         } else if (selectedCount == parent.getChildCount()) {
    //             parent.setUserObject(new CheckBoxNode(file, Status.SELECTED));
    //         } else {
    //             parent.setUserObject(new CheckBoxNode(file));
    //         }
    //     }
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
            if (list.stream().allMatch(s -> Objects.equals(s, Status.DESELECTED))) {
                parent.setUserObject(new CheckBoxNode(file, Status.DESELECTED));
            } else if (list.stream().allMatch(s -> Objects.equals(s, Status.SELECTED))) {
                parent.setUserObject(new CheckBoxNode(file, Status.SELECTED));
            } else {
                parent.setUserObject(new CheckBoxNode(file, Status.INDETERMINATE));
            }
        }
    }
    private void updateAllChildrenUserObject(DefaultMutableTreeNode root, Status status) {
        // Java 9: Collections.list(root.breadthFirstEnumeration()).stream()
        Collections.list((Enumeration<?>) root.breadthFirstEnumeration()).stream()
            .filter(DefaultMutableTreeNode.class::isInstance)
            .map(DefaultMutableTreeNode.class::cast)
            .filter(node -> !Objects.equals(root, node))
            .forEach(node -> {
                CheckBoxNode check = (CheckBoxNode) node.getUserObject();
                node.setUserObject(new CheckBoxNode(check.getFile(), status));
            });
    }
    @Override public void treeNodesInserted(TreeModelEvent e) { /* not needed */ }
    @Override public void treeNodesRemoved(TreeModelEvent e) { /* not needed */ }
    @Override public void treeStructureChanged(TreeModelEvent e) { /* not needed */ }
}
