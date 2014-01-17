package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox("一時ウィンドウ(入力モード)->enterでセル編集開始");
    private final String[] columnNames = {"A", "B", "C"};
    private final Object[][] data = {
        {"aaa", "eeee", "l"}, {"bbb", "ff", "ggg"},
        {"CCC", "kkk", "jj"}, {"DDD", "ii", "hhh"}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table;
    public MainPanel() {
        super(new BorderLayout());
        table = new JTable(model) {
            private final Color evenColor = new Color(250, 250, 250);
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if(isRowSelected(row)) {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                }else{
                    c.setForeground(getForeground());
                    c.setBackground((row%2==0)?evenColor:getBackground());
                }
                return c;
            }
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
                //System.out.println("key: "+ks.toString());
                boolean retValue = super.processKeyBinding(ks, e, condition, pressed);
                if(!check.isSelected()) { return retValue; }
                if(KeyStroke.getKeyStroke('\t').equals(ks) || KeyStroke.getKeyStroke('\n').equals(ks)) {
                    System.out.println("tab or enter typed");
                    return retValue;
                }
                if(getInputContext().isCompositionEnabled() && !isEditing() && !pressed && !ks.isOnKeyRelease()) {
                    int selectedRow = getSelectedRow();
                    int selectedColumn = getSelectedColumn();
                    if(selectedRow!=-1 && selectedColumn!=-1) {
                        boolean dummy = editCellAt(selectedRow, selectedColumn);
                        System.out.println("editCellAt: "+dummy);
                    }
                }
                return retValue;
            }
        };
        //table.setSurrendersFocusOnKeystroke(true);
        table.setShowGrid(false);
        //table.setShowHorizontalLines(false);
        //table.setShowVerticalLines(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        add(check, BorderLayout.NORTH);
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
