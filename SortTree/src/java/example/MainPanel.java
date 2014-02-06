package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
//import java.io.Serializable;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    private static final boolean DEBUG = true;
    private final DefaultMutableTreeNode root = TreeUtil.makeTreeRoot();
    private final JTree tree = new JTree(new DefaultTreeModel(TreeUtil.makeTreeRoot()));
    private final TreeNodeComparator tnc = new TreeNodeComparator();
    private int compareCount;
    private int swapCount;

    public MainPanel() {
        super(new BorderLayout());
        JPanel box = new JPanel(new GridLayout(2,2));
        box.add(new JCheckBox(new AbstractAction("0: bubble sort") {
            @Override public void actionPerformed(ActionEvent e) {
                if(((JCheckBox)e.getSource()).isSelected()) {
                    compareCount = swapCount = 0;
                    DefaultMutableTreeNode r = TreeUtil.deepCopyTree(root, (DefaultMutableTreeNode)root.clone());
                    sortTree0(r);
                    System.out.format("0: bubble sort          - compare: %3d, swap: %3d%n", compareCount, swapCount);
                    tree.setModel(new DefaultTreeModel(r));
                }else{
                    tree.setModel(new DefaultTreeModel(root));
                }
                TreeUtil.expandAll(tree);
            }
        }));
        box.add(new JCheckBox(new AbstractAction("1: bubble sort") {
            @Override public void actionPerformed(ActionEvent e) {
                if(((JCheckBox)e.getSource()).isSelected()) {
                    compareCount = swapCount = 0;
                    DefaultMutableTreeNode r = TreeUtil.deepCopyTree(root, (DefaultMutableTreeNode)root.clone());
                    sortTree1(r);
                    System.out.format("1: bubble sort          - compare: %3d, swap: %3d%n", compareCount, swapCount);
                    tree.setModel(new DefaultTreeModel(r));
                }else{
                    tree.setModel(new DefaultTreeModel(root));
                }
                TreeUtil.expandAll(tree);
            }
        }));
        box.add(new JCheckBox(new AbstractAction("2: selection sort") {
            @Override public void actionPerformed(ActionEvent e) {
                if(((JCheckBox)e.getSource()).isSelected()) {
                    compareCount = swapCount = 0;
                    DefaultMutableTreeNode r = TreeUtil.deepCopyTree(root, (DefaultMutableTreeNode)root.clone());
                    sortTree2(r);
                    System.out.format("2: selection sort       - compare: %3d, swap: %3d%n", compareCount, swapCount);
                    tree.setModel(new DefaultTreeModel(r));
                }else{
                    tree.setModel(new DefaultTreeModel(root));
                }
                TreeUtil.expandAll(tree);
            }
        }));
        box.add(new JCheckBox(new AbstractAction("3: iterative merge sort") {
            @Override public void actionPerformed(ActionEvent e) {
                if(((JCheckBox)e.getSource()).isSelected()) {
                    compareCount = swapCount = 0;
                    DefaultMutableTreeNode r = TreeUtil.deepCopyTree(root, (DefaultMutableTreeNode)root.clone());
                    sortTree3(r);
                    System.out.format("3: iterative merge sort - compare: %3d, swap: ---%n", compareCount);
                    tree.setModel(new DefaultTreeModel(r));
                }else{
                    tree.setModel(new DefaultTreeModel(root));
                }
                TreeUtil.expandAll(tree);
            }
        }));

        add(box, BorderLayout.SOUTH);
        add(makeTitledPanel("Sort JTree", tree));
        TreeUtil.expandAll(tree);
        setPreferredSize(new Dimension(320, 240));
    }

    private JComponent makeTitledPanel(String title, JTree tree) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(tree));
        return p;
    }

    // https://forums.oracle.com/thread/1355435 How to sort jTree Nodes
    public void sortTree0(DefaultMutableTreeNode root) {
        for(int i=0;i<root.getChildCount();i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
            for(int j=0; j<i; j++) {
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) root.getChildAt(j);
                compareCount++;
                if(tnc.compare(node, prevNode)<0) {
                    root.insert(node, j);
                    root.insert(prevNode, i);
                    swapCount++;
                    if(DEBUG) {
                        i--;
                        break;
                    }
                }
            }
            if(node.getChildCount() > 0) { sortTree0(node); }
        }
    }

    // https://forums.oracle.com/thread/1355435 How to sort jTree Nodes
    public void sortTree1(DefaultMutableTreeNode root) {
        int n = root.getChildCount();
        for(int i=0;i<n-1;i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
            for(int j=i+1; j<n; j++) {
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) root.getChildAt(j);
                if(tnc.compare(node, prevNode)>0) {
                    swapCount++;
                    root.insert(node, j);
                    root.insert(prevNode, i);
                    i--;
                    break;
                }
            }
            if(node.getChildCount() > 0) { sortTree1(node); }
        }
    }

    public void sort2(DefaultMutableTreeNode parent) {
        int n = parent.getChildCount();
        for(int i=0; i<n-1; i++) {
            int min = i;
            for(int j=i+1; j<n; j++) {
                if(tnc.compare((DefaultMutableTreeNode)parent.getChildAt(min),
                               (DefaultMutableTreeNode)parent.getChildAt(j))>0) {
                    min = j;
                }
            }
            if(i!=min) {
                swapCount++;
                MutableTreeNode a = (MutableTreeNode)parent.getChildAt(i);
                MutableTreeNode b = (MutableTreeNode)parent.getChildAt(min);
                parent.insert(b, i);
                parent.insert(a, min);
                //MutableTreeNode node = (MutableTreeNode)parent.getChildAt(min);
                //parent.insert(node, i);
                //compareCount++;
            }
        }
    }
    public void sortTree2(DefaultMutableTreeNode root) {
        Enumeration e = root.depthFirstEnumeration();
        while(e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
            if(!node.isLeaf()) {
                sort2(node);
            }
        }
    }
