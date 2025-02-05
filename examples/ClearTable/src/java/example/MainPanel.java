// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultTableModel model = makeModel();
    JTable table = new JTable(model);
    // RowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
    // table.setRowSorter(sorter);
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());

    JButton button = new JButton("remove all rows");
    button.addActionListener(e -> model.setRowCount(0));
    // button.addActionListener(e -> {
    //   // ArrayIndexOutOfBoundsException: 0 >= 0
    //   // [JDK-6967479] JTable sorter fires even if the model is empty - Java Bug System
    //   // https://bugs.openjdk.org/browse/JDK-6967479
    //   table.setRowSorter(null);
    //   table.getTableHeader().repaint();
    //   model.setRowCount(0);
    //   table.setAutoCreateColumnsFromModel(false);
    //   table.setModel(new DefaultTableModel());
    // });

    add(button, BorderLayout.SOUTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultTableModel makeModel() {
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

final class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  /* default */ TablePopupMenu() {
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
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTable) {
      delete.setEnabled(((JTable) c).getSelectedRowCount() > 0);
      super.show(c, x, y);
    }
  }
}
