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
    public MainPanel() {
        super(new BorderLayout());
        String[] columnNames = {"user", "rwx"};
        Object[][] data = {
            {"owner", 7}, {"group", 6}, {"other", 5}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        //http://terai.xrea.jp/Swing/TerminateEdit.html
        //table.getTableHeader().setReorderingAllowed(false);
        //frame.setResizeable(false);
        //or
//         table.addMouseListener(new MouseAdapter() {
//             @Override public void mouseReleased(MouseEvent e) {
//                 JTable t = (JTable)e.getComponent();
//                 Point p  = e.getPoint();
//                 int row  = t.rowAtPoint(p);
//                 int col  = t.columnAtPoint(p);
//                 if(t.convertColumnIndexToModel(col)==1) {
//                     t.getCellEditor(row, col).stopCellEditing();
//                 }
//             }
//         });
        CheckBoxEditorRenderer cer = new CheckBoxEditorRenderer();
        //CheckBoxEditorRenderer2 cer = new CheckBoxEditorRenderer2();
        table.getColumnModel().getColumn(1).setCellRenderer(cer);
        table.getColumnModel().getColumn(1).setCellEditor(cer);

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
class CheckBoxEditorRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
    protected final String[] title = {"r", "w", "x"};
    protected final CheckBoxPanel editor = new CheckBoxPanel(title);
    protected final CheckBoxPanel renderer = new CheckBoxPanel(title);
    public CheckBoxEditorRenderer() {
        ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
            }
        };
        for(AbstractButton b: editor.buttons) b.addActionListener(al);
    }
    protected void updateButtons(CheckBoxPanel p, Object v) {
        Integer i = (Integer)(v==null?0:v);
        p.buttons[0].setSelected((i&(1<<2))!=0);
        p.buttons[1].setSelected((i&(1<<1))!=0);
        p.buttons[2].setSelected((i&(1<<0))!=0);
    }
    class CheckBoxPanel extends JPanel {
        public final JCheckBox[] buttons;
        public CheckBoxPanel(String[] t) {
            super();
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(false);
            buttons = new JCheckBox[t.length];
            ActionMap am = getActionMap();
            for(int i=0; i<buttons.length; i++) {
                final JCheckBox b = new JCheckBox(t[i]);
                b.setOpaque(false);
                b.setFocusable(false);
                b.setRolloverEnabled(false);
                b.setBackground(new Color(0,0,0,0));
                buttons[i] = b;
                add(b);
                am.put(t[i], new AbstractAction(t[i]) {
                    public void actionPerformed(ActionEvent e) {
                        b.setSelected(!b.isSelected());
                        fireEditingStopped();
                    }
                });
            }
            InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), t[0]);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), t[1]);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), t[2]);
        }
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        updateButtons(renderer, value);
        return renderer;
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        updateButtons(editor, value);
        return editor;
    }
    @Override public Object getCellEditorValue() {
        int i = 0;
        if(editor.buttons[0].isSelected()) i|=1<<2;
        if(editor.buttons[1].isSelected()) i|=1<<1;
        if(editor.buttons[2].isSelected()) i|=1<<0;
        return i;
    }
//     @Override public boolean isCellEditable(EventObject anEvent) { 
//         return true;
//     }
//     @Override public boolean shouldSelectCell(EventObject anEvent) { 
//         return true;
//     }
//     @Override public boolean stopCellEditing() {
//         fireEditingStopped();
//         return true;
//     }
//     @Override public void cancelCellEditing() {
//         fireEditingCanceled();
//     }
}

class CheckBoxEditorRenderer2 extends CheckBoxEditorRenderer {
    protected final String[] title = {"r", "w", "x"};
    protected final CheckBoxPanel editor = new CheckBoxPanel(title);
    protected final CheckBoxPanel renderer = new CheckBoxPanel(title);
    protected EditorDelegate delegate;
    protected int clickCountToStart = 1;

    public CheckBoxEditorRenderer2() {
        delegate = new EditorDelegate() {
            public void setValue(Object value) {
                updateButtons(editor, value);
            }
            public Object getCellEditorValue() {
                int i = 0;
                if(editor.buttons[0].isSelected()) i|=1<<2;
                if(editor.buttons[1].isSelected()) i|=1<<1;
                if(editor.buttons[2].isSelected()) i|=1<<0;
                return i;
            }
        };
        for(AbstractButton b: editor.buttons) b.addActionListener(delegate);
    }
    protected void updateButtons(CheckBoxPanel p, Object v) {
        Integer i = (Integer)(v==null?0:v);
        p.buttons[0].setSelected((i&(1<<2))!=0);
        p.buttons[1].setSelected((i&(1<<1))!=0);
        p.buttons[2].setSelected((i&(1<<0))!=0);
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        updateButtons(renderer, value);
        return renderer;
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        updateButtons(editor, value);
        return editor;
    }
    @Override public Object getCellEditorValue() {
        return delegate.getCellEditorValue();
    }
    @Override public boolean isCellEditable(EventObject anEvent) { 
        return delegate.isCellEditable(anEvent); 
    }
    @Override public boolean shouldSelectCell(EventObject anEvent) { 
        return delegate.shouldSelectCell(anEvent); 
    }
    @Override public boolean stopCellEditing() {
        return delegate.stopCellEditing();
    }
    @Override public void cancelCellEditing() {
        delegate.cancelCellEditing();
    }
    protected class EditorDelegate implements ActionListener, ItemListener, java.io.Serializable {
        protected Object value;
        public Object getCellEditorValue() {
            return value;
        }
        public void setValue(Object value) {
            this.value = value;
        }
        public boolean isCellEditable(EventObject anEvent) {
            if (anEvent instanceof MouseEvent) {
                return ((MouseEvent)anEvent).getClickCount() >= clickCountToStart;
            }
            return true;
        }
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }
        public boolean startCellEditing(EventObject anEvent) {
            return true;
        }
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }
        public void cancelCellEditing() {
            fireEditingCanceled();
        }
        @Override public void actionPerformed(ActionEvent e) {
            CheckBoxEditorRenderer2.this.stopCellEditing();
        }
        @Override public void itemStateChanged(ItemEvent e) {
            CheckBoxEditorRenderer2.this.stopCellEditing();
        }
    }
}
