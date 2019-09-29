// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add("JList", makeListPanel());
    tabbedPane.add("JTable", makeTablePanel());
    tabbedPane.add("JTree", makeTreePanel());

    // Default drop line color: UIManager.put("List.dropLineColor", null);
    // Hide drop lines: UIManager.put("List.dropLineColor", new Color(0x0, true));

    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JButton makeColorChooserButton(String key) {
    JButton button = new JButton(key);
    button.addActionListener(e -> {
      Color c = JColorChooser.showDialog(button.getRootPane(), key, UIManager.getColor(key));
      UIManager.put(key, c);
    });
    return button;
  }

  private static Component makeListPanel() {
    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement("1111");
    model.addElement("22222222");
    model.addElement("333333333333");
    model.addElement("****");

    JList<String> list = new JList<>(model);
    list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    list.setTransferHandler(new ListItemTransferHandler());
    list.setDropMode(DropMode.INSERT);
    list.setDragEnabled(true);

    // Disable row Cut, Copy, Paste
    ActionMap map = list.getActionMap();
    Action dummy = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        /* Dummy action */
      }
    };
    map.put(TransferHandler.getCutAction().getValue(Action.NAME), dummy);
    map.put(TransferHandler.getCopyAction().getValue(Action.NAME), dummy);
    map.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(makeColorChooserButton("List.dropLineColor"));

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(list));
    p.add(box, BorderLayout.SOUTH);
    return p;
  }

  private static Component makeTablePanel() {
    TransferHandler handler = new TableRowTransferHandler();
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
      {"AAA", 12, true}, {"aaa", 1, false},
      {"BBB", 13, true}, {"bbb", 2, false},
      {"CCC", 15, true}, {"ccc", 3, false},
      {"DDD", 17, true}, {"ddd", 4, false},
      {"EEE", 18, true}, {"eee", 5, false},
      {"FFF", 19, true}, {"fff", 6, false},
      {"GGG", 92, true}, {"ggg", 0, false}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        switch (column) {
          case 0: return String.class;
          case 1: return Number.class;
          case 2: return Boolean.class;
          default: return super.getColumnClass(column);
        }
      }
    };
    JTable table = new JTable(model);
    table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    table.setTransferHandler(handler);
    table.setDropMode(DropMode.INSERT_ROWS);
    table.setDragEnabled(true);
    table.setFillsViewportHeight(true);

    // Disable row Cut, Copy, Paste
    ActionMap map = table.getActionMap();
    Action dummy = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        /* Dummy action */
      }
    };
    map.put(TransferHandler.getCutAction().getValue(Action.NAME), dummy);
    map.put(TransferHandler.getCopyAction().getValue(Action.NAME), dummy);
    map.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(makeColorChooserButton("Table.dropLineColor"));
    box.add(makeColorChooserButton("Table.dropLineShortColor"));


    UIManager.put("Table.dropLineShortColor", new Color(0x0, true));


    
    JPanel p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(table));
    p.add(box, BorderLayout.SOUTH);
    return p;
  }

  private static JTree makeTree(TransferHandler handler) {
    JTree tree = new JTree();
    tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    tree.setRootVisible(false);
    tree.setDragEnabled(true);
    tree.setTransferHandler(handler);
    tree.setDropMode(DropMode.INSERT);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    // Disable node Cut action
    tree.getActionMap().put(TransferHandler.getCutAction().getValue(Action.NAME), new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        /* Dummy action */
      }
    });

    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
    }
    return tree;
  }

  private static Component makeTreePanel() {
    TreeTransferHandler handler = new TreeTransferHandler();
    JPanel p = new JPanel(new GridLayout(1, 2));
    p.add(new JScrollPane(makeTree(handler)));
    p.add(new JScrollPane(makeTree(handler)));

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(makeColorChooserButton("Tree.dropLineColor"));

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(p);
    panel.add(box, BorderLayout.SOUTH);
    return panel;
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

class ListItemTransferHandler extends TransferHandler {
  protected static final DataFlavor FLAVOR = new DataFlavor(List.class, "List of items");
  private int[] indices;
  private int addIndex = -1; // Location where items were added
  private int addCount; // Number of items added.

  @Override protected Transferable createTransferable(JComponent c) {
    JList<?> source = (JList<?>) c;
    indices = source.getSelectedIndices();
    List<?> transferredObjects = source.getSelectedValuesList();
    return new Transferable() {
      @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {FLAVOR};
      }

      @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Objects.equals(FLAVOR, flavor);
      }

      @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
          return transferredObjects;
        } else {
          throw new UnsupportedFlavorException(flavor);
        }
      }
    };
  }

  @Override public boolean canImport(TransferHandler.TransferSupport info) {
    return info.isDrop() && info.isDataFlavorSupported(FLAVOR);
  }

  @Override public int getSourceActions(JComponent c) {
    return TransferHandler.MOVE;
  }

  @SuppressWarnings("unchecked")
  @Override public boolean importData(TransferHandler.TransferSupport info) {
    TransferHandler.DropLocation tdl = info.getDropLocation();
    if (!canImport(info) || !(tdl instanceof JList.DropLocation)) {
      return false;
    }
    JList.DropLocation dl = (JList.DropLocation) tdl;
    JList<?> target = (JList<?>) info.getComponent();
    DefaultListModel<Object> listModel = (DefaultListModel<Object>) target.getModel();
    int max = listModel.getSize();
    int index = dl.getIndex();
    index = index >= 0 && index < max ? index : max;
    addIndex = index;
    try {
      List<?> values = (List<?>) info.getTransferable().getTransferData(FLAVOR);
      for (Object o: values) {
        int i = index++;
        listModel.add(i, o);
        target.addSelectionInterval(i, i);
      }
      addCount = values.size();
      return true;
    } catch (UnsupportedFlavorException | IOException ex) {
      return false;
    }
  }

  @Override protected void exportDone(JComponent c, Transferable data, int action) {
    cleanup(c, action == TransferHandler.MOVE);
  }

  private void cleanup(JComponent c, boolean remove) {
    if (remove && Objects.nonNull(indices)) {
      if (addCount > 0) {
        for (int i = 0; i < indices.length; i++) {
          if (indices[i] >= addIndex) {
            indices[i] += addCount;
          }
        }
      }
      JList<?> source = (JList<?>) c;
      DefaultListModel<?> model = (DefaultListModel<?>) source.getModel();
      for (int i = indices.length - 1; i >= 0; i--) {
        model.remove(indices[i]);
      }
    }
    indices = null;
    addCount = 0;
    addIndex = -1;
  }
}

