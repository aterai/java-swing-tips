package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTree tree = new JTree();
        //tree.setModel(makeModel());

        JButton button1 = new JButton("expand A");
        button1.addActionListener(e -> expandAll(tree));

        JButton button2 = new JButton("collapse A");
        button2.addActionListener(e -> collapseAll(tree));

        JButton button3 = new JButton("expand B");
        button3.addActionListener(e -> {
            TreeNode root = (TreeNode) tree.getModel().getRoot();
            visitAll(tree, new TreePath(root), true);
            //expandAPath(new TreePath(root));
        });

        JButton button4 = new JButton("collapse B");
        button4.addActionListener(e -> {
            TreeNode root = (TreeNode) tree.getModel().getRoot();
            visitAll(tree, new TreePath(root), false);
        });

        JPanel p = new JPanel(new GridLayout(0, 1, 2, 2));
        Arrays.asList(button1, button2, button3, button4).forEach(p::add);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(p, BorderLayout.NORTH);
        add(panel, BorderLayout.EAST);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }

//     private static DefaultTreeModel makeModel() {
//         DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
//         DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
//         DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
//         DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
//         set1.add(new DefaultMutableTreeNode("111111111"));
//         set1.add(new DefaultMutableTreeNode("22222222222"));
//         set1.add(new DefaultMutableTreeNode("33333"));
//         set2.add(new DefaultMutableTreeNode("asdfasdfas"));
//         set2.add(new DefaultMutableTreeNode("asdf"));
//         set3.add(new DefaultMutableTreeNode("asdfasdfasdf"));
//         set3.add(new DefaultMutableTreeNode("zxcvzxcvzx"));
//         set3.add(new DefaultMutableTreeNode("qwerqwerqwerqwerqwer"));
//         root.add(set1);
//         root.add(set2);
//         set2.add(set3);
//         return new DefaultTreeModel(root);
//     }

    public static Stream<TreeNode> children(TreeNode node) {
        return Collections.list((Enumeration<?>) node.children())
            .stream().filter(TreeNode.class::isInstance).map(TreeNode.class::cast);
    }

    //Expanding or Collapsing All Nodes in a JTree Component (Java Developers Almanac Example)
    //http://www.exampledepot.com/egs/javax.swing.tree/ExpandAll.html
    protected static void visitAll(JTree tree, TreePath parent, boolean expand) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        children(node).forEach(n -> visitAll(tree, parent.pathByAddingChild(n), expand));
//         if (!node.isLeaf() && node.getChildCount() >= 0) {
//             Enumeration<?> e = node.children();
//             while (e.hasMoreElements()) {
//                 visitAll(tree, parent.pathByAddingChild(e.nextElement()), expand);
//             }
//         }
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    //Expand or collapse a JTree - Real's Java How-to
    //http://www.rgagnon.com/javadetails/java-0210.html
    protected static void expandAll(JTree tree) {
        int row = 0;
        while (row < tree.getRowCount()) {
            tree.expandRow(row);
            row++;
        }
    }
    protected static void collapseAll(JTree tree) {
        int row = tree.getRowCount() - 1;
        while (row >= 0) {
            tree.collapseRow(row);
            row--;
        }
    }
//     // https://community.oracle.com/thread/1393385 How to Expand a JTree completely
//     private void expandAPath(TreePath p) {
//         tree.expandPath(p);
//         DefaultMutableTreeNode n = (DefaultMutableTreeNode) p.getLastPathComponent();
//         for (int i = 0; i < n.getChildCount(); i++) {
//             expandAPath(p.pathByAddingChild(n.getChildAt(i)));
//         }
//     }
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
