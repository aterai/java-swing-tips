// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    FileModel model = new FileModel();
    JTable table = new JTable(model);

    DropTargetListener dtl = new DropTargetAdapter() {
      @Override public void dragOver(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
          dtde.acceptDrag(DnDConstants.ACTION_COPY);
          return;
        }
        dtde.rejectDrag();
      }

      @Override public void drop(DropTargetDropEvent dtde) {
        try {
          if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            Transferable transferable = dtde.getTransferable();
            List<?> list = (List<?>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            for (Object o: list) {
              if (o instanceof File) {
                model.addPath(((File) o).toPath());
              }
            }
            dtde.dropComplete(true);
            return;
          }
        } catch (UnsupportedFlavorException | IOException ex) {
          ex.printStackTrace();
        }
        dtde.rejectDrop();
      }
    };

    new DropTarget(table, DnDConstants.ACTION_COPY, dtl, true);
    // new DropTarget(scroll.getViewport(), DnDConstants.ACTION_COPY, dtl, true);

    // table.setDropMode(DropMode.INSERT_ROWS);
    // table.setTransferHandler(new FileTransferHandler());

    TableColumn col = table.getColumnModel().getColumn(0);
    col.setMinWidth(60);
    col.setMaxWidth(60);
    col.setResizable(false);

    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());
    add(new JScrollPane(table));
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
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// // table.setDropMode(DropMode.INSERT_ROWS);
// // table.setTransferHandler(new FileTransferHandler());
// class FileTransferHandler extends TransferHandler {
//   @Override public boolean importData(JComponent component, Transferable transferable) {
//     try {
//       if (canImport(component, transferable.getTransferDataFlavors())) {
//         // DefaultTableModel model = (DefaultTableModel) ((JTable) component).getModel();
//         FileModel model = (FileModel) ((JTable) component).getModel();
//         for (Object o: (List) transferable.getTransferData(DataFlavor.javaFileListFlavor)) {
//           if (o instanceof File) {
//             File file = (File) o;
//             // model.addRow(new Object[] {file, file.length(), file.getAbsolutePath()});
//             model.addPath(file.toPath());
//           }
//         }
//         return true;
//       }
//     } catch (Exception ex) {
//       ex.printStackTrace();
//     }
//     return false;
//   }
//   @Override public boolean canImport(JComponent component, DataFlavor[] flavors) {
//     for (DataFlavor f: flavors) {
//       if (DataFlavor.javaFileListFlavor.equals(f)) {
//         return true;
//       }
//     }
//     return false;
//   }
//   @Override public int getSourceActions(JComponent component) {
//     return TransferHandler.COPY;
//   }
// }

class FileModel extends DefaultTableModel {
  private static final ColumnContext[] COLUMN_ARRAY = {
    new ColumnContext("No.", Integer.class, false),
    new ColumnContext("Name", String.class, true),
    new ColumnContext("Full Path", String.class, true)
  };
  private int number;

  public void addPath(Path path) {
    Object[] obj = {number, path.getFileName(), path.toAbsolutePath()};
    super.addRow(obj);
    number++;
  }

  @Override public boolean isCellEditable(int row, int col) {
    return COLUMN_ARRAY[col].isEditable;
  }

  @Override public Class<?> getColumnClass(int column) {
    return COLUMN_ARRAY[column].columnClass;
  }

  @Override public int getColumnCount() {
    return COLUMN_ARRAY.length;
  }

  @Override public String getColumnName(int column) {
    return COLUMN_ARRAY[column].columnName;
  }

  private static class ColumnContext {
    public final String columnName;
    public final Class<?> columnClass;
    public final boolean isEditable;

    protected ColumnContext(String columnName, Class<?> columnClass, boolean isEditable) {
      this.columnName = columnName;
      this.columnClass = columnClass;
      this.isEditable = isEditable;
    }
  }
}

class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  protected TablePopupMenu() {
    super();
    delete = add("Remove only from JTable");
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
