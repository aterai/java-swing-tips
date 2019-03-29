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
  private final String[] columnNames = {"String", "Integer", "Boolean"};
  private final Object[][] data = {
    {"aaa", 12, true}, {"bbb", 5, false},
    {"CCC", 92, true}, {"DDD", 0, false}
  };
  private final TableModel model = new DefaultTableModel(data, columnNames) {
    @Override public Class<?> getColumnClass(int column) {
      return getValueAt(0, column).getClass();
    }
  };
  private final JTable table = new JTable(model) {
    private final Color evenColor = new Color(250, 250, 250);
    @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
      Component c = super.prepareRenderer(tcr, row, column);
      if (isCellSelected(row, column)) {
        c.setForeground(getSelectionForeground());
        c.setBackground(getSelectionBackground());
      } else {
        c.setForeground(getForeground());
        c.setBackground(row % 2 == 0 ? evenColor : getBackground());
      }
      return c;
    }
  };
  private final JScrollPane scrollPane = new JScrollPane(table);

  public MainPanel() {
    super(new BorderLayout());

    table.setAutoCreateRowSorter(true);

    // table.setTableHeader(null);
    // table.setTableHeader(new JTableHeader(table.getColumnModel()));

    add(scrollPane);
    JCheckBox check = new JCheckBox("JTableHeader visible: ", true);
    check.addActionListener(e -> {
      JCheckBox cb = (JCheckBox) e.getSource();
      // table.getTableHeader().setVisible(cb.isSelected());
      scrollPane.getColumnHeader().setVisible(cb.isSelected());
      scrollPane.revalidate();
    });
    add(check, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
