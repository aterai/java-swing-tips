// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JRadioButton r1 = new JRadioButton("Default: ASCENDING<->DESCENDING", false);
    JRadioButton r2 = new JRadioButton("ASCENDING->DESCENDING->UNSORTED", true);
    ButtonGroup bg = new ButtonGroup();
    bg.add(r1);
    bg.add(r2);
    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(r1);
    p.add(r2);

    TableModel model = makeModel();
    TableRowSorter<TableModel> sorter = makeSorter(model, r2);
    JTable table = new JTable(model);
    table.setRowSorter(sorter);
    // sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(1, SortOrder.DESCENDING)));
    // sorter.toggleSortOrder(1);

    add(p, BorderLayout.NORTH);
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

  private static TableRowSorter<TableModel> makeSorter(TableModel m, AbstractButton b) {
    return new TableRowSorter<TableModel>(m) {
      @Override public void toggleSortOrder(int column) {
        if (b.isSelected() && isSortable(column) && isDescending(column)) {
          setSortKeys(Collections.emptyList());
          // or: setSortKeys(null);
        } else {
          super.toggleSortOrder(column);
        }
      }

      private boolean isDescending(int column) {
        return Optional.of(getSortKeys())
            .filter(keys -> !keys.isEmpty())
            .map(keys -> keys.get(0))
            .map(k -> k.getColumn() == column && k.getSortOrder() == SortOrder.DESCENDING)
            .orElse(false);
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
