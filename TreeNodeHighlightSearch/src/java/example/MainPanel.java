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
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
    private final JTree tree = new JTree();
    private final JTextField field = new JTextField("foo");
    private final HighlightTreeCellRenderer renderer = new HighlightTreeCellRenderer();
    public MainPanel() {
        super(new BorderLayout(5, 5));
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                fireDocumentChangeEvent();
            }
            @Override public void removeUpdate(DocumentEvent e) {
                fireDocumentChangeEvent();
            }
            @Override public void changedUpdate(DocumentEvent e) { /* not needed */ }
        });
        JPanel n = new JPanel(new BorderLayout());
        n.add(field);
        n.setBorder(BorderFactory.createTitledBorder("Highlight Search"));

        tree.setCellRenderer(renderer);
        renderer.query = field.getText();
        fireDocumentChangeEvent();

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(n, BorderLayout.NORTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    protected void fireDocumentChangeEvent() {
        String q = field.getText();
        renderer.query = q;
        TreePath root = tree.getPathForRow(0);
        collapseAll(tree, root);
        if (!q.isEmpty()) {
            searchTree(tree, root, q);
        }
    }
    private static void searchTree(JTree tree, TreePath path, String q) {
        Object o = path.getLastPathComponent();
        if (o instanceof TreeNode) {
            TreeNode node = (TreeNode) o;
            if (Objects.toString(node).startsWith(q)) {
                tree.expandPath(path.getParentPath());
            }
            if (!node.isLeaf()) {
                // Java 9: Collections.list(node.children()).stream()
                Collections.list((Enumeration<?>) node.children()).stream()
                    .forEach(n -> searchTree(tree, path.pathByAddingChild(n), q));
            }
        }
    }
    private static void collapseAll(JTree tree, TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (!node.isLeaf()) {
            // Java 9: Collections.list(node.children()).stream()
            Collections.list((Enumeration<?>) node.children()).stream()
              .forEach(parent::pathByAddingChild);
        }
        tree.collapsePath(parent);
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

//*
class HighlightTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final Color ROLLOVER_ROW_COLOR = new Color(220, 240, 255);
    protected String query;
    private boolean rollOver;

    @Override public void updateUI() {
        setTextSelectionColor(null);
        setTextNonSelectionColor(null);
        setBackgroundSelectionColor(null);
        setBackgroundNonSelectionColor(null);
        super.updateUI();
    }
    @Override public Color getBackgroundNonSelectionColor() {
        return rollOver ? ROLLOVER_ROW_COLOR : super.getBackgroundNonSelectionColor();
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (selected) {
            c.setForeground(getTextSelectionColor());
        } else {
            rollOver = Objects.nonNull(query) && !query.isEmpty() && Objects.toString(value, "").startsWith(query);
            c.setForeground(getTextNonSelectionColor());
            c.setBackground(getBackgroundNonSelectionColor());
        }
        return c;
    }
}
/*/
class HighlightTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final Color ROLLOVER_ROW_COLOR = new Color(220, 240, 255);
    public String q;
    @Override public void updateUI() {
        setTextSelectionColor(null);
        setTextNonSelectionColor(null);
        setBackgroundSelectionColor(null);
        setBackgroundNonSelectionColor(null);
        super.updateUI();
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JComponent c = (JComponent) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        // DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (selected) {
            c.setOpaque(false);
            c.setForeground(getTextSelectionColor());
            // c.setBackground(Color.BLUE); // getBackgroundSelectionColor());
        } else {
            c.setOpaque(true);
            if (Objects.nonNull(q) && !q.isEmpty() && value.toString().startsWith(q)) {
                c.setForeground(getTextNonSelectionColor());
                c.setBackground(ROLLOVER_ROW_COLOR);
            } else {
                c.setForeground(getTextNonSelectionColor());
                c.setBackground(getBackgroundNonSelectionColor());
            }
        }
        return c;
    }
}
//*/
