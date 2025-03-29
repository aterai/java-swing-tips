// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = makeTable(makeModel());
    add(new JLayer<>(new JScrollPane(table), new BorderPaintLayerUI()));
    setPreferredSize(new Dimension(320, 240));
  }

  private JTable makeTable(TableModel model) {
    return new JTable(model) {
      @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        int promotion = 2;
        int playoffs = 6;
        int relegation = 21;
        boolean isSelected = isRowSelected(row);
        if (!isSelected) {
          Integer num = (Integer) model.getValueAt(convertRowIndexToModel(row), 0);
          if (num <= promotion) {
            c.setBackground(new Color(0xCF_F3_C0));
          } else if (num <= playoffs) {
            c.setBackground(new Color(0xCB_F7_F5));
          } else if (num >= relegation) {
            c.setBackground(new Color(0xFB_DC_DC));
          } else if (row % 2 == 0) {
            c.setBackground(Color.WHITE);
          } else {
            c.setBackground(new Color(0xF0_F0_F0));
          }
        }
        c.setForeground(Color.BLACK);
        if (c instanceof JLabel && column != 1) {
          ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
        }
        return c;
      }

      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }

      @Override public void updateUI() {
        super.updateUI();
        setFillsViewportHeight(true);
        setShowVerticalLines(false);
        setShowHorizontalLines(false);
        setIntercellSpacing(new Dimension());
        setSelectionForeground(getForeground());
        setSelectionBackground(new Color(0, 0, 100, 50));
        setAutoCreateRowSorter(true);
        setFocusable(false);
        initTableHeader(this);
      }
    };
  }

  private static void initTableHeader(JTable table) {
    JTableHeader header = table.getTableHeader();
    ((JLabel) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    TableColumnModel columnModel = table.getColumnModel();
    IntStream.range(0, columnModel.getColumnCount())
        .filter(i -> i != 1)
        .forEach(i -> columnModel.getColumn(i).setMaxWidth(26));
    columnModel.getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
      @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String v = Objects.toString(value);
        String txt = v.startsWith("-") || "0".equals(v) ? v : "+" + v;
        setHorizontalAlignment(RIGHT);
        return super.getTableCellRendererComponent(table, txt, isSelected, hasFocus, row, column);
      }
    });
  }

  private static TableModel makeModel() {
    String[] columnNames = {"#", "Team", "MP", "W", "D", "L", "F", "A", "GD", "P"};
    Object[][] data = {
        {1, "Machida", 33, 20, 7, 6, 57, 27, +30, 67},
        {2, "Iwata", 35, 17, 11, 7, 61, 39, +22, 62},
        {3, "Shimizu", 34, 16, 12, 6, 61, 27, +34, 60},
        {4, "Tokyo", 35, 17, 9, 9, 47, 26, +21, 60},
        {5, "Nagasaki", 35, 15, 10, 10, 58, 43, +15, 55},
        {6, "Chiba", 35, 15, 9, 11, 46, 44, +2, 54},
        {7, "Kofu", 35, 15, 7, 13, 49, 43, +6, 52},
        {8, "Okayama", 35, 12, 15, 8, 43, 37, +6, 51},
        {9, "Yamagata", 35, 16, 3, 16, 53, 49, +4, 51},
        {10, "Oita", 35, 14, 9, 12, 46, 49, -3, 51},
        {11, "Gunma", 32, 12, 12, 8, 36, 30, +6, 48},
        {12, "Mito", 35, 11, 12, 12, 45, 53, -8, 45},
        {13, "Tochigi", 35, 10, 12, 13, 35, 35, +0, 42},
        {14, "Tokushima", 35, 8, 17, 10, 39, 46, -7, 41},
        {15, "Akita", 34, 9, 13, 12, 27, 36, -9, 40},
        {16, "Sendai", 35, 10, 10, 15, 40, 50, -10, 40},
        {17, "Fujieda", 33, 11, 7, 15, 46, 57, -11, 40},
        {18, "Kumamoto", 35, 9, 10, 16, 42, 45, -3, 37},
        {19, "Iwaki", 34, 9, 10, 15, 33, 51, -18, 37},
        {20, "Yamaguchi", 35, 8, 12, 15, 28, 55, -27, 36},
        {21, "Kanazawa", 33, 9, 5, 19, 35, 55, -20, 32},
        {22, "Omiya", 35, 7, 6, 22, 30, 60, -30, 27},
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

class BorderPaintLayerUI extends LayerUI<JScrollPane> {
  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    JTable table = getTable(c);
    RowSorter<? extends TableModel> sorter = table == null ? null : table.getRowSorter();
    if (Objects.nonNull(sorter)) {
      List<? extends RowSorter.SortKey> keys = sorter.getSortKeys();
      int column = keys.isEmpty() ? -1 : keys.get(0).getColumn();
      if (column <= 0 || column == 9) {
        boolean b1 = column == 0 && keys.get(0).getSortOrder() == SortOrder.ASCENDING;
        boolean b2 = column == 9 && keys.get(0).getSortOrder() == SortOrder.DESCENDING;
        paintLines(g, c, table, column < 0 || b1 || b2);
      }
    }
  }

  private static void paintLines(Graphics g, Component layer, JTable table, boolean b) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(Color.GREEN.darker());
    if (b) {
      g2.draw(makeUnderline(layer, table, 2));
      g2.setPaint(Color.BLUE.darker());
      g2.draw(makeUnderline(layer, table, 6));
      g2.setPaint(Color.RED.darker());
      g2.draw(makeUnderline(layer, table, 20));
    } else {
      g2.draw(makeUnderline(layer, table, 22 - 2));
      g2.setPaint(Color.BLUE.darker());
      g2.draw(makeUnderline(layer, table, 22 - 6));
      g2.setPaint(Color.RED.darker());
      g2.draw(makeUnderline(layer, table, 22 - 20));
    }
    g2.dispose();
  }

  private static JTable getTable(Component c) {
    JTable table = null;
    if (c instanceof JLayer) {
      Component c1 = ((JLayer<?>) c).getView();
      if (c1 instanceof JScrollPane) {
        table = (JTable) ((JScrollPane) c1).getViewport().getView();
      }
    }
    return table;
  }

  private static Line2D makeUnderline(Component c, JTable table, int idx) {
    Rectangle r0 = table.getCellRect(idx - 1, 0, false);
    Rectangle r1 = table.getCellRect(idx - 1, table.getColumnCount() - 1, false);
    Rectangle r = SwingUtilities.convertRectangle(table, r0.union(r1), c);
    return new Line2D.Double(r.getX(), r.getMaxY(), r.getMaxX(), r.getMaxY());
  }
}
