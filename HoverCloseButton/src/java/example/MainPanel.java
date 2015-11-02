package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTabbedPane tabbedPane = new HoverCloseButtonTabbedPane();
    private final JPopupMenu pop = new JPopupMenu();
    private static int count;

    public MainPanel() {
        super(new BorderLayout());
        pop.add(new NewTabAction("Add", null));
        pop.addSeparator();
        pop.add(new CloseAllAction("Close All", null));
        tabbedPane.setComponentPopupMenu(pop);
        tabbedPane.addTab("aaaaaa", new JScrollPane(new JTree()));
        tabbedPane.addTab("12345678901234567890", new JScrollPane(new JLabel("asdfasdfsadf")));
        tabbedPane.addTab("b", new JScrollPane(new JTree()));

//         tab.setSelectedIndex(0);
//         TabPanel titleTab = (TabPanel) tab.getTabComponentAt(0);
//         titleTab.setButtonVisible(true);

        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
    }
    class NewTabAction extends AbstractAction {
        protected NewTabAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            tabbedPane.addTab("Title" + count, new JLabel("Tab" + count));
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            count++;
        }
    }
    class CloseAllAction extends AbstractAction {
        protected CloseAllAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            tabbedPane.removeAll();
        }
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

class HoverCloseButtonTabbedPane extends JTabbedPane {
    //private final Insets tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
    private transient MouseMotionListener hoverHandler;
    protected HoverCloseButtonTabbedPane() {
        super(TOP, SCROLL_TAB_LAYOUT);
    }
    protected HoverCloseButtonTabbedPane(int tabPlacement) {
        super(tabPlacement, SCROLL_TAB_LAYOUT);
    }
//     public HoverCloseButtonTabbedPane(int tabPlacement, int tabLayoutPolicy) {
//         super(tabPlacement, SCROLL_TAB_LAYOUT);
//     }
    @Override public void updateUI() {
        removeMouseMotionListener(hoverHandler);
        super.updateUI();
        //setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        hoverHandler = new MouseMotionAdapter() {
            private int prev = -1;
            @Override public void mouseMoved(MouseEvent e) {
                JTabbedPane source = (JTabbedPane) e.getComponent();
                int focussed = source.indexAtLocation(e.getX(), e.getY());
                if (focussed == prev) {
                    return;
                }
                for (int i = 0; i < source.getTabCount(); i++) {
                    TabPanel tab = (TabPanel) source.getTabComponentAt(i);
                    tab.setButtonVisible(i == focussed);
                }
                prev = focussed;
            }
        };
        addMouseMotionListener(hoverHandler);
    }
    @Override public void addTab(String title, final Component content) {
        super.addTab(title, content);
        setTabComponentAt(getTabCount() - 1, new TabPanel(this, title, content));
    }
}

class TabPanel extends JPanel {
    private static final int PREFERRED_TAB_WIDTH = 80;
    private final JButton button = new JButton(new CloseTabIcon()) {
        @Override public void updateUI() {
            super.updateUI();
            setBorder(BorderFactory.createEmptyBorder());
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setFocusable(false);
            setVisible(false);
        }
    };
    private final JLabel label = new JLabel() {
        @Override public Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize();
            int bw = button.isVisible() ? button.getPreferredSize().width : 0;
            return new Dimension(PREFERRED_TAB_WIDTH - bw, dim.height);
        }
    };
    protected TabPanel(final JTabbedPane pane, String title, final Component content) {
        super(new BorderLayout(0, 0));
        setOpaque(false);

        label.setText(title);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));

        button.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                int idx = pane.indexOfComponent(content);
                pane.removeTabAt(idx);
                int count = pane.getTabCount();
                if (count > idx) {
                    TabPanel tab = (TabPanel) pane.getTabComponentAt(idx);
                    tab.setButtonVisible(true);
                }
            }
        });
        add(label);
        add(button, BorderLayout.EAST);
    }
    public void setButtonVisible(boolean flag) {
        button.setVisible(flag);
    }
}

class CloseTabIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(Color.ORANGE);
        g2.drawLine(2, 3, 9, 10);
        g2.drawLine(2, 4, 8, 10);
        g2.drawLine(3, 3, 9, 9);
        g2.drawLine(9, 3, 2, 10);
        g2.drawLine(9, 4, 3, 10);
        g2.drawLine(8, 3, 2, 9);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 12;
    }
    @Override public int getIconHeight() {
        return 12;
    }
}
