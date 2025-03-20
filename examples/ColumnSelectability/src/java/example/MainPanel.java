// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    TableModel model = makeModel();
    JTable table = new JTable(model) {
      private boolean isColumnSelectable(int column) {
        return convertColumnIndexToModel(column) == 0;
      }

      @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        if (!isColumnSelectable(columnIndex)) {
          return;
        }
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
      }

      @Override public boolean isCellEditable(int row, int column) {
        return isColumnSelectable(column);
      }

      @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        // return isColumnSelectable(column)
        //     ? super.prepareRenderer(renderer, row, column)
        //     : renderer.getTableCellRendererComponent(
        //         this, getValueAt(row, column), false, false, row, column);
        Component c;
        if (isColumnSelectable(column)) {
          c = super.prepareRenderer(renderer, row, column);
        } else {
          Object o = getValueAt(row, column);
          c = renderer.getTableCellRendererComponent(this, o, false, false, row, column);
        }
        return c;
      }
    };
    table.setCellSelectionEnabled(true);
    // table.putClientProperty("Table.isFileList", Boolean.TRUE);
    // table.getTableHeader().setReorderingAllowed(false);
    add(new JScrollPane(new JTable(model)));
    add(new JScrollPane(table));
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
