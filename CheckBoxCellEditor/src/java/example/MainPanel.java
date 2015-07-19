package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Boolean"};
    private final Object[][] data = {
        {"AAA", true}, {"bbb", false},
        {"CCC", true}, {"ddd", false},
        {"EEE", true}, {"fff", false},
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return column == 1 ? Boolean.class : super.getColumnClass(column);
        }
        @Override public boolean isCellEditable(int row, int column) {
            return column == 1;
        }
    };
    private final JTable table = new JTable(model) {
        @Override public void updateUI() {
            //setDefaultRenderer(Boolean.class, null);
            setDefaultEditor(Boolean.class, null);
            super.updateUI();
            //setDefaultRenderer(Boolean.class, new CheckBoxPanelRenderer());
            setDefaultEditor(Boolean.class, new CheckBoxPanelEditor());
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setRowHeight(24);
        table.setRowSelectionAllowed(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFocusable(false);
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

class CheckBoxPanelEditor extends AbstractCellEditor implements TableCellEditor {
    private final JPanel p = new JPanel(new GridBagLayout());
    private final JCheckBox checkBox = new JCheckBox();
    public CheckBoxPanelEditor() {
        super();
        checkBox.setOpaque(false);
        checkBox.setFocusable(false);
        checkBox.setRolloverEnabled(false);
        Handler handler = new Handler();
        checkBox.addActionListener(handler);
        checkBox.addMouseListener(handler);
        p.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                fireEditingStopped();
            }
        });
        p.add(checkBox);
        p.setBorder(UIManager.getBorder("Table.noFocusBorder"));
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        checkBox.setSelected(Objects.equals(value, Boolean.TRUE));
        //p.setBackground(table.getSelectionBackground());
        return p;
    }
    @Override public Object getCellEditorValue() {
        return checkBox.isSelected();
    }
    private class Handler extends MouseAdapter implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            fireEditingStopped();
        }
        @Override public void mousePressed(MouseEvent e) {
            Container c = SwingUtilities.getAncestorOfClass(JTable.class, e.getComponent());
            if (c instanceof JTable) {
                JTable table = (JTable) c;
                if (checkBox.getModel().isPressed() && table.isRowSelected(table.getEditingRow()) && e.isControlDown()) {
                    p.setBackground(table.getBackground());
                } else {
                    p.setBackground(table.getSelectionBackground());
                }
            }
        }
        @Override public void mouseExited(MouseEvent e) {
            Container c = SwingUtilities.getAncestorOfClass(JTable.class, e.getComponent());
            if (c instanceof JTable) {
                JTable table = (JTable) c;
                if (table.isEditing() && !table.getCellEditor().stopCellEditing()) {
                    table.getCellEditor().cancelCellEditing();
                }
            }
        }
    }
}
// class CheckBoxPanelEditor extends AbstractCellEditor implements TableCellEditor {
//     private final JPanel p = new JPanel(new GridBagLayout());
//     private final JCheckBox checkBox = new JCheckBox();
//     public CheckBoxPanelEditor() {
//         super();
//         checkBox.setOpaque(false);
//         checkBox.setFocusable(false);
//         checkBox.setRolloverEnabled(false);
//         checkBox.addActionListener(new ActionListener() {
//             @Override public void actionPerformed(ActionEvent e) {
//                 fireEditingStopped();
//             }
//         });
//         p.add(checkBox);
//         p.setBorder(UIManager.getBorder("Table.noFocusBorder"));
//     }
//     @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//         checkBox.setSelected(Objects.equals(value, Boolean.TRUE));
//         p.setBackground(table.getSelectionBackground());
//         return p;
//     }
//     @Override public Object getCellEditorValue() {
//         return checkBox.isSelected();
//     }
// }
// class CheckBoxPanelRenderer implements TableCellRenderer {
//     private final JPanel p = new JPanel(new GridBagLayout());
//     private final JCheckBox checkBox = new JCheckBox();
//     public CheckBoxPanelRenderer() {
//         checkBox.setOpaque(false);
//         checkBox.setFocusable(false);
//         checkBox.setRolloverEnabled(false);
//         p.add(checkBox);
//         p.setBorder(UIManager.getBorder("Table.noFocusBorder"));
//     }
//     @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//         checkBox.setSelected(Objects.equals(value, Boolean.TRUE));
//         p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
//         return p;
//     }
// }
