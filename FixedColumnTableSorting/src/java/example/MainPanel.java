package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    public static final int FIXED_COLUMN_RANGE = 2;
    //<blockquote cite="FixedColumnExample.java">
    //@auther Nobuo Tamemasa
    private static final String ES = "";
    private final Object[][] data = {
        {1, 11, "A",  ES,  ES,  ES,  ES,  ES},
        {2, 22,  ES, "B",  ES,  ES,  ES,  ES},
        {3, 33,  ES,  ES, "C",  ES,  ES,  ES},
        {4,  1,  ES,  ES,  ES, "D",  ES,  ES},
        {5, 55,  ES,  ES,  ES,  ES, "E",  ES},
        {6, 66,  ES,  ES,  ES,  ES,  ES, "F"}
    };
    private final Object[] columnNames = {"fixed 1", "fixed 2", "A", "B", "C", "D", "E", "F"};
    //</blockquote>
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return column < FIXED_COLUMN_RANGE ? Integer.class : Object.class;
        }
    };
    private final transient RowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
    private final JButton addButton = new JButton("add");

    public MainPanel() {
        super(new BorderLayout());
        JTable fixedTable = new JTable(model);
        JTable table      = new JTable(model);
        fixedTable.setSelectionModel(table.getSelectionModel());

        for (int i = model.getColumnCount() - 1; i >= 0; i--) {
            if (i < FIXED_COLUMN_RANGE) {
                table.removeColumn(table.getColumnModel().getColumn(i));
                fixedTable.getColumnModel().getColumn(i).setResizable(false);
            } else {
                fixedTable.removeColumn(fixedTable.getColumnModel().getColumn(i));
            }
        }

        fixedTable.setRowSorter(sorter);
        fixedTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        fixedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fixedTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        table.setRowSorter(sorter);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        JScrollPane scroll = new JScrollPane(table);
//         JViewport viewport = new JViewport();
//         viewport.setView(fixedTable);
//         viewport.setPreferredSize(fixedTable.getPreferredSize());
//         scroll.setRowHeader(viewport);

        fixedTable.setPreferredScrollableViewportSize(fixedTable.getPreferredSize());
        scroll.setRowHeaderView(fixedTable);
        scroll.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, fixedTable.getTableHeader());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getRowHeader().setBackground(Color.WHITE);

        //<blockquote cite="http://tips4java.wordpress.com/2008/11/05/fixed-column-table/">
        //@auther Rob Camick
        scroll.getRowHeader().addChangeListener(e -> {
            JViewport viewport = (JViewport) e.getSource();
            scroll.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
        });
        //</blockquote>

        addButton.addActionListener(e -> {
            sorter.setSortKeys(null);
            IntStream.range(0, 100).forEach(i -> model.addRow(new Object[] {i, i + 1, "A" + i, "B" + i}));
        });

        add(scroll);
        add(addButton, BorderLayout.SOUTH);
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
            //for (UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
            //    if ("Nimbus".equals(laf.getName())) {
            //        UIManager.setLookAndFeel(laf.getClassName());
            //    }
            //}
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
