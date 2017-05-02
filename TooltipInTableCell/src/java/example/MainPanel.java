package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "List<Icon>"};
    private final Object[][] data = {
        {"aa", Arrays.asList(getOptionPaneIcon("OptionPane.informationIcon"), getOptionPaneIcon("OptionPane.errorIcon"))},
        {"bb", Arrays.asList(getOptionPaneIcon("OptionPane.errorIcon"), getOptionPaneIcon("OptionPane.informationIcon"), getOptionPaneIcon("OptionPane.warningIcon"), getOptionPaneIcon("OptionPane.questionIcon"))},
        {"cc", Arrays.asList(getOptionPaneIcon("OptionPane.questionIcon"), getOptionPaneIcon("OptionPane.errorIcon"), getOptionPaneIcon("OptionPane.warningIcon"))},
        {"dd", Arrays.asList(getOptionPaneIcon("OptionPane.informationIcon"))},
        {"ee", Arrays.asList(getOptionPaneIcon("OptionPane.warningIcon"), getOptionPaneIcon("OptionPane.questionIcon"))}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model) {
        private static final int LIST_ICON_COLUMN = 1;
        @Override public String getToolTipText(MouseEvent e) {
            Point pt = e.getPoint();
            int vrow = rowAtPoint(pt);
            int vcol = columnAtPoint(pt);
            //int mrow = convertRowIndexToModel(vrow);
            int mcol = convertColumnIndexToModel(vcol);
            if (mcol == LIST_ICON_COLUMN) {
                TableCellRenderer tcr = getCellRenderer(vrow, vcol);
                Component c = prepareRenderer(tcr, vrow, vcol); //Component c = tcr.getTableCellRendererComponent(this, getValueAt(vrow, vcol), false, false, vrow, vcol);
                if (c instanceof JPanel) {
                    Rectangle r = getCellRect(vrow, vcol, true);
                    c.setBounds(r);
                    //@see https://stackoverflow.com/questions/10854831/tool-tip-in-jpanel-in-jtable-not-working
                    c.doLayout();
                    pt.translate(-r.x, -r.y);
                    Component l = SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y);
                    if (l instanceof JLabel) {
                        ImageIcon icon = (ImageIcon) ((JLabel) l).getIcon();
                        return icon.getDescription();
                    }
                }
            }
            return super.getToolTipText(e);
        }
        @Override public void updateUI() {
            // Bug ID: 6788475 Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
            // http://bugs.java.com/view_bug.do?bug_id=6788475
            // XXX: set dummy ColorUIResource
            setSelectionForeground(new ColorUIResource(Color.RED));
            setSelectionBackground(new ColorUIResource(Color.RED));
            super.updateUI();
            getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer());
            getColumnModel().getColumn(LIST_ICON_COLUMN).setCellRenderer(new ListIconRenderer());
            setRowHeight(40);
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    public static Icon getOptionPaneIcon(String key) {
        ImageIcon icon = (ImageIcon) UIManager.getIcon(key);
        icon.setDescription(key);
        return icon;
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
        frame.setVisible(true);
    }
}
class ListIconRenderer implements TableCellRenderer {
    private final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        p.removeAll();
        if (isSelected) {
            p.setOpaque(true);
            p.setBackground(table.getSelectionBackground());
        } else {
            p.setOpaque(false);
            //p.setBackground(table.getBackground());
        }
        if (value instanceof List<?>) {
            for (Object o: (List<?>) value) {
                if (o instanceof Icon) {
                    Icon icon = (Icon) o;
                    JLabel label = new JLabel(icon);
                    label.setToolTipText(icon.toString());
                    p.add(label);
                }
            }
        }
        return p;
    }
}
