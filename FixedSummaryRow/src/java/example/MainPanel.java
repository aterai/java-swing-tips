// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    String[] columnNames = {"aaa", "bbb"};
    Object[][] data = {
      {Integer.MIN_VALUE, Integer.MIN_VALUE},
      {1, 1}, {1, 2}, {1, -1}, {1, 3}, {1, 0},
      {1, 5}, {1, 4}, {1, -5}, {1, 0}, {1, 6},
      {Integer.MAX_VALUE, Integer.MAX_VALUE}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return Integer.class; // getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int column) {
        return row > 0 && row != getRowCount() - 1;
      }
    };
    JTable table = new JTable(model);
    RowFilter<TableModel, Integer> filter = new RowFilter<TableModel, Integer>() {
      @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
        return 0 != table.convertRowIndexToView(entry.getIdentifier());
      }
    };
    TableRowSorter<TableModel> s = new TableRowSorter<TableModel>(model) {
      @Override public void toggleSortOrder(int column) {
        RowFilter<? super TableModel, ? super Integer> f = getRowFilter();
        setRowFilter(null);
        super.toggleSortOrder(column);
        setRowFilter(f);
      }
    };
    s.setRowFilter(filter);
    // s.setSortsOnUpdates(true);
    s.toggleSortOrder(1);
    table.setRowSorter(s);

    model.addTableModelListener(e -> {
      if (e.getType() == TableModelEvent.UPDATE) {
        table.repaint();
      }
    });
    TableCellRenderer renderer = new DefaultTableCellRenderer() {
      @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel l;
        TableModel m = table.getModel();
        if (row == m.getRowCount() - 2) {
          // int total = getSum(model, table.convertColumnIndexToModel(column));
          int total = IntStream.range(1, m.getRowCount() - 1).map(i -> (Integer) m.getValueAt(i, column)).sum();
          l = (JLabel) super.getTableCellRendererComponent(table, total, isSelected, hasFocus, row, column);
          l.setBackground(Color.ORANGE);
        } else {
          l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
          l.setBackground(Color.WHITE);
        }
        l.setForeground(Color.BLACK);
        return l;
      }
    };
    TableColumnModel cm = table.getColumnModel();
    for (int i = 0; i < cm.getColumnCount(); i++) {
      cm.getColumn(i).setCellRenderer(renderer);
    }

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }
  // protected static int getSum(DefaultTableModel model, int column) {
  //   int counter = 0;
  //   for (int i = 1; i < model.getRowCount() - 1; i++) {
  //     counter += (Integer) model.getValueAt(i, column);
  //   }
  //   return counter;
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
