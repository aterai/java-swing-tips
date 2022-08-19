// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private static final int START_HEIGHT = 4;
  private static final int END_HEIGHT = 24;
  private static final int DELAY = 10;

  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
    };
    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        // ArrayIndexOutOfBoundsException: 0 >= 0
        // [JDK-6967479] JTable sorter fires even if the model is empty - Java Bug System
        // https://bugs.openjdk.org/browse/JDK-6967479
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
    table.setFillsViewportHeight(true);
    table.setAutoCreateRowSorter(true);
    table.setRowHeight(START_HEIGHT);
    for (int i = 0; i < model.getRowCount(); i++) {
      table.setRowHeight(i, END_HEIGHT);
    }
    Action deleteAction = new AbstractAction("delete") {
      @Override public void actionPerformed(ActionEvent e) {
        deleteActionPerformed(table, model);
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
        createActionPerformed(table, model);
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

  public void createActionPerformed(JTable table, DefaultTableModel model) {
    model.addRow(new Object[] {"New name", model.getRowCount(), false});
    int index = table.convertRowIndexToView(model.getRowCount() - 1);
    AtomicInteger height = new AtomicInteger(START_HEIGHT);
    new Timer(DELAY, e -> {
      int h = height.getAndIncrement();
      if (h < END_HEIGHT) {
        table.setRowHeight(index, h);
      } else {
        ((Timer) e.getSource()).stop();
      }
    }).start();
  }

  public void deleteActionPerformed(JTable table, DefaultTableModel model) {
    int[] selection = table.getSelectedRows();
    if (selection.length == 0) {
      return;
    }
    AtomicInteger height = new AtomicInteger(END_HEIGHT);
    new Timer(DELAY, e -> {
      int h = height.getAndDecrement();
      if (h > START_HEIGHT) {
        for (int i = selection.length - 1; i >= 0; i--) {
          table.setRowHeight(selection[i], h);
        }
      } else {
        ((Timer) e.getSource()).stop();
        for (int i = selection.length - 1; i >= 0; i--) {
          model.removeRow(table.convertRowIndexToModel(selection[i]));
        }
      }
    }).start();
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
