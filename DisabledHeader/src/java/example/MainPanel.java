package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final TestModel model = new TestModel();
    private final JTable table = new JTable(model) {
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
    public MainPanel() {
        super(new BorderLayout());
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        final SortButtonRenderer hrenderer = new SortButtonRenderer(table.getTableHeader());
        hrenderer.setEnabledAt(0, false);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(50);
        col.setMaxWidth(50);

        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));

        JCheckBox cbox = new JCheckBox("setEnabledAt(2, false)");
        cbox.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent ie) {
                JCheckBox box = (JCheckBox)ie.getSource();
                hrenderer.setEnabledAt(2, !box.isSelected());
            }
        });

        add(new JScrollPane(table));
        add(cbox, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 200));
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
