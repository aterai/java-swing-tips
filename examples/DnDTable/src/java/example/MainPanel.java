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
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
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
  protected int draggedIndex = -1;
  protected int targetIndex = -1;
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

  protected void initTargetLine(Point p) {
    Rectangle rect = new Rectangle();
    int cellHeight = getRowHeight();
    int lineWidth = getWidth();
    int lineHeight = 2;
    rect.setSize(lineWidth, cellHeight);
    targetLine.setSize(lineWidth, lineHeight);
    targetIndex = -1;
    int rowCount = getRowCount();
    for (int i = 0; i < rowCount; i++) {
      rect.setLocation(0, cellHeight * i - cellHeight / 2);
      if (rect.contains(p)) {
        targetIndex = i;
        targetLine.setLocation(0, i * cellHeight);
        break;
      }
    }
    if (targetIndex < 0) {
      targetIndex = rowCount;
      targetLine.setLocation(0, targetIndex * cellHeight - lineHeight);
    }
  }

  // Interface: DragGestureListener
  @Override public void dragGestureRecognized(DragGestureEvent e) {
    boolean oneOrMore = getSelectedRowCount() > 1;
    draggedIndex = rowAtPoint(e.getDragOrigin());
    if (oneOrMore || draggedIndex < 0) {
      return;
    }
    try {
      e.startDrag(DragSource.DefaultMoveDrop, this, this);
    } catch (InvalidDnDOperationException ex) {
      throw new IllegalStateException(ex);
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
    // e.getDragSourceContext().setCursor(Cursor.getDefaultCursor());
  }

  // Interface: Transferable
  @Override public Object getTransferData(DataFlavor flavor) {
    return this;
  }

  @Override public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {FLAVOR};
  }

  @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
    return NAME.equals(flavor.getHumanPresentableName());
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
      } else {
        e.rejectDrag();
        setCursor(DragSource.DefaultMoveNoDrop);
        return;
      }
      initTargetLine(e.getLocation());
      repaint();
    }

    @Override public void dropActionChanged(DropTargetDragEvent e) {
      // if (isDragAcceptable(e)) {
      //   e.acceptDrag(e.getDropAction());
      // } else {
      //   e.rejectDrag();
      // }
    }

    @Override public void drop(DropTargetDropEvent e) {
      // Transferable t = e.getTransferable();
      // DataFlavor[] f = t.getTransferDataFlavors();
      // Component c = null;
      // try {
      //   c = (Component) t.getTransferData(f[0]);
      // } catch (UnsupportedFlavorException | IOException ex) {
      //   e.dropComplete(false);
      // }
      // if (c instanceof JTable) {
      //   JTable table = (JTable) c;
      //   DefaultTableModel model = (DefaultTableModel) table.getModel();
      DefaultTableModel model = (DefaultTableModel) getModel();
      if (isDropAcceptable(e)) {
        if (targetIndex == draggedIndex) {
          setRowSelectionInterval(targetIndex, targetIndex);
        } else {
          int tg = targetIndex < draggedIndex ? targetIndex : targetIndex - 1;
          model.moveRow(draggedIndex, draggedIndex, tg);
          setRowSelectionInterval(tg, tg);
        }
        e.dropComplete(true);
      } else {
        e.dropComplete(false);
      }
      e.dropComplete(false);
      setCursor(Cursor.getDefaultCursor());
      targetIndex = -1;
      repaint();
    }

    private boolean isDragAcceptable(DropTargetDragEvent e) {
      return isDataFlavorSupported(e.getCurrentDataFlavors()[0]);
    }

    private boolean isDropAcceptable(DropTargetDropEvent e) {
      return isDataFlavorSupported(e.getTransferable().getTransferDataFlavors()[0]);
    }
  }
}
