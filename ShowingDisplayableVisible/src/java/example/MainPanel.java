package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JButton button = new JButton("JButton JButton");

        Timer timer = new Timer(4000, e -> printInfo(button, LocalTime.now().toString()));

        JCheckBox vcheck = new JCheckBox("setVisible", true);
        vcheck.addActionListener(e -> button.setVisible(((JCheckBox) e.getSource()).isSelected()));

        JCheckBox echeck = new JCheckBox("setEnabled", true);
        echeck.addActionListener(e -> button.setEnabled(((JCheckBox) e.getSource()).isSelected()));

        JCheckBox tcheck = new JCheckBox("start", true);
        tcheck.addActionListener(e -> {
            if (((JCheckBox) e.getSource()).isSelected()) {
                timer.start();
            } else {
                timer.stop();
            }
        });

        JTabbedPane tab = new JTabbedPane();
        tab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        button.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                printInfo(button, "SHOWING_CHANGED");
            }
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                printInfo(button, "DISPLAYABILITY_CHANGED");
            }
        });

        printInfo(button, "after: new JButton, before: add(button); frame.setVisible(true)");

        JPanel panel = new JPanel();
        panel.add(button);
        for (int i = 0; i < 5; i++) {
            panel.add(new JLabel("<html>asfasfdasdfasdfsa<br>asfdd134123fgh"));
        }
        tab.addTab("Main", new JScrollPane(panel));
        tab.addTab("JTree", new JScrollPane(new JTree()));
        tab.addTab("JLabel", new JLabel("Test"));

        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p1.add(new JLabel("JButton:"));
        p1.add(vcheck);
        p1.add(echeck);
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p2.add(new JLabel("Timer:"));
        p2.add(tcheck);

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(p1);
        p.add(p2);
        add(p, BorderLayout.NORTH);
        add(tab);

        timer.start();
        setPreferredSize(new Dimension(320, 240));
    }
    public static void printInfo(Component c, String str) {
        System.out.println(c.getClass().getName() + ": " + str);
        System.out.println("  isDisplayable:" + c.isDisplayable());
        System.out.println("  isShowing:" + c.isShowing());
        System.out.println("  isVisible:" + c.isVisible());
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
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
