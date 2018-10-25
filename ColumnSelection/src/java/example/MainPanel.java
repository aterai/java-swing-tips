package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String[] columnNames = {"String", "Integer", "Boolean"};
        Object[][] data = {
            {"aaa", 12, true}, {"bbb", 5, false},
            {"CCC", 92, true}, {"DDD", 0, false}
        };
        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model) {
            private final Color evenColor = new Color(250, 250, 250);
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if (isCellSelected(row, column)) {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                } else {
                    c.setForeground(getForeground());
                    c.setBackground(row % 2 == 0 ? evenColor : getBackground());
                }
                return c;
            }
        };
        table.setCellSelectionEnabled(true);

        JCheckBox check = new JCheckBox("Header click: Select all cells in a column", true);

        JTableHeader header = table.getTableHeader();
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

        JButton button = new JButton("clear selection");
        button.addActionListener(e -> table.clearSelection());

        // table.getTableHeader().addMouseListener(new MouseAdapter() {
        //     @Override public void mousePressed(MouseEvent e) {
        //         JTable table = ((JTableHeader) e.getSource()).getTable();
        //         if (table.isEditing()) {
        //             table.getCellEditor().stopCellEditing();
        //         }
        //         if (check.isSelected()) {
        //             // table.getSelectionModel().clearSelection();
        //             // table.getSelectionModel().setAnchorSelectionIndex(-1);
        //             // table.getSelectionModel().setLeadSelectionIndex(-1);
        //             table.getColumnModel().getSelectionModel().setAnchorSelectionIndex(-1);
        //             table.getColumnModel().getSelectionModel().setLeadSelectionIndex(-1);
        //         }
        //     }
        // });

        add(check, BorderLayout.NORTH);
        add(new JScrollPane(table));
        add(button, BorderLayout.SOUTH);
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
