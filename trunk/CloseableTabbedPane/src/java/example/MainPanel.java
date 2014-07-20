package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Date;
import javax.swing.*;
import javax.swing.plaf.*;

public class MainPanel extends JPanel {
    private final JTabbedPane tabbedPane0 = new CloseableTabbedPane();
    private final JTabbedPane tabbedPane1 = new JTabbedPane();

    public MainPanel() {
        super(new BorderLayout());

        for (JTabbedPane t: Arrays.asList(tabbedPane0, tabbedPane1)) {
            t.addTab("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new JLabel("aaa"));
            t.addTab("bbbbbbbbaa", new JLabel("bbb"));
            t.addTab("ccc", new JLabel("ccc"));
            t.addTab("d", new JLabel("ddd"));
        }

//         EventQueue.invokeLater(new Runnable() {
//             @Override public void run() {
//                 JPanel gp = new CloseableTabbedPaneGlassPane(tabbedPane);
//                 tabbedPane.getRootPane().setGlassPane(gp);
//                 gp.setOpaque(false);
//                 gp.setVisible(true);
//             }
//         });

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setTopComponent(tabbedPane0);
        sp.setBottomComponent(new JLayer<JTabbedPane>(tabbedPane1, new CloseableTabbedPaneLayerUI()));

        add(sp);
        add(new JButton(new AbstractAction("add tab") {
            @Override public void actionPerformed(ActionEvent e) {
                String title = new Date().toString();
                for (JTabbedPane t: Arrays.asList(tabbedPane0, tabbedPane1)) {
                    t.addTab(title, new JLabel(title));
                }
            }
        }), BorderLayout.SOUTH);
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("TabbedPane.tabInsets", new Insets(2, 18, 2, 18));
        } catch (ClassNotFoundException | InstantiationException |
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

class CloseTabIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        g.translate(x, y);
        g.setColor(Color.BLACK);
        if (c instanceof AbstractButton) {
            ButtonModel m = ((AbstractButton) c).getModel();
            if (m.isRollover()) {
                g.setColor(Color.ORANGE);
            }
        }
        g.drawLine(4,  4, 11, 11);
        g.drawLine(4,  5, 10, 11);
        g.drawLine(5,  4, 11, 10);
        g.drawLine(11, 4,  4, 11);
        g.drawLine(11, 5,  5, 11);
        g.drawLine(10, 4,  4, 10);
        g.translate(-x, -y);
    }
    @Override public int getIconWidth() {
        return 16;
    }
    @Override public int getIconHeight() {
        return 16;
    }
}

class CloseableTabbedPane extends JTabbedPane {
    private static final Icon CLOSE_ICON = new CloseTabIcon();
    @Override public void addTab(String title, final Component content) {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        JButton button = new JButton(CLOSE_ICON);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                removeTabAt(indexOfComponent(content));
            }
        });
        tab.add(label,  BorderLayout.WEST);
        tab.add(button, BorderLayout.EAST);
        tab.setBorder(BorderFactory.createEmptyBorder(2, 1, 1, 1));
        super.addTab(title, content);
        setTabComponentAt(getTabCount() - 1, tab);
    }
}

class CloseableTabbedPaneLayerUI extends LayerUI<JTabbedPane> {
    private final JComponent rubberStamp = new JPanel();
    private final Point pt = new Point(-100, -100);
    private final JButton button = new JButton(new CloseTabIcon());
//     {
//         @Override public Dimension getPreferredSize() {
//             return new Dimension(16, 16);
//         }
//     };
    public CloseableTabbedPaneLayerUI() {
        super();
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setRolloverEnabled(false);
    }
    @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (c instanceof JLayer) {
            JLayer jlayer = (JLayer) c;
            JTabbedPane tabPane = (JTabbedPane) jlayer.getView();
            for (int i = 0; i < tabPane.getTabCount(); i++) {
                Rectangle rect = tabPane.getBoundsAt(i);
                Dimension d = button.getPreferredSize();
                int x = rect.x + rect.width - d.width - 2;
                int y = rect.y + (rect.height - d.height) / 2;
                Rectangle r = new Rectangle(x, y, d.width, d.height);
                button.getModel().setRollover(r.contains(pt));
                SwingUtilities.paintComponent(g, button, rubberStamp, r);
            }
        }
    }
    @Override public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
            ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        }
    }
    @Override public void uninstallUI(JComponent c) {
        if (c instanceof JLayer) {
            ((JLayer) c).setLayerEventMask(0);
        }
        super.uninstallUI(c);
    }
    @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JTabbedPane> l) {
        if (e.getID() == MouseEvent.MOUSE_CLICKED) {
            pt.setLocation(e.getPoint());
            JTabbedPane tabbedPane = (JTabbedPane) l.getView();
            int index = tabbedPane.indexAtLocation(pt.x, pt.y);
            if (index >= 0) {
                Rectangle rect = tabbedPane.getBoundsAt(index);
                Dimension d = button.getPreferredSize();
                int x = rect.x + rect.width - d.width - 2;
                int y = rect.y + (rect.height - d.height) / 2;
                Rectangle r = new Rectangle(x, y, d.width, d.height);
                if (r.contains(pt)) {
                    tabbedPane.removeTabAt(index);
                }
            }
        }
    }
    @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JTabbedPane> l) {
        pt.setLocation(e.getPoint());
        JTabbedPane tabbedPane = (JTabbedPane) l.getView();
        int index = tabbedPane.indexAtLocation(pt.x, pt.y);
        if (index >= 0) {
            Point loc = e.getPoint();
            loc.translate(-16, -16);
            l.repaint(new Rectangle(loc, new Dimension(32, 32)));
        }
    }
