package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import javax.swing.*;

public class MainPanel extends JPanel {
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
    private final JTable table;
    public MainPanel() {
        super(new BorderLayout());
        table = new JTable(model) {
//             private final Color evenColor = new Color(250, 250, 250);
//             @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
//                 Component c = super.prepareRenderer(tcr, row, column);
//                 if(isRowSelected(row)) {
//                     c.setForeground(getSelectionForeground());
//                     c.setBackground(getSelectionBackground());
//                 }else{
//                     c.setForeground(getForeground());
//                     c.setBackground((row%2==0)?evenColor:getBackground());
//                 }
//                 return c;
//             }
            @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
                Component c = super.prepareEditor(editor, row, column);
                if(c instanceof JCheckBox) {
                    JCheckBox b = (JCheckBox)c;
                    b.setBorderPainted(true);
                    b.setBackground(getSelectionBackground());
                }else if(convertColumnIndexToModel(column)==1) {
                    ((JComponent)c).setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
                }
                return c;
            }
        };

        JTextField field = new JTextField();
        field.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        table.setDefaultEditor(Object.class, new DefaultCellEditor(field));

//         JTextField tf2 = new JTextField();
//         tf2.setHorizontalAlignment(JTextField.RIGHT);
//         tf2.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
//         table.setDefaultEditor(Integer.class, new DefaultCellEditor(tf2) {
//             @Override public boolean stopCellEditing() {
//                 Object o = getCellEditorValue();
//                 return (o==null)?false:super.stopCellEditing();
//             }
//             @Override public Object getCellEditorValue() {
//                 Object o = super.getCellEditorValue();
//                 Integer iv;
//                 try{
//                     iv = Integer.valueOf(o.toString());
//                 }catch(NumberFormatException nfe) {
//                     iv = null;
//                 }
//                 return iv;
//             }
//         });

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu());
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 200));
    }

    class TestCreateAction extends AbstractAction {
        public TestCreateAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            model.addRow(new Object[] {"New row", 0, true});
            Rectangle r = table.getCellRect(model.getRowCount()-1, 0, true);
            table.scrollRectToVisible(r);
        }
    }
    class DeleteAction extends AbstractAction {
        public DeleteAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            int[] selection = table.getSelectedRows();
            if(selection.length == 0) {
                return;
            }
            for(int i=selection.length-1;i>=0;i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    }
    private class TablePopupMenu extends JPopupMenu {
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(new TestCreateAction("add", null));
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
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
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
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
