// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = createTable();
    table.getTableHeader().addMouseListener(new ColumnHeaderHandler());
    List<JTable> tables = Arrays.asList(createTable(), table);
    JPanel p = new JPanel(new GridLayout(2, 1));
    tables.stream().map(JScrollPane::new).forEach(p::add);
    JButton button = new JButton("clear selection");
    button.addActionListener(e -> tables.forEach(JTable::clearSelection));
    add(p);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable createTable() {
    JTable table = new JTable(createModel());
    table.setCellSelectionEnabled(true);
    table.setAutoCreateRowSorter(true);
    return table;
  }

  private static TableModel createModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false},
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

class ColumnHeaderHandler extends MouseAdapter {
  @Override public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    if (c instanceof JTableHeader) {
      JTableHeader header = (JTableHeader) c;
      JTable table = header.getTable();
      if (table.isEditing()) {
        table.getCellEditor().stopCellEditing();
      }
      int col = header.columnAtPoint(e.getPoint());
      table.changeSelection(0, col, false, false);
      table.changeSelection(table.getRowCount() - 1, col, false, true);
    }
  }
}
