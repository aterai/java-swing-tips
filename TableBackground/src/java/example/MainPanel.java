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
    private final JCheckBox check = new JCheckBox("viewport setOpaque");

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        final JScrollPane scroll = new JScrollPane(table);
        scroll.setComponentPopupMenu(new TablePopupMenu());
        //scroll.getViewport().setInheritsPopupMenu(true); // 1.5.0
        table.setInheritsPopupMenu(true);
        //table.setFillsViewportHeight(true);

        scroll.getViewport().setOpaque(true);
        //scroll.getViewport().setBackground(Color.WHITE);
        check.setSelected(true);
        check.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    scroll.getViewport().setOpaque(true);
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    scroll.getViewport().setOpaque(false);
                }
                scroll.repaint();
            }
        });
        JPanel pnl = new JPanel();
        pnl.add(check);
        pnl.add(new JButton(new AbstractAction("Choose background color") {
            private final JColorChooser cc = new JColorChooser();
            @Override public void actionPerformed(ActionEvent evt) {
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        Color color = cc.showDialog(getRootPane(), "background color",
                                                    scroll.getViewport().getBackground());
                        scroll.getViewport().setBackground(color);
                    }
                });
                scroll.repaint();
            }
        }));
        add(scroll);
        add(pnl, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    class TestCreateAction extends AbstractAction {
        public TestCreateAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            model.addRow(new Object[] {"New row", model.getRowCount(), false});
            Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
            table.scrollRectToVisible(r);
        }
    }

    class DeleteAction extends AbstractAction {
        public DeleteAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            int[] selection = table.getSelectedRows();
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
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
            deleteAction.setEnabled(table.getSelectedRows().length > 0);
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
