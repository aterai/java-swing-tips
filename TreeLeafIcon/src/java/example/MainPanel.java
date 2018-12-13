// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTree tree = new JTree();
    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
    }

    // TEST: Icon emptyIcon = null;
    Icon emptyIcon = new EmptyIcon();

    DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
    renderer.setOpenIcon(emptyIcon);
    renderer.setClosedIcon(emptyIcon);
    renderer.setLeafIcon(emptyIcon);
    allNodesChanged(tree);

    JCheckBox folderCheck = new JCheckBox("OpenIcon, ClosedIcon");
    folderCheck.addActionListener(e -> {
      DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) tree.getCellRenderer();
      if (((JCheckBox) e.getSource()).isSelected()) {
        r.setOpenIcon(r.getDefaultOpenIcon());
        r.setClosedIcon(r.getDefaultClosedIcon());
      } else {
        r.setOpenIcon(emptyIcon);
        r.setClosedIcon(emptyIcon);
      }
      allNodesChanged(tree);
    });

    JCheckBox leafCheck = new JCheckBox("LeafIcon");
    leafCheck.addActionListener(e -> {
      DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) tree.getCellRenderer();
      if (((JCheckBox) e.getSource()).isSelected()) {
        r.setLeafIcon(r.getDefaultLeafIcon());
      } else {
        r.setLeafIcon(emptyIcon);
      }
      allNodesChanged(tree);
    });

    // tree.setCellRenderer(new DefaultTreeCellRenderer() {
    //   @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    //     super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    //     if (leaf) {
    //       if (!leafCheck.isSelected()) {
    //         setIcon(null);
    //       } else {
    //         setIcon(getDefaultLeafIcon());
    //       }
    //     } else {
    //       if (!folderCheck.isSelected()) {
    //         setIcon(null);
    //       } else {
    //         if (expanded) {
    //           setIcon(getDefaultOpenIcon());
    //         } else {
    //           setIcon(getDefaultClosedIcon());
    //         }
    //       }
    //     }
    //     return this;
    //   }
    // });

    JPanel np = new JPanel();
    np.add(folderCheck);
    np.add(leafCheck);

    add(np, BorderLayout.NORTH);
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void allNodesChanged(JTree tree) {
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    // Java 9: Collections.list(root.preorderEnumeration()).stream()
    Collections.list((Enumeration<?>) root.preorderEnumeration()).stream()
      .filter(TreeNode.class::isInstance).map(TreeNode.class::cast)
      .forEach(model::nodeChanged);
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

class EmptyIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* Empty icon */ }

  @Override public int getIconWidth() {
    return 2;
  }

  @Override public int getIconHeight() {
    return 0;
  }
}
