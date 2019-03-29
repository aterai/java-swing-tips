// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
      {"aaa", 12, true}, {"bbb", 5, false},
      {"CCC", 92, true}, {"DDD", 0, false}
    };
    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        // ArrayIndexOutOfBoundsException: 0 >= 0
        // [JDK-6967479] JTable sorter fires even if the model is empty - Java Bug System
        // https://bugs.openjdk.java.net/browse/JDK-6967479
        // return getValueAt(0, column).getClass();
        switch (column) {
          case 0: return String.class;
          case 1: return Number.class;
          case 2: return Boolean.class;
          default: return super.getColumnClass(column);
        }
      }
    };
    JTable table = new JTable(model);
    // RowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
    // table.setRowSorter(sorter);
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());

    JButton button = new JButton("remove all rows");
    button.addActionListener(e -> {
      // if (DEBUG) {
      //   // ArrayIndexOutOfBoundsException: 0 >= 0
      //   // [JDK-6967479] JTable sorter fires even if the model is empty - Java Bug System
      //   // https://bugs.openjdk.java.net/browse/JDK-6967479
      //   table.setRowSorter(null);
      //   table.getTableHeader().repaint();
      // }
      model.setRowCount(0);
      // table.setAutoCreateColumnsFromModel(false);
      // table.setModel(new DefaultTableModel());
    });

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

class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  protected TablePopupMenu() {
    super();
    add("add").addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      // if (MainPanel.DEBUG && model.getRowCount() == 0) {
      //   table.setRowSorter(new TableRowSorter<>(model));
      //   // table.setRowSorter(sorter);
      //   model.fireTableDataChanged();
      // }
      model.addRow(new Object[] {"", model.getRowCount(), false});
      Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
      table.scrollRectToVisible(r);
    });
    addSeparator();
    delete = add("delete");
    delete.addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      int[] selection = table.getSelectedRows();
      for (int i = selection.length - 1; i >= 0; i--) {
        model.removeRow(table.convertRowIndexToModel(selection[i]));
      }
      // if (MainPanel.DEBUG && model.getRowCount() == 0) {
      //   table.setRowSorter(null);
      //   table.getTableHeader().repaint();
      // }
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTable) {
      delete.setEnabled(((JTable) c).getSelectedRowCount() > 0);
      super.show(c, x, y);
    }
  }
}
