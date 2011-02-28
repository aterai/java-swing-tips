package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
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
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                JTable t = (JTable)e.getComponent();
                Point p  = e.getPoint();
                int row  = t.rowAtPoint(p);
                int col  = t.columnAtPoint(p);
                if(t.convertColumnIndexToModel(col)==1) {
                    t.getCellEditor(row, col).stopCellEditing();
                }
            }
        });
        CheckBoxEditorRenderer cer = new CheckBoxEditorRenderer();
        //or
        //CheckBoxEditorRenderer2 cer = new CheckBoxEditorRenderer2(table);
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
class CheckBoxPanel extends JPanel {
    public final JCheckBox[] buttons;
    public CheckBoxPanel(String[] t) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        buttons = new JCheckBox[t.length];
        for(int i=0; i<buttons.length; i++) {
            add(buttons[i] = new JCheckBox(t[i]));
        }
    }
}
class CheckBoxEditorRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
    protected final String[] title = {"r", "w", "x"};
    protected final CheckBoxPanel editor = new CheckBoxPanel(title);
    protected final CheckBoxPanel renderer = new CheckBoxPanel(title);
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
        int i = 0;
        if(editor.buttons[0].isSelected()) i|=1<<2;
        if(editor.buttons[1].isSelected()) i|=1<<1;
        if(editor.buttons[2].isSelected()) i|=1<<0;
        return i;
    }
}

class CheckBoxEditorRenderer2 extends CheckBoxEditorRenderer implements MouseListener {
    private final JTable table;
    public CheckBoxEditorRenderer2(JTable table) {
        super();
        this.table = table;
        editor.addMouseListener(this);
    }
    //Copied form http://tips4java.wordpress.com/2009/07/12/table-button-column/
    private boolean isButtonColumnEditor;
    @Override public void mousePressed(MouseEvent e) {
        if(table.isEditing() &&  table.getCellEditor() == this) {
            isButtonColumnEditor = true;
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        if(isButtonColumnEditor &&  table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        isButtonColumnEditor = false;
    }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
