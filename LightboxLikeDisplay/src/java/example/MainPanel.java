package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(1, 2));
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                getRootPane().setGlassPane(new LightboxGlassPane());
                getRootPane().getGlassPane().setVisible(false);
            }
        });
        JButton button = new JButton(new AbstractAction("Open") {
            @Override public void actionPerformed(ActionEvent e) {
                getRootPane().getGlassPane().setVisible(true);
            }
        });
        add(makeDummyPanel());
        add(button);
        setPreferredSize(new Dimension(320, 240));
    }
    private JPanel makeDummyPanel() {
        JButton b = new JButton("Button & Mnemonic");
        b.setMnemonic(KeyEvent.VK_B);
        JTextField t = new JTextField("TextField & ToolTip");
        t.setToolTipText("ToolTip");
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        p.add(b, BorderLayout.NORTH);
        p.add(t, BorderLayout.SOUTH);
        p.add(new JScrollPane(new JTree()));
        return p;
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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class LightboxGlassPane extends JPanel {
    private static final int BW = 5;
    private final ImageIcon image = new ImageIcon(LightboxGlassPane.class.getResource("test.png"));
    private final transient AnimeIcon animatedIcon = new AnimeIcon();
    private float alpha;
    private int w;
    private int h;
    private final Rectangle rect = new Rectangle();
    private Timer animator;
    private transient Handler handler;

    @Override public void updateUI() {
        removeMouseListener(handler);
        removeHierarchyListener(handler);
        super.updateUI();
        setOpaque(false);
        super.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        handler = new Handler();
        addMouseListener(handler);
        addHierarchyListener(handler);
    }
    private class Handler extends MouseAdapter implements HierarchyListener {
        @Override public void mouseClicked(MouseEvent e) {
            e.getComponent().setVisible(false);
        }
        @Override public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable() && Objects.nonNull(animator)) {
                animator.stop();
            }
        }
    }
    @Override public void setVisible(boolean isVisible) {
        boolean oldVisible = isVisible();
        super.setVisible(isVisible);
        JRootPane rootPane = getRootPane();
        if (Objects.nonNull(rootPane) && isVisible() != oldVisible) {
            rootPane.getLayeredPane().setVisible(!isVisible);
        }
        boolean b = Objects.isNull(animator) || !animator.isRunning();
        if (isVisible && b) {
            w = 40;
            h = 40;
            alpha = 0f;
            animator = new Timer(10, new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    animatedIcon.next();
                    repaint();
                }
            });
            animator.start();
        } else {
            if (Objects.nonNull(animator)) {
                animator.stop();
            }
        }
        animatedIcon.setRunning(isVisible);
    }
    @Override protected void paintComponent(Graphics g) {
        JRootPane rootPane = getRootPane();
        if (Objects.nonNull(rootPane)) {
            rootPane.getLayeredPane().print(g);
        }
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        if (h < image.getIconHeight() + BW + BW) {
            h += image.getIconHeight() / 16;
        } else if (w < image.getIconWidth() + BW + BW) {
            h  = image.getIconHeight() + BW + BW;
            w += image.getIconWidth() / 16;
        } else if (alpha < 1f) {
            w  = image.getIconWidth() + BW + BW;
            alpha = alpha + .1f;
        } else {
            animatedIcon.setRunning(false);
            animator.stop();
        }
        rect.setSize(w, h);
        Rectangle screen = getBounds();
        Point centerPt = new Point(screen.x + screen.width / 2, screen.y + screen.height / 2);
        rect.setLocation(centerPt.x - rect.width / 2, centerPt.y - rect.height / 2);

        g2.setPaint(new Color(0x64646464, true));
        g2.fill(screen);
        g2.setPaint(new Color(0xC8FFFFFF, true));
        g2.fill(rect);

        if (alpha > 0) {
            if (alpha > 1f) {
                alpha = 1f;
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.drawImage(image.getImage(), rect.x + BW, rect.y + BW, image.getIconWidth(), image.getIconHeight(), this);
        } else {
            animatedIcon.paintIcon(this, g2, centerPt.x - animatedIcon.getIconWidth() / 2, centerPt.y - animatedIcon.getIconHeight() / 2);
        }
        g2.dispose();
    }
}

class AnimeIcon implements Icon {
    private static final Color ELLIPSE_COLOR = new Color(.5f, .5f, .5f);
    private static final double R  = 2d;
    private static final double SX = 0d;
    private static final double SY = 0d;
    private static final int WIDTH  = (int) (R * 8 + SX * 2);
    private static final int HEIGHT = (int) (R * 8 + SY * 2);
    private final List<Shape> list = new ArrayList<Shape>(Arrays.asList(
        new Ellipse2D.Double(SX + 3 * R, SY + 0 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 5 * R, SY + 1 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 6 * R, SY + 3 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 5 * R, SY + 5 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 3 * R, SY + 6 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 1 * R, SY + 5 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 0 * R, SY + 3 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 1 * R, SY + 1 * R, 2 * R, 2 * R)));

    private boolean isRunning;
    public void next() {
        if (isRunning) {
            list.add(list.remove(0));
        }
    }
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new Color(0x0, true));
        g2.fillRect(x, y, getIconWidth(), getIconHeight());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(ELLIPSE_COLOR);
        g2.translate(x, y);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            float alpha = isRunning ? (i + 1) / (float) size : .5f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.fill(list.get(i));
        }
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return WIDTH;
    }
    @Override public int getIconHeight() {
        return HEIGHT;
    }
}
