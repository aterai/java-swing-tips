/**
@version 1.0 02/25/99
@author Nobuo Tamemasa
*/
package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

//public abstract class SortableTableModel extends DefaultTableModel {
public class SortableTableModel extends DefaultTableModel {
//     public SortableTableModel(String[] str, int row) {
//         super(str, row);
//     }
    @SuppressWarnings("unchecked")
    public void sortByColumn(int column, boolean isAscent) {
        Collections.sort(getDataVector(), new ColumnComparator(column, isAscent));
        fireTableDataChanged();
    }
    //abstract public void initColumnOrder();
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

    private int pushedColumn = -1;
    private final Hashtable<Integer, Integer> state = new Hashtable<Integer, Integer>();

    public SortButtonRenderer() {
        setMargin(new Insets(0,0,0,0));
        setHorizontalTextPosition(JButton.LEFT);
        setIcon(new BlankIcon());
    }

    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value==null) ? "" : value.toString());
        setIcon(new BlankIcon());
        int modelColumn = table.convertColumnIndexToModel(column);
        Integer ivalue = state.get(modelColumn);
        if(ivalue != null) {
            if(ivalue == DOWN) {
                setIcon(new BevelArrowIcon(BevelArrowIcon.DOWN, false, false));
                setPressedIcon(new BevelArrowIcon(BevelArrowIcon.DOWN, false, true));
            }else if(ivalue == UP) {
                setIcon(new BevelArrowIcon(BevelArrowIcon.UP, false, false));
                setPressedIcon(new BevelArrowIcon(BevelArrowIcon.UP, false, true));
            }
        }
        boolean isPressed = (modelColumn == pushedColumn);
        getModel().setPressed(isPressed);
        getModel().setArmed(isPressed);
        return this;
    }

    public void setPressedColumn(int col) {
        pushedColumn = col;
    }

    public void setSelectedColumn(int col) {
        if(col < 0) return;
        Integer value = null;
        Integer obj = state.get(col);
        if(obj==null) {
            value = DOWN;
        }else if(obj == DOWN) {
            value = UP;
        }else if(obj == UP) {
    //        value = NONE;
    //    }else{
            value = DOWN;
        }
        state.clear();
        state.put(col, value);
    }

    public int getState(int col) {
        Integer i = state.get(col);
        return (i==null)?NONE:i;
//         int retValue;
//         if(obj == null) {
//             retValue = NONE;
//         }else if(obj == DOWN) {
//             retValue = DOWN;
//         }else if(obj == UP) {
//             retValue = UP;
//         }else{
//             retValue = NONE;
//         }
//         return retValue;
    }
}

// class HeaderListener extends MouseAdapter {
//     JTableHeader   header;
//     SortButtonRenderer renderer;
// 
//     HeaderListener(JTableHeader header,SortButtonRenderer renderer) {
//         this.header   = header;
//         this.renderer = renderer;
//     }
// 
//     public void mousePressed(MouseEvent e) {
//         int col = header.columnAtPoint(e.getPoint());
//         int sortCol = header.getTable().convertColumnIndexToModel(col);
//         renderer.setPressedColumn(col);
//         renderer.setSelectedColumn(col);
//         header.repaint();
// 
//         if (header.getTable().isEditing()) {
//             header.getTable().getCellEditor().stopCellEditing();
//         }
// 
//         boolean isAscent;
//         if (SortButtonRenderer.DOWN == renderer.getState(col)) {
//             isAscent = true;
//         } else {
//             isAscent = false;
//         }
//         ((SortableTableModel)header.getTable().getModel())
//           .sortByColumn(sortCol, isAscent);
//     }
// 
//     public void mouseReleased(MouseEvent e) {
//         int col = header.columnAtPoint(e.getPoint());
//         renderer.setPressedColumn(-1);                // clear
//         header.repaint();
//     }
// }

class HeaderMouseListener extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
        JTableHeader h = (JTableHeader) e.getSource();
        TableColumnModel columnModel = h.getColumnModel();
        TableCellRenderer tcr = h.getDefaultRenderer();
        int viewColumn = columnModel.getColumnIndexAtX(e.getX());
        // ArrayIndexOutOfBoundsException: -1
        if(viewColumn<0) return;
        int column = columnModel.getColumn(viewColumn).getModelIndex();
//        int column  = h.columnAtPoint(e.getPoint());
//        int sortCol = h.getTable().convertColumnIndexToModel(column);
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
            if(SortButtonRenderer.DOWN==sbr.getState(column)) {
                model.sortByColumn(column, true);
        //    }else if(SortButtonRenderer.UP==sbr.getState(column)) {
        //        model.sortByColumn(column, false);
            }else{
                model.sortByColumn(column, false);
                //model.initColumnOrder();
            }
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        JTableHeader h = (JTableHeader) e.getSource();
        TableCellRenderer tcr = h.getDefaultRenderer();
        if(tcr instanceof SortButtonRenderer) {
            SortButtonRenderer sbr = (SortButtonRenderer)tcr;
            sbr.setPressedColumn(-1);
            h.repaint();
        }
    }
}

