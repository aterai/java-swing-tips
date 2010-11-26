package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        String[] columnNames = {"Integer", "Integer", "Boolean"};
        Object[][] data = {
            {50, 50, false},
            {13, 13, true},
            {0,  0,  false},
            {20, 20, true},
            {99, 99, false}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
            @Override public boolean isCellEditable(int row, int column) {
                return (column!=0);
            }
        };
        JTable table = new JTable(model);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(26);

        SliderEditorRednerer ser = new SliderEditorRednerer(table);
        table.getColumnModel().getColumn(1).setCellRenderer(ser);
        table.getColumnModel().getColumn(1).setCellEditor(ser);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 200));
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
        } catch (Exception e) {
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
class SliderEditorRednerer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
    private final JSlider sliderEditor   = new JSlider();
    private final JSlider sliderRenderer = new JSlider() {
        //Overridden for performance reasons. ---->
        @Override public boolean isOpaque() {
            Color back = getBackground();
            Component p = getParent();
            if(p != null) {
                p = p.getParent();
            } // p should now be the JTable. //System.out.println(p.getClass());
            boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
            return !colorMatch && super.isOpaque();
        }
        @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
//             //System.out.println(propertyName);
//             if((propertyName == "font" || propertyName == "foreground") && oldValue != newValue) {
//                 super.firePropertyChange(propertyName, oldValue, newValue);
//             }
        }
        @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
        @Override public void repaint(long tm, int x, int y, int width, int height) {}
        @Override public void repaint(Rectangle r) {}
        @Override public void repaint() {}
        @Override public void invalidate() {}
        @Override public void validate() {}
        @Override public void revalidate() {}
        //<---- Overridden for performance reasons.
    };
    public SliderEditorRednerer(final JTable table) {
        sliderEditor.setOpaque(true);
        sliderRenderer.setOpaque(true);
        sliderEditor.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        int row = table.convertRowIndexToModel(table.getSelectedRow());
                        table.getModel().setValueAt(sliderEditor.getValue(), row, 0);
                    }
                });
            }
        });
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Integer i = (Integer)value;
        sliderEditor.setBackground(table.getSelectionBackground());
        sliderEditor.setValue(i.intValue());
        return sliderEditor;
    }
    @Override public Object getCellEditorValue() {
        return Integer.valueOf(sliderEditor.getValue());
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Integer i = (Integer)value;
        sliderRenderer.setBackground(isSelected?table.getSelectionBackground():table.getBackground());
        sliderRenderer.setValue(i.intValue());
        return sliderRenderer;
    }
}
