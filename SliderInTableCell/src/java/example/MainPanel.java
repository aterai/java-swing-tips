package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"Integer", "Integer", "Boolean"};
    private final Object[][] data = {
        {50, 50, false},
        {13, 13, true},
        {0,  0,  false},
        {20, 20, true},
        {99, 99, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
        @Override public boolean isCellEditable(int row, int column) {
            return column != 0;
        }
    };
    private final JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());

        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(26);

        //SliderEditorRednerer ser = new SliderEditorRednerer(table);
        table.getColumnModel().getColumn(1).setCellRenderer(new SliderRednerer());
        table.getColumnModel().getColumn(1).setCellEditor(new SliderEditor(table));
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class SliderRednerer extends JSlider implements TableCellRenderer {
    public SliderRednerer() {
        super();
        setName("Table.cellRenderer");
        setOpaque(true);
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Integer i = (Integer) value;
        this.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        this.setValue(i.intValue());
        return this;
    }
    //Overridden for performance reasons. ---->
    @Override public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if (Objects.nonNull(p)) {
            p = p.getParent();
        } // p should now be the JTable. //System.out.println(p.getClass());
        boolean colorMatch = Objects.nonNull(back) && Objects.nonNull(p) && back.equals(p.getBackground()) && p.isOpaque();
        return !colorMatch && super.isOpaque();
    }
    @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
//         //System.out.println(propertyName);
//         if ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue) {
//             super.firePropertyChange(propertyName, oldValue, newValue);
//         }
    }
    @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { /* Overridden for performance reasons. */ }
    @Override public void repaint(long tm, int x, int y, int width, int height) { /* Overridden for performance reasons. */ }
    @Override public void repaint(Rectangle r) { /* Overridden for performance reasons. */ }
    @Override public void repaint()    { /* Overridden for performance reasons. */ }
    @Override public void invalidate() { /* Overridden for performance reasons. */ }
    @Override public void validate()   { /* Overridden for performance reasons. */ }
    @Override public void revalidate() { /* Overridden for performance reasons. */ }
    //<---- Overridden for performance reasons.
}

class SliderEditor extends JSlider implements TableCellEditor {
    public SliderEditor(final JTable table) {
        super();
        setOpaque(true);
        addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        int row = table.convertRowIndexToModel(table.getEditingRow());
                        table.getModel().setValueAt(getValue(), row, 0);
                        table.getModel().setValueAt(getValue(), row, 1);
                    }
                });
            }
        });
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Integer i = (Integer) value;
        this.setBackground(table.getSelectionBackground());
        this.setValue(i.intValue());
        return this;
    }
    @Override public Object getCellEditorValue() {
        return Integer.valueOf(getValue());
    }

    //Copied from AbstractCellEditor
    //protected EventListenerList listenerList = new EventListenerList();
    //protected transient ChangeEvent changeEvent;

    @Override public boolean isCellEditable(EventObject e) {
        return true;
    }
    @Override public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
    @Override public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }
    @Override public void cancelCellEditing() {
        fireEditingCanceled();
    }
    @Override public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }
    @Override public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }
    public CellEditorListener[] getCellEditorListeners() {
        return listenerList.getListeners(CellEditorListener.class);
    }
    protected void fireEditingStopped() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
            }
        }
    }
    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
            }
        }
    }
}
