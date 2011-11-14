package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public JComponent makeUI() {
        String[] columnNames = {"String", "Integer", "Boolean"};
        Object[][] data = {
            {"aaa", 12, true}, {"bbb", 5, false},
            {"CCC", 92, true}, {"DDD", 0, false}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JScrollPane s1, s2;
        final JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                             new JScrollPane(new JTable(model)),
                                             new JScrollPane(new JTree()));

//         System.out.println(s1.getPreferredSize());
//         System.out.println(s2.getPreferredSize());
//         s1.setPreferredSize(new Dimension(320, 100));
//         s2.setPreferredSize(new Dimension(320, 100));
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                sp.setDividerLocation(0.5);
                //sp.setResizeWeight(0.5);
            }
        });
        return sp;
    }
    public MainPanel() {
        super(new BorderLayout());
        add(makeUI());
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
