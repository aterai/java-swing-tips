package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    protected static final int START_HEIGHT = 4;
    protected static final int END_HEIGHT = 24;
    protected static final int DELAY = 10;
    protected final String[] columnNames = {"String", "Integer", "Boolean"};
    protected final Object[][] data = {
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
                case 0: return String.class;
                case 1: return Number.class;
                case 2: return Boolean.class;
                default: return super.getColumnClass(column);
            }
        }
    };
    protected final JTable table = new JTable(model);
    protected final Action createAction = new AbstractAction("add") {
        @Override public void actionPerformed(ActionEvent e) {
            createActionPerformed();
        }
    };
    protected final Action deleteAction = new AbstractAction("delete") {
        @Override public void actionPerformed(ActionEvent e) {
            deleteActionPerformed();
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(START_HEIGHT);
        for (int i = 0; i < model.getRowCount(); i++) {
            table.setRowHeight(i, END_HEIGHT);
        }

        JPopupMenu popup = new JPopupMenu() {
            @Override public void show(Component c, int x, int y) {
                if (c instanceof JTable) {
                    deleteAction.setEnabled(((JTable) c).getSelectedRowCount() > 0);
                    super.show(c, x, y);
                }
            }
        };
        popup.add(createAction);
        popup.addSeparator();
        popup.add(deleteAction);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setComponentPopupMenu(popup);
        table.setInheritsPopupMenu(true);
        add(scroll);
        add(new JButton(createAction), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    protected final void createActionPerformed() {
        model.addRow(new Object[] {"New name", model.getRowCount(), false});
        new Timer(DELAY, new ActionListener() {
            private int index = table.convertRowIndexToView(model.getRowCount() - 1);
            private int height = START_HEIGHT;
            @Override public void actionPerformed(ActionEvent e) {
                if (height < END_HEIGHT) {
                    table.setRowHeight(index, height++);
                } else {
                    ((Timer) e.getSource()).stop();
                }
            }
        }).start();
    }

    protected final void deleteActionPerformed() {
        int[] selection = table.getSelectedRows();
        if (selection.length == 0) {
            return;
        }
        new Timer(DELAY, new ActionListener() {
            private int height = END_HEIGHT;
            @Override public void actionPerformed(ActionEvent e) {
                height--;
                if (height > START_HEIGHT) {
                    for (int i = selection.length - 1; i >= 0; i--) {
                        table.setRowHeight(selection[i], height);
                    }
                } else {
                    ((Timer) e.getSource()).stop();
                    for (int i = selection.length - 1; i >= 0; i--) {
                        model.removeRow(table.convertRowIndexToModel(selection[i]));
                    }
                }
            }
        }).start();
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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
