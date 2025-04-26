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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
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

  private static JPanel makeListPanel() {
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

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(makeColorChooserButton("List.dropLineColor"));

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(list));
    p.add(box, BorderLayout.SOUTH);
    return p;
  }

  private static DefaultTableModel makeModel() {
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
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  private static JPanel makeTablePanel() {
    TransferHandler handler = new TableRowTransferHandler();
    JTable table = new JTable(makeModel());
    table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    table.setTransferHandler(handler);
    table.setDropMode(DropMode.INSERT_ROWS);
    table.setDragEnabled(true);
    table.setFillsViewportHeight(true);
    // table.setAutoCreateRowSorter(true); // XXX

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(makeColorChooserButton("Table.dropLineColor"));
    box.add(makeColorChooserButton("Table.dropLineShortColor"));

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(table));
    p.add(box, BorderLayout.SOUTH);
    return p;
  }

  private static JPanel makeTreePanel() {
    JTree tree = new JTree();
    tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    tree.setDragEnabled(true);
    tree.setDropMode(DropMode.ON_OR_INSERT);
    tree.setTransferHandler(new TreeTransferHandler());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
    // Disable node Cut action
    Action empty = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        /* do nothing */
      }
    };
    tree.getActionMap().put(TransferHandler.getCutAction().getValue(Action.NAME), empty);

    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
    }

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(makeColorChooserButton("Tree.dropLineColor"));

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(tree));
    p.add(box, BorderLayout.SOUTH);
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

class ListItemTransferHandler extends TransferHandler {
  protected static final DataFlavor FLAVOR = new DataFlavor(List.class, "List of items");
  private final List<Integer> indices = new ArrayList<>();
  private int addIndex = -1; // Location where items were added
  private int addCount; // Number of items added.

  @Override protected Transferable createTransferable(JComponent c) {
    JList<?> source = (JList<?>) c;
    for (int i : source.getSelectedIndices()) {
      indices.add(i);
    }
    List<?> selectedValues = source.getSelectedValuesList();
    return new Transferable() {
      @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {FLAVOR};
      }

      @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Objects.equals(FLAVOR, flavor);
      }

      @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
          return selectedValues;
        } else {
          throw new UnsupportedFlavorException(flavor);
        }
      }
    };
  }

  @Override public boolean canImport(TransferSupport info) {
    return info.isDataFlavorSupported(FLAVOR);
  }

  @Override public int getSourceActions(JComponent c) {
    return COPY_OR_MOVE;
  }

  private static int getIndex(TransferSupport info) {
    JList<?> target = (JList<?>) info.getComponent();
    int index; // = dl.getIndex();
    if (info.isDrop()) { // Mouse Drag & Drop
      DropLocation tdl = info.getDropLocation();
      if (tdl instanceof JList.DropLocation) {
        index = ((JList.DropLocation) tdl).getIndex();
      } else {
        index = target.getSelectedIndex();
      }
    } else { // Keyboard Copy & Paste
      index = target.getSelectedIndex();
    }
    // boolean insert = dl.isInsert();
    int max = target.getModel().getSize();
    // int index = dl.getIndex();
    index = index < 0 ? max : index; // If it is out of range, it is appended to the end
    index = Math.min(index, max);
    return index;
  }

  private static List<?> getTransferData(TransferSupport info) {
    List<?> values;
    try {
      values = (List<?>) info.getTransferable().getTransferData(FLAVOR);
    } catch (UnsupportedFlavorException | IOException ex) {
      values = Collections.emptyList();
    }
    return values;
  }

  @SuppressWarnings("unchecked")
  @Override public boolean importData(TransferSupport info) {
    JList<?> target = (JList<?>) info.getComponent();
    DefaultListModel<Object> model = (DefaultListModel<Object>) target.getModel();
    int index = getIndex(info);
    addIndex = index;
    List<?> values = getTransferData(info);
    for (Object o : values) {
      int i = index++;
      model.add(i, o);
      target.addSelectionInterval(i, i);
    }
    addCount = info.isDrop() ? values.size() : 0;
    // target.requestFocusInWindow();
    return !values.isEmpty();
  }

  @Override public boolean importData(JComponent comp, Transferable t) {
    return importData(new TransferSupport(comp, t));
  }

  @Override protected void exportDone(JComponent c, Transferable data, int action) {
    cleanup(c, action == MOVE);
  }

  private void cleanup(JComponent c, boolean remove) {
    if (remove && !indices.isEmpty()) {
      if (addCount > 0) {
        for (int i = 0; i < indices.size(); i++) {
          if (indices.get(i) >= addIndex) {
            indices.set(i, indices.get(i) + addCount);
          }
        }
      }
      JList<?> src = (JList<?>) c;
      DefaultListModel<?> model = (DefaultListModel<?>) src.getModel();
      for (int i = indices.size() - 1; i >= 0; i--) {
        model.remove(indices.get(i));
      }
    }
    indices.clear();
    addCount = 0;
    addIndex = -1;
  }
}

