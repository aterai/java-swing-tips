package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    private final JTree tree       = new JTree();
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
        renderer.q = field.getText();
        fireDocumentChangeEvent();

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(n, BorderLayout.NORTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private void fireDocumentChangeEvent() {
        String q = field.getText();
        renderer.q = q;
        TreePath root = tree.getPathForRow(0);
        collapseAll(tree, root);
        if(!q.isEmpty()) { searchTree(tree, root, q); }
        //tree.repaint();
    }
    private static void searchTree(JTree tree, TreePath path, String q) {
        TreeNode node = (TreeNode)path.getLastPathComponent();
        if(node==null) { return; }
        if(node.toString().startsWith(q)) { tree.expandPath(path.getParentPath()); }
        if(!node.isLeaf() && node.getChildCount()>=0) {
            Enumeration e = node.children();
            while(e.hasMoreElements()) {
                searchTree(tree, path.pathByAddingChild(e.nextElement()), q);
            }
        }
    }
    private static void collapseAll(JTree tree, TreePath parent) {
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if(!node.isLeaf() && node.getChildCount()>=0) {
            Enumeration e = node.children();
            while(e.hasMoreElements()) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                collapseAll(tree, path);
            }
        }
        tree.collapsePath(parent);
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
//*
class HighlightTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final Color rollOverRowColor = new Color(220, 240, 255);
    public String q;
    private boolean rollOver;

    @Override public void updateUI() {
        setTextSelectionColor(null);
        setTextNonSelectionColor(null);
        setBackgroundSelectionColor(null);
        setBackgroundNonSelectionColor(null);
        super.updateUI();
    }
    @Override public Color getBackgroundNonSelectionColor() {
        return rollOver ? rollOverRowColor : super.getBackgroundNonSelectionColor();
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JComponent c = (JComponent)super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
        if(isSelected) {
            c.setForeground(getTextSelectionColor());
        }else{
            rollOver = q!=null && !q.isEmpty() && Objects.toString(value, "").startsWith(q);
            c.setForeground(getTextNonSelectionColor());
            c.setBackground(getBackgroundNonSelectionColor());
        }
        return c;
    }
}
/*/
class HighlightTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final Color rollOverRowColor = new Color(220, 240, 255);
    public String q;
    @Override public void updateUI() {
        setTextSelectionColor(null);
        setTextNonSelectionColor(null);
        setBackgroundSelectionColor(null);
        setBackgroundNonSelectionColor(null);
        super.updateUI();
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JComponent c = (JComponent)super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
        //DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        if(isSelected) {
            c.setOpaque(false);
            c.setForeground(getTextSelectionColor());
            //c.setBackground(Color.BLUE); //getBackgroundSelectionColor());
        }else{
            c.setOpaque(true);
            if(q!=null && !q.isEmpty() && value.toString().startsWith(q)) {
                c.setForeground(getTextNonSelectionColor());
                c.setBackground(rollOverRowColor);
            }else{
                c.setForeground(getTextNonSelectionColor());
                c.setBackground(getBackgroundNonSelectionColor());
            }
        }
        return c;
    }
}
//*/
