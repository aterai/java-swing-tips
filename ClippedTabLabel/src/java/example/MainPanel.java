package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.synth.*;

public final class MainPanel extends JPanel {
    private final List<? extends JTabbedPane> list = Arrays.asList(
        makeTestTabbedPane(new JTabbedPane()),
        makeTestTabbedPane(new ClippedTitleTabbedPane()));
    public MainPanel() {
        super(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(2, 1));
        for (JTabbedPane t: list) {
            p.add(t);
        }
        add(p);
        add(new JCheckBox(new AbstractAction("LEFT") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox c = (JCheckBox) e.getSource();
                for (JTabbedPane t: list) {
                    t.setTabPlacement(c.isSelected() ? JTabbedPane.LEFT : JTabbedPane.TOP);
                }
            }
        }), BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JTabbedPane makeTestTabbedPane(JTabbedPane jtp) {
        jtp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        jtp.addTab("1111111111111111111", new JScrollPane(new JTree()));
        jtp.addTab("2", new JLabel("bbbbbbbbb"));
        jtp.addTab("33333333333333", new JScrollPane(new JTree()));
        jtp.addTab("444444444444444", new JLabel("dddddddddd"));
        jtp.addTab("55555555555555555555555555555555", new JLabel("e"));
        return jtp;
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
        frame.setMinimumSize(new Dimension(256, 200));
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
        Insets insets = UIManager.getInsets("TabbedPane.tabInsets");
        if (insets == null) {
            SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB);
            SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB, style, SynthConstants.ENABLED);
            return style.getInsets(context, null);
        } else {
            return insets;
        }
    }
    private Insets getTabAreaInsets() {
        Insets insets = UIManager.getInsets("TabbedPane.tabAreaInsets");
        if (insets == null) {
            SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB_AREA);
            SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB_AREA, style, SynthConstants.ENABLED);
            return style.getInsets(context, null);
        } else {
            return insets;
        }
    }
    @Override public void doLayout() {
        int tabCount = getTabCount();
        if (tabCount == 0) {
            return;
        }
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
            break;
        }
        // "3" is magic number @see BasicTabbedPaneUI#calculateTabWidth
        tabWidth = tabWidth - tabInsets.left - tabInsets.right - 3;
        for (int i = 0; i < tabCount; i++) {
            JComponent l = (JComponent) getTabComponentAt(i);
            int v = i < gap ? 1 : 0;
            l.setPreferredSize(new Dimension(tabWidth + v, l.getPreferredSize().height));
        }
        super.doLayout();
    }
    @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
        super.insertTab(title, icon, component, tip == null ? title : tip, index);
        JLabel label = new JLabel(title, JLabel.CENTER);
        //Dimension dim = label.getPreferredSize();
        //Insets tabInsets = getTabInsets();
        //label.setPreferredSize(new Dimension(0, dim.height + tabInsets.top + tabInsets.bottom));
        setTabComponentAt(index, label);
    }
}
