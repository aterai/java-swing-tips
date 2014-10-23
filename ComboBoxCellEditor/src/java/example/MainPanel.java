package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final JTextArea textArea = new JTextArea(5, 1);
    public MainPanel() {
        super(new BorderLayout());

        String[] m1 = {"Disabled", "Enabled", "Debug mode"};
        String[] m2 = {"Disabled", "Enabled"};
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new Node("Plugins"));
        root.add(new DefaultMutableTreeNode(new Node("Plugin 1", m1)));
        root.add(new DefaultMutableTreeNode(new Node("Plugin 2", m1)));
        DefaultMutableTreeNode leaf = new DefaultMutableTreeNode(new Node("Plugin 3"));
        root.add(leaf);
        leaf.add(new DefaultMutableTreeNode(new Node("Plugin 3A", m2)));
        leaf.add(new DefaultMutableTreeNode(new Node("Plugin 3B", m2)));

        JTree tree = new JTree(root);
        tree.setRowHeight(0);
        tree.setEditable(true);
        tree.setCellRenderer(new PluginCellRenderer(new JComboBox<String>()));
        tree.setCellEditor(new PluginCellEditor(new JComboBox<String>()));

        tree.getModel().addTreeModelListener(new TreeModelListener() {
            @Override public void treeNodesChanged(TreeModelEvent e) {
                Object[] children = e.getChildren();
                if (children != null && children.length == 1 && children[0] instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) children[0];
                    Object userObject = node.getUserObject();
                    if (userObject instanceof Node) {
                        Node uo = (Node) userObject;
                        textArea.append(String.format("%s %s%n", uo, uo.plugins[uo.getSelectedPluginIndex()]));
                    }
                }
            }
            @Override public void treeNodesInserted(TreeModelEvent e)    { /* not needed */ }
            @Override public void treeNodesRemoved(TreeModelEvent e)     { /* not needed */ }
            @Override public void treeStructureChanged(TreeModelEvent e) { /* not needed */ }
        });
        add(new JScrollPane(tree));
        add(new JScrollPane(textArea), BorderLayout.SOUTH);
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

class Node {
    protected final String name;
    protected final String[] plugins;
    private int selectedPluginIndex;

    public Node(String name, String... plugins) {
        this.name = name;
        this.plugins = plugins;
    }
    public int getSelectedPluginIndex() {
        return selectedPluginIndex;
    }
    public void setSelectedPluginIndex(int selectedPluginIndex) {
        this.selectedPluginIndex = selectedPluginIndex;
    }
    @Override public String toString() {
        return name;
    }
}

class PluginPanel extends JPanel {
    protected final JLabel pluginName = new JLabel();
    protected final JComboBox<String> comboBox;
    public PluginPanel(JComboBox<String> comboBox) {
        super();
        this.comboBox = comboBox;
        comboBox.setPrototypeDisplayValue("Debug mode x");
        setOpaque(false);
        add(pluginName);
        add(comboBox);
    }
    protected static Node extractNode(Object value) {
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            if (userObject instanceof Node) {
                return (Node) userObject;
            }
        }
        return null;
    }
    protected void setContents(Node node) {
        if (node == null) {
            return;
        }
        pluginName.setText(node.toString());
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) comboBox.getModel();
        model.removeAllElements();
        if (node.plugins.length > 0) {
            add(comboBox);
            for (String s: node.plugins) {
                model.addElement(s);
            }
            comboBox.setSelectedIndex(node.getSelectedPluginIndex());
        } else {
            remove(comboBox);
        }
    }
}

class PluginCellRenderer implements TreeCellRenderer {
    private final PluginPanel panel;

    public PluginCellRenderer(JComboBox<String> comboBox) {
        super();
        panel = new PluginPanel(comboBox);
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Node node = panel.extractNode(value);
        panel.setContents(node);
        return panel;
    }
}

class PluginCellEditor extends DefaultCellEditor {
    private final PluginPanel panel;
    private transient Node node;

    public PluginCellEditor(JComboBox<String> comboBox) {
        super(comboBox);
        panel = new PluginPanel(comboBox);
    }
    @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
        Node node = panel.extractNode(value);
        panel.setContents(node);
        this.node = node;
        return panel;
    }
    @Override public Object getCellEditorValue() {
        Object o = super.getCellEditorValue();
        if (node == null) {
            return o;
        }
        DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) panel.comboBox.getModel();
        Node n = new Node(panel.pluginName.getText(), node.plugins);
        n.setSelectedPluginIndex(m.getIndexOf(o));
        return n;
    }
    @Override public boolean isCellEditable(EventObject e) {
        Object source = e.getSource();
        if (!(source instanceof JTree) || !(e instanceof MouseEvent)) {
            return false;
        }
        JTree tree = (JTree) source;
        MouseEvent me = (MouseEvent) e;
        TreePath path = tree.getPathForLocation(me.getX(), me.getY());
        if (path == null) {
            return false;
        }
        Object node = path.getLastPathComponent();
        if (!(node instanceof DefaultMutableTreeNode)) {
            return false;
        }
        Rectangle r = tree.getPathBounds(path);
        if (r == null) {
            return false;
        }
        Dimension d = panel.getPreferredSize();
        r.setSize(new Dimension(d.width, r.height));
        if (r.contains(me.getX(), me.getY())) {
            showComboPopup(tree, me);
            return true;
        }
        return delegate.isCellEditable(e);
    }
    private void showComboPopup(final JTree tree, final MouseEvent me) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                Point pt = SwingUtilities.convertPoint(tree, me.getPoint(), panel);
                Object o = SwingUtilities.getDeepestComponentAt(panel, pt.x, pt.y);
                if (o instanceof JComboBox) {
                    panel.comboBox.showPopup();
                } else if (o != null) {
                    Object oo = SwingUtilities.getAncestorOfClass(JComboBox.class, (Component) o);
                    if (oo instanceof JComboBox) {
                        panel.comboBox.showPopup();
                    }
                }
            }
        });
    }
}
