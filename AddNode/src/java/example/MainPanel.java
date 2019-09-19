// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
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

class TreePopupMenu extends JPopupMenu {
  private TreePath path;

  protected TreePopupMenu() {
    super();
    JTextField textField = new JTextField(24) {
      private transient AncestorListener listener;
      @Override public void updateUI() {
        removeAncestorListener(listener);
        super.updateUI();
        listener = new FocusAncestorListener();
        addAncestorListener(listener);
      }
    };

    add("add").addActionListener(e -> {
      // https://ateraimemo.com/Swing/ScrollRectToVisible.html
      JTree tree = (JTree) getInvoker();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
      DefaultMutableTreeNode child = new DefaultMutableTreeNode("New node");
      model.insertNodeInto(child, parent, parent.getChildCount());
      tree.scrollPathToVisible(new TreePath(child.getPath()));
    });
    add("add & reload").addActionListener(e -> {
      JTree tree = (JTree) getInvoker();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
      DefaultMutableTreeNode child = new DefaultMutableTreeNode("New node");
      parent.add(child);
      model.reload(parent); // = model.nodeStructureChanged(parent);
      tree.scrollPathToVisible(new TreePath(child.getPath()));
    });
    add("edit").addActionListener(e -> {
      Object node = path.getLastPathComponent();
      if (!(node instanceof DefaultMutableTreeNode)) {
        return;
      }
      DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) node;
      textField.setText(leaf.getUserObject().toString());
      JTree tree = (JTree) getInvoker();
      int ret = JOptionPane.showConfirmDialog(
          tree, textField, "edit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (ret == JOptionPane.OK_OPTION) {
        tree.getModel().valueForPathChanged(path, textField.getText());
        // leaf.setUserObject(str);
        // model.nodeChanged(leaf);
      }
    });
    addSeparator();
    add("remove").addActionListener(e -> {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
      if (!node.isRoot()) {
        JTree tree = (JTree) getInvoker();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.removeNodeFromParent(node);
      }
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTree) {
      JTree tree = (JTree) c;
      // TreePath[] tsp = tree.getSelectionPaths();
      path = tree.getPathForLocation(x, y);
      // if (Objects.nonNull(path) && Arrays.asList(tsp).contains(path)) {
      Optional.ofNullable(path).ifPresent(treePath -> {
        tree.setSelectionPath(treePath);
        super.show(c, x, y);
      });
    }
  }
}

class FocusAncestorListener implements AncestorListener {
  @Override public void ancestorAdded(AncestorEvent e) {
    e.getComponent().requestFocusInWindow();
  }

  @Override public void ancestorMoved(AncestorEvent e) {
    /* not needed */
  }

  @Override public void ancestorRemoved(AncestorEvent e) {
    /* not needed */
  }
}
