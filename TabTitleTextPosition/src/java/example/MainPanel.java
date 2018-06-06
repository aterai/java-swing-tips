package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.synth.*;

public final class MainPanel extends JPanel {
    private static void addTab(JTabbedPane tabbedPane, String title, Icon icon, Component c) {
        tabbedPane.addTab(title, c);
        JLabel label = new JLabel(title, icon, SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        // label.setVerticalAlignment(SwingConstants.CENTER);
        // label.setHorizontalAlignment(SwingConstants.CENTER);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, label);
    }
    private MainPanel() {
        super(new BorderLayout());
        JTabbedPane t = new ClippedTitleTabbedPane();
        t.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        // http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
        addTab(t, "JTree", new ImageIcon(getClass().getResource("wi0009-32.png")), new JScrollPane(new JTree()));
        addTab(t, "JTextArea", new ImageIcon(getClass().getResource("wi0054-32.png")), new JScrollPane(new JTextArea()));
        addTab(t, "Preference", new ImageIcon(getClass().getResource("wi0062-32.png")), new JScrollPane(new JTree()));
        addTab(t, "Help", new ImageIcon(getClass().getResource("wi0063-32.png")), new JScrollPane(new JTextArea()));

        // t.addTab(makeTitle("Title", "wi0009-32.png"), new JLabel("a"));
        // t.addTab(makeTitle("Help", "wi0054-32.png"), new JLabel("b"));

        add(t);
        setPreferredSize(new Dimension(320, 240));
    }
    // private String makeTitle(String t, String p) {
    //     return "<html><center><img src='" + getClass().getResource(p) + "'/><br/>" + t;
    // }
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

class ClippedTitleTabbedPane extends JTabbedPane {
    protected ClippedTitleTabbedPane() {
        super();
    }
    protected ClippedTitleTabbedPane(int tabPlacement) {
        super(tabPlacement);
    }
    protected Insets getTabInsets() {
        Insets insets = UIManager.getInsets("TabbedPane.tabInsets");
        if (Objects.nonNull(insets)) {
            return insets;
        } else {
            SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB);
            SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB, style, SynthConstants.ENABLED);
            return style.getInsets(context, null);
        }
    }
    protected Insets getTabAreaInsets() {
        Insets insets = UIManager.getInsets("TabbedPane.tabAreaInsets");
        if (Objects.nonNull(insets)) {
            return insets;
        } else {
            SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB_AREA);
            SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB_AREA, style, SynthConstants.ENABLED);
            return style.getInsets(context, null);
        }
    }
    @Override public void doLayout() {
        int tabCount = getTabCount();
        if (tabCount == 0 || !isVisible()) {
            super.doLayout();
            return;
        }
        Insets tabInsets = getTabInsets();
        Insets tabAreaInsets = getTabAreaInsets();
        Insets insets = getInsets();
        int tabPlacement = getTabPlacement();
        int areaWidth = getWidth() - tabAreaInsets.left - tabAreaInsets.right - insets.left - insets.right;
        int tabWidth = 0; // = tabInsets.left + tabInsets.right + 3;
        int gap = 0;

        if (tabPlacement == LEFT || tabPlacement == RIGHT) {
            tabWidth = areaWidth / 4;
            gap = 0;
        } else { // TOP || BOTTOM
            tabWidth = areaWidth / tabCount;
            gap = areaWidth - tabWidth * tabCount;
        }
        // "3" is magic number @see BasicTabbedPaneUI#calculateTabWidth
        tabWidth -= tabInsets.left + tabInsets.right + 3;
        for (int i = 0; i < tabCount; i++) {
            int w = i < gap ? tabWidth + 1 : tabWidth;
            Optional.ofNullable((JComponent) getTabComponentAt(i))
                .ifPresent(t -> t.setPreferredSize(new Dimension(w, t.getPreferredSize().height)));
        }
        super.doLayout();
    }
    // @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    //     super.insertTab(title, icon, component, Objects.toString(tip, title), index);
    //     setTabComponentAt(index, new JLabel(title, SwingConstants.CENTER));
    // }
}
