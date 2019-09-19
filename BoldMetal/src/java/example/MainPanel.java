// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private static final String TAG = "<html><b>";

  private MainPanel() {
    super(new BorderLayout());

    JCheckBox check = new JCheckBox("swing.boldMetal");
    check.addActionListener(e -> {
      // https://docs.oracle.com/javase/8/docs/api/javax/swing/plaf/metal/DefaultMetalTheme.html
      JCheckBox c = (JCheckBox) e.getSource();
      UIManager.put("swing.boldMetal", c.isSelected());
      // re-install the Metal Look and Feel
      try {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
      } catch (UnsupportedLookAndFeelException ex) {
        throw new IllegalStateException(ex); // should never happen
      }
      // Update the ComponentUIs for all Components. This
      // needs to be invoked for all windows.
      SwingUtilities.updateComponentTreeUI(c.getTopLevelAncestor());
    });

    JTree tree = new JTree();
    tree.setComponentPopupMenu(new TreePopupMenu());
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setBorder(BorderFactory.createTitledBorder("TitledBorder"));
    tabbedPane.addTab(TAG + "JTree", new JScrollPane(tree));
    tabbedPane.addTab("JLabel", new JLabel("JLabel"));
    tabbedPane.addTab("JTextArea", new JScrollPane(new JTextArea("JTextArea")));
    tabbedPane.addTab("JButton", new JScrollPane(new JButton("JButton")));
    tabbedPane.addChangeListener(e -> {
      JTabbedPane t = (JTabbedPane) e.getSource();
      for (int i = 0; i < t.getTabCount(); i++) {
        String title = t.getTitleAt(i);
        if (i == t.getSelectedIndex()) {
          t.setTitleAt(i, TAG + title);
        } else if (title.startsWith(TAG)) {
          t.setTitleAt(i, title.substring(TAG.length()));
        }
      }
    });
    add(check, BorderLayout.NORTH);
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    UIManager.put("swing.boldMetal", Boolean.FALSE);
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    // XXX: UIManager.put("swing.boldMetal", Boolean.FALSE);
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
