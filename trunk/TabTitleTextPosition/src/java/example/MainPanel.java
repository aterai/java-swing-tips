package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.plaf.synth.*;

public class MainPanel extends JPanel {
    private static void addTab(JTabbedPane tabbedPane, String title, Icon icon, Component c) {
        tabbedPane.addTab(title, c);
        JLabel label = new JLabel(title, icon, SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        //label.setVerticalAlignment(SwingConstants.CENTER);
        //label.setHorizontalAlignment(SwingConstants.CENTER);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount()-1, label);
    }
    public MainPanel() {
        super(new BorderLayout());
        JTabbedPane t = new ClippedTitleTabbedPane();
        t.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
        addTab(t, "JTree",      new ImageIcon(getClass().getResource("wi0009-32.png")), new JScrollPane(new JTree()));
        addTab(t, "JTextArea",  new ImageIcon(getClass().getResource("wi0054-32.png")), new JScrollPane(new JTextArea()));
        addTab(t, "Preference", new ImageIcon(getClass().getResource("wi0062-32.png")), new JScrollPane(new JTree()));
        addTab(t, "Help",       new ImageIcon(getClass().getResource("wi0063-32.png")), new JScrollPane(new JTextArea()));

//         t.addTab(makeTitle("Title","wi0009-32.png"), new JLabel("a"));
//         t.addTab(makeTitle("Help", "wi0054-32.png"), new JLabel("b"));

        add(t);
        setPreferredSize(new Dimension(320, 200));
    }
//     private String makeTitle(String t, String p) {
//         return "<html><center><img src='"+getClass().getResource(p)+"'/><br/>"+t;
//     }
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
class ClippedTitleTabbedPane extends JTabbedPane {
    public ClippedTitleTabbedPane() {
        super();
    }
    public ClippedTitleTabbedPane(int tabPlacement) {
        super(tabPlacement);
    }
    private Insets getTabInsets() {
        Insets i = UIManager.getInsets("TabbedPane.tabInsets");
        if(i!=null) {
            return i;
        }else{
            SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB);
            SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB, style, SynthConstants.ENABLED);
            return style.getInsets(context, null);
        }
    }
    private Insets getTabAreaInsets() {
        Insets i = UIManager.getInsets("TabbedPane.tabAreaInsets");
        if(i!=null) {
            return i;
        }else{
            SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB_AREA);
            SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB_AREA, style, SynthConstants.ENABLED);
            return style.getInsets(context, null);
        }
    }
    @Override public void doLayout() {
        int tabCount  = getTabCount();
        if(tabCount==0) return;
        Insets tabInsets     = getTabInsets();
        Insets tabAreaInsets = getTabAreaInsets();
        Insets insets = getInsets();
        int areaWidth = getWidth() - tabAreaInsets.left - tabAreaInsets.right - insets.left - insets.right;
        int tabWidth  = 0; // = tabInsets.left + tabInsets.right + 3;
        int gap       = 0;

        switch(getTabPlacement()) {
          case LEFT: case RIGHT:
            tabWidth = areaWidth / 4;
            gap = 0;
            break;
          case BOTTOM: case TOP: default:
            tabWidth = areaWidth / tabCount;
            gap = areaWidth - tabWidth * tabCount;
        }
        // "3" is magic number @see BasicTabbedPaneUI#calculateTabWidth
        tabWidth = tabWidth - tabInsets.left - tabInsets.right - 3;
        for(int i=0;i<tabCount;i++) {
            JComponent l = (JComponent)getTabComponentAt(i);
            if(l==null) break;
            int v = i < gap ? 1 : 0;
            l.setPreferredSize(new Dimension(tabWidth + v, l.getPreferredSize().height));
        }
        super.doLayout();
    }
//     @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
//         super.insertTab(title, icon, component, tip==null?title:tip, index);
//         JLabel label = new JLabel(title, JLabel.CENTER);
//         //Dimension dim = label.getPreferredSize();
//         //Insets tabInsets = getTabInsets();
//         //label.setPreferredSize(new Dimension(0, dim.height+tabInsets.top+tabInsets.bottom));
//         setTabComponentAt(index, label);
//     }
}
