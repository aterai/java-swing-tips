package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final Color EVEN_COLOR = new Color(250, 250, 250);
    private final JCheckBox check = new JCheckBox("Header click: Select all cells in a column", true);
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table;
    public MainPanel() {
        super(new BorderLayout());
        table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if (isCellSelected(row, column)) {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                } else {
                    c.setForeground(getForeground());
                    c.setBackground((row % 2 == 0) ? EVEN_COLOR : getBackground());
                }
                return c;
            }
        };
        table.setCellSelectionEnabled(true);

        final JTableHeader header = table.getTableHeader();
        header.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (!check.isSelected()) {
                    return;
                }
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
                int col = header.columnAtPoint(e.getPoint());
                table.changeSelection(0, col, false, false);
                table.changeSelection(table.getRowCount() - 1, col, false, true);
            }
        });

//         table.getTableHeader().addMouseListener(new MouseAdapter() {
//             @Override public void mousePressed(MouseEvent e) {
//                 JTable table = ((JTableHeader) e.getSource()).getTable();
//                 if (table.isEditing()) {
//                     table.getCellEditor().stopCellEditing();
//                 }
//                 if (check.isSelected()) {
//                     //table.getSelectionModel().clearSelection();
//                     //table.getSelectionModel().setAnchorSelectionIndex(-1);
//                     //table.getSelectionModel().setLeadSelectionIndex(-1);
//                     table.getColumnModel().getSelectionModel().setAnchorSelectionIndex(-1);
//                     table.getColumnModel().getSelectionModel().setLeadSelectionIndex(-1);
//                 }
//             }
//         });

        add(check, BorderLayout.NORTH);
        add(new JScrollPane(table));
        add(new JButton(new AbstractAction("clear selection") {
            @Override public void actionPerformed(ActionEvent e) {
                table.clearSelection();
            }
        }), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
