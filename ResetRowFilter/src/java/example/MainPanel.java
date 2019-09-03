// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  public static final int USER_SPECIFIED_NUMBER_OF_ROWS = 5;

  private MainPanel() {
    super(new BorderLayout());

    JCheckBox check1 = new JCheckBox("Custom Sorting");

    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
      {"AA",  1, true}, {"BB",  2, false}, {"cc",  3, true}, {"dd",  4, false}, {"ee",  5, false},
      {"FF", -1, true}, {"GG", -2, false}, {"HH", -3, true}, {"II", -4, false}, {"JJ", -5, false},
      {"KK", 11, true}, {"LL", 22, false}, {"MM", 33, true}, {"NN", 44, false}, {"OO", 55, false},
    };
    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model);
    table.setFillsViewportHeight(true);
    // XXX: sorter.setSortsOnUpdates(true);

    RowFilter<TableModel, Integer> filter = new RowFilter<TableModel, Integer>() {
      @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
        int vidx = table.convertRowIndexToView(entry.getIdentifier());
        return vidx < USER_SPECIFIED_NUMBER_OF_ROWS;
      }
    };
    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model) {
      @Override public void toggleSortOrder(int column) {
        super.toggleSortOrder(column);
        if (check1.isSelected()) {
          model.fireTableDataChanged();
          sort(); // allRowsChanged();
        }
        // if (check1.isSelected()) {
        //   RowFilter<? super TableModel, ? super Integer> f = getRowFilter();
        //   setRowFilter(null);
        //   super.toggleSortOrder(column);
        //   setRowFilter(f);
        // } else {
        //   super.toggleSortOrder(column);
        // }
      }
    };

    table.setRowSorter(sorter);
    sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(1, SortOrder.DESCENDING)));

    JCheckBox check2 = new JCheckBox("viewRowIndex < " + USER_SPECIFIED_NUMBER_OF_ROWS);
    check2.addActionListener(e -> sorter.setRowFilter(((JCheckBox) e.getSource()).isSelected() ? filter : null));

    Box box = Box.createHorizontalBox();
    box.add(check1);
    box.add(Box.createHorizontalStrut(5));
    box.add(check2);
    add(box, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
