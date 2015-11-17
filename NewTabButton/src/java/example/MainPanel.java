package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        UIManager.put("example.TabButton", "TabViewButtonUI");
        UIManager.put("TabViewButtonUI", "example.OperaTabViewButtonUI");
        CardLayoutTabbedPane tab3 = new CardLayoutTabbedPane();
        tab3.setBorder(BorderFactory.createTitledBorder("CardLayout+JRadioButton(opera like)"));
        tab3.addTab("9999", new JScrollPane(new JTree()));
        tab3.addTab("aaaaaaaaaaaaaaaaaaaaaaa", new JLabel("hhhhh"));
        tab3.addTab("bbbb", new JLabel("iiii"));
        tab3.addTab("cccc", new JButton("jjjjjj"));
        add(tab3);
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
            //for (UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
            //    if ("Nimbus".equals(laf.getName())) {
            //        UIManager.setLookAndFeel(laf.getClassName());
            //    }
            //}
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

class CardLayoutTabbedPane extends JPanel {
    protected final CardLayout cardLayout = new CardLayout();
    protected final JPanel tabPanel = new JPanel(new TabLayout());
    protected final JPanel wrapPanel = new JPanel(new BorderLayout());
    protected final JPanel contentsPanel = new JPanel(cardLayout);
    protected final ButtonGroup bg = new ButtonGroup();
    private final JButton button = new JButton(new PlusIcon());

    //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
    private final List<ImageIcon> icons = Arrays.asList(
        new ImageIcon(getClass().getResource("wi0009-16.png")),
        new ImageIcon(getClass().getResource("wi0054-16.png")),
        new ImageIcon(getClass().getResource("wi0062-16.png")),
        new ImageIcon(getClass().getResource("wi0063-16.png")),
        new ImageIcon(getClass().getResource("wi0064-16.png")),
        new ImageIcon(getClass().getResource("wi0096-16.png")),
        new ImageIcon(getClass().getResource("wi0111-16.png")),
        new ImageIcon(getClass().getResource("wi0122-16.png")),
        new ImageIcon(getClass().getResource("wi0124-16.png")),
        new ImageIcon(getClass().getResource("wi0126-16.png"))
    );

    protected CardLayoutTabbedPane() {
        super(new BorderLayout());
        int left  = 0;
        int right = 0;
        tabPanel.setBorder(BorderFactory.createMatteBorder(0, left, 0, right, new Color(20, 30, 50)));
        contentsPanel.setBorder(BorderFactory.createEmptyBorder(4, left, 2, right));

        tabPanel.setOpaque(true);
        tabPanel.setBackground(new Color(20, 30, 50));

        wrapPanel.setOpaque(true);
        wrapPanel.setBackground(new Color(20, 30, 50));

        //contentsPanel.setOpaque(true);
        //contentsPanel.setBackground(new Color(20, 30, 50));

        wrapPanel.add(tabPanel);
        //TEST: wrapPanel.add(new JButton("a"), BorderLayout.WEST);

//         JPanel locPanel = new JPanel();
//         wrapPanel.add(new JButton("b"), BorderLayout.SOUTH);

        add(wrapPanel, BorderLayout.NORTH);
        add(contentsPanel);

        button.setBorder(BorderFactory.createEmptyBorder());
        button.addActionListener(new ActionListener() {
            private int count;
            @Override public void actionPerformed(ActionEvent e) {
                addTab("new tab:" + count, new JLabel("xxx:" + count));
                count++;
            }
        });
    }
    protected JComponent createTabComponent(final String title, final Component comp) {
//         final TabButton tab = new TabButton(new AbstractAction(title) {
//             @Override public void actionPerformed(ActionEvent e) {
//                 cardLayout.show(contentsPanel, title);
//             }
//         });
        final TabButton tab = new TabButton(title);
        tab.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                ((AbstractButton) e.getComponent()).setSelected(true);
                cardLayout.show(contentsPanel, title);
            }
        });
        tab.setIcon(icons.get(new Random().nextInt(icons.size())));
        tab.setLayout(new BorderLayout());
        JButton close = new JButton(new CloseTabIcon(Color.GRAY)) {
            @Override public Dimension getPreferredSize() {
                return new Dimension(12, 12);
            }
        };
        close.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                tabPanel.remove(tab);
                contentsPanel.remove(comp);
                if (tabPanel.getComponentCount() > 1) {
                    tabPanel.revalidate();
                    TabButton b = (TabButton) tabPanel.getComponent(0);
                    b.setSelected(true);
                    cardLayout.first(contentsPanel);
                }
                tabPanel.revalidate();
            }
        });
        close.setBorder(BorderFactory.createEmptyBorder());
        close.setFocusPainted(false);
        close.setContentAreaFilled(false);
        close.setPressedIcon(new CloseTabIcon(Color.BLACK));
        close.setRolloverIcon(new CloseTabIcon(Color.ORANGE));

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(close, BorderLayout.NORTH);
        tab.add(p, BorderLayout.EAST);
        bg.add(tab);
        tab.setSelected(true);
        return tab;
    }
    public void addTab(String title, Component comp) {
        tabPanel.remove(button);
        tabPanel.add(createTabComponent(title, comp));
        tabPanel.add(button);
        tabPanel.revalidate();
        contentsPanel.add(comp, title);
        cardLayout.show(contentsPanel, title);
    }
}

