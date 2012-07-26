package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(makeTable()));
        setBorder(BorderFactory.createTitledBorder("Multiple Buttons in a Table Cell"));
        setPreferredSize(new Dimension(320, 240));
    }
    private JTable makeTable() {
        String empty = "";
        String[] columnNames = {"String", "Button"};
        Object[][] data = {
            {"AAA", empty}, {"CCC", empty}, {"BBB", empty}, {"ZZZ", empty}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        final JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setAutoCreateRowSorter(true);
        //table.addMouseListener(new CellButtonsMouseListener());
        //ButtonsEditorRenderer er = new ButtonsEditorRenderer(table);
        TableColumn column = table.getColumnModel().getColumn(1);
        column.setCellRenderer(new ButtonsRenderer());
        column.setCellEditor(new ButtonsEditor(table));
        return table;
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
// class CellButtonsMouseListener extends MouseAdapter{
//     @Override public void mouseReleased(MouseEvent e) {
//         JTable t = (JTable)e.getComponent();
//         Point pt = e.getPoint();
//         int row  = t.rowAtPoint(pt);
//         int col  = t.columnAtPoint(pt);
//         if(t.convertRowIndexToModel(row)>=0 && t.convertColumnIndexToModel(col)==1) {
//             TableCellEditor ce = t.getCellEditor(row, col);
//             ce.stopCellEditing();
//             Component c = ce.getTableCellEditorComponent(t, null, true, row, col);
//             Point p = SwingUtilities.convertPoint(t, pt, c);
//             Component b = SwingUtilities.getDeepestComponentAt(c, p.x, p.y);
//             if(b instanceof JButton) ((JButton)b).doClick();
//         }
//     }
// }

class ButtonsPanel extends JPanel {
    public final List<JButton> buttons = Arrays.asList(new JButton("view"), new JButton("edit"));
    public ButtonsPanel() {
        super();
        setOpaque(true);
        for(JButton b: buttons) {
            b.setFocusable(false);
            b.setRolloverEnabled(false);
            add(b);
        }
    }
//     @Override public void updateUI() {
//         super.updateUI();
//     }
}
class ButtonsRenderer extends ButtonsPanel implements TableCellRenderer {
    public ButtonsRenderer() {
        super();
        setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setBackground(isSelected?table.getSelectionBackground():table.getBackground());
        return this;
    }
}
class ButtonsEditor extends ButtonsPanel implements TableCellEditor {
    public ButtonsEditor(final JTable table) {
        super();

        //---->
        //DEBUG: view button click -> control key down + edit button(same cell) press -> remain selection color
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                ButtonModel m = ((JButton)e.getSource()).getModel();
                if(m.isPressed() && table.isRowSelected(table.getEditingRow()) && e.isControlDown()) {
                    setBackground(table.getBackground());
                }
            }
        };
        buttons.get(0).addMouseListener(ml);
        buttons.get(1).addMouseListener(ml);
        //<----

        buttons.get(0).addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
                JOptionPane.showMessageDialog(table, "Viewing");
            }
        });

        buttons.get(1).addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                //Object o = table.getModel().getValueAt(table.getSelectedRow(), 0);
                int row = table.convertRowIndexToModel(table.getEditingRow());
                Object o = table.getModel().getValueAt(row, 0);
                fireEditingStopped();
                JOptionPane.showMessageDialog(table, "Editing: "+o);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                fireEditingStopped();
            }
        });
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.setBackground(table.getSelectionBackground());
        return this;
    }
    @Override public Object getCellEditorValue() {
        return "";
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
