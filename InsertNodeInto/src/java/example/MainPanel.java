// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.time.LocalDateTime;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTree tree1 = new JTree();
    JTree tree2 = new JTree();

    JPanel p1 = new JPanel(new GridLayout(1, 2));
    p1.add(makeTitledScrollPane(tree1, "p.add(c) & m.reload(p)"));
    p1.add(makeTitledScrollPane(tree2, "m.insertNodeInto(c, p, p.size)"));

    JButton expandButton = new JButton("expand all");
    expandButton.addActionListener(e -> {
      expandAll(tree1);
      expandAll(tree2);
    });

    JButton addButton = new JButton("add");
    addButton.addActionListener(e -> {
      LocalDateTime date = LocalDateTime.now();

      DefaultTreeModel model1 = (DefaultTreeModel) tree1.getModel();
      DefaultMutableTreeNode parent1 = (DefaultMutableTreeNode) model1.getRoot();
      DefaultMutableTreeNode child1 = new DefaultMutableTreeNode(date);
      parent1.add(child1);
      model1.reload(parent1);
      tree1.scrollPathToVisible(new TreePath(child1.getPath()));

      DefaultTreeModel model2 = (DefaultTreeModel) tree2.getModel();
      DefaultMutableTreeNode parent2 = (DefaultMutableTreeNode) model2.getRoot();
      DefaultMutableTreeNode child2 = new DefaultMutableTreeNode(date);
      model2.insertNodeInto(child2, parent2, parent2.getChildCount());
      tree2.scrollPathToVisible(new TreePath(child2.getPath()));
    });

    JPanel p2 = new JPanel(new GridLayout(1, 2));
    p2.add(expandButton);
    p2.add(addButton);

    add(p1);
    add(p2, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  protected static void expandAll(JTree tree) {
    int row = 0;
    while (row < tree.getRowCount()) {
      tree.expandRow(row);
      row++;
    }
  }

  protected static JScrollPane makeTitledScrollPane(Component view, String title) {
    JScrollPane scroll = new JScrollPane(view);
    scroll.setBorder(BorderFactory.createTitledBorder(title));
    return scroll;
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