class TabButton extends JRadioButton {
    private static final String UI_CLASS_ID = "TabViewButtonUI";
    private Color textColor; // = Color.WHITE;
    private Color pressedTextColor; // = Color.WHITE.darker();
    private Color rolloverTextColor; // = Color.WHITE;
    private Color rolloverSelectedTextColor; // = Color.WHITE;
    private Color selectedTextColor; // = Color.WHITE;
    @Override public void updateUI() {
        if (Objects.nonNull(UIManager.get(getUIClassID()))) {
            setUI((TabViewButtonUI) UIManager.getUI(this));
        } else {
            setUI(new BasicTabViewButtonUI());
        }
    }
    @Override public String getUIClassID() {
        return UI_CLASS_ID;
    }
//     @Override public void setUI(TabViewButtonUI ui) {
//         super.setUI(ui);
//     }
    @Override public TabViewButtonUI getUI() {
        return (TabViewButtonUI) ui;
    }
    protected TabButton() {
        super(null, null);
    }
    protected TabButton(Icon icon) {
        super(null, icon);
    }
    protected TabButton(String text) {
        super(text, null);
    }
    protected TabButton(Action a) {
        super(a);
        //super.setAction(a);
        //updateUI();
    }
    protected TabButton(String text, Icon icon) {
        super(text, icon);
        //updateUI();
    }
    @Override protected void fireStateChanged() {
        ButtonModel model = getModel();
        if (model.isEnabled()) {
            if (model.isPressed() && model.isArmed()) {
                setForeground(getPressedTextColor());
            } else if (model.isSelected()) {
                setForeground(getSelectedTextColor());
            } else if (isRolloverEnabled() && model.isRollover()) {
                setForeground(getRolloverTextColor());
            } else {
                setForeground(getTextColor());
            }
        } else {
            setForeground(Color.GRAY);
        }
        super.fireStateChanged();
    }
    public Color getTextColor() {
        return textColor;
    }
    public Color getPressedTextColor() {
        return pressedTextColor;
    }
    public Color getRolloverTextColor() {
        return rolloverTextColor;
    }
    public Color getRolloverSelectedTextColor() {
        return rolloverSelectedTextColor;
    }
    public Color getSelectedTextColor() {
        return selectedTextColor;
    }
    public void setTextColor(Color color) {
        textColor = color;
    }
    public void setPressedTextColor(Color color) {
        pressedTextColor = color;
    }
    public void setRolloverTextColor(Color color) {
        rolloverTextColor = color;
    }
    public void setRolloverSelectedTextColor(Color color) {
        rolloverSelectedTextColor = color;
    }
    public void setSelectedTextColor(Color color) {
        selectedTextColor = color;
    }
}

class TabLayout implements LayoutManager, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int TAB_WIDTH = 100;
    @Override public void addLayoutComponent(String name, Component comp) { /* not needed */ }
    @Override public void removeLayoutComponent(Component comp)           { /* not needed */ }
    @Override public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int last = parent.getComponentCount() - 1;
            int w = 0;
            int h = 0;
            if (last >= 0) {
                Component comp = parent.getComponent(last);
                Dimension d = comp.getPreferredSize();
                w = d.width;
                h = d.height;
            }
            return new Dimension(insets.left + insets.right + w,
                                 insets.top + insets.bottom + h);
        }
    }

    @Override public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            return new Dimension(100, 24);
        }
    }

    @Override public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            int ncomponents = parent.getComponentCount();
            if (ncomponents == 0) {
                return;
            }
            //int nrows = 1;
            //boolean ltr = parent.getComponentOrientation().isLeftToRight();
            Insets insets = parent.getInsets();
            int ncols = ncomponents - 1;
            int lastw = parent.getComponent(ncomponents - 1).getPreferredSize().width;
            int width = parent.getWidth() - insets.left - insets.right - lastw;
            int h = parent.getHeight() - insets.top - insets.bottom;
            int w = width > TAB_WIDTH * ncols ? TAB_WIDTH : width / ncols;
            int gap = width - w * ncols;
            int x = insets.left;
            int y = insets.top;
            for (int i = 0; i < ncomponents; i++) {
                int cw = i == ncols ? lastw : w + (gap-- > 0 ? 1 : 0);
                parent.getComponent(i).setBounds(x, y, cw, h);
                x += cw;
            }
        }
    }
    @Override public String toString() {
        return getClass().getName();
    }
}
