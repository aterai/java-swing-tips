package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final JRadioButton check1 = new JRadioButton("ASCENDING<->DESCENDING", false);
    private final JRadioButton check2 = new JRadioButton("ASCENDING->DESCENDING->UNSORTED", true);
    public MainPanel() {
        super(new BorderLayout());
        ButtonGroup bg = new ButtonGroup();
        bg.add(check1); bg.add(check2);
        JPanel p = new JPanel(new GridLayout(2,1));
        p.add(check1); p.add(check2);

        TestModel model = new TestModel();
        JTable table = new JTable(model);
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model) {
            @Override public void toggleSortOrder(int column) {
                //if(column>=0 && column<getModelWrapper().getColumnCount() && isSortable(column)) {
                if(check2.isSelected() && column>=0 && column<getModelWrapper().getColumnCount() && isSortable(column)) {
                    List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
                    if(!keys.isEmpty()) {
                        SortKey sortKey = keys.get(0);
                        if(sortKey.getColumn()==column && sortKey.getSortOrder()==SortOrder.DESCENDING) {
                            setSortKeys(null);
                            return;
                        }
                    }
                }
                super.toggleSortOrder(column);
            }
        };
        table.setRowSorter(sorter);
        //sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(1, SortOrder.DESCENDING)));
        //sorter.toggleSortOrder(1);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));

        add(p, BorderLayout.NORTH);
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
