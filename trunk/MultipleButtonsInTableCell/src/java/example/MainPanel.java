package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
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
        String[] columnNames = {"String", "Button"};
        Object[][] data = {
            {"AAA", ""}, {"CCC", ""}, {"BBB", ""}, {"ZZZ", ""}
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

class ButtonsEditorRenderer extends AbstractCellEditor implements TableCellRenderer,TableCellEditor{
    private final JPanel renderer = new JPanel();
    private final JPanel editor   = new JPanel();
    private final JTable table;
    private final MouseListener ml = new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            ButtonModel m = ((JButton)e.getSource()).getModel();
            if(m.isPressed() && table.isRowSelected(table.getEditingRow()) && !e.isShiftDown()) {
                editor.setBackground(table.getBackground());
            }
        }
    };
    public ButtonsEditorRenderer(JTable t) {
        super();
        this.table = t;
        JButton viewButton2 = new JButton(new AbstractAction("view") {;
            @Override public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
                JOptionPane.showMessageDialog(table, "Viewing");
            }
        });
        JButton editButton2 = new JButton(new AbstractAction("edit") {;
            @Override public void actionPerformed(ActionEvent e) {
                //Object o = table.getModel().getValueAt(table.getSelectedRow(), 0);
                int row = table.convertRowIndexToModel(table.getEditingRow());
                Object o = table.getModel().getValueAt(row, 0);
                fireEditingStopped();
                JOptionPane.showMessageDialog(table, "Editing: "+o);
            }
        });
        viewButton2.addMouseListener(ml);
        viewButton2.setFocusable(false);
        viewButton2.setRolloverEnabled(false);

        editButton2.addMouseListener(ml);
        editButton2.setFocusable(false);
        editButton2.setRolloverEnabled(false);

        renderer.setOpaque(true);
        renderer.add(new JButton("view"));
        renderer.add(new JButton("edit"));
        editor.setOpaque(true);
        editor.add(viewButton2);
        editor.add(editButton2);
        editor.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                fireEditingStopped();
            }
        });
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
