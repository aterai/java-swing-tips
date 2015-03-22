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
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            // ArrayIndexOutOfBoundsException: 0 >= 0
            // Bug ID: JDK-6967479 JTable sorter fires even if the model is empty
            // http://bugs.java.com/view_bug.do?bug_id=6967479
            //return getValueAt(0, column).getClass();
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
    private final JTable table = new JTable(model);
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

        cb1.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                table.setInheritsPopupMenu(cb.isSelected());
            }
        });
        cb2.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                table.setFillsViewportHeight(cb.isSelected());
            }
        });
        Box box = Box.createHorizontalBox();
        box.add(cb1); box.add(cb2);
        add(box, BorderLayout.NORTH);
        add(scroll);
        setPreferredSize(new Dimension(320, 200));
    }

    private class TablePopupMenu extends JPopupMenu {
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(new TestCreateAction("add", null));
            //add(new ClearAction("clearSelection", null));
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            int[] l = table.getSelectedRows();
            deleteAction.setEnabled(l.length > 0);
            super.show(c, x, y);
        }
    }

    class TestCreateAction extends AbstractAction {
        public TestCreateAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            model.addRow(new Object[] {"example", 0, false});
        }
    }
    class DeleteAction extends AbstractAction {
        public DeleteAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            int[] selection = table.getSelectedRows();
            if (selection.length == 0) {
                return;
            }
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
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
