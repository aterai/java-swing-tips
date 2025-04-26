// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    RowDataModel model = makeModel();
    JTable table = new JTable(model) {
      @Override public void updateUI() {
        super.updateUI();
        setRowSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        getTableHeader().setReorderingAllowed(false);
        TableColumn col = getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);
      }
    };

    JCheckBox check = new JCheckBox("display an odd number of rows");
    check.addActionListener(e -> model.filterRows(check.isSelected()));
    add(new JScrollPane(table));
    add(check, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static RowDataModel makeModel() {
    RowDataModel model = new RowDataModel();
    model.addRowData(new RowData("Name 1", "comment..."));
    model.addRowData(new RowData("Name 2", "Test"));
    model.addRowData(new RowData("Name d", "ee"));
    model.addRowData(new RowData("Name c", "Test cc"));
    model.addRowData(new RowData("Name b", "Test bb"));
    model.addRowData(new RowData("Name a", "ff"));
    model.addRowData(new RowData("Name 0", "Test aa"));
    model.addRowData(new RowData("Name 1", "comment..."));
    model.addRowData(new RowData("Name 2", "Test"));
    model.addRowData(new RowData("Name d", "gg"));
    model.addRowData(new RowData("Name c", "Test cc"));
    model.addRowData(new RowData("Name b", "Test bb"));
    model.addRowData(new RowData("Name a", "hh"));
    model.addRowData(new RowData("Name 0", "Test aa"));
    return model;
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
      Logger.getGlobal().severe(ex::getMessage);
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
  private final List<RowData> list = new ArrayList<>();
  private int number;

  public void addRowData(RowData t) {
    Object[] obj = {number, t.getName(), t.getComment()};
    super.addRow(obj);
    number++;
    list.add(t);
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

  public void filterRows(boolean flg) {
    // dataVector.clear();
    setRowCount(0);
    for (int i = 0; i < list.size(); i++) {
      if (flg && i % 2 == 0) {
        continue;
      }
      RowData t = list.get(i);
      Object[] obj = {i, t.getName(), t.getComment()};
      addRow(convertToVector(obj));
      // dataVector.add(convertToVector(obj));
    }
    // setDataVector(v, columnIdentifiers);
    fireTableDataChanged();
  }
}

class RowData implements Serializable {
  private static final long serialVersionUID = 1L;
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
