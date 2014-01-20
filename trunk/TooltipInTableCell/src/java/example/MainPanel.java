package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());

        String[] columnNames = {"String", "List<Icon>"};
        Object[][] data = {
            {"aaa", Arrays.<Icon>asList(getOptionPaneIcon("OptionPane.informationIcon"), getOptionPaneIcon("OptionPane.errorIcon"))},
            {"bbb", Arrays.<Icon>asList(getOptionPaneIcon("OptionPane.warningIcon"), getOptionPaneIcon("OptionPane.questionIcon"))}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model) {
            @Override public String getToolTipText(MouseEvent e) {
                Point pt = e.getPoint();
                int vrow = rowAtPoint(pt);
                int vcol = columnAtPoint(pt);
                //int mrow = convertRowIndexToModel(vrow);
                int mcol = convertColumnIndexToModel(vcol);
                if(mcol==1) {
                    TableCellRenderer tcr = getCellRenderer(vrow, vcol);
                    Component c = prepareRenderer(tcr, vrow, vcol); //Component c = tcr.getTableCellRendererComponent(this, getValueAt(vrow, vcol), false, false, vrow, vcol);
                    if(c instanceof JPanel) {
                        Rectangle r = getCellRect(vrow, vcol, true);
                        c.setBounds(r);
                        //@see http://stackoverflow.com/questions/10854831/tool-tip-in-jpanel-in-jtable-not-working
                        c.doLayout();
                        pt.translate(-r.x, -r.y);
                        Component l = SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y);
                        if(l!=null && l instanceof JLabel) {
                            ImageIcon icon = (ImageIcon)((JLabel)l).getIcon();
                            return icon.getDescription();
                        }
                    }
                }
                return super.getToolTipText(e);
            }
        };
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(1).setCellRenderer(new ListIconRenderer());
        table.setRowHeight(40);

        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    public static Icon getOptionPaneIcon(String key) {
        ImageIcon icon = (ImageIcon)UIManager.getIcon(key);
        icon.setDescription(key);
        return icon;
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
class ListIconRenderer extends JPanel implements TableCellRenderer {
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        removeAll();
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        if(value != null && value instanceof List<?>) {
            for(Object o: (List<?>) value) {
                if(o!=null && o instanceof Icon) {
                    Icon icon = (Icon)o;
                    JLabel label = new JLabel(icon);
                    label.setToolTipText(icon.toString());
                    add(label);
                }
            }
        }
        return this;
    }
}
