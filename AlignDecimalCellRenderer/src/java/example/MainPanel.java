package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Double", "ALIGN_DECIMAL"};
    private final Object[][] data = {
        {"aaa", 1.4142, 1.4142}, {"bbb", 98.765, 98.765},
        {"CCC", 1.73, 1.73}, {"DDD", 0d, 0d}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private MainPanel() {
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
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JTextPane textPane = new JTextPane() {
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = 60;
            return d;
        }
        @Override public void updateUI() {
            super.updateUI();
            setOpaque(false);
            putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            EventQueue.invokeLater(() -> {
                // MutableAttributeSet attr = new SimpleAttributeSet();
                Style attr = getStyle(StyleContext.DEFAULT_STYLE);
                StyleConstants.setTabSet(attr, new TabSet(new TabStop[] {new TabStop(25f, TabStop.ALIGN_DECIMAL, TabStop.LEAD_NONE)}));
                setParagraphAttributes(attr, false);
            });
        }
    };
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        textPane.setFont(table.getFont());
        textPane.setText("\t" + Objects.toString(value, ""));
        if (isSelected) {
            textPane.setForeground(table.getSelectionForeground());
            panel.setBackground(table.getSelectionBackground());
        } else {
            textPane.setForeground(table.getForeground());
            panel.setBackground(table.getBackground());
        }
        panel.add(textPane, BorderLayout.EAST);
        return panel;
    }
}
