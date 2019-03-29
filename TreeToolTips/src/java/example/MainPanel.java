// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTree tree1 = new JTree() {
      @Override public String getToolTipText(MouseEvent e) {
        Object o = null;
        TreePath path = getPathForLocation(e.getX(), e.getY());
        if (Objects.nonNull(path)) {
          o = path.getLastPathComponent();
        }
        return Objects.toString(o, "getToolTipText");
      }
    };
    ToolTipManager.sharedInstance().registerComponent(tree1);

    JTree tree2 = new JTree();
    tree2.setCellRenderer(new DefaultTreeCellRenderer() {
      // private void init() {
      //   setLeafIcon(DefaultLookup.getIcon(this, ui, "Tree.leafIcon"));
      //   setClosedIcon(DefaultLookup.getIcon(this, ui, "Tree.closedIcon"));
      //   setOpenIcon(DefaultLookup.getIcon(this, ui, "Tree.openIcon"));
      //   setTextSelectionColor(DefaultLookup.getColor(this, ui, "Tree.selectionForeground"));
      //   setTextNonSelectionColor(DefaultLookup.getColor(this, ui, "Tree.textForeground"));
      //   setBackgroundSelectionColor(DefaultLookup.getColor(this, ui, "Tree.selectionBackground"));
      //   setBackgroundNonSelectionColor(DefaultLookup.getColor(this, ui, "Tree.textBackground"));
      //   setBorderSelectionColor(DefaultLookup.getColor(this, ui, "Tree.selectionBorderColor"));
      //   // drawsFocusBorderAroundIcon = DefaultLookup.getBoolean(this, ui, "Tree.drawsFocusBorderAroundIcon", false);
      //   // drawDashedFocusIndicator = DefaultLookup.getBoolean(this, ui, "Tree.drawDashedFocusIndicator", false);
      //   // fillBackground = DefaultLookup.getBoolean(this, ui, "Tree.rendererFillBackground", true);
      //   Insets margins = DefaultLookup.getInsets(this, ui, "Tree.rendererMargins");
      //   if (margins != null) {
      //     setBorder(BorderFactory.createEmptyBorder(margins.top, margins.left, margins.bottom, margins.right));
      //   }
      // }
      // @Override public void updateUI() {
      //   super.updateUI();
      //   init();
      // }

      @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (c instanceof JComponent) {
          ((JComponent) c).setToolTipText(Objects.nonNull(value) ? "TreeCellRenderer: " + value.toString() : null);
        }
        return c;
      }
    });
    // tree2.setToolTipText("dummy");
    ToolTipManager.sharedInstance().registerComponent(tree2);

    add(makeTitledPanel("Override getToolTipText", new JScrollPane(tree1)));
    add(makeTitledPanel("Use TreeCellRenderer", new JScrollPane(tree2)));
    setPreferredSize(new Dimension(320, 240));
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
