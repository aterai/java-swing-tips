package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    private final List<String> icons = Arrays.asList(
        "wi0009-16.png",
        "wi0054-16.png",
        "wi0062-16.png",
        "wi0063-16.png",
        "wi0124-16.png",
        "wi0126-16.png");

    private MainPanel() {
        super(new BorderLayout());

        // if (tabbedPane.getUI() instanceof WindowsTabbedPaneUI) {
        //     tabbedPane.setUI(new WindowsTabbedPaneUI() {
        //         @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        //             int defaultWidth = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
        //             int selectedIndex = tabPane.getSelectedIndex();
        //             boolean isSelected = selectedIndex == tabIndex;
        //             if (isSelected) {
        //                 return defaultWidth + 100;
        //             } else {
        //                 return defaultWidth;
        //             }
        //         }
        //         // @Override public Rectangle getTabBounds(JTabbedPane pane, int i) {
        //         //     Rectangle tabRect = super.getTabBounds(pane, i);
        //         //     tabRect.translate(0, -16);
        //         //     tabRect.height = 16;
        //         //     return tabRect;
        //         // }
        //         // @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
        //         //     // Rectangle tabRect = rects[tabIndex];
        //         //     int selectedIndex = tabPane.getSelectedIndex();
        //         //     boolean isSelected = selectedIndex == tabIndex;
        //         //     if (isSelected) {
        //         //         // JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT
        //         //         rects[tabIndex].width += 16;
        //         //     }
        //         //     super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
        //         // }
        //     });
        // }
        icons.forEach(s -> tabbedPane.addTab(s, new ImageIcon(getClass().getResource(s)), new JLabel(s), s));
        tabbedPane.setComponentPopupMenu(new PinTabPopupMenu());
        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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

class PinTabPopupMenu extends JPopupMenu {
    private final JCheckBoxMenuItem pinTabMenuItem = new JCheckBoxMenuItem(new AbstractAction("pin tab") {
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane t = (JTabbedPane) getInvoker();
            JCheckBoxMenuItem check = (JCheckBoxMenuItem) e.getSource();
            int idx = t.getSelectedIndex();
            Component cmp = t.getComponentAt(idx);
            Component tab = t.getTabComponentAt(idx);
            Icon icon = t.getIconAt(idx);
            String tip = t.getToolTipTextAt(idx);
            boolean flg = t.isEnabledAt(idx);

            int i = searchNewSelectedIndex(t, idx, check.isSelected());
            t.remove(idx);
            t.insertTab(check.isSelected() ? "" : tip, icon, cmp, tip, i);
            t.setTabComponentAt(i, tab);
            t.setEnabledAt(i, flg);
            if (flg) {
                t.setSelectedIndex(i);
            }
            // JComponent c = (JComponent) t.getTabComponentAt(idx);
            // c.revalidate();
        }
    });
    // private final Action newTabAction = new AbstractAction("new tab") {
    //     @Override public void actionPerformed(ActionEvent e) {
    //         JTabbedPane t = (JTabbedPane) getInvoker();
    //         int count = t.getTabCount();
    //         String title = "Tab " + count;
    //         t.add(title, new JLabel(title));
    //         t.setTabComponentAt(count, new ButtonTabComponent(t));
    //     }
    // };
    private final Action closeAllAction = new AbstractAction("close all") {
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane t = (JTabbedPane) getInvoker();
            // t.removeAll();
            for (int i = t.getTabCount() - 1; i >= 0; i--) {
                String s = t.getTitleAt(i);
                if (!isEmpty(s)) {
                    t.removeTabAt(i);
                }
            }
        }
    };
    protected PinTabPopupMenu() {
        super();
        add(pinTabMenuItem);
        addSeparator();
        add(closeAllAction);
    }
    protected static int searchNewSelectedIndex(JTabbedPane t, int idx, boolean dir) {
        int i;
        if (dir) {
            for (i = 0; i < idx; i++) {
                String s = t.getTitleAt(i);
                if (isEmpty(s)) {
                    continue;
                } else {
                    break;
                }
            }
        } else {
            for (i = t.getTabCount() - 1; i > idx; i--) {
                String s = t.getTitleAt(i);
                if (isEmpty(s)) {
                    break;
                } else {
                    continue;
                }
            }
        }
        return i;
    }
    private static boolean isPinTab(JTabbedPane t, int x, int y) {
        int i = t.indexAtLocation(x, y);
        if (i >= 0 && i == t.getSelectedIndex()) {
            String s = t.getTitleAt(i);
            if (isEmpty(s)) {
                return true;
            }
        }
        return false;
    }
    protected static boolean isEmpty(String s) {
        return Objects.isNull(s) || s.isEmpty();
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTabbedPane) {
            JTabbedPane t = (JTabbedPane) c;
            pinTabMenuItem.setEnabled(t.indexAtLocation(x, y) >= 0);
            pinTabMenuItem.setSelected(isPinTab(t, x, y));
            super.show(c, x, y);
        }
    }
}
