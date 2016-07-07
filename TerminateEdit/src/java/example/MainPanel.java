package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final Color EVEN_COLOR = new Color(250, 250, 250);
    private final String[] columnNames = {"String", "Integer"};
    private final Object[][] data = {
        {"aaa", 12}, {"bbb", 5}, {"CCC", 92}, {"DDD", 0}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
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
                c.setBackground(row % 2 == 0 ? EVEN_COLOR : getBackground());
            }
            return c;
        }
    };
    private final JComboBox<? extends Enum> combobox = new JComboBox<>(AutoResizeMode.values());
    private final JCheckBox focusCheck  = new JCheckBox("DefaultCellEditor:focusLost", true);
    private final JCheckBox headerCheck = new JCheckBox("TableHeader:mousePressed", true);
    private final JCheckBox teoflCheck  = new JCheckBox(new AbstractAction("terminateEditOnFocusLost") {
        @Override public void actionPerformed(ActionEvent e) {
            JCheckBox c = (JCheckBox) e.getSource();
            table.putClientProperty("terminateEditOnFocusLost", c.isSelected());
        }
    });

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);

//         // Bug ID: 4330950 Lost newly entered data in the cell when resizing column width
//         // http://bugs.java.com/view_bug.do?bug_id=4330950
//         frame.addWindowListener(new WindowAdapter() {
//             @Override public void windowClosing(WindowEvent e) {
// //                 if (table.isEditing()) {
// //                     table.getCellEditor().stopCellEditing();
// //                 }
// //                 System.out.println(table.getValueAt(0, 1));
//             }
//         });
//         frame.addWindowStateListener(new WindowStateListener() {
//             @Override public void windowStateChanged(WindowEvent e) {
//                 if (frame.getExtendedState() == Frame.MAXIMIZED_BOTH && table.isEditing()) {
//                     table.getCellEditor().stopCellEditing();
//                 }
//             }
//         });

        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        DefaultCellEditor dce = (DefaultCellEditor) table.getDefaultEditor(Object.class);
        dce.getComponent().addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                if (!focusCheck.isSelected()) {
                    return;
                }
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
            }
        });
        //table.setSurrendersFocusOnKeystroke(true);

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (!headerCheck.isSelected()) {
                    return;
                }
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
            }
        });

//         // Bug ID: 4330950 Lost newly entered data in the cell when resizing column width
//         // http://bugs.java.com/view_bug.do?bug_id=4330950
//         table.getTableHeader().addComponentListener(new ComponentAdapter() {
//             @Override public void componentResized(ComponentEvent e) {
//                 System.out.println("componentResized");
//                 if (table.isEditing()) {
//                     table.getCellEditor().stopCellEditing();
//                 }
//             }
//         });

        teoflCheck.setSelected(true);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        combobox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                table.setAutoResizeMode(((AutoResizeMode) e.getItem()).mode);
            }
        });

        JPanel box = new JPanel(new GridLayout(4, 0));
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(teoflCheck);
        box.add(focusCheck);
        box.add(headerCheck);
        box.add(combobox);

        add(box, BorderLayout.NORTH);
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

        JFrame frame2 = new JFrame("@title@" + 2);
        frame2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame2.getContentPane().add(new MainPanel());
        frame2.pack();
        frame2.setLocation(frame.getX() + 50, frame.getY() + 50);

        frame2.setVisible(true);
        frame.setVisible(true);
    }
}

enum AutoResizeMode {
    AUTO_RESIZE_OFF(JTable.AUTO_RESIZE_OFF),
    AUTO_RESIZE_ALL_COLUMNS(JTable.AUTO_RESIZE_ALL_COLUMNS);
    public int mode;
    AutoResizeMode(int mode) {
        this.mode = mode;
    }
}
