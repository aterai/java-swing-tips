// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.Logger;
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
      addAndReload(tree1);
      insertNodeInto(tree2);
    });

    JPanel p2 = new JPanel(new GridLayout(1, 2));
    p2.add(expandButton);
    p2.add(addButton);

    add(p1);
    add(p2, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addAndReload(JTree tree) {
    LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) model.getRoot();
    DefaultMutableTreeNode child = new DefaultMutableTreeNode(date);
    parent.add(child);
    model.reload(parent);
    tree.scrollPathToVisible(new TreePath(child.getPath()));
  }

  private static void insertNodeInto(JTree tree) {
    LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) model.getRoot();
    DefaultMutableTreeNode child = new DefaultMutableTreeNode(date);
    model.insertNodeInto(child, parent, parent.getChildCount());
    tree.scrollPathToVisible(new TreePath(child.getPath()));
  }

  public static void expandAll(JTree tree) {
    int row = 0;
    while (row < tree.getRowCount()) {
      tree.expandRow(row);
      row++;
    }
  }

  public static JScrollPane makeTitledScrollPane(Component view, String title) {
    JScrollPane scroll = new JScrollPane(view);
    scroll.setBorder(BorderFactory.createTitledBorder(title));
    return scroll;
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
      Logger.getGlobal().severe(ex::getMessage);
      return;
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
