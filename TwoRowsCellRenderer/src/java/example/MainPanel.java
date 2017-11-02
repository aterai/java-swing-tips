package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"A", "B"};
    private final Object[][] data = {
        {"123456789012345678901234567890123456789012345678901234567890", "12345"},
        {"bbb", "abcdefghijklmnopqrstuvwxyz----abcdefghijklmnopqrstuvwxyz"},
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public boolean isCellEditable(int row, int column) {
            return false;
        }
        @Override public Class<?> getColumnClass(int column) {
            return String.class;
        }
    };
    private final JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());

        table.setAutoCreateRowSorter(true);
        table.setRowHeight(table.getRowHeight() * 2);
        table.setDefaultRenderer(String.class, new TwoRowsCellRenderer());

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

class TwoRowsCellRenderer extends JPanel implements TableCellRenderer {
    private final JLabel top = new JLabel();
    private final JLabel bottom = new JLabel();
    protected TwoRowsCellRenderer() {
        super(new GridLayout(2, 1, 0, 0));
        add(top);
        add(bottom);
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        setFont(table.getFont());
        FontMetrics fm  = top.getFontMetrics(top.getFont());
        String text     = Objects.toString(value, "");
        String first    = text;
        String second   = "";
        int columnWidth = table.getCellRect(0, column, false).width;
        int textWidth   = 0;
//         for (int i = 0; i < text.length(); i++) {
//             textWidth += fm.charWidth(text.charAt(i));
//             if (textWidth > columnWidth) {
//                 first  = text.substring(0, i - 1);
//                 second = text.substring(i - 1);
//                 break;
//             }
//         }

//         // @see Unicode surrogate programming with the Java language
//         // https://www.ibm.com/developerworks/library/j-unicode/index.html
//         // https://www.ibm.com/developerworks/jp/ysl/library/java/j-unicode_surrogate/index.html
//         char[] ach = text.toCharArray();
//         int len = ach.length;
//         int[] acp = new int[Character.codePointCount(ach, 0, len)];
//         int j = 0;
//         int cp;
//         for (int i = 0; i < len; i += Character.charCount(cp)) {
//             cp = Character.codePointAt(ach, i);
//             acp[j++] = cp;
//         }
//         for (int i = 0; i < acp.length; i++) {
//             textWidth += fm.charWidth(acp[i]);
//             if (textWidth > columnWidth) {
//                 first  = new String(acp, 0, i);
//                 second = new String(acp, i, acp.length - i);
//                 break;
//             }
//         }

        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            textWidth += fm.charWidth(cp);
            if (textWidth > columnWidth) {
                first  = text.substring(0, i);
                second = text.substring(i);
                break;
            }
            i += Character.charCount(cp);
        }
        top.setText(first);
        bottom.setText(second);
        return this;
    }
}
