package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final JRadioButton check1 = new JRadioButton("Default", true);
    private final JRadioButton check2 = new JRadioButton("Directory < File", false);
    private final JRadioButton check3 = new JRadioButton("Group Sorting", false);
    private final String[] columnNames = {"Name", "Size", "Full Path"};
    private final DefaultTableModel model = new DefaultTableModel(null, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return File.class;
//             switch (column) {
//               case 0:  return File.class;
//               case 1:  return Long.class;
//               case 2:  return String.class;
//               default: return Object.class;
//             }
        }
    };
    //private final FileTableModel model = new FileTableModel();
    private final JTable table = new JTable(model);
    private final ButtonGroup bg = new ButtonGroup();
    private final JPanel p = new JPanel();

    public MainPanel() {
        super(new BorderLayout());

        final ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                RowSorter<? extends TableModel> rs = table.getRowSorter();
                if (rs instanceof TableRowSorter) {
                    TableRowSorter<? extends TableModel> sorter = (TableRowSorter<? extends TableModel>) rs;
                    Object source = e.getSource();
                    if (source.equals(check2)) {
                        sorter.setComparator(0, new FileComparator(0));
                        sorter.setComparator(1, new FileComparator(1));
                        sorter.setComparator(2, new FileComparator(2));
                    } else if (source.equals(check3)) {
                        sorter.setComparator(0, new FileGroupComparator(table, 0));
                        sorter.setComparator(1, new FileGroupComparator(table, 1));
                        sorter.setComparator(2, new FileGroupComparator(table, 2));
                    } else {
                        sorter.setComparator(0, new DefaultFileComparator(0));
                        sorter.setComparator(1, new DefaultFileComparator(1));
                        sorter.setComparator(2, new DefaultFileComparator(2));
                    }
                }
            }
        };
        for (JRadioButton rb: Arrays.asList(check1, check2, check3)) {
            rb.addActionListener(al);
            bg.add(rb);
            p.add(rb);
        }
        table.putClientProperty("Table.isFileList", Boolean.TRUE);
        table.setCellSelectionEnabled(true);
        table.setIntercellSpacing(new Dimension());
        table.setComponentPopupMenu(new TablePopupMenu());
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        RowSorter<? extends TableModel> rs = table.getRowSorter();
        if (rs instanceof TableRowSorter) {
            TableRowSorter<? extends TableModel> sorter = (TableRowSorter<? extends TableModel>) rs;
            sorter.setComparator(0, new DefaultFileComparator(0));
            sorter.setComparator(1, new DefaultFileComparator(1));
            sorter.setComparator(2, new DefaultFileComparator(2));
        }
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        table.setDefaultRenderer(Object.class, new FileIconTableCellRenderer(fileSystemView));

        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new FileTransferHandler());

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    class DeleteAction extends AbstractAction {
        public DeleteAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            int[] selection = table.getSelectedRows();
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    }
    private class TablePopupMenu extends JPopupMenu {
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            deleteAction.setEnabled(table.getSelectedRowCount() > 0);
            super.show(c, x, y);
        }
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class FileIconTableCellRenderer extends DefaultTableCellRenderer {
    private final FileSystemView fileSystemView;
    public FileIconTableCellRenderer(FileSystemView fileSystemView) {
        super();
        this.fileSystemView = fileSystemView;
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        l.setIcon(null);
        File file = (File) value;
        int c = table.convertColumnIndexToModel(column);
        switch (c) {
          case 0:
            //???: WindowsLnF, Java 1.7.0
            //if (file.isDirectory()) {
            //    l.setIcon(UIManager.getIcon("FileView.directoryIcon"));
            //} else {
            //    l.setIcon(UIManager.getIcon("FileView.fileIcon"));
            //}
            l.setIcon(fileSystemView.getSystemIcon(file));
            l.setText(fileSystemView.getSystemDisplayName(file));
            //l.setText(file.getName());
            break;
          case 1:
            l.setHorizontalAlignment(SwingConstants.RIGHT);
            l.setText(file.isDirectory() ? null : Long.toString(file.length()));
            break;
          case 2:
            l.setText(file.getAbsolutePath());
            break;
          default:
            break;
        }
        return l;
    }
}

