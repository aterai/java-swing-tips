// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

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
    JTable table = new JTable(model);
    // table.setAutoCreateRowSorter(true);
    // TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
    table.setRowSorter(new TableRowSorter<>(model));
    // sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.DESCENDING)));

    TableCellRenderer renderer = new TableCellRenderer() {
      @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
        JLabel l = (JLabel) r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        RowSorter<? extends TableModel> rs = table.getRowSorter();
        if (rs instanceof DefaultRowSorter) {
          int cmi = table.convertColumnIndexToModel(column);
          l.setForeground(((DefaultRowSorter<?, ?>) rs).isSortable(cmi) ? Color.BLACK : Color.GRAY);
        }
        return l;
      }
    };
    TableColumnModel columns = table.getColumnModel();
    for (int i = 0; i < columns.getColumnCount(); i++) {
      TableColumn c = columns.getColumn(i);
      c.setHeaderRenderer(renderer);
      if (i == 0) {
        c.setMinWidth(60);
        c.setMaxWidth(60);
        c.setResizable(false);
      }
    }

    JCheckBox check = new JCheckBox("Sortable(1, false)");
    check.addActionListener(e -> {
      RowSorter<? extends TableModel> rs = table.getRowSorter();
      if (rs instanceof DefaultRowSorter) {
        JCheckBox cb = (JCheckBox) e.getSource();
        ((DefaultRowSorter<?, ?>) rs).setSortable(1, !cb.isSelected());
        table.getTableHeader().repaint();
      }
    });

    JButton button = new JButton("clear SortKeys");
    button.addActionListener(e -> table.getRowSorter().setSortKeys(null));

    add(check, BorderLayout.NORTH);
    add(button, BorderLayout.SOUTH);
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
