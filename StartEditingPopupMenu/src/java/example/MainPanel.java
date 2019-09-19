// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new JTree();

    // UIManager.put("PopupMenu.consumeEventOnClose", Boolean.FALSE);
    // tree.addMouseListener(new MouseAdapter() {
    //   @Override public void mousePressed(MouseEvent e) {
    //     JTree tree = (JTree) e.getSource();
    //     TreePath path = tree.getPathForLocation(e.getX(), e.getY());
    //     if (!tree.getSelectionModel().isPathSelected(path)) {
    //       tree.getSelectionModel().setSelectionPath(path);
    //     }
    //   }
    // });

    tree.setCellEditor(new DefaultTreeCellEditor(tree, (DefaultTreeCellRenderer) tree.getCellRenderer()) {
      // @Override protected boolean shouldStartEditingTimer(EventObject e) {
      //   return false;
      // }
      // @Override protected boolean canEditImmediately(EventObject e) {
      //   // ((MouseEvent) e).getClickCount() - 2 >= 0
      //   return !(e instanceof MouseEvent) && super.canEditImmediately(e);
      // }

      @Override public boolean isCellEditable(EventObject e) {
        return !(e instanceof MouseEvent) && super.isCellEditable(e);
      }
    });
    tree.setEditable(true);
    tree.setComponentPopupMenu(new TreePopupMenu());

    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
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

class TreePopupMenu extends JPopupMenu {
  private TreePath path;
  private final JMenuItem editItem;
  private final JMenuItem editDialogItem;

  protected TreePopupMenu() {
    super();
    JTextField field = new JTextField();
    field.addAncestorListener(new FocusAncestorListener());

    editItem = add("Edit");
    editItem.addActionListener(e -> {
      if (Objects.nonNull(path)) {
        JTree tree = (JTree) getInvoker();
        tree.startEditingAtPath(path);
      }
    });

    editDialogItem = add("Edit Dialog");
    editDialogItem.addActionListener(e -> {
      if (Objects.isNull(path)) {
        return;
      }
      Object node = path.getLastPathComponent();
      if (node instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) node;
        field.setText(leaf.getUserObject().toString());
        JTree tree = (JTree) getInvoker();
        int ret = JOptionPane.showConfirmDialog(
            tree, field, "Rename", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        // if (ret == JOptionPane.OK_OPTION && !field.getText().trim().isEmpty()) {
        //   tree.getModel().valueForPathChanged(path, field.getText().trim());
        // }
        if (ret == JOptionPane.OK_OPTION) {
          tree.getModel().valueForPathChanged(path, field.getText());
        }
      }
    });
    add("dummy");
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTree) {
      JTree tree = (JTree) c;
      // // Test:
      // path = tree.getPathForLocation(x, y);
      // if (tree.getSelectionModel().isPathSelected(path)) {
      //   super.show(c, x, y);
      // }
      TreePath[] tsp = tree.getSelectionPaths();
      path = tree.getPathForLocation(x, y); // Test: path = tree.getClosestPathForLocation(x, y);
      boolean isEditable = tsp != null && tsp.length == 1 && tsp[0].equals(path);
      // Test: if (Objects.nonNull(path) && Arrays.asList(tsp).contains(path)) {
      editItem.setEnabled(isEditable);
      editDialogItem.setEnabled(isEditable);
      super.show(c, x, y);
    }
  }
}

class FocusAncestorListener implements AncestorListener {
  @Override public void ancestorAdded(AncestorEvent e) {
    e.getComponent().requestFocusInWindow();
  }

  @Override public void ancestorMoved(AncestorEvent e) {
    /* not needed */
  }

  @Override public void ancestorRemoved(AncestorEvent e) {
    /* not needed */
  }
}
