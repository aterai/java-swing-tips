// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public String getToolTipText(MouseEvent e) {
        String txt = super.getToolTipText(e);
        int idx = rowAtPoint(e.getPoint());
        if (idx >= 0) {
          int row = convertRowIndexToModel(idx);
          TableModel m = getModel();
          Object v0 = m.getValueAt(row, 0);
          Object v1 = m.getValueAt(row, 1);
          Object v2 = m.getValueAt(row, 2);
          txt = String.format("<html>%s<br>%s<br>%s</html>", v0, v1, v2);
        }
        return txt;
      }

      // public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
      //   Component c = super.prepareRenderer(tcr, row, column);
      //   if (c instanceof JComponent) {
      //     int mr = convertRowIndexToModel(row);
      //     int mc = convertColumnIndexToModel(column);
      //     Object o = getModel().getValueAt(mr, mc);
      //     String s = Objects.toString(o, "");
      //     ((JComponent) c).setToolTipText(s.isEmpty() ? null : s);
      //   }
      //   return c;
      // }
    };
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
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
