package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    String[] columnNames = {"Integer", "String", "Date"};
    Object[][] data = {
        {-1, "AAA", new Date()}, {2, "BBB", new Date()},
        {-9, "EEE", new Date()}, {1, "",    new Date()},
        {10, "CCC", new Date()}, {7, "FFF", new Date()},
    };
    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table  = new JTable(model);
    public MainPanel() {
        super(new BorderLayout());
        JLabel r = (JLabel)table.getDefaultRenderer(Date.class);
        r.setHorizontalAlignment(JLabel.LEFT);
        table.setDefaultEditor(Date.class, new SpinnerCellEditor(table));
        //table.setShowGrid(false);
        //table.setAutoCreateRowSorter(true);
        table.setSurrendersFocusOnKeystroke(true);
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
//class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
class SpinnerCellEditor extends JSpinner implements TableCellEditor {
    private final JSpinner.DateEditor editor;
    public SpinnerCellEditor(final JTable table) {
        super(new SpinnerDateModel());
        setEditor(editor = new JSpinner.DateEditor(this, "yyyy/MM/dd"));
        setArrowButtonEnabled(false);

        addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        int row = table.convertRowIndexToModel(table.getEditingRow());
                        table.getModel().setValueAt(getValue(), row, 2);
                    }
                });
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                //System.out.println("spinner");
                editor.getTextField().requestFocusInWindow();
            }
        });
        editor.getTextField().addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                setArrowButtonEnabled(false);
            }
            @Override public void focusGained(FocusEvent e) {
                //System.out.println("getTextField");
                setArrowButtonEnabled(true);
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        editor.getTextField().setCaretPosition(8);
                        editor.getTextField().setSelectionStart(8);
                        editor.getTextField().setSelectionEnd(10);
                    }
                });
            }
        });
        setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
    }
    private void setArrowButtonEnabled(boolean flag) {
        for(Component c: getComponents()) {
            if(c instanceof JButton) {
                ((JButton)c).setEnabled(flag);
            }
        }
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editor.getTextField().setHorizontalAlignment(JFormattedTextField.LEFT);
        setValue(value);
        return this;
    }
    @Override public Object getCellEditorValue() {
        return getValue();
    }

    //Copid from AbstractCellEditor
    //protected EventListenerList listenerList = new EventListenerList();
    transient protected ChangeEvent changeEvent = null;

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
    @Override public void  cancelCellEditing() {
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
        for(int i = listeners.length-2; i>=0; i-=2) {
            if(listeners[i]==CellEditorListener.class) {
                // Lazily create the event:
                if(changeEvent == null) changeEvent = new ChangeEvent(this);
                ((CellEditorListener)listeners[i+1]).editingStopped(changeEvent);
            }
        }
    }
    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = listeners.length-2; i>=0; i-=2) {
            if(listeners[i]==CellEditorListener.class) {
                // Lazily create the event:
                if(changeEvent == null) changeEvent = new ChangeEvent(this);
                ((CellEditorListener)listeners[i+1]).editingCanceled(changeEvent);
            }
        }
    }
}
