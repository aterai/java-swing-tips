// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
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
    int vci = table.convertColumnIndexToView(targetColumnIndex);
    TableColumn column = table.getColumnModel().getColumn(vci);
    Object status = column.getHeaderValue();
    TableModel m = table.getModel();
    boolean repaint = false;
    if (e.getType() == TableModelEvent.DELETE) {
      // System.out.println("DELETE");
      // System.out.println(status + ":   " + Status.INDETERMINATE.equals(status));
      repaint = fireDeleteEvent(m, column, status);
    } else if (e.getType() == TableModelEvent.INSERT && status != Status.INDETERMINATE) {
      // System.out.println("INSERT");
      repaint = fireInsertEvent(m, column, status, e);
    } else if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == targetColumnIndex) {
      // System.out.println("UPDATE");
      repaint = fireUpdateEvent(m, column, status);
    }
    if (repaint) {
      JTableHeader h = table.getTableHeader();
      h.repaint(h.getHeaderRect(vci));
    }
  }

  private boolean fireDeleteEvent(TableModel m, TableColumn column, Object status) {
    boolean repaint = true;
    if (m.getRowCount() == 0) {
      column.setHeaderValue(Status.DESELECTED);
    } else if (status == Status.INDETERMINATE) {
      boolean selected = true;
      boolean deselected = true;
      for (int i = 0; i < m.getRowCount(); i++) {
        Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
        selected &= b;
        deselected &= !b;
      }
      // System.out.println(selected);
      // System.out.println(deselected);
      if (deselected) {
        column.setHeaderValue(Status.DESELECTED);
      } else if (selected) {
        column.setHeaderValue(Status.SELECTED);
      } else {
        repaint = false;
      }
    }
    return repaint;
  }

  private boolean fireInsertEvent(
      TableModel m, TableColumn column, Object status, TableModelEvent e) {
    boolean repaint = true;
    boolean selected = status == Status.DESELECTED;
    boolean deselected = status == Status.SELECTED;
    for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
      Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
      selected &= b;
      deselected &= !b;
    }
    if (selected && m.getRowCount() == 1) {
      column.setHeaderValue(Status.SELECTED);
    } else if (selected || deselected) {
      column.setHeaderValue(Status.INDETERMINATE);
    } else {
      repaint = false;
    }
    return repaint;
  }

  private boolean fireUpdateEvent(TableModel m, TableColumn column, Object status) {
    boolean repaint = true;
    if (status == Status.INDETERMINATE) {
      boolean selected = true;
      boolean deselected = true;
      for (int i = 0; i < m.getRowCount(); i++) {
        Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
        selected &= b;
        deselected &= !b;
        if (selected == deselected) {
          break;
        }
      }
      if (selected == deselected) {
        repaint = false;
      } else if (deselected) {
        column.setHeaderValue(Status.DESELECTED);
      } else {
        column.setHeaderValue(Status.SELECTED);
      }
    } else {
      column.setHeaderValue(Status.INDETERMINATE);
    }
    return repaint;
  }

  @Override public void mouseClicked(MouseEvent e) {
    JTableHeader header = (JTableHeader) e.getComponent();
    JTable tbl = header.getTable();
    TableColumnModel columnModel = tbl.getColumnModel();
    TableModel m = tbl.getModel();
    int vci = columnModel.getColumnIndexAtX(e.getX());
    int mci = tbl.convertColumnIndexToModel(vci);
    if (mci == targetColumnIndex && m.getRowCount() > 0) {
      TableColumn column = columnModel.getColumn(vci);
      boolean b = column.getHeaderValue() == Status.DESELECTED;
      for (int i = 0; i < m.getRowCount(); i++) {
        m.setValueAt(b, i, mci);
      }
      column.setHeaderValue(b ? Status.SELECTED : Status.DESELECTED);
      // header.repaint();
    }
  }
}
