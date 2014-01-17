package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
// import javax.swing.event.*;
import javax.swing.table.*;
// import javax.swing.text.*;

public class MainPanel extends JPanel {
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
//             private final Color evenColor = new Color(230, 240, 255);
//             @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
//                 Component c = super.prepareRenderer(tcr, row, column);
//                 if(isRowSelected(row)) {
//                     c.setForeground(getSelectionForeground());
//                     c.setBackground(getSelectionBackground());
//                 }else{
//                     c.setForeground(getForeground());
//                     c.setBackground((row%2==0)?evenColor:getBackground());
//                 }
//                 return c;
//             }
//             @Override public void doLayout() {
//                 //System.out.println("doLayout");
//                 initPreferredHeight();
//                 super.doLayout();
//             }
//             @Override public void columnMarginChanged(final ChangeEvent e) {
//                 //System.out.println("columnMarginChanged");
//                 super.columnMarginChanged(e);
//                 initPreferredHeight();
//             }
//             private void initPreferredHeight() {
//                 for(int row=0;row<getRowCount();row++) {
//                     int maximum_height = 0;
//                     for(int col=0;col<getColumnModel().getColumnCount();col++) {
//                         Component c = prepareRenderer(getCellRenderer(row, col), row, col);
//                         if(c instanceof JTextArea) {
//                             JTextArea a = (JTextArea)c;
//                             int h = getPreferredHeight(a); // + getIntercellSpacing().height;
//                             maximum_height = Math.max(maximum_height, h);
//                         }
//                     }
//                     setRowHeight(row, maximum_height);
//                 }
//             }
//             //http://tips4java.wordpress.com/2008/10/26/text-utilities/
//             private int getPreferredHeight(JTextComponent c) {
//                 Insets insets = c.getInsets();
//                 //Insets margin = c.getMargin();
//                 //System.out.println(insets);
//                 View view = c.getUI().getRootView(c).getView(0);
//                 float f = view.getPreferredSpan(View.Y_AXIS);
//                 //System.out.println(f);
//                 int preferredHeight = (int)f;
//                 return preferredHeight + insets.top + insets.bottom;
//             }
        };
        table.setEnabled(false);
        table.setShowGrid(false);
        table.getColumnModel().getColumn(AUTOWRAP_COLUMN).setCellRenderer(new TextAreaCellRenderer());
        //table.setIntercellSpacing(new Dimension(0,0));
        add(new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
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
    //public static class UIResource extends TextAreaCellRenderer implements javax.swing.plaf.UIResource {}
    public TextAreaCellRenderer() {
        super();
        setLineWrap(true);
        setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        //setBorder(BorderFactory.createLineBorder(Color.RED,2));
        //setMargin(new Insets(2,2,2,2));
        //setBorder(BorderFactory.createEmptyBorder());
        setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setFont(table.getFont());
        setText((value ==null) ? "" : value.toString());
        adjustRowHeight(table, row, column);
        return this;
    }

    /**
     * Calculate the new preferred height for a given row, and sets the height on the table.
     * http://blog.botunge.dk/post/2009/10/09/JTable-multiline-cell-renderer.aspx
     */
    private List<List<Integer>> rowColHeight = new ArrayList<>();
    private void adjustRowHeight(JTable table, int row, int column) {
        //The trick to get this to work properly is to set the width of the column to the
        //textarea. The reason for this is that getPreferredSize(), without a width tries
        //to place all the text in one line. By setting the size with the with of the column,
        //getPreferredSize() returnes the proper height which the row should have in
        //order to make room for the text.
        //int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
        //int cWidth = table.getCellRect(row, column, false).width; //Ignore IntercellSpacing
        //setSize(new Dimension(cWidth, 1000));

        setBounds(table.getCellRect(row, column, false));
        //doLayout();

        int prefH = getPreferredSize().height;
        while(rowColHeight.size() <= row) {
            rowColHeight.add(new ArrayList<Integer>(column));
        }
        List<Integer> colHeights = rowColHeight.get(row);
        while(colHeights.size() <= column) {
            colHeights.add(0);
        }
        colHeights.set(column, prefH);
        int maxH = prefH;
        for(Integer colHeight : colHeights) {
            if(colHeight > maxH) {
                maxH = colHeight;
            }
        }
        if(table.getRowHeight(row) != maxH) {
            table.setRowHeight(row, maxH);
        }
    }

    //Overridden for performance reasons. ---->
    @Override public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if(p != null) {
            p = p.getParent();
        } // p should now be the JTable.
        boolean colorMatch = back != null && p != null && back.equals(p.getBackground()) && p.isOpaque();
        return !colorMatch && super.isOpaque();
    }
    @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        //String literal pool
        //if(propertyName=="document" || ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue)) {
        if("document".equals(propertyName) || oldValue != newValue && ("font".equals(propertyName) || "foreground".equals(propertyName))) {
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
