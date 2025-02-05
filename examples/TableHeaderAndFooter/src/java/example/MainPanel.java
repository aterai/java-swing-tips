// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);
    JTableHeader footer = new JTableHeader(table.getColumnModel());
    footer.setTable(table);
    // footer.setResizingAllowed(true);
    // table.setTableHeader(footer);
    JScrollPane south = new JScrollPane();
    JViewport vp = new JViewport();
    vp.setView(footer);
    south.setColumnHeader(vp);
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(new JScrollPane(table));
    p.add(south, BorderLayout.SOUTH);
    // p.add(footer, BorderLayout.SOUTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false},
        {"eee", 22, true}, {"fff", 6, false}, {"ggg", 83, true}, {"hhh", 9, false},
        {"iii", 31, true}, {"jjj", 4, false}, {"kkk", 75, true}, {"lll", 8, false},
        {"mmm", 77, true}, {"nnn", 2, false}, {"OOO", 68, true}, {"PPP", 7, false}
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
