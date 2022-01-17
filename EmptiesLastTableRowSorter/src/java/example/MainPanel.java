package example;

import java.awt.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] empty = {"", ""};
    String[] columnNames = {"DefaultTableRowSorter", "EmptiesLastTableRowSorter"};
    Object[][] data = {
        {"aaa", "aaa"}, {"ddd", "ddd"},
        {"bbb", "bbb"}, {"eee", "eee"},
        {"ccc", "ddd"}, {"fff", "fff"},
        empty, empty, empty, empty, empty
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return String.class;
      }
    };
    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);
    RowSorter<? extends TableModel> sorter = table.getRowSorter();
    if (sorter instanceof TableRowSorter) {
      RowComparator comparator = new RowComparator(table, 1);
      ((TableRowSorter<? extends TableModel>) sorter).setComparator(1, comparator);
    }

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

class RowComparator implements Comparator<String>, Serializable {
  private static final long serialVersionUID = 1L;
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
