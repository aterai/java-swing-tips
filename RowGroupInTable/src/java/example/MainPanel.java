// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setFillsViewportHeight(true);
        setDefaultRenderer(RowData.class, new RowDataRenderer());
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(getModel());
        Comparator<RowData> c = Comparator.comparing(RowData::getGroup);
        sorter.setComparator(0, c);
        sorter.setComparator(1, c.thenComparing(RowData::getName));
        sorter.setComparator(2, c.thenComparing(RowData::getCount));
        setRowSorter(sorter);
      }
    };

    JButton button = new JButton("clear SortKeys");
    button.addActionListener(e -> table.getRowSorter().setSortKeys(null));

    add(new JScrollPane(table));
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"Group", "Name", "Count"};
    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
      @Override public Class<?> getColumnClass(int column) {
        return RowData.class;
      }
    };
    String colors = "colors";
    addRowData(model, new RowData(colors, "blue", 1));
    addRowData(model, new RowData(colors, "violet", 2));
    addRowData(model, new RowData(colors, "red", 3));
    addRowData(model, new RowData(colors, "yellow", 4));
    String sports = "sports";
    addRowData(model, new RowData(sports, "baseball", 23));
    addRowData(model, new RowData(sports, "soccer", 22));
    addRowData(model, new RowData(sports, "football", 21));
    addRowData(model, new RowData(sports, "hockey", 20));
    String food = "food";
    addRowData(model, new RowData(food, "hot dogs", 10));
    addRowData(model, new RowData(food, "pizza", 11));
    addRowData(model, new RowData(food, "ravioli", 12));
    addRowData(model, new RowData(food, "bananas", 13));
    return model;
  }

  private static void addRowData(DefaultTableModel model, RowData data) {
    model.addRow(Collections.nCopies(model.getColumnCount(), data).toArray());
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

class RowData {
  private final String group;
  private final String name;
  private final int count;

  protected RowData(String group, String name, int count) {
    this.group = group;
    this.name = name;
    this.count = count;
  }

  public String getGroup() {
    return group;
  }

  public String getName() {
    return name;
  }

  public int getCount() {
    return count;
  }
}

class RowDataRenderer implements TableCellRenderer {
  private final TableCellRenderer renderer = new DefaultTableCellRenderer();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = renderer.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (value instanceof RowData && c instanceof JLabel) {
      updateLabel(table, (RowData) value, row, column, (JLabel) c);
    }
    return c;
  }

  private void updateLabel(JTable table, RowData value, int row, int col, JLabel label) {
    label.setHorizontalAlignment(SwingConstants.LEFT);
    switch (table.convertColumnIndexToModel(col)) {
      case 0:
        String str = value.getGroup();
        if (row > 0) {
          RowData prev = (RowData) table.getValueAt(row - 1, col);
          if (Objects.equals(prev.getGroup(), str)) {
            label.setText(" ");
            break;
          }
        }
        label.setText("+ " + str);
        break;
      case 1:
        label.setText(value.getName());
        break;
      case 2:
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setText(Integer.toString(value.getCount()));
        break;
      default:
        break;
    }
  }
}

// class RowDataGroupComparator implements Comparator<RowData> {
//   private final int column;
//   protected RowDataGroupComparator(int column) {
//     this.column = column;
//   }
//
//   @SuppressWarnings("unchecked")
//   @Override public int compare(RowData a, RowData b) {
//     if (a == null && b == null) {
//       return 0;
//     } else if (a != null && b == null) {
//       return -1;
//     } else if (a == null && b != null) {
//       return 1;
//     } else {
//       Comparator nullsFirst = Comparator.nullsFirst(Comparator.<Comparable>naturalOrder());
//       int v = Objects.compare(a.getGroup(), b.getGroup(), nullsFirst);
//       if (v == 0) {
//         switch (column) {
//           case 2:
//           return Objects.compare(a.getCount(), b.getCount(), nullsFirst);
//           case 1:
//           return Objects.compare(a.getName(), b.getName(), nullsFirst);
//           case 0:
//           default:
//           return v;
//         }
//       }
//       return v;
//     }
//   }
// }
