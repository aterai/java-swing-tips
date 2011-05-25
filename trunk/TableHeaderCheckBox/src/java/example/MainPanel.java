package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        String[] columnNames = {"", "Integer", "String"};
        Object[][] data = {{true, 1, "BBB"}, {false, 12, "AAA"},
            {true, 2, "DDD"}, {false, 5, "CCC"},
            {true, 3, "EEE"}, {false, 6, "GGG"},
            {true, 4, "FFF"}, {false, 7, "HHH"}};
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model) {
            @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
                Component c = super.prepareEditor(editor, row, column);
                if(c instanceof JCheckBox) {
                    JCheckBox b = (JCheckBox)c;
                    b.setBackground(getSelectionBackground());
                    b.setBorderPainted(true);
                }
                return c;
            }
        };
        table.getColumnModel().getColumn(0).setHeaderRenderer(
            new HeaderRenderer(table.getTableHeader()));
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(c);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
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
class HeaderRenderer implements TableCellRenderer {
    private final JCheckBox check = new JCheckBox("Check All");
    public HeaderRenderer(JTableHeader header) {
        check.setOpaque(false);
        check.setFont(header.getFont());
        header.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JTable table = ((JTableHeader)e.getSource()).getTable();
                TableColumnModel columnModel = table.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int modelColumn = table.convertColumnIndexToModel(viewColumn);
                if(modelColumn == 0) {
                    check.setSelected(!check.isSelected());
                    TableModel m = table.getModel();
                    Boolean f = check.isSelected();
                    for(int i=0; i<m.getRowCount(); i++) m.setValueAt(f, i, 0);
                    ((JTableHeader)e.getSource()).repaint();
                }
            }
        });
    }
    @Override public Component getTableCellRendererComponent(
            JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
        TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
        JLabel l =(JLabel)r.getTableCellRendererComponent(tbl, val, isS, hasF, row, col);
        l.setIcon(new CheckBoxIcon(check));
        return l;
    }
    private static class CheckBoxIcon implements Icon{
        private final JCheckBox check;
        public CheckBoxIcon(JCheckBox check) {
            this.check = check;
        }
        @Override public int getIconWidth() {
            return check.getPreferredSize().width;
        }
        @Override public int getIconHeight() {
            return check.getPreferredSize().height;
        }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            SwingUtilities.paintComponent(
                g, check, (Container)c, x, y, getIconWidth(), getIconHeight());
        }
    }
}
