// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1));

    String[] columnNames = {"#", "String", "Integer"};
    Object[][] data = {
      {false, "aaa", 12}, {false, "bbb", 5},
      {false, "CCC", 92}, {false, "DDD", 0}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable selector = new JTable(model);
    selector.setAutoCreateRowSorter(true);
    selector.getColumnModel().getColumn(0).setMaxWidth(32);

    JTable viewer = new JTable(model) {
      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    viewer.setAutoCreateRowSorter(true);
    TableColumnModel cm = viewer.getColumnModel();
    cm.removeColumn(cm.getColumn(0));

    TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
    viewer.setRowSorter(sorter);
    sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
      @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
        return Objects.equals(entry.getModel().getValueAt(entry.getIdentifier(), 0), Boolean.TRUE);
      }
    });
    model.addTableModelListener(e -> {
      if (e.getType() == TableModelEvent.UPDATE) {
        sorter.allRowsChanged();
        // sorter.modelStructureChanged();
      }
    });

    add(new JScrollPane(selector));
    add(new JScrollPane(viewer));
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
