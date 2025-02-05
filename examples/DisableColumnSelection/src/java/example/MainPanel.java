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
    super(new BorderLayout());
    int targetColIdx = 0;
    TableModel model = makeModel();
    JTable table1 = new JTable(model) {
      @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        if (convertColumnIndexToModel(columnIndex) != targetColIdx) {
          return;
        }
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
      }

      @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c;
        if (convertColumnIndexToModel(column) == targetColIdx) {
          c = super.prepareRenderer(renderer, row, column);
        } else {
          Object value = getValueAt(row, column);
          c = renderer.getTableCellRendererComponent(this, value, false, false, row, column);
        }
        return c;
      }
    };

    JTable table2 = new JTable(model) {
      @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        if (convertColumnIndexToModel(columnIndex) != targetColIdx) {
          return;
        }
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
      }
    };
    table2.setCellSelectionEnabled(true);
    table2.getColumnModel().setSelectionModel(new DefaultListSelectionModel() {
      @Override public boolean isSelectedIndex(int index) {
        return table2.convertColumnIndexToModel(index) == targetColIdx;
      }
    });

    JPanel p = new JPanel(new GridLayout(0, 1));
    p.add(new JScrollPane(table1));
    p.add(new JScrollPane(table2));
    add(p);
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

      @Override public boolean isCellEditable(int row, int column) {
        return column != 0;
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
