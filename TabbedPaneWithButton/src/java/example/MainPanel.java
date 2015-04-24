package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.synth.*;

public final class MainPanel extends JPanel {
    private final ClippedTitleTabbedPane tabs;
    public MainPanel() {
        super(new BorderLayout());

        //famfamfam.com: Mini Icons>http://www.famfamfam.com/lab/icons/mini/
        ImageIcon icon = new ImageIcon(getClass().getResource("page_new.gif"));

        final JButton b = new ToolBarButton(icon);
        b.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                tabs.addTab("qwerqwer", new JLabel("yetyet"));
            }
        });
        //b.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        //UIManager.put("TabbedPane.tabAreaInsets", getButtonPaddingTabAreaInsets(b));

        tabs = new ClippedTitleTabbedPane() {
            private Insets tabAreaInsets;
            @Override public void updateUI() {
                UIManager.put("TabbedPane.tabAreaInsets", null); //uninstall
                super.updateUI();
                setAlignmentX(Component.LEFT_ALIGNMENT);
                setAlignmentY(Component.TOP_ALIGNMENT);
                b.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
                b.setAlignmentX(Component.LEFT_ALIGNMENT);
                b.setAlignmentY(Component.TOP_ALIGNMENT);
                tabAreaInsets = getTabAreaInsets();
                UIManager.put("TabbedPane.tabAreaInsets",
                              getButtonPaddingTabAreaInsets(b, getTabInsets(), tabAreaInsets));
                super.updateUI();
            }
        };
        tabs.addTab("asdfasd", new JLabel("456746"));
        tabs.addTab("1234123", new JScrollPane(new JTree()));
        tabs.addTab("6780969", new JLabel("zxcvzxc"));

        JPanel p = new JPanel();
        p.setLayout(new OverlayLayout(p));
        p.add(b);
        p.add(tabs);

        JMenuBar menubar = new JMenuBar();
        JMenu m1 = new JMenu("Tab");
        m1.add(new AbstractAction("removeAll") {
            @Override public void actionPerformed(ActionEvent e) {
                tabs.removeAll();
            }
        });
        menubar.add(m1);
        menubar.add(new JMenu("Dummy1"));
        menubar.add(new JMenu("Dummy2"));

        add(menubar, BorderLayout.NORTH);
        add(p);
        setPreferredSize(new Dimension(320, 240));
    }
    public Insets getButtonPaddingTabAreaInsets(JButton b, Insets ti, Insets ai) {
        FontMetrics fm = b.getFontMetrics(b.getFont());
        int tih = b.getPreferredSize().height - fm.getHeight() - ti.top - ti.bottom - ai.bottom;
        return new Insets(Math.max(ai.top, tih), b.getPreferredSize().width + ai.left, ai.bottom, ai.right);
        //NO EFFECT?: return new javax.swing.plaf.InsetsUIResource(Math.max(ai.top, tih), b.getPreferredSize().width + ai.left, ai.bottom, ai.right);
    }
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
        frame.setMinimumSize(new Dimension(256, 200));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class ToolBarButton extends JButton {
    public ToolBarButton(ImageIcon icon) {
        super(icon);
        setFocusable(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent me) {
                setContentAreaFilled(true);
            }
            @Override public void mouseExited(MouseEvent me) {
                setContentAreaFilled(false);
            }
        });
    }
}

class ClippedTitleTabbedPane extends JTabbedPane {
    public ClippedTitleTabbedPane() {
        super();
    }
    public ClippedTitleTabbedPane(int tabPlacement) {
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
        Insets tabInsets     = getTabInsets();
        Insets tabAreaInsets = getTabAreaInsets();
        Insets insets = getInsets();
        int areaWidth = getWidth() - tabAreaInsets.left - tabAreaInsets.right - insets.left - insets.right;
        int tabWidth  = 0; // = tabInsets.left + tabInsets.right + 3;
        int gap       = 0;

        switch (getTabPlacement()) {
          case LEFT: case RIGHT:
            tabWidth = areaWidth / 4;
            gap = 0;
            break;
          case BOTTOM: case TOP: default:
            tabWidth = areaWidth / tabCount;
            gap = areaWidth - tabWidth * tabCount;
            break;
        }
        if (tabWidth > 80) {
            tabWidth = 80;
            gap = 0;
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
        setVisible(false);
        super.insertTab(title, icon, component, Objects.toString(tip, title), index);
        setTabComponentAt(index, new JLabel(title, JLabel.CENTER));
        setVisible(true);
    }
}
