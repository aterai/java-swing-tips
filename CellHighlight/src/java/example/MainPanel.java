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
        TestModel model = new TestModel();
        model.addTest(new Test("Name 1", "Comment"));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));
        model.addTest(new Test("Name 0", ""));

        //JTable table = new HighlightableTable(model);
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        HighlightListener highlighter = new HighlightListener(table);
        table.addMouseListener(highlighter);
        table.addMouseMotionListener(highlighter);

        table.setDefaultRenderer(Object.class, new HighlightRenderer(highlighter));
        table.setDefaultRenderer(Number.class, new HighlightRenderer(highlighter));

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

class HighlightListener extends MouseAdapter {
    private int row = -1;
    private int col = -1;
    private final JTable table;
    public HighlightListener(JTable table) {
        this.table = table;
    }
    public boolean isHighlightableCell(int row, int column) {
        return this.row==row && this.col==column;
    }
    @Override public void mouseMoved(MouseEvent e) {
        Point pt = e.getPoint();
        row = table.rowAtPoint(pt);
        col = table.columnAtPoint(pt);
        if(row<0 || col<0) row = col = -1;
        table.repaint();
    }
    @Override public void mouseExited(MouseEvent e) {
        row = col = -1;
        table.repaint();
    }
}

class HighlightRenderer extends DefaultTableCellRenderer {
    private final HighlightListener highlighter;
    public HighlightRenderer(HighlightListener highlighter) {
        super();
        this.highlighter = highlighter;
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setHorizontalAlignment((value instanceof Number)?RIGHT:LEFT);
        if(highlighter.isHighlightableCell(row, column)) {
            setBackground(Color.RED);
        }else{
            setBackground(isSelected?table.getSelectionBackground():table.getBackground());
        }
        return this;
    }
}

// class HighlightableTable extends JTable {
//     private final HighlightListener highlighter;
//     public HighlightableTable(TableModel model) {
//         super(model);
//         highlighter = new HighlightListener(this);
//         addMouseListener(highlighter);
//         addMouseMotionListener(highlighter);
//     }
//     @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
//         Component c = super.prepareRenderer(renderer, row, column);
//         if(highlighter.isHighlightableCell(row, column)) {
//             c.setBackground(Color.RED);
//         }else if(isRowSelected(row)) {
//             c.setBackground(getSelectionBackground());
//         }else{
//             c.setBackground(Color.WHITE);
//         }
//         return c;
//     }
// }
