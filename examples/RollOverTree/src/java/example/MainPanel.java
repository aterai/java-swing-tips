// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new JScrollPane(new RollOverTree(makeModel())));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TreeModel makeModel() {
    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
    set1.add(new DefaultMutableTreeNode("111111111"));
    set1.add(new DefaultMutableTreeNode("22222222222"));
    set1.add(new DefaultMutableTreeNode("33333"));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
    set2.add(new DefaultMutableTreeNode("444444444"));
    set2.add(new DefaultMutableTreeNode("5555"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
    set3.add(new DefaultMutableTreeNode("666666666666"));
    set3.add(new DefaultMutableTreeNode("777777777"));
    set3.add(new DefaultMutableTreeNode("88888888888888"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    root.add(set1);
    root.add(set2);
    set2.add(set3);
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

class RollOverTree extends JTree {
  private final Color rolloverRowColor = new Color(0xDC_F0_FF);
  private int rollOverRowIndex = -1;
  private transient MouseMotionListener listener;

  protected RollOverTree(TreeModel model) {
    super(model);
  }

  @Override public void updateUI() {
    removeMouseMotionListener(listener);
    setCellRenderer(null);
    super.updateUI();
    setCellRenderer(new RollOverTreeCellRenderer());
    listener = new MouseAdapter() {
      @Override public void mouseMoved(MouseEvent e) {
        int row = getRowForLocation(e.getX(), e.getY());
        if (row != rollOverRowIndex) {
          rollOverRowIndex = row;
          e.getComponent().repaint();
        }
      }
    };
    addMouseMotionListener(listener);
  }

  private class RollOverTreeCellRenderer implements TreeCellRenderer {
    private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      Component c = renderer.getTreeCellRendererComponent(
          tree, value, selected, expanded, leaf, row, hasFocus);
      boolean isRollOver = row == rollOverRowIndex;
      if (isRollOver) {
        c.setBackground(rolloverRowColor);
        if (selected) {
          c.setForeground(renderer.getTextNonSelectionColor());
        }
      }
      if (c instanceof JComponent) {
        ((JComponent) c).setOpaque(isRollOver);
      }
      return c;
    }
  }
}
