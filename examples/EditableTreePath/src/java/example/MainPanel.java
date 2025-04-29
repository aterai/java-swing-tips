// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private static final JTextArea LOG = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    LOG.setEditable(false);
    JPopupMenu popup = new JPopupMenu();
    popup.add("clear").addActionListener(e -> LOG.setText(""));
    LOG.setComponentPopupMenu(popup);

    JTree tree = new JTree() {
      @Override public void updateUI() {
        super.updateUI();
        setEditable(true);
        setCellEditor(makeTreeCellEditor(this));
      }

      @Override public boolean isPathEditable(TreePath path) {
        appendLog("JTree#isPathEditable(TreePath)");
        appendLog(String.format("  getPathCount(): %d", path.getPathCount()));
        return Optional.ofNullable(path.getLastPathComponent())
            .filter(TreeNode.class::isInstance)
            .map(TreeNode.class::cast)
            .map(MainPanel::isLeaf)
            .orElse(false);
      }
    };

    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setResizeWeight(.5);
    sp.setTopComponent(new JScrollPane(tree));
    sp.setBottomComponent(new JScrollPane(LOG));
    add(sp);
    setPreferredSize(new Dimension(320, 240));
  }

  private static boolean isLeaf(TreeNode node) {
    boolean isLeaf = node.isLeaf();
    appendLog(String.format("  isLeaf: %s", isLeaf));
    if (node instanceof DefaultMutableTreeNode) {
      int lv = ((DefaultMutableTreeNode) node).getLevel();
      appendLog(String.format("  getLevel: %d", lv));
    }
    return isLeaf;
  }

  private static TreeCellEditor makeTreeCellEditor(JTree tree) {
    return new DefaultTreeCellEditor(tree, (DefaultTreeCellRenderer) tree.getCellRenderer()) {
      @Override public boolean isCellEditable(EventObject e) {
        appendLog("TreeCellEditor#isCellEditable(EventObject)");
        boolean ret;
        if (e instanceof MouseEvent) {
          MouseEvent me = (MouseEvent) e;
          info(me);
          ret = me.getClickCount() >= 2 || me.isShiftDown() || me.isControlDown();
        } else if (e instanceof KeyEvent) {
          appendLog("  KeyEvent");
          ret = super.isCellEditable(e);
        } else { // e == null
          appendLog("  startEditing Action(F2)");
          ret = super.isCellEditable(e);
        }
        return ret;
      }
    };
  }

  private static void info(MouseEvent e) {
    appendLog("  MouseEvent");
    appendLog(String.format("  getPoint(): %s", e.getPoint()));
    appendLog(String.format("  getClickCount: %d", e.getClickCount()));
    appendLog(String.format("  isShiftDown: %s", e.isShiftDown()));
    appendLog(String.format("  isControlDown: %s", e.isControlDown()));
  }

  private static void appendLog(String str) {
    LOG.append(str + "\n");
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