class TableRowTransferHandler extends TransferHandler {
  protected static final DataFlavor FLAVOR = new DataFlavor(List.class, "List of items");
  private final List<Integer> indices = new ArrayList<>();
  private int addIndex = -1; // Location where items were added
  private int addCount; // Number of items added.

  @Override protected Transferable createTransferable(JComponent c) {
    c.getRootPane().getGlassPane().setVisible(true);
    JTable table = (JTable) c;
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    for (int i : table.getSelectedRows()) {
      indices.add(i);
    }
    @SuppressWarnings("JdkObsolete")
    List<?> transferredRows = indices.stream()
        .map(model.getDataVector()::get)
        .collect(Collectors.toList());
    return new Transferable() {
      @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {FLAVOR};
      }

      @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Objects.equals(FLAVOR, flavor);
      }

      @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
          return transferredRows;
        } else {
          throw new UnsupportedFlavorException(flavor);
        }
      }
    };
  }

  @Override public boolean canImport(TransferSupport info) {
    boolean canDrop = info.isDrop() && info.isDataFlavorSupported(FLAVOR);
    Component glassPane = ((JComponent) info.getComponent()).getRootPane().getGlassPane();
    glassPane.setCursor(canDrop ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
    return canDrop;
  }

  @Override public int getSourceActions(JComponent c) {
    return COPY_OR_MOVE;
  }

  @Override public boolean importData(TransferSupport info) {
    JTable target = (JTable) info.getComponent();
    DefaultTableModel model = (DefaultTableModel) target.getModel();
    int max = model.getRowCount();
    int index;
    if (info.isDrop()) {
      index = ((JTable.DropLocation) info.getDropLocation()).getRow();
    } else {
      index = target.getSelectedRow();
    }
    index = index >= 0 && index < max ? index : max;
    addIndex = index;
    // target.setCursor(Cursor.getDefaultCursor());
    List<?> values = getTransferData(info);
    Object[] type = new Object[0];
    for (Object o : values) {
      int row = index++;
      // model.insertRow(row, (Vector<?>) o);
      model.insertRow(row, ((List<?>) o).toArray(type));
      target.getSelectionModel().addSelectionInterval(row, row);
    }
    target.requestFocusInWindow();
    addCount = info.isDrop() ? values.size() : 0;
    return !values.isEmpty();
  }

  private static List<?> getTransferData(TransferSupport info) {
    List<?> values;
    try {
      values = (List<?>) info.getTransferable().getTransferData(FLAVOR);
    } catch (UnsupportedFlavorException | IOException ex) {
      values = Collections.emptyList();
    }
    return values;
  }

  @Override protected void exportDone(JComponent c, Transferable data, int action) {
    cleanup(c, action == MOVE);
  }

  private void cleanup(JComponent c, boolean remove) {
    c.getRootPane().getGlassPane().setVisible(false);
    // c.setCursor(Cursor.getDefaultCursor());
    if (remove && !indices.isEmpty()) {
      DefaultTableModel model = (DefaultTableModel) ((JTable) c).getModel();
      if (addCount > 0) {
        for (int i = 0; i < indices.size(); i++) {
          if (indices.get(i) >= addIndex) {
            indices.set(i, indices.get(i) + addCount);
          }
        }
      }
      for (int i = indices.size() - 1; i >= 0; i--) {
        model.removeRow(indices.get(i));
      }
    }
    indices.clear();
    addCount = 0;
    addIndex = -1;
  }
}

class TreeTransferHandler extends TransferHandler {
  private final DataFlavor nodesFlavor = new DataFlavor(List.class, "List of TreeNode");

  @Override public int getSourceActions(JComponent c) {
    return c instanceof JTree && TreeUtils.canStartDrag((JTree) c) ? COPY_OR_MOVE : NONE;
  }

