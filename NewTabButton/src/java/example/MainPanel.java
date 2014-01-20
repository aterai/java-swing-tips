package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
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
            //for(UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
            //    if("Nimbus".equals(laf.getName())) { UIManager.setLookAndFeel(laf.getClassName()); }
            //}
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
    protected final JPanel wrapPanel = new JPanel(new BorderLayout(0, 0));
    protected final JPanel contentsPanel = new JPanel(cardLayout);
    protected final ButtonGroup bg = new ButtonGroup();

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
    public CardLayoutTabbedPane() {
        super(new BorderLayout());
        int left  = 0;
        int right = 0;
        tabPanel.setBorder(BorderFactory.createMatteBorder(0,left,0,right,new Color(20,30,50)));
        contentsPanel.setBorder(BorderFactory.createEmptyBorder(4,left,2,right));

        tabPanel.setOpaque(true);
        tabPanel.setBackground(new Color(20,30,50));

        wrapPanel.setOpaque(true);
        wrapPanel.setBackground(new Color(20,30,50));

        //contentsPanel.setOpaque(true);
        //contentsPanel.setBackground(new Color(20,30,50));

        wrapPanel.add(tabPanel);
        //TEST: wrapPanel.add(new JButton("a"), BorderLayout.WEST);

//         JPanel locPanel = new JPanel();
//         wrapPanel.add(new JButton("b"), BorderLayout.SOUTH);

        add(wrapPanel, BorderLayout.NORTH);
        add(contentsPanel);

        DummyIcon icon = new DummyIcon();
        button.setText("");
        button.setIcon(icon);
        button.setBorder(BorderFactory.createEmptyBorder());
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
                ((AbstractButton)e.getSource()).setSelected(true);
                cardLayout.show(contentsPanel, title);
            }
        });
        tab.setIcon(icons.get(new Random().nextInt(icons.size())));
        tab.setLayout(new BorderLayout());
        JButton close = new JButton(new AbstractAction("") {
            @Override public void actionPerformed(ActionEvent e) {
                tabPanel.remove(tab);
                contentsPanel.remove(comp);
                if(tabPanel.getComponentCount()>1) {
                    tabPanel.revalidate();
                    TabButton b = (TabButton)tabPanel.getComponent(0);
                    b.setSelected(true);
                    cardLayout.first(contentsPanel);
                }
                tabPanel.revalidate();
            }
        });
        Dimension dim = new Dimension(12, 12);
        close.setPreferredSize(dim);
        close.setMaximumSize(dim);
        close.setBorder(BorderFactory.createEmptyBorder());
        close.setFocusPainted(false);
        close.setContentAreaFilled(false);
        close.setIcon(new CloseTabIcon(Color.GRAY));
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
    private int count = 0;
    private final JButton button = new JButton(new AbstractAction("+") {
        @Override public void actionPerformed(ActionEvent e) {
            addTab("new tab:"+count, new JLabel("xxx:"+count));
            count++;
        }
    });
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
    private static final String uiClassID = "TabViewButtonUI";
    @Override public void updateUI() {
        if(UIManager.get(getUIClassID())!=null) {
            setUI((TabViewButtonUI)UIManager.getUI(this));
        }else{
            setUI(new BasicTabViewButtonUI());
        }
    }
    @Override public String getUIClassID() {
        return uiClassID;
    }
//     @Override public void setUI(TabViewButtonUI ui) {
//         super.setUI(ui);
//     }
    public TabViewButtonUI getUI() {
        return (TabViewButtonUI)ui;
    }
    public TabButton() {
        this(null, null);
    }
    public TabButton(Icon icon) {
        this(null, icon);
    }
    public TabButton(String text) {
        this(text, null);
    }
    public TabButton(Action a) {
        this();
        super.setAction(a);
        updateUI();
    }
    public TabButton(String text, Icon icon) {
        super(text, icon);
        updateUI();
    }
    @Override protected void fireStateChanged() {
        ButtonModel model = getModel();
        if(!model.isEnabled()) {
            setForeground(Color.GRAY);
        }else if(model.isPressed() && model.isArmed()) {
            setForeground(getPressedTextColor());
        }else if(model.isSelected()) {
            setForeground(getSelectedTextColor());
        }else if(isRolloverEnabled() && model.isRollover()) {
            setForeground(getRolloverTextColor());
        }else{
            setForeground(getTextColor());
        }
        super.fireStateChanged();
    };
    private Color textColor = Color.BLACK;
    private Color pressedTextColor = Color.BLACK;
    private Color rolloverTextColor = Color.BLACK;
    private Color rolloverSelectedTextColor = Color.BLACK;
    private Color selectedTextColor = Color.BLACK;
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
class CloseTabIcon implements Icon {
    private int width;
    private int height;
    private final Color color;
    public CloseTabIcon(Color color) {
        this.color = color;
        width  = 12;
        height = 12;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        //g.translate(x, y);
        g.setColor(color);
        g.drawLine(2, 2, 9, 9);
        g.drawLine(2, 3, 8, 9);
        g.drawLine(3, 2, 9, 8);
        g.drawLine(9, 2, 2, 9);
        g.drawLine(9, 3, 3, 9);
        g.drawLine(8, 2, 2, 8);
        //g.translate(-x, -y);
    }
    @Override public int getIconWidth() {
        return width;
    }
    @Override public int getIconHeight() {
        return height;
    }
}

class TabLayout implements LayoutManager, java.io.Serializable {
    @Override public void addLayoutComponent(String name, Component comp) {}
    @Override public void removeLayoutComponent(Component comp) {}
    @Override public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int last = parent.getComponentCount()-1;
            int w = 0, h = 0;
            if(last>=0) {
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
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            //int nrows = 1;
            int ncols = ncomponents-1;
            //boolean ltr = parent.getComponentOrientation().isLeftToRight();

            if(ncomponents == 0) {
                return;
            }
            int lastw = parent.getComponent(ncomponents-1).getPreferredSize().width;
            int width = parent.getWidth() - insets.left - insets.right - lastw;
            int h = parent.getHeight() - insets.top - insets.bottom;
            int w = width>100*(ncomponents-1) ? 100 : width/ncols;
            int gap = width - w*ncols;
            int x = insets.left;
            int y = insets.top;
            for(int i=0;i<ncomponents;i++) {
                int a = 0;
                if(gap>0) {
                    a = 1;
                    gap--;
                }
                int cw = w + a;
                if(i==ncols) {
                    cw = lastw;
                }
                parent.getComponent(i).setBounds(x, y, cw, h);
                x += w + a;
            }
        }
    }
    @Override public String toString() {
        return getClass().getName();
    }
}

class DummyIcon implements Icon {
    private static Rectangle viewRect = new Rectangle();
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g;
        g2.translate(x,y);

        Insets i = ((JComponent)c).getInsets();
        Dimension size = c.getSize();

        viewRect.x      = i.left;
        viewRect.y      = i.top;
        viewRect.width  = size.width  - i.right  - viewRect.x;
        viewRect.height = size.height - i.bottom - viewRect.y;
        OperaTabViewButtonUI.tabPainter(g2, viewRect);

        g2.setPaint(Color.WHITE);
        int w = viewRect.width;
        int a = w/2;
        int b = w/3;
        w-=2;
        g2.drawLine(a,   b, a,   w-b);
        g2.drawLine(a-1, b, a-1, w-b);
        g2.drawLine(b, a,   w-b, a);
        g2.drawLine(b, a-1, w-b, a-1);
        g2.translate(-x,-y);
    }
    @Override public int getIconWidth()  {
        return 24;
    }
    @Override public int getIconHeight() {
        return 24;
    }
}
