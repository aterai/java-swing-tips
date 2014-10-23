package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
// import javax.swing.event.*;
import javax.swing.table.*;
// import java.util.*;

public final class MainPanel extends JPanel {
    private static final Color EVEN_COLOR = new Color(245, 245, 245);
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            // ArrayIndexOutOfBoundsException:  0 >= 0
            // Bug ID: JDK-6967479 JTable sorter fires even if the model is empty
            // http://bugs.sun.com/view_bug.do?bug_id=6967479
            //return getValueAt(0, column).getClass();
            switch(column) {
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
    private final JTable table = new JTable(model) {
        @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
            Component c = super.prepareRenderer(tcr, row, column);
            if (isRowSelected(row)) {
                c.setForeground(getSelectionForeground());
                c.setBackground(getSelectionBackground());
            } else {
                c.setForeground(getForeground());
                c.setBackground((row % 2 == 0) ? EVEN_COLOR : getBackground());
            }
            return c;
        }
    };
    public MainPanel() {
        super(new BorderLayout());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Color.RED);
        scroll.getViewport().setBackground(Color.GREEN);
        //table.setBackground(Color.BLUE);
        //table.setOpaque(false);
        //table.setBackground(scroll.getBackground());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setComponentPopupMenu(new TablePopupMenu(table, model));
        //scroll.getViewport().setComponentPopupMenu(makePop());
        //scroll.setComponentPopupMenu(makePop());
        table.setRowSorter(new TableRowSorter<TableModel>(model));

        add(makeToolBox(table),  BorderLayout.NORTH);
        add(makeColorBox(table), BorderLayout.SOUTH);
        add(scroll);
        setPreferredSize(new Dimension(320, 240));
    }

    private static JComponent makeToolBox(final JTable table) {
        Box box = Box.createHorizontalBox();
        box.add(new JCheckBox(new AbstractAction("FillsViewportHeight") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                table.setFillsViewportHeight(cb.isSelected());
            }
        }));
        box.add(new JButton(new AbstractAction("clearSelection") {
            @Override public void actionPerformed(ActionEvent e) {
                table.clearSelection();
            }
        }));
        return box;
    }

    private static JComponent makeColorBox(final JTable table) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JRadioButton r1 = new JRadioButton("WHITE");
        final JRadioButton r2 = new JRadioButton("BLUE");
        ButtonGroup bg = new ButtonGroup();
        ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                table.setBackground(r1.isSelected() ? Color.WHITE : Color.BLUE);
            }
        };
        p.add(new JLabel("table.setBackground: "));
        bg.add(r1); p.add(r1); r1.addActionListener(al);
        bg.add(r2); p.add(r2); r2.addActionListener(al);
        r1.setSelected(true);
        return p;
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
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class TablePopupMenu extends JPopupMenu {
    private final Action deleteAction = new AbstractAction("delete") {
        @Override public void actionPerformed(ActionEvent e) {
            int[] selection = table.getSelectedRows();
            if (selection.length == 0) {
                return;
            }
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    };
    private final Action addAction = new AbstractAction("add") {
        @Override public void actionPerformed(ActionEvent e) {
            model.addRow(new Object[] {"example", model.getRowCount(), false});
        }
    };
    private final DefaultTableModel model;
    private final JTable table;
    public TablePopupMenu(JTable table, DefaultTableModel model) {
        super();
        this.table = table;
        this.model = model;
        add(addAction);
        addSeparator();
        add(deleteAction);
    }
    @Override public void show(Component c, int x, int y) {
        int[] l = table.getSelectedRows();
        deleteAction.setEnabled(l.length > 0);
        super.show(c, x, y);
    }
}
