// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private final String[] columnNames = {"String-String/String", "Integer", "Boolean"};
  private final Object[][] data = {
    {"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", 12, true},
    {"BBB", 2, true}, {"EEE", 3, false},
    {"CCC", 4, true}, {"FFF", 5, false},
    {"DDD", 6, true}, {"GGG", 7, false}
  };
  private final TableModel model = new DefaultTableModel(data, columnNames) {
    @Override public Class<?> getColumnClass(int column) {
      return getValueAt(0, column).getClass();
    }
  };
  private final JTable table = new JTable(model) {
    @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
      Component c = super.prepareRenderer(tcr, row, column);
      if (c instanceof JComponent) {
        JComponent l = (JComponent) c;
        Insets i = l.getInsets();
        Rectangle rect = getCellRect(row, column, false);
        rect.width -= i.left + i.right;
        FontMetrics fm = l.getFontMetrics(l.getFont());
        String str = Objects.toString(getValueAt(row, column), "");
        int cellTextWidth = fm.stringWidth(str);
        l.setToolTipText(cellTextWidth > rect.width ? str : null);
      }
      return c;
    }

    @Override public void updateUI() {
      super.updateUI();
      TableCellRenderer r = new ToolTipHeaderRenderer();
      for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
        getColumnModel().getColumn(i).setHeaderRenderer(r);
      }
      // JTableHeader h = getTableHeader();
      // h.setDefaultRenderer(new ToolTipHeaderRenderer(h.getDefaultRenderer()));
    }
  };

  private MainPanel() {
    super(new BorderLayout());
    table.setAutoCreateRowSorter(true);
    add(new JScrollPane(table));
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

class ToolTipHeaderRenderer implements TableCellRenderer {
  // private final Icon icon = UIManager.getIcon("Table.ascendingSortIcon");

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
    JLabel l = (JLabel) renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    Insets i = l.getInsets();
    Rectangle rect = table.getCellRect(row, column, false);
    rect.width -= i.left + i.right;
    // RowSorter<? extends TableModel> sorter = table.getRowSorter();
    // if (sorter != null && !sorter.getSortKeys().isEmpty() && sorter.getSortKeys().get(0).getColumn() == column) {
    //   rect.width -= icon.getIconWidth() + l.getIconTextGap();
    // }

    // Optional.ofNullable(table.getRowSorter())
    //   .filter(sorter -> !sorter.getSortKeys().isEmpty() && sorter.getSortKeys().get(0).getColumn() == column)
    //   .filter(sorter -> Objects.nonNull(l.getIcon()))
    //   .ifPresent(sorter -> rect.width -= icon.getIconWidth() + l.getIconTextGap());

    Optional.ofNullable(l.getIcon())
      .ifPresent(icon -> rect.width -= icon.getIconWidth() + l.getIconTextGap());

    FontMetrics fm = l.getFontMetrics(l.getFont());
    String str = Objects.toString(value, "");
    int cellTextWidth = fm.stringWidth(str);
    l.setToolTipText(cellTextWidth > rect.width ? str : null);
    return l;
  }
}
