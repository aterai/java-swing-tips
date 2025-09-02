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
  private static final String BOLD_KEY = "swing.boldMetal";
  private static final String TAG = "<html><b>";

  private MainPanel() {
    super(new BorderLayout());
    JCheckBox check = new JCheckBox(BOLD_KEY);
    check.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      UIManager.put(BOLD_KEY, c.isSelected());
      reinstallMetalLookAndFeel(c);
    });
    JTree tree = new JTree();
    tree.setComponentPopupMenu(new TreePopupMenu());
    JTabbedPane tabs = new JTabbedPane();
    tabs.setBorder(BorderFactory.createTitledBorder("TitledBorder"));
    tabs.addTab(TAG + "JTree", new JScrollPane(tree));
    tabs.addTab("JLabel", new JLabel("JLabel"));
    tabs.addTab("JTextArea", new JScrollPane(new JTextArea("JTextArea")));
    tabs.addTab("JButton", new JScrollPane(new JButton("JButton")));
    tabs.addChangeListener(e -> updateTabTitle((JTabbedPane) e.getSource()));
    add(check, BorderLayout.NORTH);
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  // https://docs.oracle.com/javase/8/docs/api/javax/swing/plaf/metal/DefaultMetalTheme.html
  private static void reinstallMetalLookAndFeel(JComponent c) {
    // re-install the Metal Look and Feel
    try {
      UIManager.setLookAndFeel(new MetalLookAndFeel());
    } catch (UnsupportedLookAndFeelException ex) {
      throw new IllegalStateException(ex); // should never happen
    }
    // Update the ComponentUIs for all Components. This
    // needs to be invoked for all windows.
    SwingUtilities.updateComponentTreeUI(c.getTopLevelAncestor());
  }

  private static void updateTabTitle(JTabbedPane tabbedPane) {
    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      String title = tabbedPane.getTitleAt(i);
      if (i == tabbedPane.getSelectedIndex()) {
        tabbedPane.setTitleAt(i, TAG + title);
      } else if (title.startsWith(TAG)) {
        tabbedPane.setTitleAt(i, title.substring(TAG.length()));
      }
    }
  }

  public static void main(String[] args) {
    UIManager.put(BOLD_KEY, false);
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    // XXX: UIManager.put("swing.boldMetal", false);
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class TreePopupMenu extends JPopupMenu {
  private final JTextField editor = new JTextField(24) {
    private transient AncestorListener listener;

    @Override public void updateUI() {
      removeAncestorListener(listener);
      super.updateUI();
      listener = new FocusAncestorListener();
      addAncestorListener(listener);
    }
  };
  private TreePath path;

  /* default */ TreePopupMenu() {
    super();
    add("add").addActionListener(e -> addTab());
    add("edit").addActionListener(e -> editTab());
    addSeparator();
    add("remove").addActionListener(e -> removeTab());
  }

  private void addTab() {
    // https://ateraimemo.com/Swing/ScrollRectToVisible.html
    JTree tree = (JTree) getInvoker();
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
    DefaultMutableTreeNode child = new DefaultMutableTreeNode("New node");
    model.insertNodeInto(child, parent, parent.getChildCount());
    tree.scrollPathToVisible(new TreePath(child.getPath()));
  }

  private void editTab() {
    Object node = path.getLastPathComponent();
    if (node instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) node;
      editor.setText(leaf.getUserObject().toString());
      JTree tree = (JTree) getInvoker();
      int ret = JOptionPane.showConfirmDialog(
          tree, editor, "edit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (ret == JOptionPane.OK_OPTION) {
        tree.getModel().valueForPathChanged(path, editor.getText());
        // leaf.setUserObject(str);
        // model.nodeChanged(leaf);
      }
    }
  }

  private void removeTab() {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
    if (!node.isRoot()) {
      JTree tree = (JTree) getInvoker();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      model.removeNodeFromParent(node);
    }
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
