package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
    private final List<String> icons = Arrays.asList(
        "wi0009-16.png",
        "wi0054-16.png",
        "wi0062-16.png",
        "wi0063-16.png",
        "wi0124-16.png",
        "wi0126-16.png");

    public MainPanel() {
        super(new BorderLayout());

//         if(tabbedPane.getUI() instanceof com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI) {
//             tabbedPane.setUI(new com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI() {
//                 @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
//                     int defaultWidth = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
//                     int selectedIndex  = tabPane.getSelectedIndex();
//                     boolean isSelected = selectedIndex == tabIndex;
//                     if(isSelected) {
//                         return defaultWidth + 100;
//                     }else{
//                         return defaultWidth;
//                     }
//                 }
//                 //@Override public Rectangle getTabBounds(JTabbedPane pane, int i) {
//                 //    Rectangle tabRect = super.getTabBounds(pane, i);
//                 //    tabRect.translate(0, -16);
//                 //    tabRect.height = 16;
//                 //    return tabRect;
//                 //}
// //                 @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
// //                     //Rectangle tabRect  = rects[tabIndex];
// //                     int selectedIndex  = tabPane.getSelectedIndex();
// //                     boolean isSelected = selectedIndex == tabIndex;
// //                     if(isSelected) {
// //                         //JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT
// //                         rects[tabIndex].width += 16;
// //                     }
// //                     super.paintTab(g,tabPlacement,rects,tabIndex,iconRect,textRect);
// //                 }
//             });
//         }
        for(String str: icons) {
            tabbedPane.addTab(str, new ImageIcon(getClass().getResource(str)), new JLabel(str), str);
        }
        tabbedPane.setComponentPopupMenu(new TabTitleRenamePopupMenu());
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

class TabTitleRenamePopupMenu extends JPopupMenu {
    private final JCheckBoxMenuItem pinTabMenuItem = new JCheckBoxMenuItem(new AbstractAction("pin tab") {
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane t = (JTabbedPane)getInvoker();
            JCheckBoxMenuItem check = (JCheckBoxMenuItem)e.getSource();
            int idx       = t.getSelectedIndex();
            Component cmp = t.getComponentAt(idx);
            Component tab = t.getTabComponentAt(idx);
            Icon icon     = t.getIconAt(idx);
            String tip    = t.getToolTipTextAt(idx);
            boolean flg   = t.isEnabledAt(idx);

            int i;
            if(check.isSelected()) {
                for(i=0;i<idx;i++) {
                    String s = t.getTitleAt(i);
                    if(s==null || s.length()==0) {
                        continue;
                    }else{
                        break;
                    }
                }
            }else{
                for(i=t.getTabCount()-1;i>idx;i--) {
                    String s = t.getTitleAt(i);
                    if(s!=null && s.length()>0) {
                        continue;
                    }else{
                        break;
                    }
                }
            }
            t.remove(idx);
            t.insertTab(check.isSelected()?"":tip, icon, cmp, tip, i);
            t.setTabComponentAt(i, tab);
            t.setEnabledAt(i, flg);
            if(flg) t.setSelectedIndex(i);

            //JComponent c = (JComponent)t.getTabComponentAt(idx);
            //c.revalidate();
        }
    });
//     private final Action newTabAction = new AbstractAction("new tab") {
//         @Override public void actionPerformed(ActionEvent evt) {
//             JTabbedPane t = (JTabbedPane)getInvoker();
//             int count = t.getTabCount();
//             String title = "Tab " + count;
//             t.add(title, new JLabel(title));
//             t.setTabComponentAt(count, new ButtonTabComponent(t));
//         }
//     };
    private final Action closeAllAction = new AbstractAction("close all") {
        @Override public void actionPerformed(ActionEvent evt) {
            JTabbedPane t = (JTabbedPane)getInvoker();
            //t.removeAll();
            for(int i=t.getTabCount()-1;i>=0;i--) {
                String s = t.getTitleAt(i);
                if(s!=null && s.length()>0) {
                    t.removeTabAt(i);
                }
            }
        }
    };
    public TabTitleRenamePopupMenu() {
        super();
        add(pinTabMenuItem);
        addSeparator();
        add(closeAllAction);
    }
    private static boolean isPinTab(JTabbedPane t, int x, int y) {
        int i = t.indexAtLocation(x, y);
        if(i>=0 && i==t.getSelectedIndex()) {
            String s = t.getTitleAt(i);
            if(s==null || s.length()==0) {
                return true;
            }
        }
        return false;
    }
    @Override public void show(Component c, int x, int y) {
        JTabbedPane t = (JTabbedPane)c;
        pinTabMenuItem.setEnabled(t.indexAtLocation(x, y)>=0);
        pinTabMenuItem.setSelected(isPinTab(t,x,y));
        super.show(c, x, y);
    }
};
