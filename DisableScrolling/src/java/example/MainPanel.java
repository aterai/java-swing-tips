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
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public String getToolTipText(MouseEvent e) {
        String txt = super.getToolTipText(e);
        int idx = rowAtPoint(e.getPoint());
        if (idx >= 0) {
          int row = convertRowIndexToModel(idx);
          TableModel m = getModel();
          txt = String.format("%s, %s", m.getValueAt(row, 0), m.getValueAt(row, 2));
        }
        return txt;
      }
    };
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());
    // table.setInheritsPopupMenu(true);
    JScrollPane scroll = new JScrollPane(table);
    // [JDK-6299213]
    // The PopupMenu is not updated if the LAF is changed (incomplete fix of 4962731)
    // Fixed: https://bugs.openjdk.org/browse/JDK-6299213
    // JScrollPane scroll = new JScrollPane(table) {
    //   @Override public void updateUI() {
    //     super.updateUI();
    //     JPopupMenu jpm = getComponentPopupMenu();
    //     if (jpm == null && pop != null) {
    //       SwingUtilities.updateComponentTreeUI(pop);
    //     }
    //   }
    // };
    // scroll.setComponentPopupMenu(new TablePopupMenu());
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
    add(scroll);
    add(check, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
    };
    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    IntStream.range(0, 100)
        .mapToObj(i -> new Object[] {"Name " + i, i, false})
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

final class TablePopupMenu extends JPopupMenu {
  private final JMenuItem createMenuItem;
  private final JMenuItem deleteMenuItem;

  /* default */ TablePopupMenu() {
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
