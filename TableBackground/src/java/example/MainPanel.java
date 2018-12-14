// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public final class MainPanel extends JPanel {
  private final String[] columnNames = {"String", "Integer", "Boolean"};
  private final Object[][] data = {
    {"aaa", 12, true}, {"bbb", 5, false},
    {"CCC", 92, true}, {"DDD", 0, false}
  };
  private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
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
  private final JTable table = new JTable(model);
  private final JCheckBox check = new JCheckBox("viewport setOpaque", true);
  private final JButton button = new JButton("Choose background color");

  public MainPanel() {
    super(new BorderLayout());
    table.setAutoCreateRowSorter(true);
    table.setRowSelectionAllowed(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

    TableColumn col = table.getColumnModel().getColumn(0);
    col.setMinWidth(60);
    col.setMaxWidth(60);
    col.setResizable(false);

    JScrollPane scroll = new JScrollPane(table);
    scroll.setComponentPopupMenu(new TablePopupMenu());
    // scroll.getViewport().setInheritsPopupMenu(true); // 1.5.0
    table.setInheritsPopupMenu(true);
    // table.setFillsViewportHeight(true);

    scroll.getViewport().setOpaque(true);
    // scroll.getViewport().setBackground(Color.WHITE);

    check.addActionListener(e -> {
      scroll.getViewport().setOpaque(((JCheckBox) e.getSource()).isSelected());
      scroll.repaint();
    });

    button.addActionListener(e -> {
      Color color = JColorChooser.showDialog(getRootPane(), "background color", scroll.getViewport().getBackground());
      scroll.getViewport().setBackground(color);
      scroll.repaint();
    });

    JPanel pnl = new JPanel();
    pnl.add(check);
    pnl.add(button);

    add(scroll);
    add(pnl, BorderLayout.SOUTH);
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

class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  protected TablePopupMenu() {
    super();
    add("add").addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      model.addRow(new Object[] {"New row", model.getRowCount(), false});
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
