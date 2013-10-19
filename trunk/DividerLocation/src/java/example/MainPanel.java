package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final JRadioButton r0 = new JRadioButton("0.0");
    private final JRadioButton r1 = new JRadioButton("0.5");
    private final JRadioButton r2 = new JRadioButton("1.0");
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                                 new JScrollPane(new JTable(model)),
                                                 new JScrollPane(new JTree()));
    public JComponent makeUI() {
//         JScrollPane s1, s2;
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
        ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if(r2.isSelected()) {
                    sp.setResizeWeight(1.0);
                }else if(r1.isSelected()) {
                    sp.setResizeWeight(0.5);
                }else{
                    sp.setResizeWeight(0.0);
                }
            }
        };
        ButtonGroup bg = new ButtonGroup();
        JPanel p = new JPanel();
        p.add(new JLabel("JSplitPane#setResizeWeight: "));
        for(JRadioButton r: Arrays.asList(r0, r1, r2)) {
            r.addActionListener(al);
            bg.add(r);
            p.add(r);
        }
        r0.setSelected(true);
        add(p, BorderLayout.NORTH);
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
