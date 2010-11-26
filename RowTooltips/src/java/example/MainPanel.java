package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        TestModel model = new TestModel();
        JTable table = new JTable(model) {
            @Override public String getToolTipText(MouseEvent e) {
                int row = convertRowIndexToModel(rowAtPoint(e.getPoint()));
                TableModel m = getModel();
                return "<html>"+m.getValueAt(row, 1)+"<br>"+m.getValueAt(row, 2)+"</html>";
            }
//             public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
//                 Component c = super.prepareRenderer(tcr, row, column);
//                 if(c instanceof JComponent) {
//                     int mr = convertRowIndexToModel(row);
//                     int mc = convertColumnIndexToModel(column);
//                     Object o = getModel().getValueAt(mr, mc);
//                     String s = (o!=null)?o.toString():null;
//                     ((JComponent)c).setToolTipText(s.isEmpty()?null:s);
//                 }
//                 return c;
//             }
        };

        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));

        table.setAutoCreateRowSorter(true);
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
