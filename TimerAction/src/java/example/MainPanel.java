package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final Random rnd = new Random();
    private final JTabbedPane tabs = new JTabbedPane();
    public MainPanel() {
        super(new BorderLayout());
        JPanel c1 = new JPanel(new GridLayout(10, 10));
        final JPanel c2 = new JPanel(new GridLayout(10, 10));
        final Timer timer = new Timer(16, null);
        for (int i = 0; i < 100; i++) {
            c1.add(new Tile1(rnd));
            c2.add(new Tile2(rnd, timer));
        }
        c2.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (e.getComponent().isShowing()) {
                    timer.start();
                } else {
                    timer.stop();
                }
            }
        });
        tabs.addTab("Timer: 100", c1);
        tabs.addTab("Timer: 1, ActionListener: 100", c2);
        tabs.addTab("Timer: 1, ActionListener: 1", new TilePanel(rnd));
        add(tabs);
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

class Tile1 extends JComponent implements HierarchyListener {
    //java - javax.swing.Timer slowdown in Java7u40 - Stack Overflow
    //http://stackoverflow.com/questions/18933986/javax-swing-timer-slowdown-in-java7u40
    private int red;
    private final Timer timer;
    protected Tile1(final Random rnd) {
        super();
        addHierarchyListener(this);
        timer = new Timer(16, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                red = rnd.nextInt(255);
                repaint();
            }
        });
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
            if (e.getComponent().isShowing()) {
                timer.start();
            } else {
                timer.stop();
            }
        }
    }
    @Override public Dimension getPreferredSize() {
        return new Dimension(10, 10);
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isVisible()) {
            if (!timer.isRunning()) {
                timer.start();
            }
        } else {
            timer.stop();
        }
        g.setColor(new Color(red, 255 - red, 0));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}

class Tile2 extends JComponent {
    private int red;
    protected Tile2(final Random rnd, Timer timer) {
        super();
        timer.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                red = rnd.nextInt(255);
                repaint();
            }
        });
    }
    @Override public Dimension getPreferredSize() {
        return new Dimension(10, 10);
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(red, 255 - red, 0));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}

class TilePanel extends JPanel {
    protected TilePanel(final Random rnd) {
        super(new GridLayout(10, 10));
        for (int i = 0; i < 100; i++) {
            JLabel l = new JLabel();
            l.setOpaque(true);
            add(l);
        }
        final Timer timer = new Timer(16, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < 100; i++) {
                    JComponent l = (JComponent) getComponent(i);
                    int red = rnd.nextInt(256);
                    l.setBackground(new Color(red, 255 - red, 0));
                }
            }
        });
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (e.getComponent().isShowing()) {
                    timer.start();
                } else {
                    timer.stop();
                }
            }
        });
    }
}
