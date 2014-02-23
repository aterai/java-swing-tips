package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final Timer timer;
    private final JButton button   = new JButton("JButton JButton");
    private final JCheckBox vcheck = new JCheckBox(new AbstractAction("setVisible") {
        @Override public void actionPerformed(ActionEvent e) {
            JCheckBox c = (JCheckBox) e.getSource();
            button.setVisible(c.isSelected());
        }
    });
    private final JCheckBox echeck = new JCheckBox(new AbstractAction("setEnabled") {
        @Override public void actionPerformed(ActionEvent e) {
            JCheckBox c = (JCheckBox) e.getSource();
            button.setEnabled(c.isSelected());
        }
    });
    private final JCheckBox tcheck = new JCheckBox(new AbstractAction("start") {
        @Override public void actionPerformed(ActionEvent e) {
            if (tcheck.isSelected()) {
                timer.start();
            } else {
                timer.stop();
            }
        }
    });
    public MainPanel() {
        super(new BorderLayout());
        vcheck.setSelected(true);
        echeck.setSelected(true);
        tcheck.setSelected(true);

        JTabbedPane tab = new JTabbedPane();
        tab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        button.addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) { //NOPMD
                    printInfo("SHOWING_CHANGED");
                } else if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) { //NOPMD
                    printInfo("DISPLAYABILITY_CHANGED");
                }
            }
        });

        JPanel panel = new JPanel();
        panel.add(button);
        for (int i = 0; i < 5; i++) {
            panel.add(new JLabel("<html>asfasfdasdfasdfsa<br>asfdd134123fgh"));
        }
        tab.addTab("Main",   new JScrollPane(panel));
        tab.addTab("JTree",  new JScrollPane(new JTree()));
        tab.addTab("JLabel", new JLabel("Test"));

        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p1.add(new JLabel("JButton:")); p1.add(vcheck); p1.add(echeck);
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p2.add(new JLabel("Timer:")); p2.add(tcheck);

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(p1); p.add(p2);
        add(p, BorderLayout.NORTH);
        add(tab);
        timer = new Timer(4000, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                printInfo(new Date().toString());
            }
        });
        timer.start();
        setPreferredSize(new Dimension(320, 240));
    }
    private void printInfo(String str) {
        System.out.println("JButton: " + str);
        System.out.println("  isDisplayable:" + button.isDisplayable());
        System.out.println("  isShowing:" + button.isShowing());
        System.out.println("  isVisible:" + button.isVisible());
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
