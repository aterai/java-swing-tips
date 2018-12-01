package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

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

        String[] columnNames = {"Group", "Name", "Count"};
        DefaultTableModel model = new DefaultTableModel(null, columnNames) {
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

        JTable table = new JTable(model) {
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
    private static void addRowData(DefaultTableModel model, RowData data) {
        model.addRow(Collections.nCopies(model.getColumnCount(), data).toArray());
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
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
        JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        RowData data = (RowData) value;
        switch (table.convertColumnIndexToModel(column)) {
            case 0:
                String str = data.getGroup();
                if (row > 0) {
                    RowData prev = (RowData) table.getValueAt(row - 1, column);
                    if (Objects.equals(prev.getGroup(), str)) {
                        label.setText(" ");
                        break;
                    }
                }
                label.setText("+ " + str);
                break;
            case 1:
                label.setText(data.getName());
                break;
            case 2:
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                label.setText(Integer.toString(data.getCount()));
                break;
            default:
                break;
        }
        return label;
    }
}

// class RowDataGroupComparator implements Comparator<RowData> {
//     private final int column;
//     protected RowDataGroupComparator(int column) {
//         this.column = column;
//     }
//     @SuppressWarnings("unchecked")
//     @Override public int compare(RowData a, RowData b) {
//         if (a == null && b == null) {
//             return 0;
//         } else if (a != null && b == null) {
//             return -1;
//         } else if (a == null && b != null) {
//             return 1;
//         } else {
//             Comparator nullsFirst = Comparator.nullsFirst(Comparator.<Comparable>naturalOrder());
//             int v = Objects.compare(a.getGroup(), b.getGroup(), nullsFirst);
//             if (v == 0) {
//                 switch (column) {
//                   case 2:
//                     return Objects.compare(a.getCount(), b.getCount(), nullsFirst);
//                   case 1:
//                     return Objects.compare(a.getName(), b.getName(), nullsFirst);
//                   case 0:
//                   default:
//                     return v;
//                 }
//             }
//             return v;
//         }
//     }
// }
