// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new JTree();
    tree.setComponentPopupMenu(new TreePopupMenu());
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
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

final class TreePopupMenu extends JPopupMenu {
  private TreePath path;

  /* default */ TreePopupMenu() {
    super();
    add("add child node").addActionListener(e -> {
      JTree tree = (JTree) getInvoker();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      DefaultMutableTreeNode self = (DefaultMutableTreeNode) path.getLastPathComponent();
      DefaultMutableTreeNode child = new DefaultMutableTreeNode("New child node");
      self.add(child);
      model.reload(self);
      // or: model.insertNodeInto(child, self, self.getChildCount());
    });
    addSeparator();
    add("insert preceding sibling node").addActionListener(e -> {
      JTree tree = (JTree) getInvoker();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      MutableTreeNode self = (MutableTreeNode) path.getLastPathComponent();
      MutableTreeNode parent = (MutableTreeNode) self.getParent();
      MutableTreeNode child = new DefaultMutableTreeNode("New preceding sibling");
      int index = model.getIndexOfChild(parent, self);
      parent.insert(child, index);
      model.reload(parent);
      // or: model.insertNodeInto(child, parent, index);
    });
    add("insert following sibling node").addActionListener(e -> {
      JTree tree = (JTree) getInvoker();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      MutableTreeNode self = (MutableTreeNode) path.getLastPathComponent();
      MutableTreeNode parent = (MutableTreeNode) self.getParent();
      MutableTreeNode child = new DefaultMutableTreeNode("New following sibling");
      int index = model.getIndexOfChild(parent, self);
      parent.insert(child, index + 1);
      model.reload(parent);
      // or: model.insertNodeInto(child, parent, index + 1);
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTree) {
      JTree tree = (JTree) c;
      path = tree.getPathForLocation(x, y);
      Optional.ofNullable(path).ifPresent(tp -> super.show(c, x, y));
    }
  }
}
