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
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private final TransferHandler handler = new TableRowTransferHandler();
  private final String[] columnNames = {"String", "Integer", "Boolean"};
  private final Object[][] data = {
    {"AAA", 12, true}, {"aaa", 1, false},
    {"BBB", 13, true}, {"bbb", 2, false},
    {"CCC", 15, true}, {"ccc", 3, false},
    {"DDD", 17, true}, {"ddd", 4, false},
    {"EEE", 18, true}, {"eee", 5, false},
    {"FFF", 19, true}, {"fff", 6, false},
    {"GGG", 92, true}, {"ggg", 0, false}
  };
  private final TableModel model = new DefaultTableModel(data, columnNames) {
    @Override public Class<?> getColumnClass(int column) {
      // ArrayIndexOutOfBoundsException: 0 >= 0
      // [JDK-6967479] JTable sorter fires even if the model is empty - Java Bug System
      // https://bugs.openjdk.java.net/browse/JDK-6967479
      // return getValueAt(0, column).getClass();
      switch (column) {
        case 0: return String.class;
        case 1: return Number.class;
        case 2: return Boolean.class;
        default: return super.getColumnClass(column);
      }
    }
  };
  private final JTable table = new JTable(model);

  private MainPanel() {
    super(new BorderLayout());
    table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    table.setTransferHandler(handler);
    table.setDropMode(DropMode.INSERT_ROWS);
    table.setDragEnabled(true);
    table.setFillsViewportHeight(true);
    // table.setAutoCreateRowSorter(true); // XXX

    // Disable row Cut, Copy, Paste
    ActionMap map = table.getActionMap();
    Action dummy = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) { /* Dummy action */ }
    };
    map.put(TransferHandler.getCutAction().getValue(Action.NAME), dummy);
    map.put(TransferHandler.getCopyAction().getValue(Action.NAME), dummy);
    map.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy);

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(table));
    p.setBorder(BorderFactory.createTitledBorder("Drag & Drop JTable"));
    add(p);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
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

// Demo - BasicDnD (The Java™ Tutorials > Creating a GUI With JFC/Swing > Drag and Drop and Data Transfer)
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/basicdemo.html)
// Demo - DropDemo (The Java™ Tutorials > Creating a GUI With JFC/Swing > Drag and Drop and Data Transfer)
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/dropmodedemo.html
// @see https://docs.oracle.com/javase/tutorial/uiswing/examples/dnd/DropDemoProject/src/dnd/ListTransferHandler.java
class TableRowTransferHandler extends TransferHandler {
  protected final DataFlavor localObjectFlavor;
  protected int[] indices;
  protected int addIndex = -1; // Location where items were added
  protected int addCount; // Number of items added.

  protected TableRowTransferHandler() {
    super();
    // localObjectFlavor = new ActivationDataFlavor(
    //     Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
    localObjectFlavor = new DataFlavor(List.class, "List of items");
  }

  @Override protected Transferable createTransferable(JComponent c) {
    c.getRootPane().getGlassPane().setVisible(true);
    JTable table = (JTable) c;
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    // List<Object> list = new ArrayList<>();
    // indices = table.getSelectedRows();
    // for (int i: indices) {
    //   list.add(model.getDataVector().get(i));
    // }
    // Object[] transferedObjects = list.toArray();
    indices = table.getSelectedRows();
    @SuppressWarnings("JdkObsolete")
    List<?> transferedObjects = Arrays.stream(indices)
        .mapToObj(model.getDataVector()::get).collect(Collectors.toList());
    // return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    return new Transferable() {
      @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {localObjectFlavor};
      }

      @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Objects.equals(localObjectFlavor, flavor);
      }

      @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
          return transferedObjects;
        } else {
          throw new UnsupportedFlavorException(flavor);
        }
      }
    };
  }

  @Override public boolean canImport(TransferHandler.TransferSupport info) {
    boolean isDroppable = info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
    // XXX bug? The cursor flickering problem with JTableHeader:
    // info.getComponent().setCursor(isDroppable ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
    Component glassPane = ((JComponent) info.getComponent()).getRootPane().getGlassPane();
    glassPane.setCursor(isDroppable ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
    return isDroppable;
  }

  @Override public int getSourceActions(JComponent c) {
    return TransferHandler.MOVE; // TransferHandler.COPY_OR_MOVE;
  }

  @SuppressWarnings("PMD.ReplaceVectorWithList")
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
    // boolean insert = dl.isInsert();
    int max = model.getRowCount();
    int index = dl.getRow();
    // index = index < 0 ? max : index; // If it is out of range, it is appended to the end
    // index = Math.min(index, max);
    index = index >= 0 && index < max ? index : max;
    addIndex = index;
    // target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    try {
      List<?> values = (List<?>) info.getTransferable().getTransferData(localObjectFlavor);
      addCount = values.size();
      for (Object o: values) {
        int i = index++;
        model.insertRow(i, (Vector<?>) o);
        target.getSelectionModel().addSelectionInterval(i, i);
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
