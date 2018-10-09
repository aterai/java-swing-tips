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
import javax.swing.text.*;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class MainPanel extends JPanel {
    protected final JTree tree = new JTree();
    protected final JTextField field = new JTextField("foo");
    protected final HighlightTreeCellRenderer renderer = new HighlightTreeCellRenderer();

    public MainPanel() {
        super(new BorderLayout());

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
        n.setBorder(BorderFactory.createTitledBorder("Search"));

        tree.setCellRenderer(renderer);
        renderer.query = field.getText();
        fireDocumentChangeEvent();

        add(n, BorderLayout.NORTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    protected final void fireDocumentChangeEvent() {
        String q = field.getText();
        renderer.query = q;
        TreePath root = tree.getPathForRow(0);
        collapseAll(tree, root);
        if (!q.isEmpty()) {
            searchTree(tree, root, q);
        }
    }
    private static void searchTree(JTree tree, TreePath path, String q) {
        TreeNode node = (TreeNode) path.getLastPathComponent();
        if (Objects.isNull(node)) {
            return;
        } else if (node.toString().startsWith(q)) {
            tree.expandPath(path.getParentPath());
        }
        if (!node.isLeaf()) {
            // Java 9: Collections.list(node.children())
            Collections.list((Enumeration<?>) node.children()).stream()
                .forEach(n -> searchTree(tree, path.pathByAddingChild(n), q));
        }
    }
    private static void collapseAll(JTree tree, TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (!node.isLeaf()) {
            // Java 9: Collections.list(node.children())
            Collections.list((Enumeration<?>) node.children()).stream()
                .forEach(n -> collapseAll(tree, parent.pathByAddingChild(n)));
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

class HighlightTreeCellRenderer extends JTextField implements TreeCellRenderer {
    private static final Color BACKGROUND_SELECTION_COLOR = new Color(220, 240, 255);
    private final transient Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
    protected String query;

    @Override public void updateUI() {
        super.updateUI();
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder());
        setForeground(Color.BLACK);
        setBackground(Color.WHITE);
        setEditable(false);
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        String txt = Objects.toString(value, "");
        getHighlighter().removeAllHighlights();
        setText(txt);
        setBackground(selected ? BACKGROUND_SELECTION_COLOR : Color.WHITE);
        if (Objects.nonNull(query) && !query.isEmpty() && txt.startsWith(query)) {
            try {
                getHighlighter().addHighlight(0, query.length(), highlightPainter);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
        return this;
    }
}