class TableRowTransferHandler extends TransferHandler {
  protected static final DataFlavor FLAVOR = new DataFlavor(List.class, "List of items");
  private int[] indices;
  private int addIndex = -1; // Location where items were added
  private int addCount; // Number of items added.

  @Override protected Transferable createTransferable(JComponent c) {
    c.getRootPane().getGlassPane().setVisible(true);
    JTable table = (JTable) c;
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    indices = table.getSelectedRows();
    @SuppressWarnings("JdkObsolete")
    List<?> transferredObjects = Arrays.stream(indices)
        .mapToObj(model.getDataVector()::get).collect(Collectors.toList());
    return new Transferable() {
      @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {FLAVOR};
      }

      @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Objects.equals(FLAVOR, flavor);
      }

      @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
          return transferredObjects;
        } else {
          throw new UnsupportedFlavorException(flavor);
        }
      }
    };
  }

  @Override public boolean canImport(TransferHandler.TransferSupport info) {
    boolean isDroppable = info.isDrop() && info.isDataFlavorSupported(FLAVOR);
    Component glassPane = ((JComponent) info.getComponent()).getRootPane().getGlassPane();
    glassPane.setCursor(isDroppable ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
    return isDroppable;
  }

  @Override public int getSourceActions(JComponent c) {
    return TransferHandler.MOVE; // TransferHandler.COPY_OR_MOVE;
  }

  @Override public boolean importData(TransferHandler.TransferSupport info) {
    if (!canImport(info)) {
      return false;
    }
    TransferHandler.DropLocation tdl = info.getDropLocation();
    if (!(tdl instanceof JTable.DropLocation)) {
      return false;
    }
    JTable.DropLocation dl = (JTable.DropLocation) tdl;
    JTable target = (JTable) info.getComponent();
    DefaultTableModel model = (DefaultTableModel) target.getModel();
    int max = model.getRowCount();
    int index = dl.getRow();
    index = index >= 0 && index < max ? index : max;
    addIndex = index;
    try {
      List<?> values = (List<?>) info.getTransferable().getTransferData(FLAVOR);
      addCount = values.size();
      Object[] array = new Object[0];
      for (Object o: values) {
        int row = index++;
        // model.insertRow(row, (Vector<?>) o);
        model.insertRow(row, ((List<?>) o).toArray(array));
        target.getSelectionModel().addSelectionInterval(row, row);
      }
      return true;
    } catch (UnsupportedFlavorException | IOException ex) {
      return false;
    }
  }

  @Override protected void exportDone(JComponent c, Transferable data, int action) {
    cleanup(c, action == TransferHandler.MOVE);
  }

  private void cleanup(JComponent c, boolean remove) {
    c.getRootPane().getGlassPane().setVisible(false);
    // c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    if (remove && Objects.nonNull(indices)) {
      DefaultTableModel model = (DefaultTableModel) ((JTable) c).getModel();
      if (addCount > 0) {
        for (int i = 0; i < indices.length; i++) {
          if (indices[i] >= addIndex) {
            indices[i] += addCount;
          }
        }
      }
      for (int i = indices.length - 1; i >= 0; i--) {
        model.removeRow(indices[i]);
      }
    }
    indices = null;
    addCount = 0;
    addIndex = -1;
  }
}

