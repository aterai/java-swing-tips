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
    JTable table = new JTable(makeModel());
    // table.setAutoCreateRowSorter(true);
    table.setRowSorter(new TableRowSorter<>(table.getModel()));
    // TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
    // table.setRowSorter(sorter);
    // sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.DESCENDING)));

    TableCellRenderer renderer = (tbl, value, isSelected, hasFocus, row, column) -> {
      TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
      Component c = r.getTableCellRendererComponent(
          tbl, value, isSelected, hasFocus, row, column);
      RowSorter<? extends TableModel> rs = tbl.getRowSorter();
      if (rs instanceof DefaultRowSorter) {
        int cmi = tbl.convertColumnIndexToModel(column);
        c.setForeground(((DefaultRowSorter<?, ?>) rs).isSortable(cmi) ? Color.BLACK : Color.GRAY);
      }
      return c;
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

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
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
