package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
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
                if(e.getStateChange()==ItemEvent.SELECTED) {
                    scroll.getViewport().setOpaque(true);
                }else if(e.getStateChange()==ItemEvent.DESELECTED) {
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
                        Color color = cc.showDialog(MainPanel.this, "background color",
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

    class TestCreateAction extends AbstractAction{
        public TestCreateAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            testCreateActionPerformed(evt);
        }
    }
    private void testCreateActionPerformed(ActionEvent e) {
        model.addRow(new Object[] {"New row", model.getRowCount(), false});
        Rectangle r = table.getCellRect(model.getRowCount()-1, 0, true);
        table.scrollRectToVisible(r);
    }

    class DeleteAction extends AbstractAction{
        public DeleteAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            deleteActionPerformed(evt);
        }
    }
    public void deleteActionPerformed(ActionEvent evt) {
        int[] selection = table.getSelectedRows();
        if(selection==null || selection.length<=0) return;
        for(int i=selection.length-1;i>=0;i--) {
            model.removeRow(table.convertRowIndexToModel(selection[i]));
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
            int[] l = table.getSelectedRows();
            deleteAction.setEnabled(l!=null && l.length>0);
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
