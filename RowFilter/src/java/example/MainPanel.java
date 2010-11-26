package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private static final Color evenColor = new Color(240, 255, 250);
    private final JCheckBox check1;
    private final JCheckBox check2;
    private final TestModel model = new TestModel();
    private final TableRowSorter<TestModel> sorter = new TableRowSorter<TestModel>(model);
    private final Set<RowFilter<TestModel,Integer>> filters = new HashSet<RowFilter<TestModel,Integer>>(2);
    private final JTable table;
    public MainPanel() {
        super(new BorderLayout());
        table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if(isRowSelected(row)) {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                }else{
                    c.setForeground(getForeground());
                    c.setBackground((row%2==0)?evenColor:table.getBackground());
                }
                return c;
            }
        };
        table.setRowSorter(sorter);
        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));
        //table.setRowSorter(sorter); <- IndexOutOfBoundsException: Invalid range (add, delete, etc.)

        JScrollPane scrollPane = new JScrollPane(table);
        //scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        table.setComponentPopupMenu(new TablePopupMenu());
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension());
        table.setShowGrid(false);
        //table.setShowHorizontalLines(false);
        //table.setShowVerticalLines(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        final RowFilter<TestModel,Integer> filter1 = new RowFilter<TestModel,Integer>() {
            @Override public boolean include(Entry<? extends TestModel, ? extends Integer> entry) {
                TestModel model = entry.getModel();
                Test t = model.getTest(entry.getIdentifier());
                return !t.getComment().trim().isEmpty();
            }
        };
        final RowFilter<TestModel,Integer> filter2 = new RowFilter<TestModel,Integer>() {
            @Override public boolean include(Entry<? extends TestModel, ? extends Integer> entry) {
                return entry.getIdentifier() % 2 == 0;
            }
        };
        //sorter.setRowFilter(RowFilter.andFilter(filters));
        //sorter.setRowFilter(filter1);

        Box box = Box.createHorizontalBox();
        box.add(check1 = new JCheckBox(new AbstractAction("comment!=null") {
            @Override public void actionPerformed(ActionEvent evt) {
                JCheckBox cb = (JCheckBox)evt.getSource();
                if(cb.isSelected()) {
                    filters.add(filter1);
                }else{
                    filters.remove(filter1);
                }
                sorter.setRowFilter(RowFilter.andFilter(filters));
            }
        }));
        box.add(check2 = new JCheckBox(new AbstractAction("idx%2==0") {
            @Override public void actionPerformed(ActionEvent evt) {
                JCheckBox cb = (JCheckBox)evt.getSource();
                if(cb.isSelected()) {
                    filters.add(filter2);
                }else{
                    filters.remove(filter2);
                }
                sorter.setRowFilter(RowFilter.andFilter(filters));
            }
        }));
        add(box, BorderLayout.NORTH);
        add(scrollPane);
        setPreferredSize(new Dimension(320, 180));
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
        model.addTest(new Test("example", ""));
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

    private class TablePopupMenu extends JPopupMenu {
        private final Action addAction = new TestCreateAction("add", null);
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(addAction);
            //add(new ClearAction("clearSelection", null));
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            addAction.setEnabled(!check1.isSelected() && !check2.isSelected());
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
