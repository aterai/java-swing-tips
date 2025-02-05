// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
          @Override public String getToolTipText(MouseEvent e) {
            int col = columnAtPoint(e.getPoint());
            return col >= 0 ? getToolTipText(col) : super.getToolTipText(e);
          }

          private String getToolTipText(int column) {
            TableColumn c = getColumnModel().getColumn(column);
            return String.format("%s (width=%dpx)", c.getHeaderValue(), c.getWidth());
          }
        };
      }

      @Override public void updateUI() {
        super.updateUI();
        setAutoResizeMode(AUTO_RESIZE_OFF);
      }
    };
    // table.setTableHeader(new JTableHeader(table.getColumnModel()) {
    //   @Override public String getToolTipText(MouseEvent e) {
    //     int col = columnAtPoint(e.getPoint());
    //     return col >= 0 ? getToolTipText(col) : super.getToolTipText(e);
    //   }
    // });
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
