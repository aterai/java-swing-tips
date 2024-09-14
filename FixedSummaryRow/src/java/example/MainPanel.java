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
    JTable table = new JTable(makeModel());
    TableRowSorter<TableModel> s = new TableRowSorter<TableModel>(table.getModel()) {
      @Override public void toggleSortOrder(int column) {
        RowFilter<? super TableModel, ? super Integer> f = getRowFilter();
        setRowFilter(null);
        super.toggleSortOrder(column);
        setRowFilter(f);
      }
    };
    s.setRowFilter(new RowFilter<TableModel, Integer>() {
      @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
        return 0 != table.convertRowIndexToView(entry.getIdentifier());
      }
    });
    // s.setSortsOnUpdates(true);
    s.toggleSortOrder(1);
    table.setRowSorter(s);
    table.getModel().addTableModelListener(e -> {
      if (e.getType() == TableModelEvent.UPDATE) {
        table.repaint();
      }
    });
    TableCellRenderer renderer = new SummaryRowRenderer();
    TableColumnModel cm = table.getColumnModel();
    for (int i = 0; i < cm.getColumnCount(); i++) {
      cm.getColumn(i).setCellRenderer(renderer);
    }
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"A", "B"};
    Object[][] data = {
        {Integer.MIN_VALUE, Integer.MIN_VALUE},
        {1, 1}, {1, 2}, {1, -1}, {1, 3}, {1, 0},
        {1, 5}, {1, 4}, {1, -5}, {1, 0}, {1, 6},
        {Integer.MAX_VALUE, Integer.MAX_VALUE}
    };
    // getValueAt(0, column).getClass();
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return Integer.class; // getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int column) {
        return row > 0 && row != getRowCount() - 1;
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

class SummaryRowRenderer extends DefaultTableCellRenderer {
  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c;
    TableModel m = table.getModel();
    int rc = m.getRowCount();
    if (row == rc - 2) {
      int sum = IntStream.range(1, rc - 1)
          .map(i -> (Integer) m.getValueAt(i, column))
          .sum();
      c = super.getTableCellRendererComponent(
          table, sum, isSelected, hasFocus, row, column);
      c.setBackground(Color.ORANGE);
    } else {
      c = super.getTableCellRendererComponent(
          table, value, isSelected, hasFocus, row, column);
      c.setBackground(Color.WHITE);
    }
    c.setForeground(Color.BLACK);
    return c;
  }
}
