/**
@version 1.0 02/25/99
@author Nobuo Tamemasa
modified by aterai at.terai@gmail.com
*/
package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.table.*;

public class SortableTableModel extends DefaultTableModel {
    @SuppressWarnings("unchecked")
    public void sortByColumn(int column, boolean isAscent) {
        Collections.sort(getDataVector(), new ColumnComparator(column, isAscent));
        fireTableDataChanged();
    }
}

class ColumnComparator implements Comparator, java.io.Serializable {
    protected final int index;
    protected final boolean ascending;
    public ColumnComparator(int index, boolean ascending) {
        this.index = index;
        this.ascending = ascending;
    }
    @SuppressWarnings("unchecked")
    public int compare(Object one, Object two) {
        if(one instanceof Vector && two instanceof Vector) {
            Object oOne = ((Vector)one).elementAt(index);
            Object oTwo = ((Vector)two).elementAt(index);
            if(oOne==null && oTwo==null) {
                return 0;
            }else if(oOne==null) {
                return ascending ? -1 :  1;
            }else if(oTwo==null) {
                return ascending ?  1 : -1;
            }else if(oOne instanceof Comparable && oTwo instanceof Comparable) {
                Comparable cOne = (Comparable)oOne;
                Comparable cTwo = (Comparable)oTwo;
                return ascending ? cOne.compareTo(cTwo) : cTwo.compareTo(cOne);
            }
        }
        return 1;
    }
    public int compare(Number o1, Number o2) {
        double n1 = o1.doubleValue();
        double n2 = o2.doubleValue();
        if(n1 < n2) {
            return -1;
        }else if(n1 > n2) {
            return 1;
        }else{
            return 0;
        }
    }
}

class SortButtonRenderer extends JButton implements TableCellRenderer {
    public static final int NONE = 0;
    public static final int DOWN = 1;
    public static final int UP   = 2;
    private static Icon ASCENDING_SORT_ICON  = UIManager.getIcon("Table.ascendingSortIcon");
    private static Icon DESCENDING_SORT_ICON = UIManager.getIcon("Table.descendingSortIcon");
    private static Icon NONE_SORT_ICON = new Icon() {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* Empty icon */ }
        @Override public int getIconWidth() {
            return ASCENDING_SORT_ICON.getIconWidth();
        }
        @Override public int getIconHeight() {
            return ASCENDING_SORT_ICON.getIconHeight();
        }
    };
    private int pushedColumn = -1;
    private final ConcurrentMap<Integer, Integer> state = new ConcurrentHashMap<>();

    public SortButtonRenderer() {
        super();
        setHorizontalTextPosition(JButton.LEFT);
        setIcon(NONE_SORT_ICON);
    }

    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(Objects.toString(value, ""));
        setIcon(NONE_SORT_ICON);
        int modelColumn = table.convertColumnIndexToModel(column);
        Integer ivalue = state.get(modelColumn);
        if(ivalue != null) {
            if(ivalue == DOWN) {
                setIcon(ASCENDING_SORT_ICON);
                //setIcon(new BevelArrowIcon(BevelArrowIcon.DOWN, false, false));
                //setPressedIcon(new BevelArrowIcon(BevelArrowIcon.DOWN, false, true));
            }else if(ivalue == UP) {
                setIcon(DESCENDING_SORT_ICON);
                //setIcon(new BevelArrowIcon(BevelArrowIcon.UP, false, false));
                //setPressedIcon(new BevelArrowIcon(BevelArrowIcon.UP, false, true));
            }
        }
        boolean isPressed = modelColumn == pushedColumn;
        getModel().setPressed(isPressed);
        getModel().setArmed(isPressed);
        return this;
    }
    @Override public void updateUI() {
        super.updateUI();
        ASCENDING_SORT_ICON  = UIManager.getIcon("Table.ascendingSortIcon");
        DESCENDING_SORT_ICON = UIManager.getIcon("Table.descendingSortIcon");
    }
    public void setPressedColumn(int col) {
        pushedColumn = col;
    }
    public void setSelectedColumn(int col) {
        if(col < 0) {
            state.clear();
            return;
        }
        Integer value = null;
        Integer obj = state.get(col);
        value = obj != null && obj == DOWN ? UP : DOWN;
        state.clear();
        state.put(col, value);
    }
    public int getState(int col) {
        Integer i = state.get(col);
        return (i == null) ? NONE : i;
    }
}

class HeaderMouseListener extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
        JTableHeader h = (JTableHeader) e.getSource();
        TableColumnModel columnModel = h.getColumnModel();
        int viewColumn = columnModel.getColumnIndexAtX(e.getX());
        if(viewColumn<0) {
            return;
        }
        TableCellRenderer tcr = h.getDefaultRenderer();
        int column = columnModel.getColumn(viewColumn).getModelIndex();
        if(column != -1 && tcr instanceof SortButtonRenderer) {
            SortButtonRenderer sbr = (SortButtonRenderer)tcr;
            sbr.setPressedColumn(column);
            sbr.setSelectedColumn(column);
            h.repaint();
            JTable table = h.getTable();
            if(table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            SortableTableModel model = (SortableTableModel)table.getModel();
            model.sortByColumn(column, SortButtonRenderer.DOWN==sbr.getState(column));
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        ((JTableHeader)e.getSource()).repaint();
    }
}
