package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTree tree = new JTree();
        tree.setComponentPopupMenu(new TreePopupMenu());
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
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

class TreePopupMenu extends JPopupMenu {
    protected TreePath path;
    private final Action addNodeAction = new AbstractAction("add") {
        @Override public void actionPerformed(ActionEvent e) {
            JTree tree = (JTree) getInvoker();
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode child = new DefaultMutableTreeNode("New node");
            model.insertNodeInto(child, parent, parent.getChildCount());
            tree.scrollPathToVisible(new TreePath(child.getPath())); // https://ateraimemo.com/Swing/ScrollRectToVisible.html
        }
    };
    private final Action addReloadAction = new AbstractAction("add & reload") {
        @Override public void actionPerformed(ActionEvent e) {
            JTree tree = (JTree) getInvoker();
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode child = new DefaultMutableTreeNode("New node");
            parent.add(child);
            model.reload(parent); // = model.nodeStructureChanged(parent);
            tree.scrollPathToVisible(new TreePath(child.getPath()));
        }
    };
    private final Action editNodeAction = new AbstractAction("edit") {
        protected final JTextField textField = new JTextField(24) {
            protected transient AncestorListener listener;
            @Override public void updateUI() {
                removeAncestorListener(listener);
                super.updateUI();
                listener = new AncestorListener() {
                    @Override public void ancestorAdded(AncestorEvent e) {
                        requestFocusInWindow();
                    }
                    @Override public void ancestorMoved(AncestorEvent e) { /* not needed */ }
                    @Override public void ancestorRemoved(AncestorEvent e) { /* not needed */ }
                };
                addAncestorListener(listener);
            }
        };
        @Override public void actionPerformed(ActionEvent e) {
            Object node = path.getLastPathComponent();
            if (!(node instanceof DefaultMutableTreeNode)) {
                return;
            }
            DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) node;
            textField.setText(leaf.getUserObject().toString());
            JTree tree = (JTree) getInvoker();
            int ret = JOptionPane.showConfirmDialog(tree, textField, "edit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ret == JOptionPane.OK_OPTION) {
                Optional.ofNullable(textField.getText())
                    .filter(str -> !str.trim().isEmpty())
                    .ifPresent(str -> {
                        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                        model.valueForPathChanged(path, str);
                        // leaf.setUserObject(str);
                        // model.nodeChanged(leaf);
                    });
            }
        }
    };
    private final Action removeNodeAction = new AbstractAction("remove") {
        @Override public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (!node.isRoot()) {
                JTree tree = (JTree) getInvoker();
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                model.removeNodeFromParent(node);
            }
        }
    };
    protected TreePopupMenu() {
        super();
        add(addNodeAction);
        add(addReloadAction);
        add(editNodeAction);
        addSeparator();
        add(removeNodeAction);
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTree) {
            JTree tree = (JTree) c;
            // TreePath[] tsp = tree.getSelectionPaths();
            path = tree.getPathForLocation(x, y);
            // if (Objects.nonNull(path) && Arrays.asList(tsp).contains(path)) {
            Optional.ofNullable(path).ifPresent(p -> {
                tree.setSelectionPath(p);
                super.show(c, x, y);
            });
        }
    }
}
