package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final DefaultMutableTreeNode root = TreeUtil.makeTreeRoot();
    private final JTree tree = new JTree(new DefaultTreeModel(TreeUtil.makeTreeRoot()));
    private final JCheckBox sort0 = new JCheckBox("0: bubble sort");
    private final JCheckBox sort1 = new JCheckBox("1: bubble sort");
    private final JCheckBox sort2 = new JCheckBox("2: selection sort");
    private final JCheckBox sort3 = new JCheckBox("3: iterative merge sort");

    public MainPanel() {
        super(new BorderLayout());
        JPanel box = new JPanel(new GridLayout(2, 2));
        ActionListener sortActionListener = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox check = (JCheckBox) e.getSource();
                if (check.isSelected()) {
                    TreeUtil.compareCount.set(0);
                    TreeUtil.swapCount.set(0);
                    DefaultMutableTreeNode r = TreeUtil.deepCopyTree(root, (DefaultMutableTreeNode) root.clone());
                    if (check.equals(sort0)) {
                        TreeUtil.sortTree0(r);
                    } else if (check.equals(sort1)) {
                        TreeUtil.sortTree1(r);
                    } else if (check.equals(sort2)) {
                        TreeUtil.sortTree2(r);
                    } else {
                        TreeUtil.sortTree3(r);
                    }
                    log(check.getText());
                    tree.setModel(new DefaultTreeModel(r));
                } else {
                    tree.setModel(new DefaultTreeModel(root));
                }
                TreeUtil.expandAll(tree);
            }
        };
        for (JCheckBox check: Arrays.asList(sort0, sort1, sort2, sort3)) {
            box.add(check);
            check.addActionListener(sortActionListener);
        }
        add(box, BorderLayout.SOUTH);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Sort JTree"));
        p.add(new JScrollPane(tree));
        add(p);
        TreeUtil.expandAll(tree);
        setPreferredSize(new Dimension(320, 240));
    }

    private static void log(String title) {
        if (TreeUtil.swapCount.get() == 0) {
            System.out.format("%-24s - compare: %3d, swap: ---%n",
                              title, TreeUtil.compareCount.get());
        } else {
            System.out.format("%-24s - compare: %3d, swap: %3d%n",
                              title, TreeUtil.compareCount.get(), TreeUtil.swapCount.get());
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

final class TreeUtil {
    private static final boolean DEBUG = true;
    public static AtomicInteger compareCount = new AtomicInteger();
    public static AtomicInteger swapCount = new AtomicInteger();
    private static TreeNodeComparator tnc = new TreeNodeComparator();

    private TreeUtil() { /* Singleton */ }

    // https://forums.oracle.com/thread/1355435 How to sort jTree Nodes
    public static void sortTree0(DefaultMutableTreeNode root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
            for (int j = 0; j < i; j++) {
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) root.getChildAt(j);
                compareCount.getAndIncrement();
                if (tnc.compare(node, prevNode) < 0) {
                    root.insert(node, j);
                    root.insert(prevNode, i);
                    swapCount.getAndIncrement();
                    if (DEBUG) {
                        i--;
                        break;
                    }
                }
            }
            if (node.getChildCount() > 0) {
                sortTree0(node);
            }
        }
    }

    // https://forums.oracle.com/thread/1355435 How to sort jTree Nodes
    public static void sortTree1(DefaultMutableTreeNode root) {
        int n = root.getChildCount();
        for (int i = 0; i < n - 1; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
            for (int j = i + 1; j < n; j++) {
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) root.getChildAt(j);
                if (tnc.compare(node, prevNode) > 0) {
                    swapCount.getAndIncrement();
                    root.insert(node, j);
                    root.insert(prevNode, i);
                    i--;
                    break;
                }
            }
            if (node.getChildCount() > 0) {
                sortTree1(node);
            }
        }
    }

    private static void sort2(DefaultMutableTreeNode parent) {
        int n = parent.getChildCount();
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i + 1; j < n; j++) {
                if (tnc.compare((DefaultMutableTreeNode) parent.getChildAt(min),
                               (DefaultMutableTreeNode) parent.getChildAt(j)) > 0) {
                    min = j;
                }
            }
            if (i != min) {
                swapCount.getAndIncrement();
                MutableTreeNode a = (MutableTreeNode) parent.getChildAt(i);
                MutableTreeNode b = (MutableTreeNode) parent.getChildAt(min);
                parent.insert(b, i);
                parent.insert(a, min);
                //MutableTreeNode node = (MutableTreeNode) parent.getChildAt(min);
                //parent.insert(node, i);
                //compareCount++;
            }
        }
    }
    public static void sortTree2(DefaultMutableTreeNode root) {
        Enumeration e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (!node.isLeaf()) {
                sort2(node);
            }
        }
    }

    private static void sort3(DefaultMutableTreeNode parent) {
//         @SuppressWarnings("unchecked")
//         Enumeration<DefaultMutableTreeNode> e = parent.children();
//         ArrayList<DefaultMutableTreeNode> children = Collections.list(e);

        int n = parent.getChildCount();
        List<DefaultMutableTreeNode> children = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            children.add((DefaultMutableTreeNode) parent.getChildAt(i));
        }

        Collections.sort(children, tnc);
        parent.removeAllChildren();
        for (MutableTreeNode node: children) {
            parent.add(node);
        }
    }
    public static void sortTree3(DefaultMutableTreeNode root) {
        Enumeration e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (!node.isLeaf()) {
                sort3(node);
            }
        }
    }

    private static class TreeNodeComparator implements Comparator<DefaultMutableTreeNode>, Serializable {
        private static final long serialVersionUID = 1L;
        @Override public int compare(DefaultMutableTreeNode a, DefaultMutableTreeNode b) {
            compareCount.getAndIncrement();
            if (a.isLeaf() && !b.isLeaf()) {
                return 1;
            } else if (!a.isLeaf() && b.isLeaf()) {
                return -1;
            } else {
                String sa = a.getUserObject().toString();
                String sb = b.getUserObject().toString();
                return sa.compareToIgnoreCase(sb);
            }
        }
    }

    public static DefaultMutableTreeNode deepCopyTree(DefaultMutableTreeNode src, DefaultMutableTreeNode tgt) {
        for (int i = 0; i < src.getChildCount(); i++) {
            DefaultMutableTreeNode node  = (DefaultMutableTreeNode) src.getChildAt(i);
            DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject()); //(DefaultMutableTreeNode) node.clone();
            tgt.add(clone);
            if (!node.isLeaf()) {
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
        while (row<tree.getRowCount()) {
            tree.expandRow(row++);
        }
    }
}
