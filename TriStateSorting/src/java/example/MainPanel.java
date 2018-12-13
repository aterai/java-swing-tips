// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class MainPanel extends JPanel {
  protected final JRadioButton check1 = new JRadioButton("Default: ASCENDING<->DESCENDING", false);
  protected final JRadioButton check2 = new JRadioButton("ASCENDING->DESCENDING->UNSORTED", true);
  protected final String[] columnNames = {"String", "Integer", "Boolean"};
  protected final Object[][] data = {
    {"aaa", 12, true}, {"bbb", 5, false},
    {"CCC", 92, true}, {"DDD", 0, false}
  };
  protected final TableModel model = new DefaultTableModel(data, columnNames) {
    @Override public Class<?> getColumnClass(int column) {
      return getValueAt(0, column).getClass();
    }
  };
  protected final JTable table = new JTable(model);

  public MainPanel() {
    super(new BorderLayout());

    ButtonGroup bg = new ButtonGroup();
    bg.add(check1);
    bg.add(check2);
    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(check1);
    p.add(check2);

    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model) {
      @Override public void toggleSortOrder(int column) {
        if (!check2.isSelected() || !isSortable(column)) {
          super.toggleSortOrder(column);
          return;
        }
        List<SortKey> keys = new ArrayList<>(getSortKeys());
        if (!keys.isEmpty()) {
          SortKey sortKey = keys.get(0);
          if (sortKey.getColumn() == column && sortKey.getSortOrder() == SortOrder.DESCENDING) {
            setSortKeys(null);
            return;
          }
        }
        super.toggleSortOrder(column);
      }
    };
    table.setRowSorter(sorter);
    // sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(1, SortOrder.DESCENDING)));
    // sorter.toggleSortOrder(1);

    TableColumn col = table.getColumnModel().getColumn(0);
    col.setMinWidth(60);
    col.setMaxWidth(60);
    col.setResizable(false);

    add(p, BorderLayout.NORTH);
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
