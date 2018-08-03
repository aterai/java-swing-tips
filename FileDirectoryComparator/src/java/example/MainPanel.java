package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String[] columnNames = {"Name", "Size", "Full Path"};
        DefaultTableModel model = new DefaultTableModel(null, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return File.class;
                // switch (column) {
                //   case 0: return File.class;
                //   case 1: return Long.class;
                //   case 2: return String.class;
                //   default: return Object.class;
                // }
            }
        };
        JTable table = new JTable(model);
        table.putClientProperty("Table.isFileList", Boolean.TRUE);
        table.setCellSelectionEnabled(true);
        table.setIntercellSpacing(new Dimension());
        table.setComponentPopupMenu(new TablePopupMenu());
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new FileTransferHandler());
        table.setDefaultRenderer(Object.class, new FileIconTableCellRenderer(FileSystemView.getFileSystemView()));

        TableRowSorter<? extends TableModel> sorter = (TableRowSorter<? extends TableModel>) table.getRowSorter();
        // RowSorter<? extends TableModel> rs = table.getRowSorter();
        // if (rs instanceof TableRowSorter) {
        //     TableRowSorter<? extends TableModel> sorter = (TableRowSorter<? extends TableModel>) rs;
        IntStream.range(0, 3).forEach(i -> sorter.setComparator(i, new DefaultFileComparator(i)));

        JRadioButton check1 = new JRadioButton("Default", true);
        check1.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                IntStream.range(0, 3).forEach(i -> sorter.setComparator(i, new DefaultFileComparator(i)));
            }
        });
        JRadioButton check2 = new JRadioButton("Directory < File", false);
        check2.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                IntStream.range(0, 3).forEach(i -> sorter.setComparator(i, new FileComparator(i)));
            }
        });
        JRadioButton check3 = new JRadioButton("Group Sorting", false);
        check3.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                IntStream.range(0, 3).forEach(i -> sorter.setComparator(i, new FileGroupComparator(table, i)));
            }
        });

        JPanel p = new JPanel();
        ButtonGroup bg = new ButtonGroup();
        for (JRadioButton rb: Arrays.asList(check1, check2, check3)) {
            bg.add(rb);
            p.add(rb);
        }
        add(p, BorderLayout.NORTH);
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
    protected FileIconTableCellRenderer(FileSystemView fileSystemView) {
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
                // ???: WindowsLnF, Java 1.7.0
                // if (file.isDirectory()) {
                //     l.setIcon(UIManager.getIcon("FileView.directoryIcon"));
                // } else {
                //     l.setIcon(UIManager.getIcon("FileView.fileIcon"));
                // }
                l.setIcon(fileSystemView.getSystemIcon(file));
                l.setText(fileSystemView.getSystemDisplayName(file));
                // l.setText(file.getName());
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
    @Override public boolean importData(TransferHandler.TransferSupport support) {
        try {
            if (canImport(support)) {
                // FileTableModel model = (FileTableModel) ((JTable) support.getComponent()).getModel();
                // List<?> list = (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                // model.setFiles((File[]) list.toArray(new File[list.size()]));
                DefaultTableModel model = (DefaultTableModel) ((JTable) support.getComponent()).getModel();
                for (Object o: (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
                    if (o instanceof File) {
                        File file = (File) o;
                        // model.addRow(new Object[] {file, file.length(), file.getAbsolutePath()});
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
    @Override public boolean canImport(TransferHandler.TransferSupport support) {
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }
    // @Override public boolean importData(JComponent component, Transferable transferable) {
    //     try {
    //         if (canImport(component, transferable.getTransferDataFlavors())) {
    //             DefaultTableModel model = (DefaultTableModel) ((JTable) component).getModel();
    //             for (Object o: (List) transferable.getTransferData(DataFlavor.javaFileListFlavor)) {
    //                 if (o instanceof File) {
    //                     File file = (File) o;
    //                     model.addRow(new Object[] {file, file, file});
    //                 }
    //             }
    //             return true;
    //         }
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //     }
    //     return false;
    // }
    // @Override public boolean canImport(JComponent component, DataFlavor[] flavors) {
    //     for (DataFlavor f: flavors) {
    //         if (DataFlavor.javaFileListFlavor.equals(f)) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }
    @Override public int getSourceActions(JComponent component) {
        return TransferHandler.COPY;
    }
}

class DefaultFileComparator implements Comparator<File>, Serializable {
    private static final long serialVersionUID = 1L;
    protected final int column;
    protected DefaultFileComparator(int column) {
        this.column = column;
    }
    @Override public int compare(File a, File b) {
        switch (column) {
            case 0: return a.getName().compareToIgnoreCase(b.getName());
            case 1: return Long.compare(a.length(), b.length());
            default: return a.getAbsolutePath().compareToIgnoreCase(b.getAbsolutePath());
        }
    }
}

class FileComparator extends DefaultFileComparator {
    private static final long serialVersionUID = 1L;
    protected FileComparator(int column) {
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
    protected FileGroupComparator(JTable table, int column) {
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
//     protected FileTableModel() {
//         this(new File[0]);
//     }
//     protected FileTableModel(File[] files) {
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
//     // public void removeRow(int row) {
//     //     files.removeElementAt(row);
//     //     fireTableRowsDeleted(row, row);
//     // }
// }

class TablePopupMenu extends JPopupMenu {
    private final JMenuItem delete;
    protected TablePopupMenu() {
        super();
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
