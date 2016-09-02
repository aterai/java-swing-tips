package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

//package components;

import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

/**
 * TableSorter is a decorator for TableModels; adding sorting
 * functionality to a supplied TableModel. TableSorter does
 * not store or copy the data in its TableModel; instead it maintains
 * a map from the row indexes of the view to the row indexes of the
 * model. As requests are made of the sorter (like getValueAt(row, col))
 * they are passed to the underlying model after the row numbers
 * have been translated via the internal mapping array. This way,
 * the TableSorter appears to hold another copy of the table
 * with the rows in a different order.
 * <p/>
 * TableSorter registers itself as a listener to the underlying model,
 * just as the JTable itself would. Events recieved from the model
 * are examined, sometimes manipulated (typically widened), and then
 * passed on to the TableSorter's listeners (typically the JTable).
 * If a change to the model has invalidated the order of TableSorter's
 * rows, a note of this is made and the sorter will resort the
 * rows the next time a value is requested.
 * <p/>
 * When the tableHeader property is set, either by using the
 * setTableHeader() method or the two argument constructor, the
 * table header may be used as a complete UI for TableSorter.
 * The default renderer of the tableHeader is decorated with a renderer
 * that indicates the sorting status of each column. In addition,
 * a mouse listener is installed with the following behavior:
 * <ul>
 * <li>
 * Mouse-click: Clears the sorting status of all other columns
 * and advances the sorting status of that column through three
 * values: {NOT_SORTED, ASCENDING, DESCENDING} (then back to
 * NOT_SORTED again).
 * <li>
 * SHIFT-mouse-click: Clears the sorting status of all other columns
 * and cycles the sorting status of the column through the same
 * three values, in the opposite order: {NOT_SORTED, DESCENDING, ASCENDING}.
 * <li>
 * CONTROL-mouse-click and CONTROL-SHIFT-mouse-click: as above except
 * that the changes to the column do not cancel the statuses of columns
 * that are already sorting - giving a way to initiate a compound
 * sort.
 * </ul>
 * <p/>
 * This is a long overdue rewrite of a class of the same name that
 * first appeared in the swing table demos in 1997.
 *
 * @author Philip Milne
 * @author Brendon McLean
 * @author Dan van Enckevort
 * @author Parwinder Sekhon
 * @version 2.0 02/27/04
 */
public class TableSorter extends AbstractTableModel {
    public static final int DESCENDING = -1;
    public static final int NOT_SORTED = 0;
    public static final int ASCENDING  = 1;

    private static final Directive EMPTY_DIRECTIVE = new Directive(-1, NOT_SORTED);
    public static final Comparator COMPARABLE_COMAPRATOR = new ComparableComparator();
    public static final Comparator LEXICAL_COMPARATOR = new LexicalComparator();

    protected TableModel tableModel;

    private final List<Row> viewToModel = new ArrayList<>();
    private final List<Integer> modelToView = new ArrayList<>();
    private final List<Directive> sortingColumns = new ArrayList<>();

    private JTableHeader tableHeader;
    private final transient Map<Class, Comparator> columnComparators = new ConcurrentHashMap<>();
    private final transient RowComparator<Row> rowComparator = new RowComparator<>();
    private transient MouseListener mouseListener;
    private transient TableModelListener tableModelListener;

    public void readObject() {
        this.mouseListener = new MouseHandler();
        this.tableModelListener = new TableModelHandler();
    }

    public Object readResolve() {
        this.mouseListener = new MouseHandler();
        this.tableModelListener = new TableModelHandler();
        return this;
    }

    public TableSorter() {
        super();
        this.mouseListener = new MouseHandler();
        this.tableModelListener = new TableModelHandler();
    }

    public TableSorter(TableModel tableModel) {
        this();
        setTableModel(tableModel);
    }

    public TableSorter(TableModel tableModel, JTableHeader tableHeader) {
        this();
        setTableHeader(tableHeader);
        setTableModel(tableModel);
    }

    private void clearSortingState() {
        viewToModel.clear();
        modelToView.clear();
    }

//     public TableModel getTableModel() {
//         return tableModel;
//     }

