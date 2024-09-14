// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);
    TableColumn tableColumn = table.getColumnModel().getColumn(0);
    // tableColumn.setHeaderRenderer(new HeaderRenderer());
    Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 32);
    tableColumn.setHeaderRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
      TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
      Component c = r.getTableCellRendererComponent(
          tbl, value, isSelected, hasFocus, row, column);
      c.setFont(font);
      return c;
    });
    // all column
    // table.getTableHeader().setFont(font);
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

// class HeaderRenderer implements TableCellRenderer {
//   private final Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 32);
//
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
//     Component c = r.getTableCellRendererComponent(
//         table, value, isSelected, hasFocus, row, column);
//     c.setFont(font);
//     return c;
//   }
// }
