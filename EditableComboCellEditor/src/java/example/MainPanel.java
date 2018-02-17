package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String[] columnNames = {"Column1", "Column2"};
        Object[][] data = {
            {"colors", makeModel("blue", "violet", "red", "yellow")},
            {"sports", makeModel("basketball", "soccer", "football", "hockey")},
            {"food", makeModel("hot dogs", "pizza", "ravioli", "bananas")},
        };
        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return column == 1 ? DefaultComboBoxModel.class : String.class;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);

        TableColumn col = table.getColumnModel().getColumn(1);
        col.setCellRenderer(new ComboCellRenderer());
        col.setCellEditor(new ComboCellEditor());

        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    private static DefaultComboBoxModel<String> makeModel(String... items) {
        return new DefaultComboBoxModel<String>(items) {
            @Override public String toString() {
                return Objects.toString(getSelectedItem(), "");
            }
        };
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

class ComboCellRenderer implements TableCellRenderer {
    private final JComboBox<String> combo = new JComboBox<>();
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        combo.removeAllItems();
        if (value instanceof DefaultComboBoxModel) {
            DefaultComboBoxModel m = (DefaultComboBoxModel) value;
            combo.addItem((String) m.getSelectedItem());
        }
        return combo;
    }
}

class ComboCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JComboBox<String> combo = new JComboBox<>();
    protected ComboCellEditor() {
        super();
        combo.setEditable(true);
        combo.addActionListener(e -> fireEditingStopped());
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // combo.setBackground(table.getSelectionBackground());
        if (value instanceof ComboBoxModel) {
            @SuppressWarnings("unchecked")
            ComboBoxModel<String> m = (ComboBoxModel<String>) value;
            combo.setModel(m);
        }
        return combo;
    }
    @Override public Object getCellEditorValue() {
        @SuppressWarnings("unchecked")
        DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) combo.getModel();
        if (combo.isEditable()) {
            String str = Objects.toString(combo.getEditor().getItem(), "");
            if (!str.isEmpty() && m.getIndexOf(str) < 0) {
                m.insertElementAt(str, 0);
                combo.setSelectedIndex(0);
            }
        }
        return m;
    }
}
