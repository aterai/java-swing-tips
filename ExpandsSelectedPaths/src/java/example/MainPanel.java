// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTree tree1 = new JTree();
    tree1.setExpandsSelectedPaths(false);

    JTree tree2 = new JTree();
    tree2.setExpandsSelectedPaths(true);

    JPanel p1 = new JPanel(new GridLayout(1, 2));
    p1.setBorder(BorderFactory.createTitledBorder("setExpandsSelectedPaths"));
    p1.add(makeTitledScrollPane(tree1, "false"));
    p1.add(makeTitledScrollPane(tree2, "true"));

    JTextField textField = new JTextField("soccer");
    JButton button = new JButton("Select");
    button.addActionListener(e -> {
      String q = textField.getText().trim();
      searchTree(tree1, tree1.getPathForRow(0), q);
      searchTree(tree2, tree2.getPathForRow(0), q);
    });
    JPanel p2 = new JPanel(new BorderLayout());
    p2.add(textField);
    p2.add(button, BorderLayout.EAST);

    add(p1);
    add(p2, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JScrollPane makeTitledScrollPane(Component view, String title) {
    JScrollPane scroll = new JScrollPane(view);
    scroll.setBorder(BorderFactory.createTitledBorder(title));
    return scroll;
  }

  private static void searchTree(JTree tree, TreePath path, String q) {
    Object o = path.getLastPathComponent();
    if (o instanceof TreeNode) {
      TreeNode node = (TreeNode) o;
      if (node.toString().equals(q)) {
        tree.addSelectionPath(path);
      }
      if (!node.isLeaf()) {
        // Java 9: Collections.list(node.children()).stream()
        Collections.list((Enumeration<?>) node.children()).stream()
          .forEach(n -> searchTree(tree, path.pathByAddingChild(n), q));
      }
    }
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
