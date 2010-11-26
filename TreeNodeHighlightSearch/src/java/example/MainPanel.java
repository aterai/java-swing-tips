package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        add(makeUI());
        setPreferredSize(new Dimension(320, 240));
    }
    private final JTree tree       = new JTree();
    private final JTextField field = new JTextField("foo");
    private final HighlightTreeCellRenderer renderer = new HighlightTreeCellRenderer(tree.getCellRenderer());
    public JPanel makeUI() {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                fireDocumentChangeEvent();
            }
            @Override public void removeUpdate(DocumentEvent e) {
                fireDocumentChangeEvent();
            }
            @Override public void changedUpdate(DocumentEvent e) {}
        });
        JPanel n = new JPanel(new BorderLayout());
        n.add(field);
        n.setBorder(BorderFactory.createTitledBorder("Highlight Search"));

        tree.setCellRenderer(renderer);
        renderer.q = field.getText();
        fireDocumentChangeEvent();

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(n, BorderLayout.NORTH);
        p.add(new JScrollPane(tree));
        return p;
    }
    private void fireDocumentChangeEvent() {
        String q = field.getText();
        renderer.q = q;
        TreePath root = tree.getPathForRow(0);
        collapseAll(tree, root);
        if(!q.isEmpty()) searchTree(tree, root, q);
        //tree.repaint();
    }
    private static void searchTree(JTree tree, TreePath path, String q) {
        TreeNode node = (TreeNode)path.getLastPathComponent();
        if(node==null) return;
        if(node.toString().startsWith(q)) tree.expandPath(path.getParentPath());
        if(!node.isLeaf() && node.getChildCount()>=0) {
            Enumeration e = node.children();
            while(e.hasMoreElements())
              searchTree(tree, path.pathByAddingChild(e.nextElement()), q);
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
        }catch(Exception e) {
            e.printStackTrace();
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
class HighlightTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final Color rollOverRowColor = new Color(220, 240, 255);
    private final TreeCellRenderer renderer;
    public String q;
    public HighlightTreeCellRenderer(TreeCellRenderer renderer) {
        this.renderer = renderer;
    }
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected,
                                                  boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JComponent c = (JComponent)renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
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
