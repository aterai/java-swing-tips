package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final JCheckBox vcheck = new JCheckBox("setVisible", true);
    private final JCheckBox echeck = new JCheckBox("setEnabled", true);
    private final JCheckBox tcheck = new JCheckBox("start", true);
    private final JButton button   = new JButton("JButton JButton");
    private final Timer timer;
    public MainPanel() {
        super(new BorderLayout());
        JTabbedPane tab = new JTabbedPane();
        tab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        button.addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                if((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED)!=0) {
                    printInfo("SHOWING_CHANGED");
                }else if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0) {
                    printInfo("DISPLAYABILITY_CHANGED");
                }
            }
        });

        JPanel panel = new JPanel();
        panel.add(button);
        for(int i=0;i<5;i++) {
            panel.add(new JLabel("<html>asfasfdasdfasdfsa<br>asfdd134123fgh"));
        }
        tab.addTab("Main",   new JScrollPane(panel));
        tab.addTab("JTree",  new JScrollPane(new JTree()));
        tab.addTab("JLabel", new JLabel("Test"));

        vcheck.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                button.setVisible(vcheck.isSelected());
            }
        });
        echeck.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                button.setEnabled(echeck.isSelected());
            }
        });
        tcheck.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if(tcheck.isSelected()) {
                    timer.start();
                }else{
                    timer.stop();
                }
            }
        });

        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p1.add(new JLabel("JButton:")); p1.add(vcheck); p1.add(echeck);
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p2.add(new JLabel("Timer:")); p2.add(tcheck);

        JPanel p = new JPanel(new GridLayout(2,1));
        p.add(p1); p.add(p2);
        add(p, BorderLayout.NORTH);
        add(tab);
        timer = new Timer(4000, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                printInfo(new java.util.Date().toString());
            }
        });
        timer.start();
        setPreferredSize(new Dimension(320, 240));
    }
    private void printInfo(String str) {
        System.out.println("JButton: "+str);
        System.out.println("  isDisplayable:"+button.isDisplayable());
        System.out.println("  isShowing:"+button.isShowing());
        System.out.println("  isVisible:"+button.isVisible());
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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

