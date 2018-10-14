package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTable table = new JTable(new DefaultTableModel(8, 6)) {
            @Override protected TableColumnModel createDefaultColumnModel() {
                return new SortableTableColumnModel();
            }
        };
        table.setAutoCreateRowSorter(true);

        JButton b = new JButton("restore TableColumn order");
        b.addActionListener(e -> {
            TableColumnModel m = table.getColumnModel();
            // TEST: sortTableColumn(m);
            if (m instanceof SortableTableColumnModel) {
                ((SortableTableColumnModel) m).restoreColumnOrder();
            }
        });
        add(new JScrollPane(table));
        add(b, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    // // TEST: selection sort
    // public static void sortTableColumn(TableColumnModel model) {
    //     int n = model.getColumnCount();
    //     for (int i = 0; i < n - 1; i++) {
    //         TableColumn c = (TableColumn) model.getColumn(i);
    //         for (int j = i + 1; j < n; j++) {
    //             TableColumn p = (TableColumn) model.getColumn(j);
    //             if (c.getModelIndex() - p.getModelIndex() > 0) {
    //                 model.moveColumn(j, i);
    //                 i -= 1;
    //                 break;
    //             }
    //         }
    //     }
    // }
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

class SortableTableColumnModel extends DefaultTableColumnModel {
    // TEST: private static Comparator<TableColumn> tcc = (o1, o2) -> o1.getModelIndex() - o2.getModelIndex();
    public void restoreColumnOrder() {
        Collections.sort(tableColumns, Comparator.comparingInt(TableColumn::getModelIndex));
        fireColumnMoved(new TableColumnModelEvent(this, 0, tableColumns.size()));
    }
}
