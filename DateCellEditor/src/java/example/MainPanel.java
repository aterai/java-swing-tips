package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
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
        table.setDefaultEditor(Date.class, new SpinnerCellEditor());
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
class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JSpinner spinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner.DateEditor editor;
    public SpinnerCellEditor() {
        editor = new JSpinner.DateEditor(spinner, "yyyy/MM/dd");
        spinner.setEditor(editor);
        setArrowButtonEnabled(false);
        spinner.addFocusListener(new FocusAdapter() {
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
        spinner.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
    }
    private void setArrowButtonEnabled(boolean flag) {
        for(Component c: spinner.getComponents()) {
            if(c instanceof JButton) {
                ((JButton)c).setEnabled(flag);
            }
        }
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        spinner.setValue(value);
        editor.getTextField().setHorizontalAlignment(JFormattedTextField.LEFT);
//         JTextField tf = editor.getTextField();
//         tf.setCaretPosition(8);
//         tf.setSelectionStart(8);
//         tf.setSelectionEnd(10);
        return spinner;
    }
    @Override public Object getCellEditorValue() {
        return spinner.getValue();
    }
    @Override public boolean isCellEditable(EventObject e) {
        if(e instanceof MouseEvent) {
            return ((MouseEvent)e).getClickCount() >= 2;
        }
        return true;
    }
}
