// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TreePopupMenu extends JPopupMenu {
  protected TreePath path;
  private final Action addFolderAction = new AbstractAction("add folder") {
    @Override public void actionPerformed(ActionEvent e) {
      JTree tree = (JTree) getInvoker();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
      DefaultMutableTreeNode child = new DefaultMutableTreeNode("New Folder", true);
      model.insertNodeInto(child, parent, parent.getChildCount());
      tree.scrollPathToVisible(new TreePath(child.getPath()));
    }
  };
  private final Action addItemAction = new AbstractAction("add item") {
    @Override public void actionPerformed(ActionEvent e) {
      JTree tree = (JTree) getInvoker();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
      DefaultMutableTreeNode child = new DefaultMutableTreeNode("New Item", false);
      model.insertNodeInto(child, parent, parent.getChildCount());
      tree.scrollPathToVisible(new TreePath(child.getPath()));
    }
  };
  private final Action editNodeAction = new AbstractAction("edit") {
    protected final JTextField textField = new JTextField(24) {
      protected transient AncestorListener listener;
      @Override public void updateUI() {
        removeAncestorListener(listener);
        super.updateUI();
        listener = new AncestorListener() {
          @Override public void ancestorAdded(AncestorEvent e) {
            requestFocusInWindow();
          }

          @Override public void ancestorMoved(AncestorEvent e) { /* not needed */ }

          @Override public void ancestorRemoved(AncestorEvent e) { /* not needed */ }
        };
        addAncestorListener(listener);
      }
    };
    @Override public void actionPerformed(ActionEvent e) {
      Object node = path.getLastPathComponent();
      if (node instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) node;
        textField.setText(leaf.getUserObject().toString());
        JTree tree = (JTree) getInvoker();
        int ret = JOptionPane.showConfirmDialog(tree, textField, "edit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ret == JOptionPane.OK_OPTION) {
          Optional.ofNullable(textField.getText())
            .filter(str -> !str.trim().isEmpty())
            .ifPresent(str -> ((DefaultTreeModel) tree.getModel()).valueForPathChanged(path, str));
        }
      }
    }
  };
  private final Action removeNodeAction = new AbstractAction("remove") {
    @Override public void actionPerformed(ActionEvent e) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
      if (!node.isRoot()) {
        JTree tree = (JTree) getInvoker();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.removeNodeFromParent(node);
      }
    }
  };

  protected TreePopupMenu() {
    super();
    add(addFolderAction);
    add(addItemAction);
    add(editNodeAction);
    addSeparator();
    add(removeNodeAction);
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTree) {
      JTree tree = (JTree) c;
      path = tree.getPathForLocation(x, y);
      Optional.ofNullable(path).ifPresent(treePath -> {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        boolean flag = node.getAllowsChildren();
        addFolderAction.setEnabled(flag);
        addItemAction.setEnabled(flag);
        super.show(c, x, y);
      });
    }
  }
}
