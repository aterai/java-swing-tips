// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultMutableTreeNode root = TreeUtils.makeTreeRoot();
    JTree tree = new JTree(new DefaultTreeModel(TreeUtils.makeTreeRoot()));
    // JRadioButton sort0 = new JRadioButton("0: bubble sort");
    JRadioButton sort1 = new JRadioButton("1: bubble sort");
    JRadioButton sort2 = new JRadioButton("2: selection sort");
    // JRadioButton sort3 = new JRadioButton("3: iterative merge sort"); // JDK 1.6.0
    JRadioButton sort3 = new JRadioButton("3: TimSort"); // JDK 1.7.0
    JRadioButton reset = new JRadioButton("reset");

    JPanel box = new JPanel(new GridLayout(2, 2));
    ActionListener listener = e -> {
      JRadioButton check = (JRadioButton) e.getSource();
      if (check.equals(reset)) {
        tree.setModel(new DefaultTreeModel(root));
      } else {
        TreeUtils.COMPARE_COUNTER.set(0);
        TreeUtils.SWAP_COUNTER.set(0);
        DefaultMutableTreeNode clone = (DefaultMutableTreeNode) root.clone();
        DefaultMutableTreeNode r = TreeUtils.deepCopy(root, clone);
        if (check.equals(sort1)) {
          TreeUtils.sortTree1(r);
        } else if (check.equals(sort2)) {
          TreeUtils.sortTree2(r);
        } else {
          TreeUtils.sortTree3(r);
        }
        swapCounter(check);
        tree.setModel(new DefaultTreeModel(r));
      }
      TreeUtils.expandAll(tree);
    };
    ButtonGroup bg = new ButtonGroup();
    Stream.of(reset, sort1, sort2, sort3).forEach(check -> {
      box.add(check);
      bg.add(check);
      check.addActionListener(listener);
    });
    add(box, BorderLayout.SOUTH);

    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder("Sort JTree"));
    p.add(new JScrollPane(tree));
    add(p);
    TreeUtils.expandAll(tree);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void swapCounter(JRadioButton check) {
    String title = check.getText();
    if (TreeUtils.SWAP_COUNTER.get() == 0) {
      int cc = TreeUtils.COMPARE_COUNTER.get();
      check.setToolTipText(String.format("%-24s - compare: %3d, swap: ---%n", title, cc));
    } else {
      int cc = TreeUtils.COMPARE_COUNTER.get();
      int sc = TreeUtils.SWAP_COUNTER.get();
      check.setToolTipText(String.format("%-24s - compare: %3d, swap: %3d%n", title, cc, sc));
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class TreeUtils {
  public static final AtomicInteger COMPARE_COUNTER = new AtomicInteger();
  public static final AtomicInteger SWAP_COUNTER = new AtomicInteger();

  // // JDK 1.7.0
  // TreeNodeComparator COMPARATOR = new TreeNodeComparator();
  // class TreeNodeComparator implements Comparator<DefaultMutableTreeNode>, Serializable {
  //   private static final long serialVersionUID = 1L;
  //   @Override public int compare(DefaultMutableTreeNode n1, DefaultMutableTreeNode n2) {
  //     COMPARE_COUNTER.getAndIncrement();
  //     if (n1.isLeaf() && !n2.isLeaf()) {
  //       return 1;
  //     } else if (!n1.isLeaf() && n2.isLeaf()) {
  //       return -1;
  //     } else {
  //       String s1 = n1.getUserObject().toString();
  //       String s2 = n2.getUserObject().toString();
  //       return s1.compareToIgnoreCase(s2);
  //     }
  //   }
  // }

  // JDK 1.8.0
  private static final Comparator<DefaultMutableTreeNode> COMPARATOR =
      Comparator.comparing(DefaultMutableTreeNode::isLeaf)
          .thenComparing(n -> n.getUserObject().toString());

  private TreeUtils() {
    /* Singleton */
  }

  // // https://community.oracle.com/thread/1355435 How to sort jTree Nodes
  // public static void sortTree0(DefaultMutableTreeNode root) {
  //   for (int i = 0; i < root.getChildCount(); i++) {
  //     DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
  //     for (int j = 0; j < i; j++) {
  //       DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) root.getChildAt(j);
  //       COMPARE_COUNTER.getAndIncrement();
  //       if (COMPARATOR.compare(node, prevNode) < 0) {
  //         root.insert(node, j);
  //         root.insert(prevNode, i);
  //         SWAP_COUNTER.getAndIncrement();
  //         i--; // add
  //         break;
  //       }
  //     }
  //     if (!node.isLeaf()) {
  //       sortTree0(node);
  //     }
  //   }
  // }

  public static void sortTree1(DefaultMutableTreeNode root) {
    int n = root.getChildCount();
    for (int i = 0; i < n - 1; i++) {
      for (int j = n - 1; j > i; j--) {
        DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) root.getChildAt(j);
        DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) root.getChildAt(j - 1);
        if (!prevNode.isLeaf()) {
          sortTree1(prevNode);
        }
        if (COMPARATOR.compare(prevNode, curNode) > 0) {
          SWAP_COUNTER.getAndIncrement();
          root.insert(curNode, j - 1);
          root.insert(prevNode, j);
        }
      }
    }
  }

  private static void sort2(DefaultMutableTreeNode parent) {
    int n = parent.getChildCount();
    for (int i = 0; i < n - 1; i++) {
      int min = i;
      for (int j = i + 1; j < n; j++) {
        if (COMPARATOR.compare((DefaultMutableTreeNode) parent.getChildAt(min),
            (DefaultMutableTreeNode) parent.getChildAt(j)) > 0) {
          min = j;
        }
      }
      if (i != min) {
        SWAP_COUNTER.getAndIncrement();
        MutableTreeNode a = (MutableTreeNode) parent.getChildAt(i);
        MutableTreeNode b = (MutableTreeNode) parent.getChildAt(min);
        parent.insert(b, i);
        parent.insert(a, min);
        // MutableTreeNode node = (MutableTreeNode) parent.getChildAt(min);
        // parent.insert(node, i);
        // COMPARE_COUNTER++;
      }
    }
  }

  public static void sortTree2(DefaultMutableTreeNode parent) {
    // Java 9: Collections.list(parent.preorderEnumeration()).stream()
    Collections.list((Enumeration<?>) parent.preorderEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(node -> !node.isLeaf())
        .forEach(TreeUtils::sort2);
  }

  private static void sort3(DefaultMutableTreeNode parent) {
    // @SuppressWarnings("unchecked")
    // Enumeration<DefaultMutableTreeNode> e = parent.children();
    // ArrayList<DefaultMutableTreeNode> children = Collections.list(e);

    int n = parent.getChildCount();
    List<DefaultMutableTreeNode> children = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      children.add((DefaultMutableTreeNode) parent.getChildAt(i));
    }

    children.sort(COMPARATOR);
    parent.removeAllChildren();
    children.forEach(parent::add);
  }

  public static void sortTree3(DefaultMutableTreeNode parent) {
    // Java 9: Collections.list(parent.preorderEnumeration()).stream()
    Collections.list((Enumeration<?>) parent.preorderEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(node -> !node.isLeaf())
        .forEach(TreeUtils::sort3);
  }

  public static DefaultMutableTreeNode deepCopy(MutableTreeNode src, DefaultMutableTreeNode tgt) {
    // Java 9: Collections.list(src.children()).stream()
    Collections.list((Enumeration<?>) src.children()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .forEach(node -> {
          DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
          tgt.add(clone);
          if (!node.isLeaf()) {
            deepCopy(node, clone);
          }
        });
    // for (int i = 0; i < src.getChildCount(); i++) {
    //   DefaultMutableTreeNode node = (DefaultMutableTreeNode) src.getChildAt(i);
    //   DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
    //   // DefaultMutableTreeNode clone = (DefaultMutableTreeNode) node.clone();
    //   tgt.add(clone);
    //   if (!node.isLeaf()) {
    //     deepCopyTree(node, clone);
    //   }
    // }
    return tgt;
  }

  public static DefaultMutableTreeNode makeTreeRoot() {
    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
    DefaultMutableTreeNode set4 = new DefaultMutableTreeNode("Set 004");
    set1.add(new DefaultMutableTreeNode("3333333333333333"));
    set1.add(new DefaultMutableTreeNode("111111111"));
    set1.add(new DefaultMutableTreeNode("22222222222"));
    set1.add(set4);
    set1.add(new DefaultMutableTreeNode("222222"));
    set1.add(new DefaultMutableTreeNode("222222222"));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
    set2.add(new DefaultMutableTreeNode("eee eee eee eee e"));
    set2.add(new DefaultMutableTreeNode("bbb ccc aaa bbb"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
    set3.add(new DefaultMutableTreeNode("zzz zz zz"));
    set3.add(new DefaultMutableTreeNode("aaa aaa aaa aaa"));
    set3.add(new DefaultMutableTreeNode("ccc ccc ccc"));

    set4.add(new DefaultMutableTreeNode("22222222222"));
    set4.add(new DefaultMutableTreeNode("eee eee eee ee ee"));
    set4.add(new DefaultMutableTreeNode("bbb bbb bbb bbb"));
    set4.add(new DefaultMutableTreeNode("zzz zz zz"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    root.add(new DefaultMutableTreeNode("xxx xxx xxx xx xx"));
    root.add(set3);
    root.add(new DefaultMutableTreeNode("eee eee eee ee ee"));
    root.add(set1);
    root.add(set2);
    root.add(new DefaultMutableTreeNode("222222222222"));
    root.add(new DefaultMutableTreeNode("bbb bbb bbb bbb"));
    return root;
  }

  public static void expandAll(JTree tree) {
    int row = 0;
    while (row < tree.getRowCount()) {
      tree.expandRow(row++);
    }
  }
}
