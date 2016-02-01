package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final JTree tree       = new JTree();
    private final JTextField field = new JTextField("foo");
    private final HighlightTreeCellRenderer renderer = new HighlightTreeCellRenderer();
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
        renderer.q = field.getText();
        fireDocumentChangeEvent();

        add(n, BorderLayout.NORTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private void fireDocumentChangeEvent() {
        String q = field.getText();
        renderer.q = q;
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
        if (!node.isLeaf() && node.getChildCount() >= 0) {
            Enumeration e = node.children();
            while (e.hasMoreElements()) {
                searchTree(tree, path.pathByAddingChild(e.nextElement()), q);
            }
        }
    }
    private static void collapseAll(JTree tree, TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (!node.isLeaf() && node.getChildCount() >= 0) {
            Enumeration e = node.children();
            while (e.hasMoreElements()) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                collapseAll(tree, path);
            }
        }
        tree.collapsePath(parent);
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

class HighlightTreeCellRenderer extends JTextField implements TreeCellRenderer {
    private static final Color BACKGROUND_SELECTION_COLOR = new Color(220, 240, 255);
    private final transient Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
    public String q;

    @Override public void updateUI() {
        super.updateUI();
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder());
        setForeground(Color.BLACK);
        setBackground(Color.WHITE);
        setEditable(false);
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        String txt = Objects.toString(value, "");
        getHighlighter().removeAllHighlights();
        setText(txt);
        setBackground(isSelected ? BACKGROUND_SELECTION_COLOR : Color.WHITE);
        if (Objects.nonNull(q) && !q.isEmpty() && txt.startsWith(q)) {
            try {
                getHighlighter().addHighlight(0, q.length(), highlightPainter);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        return this;
    }
}
