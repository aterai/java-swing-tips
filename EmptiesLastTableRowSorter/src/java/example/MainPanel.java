package example;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] empty = {"", "", ""};
    String[] columnNames = {"A", "B", "C"};
    Object[][] data = {
      {"aaa", "fff", "ggg"}, {"jjj", "ppp", "ooo"},
      {"bbb", "eee", "hhh"}, {"kkk", "qqq", "nnn"},
      {"ccc", "ddd", "iii"}, {"lll", "rrr", "mmm"},
      empty, empty, empty, empty, empty, empty
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return String.class;
      }
    };
    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);
    TableRowSorter<? extends TableModel> sorter = (TableRowSorter<? extends TableModel>) table.getRowSorter();
    IntStream.range(0, 3).forEach(i -> sorter.setComparator(i, new RowComparator(table, i)));

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
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

class RowComparator implements Comparator<String> {
  protected final int column;
  private final JTable table;

  protected RowComparator(JTable table, int column) {
    this.table = table;
    this.column = column;
  }

  @Override public int compare(String a, String b) {
    int flag = 1;
    List<? extends RowSorter.SortKey> keys = table.getRowSorter().getSortKeys();
    if (!keys.isEmpty()) {
      RowSorter.SortKey sortKey = keys.get(0);
      if (sortKey.getColumn() == column && sortKey.getSortOrder() == SortOrder.DESCENDING) {
        flag = -1;
      }
    }
    if (a.isEmpty() && !b.isEmpty()) {
      return flag;
    } else if (!a.isEmpty() && b.isEmpty()) {
      return -1 * flag;
    } else {
      return a.compareTo(b);
    }
  }
}
