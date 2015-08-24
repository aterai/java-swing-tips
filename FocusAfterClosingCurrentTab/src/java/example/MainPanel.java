package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.synth.*;

public class MainPanel extends JPanel {
    private static final String CLOSE_CURRENT_TAB = "close_current_tab";
    private final JTabbedPane tabbedPane = new ClippedTitleTabbedPane() {
        private final List<Component> history = new ArrayList<Component>(5);
        @Override public void setSelectedIndex(int index) {
            super.setSelectedIndex(index);
            Component component = getComponentAt(index);
            history.remove(component);
            history.add(0, component);
        }
        @Override public void removeTabAt(int index) {
            Component component = getComponentAt(index);
            super.removeTabAt(index);
            history.remove(component);
            if (!history.isEmpty()) {
                setSelectedComponent(history.get(0));
            }
        }
    };

    public MainPanel() {
        super(new BorderLayout());

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addTab("aaaa", new JLabel("aaa"));
        tabbedPane.addTab("bbbbbb", new JLabel("bbb"));
        tabbedPane.addTab("ccc", new JLabel("ccc"));
        tabbedPane.addTab("d", new JLabel("ddd"));
        tabbedPane.addTab("ee", new JLabel("eee"));

        InputMap im = tabbedPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK), CLOSE_CURRENT_TAB);
        tabbedPane.getActionMap().put(CLOSE_CURRENT_TAB, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                JTabbedPane t = (JTabbedPane) e.getSource();
                int idx = t.getSelectedIndex();
                if (idx >= 0) {
                    t.removeTabAt(idx);
                }
            }
        });

        add(tabbedPane);
        JPanel p = new JPanel(new GridLayout(1, 2, 2, 2));
        p.add(new JButton(new AbstractAction("add tab") {
            @Override public void actionPerformed(ActionEvent e) {
                String title = new Date().toString();
                tabbedPane.addTab(title, new JLabel(title));
            }
        }));
        p.add(new JButton(new AbstractAction("add tab with focus") {
            @Override public void actionPerformed(ActionEvent e) {
                String title = new Date().toString();
                tabbedPane.addTab(title, new JLabel(title));
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            }
        }));
        add(p, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// http://ateraimemo.com/Swing/ClippedTabLabel.html
class ClippedTitleTabbedPane extends JTabbedPane {
    private static final int MAX_TAB_WIDTH = 200;
    private static final int MIN_TAB_WIDTH = 50;
    public ClippedTitleTabbedPane() {
        super();
    }
    public ClippedTitleTabbedPane(int tabPlacement) {
        super(tabPlacement);
    }
    private Insets getTabInsets() {
        Insets insets = UIManager.getInsets("TabbedPane.tabInsets");
        if (Objects.nonNull(insets)) {
            return insets;
        } else {
            SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB);
            SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB, style, SynthConstants.ENABLED);
            return style.getInsets(context, null);
        }
    }
    private Insets getTabAreaInsets() {
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
        int tabWidth  = 0;
        int gap       = 0;

        switch (getTabPlacement()) {
          case LEFT: case RIGHT:
            tabWidth = Math.min(MAX_TAB_WIDTH, Math.max(MIN_TAB_WIDTH, areaWidth / 3));
            break;
          case BOTTOM: case TOP: default:
            tabWidth = areaWidth / tabCount;
            if (tabWidth < MIN_TAB_WIDTH) {
                tabWidth = MIN_TAB_WIDTH;
            } else if (tabWidth > MAX_TAB_WIDTH) {
                tabWidth = MAX_TAB_WIDTH;
            } else {
                gap = areaWidth - tabWidth * tabCount;
            }
            break;
        }

        // "3" is magic number @see BasicTabbedPaneUI#calculateTabWidth
        int tabComponentWidth = tabWidth - tabInsets.left - tabInsets.right - 3;
        for (int i = 0; i < tabCount; i++) {
            JComponent tab = (JComponent) getTabComponentAt(i);
            //if (Objects.isNull(tab)) {
            //    System.out.println(tabCount);
            //    break;
            //}
            int v = i < gap ? 1 : 0;
            tab.setPreferredSize(new Dimension(tabComponentWidth + v, tab.getPreferredSize().height));
        }
        super.doLayout();
    }
    @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
        setVisible(false);
        super.insertTab(title, icon, component, Objects.nonNull(tip) ? tip : title, index);
        setTabComponentAt(index, new ButtonTabComponent(this));
        setVisible(true);
    }
}

//How to Use Tabbed Panes (The Java Tutorials > Creating a GUI With JFC/Swing > Using Swing Components)
//http://download.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html
class ButtonTabComponent extends JPanel {
    private final JTabbedPane pane;

    public ButtonTabComponent(final JTabbedPane pane) {
        super(new BorderLayout(0, 0));
        if (Objects.isNull(pane)) {
            throw new IllegalArgumentException("TabbedPane cannot be null");
        }
        this.pane = pane;
        setOpaque(false);
        JLabel label = new JLabel() {
            @Override public String getText() {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };
        add(label);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));

        JButton button = new JButton(new CloseTabIcon(Color.BLACK));
        button.setRolloverIcon(new CloseTabIcon(Color.ORANGE));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        TabButtonHandler handler = new TabButtonHandler();
        button.addActionListener(handler);
        button.addMouseListener(handler);

        add(button, BorderLayout.EAST);
        setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
    }
    private class TabButtonHandler extends MouseAdapter implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
                pane.remove(i);
            }
        }
        @Override public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }
        @Override public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    }
}

class CloseTabIcon implements Icon {
    private final Color color;
    public CloseTabIcon(Color color) {
        this.color = color;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(color);
        g2.drawLine(4,  4, 11, 11);
        g2.drawLine(4,  5, 10, 11);
        g2.drawLine(5,  4, 11, 10);
        g2.drawLine(11, 4,  4, 11);
        g2.drawLine(11, 5,  5, 11);
        g2.drawLine(10, 4,  4, 10);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 16;
    }
    @Override public int getIconHeight() {
        return 16;
    }
}
