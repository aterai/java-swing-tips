// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    table.setFillsViewportHeight(true);
    table.setRowSelectionAllowed(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    table.setComponentPopupMenu(new TablePopupMenu(table));

    JTableHeader header = table.getTableHeader();
    header.setDefaultRenderer(new SortButtonRenderer());
    header.addMouseListener(new HeaderMouseListener());
    // header.setReorderingAllowed(false);

    TableColumn col = table.getColumnModel().getColumn(0);
    col.setMinWidth(80);
    col.setMaxWidth(80);

    add(new JScrollPane(table));
    add(makeToolBar(table), BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    RowDataModel model = new RowDataModel();
    model.addRowData(new RowData("Name 1", "comment..."));
    model.addRowData(new RowData("Name 2", "Test"));
    model.addRowData(new RowData("Name d", "ee"));
    model.addRowData(new RowData("Name c", "Test cc"));
    model.addRowData(new RowData("Name b", "Test bb"));
    model.addRowData(new RowData("Name a", "ff"));
    model.addRowData(new RowData("Name 0", "Test aa"));
    return model;
  }

  private static JToolBar makeToolBar(JTable table) {
    JToolBar tb = new JToolBar();
    tb.setFloatable(true);
    tb.add(makeToolButton(new UpAction("▲", table)));
    tb.add(makeToolButton(new DownAction("▼", table)));
    tb.add(Box.createHorizontalGlue());
    tb.add(makeToolButton(new InitAction("OK", table)));
    return tb;
  }

  private static JButton makeToolButton(Action action) {
    JButton b = new JButton(action);
    b.setFocusable(false);
    return b;
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

final class TablePopupMenu extends JPopupMenu {
  private final transient Action createAction;
  private final transient Action deleteAction;
  private final transient Action upAction;
  private final transient Action downAction;
  private final JTable table;

  /* default */ TablePopupMenu(JTable table) {
    super();
    this.table = table;

    createAction = new RowDataCreateAction("add", table);
    deleteAction = new DeleteAction("delete", table);
    upAction = new UpAction("up", table);
    downAction = new DownAction("down", table);

    add(createAction);
    addSeparator();
    add(deleteAction);
    addSeparator();
    add(upAction);
    add(downAction);
  }

  @Override public void show(Component c, int x, int y) {
    int row = table.rowAtPoint(new Point(x, y));
    int count = table.getSelectedRowCount();
    // int[] l = table.getSelectedRows();
    // boolean flg = true;
    // for (int i = 0; i < l.length; i++) {
    //   if (l[i] == row) {
    //     flg = false;
    //     break;
    //   }
    // }
    // if (row > 0 && flg) {
    //   table.setRowSelectionInterval(row, row);
    // }
    boolean flg = Arrays.stream(table.getSelectedRows()).anyMatch(i -> i == row);
    // Java 9: boolean flg = List.of(table.getSelectedRows()).contains(row);
    if (row > 0 && !flg) {
      table.setRowSelectionInterval(row, row);
    }

    createAction.setEnabled(count <= 1);
    deleteAction.setEnabled(row >= 0);
    upAction.setEnabled(count > 0);
    downAction.setEnabled(count > 0);

    super.show(c, x, y);
  }
}

class RowDataCreateAction extends AbstractAction {
  private final JTable table;

  protected RowDataCreateAction(String str, JTable table) {
    super(str);
    this.table = table;
  }

  @Override public void actionPerformed(ActionEvent e) {
    if (table.isEditing()) {
      table.getCellEditor().stopCellEditing();
    }
    RowDataModel model = (RowDataModel) table.getModel();
    model.addRowData(new RowData("New row", ""));
    Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
    table.scrollRectToVisible(r);
  }
}

class DeleteAction extends AbstractAction {
  private final JTable table;

  protected DeleteAction(String str, JTable table) {
    super(str);
    this.table = table;
  }

  @Override public void actionPerformed(ActionEvent e) {
    if (table.isEditing()) {
      table.getCellEditor().stopCellEditing();
    }
    int[] selection = table.getSelectedRows();
    RowDataModel model = (RowDataModel) table.getModel();
    for (int i = selection.length - 1; i >= 0; i--) {
      // RowData isc = model.getRowData(selection[i]);
      model.removeRow(selection[i]);
    }
  }
}

class UpAction extends AbstractAction {
  private final JTable table;

  protected UpAction(String str, JTable table) {
    super(str);
    this.table = table;
  }

  @Override public void actionPerformed(ActionEvent e) {
    if (table.isEditing()) {
      table.getCellEditor().stopCellEditing();
    }
    TableModel model = table.getModel();
    int[] pos = table.getSelectedRows();
    if (model instanceof RowDataModel && pos.length != 0) {
      boolean isShiftDown = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
      upTo((RowDataModel) model, pos, isShiftDown);
    }
  }

  private void upTo(RowDataModel model, int[] pos, boolean isShiftDown) {
    if (isShiftDown) { // Jump to the top
      model.moveRow(pos[0], pos[pos.length - 1], 0);
      table.setRowSelectionInterval(0, pos.length - 1);
    } else {
      if (pos[0] == 0) {
        return;
      }
      model.moveRow(pos[0], pos[pos.length - 1], pos[0] - 1);
      table.setRowSelectionInterval(pos[0] - 1, pos[pos.length - 1] - 1);
    }
    Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
    table.scrollRectToVisible(r);
  }
}

class DownAction extends AbstractAction {
  private final JTable table;

  protected DownAction(String str, JTable table) {
    super(str);
    this.table = table;
  }

  @Override public void actionPerformed(ActionEvent e) {
    if (table.isEditing()) {
      table.getCellEditor().stopCellEditing();
    }
    TableModel model = table.getModel();
    int[] pos = table.getSelectedRows();
    if (model instanceof RowDataModel && pos.length != 0) {
      boolean isShiftDown = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
      downTo((RowDataModel) model, pos, isShiftDown);
    }
  }

  private void downTo(RowDataModel model, int[] pos, boolean isShiftDown) {
    if (isShiftDown) { // Jump to the end
      model.moveRow(pos[0], pos[pos.length - 1], model.getRowCount() - pos.length);
      table.setRowSelectionInterval(model.getRowCount() - pos.length, model.getRowCount() - 1);
    } else {
      if (pos[pos.length - 1] == model.getRowCount() - 1) {
        return;
      }
      model.moveRow(pos[0], pos[pos.length - 1], pos[0] + 1);
      table.setRowSelectionInterval(pos[0] + 1, pos[pos.length - 1] + 1);
    }
    Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
    table.scrollRectToVisible(r);
  }
}

class InitAction extends AbstractAction {
  private final JTable table;

  protected InitAction(String str, JTable table) {
    super(str);
    this.table = table;
  }

  @Override public void actionPerformed(ActionEvent e) {
    if (table.isEditing()) {
      table.getCellEditor().stopCellEditing();
    }
    int row = table.getRowCount();
    if (row <= 0) {
      return;
    }
    RowDataModel model = (RowDataModel) table.getModel();
    RowDataModel currentModel = new RowDataModel();
    List<?> dv = model.getDataVector();
    for (int i = 0; i < row; i++) {
      currentModel.addRowData(makeRowData((List<?>) dv.get(i)));
    }
    JTableHeader h = table.getTableHeader();
    TableCellRenderer tcr = h.getDefaultRenderer();
    if (tcr instanceof SortButtonRenderer) {
      SortButtonRenderer sbr = (SortButtonRenderer) tcr;
      sbr.setPressedColumn(-1);
      sbr.setSelectedColumn(-1);
    }
    table.setAutoCreateColumnsFromModel(false);
    table.setModel(currentModel);
    table.clearSelection();
  }

  private static RowData makeRowData(List<?> list) {
    return new RowData(Objects.toString(list.get(1)), Objects.toString(list.get(2)));
  }
}

class RowDataModel extends SortableTableModel {
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
