// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tab = new JTabbedPane();
    tab.addTab("<html>Test<p>Test</p></html>", new JLabel("Test1"));
    tab.addTab("<html>Test<p>test", new JLabel("Test2"));

    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);
    table.setRowSelectionAllowed(true);
    table.setRowHeight(32);
    table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

    add(tab, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "<html><span style='color:red'>Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"<html><span style='color:blue'>bbb", 5, false},
        {"CCC", 92, true}, {"<html><span style='color:green'>DDD", 0, false}
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
