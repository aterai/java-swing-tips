// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private final SpinnerNumberModel model = new SpinnerNumberModel(10, 0, 100, 5);

  private MainPanel() {
    super(new BorderLayout());
    TableModel m = makeModel();
    JTable table = new JTable(m);
    TableRowSorter<? extends TableModel> sorter = new TableRowSorter<>(m);
    table.setRowSorter(sorter);

    JComboBox<ComparisonType> combo = new JComboBox<>(ComparisonType.values());
    combo.setEnabled(false);

    JCheckBox check = new JCheckBox("setRowFilter");
    check.addActionListener(e -> {
      if (((JCheckBox) e.getSource()).isSelected()) {
        setFilter(sorter, getComparisonType(combo));
        combo.setEnabled(true);
      } else {
        sorter.setRowFilter(null);
        combo.setEnabled(false);
      }
    });
    combo.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        Object o = e.getItem();
        if (o instanceof ComparisonType && check.isSelected()) {
          setFilter(sorter, (ComparisonType) o);
        }
      }
    });
    model.addChangeListener(e -> {
      if (check.isSelected()) {
        setFilter(sorter, getComparisonType(combo));
      }
    });

    JPanel p = new JPanel();
    p.add(check);
    p.add(new JSpinner(model));
    p.add(combo);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private void setFilter(TableRowSorter<?> sorter, ComparisonType type) {
    int num = model.getNumber().intValue();
    sorter.setRowFilter(RowFilter.numberFilter(type, num));
    // if (type == ComparisonType.AFTER || type == ComparisonType.BEFORE) {
    //   RowFilter<TableModel, Integer> f1 = RowFilter.numberFilter(type, num);
    //   ComparisonType eq = ComparisonType.EQUAL;
    //   RowFilter<TableModel, Integer> f2 = RowFilter.numberFilter(eq, num);
    //   sorter.setRowFilter(RowFilter.orFilter(Arrays.asList(f1, f2)));
    // } else {
    //   sorter.setRowFilter(RowFilter.numberFilter(type, num));
    // }
  }

  private static ComparisonType getComparisonType(JComboBox<ComparisonType> c) {
    return c.getItemAt(c.getSelectedIndex());
  }

  private static TableModel makeModel() {
    Random rnd = new Random();
    int min = 0;
    int max = 100;
    String[] columnNames = {String.format("Integer(%d..%d)", min, max)};
    DefaultTableModel model = new DefaultTableModel(columnNames, 5) {
      @Override public Class<?> getColumnClass(int column) {
        return Integer.class;
      }
    };
    IntStream.range(0, 50)
        .map(i -> min + rnd.nextInt(max - min + 1))
        .mapToObj(i -> new Object[] {i})
        .forEach(model::addRow);
    return model;
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
      Logger.getGlobal().severe(ex::getMessage);
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
