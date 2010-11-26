package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class MainPanel extends JPanel{
    private static final int AUTOWRAP_COLUMN = 1; 
    public MainPanel() {
        super(new BorderLayout());

        String[] columnNames = {"Default", "AutoWrap"};
        Object[][] data = {
            {"123456789012345678901234567890", "123456789012345678901234567890"},
            {"aaaa", "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddx"},
            {"bbbbb", "----------------------------------------------0"},
            {"ccccccccccccccccccc", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>|"},
        };
        TableModel model = new DefaultTableModel(data, columnNames);
        final JTable table = new JTable(model) {
            private final Color evenColor = new Color(230, 240, 255);
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if(isRowSelected(row)) {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                }else{
                    c.setForeground(getForeground());
                    c.setBackground((row%2==0)?evenColor:getBackground());
                }
                return c;
            }
            @Override public void doLayout() {
                //System.out.println("doLayout");
                initPreferredHeight();
                super.doLayout();
            }
            @Override public void columnMarginChanged(final ChangeEvent e) {
                //System.out.println("columnMarginChanged");
                super.columnMarginChanged(e);
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        initPreferredHeight();
                    }
                });
            }
            private SizeSequence rowModel2;
            private SizeSequence getRowModel2() {
                if (rowModel2 == null) {
                    rowModel2 = new SizeSequence(getRowCount(), getRowHeight());
                }
                return rowModel2;
            }
            @Override public int getRowHeight(int row) {
                return (rowModel2 == null) ? getRowHeight() : rowModel2.getSize(row);
            }
            @Override public int rowAtPoint(Point point) {
                int y = point.y;
                int result = (rowModel2 == null) ?  y/getRowHeight() : rowModel2.getIndex(y);
                if (result < 0) {
                    return -1;
                }
                else if (result >= getRowCount()) {
                    return -1;
                }
                else {
                    return result;
                }
            }
            @Override public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
                Rectangle r = new Rectangle();
                boolean valid = true;
                if (row < 0) {
                    // y = height = 0;
                    valid = false;
                }
                else if (row >= getRowCount()) {
                    r.y = getHeight();
                    valid = false;
                }
                else {
                    r.height = getRowHeight(row);
                    r.y = (rowModel2 == null) ? row * r.height : rowModel2.getPosition(row);
                }

                if (column < 0) {
                    if( !getComponentOrientation().isLeftToRight() ) {
                        r.x = getWidth();
                    }
                    // otherwise, x = width = 0;
                    valid = false;
                }
                else if (column >= getColumnCount()) {
                    if( getComponentOrientation().isLeftToRight() ) {
                        r.x = getWidth();
                    }
                    // otherwise, x = width = 0;
                    valid = false;
                }
                else {
                    TableColumnModel cm = getColumnModel();
                    if( getComponentOrientation().isLeftToRight() ) {
                        for(int i = 0; i < column; i++) {
                            r.x += cm.getColumn(i).getWidth();
                        }
                    } else {
                        for(int i = cm.getColumnCount()-1; i > column; i--) {
                            r.x += cm.getColumn(i).getWidth();
                        }
                    }
                    r.width = cm.getColumn(column).getWidth();
                }

                if (valid && !includeSpacing) {
                    // Bound the margins by their associated dimensions to prevent
                    // returning bounds with negative dimensions.
                    int rm = Math.min(getRowMargin(), r.height);
                    int cm = Math.min(getColumnModel().getColumnMargin(), r.width);
                    // This is not the same as grow(), it rounds differently.
                    r.setBounds(r.x + cm/2, r.y + rm/2, r.width - cm, r.height - rm);
                }
                return r;
            }

            @Override public void setRowHeight(int row, int rowHeight) {
                if (rowHeight <= 0) {
                    throw new IllegalArgumentException("New row height less than 1");
                }
                getRowModel2().setSize(row, rowHeight);
                //if (sortManager != null) {
                //    sortManager.setViewRowHeight(row, rowHeight);
                //}
                //resizeAndRepaint();
            }
            private void initPreferredHeight() {
                int vc = convertColumnIndexToView(AUTOWRAP_COLUMN);
                TableColumn col = getColumnModel().getColumn(vc);
                for(int row=0; row<getRowCount(); row++) {
                    Component c = prepareRenderer(col.getCellRenderer(), row, vc);
                    if(c instanceof JTextArea) {
                        JTextArea a = (JTextArea)c;
                        int h = getPreferredHeight(a); // + getIntercellSpacing().height;
                        //if(getRowHeight(row)!=h)
                        setRowHeight(row, h);
                    }
                }
            }
            //http://tips4java.wordpress.com/2008/10/26/text-utilities/
            private int getPreferredHeight(JTextComponent c) {
                Insets insets = c.getInsets();
                //Insets margin = c.getMargin();
                //System.out.println(insets);
                View view = c.getUI().getRootView(c).getView(0);
                float f = view.getPreferredSpan(View.Y_AXIS);
                //System.out.println(f);
                int preferredHeight = (int)f;
                return preferredHeight + insets.top + insets.bottom;
            }
        };
        table.setEnabled(false);
        table.setShowGrid(false);
        table.getColumnModel().getColumn(AUTOWRAP_COLUMN).setCellRenderer(new TextAreaCellRenderer());
        //table.setIntercellSpacing(new Dimension(0,0));
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TextAreaCellRenderer extends JTextArea implements TableCellRenderer {
    public static class UIResource extends TextAreaCellRenderer implements javax.swing.plaf.UIResource {}
    public TextAreaCellRenderer() {
        super();
        setLineWrap(true);
        setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        //setBorder(BorderFactory.createLineBorder(Color.RED,2));
        //setMargin(new Insets(2,2,2,2));
        //setBorder(BorderFactory.createEmptyBorder());
        setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        setFont(table.getFont());
        setText((value ==null) ? "" : value.toString());
        return this;
    }
    //Overridden for performance reasons. ---->
    @Override public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if(p != null) {
            p = p.getParent();
        } // p should now be the JTable.
        boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
        return !colorMatch && super.isOpaque();
    }
    @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        //String literal pool
        //if(propertyName=="document" || ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue)) {
        if("document".equals(propertyName) || (("font".equals(propertyName) || "foreground".equals(propertyName)) && oldValue != newValue)) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    @Override public void repaint(long tm, int x, int y, int width, int height) {}
    @Override public void repaint(Rectangle r) {}
    @Override public void repaint() {}
    @Override public void invalidate() {}
    @Override public void validate() {}
    @Override public void revalidate() {}
    //<---- Overridden for performance reasons.
}
