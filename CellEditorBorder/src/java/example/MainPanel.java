package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String[] columnNames = {"String", "Integer", "Boolean"};
        Object[][] data = {
            {"aaa", 12, true}, {"bbb", 5, false},
            {"CCC", 92, true}, {"DDD", 0, false}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                // ArrayIndexOutOfBoundsException: 0 >= 0
                // [JDK-6967479] JTable sorter fires even if the model is empty - Java Bug System
                // https://bugs.openjdk.java.net/browse/JDK-6967479
                // return getValueAt(0, column).getClass();
                switch (column) {
                    case 0: return String.class;
                    case 1: return Integer.class;
                    case 2: return Boolean.class;
                    default: return super.getColumnClass(column);
                }
            }
        };
        JTable table = new JTable(model) {
            @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
                Component c = super.prepareEditor(editor, row, column);
                if (c instanceof JCheckBox) {
                    JCheckBox b = (JCheckBox) c;
                    b.setBorderPainted(true);
                    b.setBackground(getSelectionBackground());
                // } else if (c instanceof JComponent && convertColumnIndexToModel(column) == 1) {
                } else if (c instanceof JComponent && Number.class.isAssignableFrom(getColumnClass(column))) {
                    ((JComponent) c).setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
                }
                return c;
            }
        };

        JTextField field = new JTextField();
        field.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        table.setDefaultEditor(Object.class, new DefaultCellEditor(field));

        // JTextField tf2 = new JTextField();
        // tf2.setHorizontalAlignment(SwingConstants.RIGHT);
        // tf2.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
        // table.setDefaultEditor(Integer.class, new DefaultCellEditor(tf2) {
        //     @Override public boolean stopCellEditing() {
        //         return Objects.nonNull(getCellEditorValue()) && super.stopCellEditing();
        //     }
        //     @Override public Object getCellEditorValue() {
        //         Object o = super.getCellEditorValue();
        //         Integer iv;
        //         try {
        //             iv = Integer.valueOf(o.toString());
        //         } catch (NumberFormatException ex) {
        //             iv = null;
        //         }
        //         return iv;
        //     }
        // });

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

class TablePopupMenu extends JPopupMenu {
    private final JMenuItem delete;
    protected TablePopupMenu() {
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
