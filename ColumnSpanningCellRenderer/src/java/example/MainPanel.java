package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final String SEE = "See Also: Constan Field Values";
    private final String[] columnNames = {"AAA", "BBB"};
    private final Object[][] data = {
        {new Test("errorIcon",       UIManager.getIcon("OptionPane.errorIcon"),       "public static final int ERROR_MESSAGE\nUsed for error messages."), SEE},
        {new Test("informationIcon", UIManager.getIcon("OptionPane.informationIcon"), "public static final int INFORMATION_MESSAGE\nUsed for information messages."), SEE},
        {new Test("questionIcon",    UIManager.getIcon("OptionPane.questionIcon"),    "public static final int QUESTION_MESSAGE\nUsed for questions."), SEE},
        {new Test("warningIcon",     UIManager.getIcon("OptionPane.warningIcon"),     "public static final int WARNING_MESSAGE\nUsed for warning messages."), SEE},
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowSelectionAllowed(true);
        //table.setFocusable(false);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setRowHeight(56);
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            TableColumn c = table.getColumnModel().getColumn(i);
            c.setCellRenderer(new ColumnSpanningCellRenderer());
            c.setMinWidth(50);
        }
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

class ColumnSpanningCellRenderer extends JPanel implements TableCellRenderer {
    private final JTextArea textArea = new JTextArea(2, 999999);
    private final JLabel label = new JLabel();
    private final JLabel iconLabel = new JLabel();
    private final JScrollPane scroll = new JScrollPane();

    public ColumnSpanningCellRenderer() {
        super(new BorderLayout(0, 0));

        scroll.setViewportView(textArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        textArea.setBorder(BorderFactory.createEmptyBorder());
        textArea.setMargin(new Insets(0, 0, 0, 0));
        textArea.setForeground(Color.RED);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setOpaque(false);

        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        iconLabel.setOpaque(false);

        Border b1 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        Border b2 = BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY);
        label.setBorder(BorderFactory.createCompoundBorder(b2, b1));

        setBackground(textArea.getBackground());
        setOpaque(true);
        add(label, BorderLayout.NORTH);
        add(scroll);
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Test test;
        if (value instanceof Test) {
            test = (Test) value;
            add(iconLabel, BorderLayout.WEST);
        } else {
            String title = Objects.toString(value, "");
            int mrow = table.convertRowIndexToModel(row);
            Test t = (Test) table.getModel().getValueAt(mrow, 0);
            if (t == null) {
                test = new Test(title, null, "");
            } else {
                test = new Test(title, t.icon, t.text);
            }
            remove(iconLabel);
        }
        label.setText(test.title);
        textArea.setText(test.text);
        iconLabel.setIcon(test.icon);

        Rectangle cr = table.getCellRect(row, column, false);
/*/ //Flickering on first visible row ?
        if (column == 0) {
            cr.x = 0;
            cr.width -= iconLabel.getPreferredSize().width;
        } else {
            cr.x -= iconLabel.getPreferredSize().width;
        }
        textArea.scrollRectToVisible(cr);
/*/
        if (column != 0) {
            cr.x -= iconLabel.getPreferredSize().width;
        }
        scroll.getViewport().setViewPosition(cr.getLocation());
//*/
        if (isSelected) {
            setBackground(Color.ORANGE);
        } else {
            setBackground(Color.WHITE);
        }
        return this;
    }
}

class Test {
    public final String title;
    public final Icon icon;
    public final String text;
    public Test(String title, Icon icon, String text) {
        this.title = title;
        this.icon  = icon;
        this.text  = text;
    }
}
