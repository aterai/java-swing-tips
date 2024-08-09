// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        MultiSortHeaderRenderer r = new MultiSortHeaderRenderer();
        TableColumnModel cm = getColumnModel();
        for (int i = 0; i < cm.getColumnCount(); i++) {
          cm.getColumn(i).setHeaderRenderer(r);
        }
      }
    };
    table.setAutoCreateRowSorter(true);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"AAA", "BBB", "CCC", "DDD"};
    Object[][] data = {
        {"aaa", "1", "true", "cc"}, {"aaa", "1", "false", "dd"},
        {"aaa", "2", "true", "ee"}, {"ddd", "3", "false", "ff"}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return String.class;
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

class MultiSortHeaderRenderer implements TableCellRenderer {
  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    String str = Objects.toString(value, "");
    RowSorter<? extends TableModel> sorter = table.getRowSorter();
    if (Objects.nonNull(sorter)) {
      List<? extends RowSorter.SortKey> keys = sorter.getSortKeys();
      for (int i = 0; i < keys.size(); i++) {
        RowSorter.SortKey sortKey = keys.get(i);
        if (column == sortKey.getColumn()) {
          // BLACK TRIANGLE
          // String k = sortKey.getSortOrder() == SortOrder.ASCENDING ? "▲ " : "▼ ";
          // BLACK SMALL TRIANGLE
          String k = sortKey.getSortOrder() == SortOrder.ASCENDING ? "▴ " : "▾ ";
          str = String.format("<html>%s<small color='gray'>%s%d", str, k, i + 1);
        }
      }
    }
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    return r.getTableCellRendererComponent(table, str, isSelected, hasFocus, row, column);
  }
}
