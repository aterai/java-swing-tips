// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

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
    table.setAutoCreateRowSorter(true);

    // TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model) {
    //   @Override public void toggleSortOrder(int column) {
    //     super.toggleSortOrder(column);
    //     if (check2.isSelected()) {
    //       table.clearSelection();
    //     }
    //   }
    // };
    // table.setRowSorter(sorter);
    // table.setUpdateSelectionOnSort(false);

    JCheckBox check1 = new JCheckBox("UpdateSelectionOnSort", true);
    check1.addActionListener(e -> table.setUpdateSelectionOnSort(((JCheckBox) e.getSource()).isSelected()));

    JCheckBox check2 = new JCheckBox("ClearSelectionOnSort", false);
    table.getTableHeader().addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        if (!check2.isSelected()) {
          return;
        }
        if (table.isEditing()) {
          table.getCellEditor().stopCellEditing();
        }
        table.clearSelection();
      }
    });

    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p.add(check1);
    p.add(check2);

    add(p, BorderLayout.NORTH);
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
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
