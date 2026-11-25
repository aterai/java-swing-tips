// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
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

class RowSelectionTree extends JTree {
  private static final Color SELECTED_COLOR = new Color(0x64_96_C8);

  @Override public void updateUI() {
    setCellRenderer(null);
    super.updateUI();
    setUI(new RowSelectionTreeUI());
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

  @Override protected void paintComponent(Graphics g) {
    // The tree is not opaque, so the background must always be filled here,
    // even when there is no selection.
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    int[] selectionRows = getSelectionRows();
    if (selectionRows == null) {
      super.paintComponent(g);
    } else {
      Graphics2D g2 = (Graphics2D) g.create();
      paintRows(g2, selectionRows);
      super.paintComponent(g);
      if (hasFocus()) {
        paintFocusRow(g2);
      }
      g2.dispose();
    }
  }

  private void paintRows(Graphics2D g2, int... selectionRows) {
    g2.setPaint(SELECTED_COLOR);
    Arrays.stream(selectionRows)
        .mapToObj(this::getRowBounds)
        .filter(Objects::nonNull)
        .forEach(r -> g2.fillRect(0, r.y, getWidth(), r.height));
  }

  private void paintFocusRow(Graphics2D g2) {
    Optional.ofNullable(getLeadSelectionPath()).map(this::getPathBounds).ifPresent(r -> {
      g2.setPaint(SELECTED_COLOR.darker());
      g2.drawRect(0, r.y, getWidth() - 1, r.height - 1);
    });
  }
}

class RowSelectionTreeUI extends BasicTreeUI {
  // Extend the path bounds to the right edge of the tree so that the node
  // can be selected by clicking anywhere to the right of the node.
  @Override public Rectangle getPathBounds(JTree tree, TreePath path) {
    Rectangle r = null;
    if (Objects.nonNull(tree) && Objects.nonNull(treeState)) {
      r = treeState.getBounds(path, new Rectangle());
      if (Objects.nonNull(r)) {
        r.width = tree.getWidth();
        r.y += tree.getInsets().top;
      }
    }
    return r;
  }

  // The BasicTreeUI repaints only the area from the node x position when the
  // focus or the lead selection changes, which would leave the left part of
  // the lead row border. Repaint the whole row instead of using the global
  // UIManager.put("Tree.repaintWholeRow", true) setting.
  @Override protected FocusListener createFocusListener() {
    FocusListener listener = super.createFocusListener();
    return new FocusListener() {
      @Override public void focusGained(FocusEvent e) {
        listener.focusGained(e);
        repaintWholeRow(tree.getLeadSelectionPath());
      }

      @Override public void focusLost(FocusEvent e) {
        listener.focusLost(e);
        repaintWholeRow(tree.getLeadSelectionPath());
      }
    };
  }

  @Override protected PropertyChangeListener createPropertyChangeListener() {
    PropertyChangeListener listener = super.createPropertyChangeListener();
    return e -> {
      listener.propertyChange(e);
      if (JTree.LEAD_SELECTION_PATH_PROPERTY.equals(e.getPropertyName())) {
        repaintWholeRow((TreePath) e.getOldValue());
        repaintWholeRow((TreePath) e.getNewValue());
      }
    };
  }

  private void repaintWholeRow(TreePath path) {
    Optional.ofNullable(path)
        .map(p -> getPathBounds(tree, p))
        .ifPresent(r -> tree.repaint(0, r.y, tree.getWidth(), r.height));
  }
}
