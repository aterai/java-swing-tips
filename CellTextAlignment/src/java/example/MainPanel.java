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
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private final JRadioButton leftRadio = new JRadioButton("left", true);
  private final JRadioButton centerRadio = new JRadioButton("center");
  private final JRadioButton rightRadio = new JRadioButton("right");
  private final JRadioButton customRadio = new JRadioButton("custom");

  private MainPanel() {
    super(new BorderLayout());
    // JTable table = new JTable(makeModel()) {
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
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);

    TableColumnModel cm = table.getColumnModel();
    cm.getColumn(0).setMinWidth(60);
    cm.getColumn(0).setMaxWidth(60);
    cm.getColumn(0).setResizable(false);
    cm.getColumn(1).setCellRenderer(new HorizontalAlignmentTableRenderer());
    cm.getColumn(2).setHeaderRenderer(new HeaderRenderer());

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

  private static TableModel makeModel() {
    String[] columnNames = {"Integer", "String", "Boolean"};
    Object[][] data = {
        {12, "aaa", true}, {5, "bbb", false}, {92, "CCC", true}, {0, "DDD", false}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  private final class HorizontalAlignmentTableRenderer extends DefaultTableCellRenderer {
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      Component c = super.getTableCellRendererComponent(
          table, value, isSelected, hasFocus, row, column);
      if (c instanceof JLabel) {
        initLabel((JLabel) c, row);
      }
      return c;
    }
  }

  public void initLabel(JLabel l, int row) {
    if (leftRadio.isSelected()) {
      l.setHorizontalAlignment(SwingConstants.LEFT);
    } else if (centerRadio.isSelected()) {
      l.setHorizontalAlignment(SwingConstants.CENTER);
    } else if (rightRadio.isSelected()) {
      l.setHorizontalAlignment(SwingConstants.RIGHT);
    } else if (customRadio.isSelected()) {
      switch (row % 3) {
        case 2:
          l.setHorizontalAlignment(SwingConstants.RIGHT);
          break;
        case 1:
          l.setHorizontalAlignment(SwingConstants.CENTER);
          break;
        default:
          l.setHorizontalAlignment(SwingConstants.LEFT);
          break;
      }
    }
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

class HeaderRenderer implements TableCellRenderer {
  private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    TableCellRenderer hr = table.getTableHeader().getDefaultRenderer();
    Component c = hr.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    c.setFont(FONT);
    if (c instanceof JLabel) {
      ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
    }
    return c;
  }
}
