// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class HeaderCheckBoxHandler extends MouseAdapter implements TableModelListener {
  private final JTable table;
  private final int targetColumnIndex;

  public HeaderCheckBoxHandler(JTable table, int index) {
    super();
    this.table = table;
    this.targetColumnIndex = index;
  }

  @Override public void tableChanged(TableModelEvent e) {
    int col = e.getColumn();
    boolean targetChanged = col == targetColumnIndex || col == TableModelEvent.ALL_COLUMNS;
    if (targetChanged && e.getFirstRow() != TableModelEvent.HEADER_ROW) {
      int vci = table.convertColumnIndexToView(targetColumnIndex);
      if (vci >= 0) {
        TableColumn column = table.getColumnModel().getColumn(vci);
        Object value = column.getHeaderValue();
        Status status = value instanceof Status ? (Status) value : Status.INDETERMINATE;
        Status newStatus = e.getType() == TableModelEvent.DELETE
            ? getStatusAfterDelete(status)
            : getStatusAfterChange(status, e);
        setHeaderStatus(vci, newStatus);
      }
    }
  }

  /* default */ void updateHeaderState() {
    int vci = table.convertColumnIndexToView(targetColumnIndex);
    if (vci >= 0) {
      TableModel m = table.getModel();
      setHeaderStatus(vci, resolveStatus(m, 0, m.getRowCount() - 1));
    }
  }

  private void setHeaderStatus(int vci, Status status) {
    TableColumn column = table.getColumnModel().getColumn(vci);
    if (column.getHeaderValue() != status) {
      column.setHeaderValue(status);
      JTableHeader h = table.getTableHeader();
      h.repaint(h.getHeaderRect(vci));
    }
  }

  // TableModelEvent.DELETE: the deleted rows no longer exist in the model
  private Status getStatusAfterDelete(Status status) {
    TableModel m = table.getModel();
    int rowCount = m.getRowCount();
    Status newStatus;
    if (rowCount == 0) {
      newStatus = Status.DESELECTED;
    } else if (status == Status.INDETERMINATE) {
      newStatus = resolveStatus(m, 0, rowCount - 1);
    } else {
      // Deleting rows from a uniform column does not change its state
      newStatus = status;
    }
    return newStatus;
  }

  // TableModelEvent.INSERT or TableModelEvent.UPDATE
  private Status getStatusAfterChange(Status status, TableModelEvent e) {
    TableModel m = table.getModel();
    int lastIndex = m.getRowCount() - 1;
    int firstRow = Math.max(0, e.getFirstRow());
    // fireTableDataChanged() sets lastRow to Integer.MAX_VALUE
    int lastRow = Math.min(e.getLastRow(), lastIndex);
    boolean allRowsChanged = firstRow == 0 && lastRow == lastIndex;
    boolean isUpdate = e.getType() == TableModelEvent.UPDATE;
    Status newStatus;
    if (allRowsChanged || isUpdate && status == Status.INDETERMINATE) {
      newStatus = resolveStatus(m, 0, lastIndex);
    } else {
      // The unchanged rows are uniform (or already mixed if INDETERMINATE),
      // so only the changed rows need to be checked
      Status changed = resolveStatus(m, firstRow, lastRow);
      newStatus = changed == status ? status : Status.INDETERMINATE;
    }
    return newStatus;
  }

  private Status resolveStatus(TableModel m, int firstRow, int lastRow) {
    List<Boolean> values = IntStream.rangeClosed(firstRow, lastRow)
        .mapToObj(i -> Objects.equals(m.getValueAt(i, targetColumnIndex), true))
        .distinct()
        .limit(2)
        .collect(Collectors.toList()); // Java 16: .toList();
    Status status;
    if (values.isEmpty()) {
      status = Status.DESELECTED;
    } else {
      boolean isUniform = values.size() == 1;
      if (isUniform) {
        boolean isSelected = values.get(0); // Java 21: values.getFirst();
        status = isSelected ? Status.SELECTED : Status.DESELECTED;
      } else {
        status = Status.INDETERMINATE;
      }
    }
    return status;
  }

  @Override public void mouseClicked(MouseEvent e) {
    JTableHeader header = (JTableHeader) e.getComponent();
    TableModel model = table.getModel();
    int vci = header.columnAtPoint(e.getPoint());
    int mci = table.convertColumnIndexToModel(vci);
    if (header.isEnabled() && mci == targetColumnIndex && model.getRowCount() > 0) {
      TableColumn column = table.getColumnModel().getColumn(vci);
      boolean selected = column.getHeaderValue() == Status.DESELECTED;
      setAllValues(model, selected);
      setHeaderStatus(vci, selected ? Status.SELECTED : Status.DESELECTED);
    }
  }

  private void setAllValues(TableModel model, boolean selected) {
    // Suppress the header state check for each row while updating all rows
    model.removeTableModelListener(this);
    try {
      for (int i = 0; i < model.getRowCount(); i++) {
        model.setValueAt(selected, i, targetColumnIndex);
      }
    } finally {
      model.addTableModelListener(this);
    }
  }
}