//         System.out.format("%d : %d%n", prevIndex, index);
//         if (index >= 0) {
//             if (prevIndex >= 0 && prevIndex != index) {
//                 Rectangle rect = tabbedPane.getBoundsAt(prevIndex);
//                 rect.add(tabbedPane.getBoundsAt(index));
//                 System.out.println(rect);
//                 tabbedPane.repaint(rect);
//             } else {
//                 tabbedPane.repaint(tabbedPane.getBoundsAt(index));
//             }
//         }
//         prevIndex = index;
//     }
//     private int prevIndex;
}

// class CloseableTabbedPaneGlassPane extends JPanel {
//     private final Point pt = new Point(-100, -100);
//     private final JButton button = new JButton("x") {
//         @Override public Dimension getPreferredSize() {
//             return new Dimension(16, 16);
//         }
//     };
//     private final JTabbedPane tabbedPane;
//     private final Rectangle buttonRect = new Rectangle(button.getPreferredSize());
//
//     public CloseableTabbedPaneGlassPane(JTabbedPane tabbedPane) {
//         super();
//         this.tabbedPane = tabbedPane;
//         MouseAdapter h = new Handler();
//         tabbedPane.addMouseListener(h);
//         tabbedPane.addMouseMotionListener(h);
//         button.setBorder(BorderFactory.createEmptyBorder());
//         button.setFocusPainted(false);
//         button.setBorderPainted(false);
//         button.setContentAreaFilled(false);
//         button.setRolloverEnabled(false);
//     }
//     @Override public void paintComponent(Graphics g) {
//         Point glassPt = SwingUtilities.convertPoint(tabbedPane, 0, 0, this);
//         for (int i = 0; i < tabbedPane.getTabCount(); i++) {
//             Rectangle tabRect = tabbedPane.getBoundsAt(i);
//             int x = tabRect.x + tabRect.width - buttonRect.width - 2;
//             int y = tabRect.y + (tabRect.height - buttonRect.height) / 2;
//             buttonRect.setLocation(x, y);
//             button.setForeground(buttonRect.contains(pt) ? Color.RED : Color.BLACK);
//             buttonRect.translate(glassPt.x, glassPt.y);
//             SwingUtilities.paintComponent(g, button, this, buttonRect);
//         }
//     }
//     class Handler extends MouseAdapter {
//         @Override public void mouseClicked(MouseEvent e) {
//             pt.setLocation(e.getPoint());
//             int index = tabbedPane.indexAtLocation(pt.x, pt.y);
//             if (index >= 0) {
//                 Rectangle tabRect = tabbedPane.getBoundsAt(index);
//                 int x = tabRect.x + tabRect.width - buttonRect.width - 2;
//                 int y = tabRect.y + (tabRect.height - buttonRect.height) / 2;
//                 buttonRect.setLocation(x, y);
//                 if (buttonRect.contains(pt)) {
//                     tabbedPane.removeTabAt(index);
//                 }
//             }
//             tabbedPane.repaint();
//         }
//         @Override public void mouseMoved(MouseEvent e) {
//             pt.setLocation(e.getPoint());
//             int index = tabbedPane.indexAtLocation(pt.x, pt.y);
//             if (index >= 0) {
//                 tabbedPane.repaint(tabbedPane.getBoundsAt(index));
//             } else {
//                 tabbedPane.repaint();
//             }
//         }
//     }
// }