    public final void setTableModel(TableModel tableModel) {
        if (this.tableModel != null) {
            this.tableModel.removeTableModelListener(tableModelListener);
        }

        this.tableModel = tableModel;
        if (this.tableModel != null) {
            this.tableModel.addTableModelListener(tableModelListener);
        }

        clearSortingState();
        fireTableStructureChanged();
    }

//     public JTableHeader getTableHeader() {
//         return tableHeader;
//     }

    public final void setTableHeader(JTableHeader tableHeader) {
        if (this.tableHeader != null) {
            this.tableHeader.removeMouseListener(mouseListener);
            TableCellRenderer defaultRenderer = this.tableHeader.getDefaultRenderer();
            if (defaultRenderer instanceof SortableHeaderRenderer) {
                this.tableHeader.setDefaultRenderer(((SortableHeaderRenderer) defaultRenderer).tableCellRenderer);
            }
        }
        this.tableHeader = tableHeader;
        if (this.tableHeader != null) {
            this.tableHeader.addMouseListener(mouseListener);
            this.tableHeader.setDefaultRenderer(new SortableHeaderRenderer(this.tableHeader.getDefaultRenderer()));
        }
    }

    public boolean isSorting() {
        return !sortingColumns.isEmpty();
    }

    private Directive getDirective(int column) {
        for (Directive directive: sortingColumns) {
            if (directive.column == column) {
                return directive;
            }
        }
        return EMPTY_DIRECTIVE;
    }

    public int getSortingStatus(int column) {
        return getDirective(column).direction;
    }

    private void sortingStatusChanged() {
        clearSortingState();
        fireTableDataChanged();
        if (tableHeader != null) {
            tableHeader.repaint();
        }
    }

    public void setSortingStatus(int column, int status) {
        Directive directive = getDirective(column);
        if (!EMPTY_DIRECTIVE.equals(directive)) {
            sortingColumns.remove(directive);
        }
        if (status != NOT_SORTED) {
            sortingColumns.add(new Directive(column, status));
        }
        sortingStatusChanged();
    }

    public Icon getHeaderRendererIcon(int column, int size) {
        Directive directive = getDirective(column);
        if (EMPTY_DIRECTIVE.equals(directive)) {
            return null;
        }
        return new Arrow(directive.direction == DESCENDING, size, sortingColumns.indexOf(directive));
    }

    private void cancelSorting() {
        sortingColumns.clear();
        sortingStatusChanged();
    }

    public void setColumnComparator(Class type, Comparator comparator) {
        if (comparator == null) {
            columnComparators.remove(type);
        } else {
            columnComparators.put(type, comparator);
        }
    }

    protected Comparator getComparator(int column) {
        Class columnType = tableModel.getColumnClass(column);
        Comparator comparator = columnComparators.get(columnType);
        if (comparator != null) {
            return comparator;
        }
        if (Comparable.class.isAssignableFrom(columnType)) {
            return COMPARABLE_COMAPRATOR;
        }
        return LEXICAL_COMPARATOR;
    }

    private List<Row> getViewToModel() {
        if (viewToModel.isEmpty()) {
            int tableModelRowCount = tableModel.getRowCount();
            //viewToModel = new Row[tableModelRowCount];
            for (int row = 0; row < tableModelRowCount; row++) {
                viewToModel.add(new Row(row));
            }
            if (isSorting()) {
                Collections.sort(viewToModel, rowComparator);
            }
        }
        return viewToModel;
    }

    public int modelIndex(int viewIndex) {
        return getViewToModel().get(viewIndex).modelIndex;
    }

    private List<Integer> getModelToView() {
        if (modelToView.isEmpty()) {
            int n = getViewToModel().size();
            for (int i = 0; i < n; i++) {
                modelToView.add(modelIndex(i));
            }
        }
        return modelToView;
    }

    // TableModel interface methods

    @Override public int getRowCount() {
        return (tableModel == null) ? 0 : tableModel.getRowCount();
    }

    @Override public int getColumnCount() {
        return (tableModel == null) ? 0 : tableModel.getColumnCount();
    }

    @Override public String getColumnName(int column) {
        return tableModel.getColumnName(column);
    }

    @Override public Class<?> getColumnClass(int column) {
        return tableModel.getColumnClass(column);
    }

    @Override public boolean isCellEditable(int row, int column) {
        return tableModel.isCellEditable(modelIndex(row), column);
    }

    @Override public Object getValueAt(int row, int column) {
        return tableModel.getValueAt(modelIndex(row), column);
    }

