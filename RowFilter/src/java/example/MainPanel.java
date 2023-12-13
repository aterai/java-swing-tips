// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private final JCheckBox check1 = new JCheckBox("!comment.isEmpty()");
  private final JCheckBox check2 = new JCheckBox("idx % 2 == 0");

  private MainPanel() {
    super(new BorderLayout());
    RowDataModel model = makeModel();
    TableRowSorter<? extends RowDataModel> sorter = new TableRowSorter<>(model);
    JTable table = new JTable(model);
    table.setRowSorter(sorter);
    table.setComponentPopupMenu(new TablePopupMenu());
    table.setFillsViewportHeight(true);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    Collection<RowFilter<? super RowDataModel, ? super Integer>> filters = new HashSet<>(2);
    RowFilter<RowDataModel, Integer> filter1 = new RowFilter<RowDataModel, Integer>() {
      @Override public boolean include(Entry<? extends RowDataModel, ? extends Integer> entry) {
        RowDataModel m = entry.getModel();
        RowData rd = m.getRowData(entry.getIdentifier());
        return !rd.getComment().isEmpty();
      }
    };
    RowFilter<RowDataModel, Integer> filter2 = new RowFilter<RowDataModel, Integer>() {
      @Override public boolean include(Entry<? extends RowDataModel, ? extends Integer> entry) {
        return entry.getIdentifier() % 2 == 0;
      }
    };
    // sorter.setRowFilter(RowFilter.andFilter(filters));
    // sorter.setRowFilter(filter1);
    check1.addActionListener(e -> {
      JCheckBox cb = (JCheckBox) e.getSource();
      if (cb.isSelected()) {
        filters.add(filter1);
      } else {
        filters.remove(filter1);
      }
      sorter.setRowFilter(RowFilter.andFilter(filters));
    });
    check2.addActionListener(e -> {
      JCheckBox cb = (JCheckBox) e.getSource();
      if (cb.isSelected()) {
        filters.add(filter2);
      } else {
        filters.remove(filter2);
      }
      sorter.setRowFilter(RowFilter.andFilter(filters));
    });
    Box box = Box.createHorizontalBox();
    box.add(check1);
    box.add(check2);
    add(box, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  public boolean canAddRow() {
    return !check1.isSelected() && !check2.isSelected();
    // return filters.isEmpty();
  }

  private static RowDataModel makeModel() {
    RowDataModel model = new RowDataModel();
    model.addRowData(new RowData("Name 1", "comment..."));
    model.addRowData(new RowData("Name 2", "Test"));
    model.addRowData(new RowData("Name d", ""));
    model.addRowData(new RowData("Name c", "Test cc"));
    model.addRowData(new RowData("Name b", "Test bb"));
    model.addRowData(new RowData("Name a", "ff"));
    model.addRowData(new RowData("Name 0", "Test aa"));
    return model;
  }

  private final class TablePopupMenu extends JPopupMenu {
    private final JMenuItem addMenuItem;
    private final JMenuItem deleteMenuItem;

    /* default */ TablePopupMenu() {
      super();
      addMenuItem = add("add");
      addMenuItem.addActionListener(e -> {
        JTable tbl = (JTable) getInvoker();
        RowDataModel m = (RowDataModel) tbl.getModel();
        m.addRowData(new RowData("example", ""));
      });
      addSeparator();
      deleteMenuItem = add("delete");
      deleteMenuItem.addActionListener(e -> {
        JTable tbl = (JTable) getInvoker();
        DefaultTableModel m = (DefaultTableModel) tbl.getModel();
        int[] selection = tbl.getSelectedRows();
        for (int i = selection.length - 1; i >= 0; i--) {
          m.removeRow(tbl.convertRowIndexToModel(selection[i]));
        }
      });
    }

    @Override public void show(Component c, int x, int y) {
      if (c instanceof JTable) {
        addMenuItem.setEnabled(canAddRow());
        deleteMenuItem.setEnabled(((JTable) c).getSelectedRowCount() > 0);
        super.show(c, x, y);
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

class RowDataModel extends DefaultTableModel {
  private static final ColumnContext[] COLUMN_ARRAY = {
      new ColumnContext("No.", Integer.class, false),
      new ColumnContext("Name", String.class, true),
      new ColumnContext("Comment", String.class, true)
  };
  private int number;

  public void addRowData(RowData t) {
    Object[] obj = {number, t.getName(), t.getComment()};
    super.addRow(obj);
    number++;
  }

  public RowData getRowData(int identifier) {
    String s1 = Objects.toString(getValueAt(identifier, 1));
    String s2 = Objects.toString(getValueAt(identifier, 2));
    return new RowData(s1, s2);
  }

  @Override public boolean isCellEditable(int row, int col) {
    return COLUMN_ARRAY[col].isEditable;
  }

  @Override public Class<?> getColumnClass(int column) {
    return COLUMN_ARRAY[column].columnClass;
  }

  @Override public int getColumnCount() {
    return COLUMN_ARRAY.length;
  }

  @Override public String getColumnName(int column) {
    return COLUMN_ARRAY[column].columnName;
  }

  private static class ColumnContext {
    public final String columnName;
    public final Class<?> columnClass;
    public final boolean isEditable;

    protected ColumnContext(String columnName, Class<?> columnClass, boolean isEditable) {
      this.columnName = columnName;
      this.columnClass = columnClass;
      this.isEditable = isEditable;
    }
  }
}

class RowData {
  private final String name;
  private final String comment;

  protected RowData(String name, String comment) {
    this.name = name;
    this.comment = comment;
  }

  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }
}
