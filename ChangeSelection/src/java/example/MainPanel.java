package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final TestModel model = new TestModel();
    private final JTable table;
    private final JSpinner rowField;
    private final JSpinner colField;
    private final JCheckBox toggle = new JCheckBox("toggle", false);
    private final JCheckBox extend = new JCheckBox("extend", false);

    public MainPanel() {
        super(new BorderLayout());
        table = new JTable(model) {
            private final Color evenColor = new Color(250, 250, 250);
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if(isCellSelected(row, column)) {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                }else{
                    c.setForeground(getForeground());
                    c.setBackground((row%2==0)?evenColor:getBackground());
                }
                return c;
            }
        };
        table.setCellSelectionEnabled(true);

        model.addTest(new Test("0,1", "0,2"));
        model.addTest(new Test("1,1", "1,2"));
        model.addTest(new Test("2,1", "2,2"));
        model.addTest(new Test("3,1", "3,2"));
        model.addTest(new Test("4,1", "4,2"));
        model.addTest(new Test("5,1", "5,2"));
        model.addTest(new Test("6,1", "6,2"));
        model.addTest(new Test("7,1", "7,2"));
        model.addTest(new Test("8,1", "8,2"));
        model.addTest(new Test("9,1", "9,2"));

        rowField = new JSpinner(new SpinnerNumberModel(1, 0, model.getRowCount()-1, 1));
        colField = new JSpinner(new SpinnerNumberModel(2, 0, model.getColumnCount()-1, 1));

        table.getActionMap().put("clear-selection", new AbstractAction("clear-selection") {
            @Override public void actionPerformed(ActionEvent ae) {
                table.clearSelection();
                requestFocusInWindow();
            }
        });
        InputMap imap = table.getInputMap(JComponent.WHEN_FOCUSED);
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear-selection");

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        box.add(new JLabel("row:"));
        box.add(rowField);
        box.add(new JLabel(" col:"));
        box.add(colField);
        box.add(toggle);
        box.add(extend);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        p.add(new JButton(new AbstractAction("changeSelection") {
            @Override public void actionPerformed(ActionEvent e) {
                int row = -1, col = -1;
                try{
                    row = Integer.parseInt(rowField.getValue().toString());
                    col = Integer.parseInt(colField.getValue().toString());
                }catch(Exception ex) {
                    ex.printStackTrace();
                }

                table.changeSelection(row, col, toggle.isSelected(), extend.isSelected());
                //table.changeSelection(row, table.convertColumnIndexToModel(col), toggle.isSelected(), extend.isSelected());
                table.requestFocusInWindow();
                table.repaint();
            }
        }));
        p.add(new JButton(new AbstractAction("clear(Esc)") {
            @Override public void actionPerformed(ActionEvent e) {
                table.clearSelection();
                requestFocusInWindow();
            }
        }));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("JTable#changeSelection(int,int,boolean,boolean)"));
        panel.add(box, BorderLayout.NORTH);
        panel.add(p, BorderLayout.SOUTH);

        add(panel, BorderLayout.NORTH);
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
