package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import javax.swing.plaf.synth.*;
import javax.swing.text.View;
import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        if (tabbedPane.getUI() instanceof WindowsTabbedPaneUI) {
            tabbedPane.setUI(new MyWindowsTabbedPaneUI());
        } else {
            tabbedPane.setUI(new MyTabbedPaneUI());
        }
        final List<? extends JTabbedPane> list = Arrays.asList(
            makeTestTabbedPane(new JTabbedPane(JTabbedPane.LEFT)),
            makeTestTabbedPane(tabbedPane),
            makeTestTabbedPane(new ClippedTitleTabbedPane(JTabbedPane.LEFT)));

        JPanel p = new JPanel(new GridLayout(list.size(), 1));
        for (JTabbedPane t: list) {
            p.add(t);
        }
        add(new JCheckBox(new AbstractAction("TOP") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox c = (JCheckBox) e.getSource();
                for (JTabbedPane t: list) {
                    t.setTabPlacement(c.isSelected() ? JTabbedPane.TOP : JTabbedPane.LEFT);
                }
            }
        }), BorderLayout.NORTH);
        add(p);
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
        int tabCount  = getTabCount();
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
            tabWidth = areaWidth / 2;
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
        //JLabel label = new JLabel(title, JLabel.LEFT);
        //Dimension dim = label.getPreferredSize();
        //Insets tabInsets = getTabInsets();
        //label.setPreferredSize(new Dimension(0, dim.height + tabInsets.top + tabInsets.bottom));
        setTabComponentAt(index, new ButtonTabComponent(this));
    }
}

class MyWindowsTabbedPaneUI extends WindowsTabbedPaneUI {
    @Override protected void layoutLabel(int tabPlacement,
                                         FontMetrics metrics, int tabIndex,
                                         String title, Icon icon,
                                         Rectangle tabRect, Rectangle iconRect,
                                         Rectangle textRect, boolean isSelected) {
        textRect.setLocation(0, 0);
        iconRect.setLocation(0, 0);
        View v = getTextViewForTab(tabIndex);
        String html = "html";
        if (v != null) {
            tabPane.putClientProperty(html, v);
        }
        SwingUtilities.layoutCompoundLabel((JComponent) tabPane,
                                           metrics, title, icon,
                                           SwingUtilities.CENTER,
                                           SwingUtilities.LEFT, //CENTER, <----
                                           SwingUtilities.CENTER,
                                           SwingUtilities.TRAILING,
                                           tabRect,
                                           iconRect,
                                           textRect,
                                           textIconGap);
        tabPane.putClientProperty(html, null);
        textRect.translate(tabInsets.left + 2, 0); //<----
        textRect.width -= tabInsets.left + tabInsets.right;

        int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
        int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
        iconRect.x += xNudge;
        iconRect.y += yNudge;
        textRect.x += xNudge;
        textRect.y += yNudge;
    }
}

class MyTabbedPaneUI extends MetalTabbedPaneUI {
    @Override protected void layoutLabel(int tabPlacement,
                                         FontMetrics metrics, int tabIndex,
                                         String title, Icon icon,
                                         Rectangle tabRect, Rectangle iconRect,
                                         Rectangle textRect, boolean isSelected) {
        textRect.setLocation(0, 0);
        iconRect.setLocation(0, 0);
        View v = getTextViewForTab(tabIndex);
        if (v != null) {
            tabPane.putClientProperty("html", v);
        }
        SwingUtilities.layoutCompoundLabel((JComponent) tabPane,
                                           metrics, title, icon,
                                           SwingUtilities.CENTER,
                                           SwingUtilities.LEFT, //CENTER, <----
                                           SwingUtilities.CENTER,
                                           SwingUtilities.TRAILING,
                                           tabRect,
                                           iconRect,
                                           textRect,
                                           textIconGap);
        tabPane.putClientProperty("html", null);
        textRect.translate(tabInsets.left, 0); //<----
        textRect.width -= tabInsets.left + tabInsets.right;

        int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
        int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
        iconRect.x += xNudge;
        iconRect.y += yNudge;
        textRect.x += xNudge;
        textRect.y += yNudge;
    }
}

//How to Use Tabbed Panes (The Java Tutorials > Creating a GUI With JFC/Swing > Using Swing Components)
//http://download.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html
class ButtonTabComponent extends JPanel {
    private final JTabbedPane pane;

    public ButtonTabComponent(final JTabbedPane pane) {
        super(new BorderLayout(0, 0)); //FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null) {
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
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        JButton button = new TabButton();
        TabButtonHandler handler = new TabButtonHandler();
        button.addActionListener(handler);
        button.addMouseListener(handler);
        add(button, BorderLayout.EAST);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
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

class TabButton extends JButton {
    private static final int SIZE  = 17;
    private static final int DELTA = 6;

    public TabButton() {
        super();
        setUI(new BasicButtonUI());
        setToolTipText("close this tab");
        setContentAreaFilled(false);
        setFocusable(false);
        setBorder(BorderFactory.createEtchedBorder());
        setBorderPainted(false);
        setRolloverEnabled(true);
    }
    @Override public Dimension getPreferredSize() {
        return new Dimension(SIZE, SIZE);
    }
    @Override public void updateUI() {
        //we don't want to update UI for this button
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.BLACK);
        if (getModel().isRollover()) {
            g2.setColor(Color.ORANGE);
        }
        if (getModel().isPressed()) {
            g2.setColor(Color.BLUE);
        }
        g2.drawLine(DELTA, DELTA, getWidth() - DELTA - 1, getHeight() - DELTA - 1);
        g2.drawLine(getWidth() - DELTA - 1, DELTA, DELTA, getHeight() - DELTA - 1);
        g2.dispose();
    }
}
