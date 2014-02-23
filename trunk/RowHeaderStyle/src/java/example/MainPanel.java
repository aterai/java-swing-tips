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
        {"ccc", 92, true}, {"ddd", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
        @Override public boolean isCellEditable(int row, int column) {
            return column != 0;
        }
    };
    private final JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());

        table.setAutoCreateRowSorter(true);
        table.setRowHeight(24);
        //table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //table.getTableHeader().setReorderingAllowed(false);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setCellRenderer(new RowHeaderRenderer(table));

        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
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

class RowHeaderRenderer extends JLabel implements TableCellRenderer {
    private int rollOverRowIndex = -1;

    public RowHeaderRenderer(JTable table) {
        super();
        RollOverListener rol = new RollOverListener();
        table.addMouseListener(rol);
        table.addMouseMotionListener(rol);
    }
    @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
        TableCellRenderer tcr = tbl.getTableHeader().getDefaultRenderer();
        boolean f = row==rollOverRowIndex;
        JLabel l = (JLabel)tcr.getTableCellRendererComponent(tbl, val, isS, f?f:hasF, -1, -1);
        if(tcr.getClass().getName().indexOf("XPDefaultRenderer")>=0) {
            l.setOpaque(!f);
            this.setIcon(new ComponentIcon(l));
            return this;
        }else{
            return l;
        }
    }
    class RollOverListener extends MouseAdapter {
//         @Override public void mouseMoved(MouseEvent e) {
//             JTable table = (JTable)e.getSource();
//             Point pt = e.getPoint();
//             int column = table.convertColumnIndexToModel(table.columnAtPoint(pt));
//             rollOverRowIndex = (column==0)?table.rowAtPoint(pt):-1;
//             table.repaint();
//         }
//         @Override public void mouseExited(MouseEvent e) {
//             JTable table = (JTable)e.getSource();
//             rollOverRowIndex = -1;
//             table.repaint();
//         }
        @Override public void mouseMoved(MouseEvent e) {
            JTable table = (JTable)e.getComponent();
            Point pt = e.getPoint();
            int col = table.columnAtPoint(pt);
            int column = table.convertColumnIndexToModel(col);
            if(column!=0) { return; }

            int prevRow = rollOverRowIndex;
            rollOverRowIndex = table.rowAtPoint(pt);
            if(rollOverRowIndex == prevRow) { return; }
            Rectangle repaintRect;
            if(rollOverRowIndex >= 0) {
                Rectangle r = table.getCellRect(rollOverRowIndex, col, false);
                if(prevRow >= 0) {
                    repaintRect = r.union(table.getCellRect(prevRow, col, false));
                }else{
                    repaintRect = r;
                }
            }else{
                repaintRect = table.getCellRect(prevRow, col, false);
            }
            table.repaint(repaintRect);
        }
        @Override public void mouseExited(MouseEvent e) {
            JTable table = (JTable)e.getComponent();
            Point pt = e.getPoint();
            int col = table.columnAtPoint(pt);
            int column = table.convertColumnIndexToModel(col);
            if(column!=0) { return; }

            if(rollOverRowIndex >= 0) {
                table.repaint(table.getCellRect(rollOverRowIndex, col, false));
            }
            rollOverRowIndex = -1;
        }
    }
}

class ComponentIcon implements Icon {
    private final JComponent cmp;
    public ComponentIcon(JComponent cmp) {
        this.cmp = cmp;
    }
    @Override public int getIconWidth() {
        return 4000; //Short.MAX_VALUE;
    }
    @Override public int getIconHeight() {
        return cmp.getPreferredSize().height + 4; //XXX: +4 for Windows 7
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        SwingUtilities.paintComponent(g, cmp, c.getParent(), x, y, getIconWidth(), getIconHeight());
    }
}
