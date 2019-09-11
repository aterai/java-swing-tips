// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultComboBoxModel<TreeNode> model1 = new DefaultComboBoxModel<>();
    DefaultComboBoxModel<TreeNode> model2 = new DefaultComboBoxModel<>();
    TreeModel tm = makeModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) tm.getRoot();
    // // Java 9: Enumeration<TreeNode> depth = root.depthFirstEnumeration();
    // Enumeration<?> depth = root.depthFirstEnumeration();
    // while (depth.hasMoreElements()) {
    //   DefaultMutableTreeNode node = (DefaultMutableTreeNode) depth.nextElement();
    //   if (node.isRoot()) {
    //     break;
    //   }
    //   model.insertElementAt(node, 0);
    // }
    makeComboBoxModel(model1, root);
    makeComboBoxModel(model2, root);
    TreeComboBox<TreeNode> combo = new TreeComboBox<>();
    combo.setModel(model2);
    combo.setSelectedIndex(-1);

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("default:", new JComboBox<>(model1)));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Tree ComboBoxModel:", combo));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void makeComboBoxModel(DefaultComboBoxModel<TreeNode> model, TreeNode node) {
    if (node instanceof DefaultMutableTreeNode && !((DefaultMutableTreeNode) node).isRoot()) {
      model.addElement(node);
    }
    if (!node.isLeaf()) {
      // Java 9: Collections.list(node.children()).stream()
      Collections.list((Enumeration<?>) node.children()).stream()
        .filter(TreeNode.class::isInstance).map(TreeNode.class::cast)
        .forEach(n -> makeComboBoxModel(model, n));
    }
  }

  private static TreeModel makeModel() {
    return new JTree().getModel();
    // DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    // DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
    // DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
    // DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
    // set1.add(new DefaultMutableTreeNode("111111111"));
    // set1.add(new DefaultMutableTreeNode("22222222222"));
    // set1.add(new DefaultMutableTreeNode("33333"));
    // set2.add(new DefaultMutableTreeNode("44444444444"));
    // set2.add(set3);
    // set2.add(new DefaultMutableTreeNode("5555"));
    // set3.add(new DefaultMutableTreeNode("666666666666"));
    // set3.add(new DefaultMutableTreeNode("7777777"));
    // set3.add(new DefaultMutableTreeNode("88888888888888888"));
    // root.add(set1);
    // root.add(set2);
    // return new DefaultTreeModel(root);
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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

class TreeComboBox<E extends TreeNode> extends JComboBox<E> {
  private boolean isNotSelectableIndex;
  private final Action up = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      int si = getSelectedIndex();
      for (int i = si - 1; i >= 0; i--) {
        if (getItemAt(i).isLeaf()) {
          setSelectedIndex(i);
          break;
        }
      }
    }
  };
  private final Action down = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      int si = getSelectedIndex();
      for (int i = si + 1; i < getModel().getSize(); i++) {
        if (getItemAt(i).isLeaf()) {
          setSelectedIndex(i);
          break;
        }
      }
    }
  };

  @Override public void updateUI() {
    super.updateUI();
    ListCellRenderer<? super E> renderer = getRenderer();
    setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      JLabel l = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      l.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
      if (index >= 0 && value instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        int indent = Math.max(0, node.getLevel() - 1) * 16;
        l.setBorder(BorderFactory.createEmptyBorder(1, indent + 1, 1, 1));
        if (!value.isLeaf()) {
          l.setForeground(Color.WHITE);
          l.setBackground(Color.GRAY.darker());
        }
      }
      return l;
    });
    EventQueue.invokeLater(() -> {
      ActionMap am = getActionMap();
      am.put("selectPrevious3", up);
      am.put("selectNext3", down);
      InputMap im = getInputMap();
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "selectPrevious3");
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), "selectPrevious3");
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "selectNext3");
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "selectNext3");
    });
  }

  @Override public void setPopupVisible(boolean v) {
    if (!v && isNotSelectableIndex) {
      isNotSelectableIndex = false;
    } else {
      super.setPopupVisible(v);
    }
  }

  @Override public void setSelectedIndex(int index) {
    TreeNode node = getItemAt(index);
    if (Objects.nonNull(node) && node.isLeaf()) {
      super.setSelectedIndex(index);
    } else {
      isNotSelectableIndex = true;
    }
  }
}
