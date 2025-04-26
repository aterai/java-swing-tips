// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    add(new JScrollPane(new JTree()));
    add(new JScrollPane(new RowSelectionTree()));
    setPreferredSize(new Dimension(320, 240));
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

class RowSelectionTree extends JTree {
  private static final Color SELECTED_COLOR = new Color(0x64_96_C8);
  // private Handler handler;

  @Override protected void paintComponent(Graphics g) {
    int[] sr = getSelectionRows();
    if (sr == null) {
      super.paintComponent(g);
      return;
    }
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(SELECTED_COLOR);
    // for (int i : sr) {
    //   Rectangle r = getRowBounds(i);
    //   g2.fillRect(0, r.y, getWidth(), r.height);
    // }
    Arrays.stream(sr).mapToObj(this::getRowBounds)
        .forEach(r -> g2.fillRect(0, r.y, getWidth(), r.height));
    super.paintComponent(g);
    if (hasFocus()) {
      Optional.ofNullable(getLeadSelectionPath()).ifPresent(path -> {
        Rectangle r = getRowBounds(getRowForPath(path));
        g2.setPaint(SELECTED_COLOR.darker());
        g2.drawRect(0, r.y, getWidth() - 1, r.height - 1);
      });
      // TreePath path = getLeadSelectionPath();
      // if (Objects.nonNull(path)) {
      //   Rectangle r = getRowBounds(getRowForPath(path));
      //   g2.setPaint(SELECTED_COLOR.darker());
      //   g2.drawRect(0, r.y, getWidth() - 1, r.height - 1);
      // }
    }
    g2.dispose();
  }

  @Override public void updateUI() {
    // removeFocusListener(handler);
    setCellRenderer(null);
    super.updateUI();
    setUI(new BasicTreeUI() {
      @Override public Rectangle getPathBounds(JTree tree, TreePath path) {
        Rectangle r = null;
        if (Objects.nonNull(tree) && Objects.nonNull(treeState)) {
          r = getTreePathBounds(path, tree.getInsets(), new Rectangle());
        }
        return r;
      }

      private Rectangle getTreePathBounds(TreePath path, Insets insets, Rectangle bounds) {
        Rectangle rect = treeState.getBounds(path, bounds);
        if (Objects.nonNull(rect)) {
          rect.width = tree.getWidth();
          rect.y += insets.top;
        }
        return rect;
      }
    });
    UIManager.put("Tree.repaintWholeRow", Boolean.TRUE);
    // handler = new Handler();
    // addFocusListener(handler);
    TreeCellRenderer r = getCellRenderer();
    setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
      Component c = r.getTreeCellRendererComponent(
          tree, value, selected, expanded, leaf, row, hasFocus);
      c.setBackground(selected ? SELECTED_COLOR : tree.getBackground());
      if (c instanceof JComponent) {
        ((JComponent) c).setOpaque(true);
      }
      return c;
    });
    setOpaque(false);
  }
}

// class Handler extends DefaultTreeCellRenderer { // implements FocusListener {
//   @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//     Component c = super.getTreeCellRendererComponent(
//         tree, value, selected, expanded, leaf, row, hasFocus);
//     c.setBackground(selected ? SELECTED_COLOR : tree.getBackground());
//     if (c instanceof JComponent) {
//       ((JComponent) c).setOpaque(true);
//     }
//     return c;
//   }
//
//   // @Override public void focusGained(FocusEvent e) {
//   //   e.getComponent().repaint();
//   // }
//
//   // @Override public void focusLost(FocusEvent e) {
//   //   e.getComponent().repaint();
//   //   // TEST:
//   //   // if (Objects.nonNull(tree.getLeadSelectionPath())) {
//   //   //   Rectangle r = tree.getRowBounds(tree.getRowForPath(tree.getLeadSelectionPath()));
//   //   //   r.width += r.x;
//   //   //   r.x = 0;
//   //   //   tree.repaint(r);
//   //   // }
//   // }
// }
