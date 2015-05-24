package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;
import javax.activation.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final DataFlavor FLAVOR = new ActivationDataFlavor(JTable.class, DataFlavor.javaJVMLocalObjectMimeType, "JTable");
    private final String[] columnNames = {"String", "Icon", "Boolean"};
    private final Object[][] data = {
        {"aaa", new ColorIcon(Color.RED),    true},
        {"bbb", new ColorIcon(Color.GREEN),  false},
        {"ccc", new ColorIcon(Color.BLUE),   true},
        {"ddd", new ColorIcon(Color.ORANGE), true},
        {"eee", new ColorIcon(Color.PINK),   false},
        {"fff", new ColorIcon(Color.CYAN),   true},
    };
    public MainPanel() {
        super(new BorderLayout());
        JTable table = new JTable(new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                switch (column) {
                  case 0:
                    return String.class;
                  case 1:
                    return Icon.class;
                  case 2:
                    return Boolean.class;
                  default:
                    return super.getColumnClass(column);
                }
            }
        });
        table.setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setTransferHandler(new TableRowTransferHandler(FLAVOR));
        table.setDragEnabled(true);
        table.setFillsViewportHeight(true);
        final TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);

        //Disable row Cut, Copy, Paste
        ActionMap map = table.getActionMap();
        AbstractAction dummy = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { /* Dummy action */ }
        };
        map.put(TransferHandler.getCutAction().getValue(Action.NAME),   dummy);
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),  dummy);
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy);

        final DefaultListModel<Icon> model = new DefaultListModel<>();
        JList<Icon> list = new JList<>(model);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(0);
        list.setFixedCellWidth(16);
        list.setFixedCellHeight(16);
        list.setCellRenderer(new IconListCellRenderer<>());
        list.setTransferHandler(new TableCellTransferHandler(FLAVOR));

        Box box = Box.createHorizontalBox();
        box.add(new JButton(new AbstractAction("clear") {
            @Override public void actionPerformed(ActionEvent e) {
                model.clear();
                sorter.setRowFilter(null);
            }
        }));
        box.add(new JButton(new AbstractAction("filter") {
            @Override public void actionPerformed(ActionEvent e) {
                sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
                    @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                        Object o = entry.getModel().getValueAt(entry.getIdentifier(), 1);
                        System.out.println(model.contains(o));
                        return model.isEmpty() || model.contains(o);
                    }
                });
            }
        }));

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(new JScrollPane(list));
        p.add(box, BorderLayout.EAST);

        add(p, BorderLayout.SOUTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
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

class TableRowTransferHandler extends TransferHandler {
    private final DataFlavor localObjectFlavor;
    public TableRowTransferHandler(DataFlavor flavor) {
        super();
        localObjectFlavor = flavor;
    }
    @Override protected Transferable createTransferable(JComponent c) {
        JTable table = (JTable) c;
        if (table.getSelectedColumn() != 1) {
            return null;
        }
        return new DataHandler(table, localObjectFlavor.getMimeType());
    }
    @Override public boolean canImport(TransferSupport info) {
        return false;
    }
    @Override public int getSourceActions(JComponent c) {
        return COPY;
    }
}

class TableCellTransferHandler extends TransferHandler {
    private final DataFlavor localObjectFlavor;
    public TableCellTransferHandler(DataFlavor flavor) {
        super();
        localObjectFlavor = flavor;
    }
    @Override public boolean canImport(TransferSupport info) {
        Component c = info.getComponent();
        if (c instanceof JList) {
            return info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
        }
        return false;
    }
    @Override public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }
    @SuppressWarnings("unchecked")
    @Override public boolean importData(TransferSupport info) {
        if (!canImport(info)) {
            return false;
        }
        JList l = (JList) info.getComponent();
        try {
            Object o = info.getTransferable().getTransferData(localObjectFlavor);
            if (o instanceof JTable) {
                JTable t = (JTable) o;
                Object obj = t.getValueAt(t.getSelectedRow(), t.getSelectedColumn());
                ((DefaultListModel) l.getModel()).addElement(obj);
            }
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}

class IconListCellRenderer<E extends Icon> implements ListCellRenderer<E> {
    private final JLabel l = new JLabel();
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E item, int index, boolean isSelected, boolean cellHasFocus) {
        l.setIcon(item);
        return l;
    }
}

class ColorIcon implements Icon {
    private final Color color;
    public ColorIcon(Color color) {
        this.color = color;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        g.translate(x, y);
        g.setColor(color);
        g.fillRect(1, 1, 11, 11);
        g.translate(-x, -y);
    }
    @Override public int getIconWidth() {
        return 12;
    }
    @Override public int getIconHeight() {
        return 12;
    }
}
