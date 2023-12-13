// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new JTree(makeModel()) {
      private final Color rolloverRowColor = new Color(0xDC_F0_FF);
      private int rollOverRowIndex = -1;
      private transient MouseMotionListener listener;
      @Override public void updateUI() {
        removeMouseMotionListener(listener);
        setCellRenderer(null);
        super.updateUI();
        DefaultTreeCellRenderer r = new DefaultTreeCellRenderer();
        setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
          Component c = r.getTreeCellRendererComponent(
              tree, value, selected, expanded, leaf, row, hasFocus);
          boolean isRollOver = row == rollOverRowIndex;
          if (isRollOver) {
            c.setBackground(rolloverRowColor);
            if (selected) {
              c.setForeground(r.getTextNonSelectionColor());
            }
          }
          if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(isRollOver);
          }
          return c;
        });
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
    };

    add(new JScrollPane(tree));
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