    @Override public void setValueAt(Object aValue, int row, int column) {
        tableModel.setValueAt(aValue, modelIndex(row), column);
    }

    // Helper classes
    private class RowComparator<E extends Row> implements Comparator<E> {
        @Override public int compare(Row r1, Row r2) {
            int row1 = r1.modelIndex;
            int row2 = r2.modelIndex;
            for (Directive directive: sortingColumns) {
                int column = directive.column;
                Object o1 = tableModel.getValueAt(row1, column);
                Object o2 = tableModel.getValueAt(row2, column);
//                 int comparison;
//                 // Define null less than everything, except null.
//                 if (o1 == null && o2 == null) {
//                     comparison = 0;
//                 } else if (o1 == null) {
//                     comparison = -1;
//                 } else if (o2 == null) {
//                     comparison = 1;
//                 } else {
//                     @SuppressWarnings("unchecked")
//                     Comparator<Object> comparator = getComparator(column);
//                     comparison = comparator.compare(o1, o2);
//                 }
//                 if (comparison != 0) {
//                     return directive.direction == DESCENDING ? -comparison : comparison;
//                 }
                @SuppressWarnings("unchecked")
                Comparator<Object> comparator = getComparator(column);
                int comparison = Objects.compare(o1, o2, Comparator.nullsFirst(comparator));
                if (comparison != 0) {
                    return directive.direction == DESCENDING ? ~comparison + 1 : comparison;
                }
            }
            return row1 - row2;
        }
    }

    private class TableModelHandler implements TableModelListener {
        @Override public void tableChanged(TableModelEvent e) {
            // If we're not sorting by anything, just pass the event along.
            if (!isSorting()) {
                clearSortingState();
                fireTableChanged(e);
                return;
            }

            // If the table structure has changed, cancel the sorting; the
            // sorting columns may have been either moved or deleted from
            // the model.
            if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                cancelSorting();
                fireTableChanged(e);
                return;
            }

            // We can map a cell event through to the view without widening
            // when the following conditions apply:
            //
            // a) all the changes are on one row (e.getFirstRow() == e.getLastRow()) and,
            // b) all the changes are in one column (column != TableModelEvent.ALL_COLUMNS) and,
            // c) we are not sorting on that column (getSortingStatus(column) == NOT_SORTED) and,
            // d) a reverse lookup will not trigger a sort (modelToView != null)
            //
            // Note: INSERT and DELETE events fail this test as they have column == ALL_COLUMNS.
            //
            // The last check, for (modelToView != null) is to see if modelToView
            // is already allocated. If we don't do this check; sorting can become
            // a performance bottleneck for applications where cells
            // change rapidly in different parts of the table. If cells
            // change alternately in the sorting column and then outside of
            // it this class can end up re-sorting on alternate cell updates -
            // which can be a performance problem for large tables. The last
            // clause avoids this problem.
            int column = e.getColumn();
            if (e.getFirstRow() == e.getLastRow() && column != TableModelEvent.ALL_COLUMNS && getSortingStatus(column) == NOT_SORTED && modelToView != null) {
                int viewIndex = getModelToView().get(e.getFirstRow());
                fireTableChanged(new TableModelEvent(TableSorter.this, viewIndex, viewIndex, column, e.getType()));
                return;
            }