  @Override protected Transferable createTransferable(JComponent c) {
    Transferable transferable = null;
    if (c instanceof JTree && ((JTree) c).getSelectionPaths() != null) {
      List<MutableTreeNode> copies = new ArrayList<>();
      Arrays.stream(((JTree) c).getSelectionPaths()).forEach(path -> {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
        copies.add(TreeUtils.deepCopy(node, clone));
      });
      transferable = new Transferable() {
        @Override public DataFlavor[] getTransferDataFlavors() {
          return new DataFlavor[] {nodesFlavor};
        }

        @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
          return Objects.equals(nodesFlavor, flavor);
        }

        @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
          if (isDataFlavorSupported(flavor)) {
            return copies;
          } else {
            throw new UnsupportedFlavorException(flavor);
          }
        }
      };
    }
    return transferable;
  }

  @Override public boolean canImport(TransferSupport support) {
    DropLocation dl = support.getDropLocation();
    Component c = support.getComponent();
    return support.isDrop()
        && support.isDataFlavorSupported(nodesFlavor)
        && c instanceof JTree
        && dl instanceof JTree.DropLocation
        && TreeUtils.canImportDropLocation((JTree) c, (JTree.DropLocation) dl);
  }

  @Override public boolean importData(TransferSupport support) {
    Component c = support.getComponent();
    DropLocation dl = support.getDropLocation();
    Transferable transferable = support.getTransferable();
    return canImport(support)
        && c instanceof JTree
        && dl instanceof JTree.DropLocation
        && insertNode((JTree) c, (JTree.DropLocation) dl, transferable);
  }

  private boolean insertNode(JTree tree, JTree.DropLocation dl, Transferable transferable) {
    TreePath path = dl.getPath();
    Object p = path.getLastPathComponent();
    TreeModel m = tree.getModel();
    List<?> nodes = getTransferData(transferable);
    if (p instanceof MutableTreeNode && m instanceof DefaultTreeModel) {
      MutableTreeNode parent = (MutableTreeNode) p;
      DefaultTreeModel model = (DefaultTreeModel) m;
      int childIndex = dl.getChildIndex();
      AtomicInteger index = new AtomicInteger(getDropIndex(parent, childIndex));
      nodes.stream()
          .filter(MutableTreeNode.class::isInstance)
          .map(MutableTreeNode.class::cast)
          .forEach(n -> model.insertNodeInto(n, parent, index.getAndIncrement()));
    }
    return !nodes.isEmpty();
  }

  private static int getDropIndex(MutableTreeNode parent, int childIndex) {
    // Configure for drop mode.
    int index = childIndex; // DropMode.INSERT
    if (childIndex == -1) { // DropMode.ON
      index = parent.getChildCount();
    }
    return index;
  }

  private List<?> getTransferData(Transferable t) {
    List<?> nodes;
    try {
      nodes = (List<?>) t.getTransferData(nodesFlavor);
    } catch (UnsupportedFlavorException | IOException ex) {
      nodes = Collections.emptyList();
    }
    return nodes;
  }

  @Override protected void exportDone(JComponent src, Transferable data, int action) {
    if (src instanceof JTree && (action & MOVE) == MOVE) {
      cleanup((JTree) src);
    }
  }

  private void cleanup(JTree tree) {
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    TreePath[] selectionPaths = tree.getSelectionPaths();
    if (selectionPaths != null) {
      for (TreePath path : selectionPaths) {
        model.removeNodeFromParent((MutableTreeNode) path.getLastPathComponent());
      }
    }
  }
}

final class TreeUtils {
  private TreeUtils() {
    /* Singleton */
  }

  public static boolean canStartDrag(JTree tree) {
    TreePath[] paths = tree.getSelectionPaths();
    return paths != null && canStartDragPaths(paths);
  }

  public static boolean canStartDragPaths(TreePath... paths) {
    return Arrays.stream(paths)
        .map(TreePath::getLastPathComponent)
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .map(DefaultMutableTreeNode::getLevel)
        .distinct()
        .filter(level -> level != 0) // Level 0 is excluded because it is the root node
        .count() == 1; // All nodes are at the same level
  }

  public static boolean canImportDropLocation(JTree tree, JTree.DropLocation dl) {
    // Do not allow drop to descendant and drag-source selections
    // int dropRow = tree.getRowForPath(dl.getPath());
    Point pt = dl.getDropPoint();
    int dropRow = tree.getRowForLocation(pt.x, pt.y);
    int[] selRows = tree.getSelectionRows();
    return selRows != null && IntStream
        .of(selRows)
        .noneMatch(r -> r == dropRow || isDescendant(tree, r, dropRow));
  }

  private static boolean isDescendant(JTree tree, int selRow, int dropRow) {
    Object node = tree.getPathForRow(selRow).getLastPathComponent();
    return node instanceof DefaultMutableTreeNode
        && isDescendant2(tree, dropRow, (DefaultMutableTreeNode) node);
  }

  private static boolean isDescendant2(JTree tree, int dropRow, DefaultMutableTreeNode node) {
    Enumeration<?> e = node.depthFirstEnumeration();
    return Collections.list(e)
        .stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .map(DefaultMutableTreeNode::getPath)
        .map(TreePath::new)
        .map(tree::getRowForPath)
        .anyMatch(row -> row == dropRow);
  }

  public static DefaultMutableTreeNode deepCopy(MutableTreeNode src, DefaultMutableTreeNode tgt) {
    // Java 9: Collections.list(src.children()).stream()
    Collections.list((Enumeration<?>) src.children()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .forEach(node -> {
          DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
          tgt.add(clone);
          if (!node.isLeaf()) {
            deepCopy(node, clone);
          }
        });
    return tgt;
  }
}
