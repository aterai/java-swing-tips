package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

public final class MainPanel extends JPanel {
    public static final int MIN_TAB_WIDTH = 100;

    private MainPanel() {
        super(new GridLayout(2, 1, 0, 10));

        JTabbedPane tabbedPane = new JTabbedPane();
        Arrays.asList(new JTabbedPane(), tabbedPane).forEach(tab -> {
            tab.addTab("aaaaaa", new JLabel("aaaaaaaaaaa"));
            tab.addTab("bbbbbbbbbbbbbbb", new JLabel("bbbbbbbbb"));
            tab.addTab("c", new JLabel("cccccccccc"));
            add(tab);
        });

        if (tabbedPane.getUI() instanceof WindowsTabbedPaneUI) {
            tabbedPane.setUI(new WindowsTabbedPaneUI() {
                @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                    return Math.max(MIN_TAB_WIDTH, super.calculateTabWidth(tabPlacement, tabIndex, metrics));
                }
            });
        } else {
            tabbedPane.setUI(new BasicTabbedPaneUI() {
                @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                    return Math.max(MIN_TAB_WIDTH, super.calculateTabWidth(tabPlacement, tabIndex, metrics));
                }
            });
        }
        setPreferredSize(new Dimension(320, 240));
    }
    // // TEST
    // public String makeTitle(String title) {
    //     return "<html><table width='100'><tr><td align='center'>" + title + "</td></tr></table>";
    // }
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
