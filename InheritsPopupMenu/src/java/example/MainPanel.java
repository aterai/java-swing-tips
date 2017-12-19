package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    protected final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            // ArrayIndexOutOfBoundsException: 0 >= 0
            // [JDK-6967479] JTable sorter fires even if the model is empty - Java Bug System
            // https://bugs.openjdk.java.net/browse/JDK-6967479
            // return getValueAt(0, column).getClass();
            switch (column) {
              case 0:
                return String.class;
              case 1:
                return Number.class;
              case 2:
                return Boolean.class;
              default:
                return super.getColumnClass(column);
            }
        }
    };
    protected final JTable table = new JTable(model);
    private final JCheckBox cb1 = new JCheckBox("InheritsPopupMenu", true);
    private final JCheckBox cb2 = new JCheckBox("FillsViewportHeight", true);
    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Color.RED);
        scroll.getViewport().setBackground(Color.GREEN);
        scroll.setComponentPopupMenu(new TablePopupMenu());
        //scroll.getViewport().setInheritsPopupMenu(true); // 1.5.0

        //table.setComponentPopupMenu(new TablePopupMenu());
        table.setInheritsPopupMenu(true);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.YELLOW);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //table.setShowGrid(false);
        //table.setShowHorizontalLines(false);
        //table.setShowVerticalLines(false);
        //table.setOpaque(false);
        //table.getTableHeader().setInheritsPopupMenu(true);

        cb1.addActionListener(e -> table.setInheritsPopupMenu(((JCheckBox) e.getSource()).isSelected()));
        cb2.addActionListener(e -> table.setFillsViewportHeight(((JCheckBox) e.getSource()).isSelected()));
        Box box = Box.createHorizontalBox();
        box.add(cb1);
        box.add(cb2);
        add(box, BorderLayout.NORTH);
        add(scroll);
        setPreferredSize(new Dimension(320, 240));
    }

    private class TablePopupMenu extends JPopupMenu {
        private final JMenuItem delete;
        protected TablePopupMenu() {
            super();
            add("add").addActionListener(e -> model.addRow(new Object[] {"example", 0, false}));
            addSeparator();
            delete = add("delete");
            delete.addActionListener(e -> {
                int[] selection = table.getSelectedRows();
                for (int i = selection.length - 1; i >= 0; i--) {
                    model.removeRow(table.convertRowIndexToModel(selection[i]));
                }
            });
        }
        @Override public void show(Component c, int x, int y) {
            delete.setEnabled(table.getSelectedRowCount() > 0);
            super.show(c, x, y);
        }
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
