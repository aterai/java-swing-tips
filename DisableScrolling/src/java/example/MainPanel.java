package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false},
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model) {
        @Override public String getToolTipText(MouseEvent e) {
            int row = convertRowIndexToModel(rowAtPoint(e.getPoint()));
            TableModel m = getModel();
            return String.format("%s, %s", m.getValueAt(row, 0), m.getValueAt(row, 2));
        }
    };
    private final JScrollPane scroll = new JScrollPane(table) {
        @Override public void updateUI() {
            super.updateUI();
            JPopupMenu jpm = getComponentPopupMenu();
            if (jpm == null && pop != null) {
                SwingUtilities.updateComponentTreeUI(pop);
            }
        }
    };
    private final JCheckBox check    = new JCheckBox("Disable Scrolling");
    private final TablePopupMenu pop = new TablePopupMenu();
    public MainPanel() {
        super(new BorderLayout());

        for (int i = 0; i < 100; i++) {
            model.addRow(new Object[] {"Name " + i, Integer.valueOf(i), Boolean.FALSE});
        }
        table.setAutoCreateRowSorter(true);

        check.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent ie) {
                table.clearSelection();
                JScrollBar bar = scroll.getVerticalScrollBar();
                if (((JCheckBox) ie.getSource()).isSelected()) {
                    bar.setEnabled(false);
                    scroll.setWheelScrollingEnabled(false);
                    table.setEnabled(false);
                    //table.getTableHeader().setEnabled(false);
                    //scroll.setComponentPopupMenu(null);
                } else {
                    bar.setEnabled(true);
                    scroll.setWheelScrollingEnabled(true);
                    table.setEnabled(true);
                    //table.getTableHeader().setEnabled(true);
                    //scroll.setComponentPopupMenu(pop);
                }
            }
        });

        scroll.setComponentPopupMenu(pop);
        table.setInheritsPopupMenu(true);

        add(scroll);
        add(check,  BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    class TestCreateAction extends AbstractAction {
        public TestCreateAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            model.addRow(new Object[] {"New Name", Integer.valueOf(0), Boolean.FALSE});
            Rectangle rect = table.getCellRect(model.getRowCount() - 1, 0, true);
            table.scrollRectToVisible(rect);
        }
    }

    class DeleteAction extends AbstractAction {
        public DeleteAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            int[] selection = table.getSelectedRows();
            if (selection.length == 0) {
                return;
            }
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    }

    private class TablePopupMenu extends JPopupMenu {
        private final Action createAction = new TestCreateAction("add", null);
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(createAction);
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            createAction.setEnabled(!check.isSelected());
            int[] l = table.getSelectedRows();
            deleteAction.setEnabled(l.length > 0);
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
