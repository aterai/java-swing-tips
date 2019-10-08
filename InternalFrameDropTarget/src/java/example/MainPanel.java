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
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private final TransferHandler handler = new TableRowTransferHandler();
  // private final TransferHandler handler2 = new TableColumnTransferHandler();
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

  private JTable makeDnDTable() {
    JTable table = new JTable(new DefaultTableModel(data, columnNames) {
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
    });
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
    return table;
  }

  public MainPanel() {
    super(new BorderLayout());

    JDesktopPane p = new JDesktopPane();

    JInternalFrame f1 = new JInternalFrame("11111111", true, true, true, true);
    f1.add(new JScrollPane(makeDnDTable()));
    f1.setOpaque(false);
    p.add(f1, 1, 1);
    f1.setBounds(0, 0, 240, 160);
    f1.setVisible(true);
    JInternalFrame f2 = new JInternalFrame("22222222", true, true, true, true);
    f2.add(new JScrollPane(makeDnDTable()));
    p.add(f2, 1, 0);
    f2.setBounds(50, 50, 240, 160);
    f2.setVisible(true);
    f2.setOpaque(false);

    add(p);
    setPreferredSize(new Dimension(320, 240));
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
  //   @Override public boolean canImport(TransferHandler.TransferSupport info) {
  //     // System.out.println("canImport");
  //     return info.isDataFlavorSupported(localObjectFlavor);
  //   }
  //   @Override public int getSourceActions(JComponent c) {
  //     System.out.println("getSourceActions");
  //     return TransferHandler.MOVE;
  //   }
  //   @Override public boolean importData(TransferHandler.TransferSupport info) {
  //     System.out.println("importData");
  //     JTableHeader target = (JTableHeader) info.getComponent();
  //     // JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
  //     try {
  //       JTableHeader source = (JTableHeader) info.getTransferable().getTransferData(localObjectFlavor);
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

// class TableDropTargetAdapter extends DropTargetAdapter {
//   @Override public void drop(...
//   @Override public void dragEnter(DropTargetDragEvent dtde) {
//     Component c = dtde.getDropTargetContext().getComponent();
//     Container cn = SwingUtilities.getAncestorOfClass(JInternalFrame.class, c);
//     if (cn instanceof JInternalFrame) {
//       JInternalFrame f = (JInternalFrame) cn;
//       f.moveToFront();
//       f.getParent().repaint();
//     }
//   }
// }

// Demo - BasicDnD (The Java™ Tutorials > Creating a GUI With JFC/Swing > Drag and Drop and Data Transfer)
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/basicdemo.html)
// Demo - DropDemo (The Java™ Tutorials > Creating a GUI With JFC/Swing > Drag and Drop and Data Transfer)
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/dropmodedemo.html
// @see https://docs.oracle.com/javase/tutorial/uiswing/examples/dnd/DropDemoProject/src/dnd/ListTransferHandler.java
class TableRowTransferHandler extends TransferHandler {
  protected static final DataFlavor FLAVOR = new DataFlavor(List.class, "List of items");
  private int[] indices;
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
    // for (int i: indices) {
    //   list.add(model.getDataVector().get(i));
    // }
    // Object[] transferredObjects = list.toArray();
    indices = table.getSelectedRows();
    @SuppressWarnings("JdkObsolete")
    List<?> transferredObjects = Arrays.stream(indices)
        .mapToObj(model.getDataVector()::get)
        .collect(Collectors.toList());
    // return new DataHandler(transferredObjects, FLAVOR.getMimeType());
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

  private static Optional<Component> getRootGlassPane(Component c) {
    Container dp = SwingUtilities.getAncestorOfClass(JDesktopPane.class, c);
    if (dp instanceof JDesktopPane) {
      return Optional.ofNullable(((JComponent) dp).getRootPane().getGlassPane());
    }
    return Optional.empty();
  }

  private static JInternalFrame getInternalFrame(Component c) {
    Container cn = SwingUtilities.getAncestorOfClass(JInternalFrame.class, c);
    if (cn instanceof JInternalFrame) {
      return (JInternalFrame) cn;
    }
    return null;
  }

  private boolean canDropTableIntersection(TransferHandler.TransferSupport info) {
    Component c = info.getComponent();
    if (!(c instanceof JTable)) {
      return false;
    }
    JTable target = (JTable) c;
    if (!target.equals(source)) {
      Container cn = SwingUtilities.getAncestorOfClass(JDesktopPane.class, target);
      if (cn instanceof JDesktopPane) {
        JDesktopPane dp = (JDesktopPane) cn;
        JInternalFrame sf = getInternalFrame(source);
        JInternalFrame tf = getInternalFrame(target);
        if (Objects.isNull(sf) || Objects.isNull(tf) || dp.getIndexOf(tf) < dp.getIndexOf(sf)) {
          return false;
        }
        Point pt = SwingUtilities.convertPoint(target, info.getDropLocation().getDropPoint(), dp);
        Rectangle rect = sf.getBounds().intersection(tf.getBounds());
        // if (rect.contains(pt)) { return false; }
        return !rect.contains(pt);
        // tf.moveToFront();
        // tf.getParent().repaint();
      }
    }
    return true;
  }

  @Override public boolean canImport(TransferHandler.TransferSupport info) {
    boolean isSupported = info.isDataFlavorSupported(FLAVOR) && canDropTableIntersection(info);
    boolean canDrop = info.isDrop() && isSupported;
    // XXX bug? The cursor flickering problem with JTableHeader:
    // info.getComponent().setCursor(canDrop ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
    getRootGlassPane(info.getComponent())
        .ifPresent(p -> p.setCursor(canDrop ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop));
    return canDrop;
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
    // boolean insert = dl.isInsert();
    int max = model.getRowCount();
    int index = dl.getRow();
    // index = index < 0 ? max : index; // If it is out of range, it is appended to the end
    // index = Math.min(index, max);
    index = index >= 0 && index < max ? index : max;
    addIndex = index;
    // target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    try {
      List<?> values = (List<?>) info.getTransferable().getTransferData(FLAVOR);
      if (Objects.equals(source, target)) {
        addCount = values.size();
      }
      Object[] type = new Object[0];
      for (Object o: values) {
        int row = index++;
        // model.insertRow(row, (Vector<?>) o);
        model.insertRow(row, ((List<?>) o).toArray(type));
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
    getRootGlassPane(c).ifPresent(p -> p.setVisible(false));
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
