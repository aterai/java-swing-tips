// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  public static final int MAXIMUM_ROW_COUNT = 5;

  private MainPanel() {
    super(new BorderLayout());
    JCheckBox check1 = new JCheckBox("Custom Sorting");
    JTable table = new JTable(makeModel());
    table.setFillsViewportHeight(true);
    // XXX: sorter.setSortsOnUpdates(true);

    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel()) {
      @Override public void toggleSortOrder(int column) {
        super.toggleSortOrder(column);
        TableModel m = getModel();
        if (check1.isSelected() && m instanceof DefaultTableModel) {
          ((DefaultTableModel) m).fireTableDataChanged();
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
    sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(1, SortOrder.DESCENDING)));

    RowFilter<TableModel, Integer> filter = new RowFilter<TableModel, Integer>() {
      @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
        int viewIndex = table.convertRowIndexToView(entry.getIdentifier());
        return viewIndex < MAXIMUM_ROW_COUNT;
      }
    };
    RowFilter<? super TableModel, ? super Integer> defFilter = sorter.getRowFilter();
    // System.out.println(defFilter); // -> null

    JCheckBox check2 = new JCheckBox("viewRowIndex < " + MAXIMUM_ROW_COUNT);
    check2.addActionListener(e -> sorter.setRowFilter(check2.isSelected() ? filter : defFilter));

    Box box = Box.createHorizontalBox();
    box.add(check1);
    box.add(Box.createHorizontalStrut(5));
    box.add(check2);
    add(box, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultTableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"AA", 1, true}, {"BB", 2, false}, {"cc", 3, true}, {"dd", 4, false}, {"ee", 5, false},
        {"FF", -1, true}, {"GG", -2, true}, {"HH", -3, true}, {"II", -4, true}, {"JJ", -5, true},
        {"KK", 6, true}, {"LL", 7, false}, {"MM", 8, true}, {"NN", 9, false}, {"OO", 0, false},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
