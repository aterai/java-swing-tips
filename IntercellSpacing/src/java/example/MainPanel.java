package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public boolean isCellEditable(int row, int column) {
            return column==2;
        }
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model) {
        @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
            Component c = super.prepareEditor(editor, row, column);
            if(c instanceof JCheckBox) {
                ((JCheckBox)c).setBackground(getSelectionBackground());
            }
            return c;
        }
    };
    public MainPanel() {
        super(new BorderLayout());

        table.setAutoCreateRowSorter(true);
        table.setRowSelectionAllowed(true);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);

        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(0,0));

        JPanel p = new JPanel(new BorderLayout());
        p.add(new JCheckBox(new AbstractAction("setShowVerticalLines") {
            @Override public void actionPerformed(ActionEvent e) {
                Dimension d = table.getIntercellSpacing();
                if(((JCheckBox)e.getSource()).isSelected()) {
                    table.setShowVerticalLines(true);
                    table.setIntercellSpacing(new Dimension(1,d.height));
                }else{
                    table.setShowVerticalLines(false);
                    table.setIntercellSpacing(new Dimension(0,d.height));
                }
            }
        }), BorderLayout.WEST);
        p.add(new JCheckBox(new AbstractAction("setShowHorizontalLines") {
            @Override public void actionPerformed(ActionEvent e) {
                Dimension d = table.getIntercellSpacing();
                if(((JCheckBox)e.getSource()).isSelected()) {
                    table.setShowHorizontalLines(true);
                    table.setIntercellSpacing(new Dimension(d.width,1));
                }else{
                    table.setShowHorizontalLines(false);
                    table.setIntercellSpacing(new Dimension(d.width,0));
                }
            }
        }), BorderLayout.EAST);
        add(p, BorderLayout.NORTH);
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
