package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel(JFrame frame) {
        super(new BorderLayout());
        final SlideInNotification handler = new SlideInNotification(frame);

//         optionPane.addPropertyChangeListener(new PropertyChangeListener() {
//             @Override public void propertyChange(PropertyChangeEvent e) {
//                 if (dialog.isVisible() && e.getSource() == optionPane && //(event.getPropertyName().equals(VALUE_PROPERTY)) &&
//                     e.getNewValue() != null && e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
//                     dialog.setVisible(false);
//                 }
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
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class SlideInNotification implements PropertyChangeListener, HierarchyListener {
    private static final int DELAY = 5;
    private final JFrame frame;
    private JWindow dialog;
    private Timer animator;
    private int dx;
    private int dy;

    public SlideInNotification(JFrame frame) {
        super();
        this.frame = frame;
    }
    public void startSlideIn(final SlideInAnimation slideInAnimation) {
        if (animator != null && animator.isRunning()) {
            return;
        }
        if (dialog != null && dialog.isVisible()) {
            dialog.dispose();
        }
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle desktopBounds = env.getMaximumWindowBounds();

        JOptionPane optionPane = new JOptionPane("Warning", JOptionPane.WARNING_MESSAGE);
        DragWindowListener dwl = new DragWindowListener();
        optionPane.addMouseListener(dwl);
        optionPane.addMouseMotionListener(dwl);
        optionPane.addPropertyChangeListener(this);
        optionPane.addHierarchyListener(this);

        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        dialog = new JWindow(gc);
        dialog.getContentPane().add(optionPane);
        dialog.pack();

        final Dimension d = dialog.getContentPane().getPreferredSize();
        dx = desktopBounds.width - d.width;
        dy = desktopBounds.height;

        dialog.setLocation(new Point(dx, dy));
        dialog.setVisible(true);

        animator = new Timer(DELAY, new ActionListener() {
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
                int visibleHeidht = (int) (.5 + a * d.height);
                if (visibleHeidht >= d.height) {
                    visibleHeidht = d.height;
                    animator.stop();
                }
                dialog.setLocation(new Point(dx, dy - visibleHeidht));
            }
        });
        animator.start();
    }
    @Override public void propertyChange(PropertyChangeEvent e) {
        if (dialog != null && dialog.isVisible() && e.getNewValue() != null && e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
            dialog.dispose();
        }
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        JComponent c = (JComponent) e.getComponent();
        if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && animator != null && !c.isDisplayable()) {
            animator.stop();
        }
    }
}

class DragWindowListener extends MouseAdapter {
    private final transient Point startPt = new Point();
    private transient Window window;
    @Override public void mousePressed(MouseEvent me) {
        if (window == null) {
            Object o = me.getSource();
            if (o instanceof Window) {
                window = (Window) o;
            } else if (o instanceof JComponent) {
                window = SwingUtilities.windowForComponent(me.getComponent());
            }
        }
        startPt.setLocation(me.getPoint());
    }
    @Override public void mouseDragged(MouseEvent me) {
        if (window != null) {
            Point eventLocationOnScreen = me.getLocationOnScreen();
            window.setLocation(eventLocationOnScreen.x - startPt.x,
                               eventLocationOnScreen.y - startPt.y);
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
            return 0.5d * Math.pow(t * 2d, N);
        } else {
            return 0.5d*(Math.pow(t * 2d - 2d, N) + 2d);
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
        double d = 1.0;
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
