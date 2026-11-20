// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private static final Class<?>[] COLUMN_CLASSES = {String.class, Integer.class, Boolean.class};

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new DnDTable(makeModel());
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());

    TableColumn col = table.getColumnModel().getColumn(0);
    col.setMinWidth(60);
    col.setMaxWidth(60);
    col.setResizable(false);

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false},
        {"eee", 1, true}, {"GGG", 3, false}, {"hhh", 72, true}, {"fff", 4, false},
    };
    return new DefaultTableModel(data, columnNames) {
      // Do not depend on getValueAt(0, column): all rows can be deleted from the popup menu
      @Override public Class<?> getColumnClass(int column) {
        return COLUMN_CLASSES[column];
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

final class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  /* default */ TablePopupMenu() {
    super();
    add("add").addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      model.addRow(new Object[] {"New row", model.getRowCount(), false});
      Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
      table.scrollRectToVisible(r);
    });
    addSeparator();
    delete = add("delete");
    delete.addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      int[] selection = table.getSelectedRows();
      for (int i = selection.length - 1; i >= 0; i--) {
        model.removeRow(table.convertRowIndexToModel(selection[i]));
      }
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTable) {
      delete.setEnabled(((JTable) c).getSelectedRowCount() > 0);
      super.show(c, x, y);
    }
  }
}

class DnDTable extends JTable implements DragGestureListener, DragSourceListener, Transferable {
  private static final String NAME = "test";
  private static final String MIME_TYPE = DataFlavor.javaJVMLocalObjectMimeType;
  private static final DataFlavor FLAVOR = new DataFlavor(MIME_TYPE, NAME);
  private static final Color LINE_COLOR = new Color(0xFF_64_64);
  private static final Color EVEN_COLOR = new Color(0xF0_F0_F0);
  private static final int LINE_HEIGHT = 2;
  private int draggedIndex = -1;
  private int targetIndex = -1;
  private final Rectangle targetLine = new Rectangle();

  protected DnDTable(TableModel model) {
    super(model);
    new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new RowDropTargetListener(), true);
    DragSource ds = DragSource.getDefaultDragSource();
    ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (targetIndex >= 0) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(LINE_COLOR);
      g2.fill(targetLine);
      g2.dispose();
    }
  }

  @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
    Component c = super.prepareRenderer(tcr, row, column);
    if (isRowSelected(row)) {
      c.setForeground(getSelectionForeground());
      c.setBackground(getSelectionBackground());
    } else {
      c.setForeground(getForeground());
      c.setBackground(row % 2 == 0 ? EVEN_COLOR : getBackground());
    }
    return c;
  }

  protected void updateTargetLine(Point p) {
    int rowCount = getRowCount();
    int row = rowAtPoint(p);
    if (row < 0) {
      targetIndex = rowCount;
    } else {
      // Insert after the row if the cursor is in its lower half
      Rectangle r = getCellRect(row, 0, true);
      targetIndex = p.y < r.getCenterY() ? row : row + 1;
    }
    int y;
    if (targetIndex < rowCount) {
      y = getCellRect(targetIndex, 0, true).y;
    } else if (rowCount > 0) {
      Rectangle r = getCellRect(rowCount - 1, 0, true);
      y = r.y + r.height - LINE_HEIGHT;
    } else {
      y = 0;
    }
    targetLine.setBounds(0, y, getWidth(), LINE_HEIGHT);
  }

  // Interface: DragGestureListener
  @Override public void dragGestureRecognized(DragGestureEvent e) {
    draggedIndex = rowAtPoint(e.getDragOrigin());
    // Only a single selected row under the drag origin can be moved
    if (getSelectedRowCount() == 1 && draggedIndex >= 0 && isRowSelected(draggedIndex)) {
      try {
        e.startDrag(DragSource.DefaultMoveDrop, this, this);
      } catch (InvalidDnDOperationException ex) {
        throw new IllegalStateException(ex);
      }
    }
  }

  // Interface: DragSourceListener
  @Override public void dragEnter(DragSourceDragEvent e) {
    e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
  }

  @Override public void dragExit(DragSourceEvent e) {
    e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
  }

  @Override public void dragOver(DragSourceDragEvent e) {
    /* not needed */
  }

  @Override public void dropActionChanged(DragSourceDragEvent e) {
    /* not needed */
  }

  @Override public void dragDropEnd(DragSourceDropEvent e) {
    // Always called at the end of a drag, including a cancel or a drop outside the table
    setCursor(Cursor.getDefaultCursor());
    draggedIndex = -1;
    targetIndex = -1;
    repaint();
  }

  // Interface: Transferable
  @Override public Object getTransferData(DataFlavor flavor) {
    return this;
  }

  @Override public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {FLAVOR};
  }

  @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
    return FLAVOR.equals(flavor);
  }

  private final class RowDropTargetListener implements DropTargetListener {
    @Override public void dragExit(DropTargetEvent e) {
      targetIndex = -1;
      repaint();
    }

    @Override public void dragEnter(DropTargetDragEvent e) {
      if (isDragAcceptable(e)) {
        e.acceptDrag(e.getDropAction());
      } else {
        e.rejectDrag();
      }
    }

    @Override public void dragOver(DropTargetDragEvent e) {
      if (isDragAcceptable(e)) {
        e.acceptDrag(e.getDropAction());
        setCursor(DragSource.DefaultMoveDrop);
        updateTargetLine(e.getLocation());
        repaint();
      } else {
        e.rejectDrag();
        setCursor(DragSource.DefaultMoveNoDrop);
      }
    }

    @Override public void dropActionChanged(DropTargetDragEvent e) {
      /* not needed */
    }

    @Override public void drop(DropTargetDropEvent e) {
      if (isDropAcceptable(e)) {
        e.acceptDrop(e.getDropAction());
        DefaultTableModel model = (DefaultTableModel) getModel();
        // Removing the dragged row shifts the following rows up by one
        int toIndex = targetIndex > draggedIndex ? targetIndex - 1 : targetIndex;
        if (toIndex != draggedIndex) {
          model.moveRow(draggedIndex, draggedIndex, toIndex);
        }
        setRowSelectionInterval(toIndex, toIndex);
        e.dropComplete(true);
      } else {
        e.rejectDrop();
      }
      setCursor(Cursor.getDefaultCursor());
      targetIndex = -1;
      repaint();
    }

    private boolean isDragAcceptable(DropTargetDragEvent e) {
      return draggedIndex >= 0 && e.isDataFlavorSupported(FLAVOR);
    }

    private boolean isDropAcceptable(DropTargetDropEvent e) {
      return draggedIndex >= 0 && targetIndex >= 0 && e.isDataFlavorSupported(FLAVOR);
    }
  }
}