class TreeTransferHandler extends TransferHandler {
  private static final String NAME = "Array of DefaultMutableTreeNode";
  protected static final DataFlavor FLAVOR = new DataFlavor(DefaultMutableTreeNode[].class, NAME);
  private JTree source;

  @Override protected Transferable createTransferable(JComponent c) {
    source = (JTree) c;
    TreePath[] paths = source.getSelectionPaths();
    assert paths != null;
    DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[paths.length];
    for (int i = 0; i < paths.length; i++) {
      nodes[i] = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
    }
    return new Transferable() {
      @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {FLAVOR};
      }

      @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Objects.equals(FLAVOR, flavor);
      }

      @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
          return nodes;
        } else {
          throw new UnsupportedFlavorException(flavor);
        }
      }
    };
  }

  @Override public int getSourceActions(JComponent c) {
    return TransferHandler.MOVE;
  }

  @Override public boolean canImport(TransferHandler.TransferSupport support) {
    if (!support.isDrop()) {
      return false;
    }
    if (!support.isDataFlavorSupported(FLAVOR)) {
      return false;
    }
    JTree tree = (JTree) support.getComponent();
    return !tree.equals(source);
  }

  @Override public boolean importData(TransferHandler.TransferSupport support) {
    DefaultMutableTreeNode[] nodes;
    try {
      Transferable t = support.getTransferable();
      nodes = (DefaultMutableTreeNode[]) t.getTransferData(FLAVOR);
    } catch (UnsupportedFlavorException | IOException ex) {
      return false;
    }
    TransferHandler.DropLocation tdl = support.getDropLocation();
    if (tdl instanceof JTree.DropLocation) {
      JTree.DropLocation dl = (JTree.DropLocation) tdl;
      int childIndex = dl.getChildIndex();
      TreePath dest = dl.getPath();
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();
      JTree tree = (JTree) support.getComponent();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      AtomicInteger idx = new AtomicInteger(childIndex < 0 ? parent.getChildCount() : childIndex);
      Stream.of(nodes).forEach(node -> {
        DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
        model.insertNodeInto(deepCopyTreeNode(node, clone), parent, idx.incrementAndGet());
      });
      return true;
    }
    return false;
  }

  private static DefaultMutableTreeNode deepCopyTreeNode(DefaultMutableTreeNode src, DefaultMutableTreeNode tgt) {
    // Java 9: Collections.list(src.children()).stream()
    Collections.list((Enumeration<?>) src.children()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .forEach(node -> {
          DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
          tgt.add(clone);
          if (!node.isLeaf()) {
            deepCopyTreeNode(node, clone);
          }
        });
    return tgt;
  }

  @Override protected void exportDone(JComponent src, Transferable data, int action) {
    if (action == TransferHandler.MOVE && src instanceof JTree) {
      JTree tree = (JTree) src;
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      TreePath[] selectionPaths = tree.getSelectionPaths();
      if (selectionPaths != null) {
        for (TreePath path : selectionPaths) {
          model.removeNodeFromParent((MutableTreeNode) path.getLastPathComponent());
        }
      }
    }
  }
}