//     public void sortTree2(DefaultMutableTreeNode root) {
//         Enumeration e = root.children();
//         while(e.hasMoreElements()) {
//             DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
//             if(!node.isLeaf()) {
//                 sortTree2(node);
//             }
//         }
//         sort2(root);
//     }

    public void sort3(DefaultMutableTreeNode parent) {
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> e = parent.children();
        ArrayList<DefaultMutableTreeNode> children = Collections.list(e);

        //java.util.List<DefaultMutableTreeNode> children = new ArrayList<DefaultMutableTreeNode>(n);
        //for(int i=0; i<n; i++) {
        //    children.add((DefaultMutableTreeNode)parent.getChildAt(i));
        //}

        Collections.sort(children, tnc);
        parent.removeAllChildren();
        for(MutableTreeNode node: children) {
            parent.add(node);
        }
    }
    public void sortTree3(DefaultMutableTreeNode root) {
        Enumeration e = root.depthFirstEnumeration();
        while(e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
            if(!node.isLeaf()) {
                sort3(node);
            }
        }
    }

    private class TreeNodeComparator implements Comparator<DefaultMutableTreeNode> { //, Serializable {
        @Override public int compare(DefaultMutableTreeNode a, DefaultMutableTreeNode b) {
            compareCount++;
            if(a.isLeaf() && !b.isLeaf()) {
                return 1;
            }else if(!a.isLeaf() && b.isLeaf()) {
                return -1;
            }else{
                String sa = a.getUserObject().toString();
                String sb = b.getUserObject().toString();
                return sa.compareToIgnoreCase(sb);
            }
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
        }catch(ClassNotFoundException | InstantiationException |
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

class TreeUtil {
    private TreeUtil() { /* Singlton */ }
    public static DefaultMutableTreeNode deepCopyTree(DefaultMutableTreeNode src, DefaultMutableTreeNode tgt) {
        for(int i=0; i<src.getChildCount(); i++) {
            DefaultMutableTreeNode node  = (DefaultMutableTreeNode)src.getChildAt(i);
            DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
            //DefaultMutableTreeNode clone = (DefaultMutableTreeNode)node.clone();
            tgt.add(clone);
            if(!node.isLeaf()) {
                deepCopyTree(node, clone);
            }
        }
        return tgt;
    }

    public static DefaultMutableTreeNode makeTreeRoot() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
        DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
        DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
        DefaultMutableTreeNode set4 = new DefaultMutableTreeNode("Set 004");

        set1.add(new DefaultMutableTreeNode("3333333333333333"));
        set1.add(new DefaultMutableTreeNode("111111111"));
        set1.add(new DefaultMutableTreeNode("22222222222"));
        set1.add(set4);
        set1.add(new DefaultMutableTreeNode("222222"));
        set1.add(new DefaultMutableTreeNode("222222222"));
        set2.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
        set2.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));
        set3.add(new DefaultMutableTreeNode("zzzzzzz"));
        set3.add(new DefaultMutableTreeNode("aaaaaaaaaaaa"));
        set3.add(new DefaultMutableTreeNode("ccccccccc"));

        set4.add(new DefaultMutableTreeNode("22222222222"));
        set4.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
        set4.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));
        set4.add(new DefaultMutableTreeNode("zzzzzzz"));

        root.add(new DefaultMutableTreeNode("xxxxxxxxxxxxx"));
        root.add(set3);
        root.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
        root.add(set1);
        root.add(set2);
        root.add(new DefaultMutableTreeNode("222222222222"));
        root.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));
        return root;
    }

    public static void expandAll(JTree tree) {
        int row = 0;
        while(row<tree.getRowCount()) {
            tree.expandRow(row);
            row++;
        }
    }
}
