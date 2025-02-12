// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private final JLabel countLabel = new JLabel("PathCount: ");
  private final JLabel levelLabel = new JLabel("Level: ");

  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new JTree();
    tree.setComponentPopupMenu(new TreePopupMenu());
    tree.getSelectionModel().addTreeSelectionListener(e ->
        Optional.ofNullable(e.getNewLeadSelectionPath()).ifPresent(this::updateLabel));

    JCheckBox check = new JCheckBox("JTree#setRootVisible(...)", true);
    check.addActionListener(e ->
        tree.setRootVisible(((JCheckBox) e.getSource()).isSelected()));

    JPanel p = new JPanel(new GridLayout(0, 1, 2, 2));
    p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    p.add(countLabel);
    p.add(levelLabel);

    add(check, BorderLayout.NORTH);
    add(new JScrollPane(tree));
    add(p, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public void updateLabel(TreePath path) {
    countLabel.setText("PathCount: " + path.getPathCount());
    Object o = path.getLastPathComponent();
    if (o instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode n = (DefaultMutableTreeNode) o;
      levelLabel.setText("Level: " + n.getLevel());
    }
  }

  public final class TreePopupMenu extends JPopupMenu {
    private static final int MAX_NODE_LEVELS = 2;

    /* default */ TreePopupMenu() {
      super();
      add("path").addActionListener(e -> {
        JTree tree = (JTree) getInvoker();
        TreePath path = tree.getSelectionPath();
        if (path != null) {
          updateLabel(path);
          JOptionPane.showMessageDialog(tree, path, "path", JOptionPane.INFORMATION_MESSAGE);
        }
      });
      add("add").addActionListener(e -> {
        JTree tree = (JTree) getInvoker();
        TreePath path = tree.getSelectionPath();
        if (path != null && path.getPathCount() <= MAX_NODE_LEVELS) {
          addNode(tree, path, "New child node");
        } else {
          String msg = String.format("ERROR: Maximum levels of %d exceeded.", MAX_NODE_LEVELS);
          JOptionPane.showMessageDialog(tree, msg, "add node", JOptionPane.ERROR_MESSAGE);
        }
      });
    }

    public void addNode(JTree tree, TreePath path, Object userObject) {
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      DefaultMutableTreeNode self = (DefaultMutableTreeNode) path.getLastPathComponent();
      DefaultMutableTreeNode child = new DefaultMutableTreeNode(userObject);
      self.add(child);
      model.reload(self);
    }

    @Override public void show(Component c, int x, int y) {
      if (c instanceof JTree && ((JTree) c).getSelectionCount() > 0) {
        super.show(c, x, y);
      }
    }
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
