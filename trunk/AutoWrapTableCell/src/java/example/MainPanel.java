package example;
//-*- mode:java; encoding:utf-8 -*-
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
                initPreferredHeight();
            }
            private void initPreferredHeight() {
                for(int row=0;row<getRowCount();row++) {
                    int maximum_height = 0;
                    for(int col=0;col<getColumnModel().getColumnCount();col++) {
                        Component c = prepareRenderer(getCellRenderer(row, col), row, col);
                        if(c instanceof JTextArea) {
                            JTextArea a = (JTextArea)c;
                            int h = getPreferredHeight(a); // + getIntercellSpacing().height;
                            maximum_height = Math.max(maximum_height, h);
                        }
                    }
                    setRowHeight(row, maximum_height);
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
