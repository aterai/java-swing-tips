package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JRadioButton r1 = new JRadioButton("prevent KeyStroke autoStartsEdit");
        JRadioButton r2 = new JRadioButton("prevent mouse from starting edit");
        JRadioButton r3 = new JRadioButton("start cell editing only F2");
        JRadioButton r4 = new JRadioButton("isCellEditable retrun false");

        String[] columnNames = {"CellEditable:false", "Integer", "String"};
        Object[][] data = {
            {"aaa", 12, "eee"}, {"bbb", 5, "ggg"},
            {"CCC", 92, "fff"}, {"DDD", 0, "hhh"}
        };
        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
            @Override public boolean isCellEditable(int row, int col) {
                return col != 0 && !r4.isSelected();
            }
        };
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setShowGrid(false);
        // table.setShowHorizontalLines(false);
        // table.setShowVerticalLines(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // // System.out.println(table.getActionMap().get("startEditing"));
        // InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        // for (KeyStroke ks: im.allKeys()) {
        //     Object actionMapKey = im.get(ks);
        //     if ("startEditing".equals(actionMapKey.toString())) {
        //         System.out.println("startEditing: "+ ks.toString());
        //     }
        // }

        DefaultCellEditor ce = (DefaultCellEditor) table.getDefaultEditor(Object.class);
        ActionListener al = e -> {
            JRadioButton rb = (JRadioButton) e.getSource();
            table.putClientProperty("JTable.autoStartsEdit", !rb.equals(r1) && !rb.equals(r3));
            ce.setClickCountToStart(rb.equals(r2) || rb.equals(r3) ? Integer.MAX_VALUE : 2);
        };

        Box p = Box.createVerticalBox();
        ButtonGroup bg = new ButtonGroup();
        for (AbstractButton b: Arrays.asList(new JRadioButton("default", true), r1, r2, r3, r4)) {
            b.addActionListener(al);
            bg.add(b);
            p.add(b);
        }
        add(p, BorderLayout.NORTH);
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
