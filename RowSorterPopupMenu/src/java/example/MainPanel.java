package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());
        table.setRowSorter(new TableRowSorter<TableModel>(model) {
            @Override public void toggleSortOrder(int column) { /* Disable header click sorting */ }
        });
        table.getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(1, SortOrder.DESCENDING)));

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(80);
        col.setMaxWidth(80);
        col.setResizable(false);

        JPopupMenu pop = new TableHeaderPopupMenu();
        JTableHeader header = table.getTableHeader();
        header.setComponentPopupMenu(pop);
        pop.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                //System.out.println("popupMenuWillBecomeInvisible");
                header.setDraggedColumn(null);
                //header.setResizingColumn(null);
                //header.setDraggedDistance(0);
                header.repaint();
            }
            @Override public void popupMenuCanceled(PopupMenuEvent e) { /* not needed */ }
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { /* not needed */ }
        });
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TableHeaderPopupMenu extends JPopupMenu {
    private final List<SortAction> actions = Arrays.asList(
        new SortAction(SortOrder.ASCENDING),
        new SortAction(SortOrder.DESCENDING));
        //new SortAction(SortOrder.UNSORTED));
    protected TableHeaderPopupMenu() {
        super();
        actions.forEach(this::add);
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTableHeader) {
            JTableHeader h = (JTableHeader) c;
            int i = h.getTable().convertColumnIndexToModel(h.columnAtPoint(new Point(x, y)));
            actions.forEach(a -> a.setIndex(i));
            super.show(c, x, y);
        }
    }
    private class SortAction extends AbstractAction {
        private final SortOrder dir;
        private int index = -1;

        protected SortAction(SortOrder dir) {
            super(dir.toString());
            this.dir = dir;
        }
        public void setIndex(int index) {
            this.index = index;
        }
        @Override public void actionPerformed(ActionEvent e) {
            JTableHeader h = (JTableHeader) getInvoker();
            h.getTable().getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(index, dir)));
        }
    }
}
