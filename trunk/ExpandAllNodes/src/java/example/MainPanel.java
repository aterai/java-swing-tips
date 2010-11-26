package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    private final JTree tree = new JTree();
    public MainPanel() {
        super(new BorderLayout());
        tree.setModel(makeModel());

        JPanel p = new JPanel(new GridLayout(0,1,2,2));
        p.add(new JButton(new AbstractAction("expand A") {
            @Override public void actionPerformed(ActionEvent e) {
                expandAll(tree);
            }
        }));
        p.add(new JButton(new AbstractAction("collapse A") {
            @Override public void actionPerformed(ActionEvent e) {
                collapseAll(tree);
            }
        }));
        p.add(new JButton(new AbstractAction("expand B") {
            @Override public void actionPerformed(ActionEvent e) {
                TreeNode root = (TreeNode)tree.getModel().getRoot();
                visitAll(tree, new TreePath(root), true);
                //expandAPath(new TreePath(root));
            }
        }));
        p.add(new JButton(new AbstractAction("collapse B") {
            @Override public void actionPerformed(ActionEvent e) {
                TreeNode root = (TreeNode)tree.getModel().getRoot();
                visitAll(tree, new TreePath(root), false);
            }
        }));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(p, BorderLayout.NORTH);
        add(panel, BorderLayout.EAST);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private static DefaultTreeModel makeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
        DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
        DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
        set1.add(new DefaultMutableTreeNode("111111111"));
        set1.add(new DefaultMutableTreeNode("22222222222"));
        set1.add(new DefaultMutableTreeNode("33333"));
        set2.add(new DefaultMutableTreeNode("asdfasdfas"));
        set2.add(new DefaultMutableTreeNode("asdf"));
        set3.add(new DefaultMutableTreeNode("asdfasdfasdf"));
        set3.add(new DefaultMutableTreeNode("zxcvzxcvzx"));
        set3.add(new DefaultMutableTreeNode("qwerqwerqwerqwerqwer"));
        root.add(set1);
        root.add(set2);
        set2.add(set3);
        return new DefaultTreeModel(root);
    }

    //Expanding or Collapsing All Nodes in a JTree Component (Java Developers Almanac Example)
    //http://www.exampledepot.com/egs/javax.swing.tree/ExpandAll.html
    private void visitAll(JTree tree, TreePath parent, boolean expand) {
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if(!node.isLeaf() && node.getChildCount()>=0) {
            Enumeration e = node.children();
            while(e.hasMoreElements()) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                visitAll(tree, path, expand);
            }
        }
        if(expand) tree.expandPath(parent);
        else       tree.collapsePath(parent);
    }

    //Expand or collapse a JTree - Real's Java How-to
    //http://64.18.163.122/rgagnon/javadetails/java-0210.html
    private void expandAll(JTree tree) {
        int row = 0;
        while(row<tree.getRowCount()) {
            tree.expandRow(row);
            row++;
        }
    }
    private void collapseAll(JTree tree) {
        int row = tree.getRowCount()-1;
        while(row>=0) {
            tree.collapseRow(row);
            row--;
        }
    }
    //Swing - How to Expand a JTree completely
    //http://forums.sun.com/thread.jspa?threadID=381292
    private void expandAPath(TreePath p) {
        tree.expandPath(p);
        DefaultMutableTreeNode n = (DefaultMutableTreeNode) p.getLastPathComponent();
        for(int i=0;i<n.getChildCount();i++) {
            TreePath path = p.pathByAddingChild(n.getChildAt(i));
            expandAPath(path);
        }
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
