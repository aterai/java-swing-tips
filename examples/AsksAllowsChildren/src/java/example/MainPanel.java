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
    super(new GridLayout(1, 2));
    JTree t = new JTree(makeDefaultTreeModel());
    t.setComponentPopupMenu(new TreePopupMenu());
    add(makeTitledPanel("Default", new JScrollPane(t)));

    DefaultTreeModel model = makeDefaultTreeModel();
    JTree tree = new JTree(model);
    tree.setComponentPopupMenu(new TreePopupMenu());
    // model.setAsksAllowsChildren(true);

    JCheckBox check = new JCheckBox("setAsksAllowsChildren");
    check.addActionListener(e -> {
      model.setAsksAllowsChildren(((JCheckBox) e.getSource()).isSelected());
      tree.repaint();
    });

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(tree));
    p.add(check, BorderLayout.SOUTH);

    add(makeTitledPanel("setAsksAllowsChildren", p));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  private static DefaultTreeModel makeDefaultTreeModel() {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    DefaultMutableTreeNode parent;

    parent = new DefaultMutableTreeNode("colors");
    root.add(parent);
    parent.add(new DefaultMutableTreeNode("blue", false));
    parent.add(new DefaultMutableTreeNode("violet", false));
    parent.add(new DefaultMutableTreeNode("red", false));
    parent.add(new DefaultMutableTreeNode("yellow", false));

    parent = new DefaultMutableTreeNode("sports");
    root.add(parent);
    parent.add(new DefaultMutableTreeNode("basketball", false));
    parent.add(new DefaultMutableTreeNode("soccer", false));
    parent.add(new DefaultMutableTreeNode("football", false));
    parent.add(new DefaultMutableTreeNode("hockey", false));

    parent = new DefaultMutableTreeNode("food");
    root.add(parent);
    parent.add(new DefaultMutableTreeNode("hot dogs", false));
    parent.add(new DefaultMutableTreeNode("pizza", false));
    parent.add(new DefaultMutableTreeNode("ravioli", false));
    parent.add(new DefaultMutableTreeNode("bananas", false));

    parent = new DefaultMutableTreeNode("test");
    root.add(parent);

    return new DefaultTreeModel(root);
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
  private final JTextField textField = new JTextField(24) {
    private transient AncestorListener listener;
    @Override public void updateUI() {
      removeAncestorListener(listener);
      super.updateUI();
      listener = new FocusAncestorListener();
      addAncestorListener(listener);
    }
  };
  private final JMenuItem addFolderItem;
  private final JMenuItem addNodeItem;
  private TreePath path;

  /* default */ TreePopupMenu() {
    super();
    addFolderItem = add("add folder");
    addFolderItem.addActionListener(e -> {
      JTree tree = (JTree) getInvoker();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
      DefaultMutableTreeNode child = new DefaultMutableTreeNode("New Folder", true);
      model.insertNodeInto(child, parent, parent.getChildCount());
      tree.scrollPathToVisible(new TreePath(child.getPath()));
    });
    addNodeItem = add("add node");
    addNodeItem.addActionListener(e -> {
      JTree tree = (JTree) getInvoker();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
      DefaultMutableTreeNode child = new DefaultMutableTreeNode("New Item", false);
      model.insertNodeInto(child, parent, parent.getChildCount());
      tree.scrollPathToVisible(new TreePath(child.getPath()));
    });
    add("edit").addActionListener(e -> {
      Object node = path.getLastPathComponent();
      if (node instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) node;
        textField.setText(leaf.getUserObject().toString());
        JTree tree = (JTree) getInvoker();
        int ret = JOptionPane.showConfirmDialog(
            tree, textField, "edit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ret == JOptionPane.OK_OPTION) {
          tree.getModel().valueForPathChanged(path, textField.getText());
        }
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
      path = tree.getPathForLocation(x, y);
      Optional.ofNullable(path).ifPresent(treePath -> {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        boolean flag = node.getAllowsChildren();
        addFolderItem.setEnabled(flag);
        addNodeItem.setEnabled(flag);
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
