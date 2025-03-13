// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    // RowSorter<? extends TableModel> defSorter = table.getRowSorter();
    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel()) {
      @Override public boolean isSortable(int column) {
        return false;
      }
    };
    table.setRowSorter(sorter);

    RowFilter<? super TableModel, ? super Integer> defFilter = sorter.getRowFilter();
    RowFilter<TableModel, Integer> filter = new RowFilter<TableModel, Integer>() {
      @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
        return entry.getIdentifier() % 2 == 0;
      }
    };

    JCheckBox check = new JCheckBox("filter: idx%2==0");
    check.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      // table.setRowSorter(c.isSelected() ? sorter : defSorter);
      sorter.setRowFilter(c.isSelected() ? filter : defFilter);
    });

    add(check, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"AAA", 0, true}, {"BBB", 1, false},
        {"CCC", 2, true}, {"DDD", 3, true},
        {"EEE", 4, true}, {"FFF", 5, false},
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
