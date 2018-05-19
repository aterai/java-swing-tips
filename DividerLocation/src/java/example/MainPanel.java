package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String[] columnNames = {"String", "Integer", "Boolean"};
        Object[][] data = {
            {"aaa", 12, true}, {"bbb", 5, false},
            {"CCC", 92, true}, {"DDD", 0, false}
        };
        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(new JTable(model)), new JScrollPane(new JTree()));

        JRadioButton r0 = new JRadioButton("0.0", true);
        r0.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                sp.setResizeWeight(0d);
            }
        });

        JRadioButton r1 = new JRadioButton("0.5");
        r1.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                sp.setResizeWeight(.5);
            }
        });

        JRadioButton r2 = new JRadioButton("1.0");
        r2.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                sp.setResizeWeight(1d);
            }
        });

        ButtonGroup bg = new ButtonGroup();
        JPanel p = new JPanel();
        p.add(new JLabel("JSplitPane#setResizeWeight: "));
        for (JRadioButton r: Arrays.asList(r0, r1, r2)) {
            bg.add(r);
            p.add(r);
        }
        add(p, BorderLayout.NORTH);
        add(sp);
        setPreferredSize(new Dimension(320, 240));

        EventQueue.invokeLater(() -> {
            sp.setDividerLocation(.5);
            // sp.setResizeWeight(.5);
        });
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
