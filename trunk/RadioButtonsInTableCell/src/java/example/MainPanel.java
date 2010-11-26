package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

//http://www.crionics.com/products/opensource/faq/swing_ex/JTableExamples2.html
public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        String[] columnNames = {"Integer", "String"};
        Object[][] data = {
            { 1, "A" }, { 2, "B" }, { 3, "C" },
            { 4, "B" }, { 5, "A" }
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        //table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                JTable t = (JTable)e.getComponent();
                Point pt = e.getPoint();
                int row  = t.rowAtPoint(pt);
                int col  = t.columnAtPoint(pt);
                if(t.convertRowIndexToModel(row)>=0 && t.convertColumnIndexToModel(col)==1) {
                    TableCellEditor ce = t.getCellEditor(row, col);
                    //http://tips4java.wordpress.com/2009/07/12/table-button-column/
                    ce.stopCellEditing();
                    Component c = ce.getTableCellEditorComponent(t, null, true, row, col);
                    Point p = SwingUtilities.convertPoint(t, pt, c);
                    Component b = SwingUtilities.getDeepestComponentAt(c, p.x, p.y);
                    if(b instanceof JRadioButton) ((JRadioButton)b).doClick();
                }
            }
        });
        RadioButtonEditorRenderer rbe = new RadioButtonEditorRenderer();
        table.getColumnModel().getColumn(1).setCellRenderer(rbe);
        table.getColumnModel().getColumn(1).setCellEditor(rbe);
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

class RadioButtonPanel extends JPanel {
    public final JRadioButton[] buttons;
    public final ButtonGroup bg = new ButtonGroup();
    RadioButtonPanel(String[] answer) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        buttons = new JRadioButton[answer.length];
        for(int i=0;i<buttons.length;i++) {
            buttons[i] = new JRadioButton(answer[i]);
            buttons[i].setActionCommand(answer[i]);
            buttons[i].setEnabled(true);
            add(buttons[i]);
            bg.add(buttons[i]);
        }
    }
}
class RadioButtonEditorRenderer extends AbstractCellEditor
                                implements TableCellRenderer, TableCellEditor{
    private final String[] answer = { "A", "B", "C" };
    private final RadioButtonPanel editor;
    private final RadioButtonPanel renderer;
    public RadioButtonEditorRenderer() {
        super();
        this.editor   = new RadioButtonPanel(answer);
        this.renderer = new RadioButtonPanel(answer);
    }
    private void setSelectedButton(RadioButtonPanel p, Object v) {
        if("A".equals(v)) {
            p.buttons[0].setSelected(true);
        }else if("B".equals(v)) {
            p.buttons[1].setSelected(true);
        }else{
            p.buttons[2].setSelected(true);
        }
    }
    @Override public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {
        setSelectedButton(renderer, value);
        return renderer;
    }
    @Override public Component getTableCellEditorComponent(
        JTable table, Object value, boolean isSelected, int row, int column) {
        setSelectedButton(editor, value);
        return editor;
    }
    @Override public Object getCellEditorValue() {
        return editor.bg.getSelection().getActionCommand();
    }
}
