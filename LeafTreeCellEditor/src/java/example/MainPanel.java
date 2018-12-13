package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    JTree tree1 = new JTree();
    tree1.setEditable(true);

    JTree tree2 = new JTree();
    tree2.setCellEditor(makeLeafTreeCellEditor(tree2));
    tree2.setEditable(true);

    add(makeTitledPanel("DefaultTreeCellEditor", new JScrollPane(tree1)));
    add(makeTitledPanel("LeafTreeCellEditor", new JScrollPane(tree2)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TreeCellEditor makeLeafTreeCellEditor(JTree tree) {
    return new DefaultTreeCellEditor(tree, (DefaultTreeCellRenderer) tree.getCellRenderer()) {
      @Override public boolean isCellEditable(EventObject e) {
        Object o = tree.getLastSelectedPathComponent();
        if (super.isCellEditable(e) && o instanceof TreeNode) {
          return ((TreeNode) o).isLeaf();
        } else {
          return false;
        }
      }
    };
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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

// class LeafTreeCellEditor extends DefaultTreeCellEditor {
//   protected LeafTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
//     super(tree, renderer);
//   }
//   @Override public boolean isCellEditable(EventObject e) {
//     Object o = tree.getLastSelectedPathComponent();
//     if (super.isCellEditable(e) && o instanceof TreeNode) {
//       return ((TreeNode) o).isLeaf();
//     } else {
//       return false;
//     }
//   }
// }
