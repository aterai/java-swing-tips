package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"Integer", "String", "Date"};
    private final Object[][] data = {
        {-1, "AAA", new Date()}, {2, "BBB", new Date()},
        {-9, "EEE", new Date()}, {1, "",    new Date()},
        {10, "CCC", new Date()}, {7, "FFF", new Date()},
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);
    public MainPanel() {
        super(new BorderLayout());
        JLabel r = (JLabel) table.getDefaultRenderer(Date.class);
        r.setHorizontalAlignment(SwingConstants.LEFT);
        table.setDefaultEditor(Date.class, new SpinnerCellEditor());
        //table.setShowGrid(false);
        //table.setAutoCreateRowSorter(true);
        table.setSurrendersFocusOnKeystroke(true);
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
//class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
class SpinnerCellEditor extends JSpinner implements TableCellEditor {
    protected transient ChangeEvent changeEvent;
    private final JSpinner.DateEditor editor;

    protected SpinnerCellEditor() {
        super(new SpinnerDateModel());
        editor = new JSpinner.DateEditor(this, "yyyy/MM/dd");
        setEditor(editor);
        setArrowButtonEnabled(false);
        editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);

//         addFocusListener(new FocusAdapter() {
//             @Override public void focusGained(FocusEvent e) {
//                 //System.out.println("spinner");
//                 editor.getTextField().requestFocusInWindow();
//             }
//         });
        editor.getTextField().addFocusListener(new FocusListener() {
            @Override public void focusLost(FocusEvent e) {
                setArrowButtonEnabled(false);
            }
            @Override public void focusGained(FocusEvent e) {
                //System.out.println("getTextField");
                setArrowButtonEnabled(true);
                EventQueue.invokeLater(() -> {
                    editor.getTextField().setCaretPosition(8);
                    editor.getTextField().setSelectionStart(8);
                    editor.getTextField().setSelectionEnd(10);
                });
            }
        });
        setBorder(BorderFactory.createEmptyBorder());
    }
    private void setArrowButtonEnabled(boolean flag) {
        for (Component c: getComponents()) {
            if (c instanceof JButton) {
                ((JButton) c).setEnabled(flag);
            }
        }
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        setValue(value);
        return this;
    }
    @Override public Object getCellEditorValue() {
        return getValue();
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
        try {
            commitEdit();
        } catch (ParseException pe) {
            Toolkit.getDefaultToolkit().beep();
            return false;
//             // Edited value is invalid, spinner.getValue() will return
//             // the last valid value, you could revert the spinner to show that:
//             editor.getTextField().setValue(getValue());
        }
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
