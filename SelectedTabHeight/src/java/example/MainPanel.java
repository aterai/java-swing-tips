package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel{
    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    public MainPanel() {
        super(new BorderLayout());

        if(tabbedPane.getUI() instanceof com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI) {
            tabbedPane.setUI(new com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI() {
                @Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                    return 32;
                }
                //@Override public Rectangle getTabBounds(JTabbedPane pane, int i) {
                //    Rectangle tabRect = super.getTabBounds(pane, i);
                //    tabRect.translate(0, -16);
                //    tabRect.height = 16;
                //    return tabRect;
                //}
                @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
                    //Rectangle tabRect  = rects[tabIndex];
                    int selectedIndex  = tabPane.getSelectedIndex();
                    boolean isSelected = selectedIndex == tabIndex;
                    if(!isSelected) {
                        //JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT
                        rects[tabIndex].y = 16+3;
                        rects[tabIndex].height = 16;
                        //rects[tabIndex].y += 16;
                        //rects[tabIndex].height -= 16;
                    }
                    super.paintTab(g,tabPlacement,rects,tabIndex,iconRect,textRect);
                }
            });
        }else{
            //t.setUI(new javax.swing.plaf.metal.MetalTabbedPaneUI() {
            tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
                @Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                    return 32;
                }
                @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
                    int selectedIndex  = tabPane.getSelectedIndex();
                    boolean isSelected = selectedIndex == tabIndex;
                    if(!isSelected) {
                        rects[tabIndex].y = 16;
                        rects[tabIndex].height = 16;
                    }
                    super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
                }
            });
        }
        tabbedPane.addTab("00000", new JLabel("aaaaaaaaaaa"));
        tabbedPane.addTab("111112", new JLabel("bbbbbbbbbbbbbbbb"));
        tabbedPane.addTab("22222232", new JScrollPane(new JTree()));
        tabbedPane.addTab("3333333333", new JSplitPane());
        add(tabbedPane);
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
