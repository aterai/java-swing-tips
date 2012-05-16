package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer"};
    Object[][] data = {
        {"aaa", 12}, {"bbb", 5}, {"CCC", 92}, {"DDD", 0}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table;
    private final JCheckBox checkbox    = new JCheckBox("terminateEditOnFocusLost", true);
    private final JCheckBox focusCheck  = new JCheckBox("DefaultCellEditor:focusLost", true);
    private final JCheckBox headerCheck = new JCheckBox("TableHeader:mousePressed", true);
    public MainPanel() {
        super(new BorderLayout());
        table = new JTable(model) {
            private final Color evenColor = new Color(250, 250, 250);
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if(isRowSelected(row)) {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                }else{
                    c.setForeground(getForeground());
                    c.setBackground((row%2==0)?evenColor:getBackground());
                }
                return c;
            }
        };
        table.setAutoCreateRowSorter(true);

// Bug ID: 4330950 Lost newly entered data in the cell when resizing column width
// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4330950
//         frame.addWindowListener(new WindowAdapter() {
//             @Override public void windowClosing(WindowEvent e) {
// //                 if(table.isEditing()) {
// //                     table.getCellEditor().stopCellEditing();
// //                 }
// //                 System.out.println(table.getValueAt(0,1));
//             }
//         });
//         frame.addWindowStateListener(new WindowStateListener() {
//             public void windowStateChanged(WindowEvent e) {
//                 if(frame.getExtendedState()==JFrame.MAXIMIZED_BOTH && table.isEditing()) {
//                     table.getCellEditor().stopCellEditing();
//                 }
//             }
//         });

        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        DefaultCellEditor dce = (DefaultCellEditor)table.getDefaultEditor(Object.class);
        dce.getComponent().addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                if(!focusCheck.isSelected()) return;
                if(table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
            }
        });
        //table.setSurrendersFocusOnKeystroke(true);

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if(!headerCheck.isSelected()) return;
                if(table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
            }
        });

// Bug ID: 4330950 Lost newly entered data in the cell when resizing column width
// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4330950
//         table.getTableHeader().addComponentListener(new ComponentAdapter() {
//             public void componentResized(ComponentEvent e) {
//                 System.out.println("componentResized");
//                 if(table.isEditing()) {
//                     table.getCellEditor().stopCellEditing();
//                 }
//             }
//         });

        checkbox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                table.putClientProperty("terminateEditOnFocusLost", checkbox.isSelected());
            }
        });

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JComboBox combobox = makeComboBox(new String[] {
            "AUTO_RESIZE_OFF",
            "AUTO_RESIZE_ALL_COLUMNS"
        });
        combobox.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()!=ItemEvent.SELECTED) return;
                JComboBox cb = (JComboBox)e.getSource();
                if("AUTO_RESIZE_OFF".equals(cb.getSelectedItem())) {
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                }else{
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                }
            }
        });

        JPanel box = new JPanel(new GridLayout(4,0));
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        box.add(checkbox);
        box.add(focusCheck);
        box.add(headerCheck);
        box.add(combobox);

        add(box, BorderLayout.NORTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    @SuppressWarnings("unchecked")
    private static JComboBox makeComboBox(Object[] model) {
        return new JComboBox(model);
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

        JFrame frame2 = new JFrame("@title@"+2);
        frame2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame2.getContentPane().add(new MainPanel());
        frame2.pack();
        frame2.setLocation(frame.getX()+50, frame.getY()+50);

        frame2.setVisible(true);
        frame.setVisible(true);
    }
}
