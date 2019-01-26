// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private final JLabel countLabel = new JLabel("PathCount: ");
  private final JLabel levelLabel = new JLabel("Level: ");

  public MainPanel() {
    super(new BorderLayout());

    JTree tree = new JTree();
    tree.setComponentPopupMenu(new TreePopupMenu());
    tree.getSelectionModel().addTreeSelectionListener(e -> {
      Optional.ofNullable(e.getNewLeadSelectionPath()).ifPresent(this::updateLabel);
    });

    JCheckBox check = new JCheckBox("JTree#setRootVisible(...)", true);
    check.addActionListener(e -> {
      tree.setRootVisible(((JCheckBox) e.getSource()).isSelected());
    });

    JPanel p = new JPanel(new GridLayout(0, 1, 2, 2));
    p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    p.add(countLabel);
    p.add(levelLabel);

    add(check, BorderLayout.NORTH);
    add(new JScrollPane(tree));
    add(p, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  protected void updateLabel(TreePath path) {
    countLabel.setText("PathCount: " + path.getPathCount());
    Object o = path.getLastPathComponent();
    if (o instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode n = (DefaultMutableTreeNode) o;
      levelLabel.setText("Level: " + n.getLevel());
    }
  }

  protected class TreePopupMenu extends JPopupMenu {
    private static final int NODE_MAXIMUM_LEVELS = 2;

    protected TreePopupMenu() {
      super();
      add("path").addActionListener(e -> {
        JTree tree = (JTree) getInvoker();
        updateLabel(tree.getSelectionPath());
        JOptionPane.showMessageDialog(tree, tree.getSelectionPaths(), "path", JOptionPane.INFORMATION_MESSAGE);
      });
      add("add").addActionListener(e -> {
        JTree tree = (JTree) getInvoker();
        TreePath path = tree.getSelectionPath();
        if (path.getPathCount() <= NODE_MAXIMUM_LEVELS) {
          DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
          DefaultMutableTreeNode self = (DefaultMutableTreeNode) path.getLastPathComponent();
          DefaultMutableTreeNode child = new DefaultMutableTreeNode("New child node");
          self.add(child);
          model.reload(self);
        } else {
          String message = String.format("ERROR: Maximum levels of %d exceeded.", NODE_MAXIMUM_LEVELS);
          JOptionPane.showMessageDialog(tree, message, "add node", JOptionPane.ERROR_MESSAGE);
        }
      });
    }

    @Override public void show(Component c, int x, int y) {
      if (c instanceof JTree && ((JTree) c).getSelectionCount() > 0) {
        super.show(c, x, y);
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
