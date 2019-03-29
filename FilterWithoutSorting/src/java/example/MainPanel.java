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

    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
      {"AAA", 0, true}, {"BBB", 1, false},
      {"CCC", 2, true}, {"DDD", 3, true},
      {"EEE", 4, true}, {"FFF", 5, false},
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model);
    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model) {
      @Override public boolean isSortable(int column) {
        return false;
      }
    };
    sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
      @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
        return entry.getIdentifier() % 2 == 0;
      }
    });

    JCheckBox check = new JCheckBox("filter: idx%2==0");
    check.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      table.setRowSorter(c.isSelected() ? sorter : null);
    });

    add(check, BorderLayout.NORTH);
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
