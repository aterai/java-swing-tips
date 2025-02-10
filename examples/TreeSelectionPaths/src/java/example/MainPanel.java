// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2, 5, 5));
    JTree tree = new JTree();
    tree.addTreeWillExpandListener(new TreeWillExpandListener() {
      @Override public void treeWillExpand(TreeExpansionEvent e) {
        JTree t = (JTree) e.getSource();
        TreePath anchor = t.getAnchorSelectionPath();
        TreePath lead = t.getLeadSelectionPath();
        TreePath path = e.getPath();
        Object o = path.getLastPathComponent();
        if (o instanceof DefaultMutableTreeNode && t.isPathSelected(path)) {
          DefaultMutableTreeNode n = (DefaultMutableTreeNode) o;
          TreePath[] paths = Collections.list((Enumeration<?>) n.children())
              .stream()
              .filter(DefaultMutableTreeNode.class::isInstance)
              .map(DefaultMutableTreeNode.class::cast)
              .map(DefaultMutableTreeNode::getPath)
              .map(TreePath::new)
              .toArray(TreePath[]::new);
          t.addSelectionPaths(paths);
          t.setAnchorSelectionPath(anchor);
          t.setLeadSelectionPath(lead);
        }
      }

      @Override public void treeWillCollapse(TreeExpansionEvent e) {
        /* do nothing */
      }
    });

    add(new JScrollPane(new JTree()));
    add(new JScrollPane(tree));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
