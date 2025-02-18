// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private static final int START_HEIGHT = 4;
  private static final int END_HEIGHT = 24;
  private static final int DELAY = 10;

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    table.setFillsViewportHeight(true);
    table.setAutoCreateRowSorter(true);
    table.setRowHeight(START_HEIGHT);
    for (int i = 0; i < table.getRowCount(); i++) {
      table.setRowHeight(i, END_HEIGHT);
    }
    Action deleteAction = new AbstractAction("delete") {
      @Override public void actionPerformed(ActionEvent e) {
        deleteActionPerformed(table);
      }
    };
    JPopupMenu popup = new JPopupMenu() {
      @Override public void show(Component c, int x, int y) {
        if (c instanceof JTable) {
          deleteAction.setEnabled(((JTable) c).getSelectedRowCount() > 0);
          super.show(c, x, y);
        }
      }
    };
    Action createAction = new AbstractAction("add") {
      @Override public void actionPerformed(ActionEvent e) {
        createActionPerformed(table);
      }
    };
    popup.add(createAction);
    popup.addSeparator();
    popup.add(deleteAction);

    JScrollPane scroll = new JScrollPane(table);
    scroll.setComponentPopupMenu(popup);
    table.setInheritsPopupMenu(true);
    add(scroll);
    add(new JButton(createAction), BorderLayout.SOUTH);
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

  public void createActionPerformed(JTable table) {
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    model.addRow(new Object[] {"New name", model.getRowCount(), false});
    int index = table.convertRowIndexToView(model.getRowCount() - 1);
    AtomicInteger height = new AtomicInteger(START_HEIGHT);
    new Timer(DELAY, e -> {
      int curHeight = height.getAndIncrement();
      if (curHeight < END_HEIGHT) {
        table.setRowHeight(index, curHeight);
      } else {
        ((Timer) e.getSource()).stop();
      }
    }).start();
  }

  public void deleteActionPerformed(JTable table) {
    int[] selection = table.getSelectedRows();
    if (selection.length > 0) {
      AtomicInteger height = new AtomicInteger(END_HEIGHT);
      new Timer(DELAY, e -> {
        int curHeight = height.getAndDecrement();
        if (curHeight > START_HEIGHT) {
          setRowsHeight(table, selection, curHeight);
        } else {
          ((Timer) e.getSource()).stop();
          removeRows(table, selection);
        }
      }).start();
    }
  }

  private static void removeRows(JTable table, int[] selection) {
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    for (int i = selection.length - 1; i >= 0; i--) {
      model.removeRow(table.convertRowIndexToModel(selection[i]));
    }
  }

  private static void setRowsHeight(JTable table, int[] selection, int height) {
    for (int i = selection.length - 1; i >= 0; i--) {
      table.setRowHeight(selection[i], height);
    }
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
