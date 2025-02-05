// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(new DefaultTableModel(10, 4));
    table.setCellSelectionEnabled(true);
    table.setAutoCreateRowSorter(true);

    TableColumnModel cm = table.getColumnModel();
    TableCellRenderer r = new ColumnHeaderRenderer();
    for (int i = 0; i < cm.getColumnCount(); i++) {
      cm.getColumn(i).setHeaderRenderer(r);
    }
    cm.getSelectionModel().addListSelectionListener(e -> table.getTableHeader().repaint());

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ColumnHeaderRenderer implements TableCellRenderer {
  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    ListSelectionModel csm = table.getColumnModel().getSelectionModel();
    boolean f = csm.getLeadSelectionIndex() == column || hasFocus;
    return r.getTableCellRendererComponent(table, value, isSelected, f, row, column);
  }
}
