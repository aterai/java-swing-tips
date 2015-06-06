package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final int MODEL_COLUMN_INDEX = 0;
    private final Object[] columnNames = {Status.INDETERMINATE, "Integer", "String"};
    private final Object[][] data = {
        {true, 1, "BBB"}, {false, 12, "AAA"},
        {true, 2, "DDD"}, {false,  5, "CCC"},
        {true, 3, "EEE"}, {false,  6, "GGG"},
        {true, 4, "FFF"}, {false,  7, "HHH"}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model) {
        @Override public void updateUI() {
            // Bug ID: 6788475 Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
            // http://bugs.java.com/view_bug.do?bug_id=6788475
            // XXX: set dummy ColorUIResource
            setSelectionForeground(new ColorUIResource(Color.RED));
            setSelectionBackground(new ColorUIResource(Color.RED));
            super.updateUI();
            TableModel m = getModel();
            for (int i = 0; i < m.getColumnCount(); i++) {
                TableCellRenderer r = getDefaultRenderer(m.getColumnClass(i));
                if (r instanceof Component) {
                    SwingUtilities.updateComponentTreeUI((Component) r);
                }
            }
        }
        @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
            Component c = super.prepareEditor(editor, row, column);
            if (c instanceof JCheckBox) {
                JCheckBox b = (JCheckBox) c;
                b.setBackground(getSelectionBackground());
                b.setBorderPainted(true);
            }
            return c;
        }
    };

    public MainPanel() {
        super(new BorderLayout());

        TableCellRenderer renderer = new HeaderRenderer(table.getTableHeader(), MODEL_COLUMN_INDEX);
//         for (int i = 0; i < table.getColumnCount(); i++) {
//             table.getColumnModel().getColumn(i).setHeaderRenderer(renderer);
//         }
//         table.getColumnModel().getColumn(MODEL_COLUMN_INDEX).setHeaderValue(Status.INDETERMINATE);
        table.getColumnModel().getColumn(MODEL_COLUMN_INDEX).setHeaderRenderer(renderer);
        table.setFillsViewportHeight(true);

        model.addTableModelListener(new HeaderCheckBoxHandler(table, MODEL_COLUMN_INDEX));

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
//             for (UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
//                 if ("Nimbus".equals(laf.getName())) {
//                     UIManager.setLookAndFeel(laf.getClassName());
//                 }
//             }
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

class HeaderRenderer extends JCheckBox implements TableCellRenderer {
    private final JLabel label = new JLabel("Check All");
    private final int targetColumnIndex;
    public HeaderRenderer(JTableHeader header, int index) {
        super((String) null);
        this.targetColumnIndex = index;
        setOpaque(false);
        setFont(header.getFont());
        header.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JTableHeader header = (JTableHeader) e.getComponent();
                JTable table = header.getTable();
                TableColumnModel columnModel = table.getColumnModel();
                int vci = columnModel.getColumnIndexAtX(e.getX());
                int mci = table.convertColumnIndexToModel(vci);
                if (mci == targetColumnIndex) {
                    TableColumn column = columnModel.getColumn(vci);
                    Object v = column.getHeaderValue();
                    boolean b = Status.DESELECTED.equals(v);
                    TableModel m = table.getModel();
                    for (int i = 0; i < m.getRowCount(); i++) {
                        m.setValueAt(b, i, mci);
                    }
                    column.setHeaderValue(b ? Status.SELECTED : Status.DESELECTED);
                    //header.repaint();
                }
            }
        });
    }
    @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
        TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
        JLabel l = (JLabel) r.getTableCellRendererComponent(tbl, val, isS, hasF, row, col);
        if (targetColumnIndex == tbl.convertColumnIndexToModel(col)) {
            if (val instanceof Status) {
                switch ((Status) val) {
                  case SELECTED:      setSelected(true);  setEnabled(true);  break;
                  case DESELECTED:    setSelected(false); setEnabled(true);  break;
                  case INDETERMINATE: setSelected(true);  setEnabled(false); break;
                  default:            throw new AssertionError("Unknown Status");
                }
            } else {
                setSelected(true); setEnabled(false);
            }
            label.setIcon(new ComponentIcon(this));
            l.setIcon(new ComponentIcon(label));
            l.setText(null);
        }
