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
    public MainPanel() {
        super(new BorderLayout());
        String[] columnNames = {"DESELECTED", "Integer", "String"};
        Object[][] data = {{true, 1, "BBB"}, {false, 12, "AAA"},
            {true, 2, "DDD"}, {false, 5, "CCC"},
            {true, 3, "EEE"}, {false, 6, "GGG"},
            {true, 4, "FFF"}, {false, 7, "HHH"}};
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        final JTable table = new JTable(model) {
            @Override public void updateUI() {
                super.updateUI();
                //XXX: Nimbus
                TableCellRenderer r = getDefaultRenderer(Boolean.class);
                if(r instanceof JComponent) {
                    ((JComponent)r).updateUI();
                }
            }
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
        table.getColumnModel().getColumn(0).setHeaderRenderer(new HeaderRenderer(table.getTableHeader(), 0));
        //table.getColumnModel().getColumn(1).setHeaderRenderer(new HeaderRenderer(table.getTableHeader(), 1));

        model.addTableModelListener(new TableModelListener() {
            @Override public void tableChanged(TableModelEvent e) {
                if(e.getType()==TableModelEvent.UPDATE && e.getColumn()==0) {
                    int vci = table.convertColumnIndexToView(0);
                    TableColumn column = table.getColumnModel().getColumn(vci);
                    String title = (String)column.getHeaderValue();
                    if("SELECTED".equals(title)) {
                        column.setHeaderValue("INDETERMINATE");
                        table.getTableHeader().repaint();
                    }
                }
            }
        });

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
class HeaderRenderer extends JCheckBox implements TableCellRenderer {
    public HeaderRenderer(JTableHeader header, final int targetColumnIndex) {
        super();
        setOpaque(false);
        setFont(header.getFont());
        header.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JTableHeader header = (JTableHeader)e.getSource();
                JTable table = header.getTable();
                TableColumnModel columnModel = table.getColumnModel();
                int vci = columnModel.getColumnIndexAtX(e.getX());
                int mci = table.convertColumnIndexToModel(vci);
                if(mci == targetColumnIndex) {
                    TableColumn column = columnModel.getColumn(vci);
                    String v = (String)column.getHeaderValue();
                    boolean b = "DESELECTED".equals(v)?true:false;
                    TableModel m = table.getModel();
                    for(int i=0; i<m.getRowCount(); i++) m.setValueAt(b, i, mci);
                    column.setHeaderValue(b?"SELECTED":"DESELECTED");
                    header.repaint();
                }
            }
        });
    }
    @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
        TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
        JLabel l =(JLabel)r.getTableCellRendererComponent(tbl, "Check All", isS, hasF, row, col);
        if("SELECTED".equals(val)) {
            setSelected(true);
            setEnabled(true);
        }else if("DESELECTED".equals(val)) {
            setSelected(false);
            setEnabled(true);
        }else{ //INDETERMINATE
            setSelected(true);
            setEnabled(false);
        }
        l.setIcon(new CheckBoxIcon(this));
        if(l.getPreferredSize().height>1000) { //XXX: Nimbus
            System.out.println(l.getPreferredSize().height);
            l.setPreferredSize(new Dimension(0, 28));
        }
        return l;
    }
}
class CheckBoxIcon implements Icon{
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
        SwingUtilities.paintComponent(g, check, (Container)c, x, y, getIconWidth(), getIconHeight());
    }
}
