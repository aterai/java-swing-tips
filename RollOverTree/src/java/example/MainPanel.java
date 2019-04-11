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
    add(new JScrollPane(makeTree(makeModel())));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTree makeTree(TreeModel model) {
    return new JTree(model) {
      private final Color rolloverRowColor = new Color(0xDC_F0_FF);
      private int rollOverRowIndex = -1;
      private transient MouseMotionListener listener;
      @Override public void updateUI() {
        removeMouseMotionListener(listener);
        super.updateUI();
        setCellRenderer(new DefaultTreeCellRenderer() {
          @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            if (c instanceof JComponent) {
              JComponent jc = (JComponent) c;
              if (row == rollOverRowIndex) {
                jc.setOpaque(true);
                jc.setBackground(rolloverRowColor);
                if (selected) {
                  jc.setForeground(getTextNonSelectionColor());
                }
              } else {
                jc.setOpaque(false);
              }
            }
            return c;
          }
        });
        listener = new MouseAdapter() {
          @Override public void mouseMoved(MouseEvent e) {
            int row = getRowForLocation(e.getX(), e.getY());
            if (row != rollOverRowIndex) {
              rollOverRowIndex = row;
              repaint();
            }
          }
        };
        addMouseMotionListener(listener);
      }
    };
  }

  private static DefaultTreeModel makeModel() {
    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
    set1.add(new DefaultMutableTreeNode("111111111"));
    set1.add(new DefaultMutableTreeNode("22222222222"));
    set1.add(new DefaultMutableTreeNode("33333"));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
    set2.add(new DefaultMutableTreeNode("asdfasdfas"));
    set2.add(new DefaultMutableTreeNode("asdf"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
    set3.add(new DefaultMutableTreeNode("asdfasdfasdf"));
    set3.add(new DefaultMutableTreeNode("qwerqwer"));
    set3.add(new DefaultMutableTreeNode("zvxcvzxcvzxzxcvzxcv"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    root.add(set1);
    root.add(set2);
    set2.add(set3);
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
