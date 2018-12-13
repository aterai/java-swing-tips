// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  public static final int BUTTON_COLUMN = 3;

  private MainPanel() {
    super(new BorderLayout());

    RowDataModel model = new RowDataModel();
    model.addRowData(new RowData("Name 1", "Comment..."));
    model.addRowData(new RowData("Name 2", "Test"));
    model.addRowData(new RowData("Name d", "ee"));
    model.addRowData(new RowData("Name c", "Test cc"));
    model.addRowData(new RowData("Name b", "Test bb"));
    model.addRowData(new RowData("Name a", "ff"));
    model.addRowData(new RowData("Name 0", "Test aa"));
    model.addRowData(new RowData("Name 0", "gg"));

    JTable table = new JTable(model) {
      @Override public void updateUI() {
        super.updateUI();
        TableColumn col = getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        col = getColumnModel().getColumn(BUTTON_COLUMN);
        col.setCellRenderer(new DeleteButtonRenderer());
        col.setCellEditor(new DeleteButtonEditor());
        col.setMinWidth(20);
        col.setMaxWidth(20);
        col.setResizable(false);
      }
      // @Override public int rowAtPoint(Point pt) {
      //   // [JDK-6291631] JTable: rowAtPoint returns 0 for negative y - Java Bug System
      //   // https://bugs.openjdk.java.net/browse/JDK-6291631
      //   return pt.y < 0 ? -1 : super.rowAtPoint(pt);
      // }
    };

    TableRowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
    table.setRowSorter(sorter);
    sorter.setSortable(BUTTON_COLUMN, false);

    JButton button = new JButton("add");
    button.addActionListener(e -> model.addRowData(new RowData("Test", "aaaaaaaaaaa")));

    add(button, BorderLayout.SOUTH);
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

class RowDataModel extends DefaultTableModel {
  private static final ColumnContext[] COLUMN_ARRAY = {
    new ColumnContext("No.", Integer.class, false),
    new ColumnContext("Name", String.class, true),
    new ColumnContext("Comment", String.class, true),
    new ColumnContext("", String.class, true)
  };
  private int number;

  public void addRowData(RowData t) {
    Object[] obj = {number, t.getName(), t.getComment(), ""};
    super.addRow(obj);
    number++;
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
  // public void setName(String str) {
  //   name = str;
  // }
  // public void setComment(String str) {
  //   comment = str;
  // }

  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }
}

// https://community.oracle.com/thread/1357722 JButton inside JTable Cell
class DeleteButton extends JButton {
  @Override public void updateUI() {
    super.updateUI();
    setBorder(BorderFactory.createEmptyBorder());
    setFocusable(false);
    setRolloverEnabled(false);
    setText("X");
  }
}

// delegation pattern
class DeleteButtonRenderer implements TableCellRenderer {
  private final DeleteButton renderer = new DeleteButton();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    return renderer;
  }
}

class DeleteButtonEditor extends AbstractCellEditor implements TableCellEditor {
  private final DeleteButton renderer = new DeleteButton();

  protected DeleteButtonEditor() {
    super();
    renderer.addActionListener(e -> {
      Object o = SwingUtilities.getAncestorOfClass(JTable.class, renderer);
      if (o instanceof JTable) {
        JTable table = (JTable) o;
        int row = table.convertRowIndexToModel(table.getEditingRow());
        fireEditingStopped();
        ((DefaultTableModel) table.getModel()).removeRow(row);
      }
    });
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    return renderer;
  }

  @Override public Object getCellEditorValue() {
    return "";
  }
}
