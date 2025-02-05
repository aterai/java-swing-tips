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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();
    TransferHandler handler = new TableRowTransferHandler();
    // TransferHandler handler2 = new TableColumnTransferHandler();

    JInternalFrame f1 = new JInternalFrame("11111111", true, true, true, true);
    f1.add(new JScrollPane(makeDragAndDropTable(handler)));
    f1.setOpaque(false);
    desktop.add(f1, 1, 1);
    f1.setBounds(0, 0, 240, 160);

    JInternalFrame f2 = new JInternalFrame("22222222", true, true, true, true);
    f2.add(new JScrollPane(makeDragAndDropTable(handler)));
    desktop.add(f2, 1, 0);
    f2.setBounds(50, 50, 240, 160);
    f2.setOpaque(false);

    EventQueue.invokeLater(() -> {
      JInternalFrame[] frames = desktop.getAllFrames();
      Arrays.asList(frames).forEach(f -> f.setVisible(true));
    });
    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable makeDragAndDropTable(TransferHandler handler) {
    JTable table = new JTable(makeModel());
    table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    table.setTransferHandler(handler);
    table.setDropMode(DropMode.INSERT_ROWS);
    table.setDragEnabled(true);
    table.setFillsViewportHeight(true);

    table.getTableHeader().setReorderingAllowed(false);
    // table.getTableHeader().setTransferHandler(handler2);
    // Handler h = new Handler();
    // table.getTableHeader().addMouseListener(h);
    // table.getTableHeader().addMouseMotionListener(h);

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
    return table;
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

  // private int index = -1;
  // private class Handler extends MouseAdapter { // , BeforeDrag
  //   private Point startPt;
  //   private final int gestureMotionThreshold = DragSource.getDragThreshold();
  //   // MouseListener
  //   @Override public void mousePressed(MouseEvent e) {
  //     JTableHeader src = (JTableHeader) e.getComponent();
  //     startPt = e.getPoint(); // e.getDragOrigin();
  //     // System.out.println(startPt);
  //   }
  //
  //   @Override public void mouseDragged(MouseEvent e) {
  //     Point tabPt = e.getPoint(); // e.getDragOrigin();
  //     if (startPt != null && startPt.distance(tabPt) > gestureMotionThreshold) {
  //       JTableHeader src = (JTableHeader) e.getComponent();
  //       System.out.println("src: " + src);
  //       TransferHandler th = src.getTransferHandler();
  //       index = src.columnAtPoint(tabPt);
  //       th.exportAsDrag(src, e, TransferHandler.MOVE);
  //       // lineRect.setBounds(0, 0, 0, 0);
  //       // src.getRootPane().getGlassPane().setVisible(true);
  //       // src.setDropLocation(new DropLocation(tabPt, -1), null, true);
  //       startPt = null;
  //     }
  //   }
  // }
  //
  // class TableColumnTransferHandler extends TransferHandler {
  //   private final DataFlavor localObjectFlavor = new ActivationDataFlavor(
  //     JTableHeader.class, DataFlavor.javaJVMLocalObjectMimeType,
  //     "Integer Column Model Index");
  //
  //   @Override protected Transferable createTransferable(JComponent c) {
  //     System.out.println("createTransferable");
  //     JTableHeader header = (JTableHeader) c;
  //     // int index = table.getSelectedColumn();
  //     // TableColumn column = header.getDraggedColumn();
  //     return new DataHandler(header, localObjectFlavor.getMimeType());
  //   }
  //
  //   @Override public boolean canImport(TransferSupport info) {
  //     // System.out.println("canImport");
  //     return info.isDataFlavorSupported(localObjectFlavor);
  //   }
  //
  //   @Override public int getSourceActions(JComponent c) {
  //     System.out.println("getSourceActions");
  //     return TransferHandler.MOVE;
  //   }
  //
  //   @Override public boolean importData(TransferSupport info) {
  //     System.out.println("importData");
  //     JTableHeader target = (JTableHeader) info.getComponent();
  //     // JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
  //     try {
  //       JTableHeader source = (JTableHeader) info.getTransferable().getTransferData(
  //           localObjectFlavor);
  //       System.out.println("source: " + source);
  //       if (!source.equals(target)) {
  //         System.out.println("-------------------------------");
  //         TableColumn column = source.getColumnModel().getColumn(index);
  //         source.getColumnModel().removeColumn(column);
  //         target.getColumnModel().addColumn(column);
  //       }
  //     } catch (Exception ex) {
  //       ex.printStackTrace();
  //     }
  //     return false;
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

// class TableDropTargetAdapter extends DropTargetAdapter {
//   @Override public void drop(...
//   @Override public void dragEnter(DropTargetDragEvent e) {
//     Component c = e.getDropTargetContext().getComponent();
//     Container cn = SwingUtilities.getAncestorOfClass(JInternalFrame.class, c);
//     if (cn instanceof JInternalFrame) {
//       JInternalFrame f = (JInternalFrame) cn;
//       f.moveToFront();
//       f.getParent().repaint();
//     }
//   }
// }

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
  private Component source;

  // protected TableRowTransferHandler() {
  //   super();
  //   localObjectFlavor = new ActivationDataFlavor(
  //       Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
  // }

  @Override protected Transferable createTransferable(JComponent c) {
    getRootGlassPane(c).ifPresent(p -> p.setVisible(true));
    source = c;
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

  private static Optional<Component> getRootGlassPane(Component c) {
    Container dp = SwingUtilities.getAncestorOfClass(JDesktopPane.class, c);
    Component glass = null;
    if (dp instanceof JDesktopPane) {
      glass = ((JComponent) dp).getRootPane().getGlassPane();
    }
    return Optional.ofNullable(glass);
  }

  private static JInternalFrame getInternalFrame(Component c) {
    Container cn = SwingUtilities.getAncestorOfClass(JInternalFrame.class, c);
    JInternalFrame frame = null;
    if (cn instanceof JInternalFrame) {
      frame = (JInternalFrame) cn;
    }
    return frame;
  }

  private boolean canDropTable(TransferSupport info) {
    Component c = info.getComponent();
    Container p = SwingUtilities.getAncestorOfClass(JDesktopPane.class, c);
    boolean b = c instanceof JTable && p instanceof JDesktopPane;
    return b && (c.equals(source) || canDropTargetTable(info, (JDesktopPane) p, (JTable) c));
  }

  private boolean canDropTargetTable(TransferSupport info, JDesktopPane dp, JTable target) {
    JInternalFrame sf = getInternalFrame(source);
    JInternalFrame tf = getInternalFrame(target);
    boolean nonNull = Objects.nonNull(sf) && Objects.nonNull(tf);
    boolean isBack = dp.getIndexOf(tf) >= dp.getIndexOf(sf);
    Point pt = SwingUtilities.convertPoint(target, info.getDropLocation().getDropPoint(), dp);
    return nonNull && isBack && notIntersectionArea(sf, tf, pt);
  }

  private static boolean notIntersectionArea(JInternalFrame sf, JInternalFrame tf, Point pt) {
    Rectangle r = sf.getBounds().intersection(tf.getBounds());
    return !r.contains(pt);
  }

  @Override public boolean canImport(TransferSupport info) {
    boolean isSupported = info.isDataFlavorSupported(FLAVOR) && canDropTable(info);
    boolean canDrop = info.isDrop() && isSupported;
    getRootGlassPane(info.getComponent()).ifPresent(p -> {
      Cursor cursor = canDrop ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop;
      p.setCursor(cursor);
    });
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
    addCount = Objects.equals(source, target) && info.isDrop() ? values.size() : 0;
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
    // cleanup(c, action == MOVE);
    getRootGlassPane(c).ifPresent(p -> p.setVisible(false));
    // c.setCursor(Cursor.getDefaultCursor());
    if (action == MOVE && !indices.isEmpty()) {
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

  // private void cleanup(JComponent c, boolean remove) {
  //   getRootGlassPane(c).ifPresent(p -> p.setVisible(false));
  //   // c.setCursor(Cursor.getDefaultCursor());
  //   if (remove && !indices.isEmpty()) {
  //     DefaultTableModel model = (DefaultTableModel) ((JTable) c).getModel();
  //     if (addCount > 0) {
  //       for (int i = 0; i < indices.size(); i++) {
  //         if (indices.get(i) >= addIndex) {
  //           // indices[i] += addCount;
  //           indices.set(i, indices.get(i) + addCount);
  //         }
  //       }
  //     }
  //     for (int i = indices.size() - 1; i >= 0; i--) {
  //       model.removeRow(indices.get(i));
  //     }
  //   }
  //   indices.clear();
  //   addCount = 0;
  //   addIndex = -1;
  // }
}
