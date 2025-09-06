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
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private final Timer timer;
  private transient HierarchyListener handler;

  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false},
    };
    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    // table.setComponentPopupMenu(new TablePopupMenu());

    DefaultListModel<LocalDateTime> listModel = new DefaultListModel<>();
    JList<LocalDateTime> list = new JList<>(listModel);
    JTree tree = new JTree();

    JTabbedPane t = new JTabbedPane();
    t.addTab("JTable", new JScrollPane(table));
    t.addTab("JList", new JScrollPane(list));
    t.addTab("JTree", new JScrollPane(tree));

    timer = new Timer(1000, e -> {
      LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
      tableScroll(table, model, date);
      listScroll(list, listModel, date);
      treeScroll(tree, date);
    });
    timer.start();
    add(t);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void tableScroll(JTable table, DefaultTableModel model, Object data) {
    model.addRow(new Object[] {data.toString(), model.getRowCount(), false});
    int i = table.convertRowIndexToView(model.getRowCount() - 1);
    Rectangle r = table.getCellRect(i, 0, true);
    table.scrollRectToVisible(r);
  }

  private static <E> void listScroll(JList<E> list, DefaultListModel<E> model, E item) {
    model.addElement(item);
    int index = model.getSize() - 1;
    list.ensureIndexIsVisible(index);
    // Rectangle cellBounds = list.getCellBounds(index, index);
    // if (Objects.nonNull(cellBounds)) {
    //   list.scrollRectToVisible(cellBounds);
    // }
  }

  private static void treeScroll(JTree tree, Object data) {
    DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) treeModel.getRoot();
    DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(data);
    treeModel.insertNodeInto(newChild, parent, parent.getChildCount());
    // tree.scrollRowToVisible(row) == tree.scrollPathToVisible(tree.getPathForRow(row))
    // tree.scrollRowToVisible(tree.getRowCount() - 1);
    tree.scrollPathToVisible(new TreePath(newChild.getPath()));
  }

  @Override public void updateUI() {
    removeHierarchyListener(handler);
    super.updateUI();
    handler = e -> {
      boolean displayability = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (displayability && Objects.nonNull(timer) && !e.getComponent().isDisplayable()) {
        timer.stop();
      }
    };
    addHierarchyListener(handler);
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
