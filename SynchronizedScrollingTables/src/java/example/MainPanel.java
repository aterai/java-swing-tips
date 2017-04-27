package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private static final int FIXED_COLUMN_RANGE = 2;
    //<blockquote cite="FixedColumnExample.java">
    //@auther Nobuo Tamemasa
    private static final String ES = "";
    protected final Object[][] data = {
        {1, 11, "A",  ES,  ES}, {2, 22, ES, "B", ES},
        {3, 33,  ES,  ES, "C"}, {4,  1, ES,  ES, ES},
        {5, 55,  ES,  ES,  ES}, {6, 66, ES,  ES, ES}
    };
    protected final String[] columnNames = {"1", "2", "a", "b", "c"};
    //</blockquote>
    protected final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return column < FIXED_COLUMN_RANGE ? Integer.class : Object.class;
        }
    };
    protected final JTable table;

    public MainPanel() {
        super(new BorderLayout());

        JTable leftTable = makeTable(model);
        table = makeTable(model);

        table.setAutoCreateRowSorter(true);
        leftTable.setRowSorter(table.getRowSorter());
        leftTable.setSelectionModel(table.getSelectionModel());

        for (int i = model.getColumnCount() - 1; i >= 0; i--) {
            if (i < FIXED_COLUMN_RANGE) {
                table.removeColumn(table.getColumnModel().getColumn(i));
                leftTable.getColumnModel().getColumn(i).setResizable(false);
            } else {
                leftTable.removeColumn(leftTable.getColumnModel().getColumn(i));
            }
        }
        JScrollPane scroll1 = new JScrollPane(leftTable);
        //scroll1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll1.setVerticalScrollBar(new JScrollBar(Adjustable.VERTICAL) {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 0;
                return d;
            }
        });
        JScrollPane scroll2 = new JScrollPane(table);
        scroll2.getVerticalScrollBar().setModel(scroll1.getVerticalScrollBar().getModel());

        JSplitPane split = new JSplitPane();
        split.setResizeWeight(.3);
        //split.setDividerSize(0);
        split.setLeftComponent(scroll1);
        split.setRightComponent(scroll2);

        JButton button = new JButton("add");
        button.addActionListener(e -> {
            table.getRowSorter().setSortKeys(null);
            IntStream.range(0, 100).forEach(i -> model.addRow(new Object[] {i, i + 1, "A" + i, "B" + i}));
        });

        add(split);
        add(button, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JTable makeTable(TableModel model) {
        JTable table = new JTable(model);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension());
        //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        return table;
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
