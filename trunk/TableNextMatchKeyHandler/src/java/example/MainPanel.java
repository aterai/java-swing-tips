package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.Position;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        String[] columnNames = {"String", "Integer", "Boolean"};
        Object[][] data = {
            {"aaa", 12, true}, {"bbb", 5, false},
            {"aaa", 15, true}, {"bbb", 6, false},
            {"abc", 92, true}, {"Bbb", 0, false}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
        table.setAutoCreateRowSorter(true);
        table.addKeyListener(new TableNextMatchKeyHandler());

        add(new JScrollPane(table));
    }
    private static final Dimension preferredSize = new Dimension(320, 240);
    @Override public Dimension getPreferredSize() {
        return preferredSize;
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

//@see javax/swing/plaf/basic/BasicListUI.Handler
//@see javax/swing/plaf/basic/BasicTreeUI.Handler
class TableNextMatchKeyHandler extends KeyAdapter{
    private static final int TARGET_COLUMN = 0;
    private String prefix;
    private String typedString;
    private long lastTime = 0L;
    private long timeFactor;
    public TableNextMatchKeyHandler() {
        //Long l = (Long)UIManager.get("List.timeFactor");
        timeFactor = 500L; //(l!=null) ? l.longValue() : 1000L;
    }
    private boolean isNavigationKey(KeyEvent event) {
        JTable table = (JTable)event.getSource();
        InputMap inputMap = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = KeyStroke.getKeyStrokeForEvent(event);
        if(inputMap != null && inputMap.get(key) != null) {
            return true;
        }
        return false;
    }
    @Override public void keyPressed(KeyEvent e) {
        if(isNavigationKey(e)) {
            prefix = typedString = "";
            lastTime = 0L;
        }
    }
    @Override public void keyTyped(KeyEvent e) {
        JTable src = (JTable)e.getSource();
        int max = src.getRowCount();
        if(max == 0 || e.isAltDown() || isNavigationKey(e)) { //|| BasicGraphicsUtils.isMenuShortcutKeyDown(e)) {
            // Nothing to select
            return;
        }
        boolean startingFromSelection = true;
        char c = e.getKeyChar();
        int increment = e.isShiftDown() ? -1 : 1;
        long time = e.getWhen();
        int startIndex = src.getSelectedRow();
        if(time - lastTime < timeFactor) {
            typedString += c;
            if(prefix.length() == 1 && c == prefix.charAt(0)) {
                // Subsequent same key presses move the keyboard focus to the next
                // object that starts with the same letter.
                startIndex += increment;
            }else{
                prefix = typedString;
            }
        }else{
            startIndex += increment;
            typedString = String.valueOf(c);
            prefix = typedString;
        }
        lastTime = time;

        if(startIndex < 0 || startIndex >= max) {
            if(e.isShiftDown()) {
                startIndex = max-1;
            }else{
                startingFromSelection = false;
                startIndex = 0;
            }
        }
        Position.Bias bias = e.isShiftDown()?Position.Bias.Backward:Position.Bias.Forward;
        int index = getNextMatch(src, prefix, startIndex, bias);
        if(index >= 0) {
            src.getSelectionModel().setSelectionInterval(index, index);
            src.scrollRectToVisible(src.getCellRect(index, TARGET_COLUMN, true));
        }else if(startingFromSelection) { // wrap
            index = getNextMatch(src, prefix, 0, bias);
            if(index >= 0) {
                src.getSelectionModel().setSelectionInterval(index, index);
                src.scrollRectToVisible(src.getCellRect(index, TARGET_COLUMN, true));
            }
        }
    }
    //@see javax/swing/JList#getNextMatch(String prefix, int startIndex, Position.Bias bias)
    //@see javax/swing/JTree#getNextMatch(String prefix, int startIndex, Position.Bias bias)
    public static int getNextMatch(JTable table, String prefix, int startingRow, Position.Bias bias) {
        int max = table.getRowCount();
        if(prefix == null) {
            throw new IllegalArgumentException();
        }
        if(startingRow < 0 || startingRow >= max) {
            throw new IllegalArgumentException();
        }
        prefix = prefix.toUpperCase();

        // start search from the next/previous element froom the
        // selected element
        int increment = (bias == Position.Bias.Forward) ? 1 : -1;
        int row = startingRow;
        do{
            Object value = table.getValueAt(row, TARGET_COLUMN);
            String text = value!=null?value.toString():"";
            if(text.toUpperCase().startsWith(prefix)) {
                return row;
            }
            row = (row + increment + max) % max;
        }while(row != startingRow);
        return -1;
    }
}
