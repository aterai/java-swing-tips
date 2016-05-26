package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        final SlideInNotification handler = new SlideInNotification();

//         optionPane.addPropertyChangeListener(e -> {
//             if (dialog.isVisible() && e.getSource() == optionPane && //(event.getPropertyName().equals(VALUE_PROPERTY)) &&
//                 Objects.nonNull(e.getNewValue()) && e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
//                 dialog.setVisible(false);
//             }
//         });
//         dialog.getContentPane().add(optionPane);
//         dialog.pack();

        JPanel p = new JPanel();
        p.add(new JButton(new AbstractAction("easeIn") {
            @Override public void actionPerformed(ActionEvent e) {
                handler.startSlideIn(SlideInAnimation.EASE_IN);
            }
        }));
        p.add(new JButton(new AbstractAction("easeOut") {
            @Override public void actionPerformed(ActionEvent e) {
                handler.startSlideIn(SlideInAnimation.EASE_OUT);
            }
        }));
        p.add(new JButton(new AbstractAction("easeInOut") {
            @Override public void actionPerformed(ActionEvent e) {
                handler.startSlideIn(SlideInAnimation.EASE_IN_OUT);
            }
        }));
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea()));
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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class SlideInNotification implements PropertyChangeListener, HierarchyListener {
    private static final int DELAY = 5;
    private final JWindow dialog = new JWindow((Frame) null);
    private final Timer animator = new Timer(DELAY, null);
    private transient ActionListener listener;
    private int dx;
    private int dy;

    public void startSlideIn(final SlideInAnimation slideInAnimation) {
        if (animator.isRunning()) {
            return;
        }
        if (dialog.isVisible()) {
            dialog.setVisible(false);
            dialog.getContentPane().removeAll();
        }

        JOptionPane optionPane = new JOptionPane("Warning", JOptionPane.WARNING_MESSAGE);
        DragWindowListener dwl = new DragWindowListener();
        optionPane.addMouseListener(dwl);
        optionPane.addMouseMotionListener(dwl);
        optionPane.addPropertyChangeListener(this);
        optionPane.addHierarchyListener(this);
        dialog.getContentPane().add(optionPane);
        dialog.pack();

        final Dimension d = dialog.getContentPane().getPreferredSize();
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle desktopBounds = env.getMaximumWindowBounds();
        dx = desktopBounds.width - d.width;
        dy = desktopBounds.height;
        dialog.setLocation(new Point(dx, dy));
        dialog.setVisible(true);

        animator.removeActionListener(listener);
        listener = new ActionListener() {
            private int count;
            @Override public void actionPerformed(ActionEvent e) {
                double a = 1d;
                switch (slideInAnimation) {
                  case EASE_IN:
                    a = AnimationUtil.easeIn(count++ / (double) d.height);
                    break;
                  case EASE_OUT:
                    a = AnimationUtil.easeOut(count++ / (double) d.height);
                    break;
                  case EASE_IN_OUT:
                  default:
                    a = AnimationUtil.easeInOut(count++ / (double) d.height);
                    break;
                }
                int visibleHeight = (int) (.5 + a * d.height);
                if (visibleHeight >= d.height) {
                    visibleHeight = d.height;
                    animator.stop();
                }
                dialog.setLocation(new Point(dx, dy - visibleHeight));
            }
        };
        animator.addActionListener(listener);
        animator.start();
    }
    @Override public void propertyChange(PropertyChangeEvent e) {
        if (dialog.isVisible() && Objects.nonNull(e.getNewValue()) && e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
            dialog.setVisible(false);
            dialog.getContentPane().removeAll();
        }
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable()) {
            animator.stop();
        }
    }
}

class DragWindowListener extends MouseAdapter {
    private final Point startPt = new Point();
    @Override public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            startPt.setLocation(e.getPoint());
        }
    }
    @Override public void mouseDragged(MouseEvent e) {
        Component c = SwingUtilities.getRoot(e.getComponent());
        if (c instanceof Window && SwingUtilities.isLeftMouseButton(e)) {
            Window window = (Window) c;
            Point pt = window.getLocation();
            window.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
        }
    }
}

enum SlideInAnimation {
    EASE_IN, EASE_OUT, EASE_IN_OUT;
}

final class AnimationUtil {
    private static final int N = 3;
    private AnimationUtil() { /* Singleton */ }
    //http://www.anima-entertainment.de/math-easein-easeout-easeinout-and-bezier-curves
    //Math: EaseIn EaseOut, EaseInOut and Bezier Curves | Anima Entertainment GmbH
    public static double easeIn(double t) {
        //range: 0.0 <= t <= 1.0
        return Math.pow(t, N);
    }
    public static double easeOut(double t) {
        return Math.pow(t - 1d, N) + 1d;
    }
    public static double easeInOut(double t) {
/*/
        if (t < .5) {
            return .5 * Math.pow(t * 2d, N);
        } else {
            return .5 *(Math.pow(t * 2d - 2d, N) + 2d);
        }
    }
/*/
        double ret;
        if (t < .5) {
            ret = .5 * intpow(t * 2d, N);
        } else {
            ret = .5 * (intpow(t * 2d - 2d, N) + 2d);
        }
        return ret;
    }
    //http://d.hatena.ne.jp/pcl/20120617/p1
    //http://d.hatena.ne.jp/rexpit/20110328/1301305266
    //http://c2.com/cgi/wiki?IntegerPowerAlgorithm
    //http://www.osix.net/modules/article/?id=696
    public static double intpow(double da, int ib) {
        int b = ib;
        if (b < 0) {
            //return d / intpow(a, -b);
            throw new IllegalArgumentException("B must be a positive integer or zero");
        }
        double a = da;
        double d = 1d;
        for (; b > 0; a *= a, b >>>= 1) {
            if ((b & 1) != 0) {
                d *= a;
            }
        }
        return d;
    }
//*/
//     public static double delta(double t) {
//         return 1d - Math.sin(Math.acos(t));
//     }
}
