package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final TestModel model = new TestModel();
    private final JTable table;
    public MainPanel() {
        super(new BorderLayout());
        table = new JTable(model) {
            @Override public void updateUI() {
                super.updateUI();
                Color sbg = UIManager.getColor("Table.selectionBackground");
                if(sbg!=null) { //Nimbus
                    setSelectionBackground(sbg);
                }
            }
        };

        TableColumnModel columns = table.getColumnModel();
        for(int i=0;i<columns.getColumnCount();i++) {
            columns.getColumn(i).setCellRenderer(new TestRenderer());
        }

        table.setRowSelectionAllowed(true);
        //table.setCellSelectionEnabled(true);
        table.setIntercellSpacing(new Dimension());
        table.setShowGrid(false);
        //table.setShowHorizontalLines(false);
        //table.setShowVerticalLines(false);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setPreferredWidth(0);
        col.setMinWidth(0);
        col.setMaxWidth(0);
        col.setResizable(false);
        //table.removeColumn(col);

        InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke tab    = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        KeyStroke enter  = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke stab   = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
        KeyStroke senter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);
        //KeyStroke sa   = KeyStroke.getKeyStroke(KeyEvent.VK_A, (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
        im.put(tab, im.get(enter));
        im.put(stab, im.get(senter));

        model.addTest(new Test("test1.jpg", "8888"));
        model.addTest(new Test("test1234.jpg", ""));
        model.addTest(new Test("test15354.gif", "comment..."));
        model.addTest(new Test("t.png", "aaaa"));
        model.addTest(new Test("tfasdfasd.jpg", "comment..."));
        model.addTest(new Test("afsdfasdfffffffffffasdfasdf.mpg", "eadfasdf"));
        model.addTest(new Test("fffffffffffasdfasdf", "asdf"));
        model.addTest(new Test("test1.jpg", "fffffffff"));

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu());
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 200));
    }

    class TestCreateAction extends AbstractAction{
        public TestCreateAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            testCreateActionPerformed(evt);
        }
    }
    private void testCreateActionPerformed(ActionEvent e) {
        model.addTest(new Test("New row", ""));
        Rectangle r = table.getCellRect(model.getRowCount()-1, 0, true);
        table.scrollRectToVisible(r);
    }

    class DeleteAction extends AbstractAction{
        public DeleteAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            deleteActionPerformed(evt);
        }
    }
    public void deleteActionPerformed(ActionEvent evt) {
        int[] selection = table.getSelectedRows();
        if(selection==null || selection.length<=0) return;
        for(int i=selection.length-1;i>=0;i--) {
            model.removeRow(table.convertRowIndexToModel(selection[i]));
        }
    }
    class ClearAction extends AbstractAction{
        public ClearAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            table.clearSelection();
        }
    }
    private class TablePopupMenu extends JPopupMenu {
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(new TestCreateAction("add", null));
            add(new ClearAction("clearSelection", null));
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            int[] l = table.getSelectedRows();
            deleteAction.setEnabled(l!=null && l.length>0);
            super.show(c, x, y);
        }
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
class TestRenderer extends DefaultTableCellRenderer {
    private static final DotBorder dotBorder = new DotBorder(2,2,2,2);
    private static final Border emptyBorder  = BorderFactory.createEmptyBorder(2,2,2,2);
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
        if(c instanceof JComponent) {
            int lsi = table.getSelectionModel().getLeadSelectionIndex();
            ((JComponent)c).setBorder(row==lsi?dotBorder:emptyBorder);
            dotBorder.setLastCellFlag(row==lsi&&column==table.getColumnCount()-1);
        }
        return c;
    }
}
class DotBorder extends EmptyBorder {
    private static final BasicStroke dashed = new BasicStroke(
        1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
        10.0f, (new float[] {1.0f}), 0.0f);
    private static final Color dotColor = new Color(200,150,150);
    public DotBorder(int top, int left, int bottom, int right) {
        super(top, left, bottom, right);
    }
    private boolean isLastCell = false;
    public void setLastCellFlag(boolean flag) {
        isLastCell = flag;
    }
    @Override public boolean isBorderOpaque() {
        return true;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D)g;
        g2.translate(x,y);
        g2.setPaint(dotColor);
        g2.setStroke(dashed);
        int cbx = c.getBounds().x;
        if(cbx==0) {
            g2.drawLine(0,0,0,h);
        }
        if(isLastCell) {
            g2.drawLine(w-1,0,w-1,h);
        }
        if(cbx%2==0) {
            g2.drawLine(0,0,w,0);
            g2.drawLine(0,h-1,w,h-1);
        }else{
            g2.drawLine(1,0,w,0);
            g2.drawLine(1,h-1,w,h-1);
        }
        g2.translate(-x,-y);
    }
}
