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
    private final JTextField textField = new JTextField(24);
    private TreePath path;
    private final Action addNodeAction = new AbstractAction("add") {
        @Override public void actionPerformed(ActionEvent e) {
            final JTree tree = (JTree) getInvoker();
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode child  = new DefaultMutableTreeNode("New node");
            model.insertNodeInto(child, parent, parent.getChildCount());
            tree.scrollPathToVisible(new TreePath(child.getPath())); //http://http://ateraimemo.com/Swing/ScrollRectToVisible.html
        }
    };
    private final Action addReloadNodeAction = new AbstractAction("add & reload") {
        @Override public void actionPerformed(ActionEvent e) {
            JTree tree = (JTree) getInvoker();
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode child  = new DefaultMutableTreeNode("New node");
            parent.add(child);
            model.reload(parent); //= model.nodeStructureChanged(parent);
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
        add(addNodeAction);
        add(addReloadNodeAction);
        add(editNodeAction);
        addSeparator();
        add(removeNodeAction);
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTree) {
            JTree tree = (JTree) c;
            //TreePath[] tsp = tree.getSelectionPaths();
            path = tree.getPathForLocation(x, y);
            //if (path != null && Arrays.asList(tsp).contains(path)) {
            if (path != null) {
                tree.setSelectionPath(path);
                super.show(c, x, y);
            }
        }
    }
}
