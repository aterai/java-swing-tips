// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTree tree = new JTree(makeModel());
    tree.setRootVisible(false);
    tree.addTreeWillExpandListener(new TreeWillExpandListener() {
      private boolean isAdjusting;
      @Override public void treeWillExpand(TreeExpansionEvent e) { // throws ExpandVetoException {
        // collapseAll(tree); // StackOverflowError when collapsing nodes below 2nd level
        if (isAdjusting) {
          return;
        }
        isAdjusting = true;
        collapseFirstHierarchy(tree);
        tree.setSelectionPath(e.getPath());
        isAdjusting = false;
      }

      @Override public void treeWillCollapse(TreeExpansionEvent e) { // throws ExpandVetoException {
        // throw new ExpandVetoException(e, "Tree collapse cancelled");
      }
    });

    JScrollPane scroll = new JScrollPane(tree) {
      @Override public void updateUI() {
        setViewportBorder(null);
        super.updateUI();
        Border b = BorderFactory.createLineBorder(getViewport().getView().getBackground(), 5);
        EventQueue.invokeLater(() -> setViewportBorder(b));
      }
    };
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultTreeModel makeModel() {
    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
    set1.add(new DefaultMutableTreeNode("111111111"));
    set1.add(new DefaultMutableTreeNode("22222222222"));
    set1.add(new DefaultMutableTreeNode("33333"));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
    set2.add(new DefaultMutableTreeNode("4444444444444"));
    set2.add(new DefaultMutableTreeNode("55555555"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
    set3.add(new DefaultMutableTreeNode("666666666666666"));
    set3.add(new DefaultMutableTreeNode("777777777777"));
    set3.add(new DefaultMutableTreeNode("888888888888888888"));

    DefaultMutableTreeNode set4 = new DefaultMutableTreeNode("Set 004");
    set4.add(new DefaultMutableTreeNode("444"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    root.add(set1);
    root.add(set2);
    set2.add(set3);
    root.add(set4);
    return new DefaultTreeModel(root);
  }

  public static void collapseFirstHierarchy(JTree tree) {
    TreeModel model = tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

    // // Java 9:
    // Collections.list(root.breadthFirstEnumeration()).stream()
    //   .filter(DefaultMutableTreeNode.class::isInstance)
    //   .map(DefaultMutableTreeNode.class::cast)
    //   .takeWhile(node -> node.getLevel() <= 1)
    //   .dropWhile(DefaultMutableTreeNode::isRoot)
    //   .dropWhile(DefaultMutableTreeNode::isLeaf)
    //   .map(DefaultMutableTreeNode::getPath)
    //   .map(TreePath::new)
    //   .forEach(tree::collapsePath);

    // Java 9: Enumeration<TreeNode> e = root.breadthFirstEnumeration();
    Enumeration<?> e = root.breadthFirstEnumeration();
    while (e.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
      boolean isOverFirstLevel = node.getLevel() > 1;
      if (isOverFirstLevel) { // Collapse only nodes in the first hierarchy
        return;
      } else if (node.isLeaf() || node.isRoot()) {
        continue;
      }
      collapseNode(tree, node);
    }
  }

  private static void collapseNode(JTree tree, DefaultMutableTreeNode node) {
    tree.collapsePath(new TreePath(node.getPath()));
  }

  // private static void collapseAll(JTree tree) {
  //   int row = tree.getRowCount() - 1;
  //   while (row >= 0) { // The root node must be hidden
  //     tree.collapseRow(row--);
  //   }
  // }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