//         System.out.println("getHeaderRect: " + tbl.getTableHeader().getHeaderRect(col));
//         System.out.println("getPreferredSize: " + l.getPreferredSize());
//         System.out.println("getMaximunSize: " + l.getMaximumSize());
//         System.out.println("----");
//         if (l.getPreferredSize().height > 1000) { //XXX: Nimbus???
//             System.out.println(l.getPreferredSize().height);
//             Rectangle rect = tbl.getTableHeader().getHeaderRect(col);
//             l.setPreferredSize(new Dimension(0, rect.height));
//         }
        return l;
    }
//     @Override public void updateUI() {
//         setText(null); //XXX: Nimbus??? Header height bug???
//         super.updateUI();
//     }
}

class HeaderCheckBoxHandler implements TableModelListener {
    private final JTable table;
    private final int targetColumnIndex;
    public HeaderCheckBoxHandler(JTable table, int index) {
        this.table = table;
        this.targetColumnIndex = index;
    }
    @Override public void tableChanged(TableModelEvent e) {
        int vci = table.convertColumnIndexToView(targetColumnIndex);
        TableColumn column = table.getColumnModel().getColumn(vci);
        Object status = column.getHeaderValue();
        TableModel m = table.getModel();
//         // @see http://ateraimemo.com/Swing/TableModelEvent.html
//         if (e.getType() == TableModelEvent.DELETE) {
//             if (m.getRowCount() == 0) {
//                 column.setHeaderValue(Status.DESELECTED);
//             } else if (Status.INDETERMINATE.equals(status)) {
//                 boolean selected = true;
//                 boolean deselected = true;
//                 for (int i = 0; i < m.getRowCount(); i++) {
//                     Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
//                     selected &= b;
//                     deselected &= !b;
//                 }
//                 if (deselected) {
//                     column.setHeaderValue(Status.DESELECTED);
//                 } else if (selected) {
//                     column.setHeaderValue(Status.SELECTED);
//                 } else {
//                     return;
//                 }
//             } else {
//                 column.setHeaderValue(Status.INDETERMINATE);
//             }
//         } else if (e.getType() == TableModelEvent.INSERT && !Status.INDETERMINATE.equals(status)) {
//             boolean selected = Status.DESELECTED.equals(status);
//             boolean deselected = Status.SELECTED.equals(status);
//             for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
//                 Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
//                 selected &= b;
//                 deselected &= !b;
//             }
//             if (selected && m.getRowCount() == 1) {
//                 column.setHeaderValue(Status.SELECTED);
//             } else if (selected || deselected) {
//                 column.setHeaderValue(Status.INDETERMINATE);
//             }
//         } else
        if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == targetColumnIndex) {
            //System.out.println("UPDATE");
            if (Status.INDETERMINATE.equals(status)) {
                boolean selected = true;
                boolean deselected = true;
                for (int i = 0; i < m.getRowCount(); i++) {
                    Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
                    selected &= b;
                    deselected &= !b;
                    if (selected == deselected) {
                        return;
                    }
                }
                if (deselected) {
                    column.setHeaderValue(Status.DESELECTED);
                } else if (selected) {
                    column.setHeaderValue(Status.SELECTED);
                } else {
                    return;
                }
            } else {
                column.setHeaderValue(Status.INDETERMINATE);
            }
        }
        JTableHeader h = table.getTableHeader();
        h.repaint(h.getHeaderRect(vci));
    }
}

class ComponentIcon implements Icon {
    private final JComponent cmp;
    public ComponentIcon(JComponent cmp) {
        this.cmp = cmp;
    }
    @Override public int getIconWidth() {
        return cmp.getPreferredSize().width;
    }
    @Override public int getIconHeight() {
        return cmp.getPreferredSize().height;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        SwingUtilities.paintComponent(g, cmp, c.getParent(), x, y, getIconWidth(), getIconHeight());
    }
}

enum Status { SELECTED, DESELECTED, INDETERMINATE }