class BevelArrowIcon implements Icon{
    public  static final int UP    = 0;
    public  static final int DOWN  = 1;
    private static final int DEFAULT_SIZE = 10;

    private Color edge1;
    private Color edge2;
    private Color fill;
    private int size;
    private int direction;

    public BevelArrowIcon(int direction, boolean isRaisedView, boolean isPressedView) {
        Color controlLtHighlight = UIManager.getColor("controlLtHighlight");
        Color controlDkShadow    = UIManager.getColor("controlDkShadow");
        Color controlShadow      = UIManager.getColor("controlShadow");
        Color controlHighlight   = UIManager.getColor("controlHighlight");
        Color control            = UIManager.getColor("control");
        if(isRaisedView) {
            if(isPressedView) {
                init(controlLtHighlight, controlDkShadow, controlShadow, DEFAULT_SIZE, direction);
            }else{
                init(controlHighlight, controlShadow, control, DEFAULT_SIZE, direction);
            }
        }else{
            if(isPressedView) {
                init(controlDkShadow, controlLtHighlight, controlShadow, DEFAULT_SIZE, direction);
            }else{
                init(controlShadow, controlHighlight, control, DEFAULT_SIZE, direction);
            }
        }
    }

    public BevelArrowIcon(Color edge1, Color edge2, Color fill, int size, int direction) {
        init(edge1, edge2, fill, size, direction);
    }

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        switch(direction) {
            case DOWN: drawDownArrow(g, x, y); break;
            case   UP: drawUpArrow(g, x, y);   break;
        }
    }

    @Override public int getIconWidth() {
        return size;
    }

    @Override public int getIconHeight() {
        return size;
    }

    private void init(Color edge1, Color edge2, Color fill, int size, int direction) {
        this.edge1 = edge1;
        this.edge2 = edge2;
        this.fill = fill;
        this.size = size;
        this.direction = direction;
    }

    private void drawDownArrow(Graphics g, int xo, int yo) {
        g.setColor(edge1);
        g.drawLine(xo, yo,   xo+size-1, yo);
        g.drawLine(xo, yo+1, xo+size-3, yo+1);
        g.setColor(edge2);
        g.drawLine(xo+size-2, yo+1, xo+size-1, yo+1);
        int x = xo+1;
        int y = yo+2;
        int dx = size-6;
        while(y+1 < yo+size) {
            g.setColor(edge1);
            g.drawLine(x, y,   x+1, y);
            g.drawLine(x, y+1, x+1, y+1);
            if(0 < dx) {
                g.setColor(fill);
                g.drawLine(x+2, y,   x+1+dx, y);
                g.drawLine(x+2, y+1, x+1+dx, y+1);
            }
            g.setColor(edge2);
            g.drawLine(x+dx+2, y,   x+dx+3, y);
            g.drawLine(x+dx+2, y+1, x+dx+3, y+1);
            x += 1;
            y += 2;
            dx -= 2;
        }
        g.setColor(edge1);
        g.drawLine(xo+(size/2), yo+size-1, xo+(size/2), yo+size-1);
    }

    private void drawUpArrow(Graphics g, int xo, int yo) {
        g.setColor(edge1);
        int x = xo+(size/2);
        g.drawLine(x, yo, x, yo);
        x--;
        int y = yo+1;
        int dx = 0;
        while(y+3 < yo+size) {
            g.setColor(edge1);
            g.drawLine(x, y,   x+1, y);
            g.drawLine(x, y+1, x+1, y+1);
            if(0 < dx) {
                g.setColor(fill);
                g.drawLine(x+2, y,   x+1+dx, y);
                g.drawLine(x+2, y+1, x+1+dx, y+1);
            }
            g.setColor(edge2);
            g.drawLine(x+dx+2, y,   x+dx+3, y);
            g.drawLine(x+dx+2, y+1, x+dx+3, y+1);
            x -= 1;
            y += 2;
            dx += 2;
        }
        g.setColor(edge1);
        g.drawLine(xo, yo+size-3,   xo+1, yo+size-3);
        g.setColor(edge2);
        g.drawLine(xo+2, yo+size-2, xo+size-1, yo+size-2);
        g.drawLine(xo, yo+size-1, xo+size, yo+size-1);
    }
}

class BlankIcon implements Icon{
    private Color fillColor;
    private int size;

    public BlankIcon() {
        this(null, 11);
    }

    public BlankIcon(Color color, int size) {
        //UIManager.getColor("control")
        //UIManager.getColor("controlShadow")
        fillColor = color;
        this.size = size;
    }

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        if(fillColor != null) {
            g.setColor(fillColor);
            g.drawRect(x, y, size-1, size-1);
        }
    }

    @Override public int getIconWidth() {
        return size;
    }

    @Override public int getIconHeight() {
        return size;
    }
}
