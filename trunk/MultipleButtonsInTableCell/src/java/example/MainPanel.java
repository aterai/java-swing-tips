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
        add(new JScrollPane(makeTable()));
        setBorder(BorderFactory.createTitledBorder("Multiple Buttons in a Table Cell"));
        setPreferredSize(new Dimension(320, 240));
    }
    private JTable makeTable() {
        String[] columnNames = {"String", "Button"};
        Object[][] data = {
            {"AAA", ""}, {"CCC", ""}, {"BBB", ""}, {"ZZZ", ""}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(36);
        //table.setAutoCreateRowSorter(true);
        table.addMouseListener(new CellButtonsMouseListener());
        ButtonsEditorRenderer er = new ButtonsEditorRenderer(table);
        TableColumn column = table.getColumnModel().getColumn(1);
        column.setCellRenderer(er);
        column.setCellEditor(er);
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
class CellButtonsMouseListener extends MouseAdapter{
    @Override public void mouseReleased(MouseEvent e) {
        JTable t = (JTable)e.getComponent();
        Point pt = e.getPoint();
        int row  = t.rowAtPoint(pt);
        int col  = t.columnAtPoint(pt);
        if(t.convertRowIndexToModel(row)>=0 && t.convertColumnIndexToModel(col)==1) {
            TableCellEditor ce = t.getCellEditor(row, col);
            ce.stopCellEditing();
            Component c = ce.getTableCellEditorComponent(t, null, true, row, col);
            Point p = SwingUtilities.convertPoint(t, pt, c);
            Component b = SwingUtilities.getDeepestComponentAt(c, p.x, p.y);
            if(b instanceof JButton) ((JButton)b).doClick();
        }
    }
}

class ButtonsEditorRenderer extends AbstractCellEditor implements TableCellRenderer,TableCellEditor{
    private final JPanel renderer = new JPanel();
    private final JPanel editor   = new JPanel();
    public ButtonsEditorRenderer(final JTable table) {
        super();
        JButton viewButton2 = new JButton(new AbstractAction("view2") {;
            @Override public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(table, "Viewing");
            }
        });
        JButton editButton2 = new JButton(new AbstractAction("edit2") {;
            @Override public void actionPerformed(ActionEvent e) {
                Object o = table.getModel().getValueAt(table.getSelectedRow(), 0);
                JOptionPane.showMessageDialog(table, "Editing: "+o);
            }
        });
        renderer.setOpaque(true);
        renderer.add(new JButton("view1"));
        renderer.add(new JButton("edit1"));
        editor.setOpaque(true);
        editor.add(viewButton2);
        editor.add(editButton2);
    }
    @Override public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column) {
        renderer.setBackground(isSelected?table.getSelectionBackground()
                                         :table.getBackground());
        return renderer;
    }
    @Override public Component getTableCellEditorComponent(
        JTable table, Object value, boolean isSelected, int row, int column) {
        editor.setBackground(table.getSelectionBackground());
        return editor;
    }
    @Override public Object getCellEditorValue() {
        return "";
    }
}
