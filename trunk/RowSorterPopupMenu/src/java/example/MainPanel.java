package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private static final String[] columnNames = {"String", "Integer", "Boolean"};
    private static final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private static final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());
        table.setRowSorter(new TableRowSorter<TableModel>(model) {
            @Override public void toggleSortOrder(int column) {}
        });
        table.getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(1, SortOrder.DESCENDING)));

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(80);
        col.setMaxWidth(80);
        col.setResizable(false);

        JPopupMenu pop = new TablePopupMenu();
        final JTableHeader header = table.getTableHeader();
        header.setComponentPopupMenu(pop);
        pop.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                //System.out.println("popupMenuWillBecomeInvisible");
                header.setDraggedColumn(null);
                //header.setResizingColumn(null);
                //header.setDraggedDistance(0);
                header.repaint();
            }
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
        });
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 200));
    }
    private class TablePopupMenu extends JPopupMenu {
        private final List<SortAction> actions = Arrays.asList(
            new SortAction(SortOrder.ASCENDING),
            new SortAction(SortOrder.DESCENDING));
            //new SortAction(SortOrder.UNSORTED));
        public TablePopupMenu() {
            super();
            for(Action a:actions) add(a);
        }
        @Override public void show(Component c, int x, int y) {
            JTableHeader h = (JTableHeader)c;
            int i = h.columnAtPoint(new Point(x, y));
            i = h.getTable().convertColumnIndexToModel(i);
            for(SortAction a:actions) a.setIndex(i);
            super.show(c, x, y);
        }
    }
    private class SortAction extends AbstractAction{
        private final SortOrder dir;
        public SortAction(SortOrder dir) {
            super(dir.toString());
            this.dir = dir;
        }
        private int index = -1;
        public void setIndex(int index) {
            this.index = index;
        }
        @Override public void actionPerformed(ActionEvent e) {
            table.getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(index, dir)));
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
