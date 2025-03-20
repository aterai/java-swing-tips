// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    TransferHandler handler = new TableRowTransferHandler();
    JTable table = new JTable(makeModel());
    table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    table.setTransferHandler(handler);
    table.setDropMode(DropMode.INSERT_ROWS);
    table.setDragEnabled(true);
    table.setFillsViewportHeight(true);
    // table.setAutoCreateRowSorter(true); // XXX

    // // Disable JTable rows Cut, Copy, Paste
    // ActionMap am = table.getActionMap();
    // Action empty = new AbstractAction() {
    //   @Override public void actionPerformed(ActionEvent e) {
    //     /* do nothing */
    //   }
    // };
    // am.put(TransferHandler.getCutAction().getValue(Action.NAME), empty);
    // am.put(TransferHandler.getCopyAction().getValue(Action.NAME), empty);
    // am.put(TransferHandler.getPasteAction().getValue(Action.NAME), empty);

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(table));
    p.setBorder(BorderFactory.createTitledBorder("Drag & Drop JTable"));
    add(p);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
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

// Demo - BasicDnD (The Java™ Tutorials > ... > Drag and Drop and Data Transfer)
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/basicdemo.html
// Demo - DropDemo (The Java™ Tutorials > ... > Drag and Drop and Data Transfer)
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/dropmodedemo.html
// @see https://docs.oracle.com/javase/tutorial/uiswing/examples/dnd/DropDemoProject/src/dnd/ListTransferHandler.java
class TableRowTransferHandler extends TransferHandler {
  protected static final DataFlavor FLAVOR = new DataFlavor(List.class, "List of items");
  // private int[] indices;
  private final List<Integer> indices = new ArrayList<>();
  private int addIndex = -1; // Location where items were added
  private int addCount; // Number of items added.

  // protected TableRowTransferHandler() {
  //   super();
  //   localObjectFlavor = new ActivationDataFlavor(
  //       Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
  // }

  @Override protected Transferable createTransferable(JComponent c) {
    c.getRootPane().getGlassPane().setVisible(true);
    JTable table = (JTable) c;
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    // List<Object> list = new ArrayList<>();
    // indices = table.getSelectedRows();
    // for (int i : indices) {
    //   list.add(model.getDataVector().get(i));
    // }
    // Object[] transferredRows = list.toArray();
    // indices = table.getSelectedRows();
    for (int i : table.getSelectedRows()) {
      indices.add(i);
    }
    @SuppressWarnings("JdkObsolete")
    List<?> transferredRows = indices.stream()
        .map(model.getDataVector()::get)
        .collect(Collectors.toList());
    // return new DataHandler(transferredRows, FLAVOR.getMimeType());
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
    // XXX bug? The cursor flickering problem with JTableHeader:
    // info.getComponent().setCursor(
    //     canDrop ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
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
    // boolean insert = dl.isInsert();
    int max = model.getRowCount();
    int index;
    if (info.isDrop()) {
      index = ((JTable.DropLocation) info.getDropLocation()).getRow();
    } else { // dl.isInsert()) {
      index = target.getSelectedRow();
    }
    // index = index < 0 ? max : index; // If it is out of range, it is appended to the end
    // index = Math.min(index, max);
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
            // indices[i] += addCount;
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
