// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
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

    JCheckBox check = new JCheckBox("viewport setOpaque", true);
    check.addActionListener(e -> {
      scroll.getViewport().setOpaque(((JCheckBox) e.getSource()).isSelected());
      scroll.repaint();
    });

    JButton button = new JButton("Choose background color");
    button.addActionListener(e -> {
      Color bgc = scroll.getViewport().getBackground();
      Color color = JColorChooser.showDialog(getRootPane(), "background color", bgc);
      if (color != null) {
        scroll.getViewport().setBackground(color);
      }
    });

    JPanel pnl = new JPanel();
    pnl.add(check);
    pnl.add(button);

    add(scroll);
    add(pnl, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
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
    } else if (c instanceof JScrollPane) {
      JScrollPane scroll = (JScrollPane) c;
      JTable table = (JTable) scroll.getViewport().getView();
      delete.setEnabled(table.getSelectedRowCount() > 0);
      Point pt = SwingUtilities.convertPoint(c, x, y, table);
      super.show(table, pt.x, pt.y);
    }
  }
}
