// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JRadioButton check1 = new JRadioButton("Default", true);
    JRadioButton check2 = new JRadioButton("Directory < File", false);
    JRadioButton check3 = new JRadioButton("Group Sorting", false);

    JTable table = makeTable();
    RowSorter<? extends TableModel> sorter = table.getRowSorter();
    if (sorter instanceof TableRowSorter) {
      TableRowSorter<? extends TableModel> rs = (TableRowSorter<? extends TableModel>) sorter;
      IntStream.range(0, 3).forEach(i -> rs.setComparator(i, new DefaultFileComparator(i)));
      check1.addItemListener(e -> {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          IntStream.range(0, 3)
              .forEach(i -> rs.setComparator(i, new DefaultFileComparator(i)));
        }
      });
      check2.addItemListener(e -> {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          IntStream.range(0, 3)
              .forEach(i -> rs.setComparator(i, new FileComparator(i)));
        }
      });
      check3.addItemListener(e -> {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          IntStream.range(0, 3)
              .forEach(i -> rs.setComparator(i, new FileGroupComparator(table, i)));
        }
      });
    }

    JPanel p = new JPanel();
    ButtonGroup bg = new ButtonGroup();
    Stream.of(check1, check2, check3).forEach(rb -> {
      bg.add(rb);
      p.add(rb);
    });
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable makeTable() {
    String[] columnNames = {"Name", "Size", "Full Path"};
    TableModel model = new DefaultTableModel(columnNames, 0) {
      @Override public Class<?> getColumnClass(int column) {
        return File.class;
      }
    };
    return new JTable(model) {
      @Override public void updateUI() {
        super.updateUI();
        putClientProperty("Table.isFileList", Boolean.TRUE);
        setCellSelectionEnabled(true);
        setIntercellSpacing(new Dimension());
        setComponentPopupMenu(new TablePopupMenu());
        setShowGrid(false);
        setFillsViewportHeight(true);
        setAutoCreateRowSorter(true);
        setDropMode(DropMode.INSERT_ROWS);
        setTransferHandler(new FileTransferHandler());
        setDefaultRenderer(Object.class, new FileIconCellRenderer());
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

class FileIconCellRenderer extends DefaultTableCellRenderer {
  private final transient FileSystemView fileSystemView = FileSystemView.getFileSystemView();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel && value instanceof File) {
      JLabel l = (JLabel) c;
      l.setHorizontalAlignment(LEFT);
      l.setIcon(null);
      File file = (File) value;
      switch (table.convertColumnIndexToModel(column)) {
        case 0:
          // ???: WindowsLnF, Java 1.7.0
          // if (file.isDirectory()) {
          //   l.setIcon(UIManager.getIcon("FileView.directoryIcon"));
          // } else {
          //   l.setIcon(UIManager.getIcon("FileView.fileIcon"));
          // }
          l.setIcon(fileSystemView.getSystemIcon(file));
          l.setText(fileSystemView.getSystemDisplayName(file));
          // l.setText(file.getName());
          break;
        case 1:
          l.setHorizontalAlignment(RIGHT);
          l.setText(file.isDirectory() ? "" : Long.toString(file.length()));
          break;
        case 2:
          l.setText(file.getAbsolutePath());
          break;
        default:
          break;
      }
    }
    return c;
  }
}

class FileTransferHandler extends TransferHandler {
  @Override public int getSourceActions(JComponent component) {
    return COPY;
  }

  @Override public boolean canImport(TransferSupport support) {
    return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
  }

  @Override public boolean importData(TransferSupport support) {
    Transferable transferable = support.getTransferable();
    List<?> list = getFileList(transferable);
    JTable table = (JTable) support.getComponent();
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    list.stream()
        .filter(File.class::isInstance)
        .map(File.class::cast)
        .map(f -> Collections.nCopies(3, f).toArray())
        .forEach(model::addRow);
    return !list.isEmpty();
  }

  private static List<?> getFileList(Transferable transferable) {
    List<?> list;
    try {
      list = (List<?>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
    } catch (UnsupportedFlavorException | IOException ex) {
      list = Collections.emptyList();
    }
    return list;
  }
}

class DefaultFileComparator implements Comparator<File>, Serializable {
  private static final long serialVersionUID = 1L;
  private final int column;

  protected DefaultFileComparator(int column) {
    this.column = column;
  }

  @SuppressWarnings("PMD.OnlyOneReturn")
  @Override public int compare(File a, File b) {
    switch (column) {
      case 0: return a.getName().compareToIgnoreCase(b.getName());
      case 1: return Long.compare(a.length(), b.length());
      default: return a.getAbsolutePath().compareToIgnoreCase(b.getAbsolutePath());
    }
  }

  public int getColumn() {
    return column;
  }
}

class FileComparator extends DefaultFileComparator {
  private static final long serialVersionUID = 1L;

  protected FileComparator(int column) {
    super(column);
  }

  @Override public int compare(File a, File b) {
    // if (a.isDirectory() && !b.isDirectory()) {
    //   return -1;
    // } else if (!a.isDirectory() && b.isDirectory()) {
    //   return 1;
    // } else {
    //   return super.compare(a, b);
    // }
    int v = getWeight(a) - getWeight(b);
    return v == 0 ? super.compare(a, b) : v;
  }

  private static int getWeight(File file) {
    return file.isDirectory() ? 1 : 2;
  }
}

// > dir /O:GN
// > ls --group-directories-first
class FileGroupComparator extends DefaultFileComparator {
  private static final long serialVersionUID = 1L;
  private final JTable table;

  protected FileGroupComparator(JTable table, int column) {
    super(column);
    this.table = table;
  }

  @Override public int compare(File a, File b) {
    // int flag = getSortOrderDirection();
    // if (a.isDirectory() && !b.isDirectory()) {
    //   return -1 * flag;
    // } else if (!a.isDirectory() && b.isDirectory()) {
    //   return flag;
    // } else {
    //   return super.compare(a, b);
    // }
    int v = getWeight(a) - getWeight(b);
    return v == 0 ? super.compare(a, b) : v * getSortOrderDirection();
  }

  private static int getWeight(File file) {
    return file.isDirectory() ? 1 : 2;
  }

  private int getSortOrderDirection() {
    int dir = 1;
    List<? extends RowSorter.SortKey> keys = table.getRowSorter().getSortKeys();
    if (!keys.isEmpty()) {
      RowSorter.SortKey sortKey = keys.get(0);
      if (sortKey.getColumn() == getColumn() && sortKey.getSortOrder() == SortOrder.DESCENDING) {
        dir = -1;
      }
    }
    return dir;
  }
}

// class FileTableModel extends AbstractTableModel {
//   private final String[] columnNames = {"Name", "Size", "Full Path"};
//   private File[] files;
//
//   protected FileTableModel() {
//     this(new File[0]);
//   }
//
//   protected FileTableModel(File[] files) {
//     this.files = files;
//   }
//
//   @Override public Object getValueAt(int row, int column) {
//     return files[row];
//   }
//
//   @Override public int getColumnCount() {
//     return columnNames.length;
//   }
//
//   @Override public Class<?> getColumnClass(int column) {
//     return File.class;
//   }
//
//   @Override public String getColumnName(int column) {
//     return columnNames[column];
//   }
//
//   @Override public int getRowCount() {
//     return files.length;
//   }
//
//   public File getFile(int row) {
//     return files[row];
//   }
//
//   public void setFiles(File[] files) {
//     this.files = files;
//     fireTableDataChanged();
//   }
//
//   // public void removeRow(int row) {
//   //   files.removeElementAt(row);
//   //   fireTableRowsDeleted(row, row);
//   // }
// }

final class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  /* default */ TablePopupMenu() {
    super();
    delete = add("delete");
    delete.addActionListener(e -> deleteSelectedRows());
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTable) {
      delete.setEnabled(((JTable) c).getSelectedRowCount() > 0);
      super.show(c, x, y);
    }
  }

  private void deleteSelectedRows() {
    JTable table = (JTable) getInvoker();
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    int[] selection = table.getSelectedRows();
    for (int i = selection.length - 1; i >= 0; i--) {
      model.removeRow(table.convertRowIndexToModel(selection[i]));
    }
  }
}
