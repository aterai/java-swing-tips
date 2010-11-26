package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private static final Color evenColor = new Color(250, 250, 250);
    private final JCheckBox modelCheck = new JCheckBox("edit the cell on single click");
    private final TestModel model = new TestModel();
    public MainPanel() {
        super(new BorderLayout());
        model.addTest(new Test("Name 1", "Comment..."));
        model.addTest(new Test("Name 2", "Test "));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));
        model.addTest(new Test("Name 0", ""));

        final JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if(isRowSelected(row)) {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                }else{
                    c.setForeground(getForeground());
                    c.setBackground((row%2==0)?evenColor:getBackground());
                }
                return c;
            }
        };
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        //table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension());
        table.setShowGrid(false);
        //table.setShowHorizontalLines(false);
        //table.setShowVerticalLines(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setReorderingAllowed(false);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setResizable(false);

        final DefaultTableCellRenderer defalutRenderer = (DefaultTableCellRenderer)table.getDefaultRenderer(Object.class);
        final TestRenderer testRenderer = new TestRenderer();
        final DefaultCellEditor ce = (DefaultCellEditor)table.getDefaultEditor(Object.class);
        modelCheck.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if(modelCheck.isSelected()) {
                    table.setDefaultRenderer(Object.class, testRenderer);
                    table.addMouseListener(testRenderer);
                    table.addMouseMotionListener(testRenderer);
                    ce.setClickCountToStart(1);
                }else{
                    table.setDefaultRenderer(Object.class, defalutRenderer);
                    table.removeMouseListener(testRenderer);
                    table.removeMouseMotionListener(testRenderer);
                    ce.setClickCountToStart(2);
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(modelCheck, BorderLayout.NORTH);
        add(scrollPane);
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

class TestRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {
    private int row = -1;
    private int col = -1;
    @Override public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(!table.isEditing() && this.row==row && this.col==column) {
            setText("<html><u>"+value.toString());
        }else{
            setText(value.toString());
        }
        //setForeground(table.getForeground());
        return this;
    }
    @Override public void mouseMoved(MouseEvent e) {
        JTable table = (JTable)e.getSource();
        Point pt = e.getPoint();
        row = table.rowAtPoint(pt);
        col = table.columnAtPoint(pt);
        if(row<0 || col<0) row = col = -1;
        table.repaint();
    }
    @Override public void mouseExited(MouseEvent e)  {
        JComponent c = (JComponent)e.getSource();
        row =  col = -1;
        c.repaint();
    }
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
}
