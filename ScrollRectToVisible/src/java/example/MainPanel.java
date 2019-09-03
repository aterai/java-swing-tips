// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private final String[] columnNames = {"String", "Integer", "Boolean"};
  private final Object[][] data = {
    {"aaa", 12, true}, {"bbb", 5, false},
    {"CCC", 92, true}, {"DDD", 0, false}
  };
  private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
    @Override public Class<?> getColumnClass(int column) {
      return getValueAt(0, column).getClass();
    }
  };
  private final JTable table = new JTable(model);
  private final DefaultListModel<LocalDateTime> listModel = new DefaultListModel<>();
  private final JList<LocalDateTime> list = new JList<>(listModel);
  private final JTree tree = new JTree();
  private final Timer timer;
  private transient HierarchyListener hierarchyListener;

  public MainPanel() {
    super(new BorderLayout());
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    // table.setComponentPopupMenu(new TablePopupMenu());

    JTabbedPane t = new JTabbedPane();
    t.addTab("JTable", new JScrollPane(table));
    t.addTab("JList", new JScrollPane(list));
    t.addTab("JTree", new JScrollPane(tree));

    timer = new Timer(1000, e -> {
      LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());

      // JTable
      model.addRow(new Object[] {date.toString(), model.getRowCount(), false});
      int i = table.convertRowIndexToView(model.getRowCount() - 1);
      Rectangle r = table.getCellRect(i, 0, true);
      table.scrollRectToVisible(r);

      // JList
      listModel.addElement(date);
      int index = listModel.getSize() - 1;
      list.ensureIndexIsVisible(index);
      // Rectangle cellBounds = list.getCellBounds(index, index);
      // if (Objects.nonNull(cellBounds)) {
      //   list.scrollRectToVisible(cellBounds);
      // }

      // JTree
      DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) treeModel.getRoot();
      DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(date);
      treeModel.insertNodeInto(newChild, parent, parent.getChildCount());
      // tree.scrollRowToVisible(row) == tree.scrollPathToVisible(tree.getPathForRow(row))
      // tree.scrollRowToVisible(tree.getRowCount() - 1);
      tree.scrollPathToVisible(new TreePath(newChild.getPath()));
    });
    timer.start();
    add(t);
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void updateUI() {
    removeHierarchyListener(hierarchyListener);
    super.updateUI();
    hierarchyListener = e -> {
      boolean isDisplayableChanged = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (isDisplayableChanged && Objects.nonNull(timer) && !e.getComponent().isDisplayable()) {
        System.out.println("case DISPOSE_ON_CLOSE: hierarchyChanged");
        timer.stop();
      }
    };
    addHierarchyListener(hierarchyListener);
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