class FileTransferHandler extends TransferHandler {
    @Override public boolean importData(TransferSupport support) {
        try {
            if (canImport(support)) {
                //FileTableModel model = (FileTableModel)((JTable) support.getComponent()).getModel();
                //List<?> list = (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                //model.setFiles((File[]) list.toArray(new File[list.size()]));
                DefaultTableModel model = (DefaultTableModel) ((JTable) support.getComponent()).getModel();
                for (Object o: (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
                    if (o instanceof File) {
                        File file = (File) o;
                        //model.addRow(new Object[] {file, file.length(), file.getAbsolutePath()});
                        model.addRow(Collections.nCopies(3, file).toArray());
                    }
                }
                return true;
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    @Override public boolean canImport(TransferSupport support) {
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }
//     @Override public boolean importData(JComponent component, Transferable transferable) {
//         try {
//             if (canImport(component, transferable.getTransferDataFlavors())) {
//                 DefaultTableModel model = (DefaultTableModel)((JTable) component).getModel();
//                 for (Object o: (List) transferable.getTransferData(DataFlavor.javaFileListFlavor)) {
//                     if (o instanceof File) {
//                         File file = (File) o;
//                         model.addRow(new Object[] {file, file, file});
//                     }
//                 }
//                 return true;
//             }
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return false;
//     }
//     @Override public boolean canImport(JComponent component, DataFlavor... flavors) {
//         for (DataFlavor f: flavors) {
//             if (DataFlavor.javaFileListFlavor.equals(f)) {
//                 return true;
//             }
//         }
//         return false;
//     }
    @Override public int getSourceActions(JComponent component) {
        return COPY;
    }
}

class DefaultFileComparator implements Comparator<File>, Serializable {
    private static final long serialVersionUID = 1L;
    protected final int column;
    public DefaultFileComparator(int column) {
        this.column = column;
    }
    @Override public int compare(File a, File b) {
        switch (column) {
          case 0:  return a.getName().compareToIgnoreCase(b.getName());
          case 1:  return (int) (a.length() - b.length());
          default: return a.getAbsolutePath().compareToIgnoreCase(b.getAbsolutePath());
        }
    }
}

class FileComparator extends DefaultFileComparator {
    private static final long serialVersionUID = 1L;
    public FileComparator(int column) {
        super(column);
    }
    @Override public int compare(File a, File b) {
        if (a.isDirectory() && !b.isDirectory()) {
            return -1;
        } else if (!a.isDirectory() && b.isDirectory()) {
            return 1;
        } else {
            return super.compare(a, b);
        }
    }
}

// > dir /O:GN
// > ls --group-directories-first
class FileGroupComparator extends DefaultFileComparator {
    private static final long serialVersionUID = 1L;
    private final JTable table;
    public FileGroupComparator(JTable table, int column) {
        super(column);
        this.table = table;
    }
    @Override public int compare(File a, File b) {
        int flag = 1;
        List<? extends TableRowSorter.SortKey> keys = table.getRowSorter().getSortKeys();
        if (!keys.isEmpty()) {
            TableRowSorter.SortKey sortKey = keys.get(0);
            if (sortKey.getColumn() == column && sortKey.getSortOrder() == SortOrder.DESCENDING) {
                flag = -1;
            }
        }
        if (a.isDirectory() && !b.isDirectory()) {
            return -1 * flag;
        } else if (!a.isDirectory() && b.isDirectory()) {
            return 1 * flag;
        } else {
            return super.compare(a, b);
        }
    }
}

// class FileTableModel extends AbstractTableModel {
//     private final String[] columnNames = {"Name", "Size", "Full Path"};
//     private File[] files;
//     public FileTableModel() {
//         this(new File[0]);
//     }
//     public FileTableModel(File[] files) {
//         this.files = files;
//     }
//     @Override public Object getValueAt(int row, int column) {
//         return files[row];
//     }
//     @Override public int getColumnCount() {
//         return columnNames.length;
//     }
//     @Override public Class<?> getColumnClass(int column) {
//         return File.class;
//     }
//     @Override public String getColumnName(int column) {
//         return columnNames[column];
//     }
//     @Override public int getRowCount() {
//         return files.length;
//     }
//     public File getFile(int row) {
//         return files[row];
//     }
//     public void setFiles(File[] files) {
//         this.files = files;
//         fireTableDataChanged();
//     }
//     //public void removeRow(int row) {
//     //    files.removeElementAt(row);
//     //    fireTableRowsDeleted(row, row);
//     //}
// }
