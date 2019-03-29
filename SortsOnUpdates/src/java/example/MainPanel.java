// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    String[] columnNames = {"Integer", "String", "Boolean"};
    Object[][] data = {
      {0, "", true}, {1, "", false},
      {2, "", true}, {3, "", false}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        switch (column) {
          case 0: return Integer.class;
          case 1: return String.class;
          case 2: return Boolean.class;
          default: return super.getColumnClass(column);
        }
      }
    };
    JTable table = new JTable(model) {
      @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
        Component c = super.prepareRenderer(tcr, row, column);
        if (isRowSelected(row)) {
          c.setForeground(getSelectionForeground());
          c.setBackground(getSelectionBackground());
        } else if (convertRowIndexToModel(row) == getRowCount() - 1) {
          c.setForeground(Color.WHITE);
          c.setBackground(Color.RED);
        } else {
          c.setForeground(getForeground());
          c.setBackground(getBackground());
        }
        return c;
      }
    };
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());

    JCheckBox check = new JCheckBox("DefaultRowSorter#setSortsOnUpdates");
    check.addActionListener(e -> {
      RowSorter<? extends TableModel> rs = table.getRowSorter();
      if (rs instanceof DefaultRowSorter) {
        ((DefaultRowSorter<?, ?>) rs).setSortsOnUpdates(((JCheckBox) e.getSource()).isSelected());
      }
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

class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  protected TablePopupMenu() {
    super();
    add("add").addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      int i = model.getRowCount();
      model.addRow(new Object[] {i, "", i % 2 == 0});
      Rectangle r = table.getCellRect(table.convertRowIndexToView(i - 1), 0, true);
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
