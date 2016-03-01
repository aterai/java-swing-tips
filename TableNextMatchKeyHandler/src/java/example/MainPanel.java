package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.Position;

public final class MainPanel extends JPanel {
    private final Dimension preferredSize = new Dimension(320, 240);
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"aaa", 15, true}, {"bbb", 6, false},
        {"abc", 92, true}, {"Bbb", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());
        table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
        table.setAutoCreateRowSorter(true);
        table.addKeyListener(new TableNextMatchKeyHandler());

        add(new JScrollPane(table));
    }
    @Override public Dimension getPreferredSize() {
        return preferredSize;
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

//@see javax/swing/plaf/basic/BasicListUI.Handler
//@see javax/swing/plaf/basic/BasicTreeUI.Handler
class TableNextMatchKeyHandler extends KeyAdapter {
    private static final int TARGET_COLUMN = 0;
    private static final long TIME_FACTOR = 500L;
    private String prefix = "";
    private String typedString;
    private long lastTime;
//     private final long timeFactor;
//     protected TableNextMatchKeyHandler() {
//         super();
//         Long l = (Long) UIManager.get("List.timeFactor");
//         timeFactor = Objects.nonNull(l) ? l.longValue() : 1000L;
//     }
    private boolean isNavigationKey(KeyEvent event) {
        JTable table = (JTable) event.getComponent();
        InputMap inputMap = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = KeyStroke.getKeyStrokeForEvent(event);
        return Objects.nonNull(inputMap) && Objects.nonNull(inputMap.get(key));
    }
    @Override public void keyPressed(KeyEvent e) {
        if (isNavigationKey(e)) {
            prefix = "";
            typedString = "";
            lastTime = 0L;
        }
    }
    @Override public void keyTyped(KeyEvent e) {
        JTable src = (JTable) e.getComponent();
        int max = src.getRowCount();
        if (max == 0 || e.isAltDown() || isNavigationKey(e)) { //|| BasicGraphicsUtils.isMenuShortcutKeyDown(e)) {
            // Nothing to select
            return;
        }
        boolean startingFromSelection = true;
        char c = e.getKeyChar();
        int increment = e.isShiftDown() ? -1 : 1;
        long time = e.getWhen();
        int startIndex = src.getSelectedRow();
        if (time - lastTime < TIME_FACTOR) {
            typedString += c;
            if (prefix.length() == 1 && c == prefix.charAt(0)) {
                // Subsequent same key presses move the keyboard focus to the next
                // object that starts with the same letter.
                startIndex += increment;
            } else {
                prefix = typedString;
            }
        } else {
            startIndex += increment;
            typedString = String.valueOf(c);
            prefix = typedString;
        }
        lastTime = time;

        selectAndScrollNextMatch(src, max, e, prefix, startIndex, startingFromSelection);
    }
    private static void selectAndScrollNextMatch(JTable src, int max, KeyEvent e, String prefix, int startIndex, boolean startingFromSelection) {
        int start = startIndex;
        boolean isStartingSelection = startingFromSelection;
        if (start < 0 || start >= max) {
            if (e.isShiftDown()) {
                start = max - 1;
            } else {
                isStartingSelection = false;
                start = 0;
            }
        }
        Position.Bias bias = e.isShiftDown() ? Position.Bias.Backward : Position.Bias.Forward;
        int index = getNextMatch(src, prefix, start, bias);
        if (index >= 0) {
            src.getSelectionModel().setSelectionInterval(index, index);
            src.scrollRectToVisible(src.getCellRect(index, TARGET_COLUMN, true));
        } else if (isStartingSelection) { // wrap
            index = getNextMatch(src, prefix, 0, bias);
            if (index >= 0) {
                src.getSelectionModel().setSelectionInterval(index, index);
                src.scrollRectToVisible(src.getCellRect(index, TARGET_COLUMN, true));
            }
        }
    }
    //@see javax/swing/JList#getNextMatch(String prefix, int startIndex, Position.Bias bias)
    //@see javax/swing/JTree#getNextMatch(String prefix, int startIndex, Position.Bias bias)
    public static int getNextMatch(JTable table, String prefix, int startingRow, Position.Bias bias) {
        int max = table.getRowCount();
        if (Objects.isNull(prefix) || startingRow < 0 || startingRow >= max) {
            throw new IllegalArgumentException();
        }
        String uprefix = prefix.toUpperCase(Locale.ENGLISH);

        // start search from the next/previous element froom the
        // selected element
        int increment = bias == Position.Bias.Forward ? 1 : -1;
        int row = startingRow;
        do {
            Object value = table.getValueAt(row, TARGET_COLUMN);
            String text = Objects.toString(value, "");
            if (text.toUpperCase(Locale.ENGLISH).startsWith(uprefix)) {
                return row;
            }
            row = (row + increment + max) % max;
        } while (row != startingRow);
        return -1;
    }
}
