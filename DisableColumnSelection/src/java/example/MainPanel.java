package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
      {"aaa", 12, true}, {"bbb", 5, false},
      {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
        @Override public boolean isCellEditable(int row, int column) {
            return column != 0;
        }
    };
    protected static final int TARGET_COLIDX = 0;

    public MainPanel() {
        super(new BorderLayout());

        JTable table1 = new JTable(model) {
            @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                if (convertColumnIndexToModel(columnIndex) != TARGET_COLIDX) {
                    return;
                }
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                if (convertColumnIndexToModel(column) != TARGET_COLIDX) {
                    return renderer.getTableCellRendererComponent(this, getValueAt(row, column), false, false, row, column);
                }
                return super.prepareRenderer(renderer, row, column);
            }
        };

        JTable table2 = new JTable(model) {
            @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                if (convertColumnIndexToModel(columnIndex) != TARGET_COLIDX) {
                    return;
                }
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        };
        table2.setCellSelectionEnabled(true);
        table2.getColumnModel().setSelectionModel(new DefaultListSelectionModel() {
            @Override public boolean isSelectedIndex(int index) {
                return table2.convertColumnIndexToModel(index) == TARGET_COLIDX;
            }
        });

        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(new JScrollPane(table1));
        p.add(new JScrollPane(table2));
        add(p);
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
