package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.DefaultPersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final JTextArea textArea = new JTextArea();
    private final JButton save = new JButton("save");
    private final JButton load = new JButton("load");
    private MainPanel() {
        super(new BorderLayout());
        JTree tree = new JTree() {
            @Override public void updateUI() {
                setCellRenderer(null);
                setCellEditor(null);
                super.updateUI();
                // ???#1: JDK 1.6.0 bug??? Nimbus LnF
                setCellRenderer(new CheckBoxNodeRenderer());
                setCellEditor(new CheckBoxNodeEditor());
            }
        };
        TreeModel model = tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        // Java 9: Enumeration<TreeNode> en = root.breadthFirstEnumeration();
        Enumeration<?> en = root.breadthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
            node.setUserObject(new CheckBoxNode(Objects.toString(node.getUserObject(), ""), Status.DESELECTED));
        }
        model.addTreeModelListener(new CheckBoxStatusUpdateListener());

        tree.setEditable(true);
        tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        tree.expandRow(0);

        save.addActionListener(e -> {
            try {
                File file = File.createTempFile("output", ".xml");
                // try (XMLEncoder xe = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)))) {
                try (XMLEncoder xe = new XMLEncoder(new BufferedOutputStream(Files.newOutputStream(file.toPath())))) {
                    xe.setPersistenceDelegate(CheckBoxNode.class, new DefaultPersistenceDelegate(new String[] {"label", "status"}));
                    xe.writeObject(tree.getModel());
                }
                // try (Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                try (Reader r = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                    textArea.read(r, "temp");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        load.addActionListener(e -> {
            String text = textArea.getText();
            if (text.isEmpty()) {
                return;
            }
            // try (XMLDecoder xd = new XMLDecoder(new BufferedInputStream(new FileInputStream(new File("output.xml"))))) {
            try (XMLDecoder xd = new XMLDecoder(new BufferedInputStream(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))))) {
                DefaultTreeModel m = (DefaultTreeModel) xd.readObject();
                m.addTreeModelListener(new CheckBoxStatusUpdateListener());
                tree.setModel(m);
            }
        });

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(save);
        box.add(Box.createHorizontalStrut(4));
        box.add(load);

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setResizeWeight(.5);
        sp.setTopComponent(new JScrollPane(tree));
        sp.setBottomComponent(new JScrollPane(textArea));

        add(sp);
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
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

class TriStateCheckBox extends JCheckBox {
    @Override public void updateUI() {
        Icon currentIcon = getIcon();
        setIcon(null);
        super.updateUI();
        if (Objects.nonNull(currentIcon)) {
            setIcon(new IndeterminateIcon());
        }
        setOpaque(false);
    }
}

class IndeterminateIcon implements Icon {
    private static final Color FOREGROUND = new Color(50, 20, 255, 200); // TEST: UIManager.getColor("CheckBox.foreground");
    private static final int SIDE_MARGIN = 4;
    private static final int HEIGHT = 2;
    private final Icon icon = UIManager.getIcon("CheckBox.icon");
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        icon.paintIcon(c, g2, 0, 0);
        g2.setPaint(FOREGROUND);
        g2.fillRect(SIDE_MARGIN, (getIconHeight() - HEIGHT) / 2, getIconWidth() - SIDE_MARGIN - SIDE_MARGIN, HEIGHT);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return icon.getIconWidth();
    }
    @Override public int getIconHeight() {
        return icon.getIconHeight();
    }
}

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
        if (Objects.nonNull(children) && children.length == 1) {
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
    private void updateParentUserObject(DefaultMutableTreeNode parent) {
        int selectedCount = 0;
        // Java 9: Enumeration<TreeNode> children = parent.children();
        Enumeration<?> children = parent.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
            CheckBoxNode check = (CheckBoxNode) node.getUserObject();
            if (check.getStatus() == Status.INDETERMINATE) {
                selectedCount = -1;
                break;
            }
            if (check.getStatus() == Status.SELECTED) {
                selectedCount++;
            }
        }
        String label = ((CheckBoxNode) parent.getUserObject()).getLabel();
        if (selectedCount == 0) {
            parent.setUserObject(new CheckBoxNode(label, Status.DESELECTED));
        } else if (selectedCount == parent.getChildCount()) {
            parent.setUserObject(new CheckBoxNode(label, Status.SELECTED));
        } else {
            parent.setUserObject(new CheckBoxNode(label));
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
            node.setUserObject(new CheckBoxNode(check.getLabel(), status));
        }
    }
    @Override public void treeNodesInserted(TreeModelEvent e) { /* not needed */ }
    @Override public void treeNodesRemoved(TreeModelEvent e) { /* not needed */ }
    @Override public void treeStructureChanged(TreeModelEvent e) { /* not needed */ }
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
        protected transient ActionListener handler;
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
            //     return false;
            // }
            // Dimension d = checkBox.getPreferredSize();
            // r.setSize(new Dimension(d.width, r.height));
            // if (r.contains(me.getPoint())) {
            //     return true;
            // }
        }
        return false;
    }
    // // AbstractCellEditor
    // @Override public boolean shouldSelectCell(EventObject anEvent) {
    //     return true;
    // }
    // @Override public boolean stopCellEditing() {
    //     fireEditingStopped();
    //     return true;
    // }
    // @Override public void cancelCellEditing() {
    //     fireEditingCanceled();
    // }
}
