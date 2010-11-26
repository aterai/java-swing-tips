package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private static int BUTTON_COLUMN = 3;
    public MainPanel() {
        super(new BorderLayout());
        final TestModel model = new TestModel();
//         final JTable table = new JTable(model) {
//             @Override public int rowAtPoint(Point pt) {
//                 //http://bugs.sun.com/view_bug.do?bug_id=6291631
//                 return (pt.y<0)?-1:super.rowAtPoint(pt);
//             }
//         };
        final JTable table = new JTable(model);
//         {
//             @Override public void updateUI() {
//                 super.updateUI();
//                 ButtonColumn buttonColumn = new ButtonColumn();
//                 TableColumn column = getColumnModel().getColumn(BUTTON_COLUMN);
//                 column.setCellRenderer(buttonColumn);
//                 column.setCellEditor(buttonColumn);
//             }
//         };
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        table.setRowSorter(sorter);
        sorter.setSortable(BUTTON_COLUMN, false);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        model.addTest(new Test("Name 1", "Comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));
        model.addTest(new Test("Name 0", ""));

        table.addMouseListener(new MouseAdapter() {
            private int targetRow = -1;
            @Override public void mousePressed(MouseEvent e) {
                Point pt = e.getPoint();
                int mcol = table.convertColumnIndexToModel(table.columnAtPoint(pt));
                int vrow = table.rowAtPoint(e.getPoint());
                int mrow = (vrow>=0)?table.convertRowIndexToModel(vrow):-1;
                if(mrow>=0 && mcol==BUTTON_COLUMN) {
                    targetRow = mrow;
                }
            }
            @Override public void mouseReleased(MouseEvent e) {
                Point pt = e.getPoint();
                int mcol = table.convertColumnIndexToModel(table.columnAtPoint(pt));
                int vrow = table.rowAtPoint(e.getPoint());
                int mrow = (vrow>=0)?table.convertRowIndexToModel(vrow):-1;
                if(targetRow==mrow && mcol==BUTTON_COLUMN) {
                    model.removeRow(mrow);
                }
                targetRow = -1;
            }
//             @Override
//             public void mouseClicked(MouseEvent e) {
//                 int col = table.convertColumnIndexToModel(table.columnAtPoint(e.getPoint()));
//                 if(col==BUTTON_COLUMN) {
//                     System.out.println("aaa");
//                     int row = table.convertRowIndexToModel(table.rowAtPoint(e.getPoint()));
//                     //model.removeRow(row);
//                 }
//             }
        });
        ButtonColumn buttonColumn = new ButtonColumn();
        //ButtonColumn buttonColumn = new ButtonColumn(table);
        TableColumn column = table.getColumnModel().getColumn(BUTTON_COLUMN);
        column.setCellRenderer(buttonColumn);
        column.setCellEditor(buttonColumn);
        column.setMinWidth(20);
        column.setMaxWidth(20);
        column.setResizable(false);

        add(new JButton(new AbstractAction("add") {
            @Override public void actionPerformed(ActionEvent e) {
                model.addTest(new Test("Test", "aaaaaaaaaaa"));
            }
        }), BorderLayout.SOUTH);
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
//Swing - JButton inside JTable Cell
//http://forums.sun.com/thread.jspa?threadID=680674
class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
    private static final String LABEL = "X";
    private final JButton renderButton = new JButton(LABEL);
    private final JButton editorButton;
    //public ButtonColumn(final JTable table) {
    public ButtonColumn() {
        super();
        //table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        editorButton = new JButton(LABEL);
        editorButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                fireEditingStopped();
                //int row = table.convertRowIndexToModel(table.getSelectedRow());
                //((DefaultTableModel)table.getModel()).removeRow(row);
            }
        });
        editorButton.setBorder(BorderFactory.createEmptyBorder());
        renderButton.setBorder(BorderFactory.createEmptyBorder());
        editorButton.setFocusPainted(false);
        editorButton.setRolloverEnabled(false);
        renderButton.setToolTipText("Delete(renderButton)");
        editorButton.setToolTipText("Delete(editorButton)");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return renderButton;
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return editorButton;
    }
    @Override public Object getCellEditorValue() {
        return LABEL;
    }
}
