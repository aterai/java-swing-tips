package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.event.*;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class HeaderCheckBoxHandler extends MouseAdapter implements TableModelListener {
    private final JTable table;
    private final int targetColumnIndex;
    public HeaderCheckBoxHandler(JTable table, int index) {
        super();
        this.table = table;
        this.targetColumnIndex = index;
    }
    @Override public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == targetColumnIndex) {
            int vci = table.convertColumnIndexToView(targetColumnIndex);
            TableColumn column = table.getColumnModel().getColumn(vci);
            Object status = column.getHeaderValue();
            TableModel m = table.getModel();
            if (m instanceof DefaultTableModel && fireUpdateEvent((DefaultTableModel) m, column, status)) {
                JTableHeader h = table.getTableHeader();
                h.repaint(h.getHeaderRect(vci));
            }
        }
    }
    @SuppressWarnings("PMD.ReplaceVectorWithList")
    private boolean fireUpdateEvent(DefaultTableModel m, TableColumn column, Object status) {
        if (Status.INDETERMINATE.equals(status)) {
            List<Boolean> l = ((Vector<?>) m.getDataVector()).stream()
                .map(v -> (Boolean) ((Vector<?>) v).get(targetColumnIndex))
                .distinct()
                .collect(Collectors.toList());
            boolean isOnlyOneSelected = l.size() == 1;
            if (isOnlyOneSelected) {
                column.setHeaderValue(l.get(0) ? Status.SELECTED : Status.DESELECTED);
                return true;
            } else {
                return false;
            }
        } else {
            column.setHeaderValue(Status.INDETERMINATE);
            return true;
        }
    }
//     private boolean fireUpdateEvent(TableModel m, TableColumn column, Object status) {
//         if (Status.INDETERMINATE.equals(status)) {
//             boolean selected = true;
//             boolean deselected = true;
//             for (int i = 0; i < m.getRowCount(); i++) {
//                 Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
//                 selected &= b;
//                 deselected &= !b;
//                 if (selected == deselected) {
//                     return false;
//                 }
//             }
//             if (deselected) {
//                 column.setHeaderValue(Status.DESELECTED);
//             } else if (selected) {
//                 column.setHeaderValue(Status.SELECTED);
//             } else {
//                 return false;
//             }
//         } else {
//             column.setHeaderValue(Status.INDETERMINATE);
//         }
//         return true;
//     }
    @Override public void mouseClicked(MouseEvent e) {
        JTableHeader header = (JTableHeader) e.getComponent();
        JTable table = header.getTable();
        TableColumnModel columnModel = table.getColumnModel();
        TableModel m = table.getModel();
        int vci = columnModel.getColumnIndexAtX(e.getX());
        int mci = table.convertColumnIndexToModel(vci);
        if (mci == targetColumnIndex && m.getRowCount() > 0) {
            TableColumn column = columnModel.getColumn(vci);
            Object v = column.getHeaderValue();
            boolean b = Status.DESELECTED.equals(v);
            for (int i = 0; i < m.getRowCount(); i++) {
                m.setValueAt(b, i, mci);
            }
            column.setHeaderValue(b ? Status.SELECTED : Status.DESELECTED);
            //header.repaint();
        }
    }
}
