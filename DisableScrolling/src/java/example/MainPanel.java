// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private final String[] columnNames = {"String", "Integer", "Boolean"};
  private final Object[][] data = {
    {"aaa", 12, true}, {"bbb", 5, false},
    {"CCC", 92, true}, {"DDD", 0, false},
  };
  private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
    @Override public Class<?> getColumnClass(int column) {
      return getValueAt(0, column).getClass();
    }
  };
  private final JTable table = new JTable(model) {
    @Override public String getToolTipText(MouseEvent e) {
      int row = convertRowIndexToModel(rowAtPoint(e.getPoint()));
      TableModel m = getModel();
      return String.format("%s, %s", m.getValueAt(row, 0), m.getValueAt(row, 2));
    }
  };
  private final JScrollPane scroll = new JScrollPane(table);
  // [JDK-6299213] The PopupMenu is not updated if the LAF is changed (incomplete fix of 4962731) - Java Bug System
  // Fixed: https://bugs.openjdk.java.net/browse/JDK-6299213
  // private final JScrollPane scroll = new JScrollPane(table) {
  //   @Override public void updateUI() {
  //     super.updateUI();
  //     JPopupMenu jpm = getComponentPopupMenu();
  //     if (jpm == null && pop != null) {
  //       SwingUtilities.updateComponentTreeUI(pop);
  //     }
  //   }
  // };

  private MainPanel() {
    super(new BorderLayout());

    IntStream.range(0, 100).forEach(i -> model.addRow(new Object[] {"Name " + i, i, Boolean.FALSE}));
    table.setAutoCreateRowSorter(true);

    JCheckBox check = new JCheckBox("Disable Scrolling");
    check.addActionListener(e -> {
      table.clearSelection();
      boolean isSelected = ((JCheckBox) e.getSource()).isSelected();
      scroll.getVerticalScrollBar().setEnabled(!isSelected);
      scroll.setWheelScrollingEnabled(!isSelected);
      table.setEnabled(!isSelected);
      // table.getTableHeader().setEnabled(!isSelected);
      // scroll.setComponentPopupMenu(isSelected ? pop : null);
    });

    // scroll.setComponentPopupMenu(new TablePopupMenu());
    // table.setInheritsPopupMenu(true);
    table.setComponentPopupMenu(new TablePopupMenu());

    add(scroll);
    add(check, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
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
  private final JMenuItem createMenuItem;
  private final JMenuItem deleteMenuItem;

  protected TablePopupMenu() {
    super();
    createMenuItem = add("add");
    createMenuItem.addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      model.addRow(new Object[] {"New row", 0, Boolean.FALSE});
      Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
      table.scrollRectToVisible(r);
    });
    addSeparator();
    deleteMenuItem = add("delete");
    deleteMenuItem.addActionListener(e -> {
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
      createMenuItem.setEnabled(c.isEnabled());
      deleteMenuItem.setEnabled(((JTable) c).getSelectedRowCount() > 0);
      super.show(c, x, y);
    }
  }
}