            // Something has happened to the data that may have invalidated the row order.
            clearSortingState();
            fireTableDataChanged();
            //return;
        }
    }

    private class MouseHandler extends MouseAdapter {
        @Override public void mouseClicked(MouseEvent e) {
            JTableHeader h = (JTableHeader) e.getComponent();
            TableColumnModel columnModel = h.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            // ArrayIndexOutOfBoundsException: -1
            if (viewColumn < 0) {
                return;
            }
            int column = columnModel.getColumn(viewColumn).getModelIndex();
            if (column != -1) {
                JTable t = h.getTable();
                int keyCol = 0;
                //List<?> list = saveSelectedRow(t, keyCol);
                List<Object> list = new ArrayList<>();
                int[] ilist = t.getSelectedRows();
                for (int i = ilist.length - 1; i >= 0; i--) {
                    list.add(tableModel.getValueAt(modelIndex(ilist[i]), keyCol));
                }
                int status = getSortingStatus(column) + (e.isShiftDown() ? -1 : 1);
                if (!e.isControlDown()) {
                    cancelSorting();
                }
                // Cycle the sorting states through {NOT_SORTED, ASCENDING, DESCENDING} or
                // {NOT_SORTED, DESCENDING, ASCENDING} depending on whether shift is pressed.
                //int d = e.isShiftDown() ? -1 : 1;
                //status = status + d;
                status = (status + 4) % 3 - 1; // signed mod, returning {-1, 0, 1}
                setSortingStatus(column, status);
                loadSelectedRow(t, list, keyCol);
            }
        }
//         private List<?> saveSelectedRow(JTable table, int keyColIndex) {
//             List<Object> list = new ArrayList<>();
//             int[] ilist = table.getSelectedRows();
//             DefaultTableModel model = (DefaultTableModel) tableModel;
//             for (int i = ilist.length - 1; i >= 0; i--) {
//                 list.add(model.getValueAt(modelIndex(ilist[i]), keyColIndex));
//             }
//             return list;
//         }
        private void loadSelectedRow(JTable table, List<?> list, int keyColIndex) {
            if (list == null || list.isEmpty()) {
                return;
            }
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (list.contains(tableModel.getValueAt(modelIndex(i), keyColIndex))) {
                    table.getSelectionModel().addSelectionInterval(i, i);
                }
            }
        }
    }
}

class SortableHeaderRenderer implements TableCellRenderer {
    protected final TableCellRenderer tableCellRenderer;
    protected SortableHeaderRenderer(TableCellRenderer tableCellRenderer) {
        this.tableCellRenderer = tableCellRenderer;
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel) {
            JLabel l = (JLabel) c;
            l.setHorizontalTextPosition(SwingConstants.LEFT);
            int modelColumn = table.convertColumnIndexToModel(column);
            TableModel model = table.getModel();
            if (model instanceof TableSorter) {
                TableSorter ts = (TableSorter) model;
                l.setIcon(ts.getHeaderRendererIcon(modelColumn, l.getFont().getSize()));
            }
        }
        return c;
    }
}

class ComparableComparator<T extends Comparable<? super T>> implements Comparator<T>, Serializable {
    private static final long serialVersionUID = 1L;
    @Override public int compare(T c1, T c2) {
        return c1.compareTo(c2);
    }
}

class LexicalComparator implements Comparator<Object>, Serializable {
    private static final long serialVersionUID = 1L;
    @Override public int compare(Object o1, Object o2) {
        return o1.toString().compareTo(o2.toString());
    }
}

class Arrow implements Icon, Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean descending;
    private final int size;
    private final int priority;

    protected Arrow(boolean descending, int size, int priority) {
        this.descending = descending;
        this.size = size;
        this.priority = priority;
    }

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Color color1 = Objects.nonNull(c) ? c.getBackground() : Color.GRAY;
        Color color2;
        // In a compound sort, make each succesive triangle 20%
        // smaller than the previous one.
        int dx = (int) (size / 2d * Math.pow(.8, priority));
        int dy;
        int d;
        int shift;

        if (descending) {
            color2 = color1.darker().darker();
            shift = 1;
            dy = dx;
            d = -dy; // Align icon (roughly) with font baseline.
        } else {
            color2 = color1.brighter().brighter();
            shift = -1;
            dy = -dx;
            d = 0; // Align icon (roughly) with font baseline.
        }
        int ty = y + 5 * size / 6 + d;

        g.translate(x, ty);

        // Right diagonal.
        g.setColor(color1.darker());
        g.drawLine(dx / 2, dy, 0, 0);
        g.drawLine(dx / 2, dy + shift, 0, shift);

        // Left diagonal.
        g.setColor(color1.brighter());
        g.drawLine(dx / 2, dy, dx, 0);
        g.drawLine(dx / 2, dy + shift, dx, shift);

        // Horizontal line.
        g.setColor(color1);
        g.drawLine(dx, 0, 0, 0);

        g.setColor(color2);
        g.translate(-x, -ty);
    }

    @Override public int getIconWidth() {
        return size;
    }

    @Override public int getIconHeight() {
        return size;
    }
}

class Directive implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int column;
    public final int direction;
    protected Directive(int column, int direction) {
        this.column = column;
        this.direction = direction;
    }
}

class Row implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int modelIndex;
    protected Row(int index) {
        this.modelIndex = index;
    }
}
