package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Double", "ALIGN_DECIMAL"};
    private final Object[][] data = {
        {"aaa", 1.4142, 1.4142}, {"bbb", 98.765, 98.765},
        {"CCC", 1.73, 1.73}, {"DDD", 0d, 0d}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        JTable table = new JTable(model) {
            @Override public void updateUI() {
                super.updateUI();
                getColumnModel().getColumn(2).setCellRenderer(new AlignDecimalCellRenderer());
            }
        };
        table.setAutoCreateRowSorter(true);
        table.setRowSelectionAllowed(true);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setFocusable(false);
        table.setIntercellSpacing(new Dimension());
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
        frame.setVisible(true);
    }
}

class AlignDecimalCellRenderer implements TableCellRenderer {
    private final JPanel p = new JPanel(new BorderLayout());
    private final JTextPane textPane = new JTextPane() {
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = 60;
            return d;
        }
    };
    protected AlignDecimalCellRenderer() {
        textPane.setOpaque(false);
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setTabSet(attr, new TabSet(new TabStop[] {
            new TabStop(25f, TabStop.ALIGN_DECIMAL, TabStop.LEAD_NONE)
        }));
        textPane.setParagraphAttributes(attr, false);
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        p.add(textPane, BorderLayout.EAST);
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        textPane.setFont(table.getFont());
        textPane.setText("\t" + Objects.toString(value, ""));
        if (isSelected) {
            textPane.setForeground(table.getSelectionForeground());
            p.setBackground(table.getSelectionBackground());
        } else {
            textPane.setForeground(table.getForeground());
            p.setBackground(table.getBackground());
        }
        return p;
    }
}
