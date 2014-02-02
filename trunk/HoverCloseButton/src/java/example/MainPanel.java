package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final MyJTabbedPane tab = new MyJTabbedPane();
    private final JPopupMenu pop = new JPopupMenu();
    private static int count;

    public MainPanel() {
        super(new BorderLayout());
        pop.add(new NewTabAction("Add", null));
        pop.addSeparator();
        pop.add(new CloseAllAction("Close All", null));
        tab.setComponentPopupMenu(pop);
        tab.addTab("aaaaaa", new JScrollPane(new JTree()));
        tab.addTab("12345678901234567890", new JScrollPane(new JLabel("asdfasdfsadf")));
        tab.addTab("b", new JScrollPane(new JTree()));

        tab.setSelectedIndex(0);
        TabPanel titleTab = (TabPanel) tab.getTabComponentAt(0);
        titleTab.setButtonVisible(true);

        add(tab);
        setPreferredSize(new Dimension(320, 240));
    }
    class NewTabAction extends AbstractAction {
        public NewTabAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            tab.addTab("Title"+count, new JLabel("Tab"+count));
            tab.setSelectedIndex(tab.getTabCount()-1);
            count++;
        }
    }
    class CloseAllAction extends AbstractAction {
        public CloseAllAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            tab.removeAll();
        }
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

class MyJTabbedPane extends JTabbedPane {
    //private final Insets tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
    public MyJTabbedPane() {
        super();
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        addMouseMotionListener(new MouseMotionAdapter() {
            private int prev = -1;
            @Override public void mouseMoved(MouseEvent e) {
                JTabbedPane source = (JTabbedPane)e.getSource();
                int focussed = source.indexAtLocation(e.getX(), e.getY());
                if(focussed==prev) { return; }
                for(int i=0;i<source.getTabCount();i++) {
                    TabPanel tab = (TabPanel)source.getTabComponentAt(i);
                    tab.setButtonVisible(i==focussed);
                }
                prev = focussed;
            }
        });
    }
    @Override public void addTab(String title, final Component content) {
        super.addTab(title, content);
        setTabComponentAt(getTabCount()-1, new TabPanel(this, title, content));
    }
}

class TabPanel extends JPanel {
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
            int w = button.isVisible() ? 80 - button.getPreferredSize().width : 80;
            return new Dimension(w, dim.height);
        }
    };
    public TabPanel(final JTabbedPane pane, String title, final Component content) {
        super(new BorderLayout(0, 0));
        setOpaque(false);

        label.setText(title);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));

        button.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                int idx = pane.indexOfComponent(content);
                pane.removeTabAt(idx);
                int count = pane.getTabCount();
                if(count>idx) {
                    TabPanel tab = (TabPanel)pane.getTabComponentAt(idx);
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
        g.translate(x, y);
        g.setColor(Color.ORANGE);
        g.drawLine(2, 3, 9, 10);
        g.drawLine(2, 4, 8, 10);
        g.drawLine(3, 3, 9, 9);
        g.drawLine(9, 3, 2, 10);
        g.drawLine(9, 4, 3, 10);
        g.drawLine(8, 3, 2, 9);
        g.translate(-x, -y);
    }
    @Override public int getIconWidth() {
        return 12;
    }
    @Override public int getIconHeight() {
        return 12;
    }
}
