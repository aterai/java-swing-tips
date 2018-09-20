package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
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

    public MainPanel() {
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
        JPanel n = new JPanel(new BorderLayout());
        n.add(field);
        n.setBorder(BorderFactory.createTitledBorder("Tree filter"));

        tree.setRowHeight(-1);
        TreeModel model = tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        // Java 9: Collections.list(root.breadthFirstEnumeration()).stream()
        Collections.list((Enumeration<?>) root.breadthFirstEnumeration()).stream()
            .filter(DefaultMutableTreeNode.class::isInstance)
            .map(DefaultMutableTreeNode.class::cast)
            .forEach(node -> node.setUserObject(new FilterableNode(Objects.toString(node.getUserObject(), ""))));

        model.addTreeModelListener(new FilterableStatusUpdateListener());

        tree.setCellRenderer(new FilterTreeCellRenderer());
        fireDocumentChangeEvent();

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(n, BorderLayout.NORTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    protected void fireDocumentChangeEvent() {
        String q = field.getText();
        TreePath rtp = tree.getPathForRow(0);
        if (q.isEmpty()) {
            TreeUtil.resetAll(tree, rtp, true);
            ((DefaultTreeModel) tree.getModel()).reload();
            // TreeUtil.visitAll(tree, rtp, true);
        } else {
            TreeUtil.visitAll(tree, rtp, false);
            TreeUtil.searchTree(tree, rtp, q);
        }
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
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
    private boolean adjusting;
    @Override public void treeNodesChanged(TreeModelEvent e) {
        if (adjusting) {
            return;
        }
        adjusting = true;
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
        adjusting = false;
    }
    private void updateParentUserObject(DefaultMutableTreeNode parent) {
        FilterableNode uo = (FilterableNode) parent.getUserObject();
        // Java 9: Enumeration<TreeNode> children = parent.children();
        Enumeration<?> children = parent.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
            FilterableNode check = (FilterableNode) node.getUserObject();
            if (check.status) {
                uo.status = true;
                return;
            }
        }
        uo.status = false;
    }
    private void updateAllChildrenUserObject(DefaultMutableTreeNode root, boolean match) {
        Enumeration<?> breadth = root.breadthFirstEnumeration();
        while (breadth.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) breadth.nextElement();
            if (Objects.equals(root, node)) {
                continue;
            }
            FilterableNode uo = (FilterableNode) node.getUserObject();
            uo.status = match;
        }
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
        Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        FilterableNode uo = (FilterableNode) node.getUserObject();
        return uo.status ? c : emptyLabel;
    }
}

final class TreeUtil {
    private TreeUtil() { /* Singleton */ }
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
            if (!uo.status && !node.isLeaf() && node.getChildCount() >= 0) {
                // Java 9: Enumeration<TreeNode> e = node.children();
                Enumeration<?> e = node.children();
                while (e.hasMoreElements()) {
                    searchTree(tree, path.pathByAddingChild(e.nextElement()), q);
                }
            }
        }
    }
    public static void resetAll(JTree tree, TreePath parent, boolean match) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
        FilterableNode uo = (FilterableNode) node.getUserObject();
        uo.status = match;
        if (!node.isLeaf() && node.getChildCount() >= 0) {
            // Java 9: Enumeration<TreeNode> e = node.children();
            Enumeration<?> e = node.children();
            while (e.hasMoreElements()) {
                resetAll(tree, parent.pathByAddingChild(e.nextElement()), match);
            }
        }
    }
    public static void visitAll(JTree tree, TreePath parent, boolean expand) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (!node.isLeaf() && node.getChildCount() >= 0) {
            // Java 9: Enumeration<TreeNode> e = node.children();
            Enumeration<?> e = node.children();
            while (e.hasMoreElements()) {
                visitAll(tree, parent.pathByAddingChild(e.nextElement()), expand);
            }
        }
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }
}
