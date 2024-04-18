// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(new DefaultTableModel(10, 3));
    table.setRowHeight(24);
    table.setAutoCreateRowSorter(true);
    add(new JLayer<>(new JScrollPane(table), new RowHeightResizeLayer()));
    setPreferredSize(new Dimension(320, 240));
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

class RowHeightResizeLayer extends LayerUI<JScrollPane> {
  private static final int MIN_ROW_HEIGHT = 16;
  private static final Cursor RESIZE_CURSOR = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
  private int mouseOffsetY;
  private int resizingRow = -1;
  private Cursor otherCursor = RESIZE_CURSOR;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    Component c = e.getComponent();
    if (c instanceof JTable && e.getID() == MouseEvent.MOUSE_PRESSED) {
      JTable table = (JTable) c;
      resizingRow = getResizeTargetRow(table, e.getPoint());
      if (resizingRow >= 0) {
        mouseOffsetY = e.getY() - table.getRowHeight(resizingRow);
        e.consume();
      }
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    Component c = e.getComponent();
    if (!(c instanceof JTable)) {
      return;
    }
    JTable table = (JTable) c;
    if (e.getID() == MouseEvent.MOUSE_MOVED) {
      boolean isResizing = RESIZE_CURSOR.equals(table.getCursor());
      int row = getResizeTargetRow(table, e.getPoint());
      if (row >= 0 != isResizing) {
        Cursor tmp = table.getCursor();
        table.setCursor(otherCursor);
        otherCursor = tmp;
      }
    } else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
      int newHeight = e.getY() - mouseOffsetY;
      if (newHeight > MIN_ROW_HEIGHT && resizingRow >= 0) {
        table.setRowHeight(resizingRow, newHeight);
      }
      e.consume();
    }
  }

  private int getResizeTargetRow(JTable table, Point p) {
    int row = table.rowAtPoint(p);
    int col = table.columnAtPoint(p);
    Rectangle r = table.getCellRect(row, col, false);
    r.grow(0, -2);
    return r.contains(p) ? -1 : getTargetRowIndex(p, r, row);
  }

  private static int getTargetRowIndex(Point p, Rectangle r, int row) {
    return p.y < r.getCenterY() ? row - 1 : row;
  }
}

// // java - Adjusting individual row height using cursor on JTable - Stack Overflow
// // https://stackoverflow.com/questions/4387995/adjusting-individual-row-height-using-cursor-on-jtable
// class RowHeightResizer extends MouseAdapter {
//   private static Cursor RESIZE_CURSOR = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
//   private int mouseOffsetY;
//   private int resizingRow;
//   private Cursor otherCursor = RESIZE_CURSOR;
//
//   private int getResizingRow(JTable table, Point p) {
//     return getResizingRow(table, p, table.rowAtPoint(p));
//   }
//
//   private int getResizingRow(JTable table, Point p, int row) {
//     int col = table.columnAtPoint(p);
//     if (row == -1 || col == -1) {
//       return -1;
//     }
//     Rectangle r = table.getCellRect(row, col, true);
//     r.grow(0, -3);
//     if (r.contains(p)) {
//       return -1;
//     }
//     return p.y < r.getCenterY() ? row - 1 : row;
//   }
//
//   @Override public void mousePressed(MouseEvent e) {
//     JTable table = (JTable) e.getComponent();
//     Point p = e.getPoint();
//     resizingRow = getResizingRow(table, p);
//     mouseOffsetY = p.y - table.getRowHeight(resizingRow);
//   }
//
//   @Override public void mouseMoved(MouseEvent e) {
//     JTable table = (JTable) e.getComponent();
//     boolean isResizing = table.getCursor() == RESIZE_CURSOR;
//     if ((getResizingRow(table, e.getPoint()) >= 0) != isResizing) {
//       Cursor tmp = table.getCursor();
//       table.setCursor(otherCursor);
//       otherCursor = tmp;
//     }
//   }
//
//   @Override public void mouseDragged(MouseEvent e) {
//     JTable table = (JTable) e.getComponent();
//     int mouseY = e.getY();
//     if (resizingRow >= 0) {
//       int newHeight = mouseY - mouseOffsetY;
//       if (newHeight > 0) {
//         table.setRowHeight(resizingRow, newHeight);
//       }
//     }
//   }
// }
