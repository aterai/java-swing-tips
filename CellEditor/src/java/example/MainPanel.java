// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JCheckBox modelCheck = new JCheckBox("isCellEditable return false");
    RowDataModel model = new RowDataModel() {
      @Override public boolean isCellEditable(int row, int col) {
        return !modelCheck.isSelected();
      }
    };
    model.addRowData(new RowData("Name 1", "Comment"));
    model.addRowData(new RowData("Name 2", "Test"));
    model.addRowData(new RowData("Name d", "ee"));
    model.addRowData(new RowData("Name c", "Test cc"));
    model.addRowData(new RowData("Name b", "Test bb"));
    model.addRowData(new RowData("Name a", "ff"));
    model.addRowData(new RowData("Name 0", "Test aa"));
    model.addRowData(new RowData("Name 0", "gg"));

    JTable table = new JTable(model);
    TableColumn col = table.getColumnModel().getColumn(0);
    col.setMinWidth(50);
    col.setMaxWidth(50);
    col.setResizable(false);

    DefaultCellEditor dce = new DefaultCellEditor(new JTextField());
    JCheckBox objectCheck = new JCheckBox("setDefaultEditor(Object.class, null)");
    JCheckBox editableCheck = new JCheckBox("setEnabled(false)");
    ActionListener al = e -> {
      table.clearSelection();
      if (table.isEditing()) {
        table.getCellEditor().stopCellEditing();
      }
      table.setDefaultEditor(Object.class, objectCheck.isSelected() ? null : dce);
      table.setEnabled(!editableCheck.isSelected());
    };
    JPanel p = new JPanel(new GridLayout(3, 1));
    Stream.of(modelCheck, objectCheck, editableCheck).forEach(cb -> {
      cb.addActionListener(al);
      p.add(cb);
    });
    add(p, BorderLayout.NORTH);
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
  private String name;
  private String comment;

  protected RowData(String name, String comment) {
    this.name = name;
    this.comment = comment;
  }

  public void setName(String str) {
    name = str;
  }

  public void setComment(String str) {
    comment = str;
  }

  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }
}
