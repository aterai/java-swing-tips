package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

public final class MainPanel extends JPanel {
    private final JComboBox<? extends Enum<?>> comboBox = new JComboBox<>(TabPlacements.values());
    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

    public MainPanel() {
        super(new BorderLayout());

        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                tabbedPane.setTabPlacement(((TabPlacements) e.getItem()).tabPlacement);
            }
        });
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JLabel("TabPlacement: "));
        box.add(Box.createHorizontalStrut(2));
        box.add(comboBox);
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        if (tabbedPane.getUI() instanceof WindowsTabbedPaneUI) {
            tabbedPane.setUI(new WindowsTabHeightTabbedPaneUI());
        } else {
            tabbedPane.setUI(new BasicTabHeightTabbedPaneUI());
        }
        tabbedPane.addTab("00000", new JLabel("aaaaaaaaaaa"));
        tabbedPane.addTab("111112", new JLabel("bbbbbbbbbbbbbbbb"));
        tabbedPane.addTab("22222232", new JScrollPane(new JTree()));
        tabbedPane.addTab("3333333333", new JSplitPane());
        add(tabbedPane);
        add(box, BorderLayout.SOUTH);
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

enum TabPlacements {
    TOP(JTabbedPane.TOP),
    BOTTOM(JTabbedPane.BOTTOM),
    LEFT(JTabbedPane.LEFT),
    RIGHT(JTabbedPane.RIGHT);
    public final int tabPlacement;
    TabPlacements(int tabPlacement) {
        this.tabPlacement = tabPlacement;
    }
}

class WindowsTabHeightTabbedPaneUI extends WindowsTabbedPaneUI {
    private static final int TAB_AREA_HEIGHT = 32;
    @Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return TAB_AREA_HEIGHT; // super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 4;
    }
    // @Override public Rectangle getTabBounds(JTabbedPane pane, int i) {
    //     Rectangle tabRect = super.getTabBounds(pane, i);
    //     tabRect.translate(0, -16);
    //     tabRect.height = 16;
    //     return tabRect;
    // }
    @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
        if (tabPane.getSelectedIndex() != tabIndex && tabPlacement != JTabbedPane.LEFT && tabPlacement != JTabbedPane.RIGHT) {
            int tabHeight = TAB_AREA_HEIGHT / 2 + 3;
            rects[tabIndex].height = tabHeight;
            if (tabPlacement == JTabbedPane.TOP) {
                rects[tabIndex].y = TAB_AREA_HEIGHT - tabHeight + 3;
            }
        }
        super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
    }
}

class BasicTabHeightTabbedPaneUI extends BasicTabbedPaneUI {
    private static final int TAB_AREA_HEIGHT = 32;
    @Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return TAB_AREA_HEIGHT;
    }
    @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
        if (tabPane.getSelectedIndex() != tabIndex && tabPlacement != JTabbedPane.LEFT && tabPlacement != JTabbedPane.RIGHT) {
            int tabHeight = TAB_AREA_HEIGHT / 2 + 3;
            rects[tabIndex].height = tabHeight;
            if (tabPlacement == JTabbedPane.TOP) {
                rects[tabIndex].y = TAB_AREA_HEIGHT - tabHeight + 3;
            }
        }
        super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
    }
}
