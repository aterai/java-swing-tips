// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new JTree();
    tree.setBorder(BorderFactory.createTitledBorder("Ctrl+T: toggle TreeNode"));

    InputMap im = tree.getInputMap(WHEN_FOCUSED);
    int modifiers1 = InputEvent.CTRL_DOWN_MASK;
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, modifiers1), "toggle");
    int modifiers2 = InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, modifiers2), "toggle2");
    ActionMap am = tree.getActionMap();
    am.put("toggle2", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        // System.out.println("toggle2");
        int row = tree.getLeadSelectionRow();
        TreePath path = tree.getPathForRow(row);
        // if (isLeaf(tree, path)) {
        //   return;
        // }
        if (tree.isExpanded(path)) {
          tree.collapsePath(path);
        } else {
          tree.expandPath(path);
        }
      }
    });
    // tree.setUI(new MetalTreeUI() {
    //   @Override protected boolean isToggleEvent(MouseEvent e) {
    //     // https://ateraimemo.com/Swing/PreventToggleClickNodeExpanding.html
    //     System.out.println(e);
    //     return super.isToggleEvent(e);
    //   }
    // });
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  // private static boolean isLeaf(JTree tree, TreePath path) {
  //   // TreePath path = tree.getPathForRow(row);
  //   TreeModel m = tree.getModel();
  //   return path != null && m != null && m.isLeaf(path.getLastPathComponent());
  // }

  // BasicTreeUI.TreeToggleAction
  // private void toggle(JTree tree, BasicTreeUI ui) {
  //   int selRow = ui.getLeadSelectionRow();
  //   if (selRow != -1 && !ui.isLeaf(selRow)) {
  //     TreePath aPath = ui.getAnchorSelectionPath();
  //     TreePath lPath = ui.getLeadSelectionPath();
  //     ui.toggleExpandState(ui.getPathForRow(tree, selRow));
  //     ui.setAnchorSelectionPath(aPath);
  //     ui.setLeadSelectionPath(lPath);
  //   }
  // }

  // BasicTreeUI
  // protected void toggleExpandState(TreePath path) {
  //   if(!tree.isExpanded(path)) {
  //     int row = getRowForPath(tree, path);
  //     tree.expandPath(path);
  //     updateSize();
  //     if(row != -1) {
  //       if(tree.getScrollsOnExpand()) {
  //         ensureRowsAreVisible(row, row + treeState.getVisibleChildCount(path));
  //       } else {
  //         ensureRowsAreVisible(row, row);
  //       }
  //     }
  //   } else {
  //     tree.collapsePath(path);
  //     updateSize();
  //   }
  // }

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
