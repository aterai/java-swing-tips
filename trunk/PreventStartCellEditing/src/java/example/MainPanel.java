package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final JRadioButton r0 = new JRadioButton("default");
    private final JRadioButton r1 = new JRadioButton("prevent KeyStroke autoStartsEdit");
    private final JRadioButton r2 = new JRadioButton("prevent mouse from starting edit");
    private final JRadioButton r3 = new JRadioButton("start cell editing only F2");
    private final JRadioButton r4 = new JRadioButton("isCellEditable retrun false");
    private final ButtonGroup bg = new ButtonGroup();
    private final TestModel model;
    public MainPanel() {
        super(new BorderLayout());
        model = new TestModel() {
            @Override public boolean isCellEditable(int row, int col) {
                return (col==0)?false:!r4.isSelected();
            }
        };
        model.addTest(new Test("Name 1", "Comment..."));
        model.addTest(new Test("Name 2", "Test "));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));
        model.addTest(new Test("Name 0", ""));

        final JTable table = new JTable(model) {
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
        };
        table.setShowGrid(false);
        //table.setShowHorizontalLines(false);
        //table.setShowVerticalLines(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setResizable(false);

// //         System.out.println(table.getActionMap().get("startEditing"));
//         InputMap im = table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
//         for(KeyStroke ks:im.allKeys()) {
//             Object actionMapKey = im.get(ks);
//             if("startEditing".equals(actionMapKey.toString())) {
//                 System.out.println("startEditing: "+ ks.toString());
//             }
//         }

        final DefaultCellEditor ce = (DefaultCellEditor)table.getDefaultEditor(Object.class);
        ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JRadioButton rb = (JRadioButton)e.getSource();
                table.putClientProperty("JTable.autoStartsEdit", rb!=r1 && rb!=r3);
                ce.setClickCountToStart((rb==r2 || rb==r3)?Integer.MAX_VALUE:2);
            }
        };
        r0.setSelected(true);
        Box p = Box.createVerticalBox();
        for(AbstractButton b:Arrays.asList(r0, r1, r2, r3, r4)) {
            b.addActionListener(al); bg.add(b); p.add(b);
        }
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
