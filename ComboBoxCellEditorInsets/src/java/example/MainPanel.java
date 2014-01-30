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
    public MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(makeTable()));
        setBorder(BorderFactory.createTitledBorder("JComboBox in a Table Cell"));
        setPreferredSize(new Dimension(320, 240));
    }
    private JTable makeTable() {
        String[] columnNames = {"Border", "JPanel+JComboBox"};
        Object[][] data = {
            {"AAA", "aaaaaa"}, {"CCC", "bbb"}, {"BBB", "c"}, {"ZZZ", "ddddd"}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setAutoCreateRowSorter(true);
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setCellRenderer(new ComboCellRenderer());
        column.setCellEditor(new DefaultCellEditor(makeComboBox()));

        column = table.getColumnModel().getColumn(1);
        column.setCellRenderer(new ComboBoxCellRenderer());
        column.setCellEditor(new ComboBoxCellEditor());
        return table;
    }
    private static JComboBox<String> makeComboBox() {
        JComboBox<String> combo = new JComboBox<>(new String[] {"aaaaaa", "bbb", "c"});
        combo.setEditable(true);
        combo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8,10,8,10), combo.getBorder()));
        return combo;
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

//*
class ComboBoxPanel extends JPanel {
    public final JComboBox<String> comboBox = new JComboBox<>(new String[] {"aaaaaa", "bbb", "c"});
    public ComboBoxPanel() {
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 1.0;
        c.insets = new Insets(0, 10, 0, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        comboBox.setEditable(true);
        setOpaque(true);
        add(comboBox, c);
        comboBox.setSelectedIndex(0);
    }
}
/*/ //TEST:
class ComboBoxPanel extends JPanel {
    private String[] m = new String[] {"a", "b", "c"};
    protected JComboBox<String> comboBox = new JComboBox<String>(m) {
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(40, d.height);
        }
    };
    public ComboBoxPanel() {
        super();
        setOpaque(true);
        comboBox.setEditable(true);
        add(comboBox);
    }
}
//*/
class ComboBoxCellRenderer extends ComboBoxPanel implements TableCellRenderer {
    public ComboBoxCellRenderer() {
        super();
        setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setBackground(isSelected?table.getSelectionBackground():table.getBackground());
        if(value!=null) {
            comboBox.setSelectedItem(value);
        }
        return this;
    }
}
class ComboBoxCellEditor extends ComboBoxPanel implements TableCellEditor {
    transient protected ChangeEvent changeEvent = null;

    public ComboBoxCellEditor() {
        super();
        comboBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
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
        comboBox.setSelectedItem(value);
        return this;
    }

    //Copid from DefaultCellEditor.EditorDelegate
    @Override public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }
    @Override public boolean shouldSelectCell(EventObject anEvent) {
        if(anEvent instanceof MouseEvent) {
            MouseEvent e = (MouseEvent)anEvent;
            return e.getID() != MouseEvent.MOUSE_DRAGGED;
        }
        return true;
    }
    @Override public boolean stopCellEditing() {
        if(comboBox.isEditable()) {
            comboBox.actionPerformed(new ActionEvent(this, 0, ""));
        }
        fireEditingStopped();
        return true;
    }

    //Copid from AbstractCellEditor
    //protected EventListenerList listenerList = new EventListenerList();
    //transient protected ChangeEvent changeEvent = null;
    @Override public boolean isCellEditable(EventObject e) {
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
        for(int i = listeners.length-2; i>=0; i-=2) {
            if(listeners[i]==CellEditorListener.class) {
                // Lazily create the event:
                if(changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
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
                if(changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener)listeners[i+1]).editingCanceled(changeEvent);
            }
        }
    }
}
class ComboCellRenderer extends JComboBox<String> implements TableCellRenderer {
    private final JTextField editor;
    //private JButton button;
    public ComboCellRenderer() {
        super();
        setEditable(true);
        //setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8,10,8,10), getBorder()));
        editor = (JTextField) getEditor().getEditorComponent();
        editor.setBorder(BorderFactory.createEmptyBorder());
        editor.setOpaque(true);
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        removeAllItems();
        if(isSelected) {
            editor.setForeground(table.getSelectionForeground());
            editor.setBackground(table.getSelectionBackground());
            //button.setBackground(table.getSelectionBackground());
        }else{
            editor.setForeground(table.getForeground());
            editor.setBackground(table.getBackground());
            //button.setBackground(bg);
        }
        addItem(Objects.toString(value, ""));
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
        //System.out.println(propertyName);
        //if((propertyName == "font" || propertyName == "foreground") && oldValue != newValue) {
        //    super.firePropertyChange(propertyName, oldValue, newValue);
        //}
    }
    //@Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { /* Overridden for performance reasons. */ }
    @Override public void repaint(long tm, int x, int y, int width, int height) { /* Overridden for performance reasons. */ }
    @Override public void repaint(Rectangle r) { /* Overridden for performance reasons. */ }
    @Override public void repaint()      { /* Overridden for performance reasons. */ }
    //@Override public void invalidate() { /* Overridden for performance reasons. */ }
    //@Override public void validate()   { /* Overridden for performance reasons. */ }
    @Override public void revalidate()   { /* Overridden for performance reasons. */ }
    //<---- Overridden for performance reasons.
}
