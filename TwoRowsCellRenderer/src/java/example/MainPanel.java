package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        String[] columnNames = {"A", "B"};
        Object[][] data = {
            {"123456789012345678901234567890123456789012345678901234567890", "12345"},
            {"bbb", "abcdefghijklmnopqrstuvwxyz----abcdefghijklmnopqrstuvwxyz"},
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(table.getRowHeight() * 2);
        table.setDefaultRenderer(String.class, new TwoRowsCellRenderer());

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

class TwoRowsCellRenderer extends JPanel implements TableCellRenderer {
    JLabel top = new JLabel();
    JLabel bottom = new JLabel();
    public TwoRowsCellRenderer() {
        super(new GridLayout(2,1,0,0));
        add(top);
        add(bottom);
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected,boolean hasFocus,int row,int column) {
        if(isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        }else{
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        setFont(table.getFont());
        FontMetrics fm  = table.getFontMetrics(table.getFont());
        String text     = (value==null) ? "" : value.toString();
        String first    = text;
        String second   = "";
        int columnWidth = table.getColumnModel().getColumn(column).getWidth();
        int textWidth   = 0;
        for(int i=0; i<text.length(); i++) {
            textWidth += fm.charWidth(text.charAt(i));
            if(textWidth>columnWidth) {
                first  = text.substring(0,i-1);
                second = text.substring(i-1);
                break;
            }
        }
        top.setText(first);
        bottom.setText(second);
        return this;
    }
}
