// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private final JRadioButton leftRadio = new JRadioButton("left", true);
  private final JRadioButton centerRadio = new JRadioButton("center");
  private final JRadioButton rightRadio = new JRadioButton("right");
  private final JRadioButton customRadio = new JRadioButton("custom");

  private MainPanel() {
    super(new BorderLayout());

    String[] columnNames = {"Integer", "String", "Boolean"};
    Object[][] data = {
      {12, "aaa", true}, {5, "bbb", false},
      {92, "CCC", true}, {0, "DDD", false}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    // JTable table = new JTable(model) {
    //   @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
    //     Component c = super.prepareRenderer(tcr, row, column);
    //     if (1 == convertColumnIndexToModel(column)) {
    //       initLabel((JLabel) c, row);
    //     } else {
    //       ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
    //     }
    //     return c;
    //   }
    // };
    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);

    TableColumn col = table.getColumnModel().getColumn(0);
    col.setMinWidth(60);
    col.setMaxWidth(60);
    col.setResizable(false);

    col = table.getColumnModel().getColumn(1);
    col.setCellRenderer(new HorizontalAlignmentTableRenderer());

    col = table.getColumnModel().getColumn(2);
    col.setHeaderRenderer(new HeaderRenderer());

    ButtonGroup bg = new ButtonGroup();
    JPanel p = new JPanel();
    Stream.of(leftRadio, centerRadio, rightRadio, customRadio).forEach(rb -> {
      bg.add(rb);
      p.add(rb);
      rb.addActionListener(e -> table.repaint());
    });

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private class HorizontalAlignmentTableRenderer extends DefaultTableCellRenderer {
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if (c instanceof JLabel) {
        initLabel((JLabel) c, row);
      }
      return c;
    }
  }

  protected void initLabel(JLabel l, int row) {
    if (leftRadio.isSelected()) {
      l.setHorizontalAlignment(SwingConstants.LEFT);
    } else if (centerRadio.isSelected()) {
      l.setHorizontalAlignment(SwingConstants.CENTER);
    } else if (rightRadio.isSelected()) {
      l.setHorizontalAlignment(SwingConstants.RIGHT);
    } else if (customRadio.isSelected()) {
      l.setHorizontalAlignment(row % 3 == 0 ? SwingConstants.LEFT
                   : row % 3 == 1 ? SwingConstants.CENTER
                          : SwingConstants.RIGHT);
    }
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
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class HeaderRenderer implements TableCellRenderer {
  private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    TableCellRenderer hr = table.getTableHeader().getDefaultRenderer();
    JLabel l = (JLabel) hr.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    l.setHorizontalAlignment(SwingConstants.CENTER);
    l.setFont(FONT);
    return l;
  }
}
