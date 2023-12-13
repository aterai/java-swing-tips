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
    JTable table = new JTable(makeModel()) {
      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    table.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        JTable t = (JTable) e.getComponent();
        boolean isDoubleClick = e.getClickCount() >= 2;
        if (isDoubleClick) {
          TableModel m = t.getModel();
          Point pt = e.getPoint();
          int i = t.rowAtPoint(pt);
          if (i >= 0) {
            int row = t.convertRowIndexToModel(i);
            String s = String.format("%s (%s)", m.getValueAt(row, 0), m.getValueAt(row, 1));
            JOptionPane.showMessageDialog(t, s, "title", JOptionPane.INFORMATION_MESSAGE);
          }
        }
      }
    });
    // DefaultCellEditor ce = (DefaultCellEditor) table.getDefaultEditor(Object.class);
    // ce.setClickCountToStart(Integer.MAX_VALUE);
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "String"};
    Object[][] data = {
        {"aaa", 1, "eee"}, {"bbb", 2, "FFF"},
        {"CCC", 0, "GGG"}, {"DDD", 3, "hhh"}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return column == 1 ? Integer.class : String.class;
      }
      // @Override public boolean isCellEditable(int row, int column) {
      //   return false;
      // }
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
  private final JMenuItem delete;

  /* default */ TablePopupMenu() {
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
