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
        Object[] columnNames = {Status.INDETERMINATE, "Integer", "String"};
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
                    int mci = 0;
                    int vci = table.convertColumnIndexToView(mci);
                    TableColumn column = table.getColumnModel().getColumn(vci);
                    Object title = column.getHeaderValue();
                    if(!Status.INDETERMINATE.equals(title)) {
                        column.setHeaderValue(Status.INDETERMINATE);
                    }else{
                        int selected = 0, deselected = 0;
                        TableModel m = table.getModel();
                        for(int i=0; i<m.getRowCount(); i++) {
                            if(Boolean.TRUE.equals(m.getValueAt(i, mci))) {
                                selected++;
                            }else{
                                deselected++;
                            }
                        }
                        if(selected==0) {
                            column.setHeaderValue(Status.DESELECTED);
                        }else if(deselected==0) {
                            column.setHeaderValue(Status.SELECTED);
                        }else{
                            return;
                        }
                    }
                    JTableHeader h = table.getTableHeader();
                    h.repaint(h.getHeaderRect(vci));
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
    JLabel label = new JLabel("Check All");
    public HeaderRenderer(JTableHeader header, final int targetColumnIndex) {
        super((String)null);
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
                    Object v = column.getHeaderValue();
                    boolean b = Status.DESELECTED.equals(v)?true:false;
                    TableModel m = table.getModel();
                    for(int i=0; i<m.getRowCount(); i++) m.setValueAt(b, i, mci);
                    column.setHeaderValue(b?Status.SELECTED:Status.DESELECTED);
                    //header.repaint();
                }
            }
        });
    }
    @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
        TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
        JLabel l =(JLabel)r.getTableCellRendererComponent(tbl, null, isS, hasF, row, col);
        if(val instanceof Status) {
            switch((Status)val) {
              case SELECTED:      setSelected(true);  setEnabled(true);  break;
              case DESELECTED:    setSelected(false); setEnabled(true);  break;
              case INDETERMINATE: setSelected(true);  setEnabled(false); break;
            }
        }else{
            setSelected(true);
            setEnabled(false);
        }
        label.setIcon(new ComponentIcon(this));
        l.setIcon(new ComponentIcon(label));
        l.setText(null); //XXX: Nimbus???

//         System.out.println("getHeaderRect: " + tbl.getTableHeader().getHeaderRect(col));
//         System.out.println("getPreferredSize: " + l.getPreferredSize());
//         System.out.println("getMaximunSize: " + l.getMaximumSize());
//         System.out.println("----");
//         if(l.getPreferredSize().height>1000) { //XXX: Nimbus???
//             System.out.println(l.getPreferredSize().height);
//             Rectangle rect = tbl.getTableHeader().getHeaderRect(col);
//             l.setPreferredSize(new Dimension(0, rect.height));
//         }
        return l;
    }
//     @Override public void updateUI() {
//         setText(null); //XXX: Nimbus???
//         super.updateUI();
//     }
}
class ComponentIcon implements Icon{
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
        SwingUtilities.paintComponent(g, cmp, (Container)c, x, y, getIconWidth(), getIconHeight());
    }
}
enum Status { SELECTED, DESELECTED, INDETERMINATE }
