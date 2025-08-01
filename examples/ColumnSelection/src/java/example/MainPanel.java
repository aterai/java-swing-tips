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
    JTable table = makeTable();
    JTableHeader header = table.getTableHeader();
    header.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        JTable t = ((JTableHeader) e.getComponent()).getTable();
        if (t.isEditing()) {
          t.getCellEditor().stopCellEditing();
        }
        int col = header.columnAtPoint(e.getPoint());
        t.changeSelection(0, col, false, false);
        t.changeSelection(t.getRowCount() - 1, col, false, true);
      }
    });
    List<JTable> list = Arrays.asList(makeTable(), table);
    JPanel p = new JPanel(new GridLayout(2, 1));
    list.stream().map(JScrollPane::new).forEach(p::add);
    JButton button = new JButton("clear selection");
    button.addActionListener(e -> list.forEach(JTable::clearSelection));
    add(p);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable makeTable() {
    JTable table = new JTable(makeModel());
    table.setCellSelectionEnabled(true);
    table.setAutoCreateRowSorter(true);
    return table;
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
