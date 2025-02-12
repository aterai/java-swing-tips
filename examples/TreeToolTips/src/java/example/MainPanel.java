// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTree tree1 = new JTree() {
      @Override public String getToolTipText(MouseEvent e) {
        return Optional.ofNullable(getPathForLocation(e.getX(), e.getY()))
            .map(TreePath::getLastPathComponent)
            .map(txt -> "getToolTipText: " + txt)
            .orElse(null);
      }
    };
    ToolTipManager.sharedInstance().registerComponent(tree1);

    JTree tree2 = new JTree() {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        TreeCellRenderer renderer = getCellRenderer();
        setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
          Component c = renderer.getTreeCellRendererComponent(
              tree, value, selected, expanded, leaf, row, hasFocus);
          if (c instanceof JComponent) {
            String txt = Optional.ofNullable(value)
                .map(str -> "TreeCellRenderer: " + str).orElse(null);
            ((JComponent) c).setToolTipText(txt);
          }
          return c;
        });
      }
    };
    //   setLeafIcon(DefaultLookup.getIcon(this, ui, "Tree.leafIcon"));
    //   setClosedIcon(DefaultLookup.getIcon(this, ui, "Tree.closedIcon"));
    //   setOpenIcon(DefaultLookup.getIcon(this, ui, "Tree.openIcon"));
    //   setTextSelectionColor(
    //       DefaultLookup.getColor(this, ui, "Tree.selectionForeground"));
    //   setTextNonSelectionColor(
    //       DefaultLookup.getColor(this, ui, "Tree.textForeground"));
    //   setBackgroundSelectionColor(
    //       DefaultLookup.getColor(this, ui, "Tree.selectionBackground"));
    //   setBackgroundNonSelectionColor(
    //       DefaultLookup.getColor(this, ui, "Tree.textBackground"));
    //   setBorderSelectionColor(
    //       DefaultLookup.getColor(this, ui, "Tree.selectionBorderColor"));
    //   ... = DefaultLookup.getBoolean(this, ui, "Tree.drawsFocusBorderAroundIcon", false);
    //   ... = DefaultLookup.getBoolean(this, ui, "Tree.drawDashedFocusIndicator", false);
    //   ... = DefaultLookup.getBoolean(this, ui, "Tree.rendererFillBackground", true);
    //   Insets i = DefaultLookup.getInsets(this, ui, "Tree.rendererMargins");
    //   if (i != null) {
    //     setBorder(BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right));
    //   }
    // };
    // tree2.setToolTipText(" ");
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
