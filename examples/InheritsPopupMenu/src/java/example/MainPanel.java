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
    DefaultTableModel model = makeModel();
    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);

    JMenuItem delete = new JMenuItem("delete");
    delete.addActionListener(e -> {
      int[] selection = table.getSelectedRows();
      for (int i = selection.length - 1; i >= 0; i--) {
        model.removeRow(table.convertRowIndexToModel(selection[i]));
      }
    });

    JPopupMenu popup = new JPopupMenu() {
      @Override public void show(Component c, int x, int y) {
        delete.setEnabled(table.getSelectedRowCount() > 0);
        super.show(c, x, y);
      }
    };
    popup.add("add").addActionListener(e -> model.addRow(new Object[] {"example", 0, false}));
    popup.addSeparator();
    popup.add(delete);

    JScrollPane scroll = new JScrollPane(table);
    scroll.setBackground(Color.RED);
    scroll.getViewport().setBackground(Color.GREEN);
    scroll.setComponentPopupMenu(popup);
    // scroll.getViewport().setInheritsPopupMenu(true); // 1.5.0

    // table.setComponentPopupMenu(new TablePopupMenu());
    table.setInheritsPopupMenu(true);
    table.setFillsViewportHeight(true);
    table.setBackground(Color.YELLOW);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    // table.setShowGrid(false);
    // table.setShowHorizontalLines(false);
    // table.setShowVerticalLines(false);
    // table.setOpaque(false);
    // table.getTableHeader().setInheritsPopupMenu(true);

    JCheckBox cb1 = new JCheckBox("InheritsPopupMenu", true);
    cb1.addActionListener(e -> table.setInheritsPopupMenu(cb1.isSelected()));

    JCheckBox cb2 = new JCheckBox("FillsViewportHeight", true);
    cb2.addActionListener(e -> table.setFillsViewportHeight(cb2.isSelected()));

    Box box = Box.createHorizontalBox();
    box.add(cb1);
    box.add(cb2);
    add(box, BorderLayout.NORTH);
    add(scroll);
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
