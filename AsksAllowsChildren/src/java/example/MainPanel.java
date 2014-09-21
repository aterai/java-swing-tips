package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final DefaultTreeModel model = makeDefaultTreeModel();
    private final JTree tree = new JTree(model);
    private MainPanel() {
        super(new GridLayout(1, 2));

        JTree t = new JTree(makeDefaultTreeModel());
        t.setComponentPopupMenu(new TreePopupMenu());
        add(makeTitledPanel("Default", new JScrollPane(t)));

        tree.setComponentPopupMenu(new TreePopupMenu());
        //model.setAsksAllowsChildren(true);
        JPanel p = makeTitledPanel("setAsksAllowsChildren", new JScrollPane(tree));
        p.add(new JCheckBox(new AbstractAction("setAsksAllowsChildren") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox c = (JCheckBox) e.getSource();
                model.setAsksAllowsChildren(c.isSelected());
                tree.repaint();
            }
        }), BorderLayout.SOUTH);
        add(p);

        setPreferredSize(new Dimension(320, 240));
    }
    private static JPanel makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    private static DefaultTreeModel makeDefaultTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode parent;

        parent = new DefaultMutableTreeNode("colors");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("blue", false));
        parent.add(new DefaultMutableTreeNode("violet", false));
        parent.add(new DefaultMutableTreeNode("red", false));
        parent.add(new DefaultMutableTreeNode("yellow", false));

        parent = new DefaultMutableTreeNode("sports");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("basketball", false));
        parent.add(new DefaultMutableTreeNode("soccer", false));
        parent.add(new DefaultMutableTreeNode("football", false));
        parent.add(new DefaultMutableTreeNode("hockey", false));

        parent = new DefaultMutableTreeNode("food");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("hot dogs", false));
        parent.add(new DefaultMutableTreeNode("pizza", false));
        parent.add(new DefaultMutableTreeNode("ravioli", false));
        parent.add(new DefaultMutableTreeNode("bananas", false));

        parent = new DefaultMutableTreeNode("test");
        root.add(parent);

        return new DefaultTreeModel(root);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
    private final JTextField textField = new JTextField(24);
    private TreePath path;
    private final Action addFolderAction = new AbstractAction("add folder") {
        @Override public void actionPerformed(ActionEvent e) {
            final JTree tree = (JTree) getInvoker();
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode child  = new DefaultMutableTreeNode("New Folder", true);
            model.insertNodeInto(child, parent, parent.getChildCount());
            tree.scrollPathToVisible(new TreePath(child.getPath()));
        }
    };
    private final Action addItemAction = new AbstractAction("add item") {
        @Override public void actionPerformed(ActionEvent e) {
            JTree tree = (JTree) getInvoker();
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode child  = new DefaultMutableTreeNode("New Item", false);
            model.insertNodeInto(child, parent, parent.getChildCount());
            tree.scrollPathToVisible(new TreePath(child.getPath()));
        }
    };
    private final Action editNodeAction = new AbstractAction("edit") {
        @Override public void actionPerformed(ActionEvent e) {
            //if (path == null) { return; }
            Object node = path.getLastPathComponent();
            if (node instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) node;
                textField.setText(leaf.getUserObject().toString());
                JTree tree = (JTree) getInvoker();
                int result = JOptionPane.showConfirmDialog(
                    tree, textField, "edit",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    String str = textField.getText();
                    if (!str.trim().isEmpty()) {
                        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                        model.valueForPathChanged(path, str);
                        //leaf.setUserObject(str);
                        //model.nodeChanged(leaf);
                    }
                }
            }
        }
    };
    private final Action removeNodeAction = new AbstractAction("remove") {
        @Override public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            //if (path.getParentPath() != null) {
            if (!node.isRoot()) {
                JTree tree = (JTree) getInvoker();
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                model.removeNodeFromParent(node);
            }
        }
    };
    public TreePopupMenu() {
        super();
        textField.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent e) {
                textField.requestFocusInWindow();
            }
            @Override public void ancestorMoved(AncestorEvent e)   { /* not needed */ }
            @Override public void ancestorRemoved(AncestorEvent e) { /* not needed */ }
        });
        add(addFolderAction);
        add(addItemAction);
        add(editNodeAction);
        addSeparator();
        add(removeNodeAction);
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTree) {
            JTree tree = (JTree) c;
            path = tree.getPathForLocation(x, y);
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                boolean flag = node.getAllowsChildren();
                addFolderAction.setEnabled(flag);
                addItemAction.setEnabled(flag);
                super.show(c, x, y);
            }
        }
    }
}
