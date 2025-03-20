// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    SlideInNotification handler = new SlideInNotification();

    // optionPane.addPropertyChangeListener(e -> {
    //   if (dialog.isVisible() && e.getSource() == optionPane
    //       // && e.getPropertyName().equals(VALUE_PROPERTY)
    //       && Objects.nonNull(e.getNewValue())
    //       && e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
    //     dialog.setVisible(false);
    //   }
    // });
    // dialog.getContentPane().add(optionPane);
    // dialog.pack();

    JButton easeIn = new JButton("easeIn");
    easeIn.addActionListener(e -> handler.startSlideIn(SlideInAnimation.EASE_IN));

    JButton easeOut = new JButton("easeOut");
    easeOut.addActionListener(e -> handler.startSlideIn(SlideInAnimation.EASE_OUT));

    JButton easeInOut = new JButton("easeInOut");
    easeInOut.addActionListener(e -> handler.startSlideIn(SlideInAnimation.EASE_IN_OUT));

    JPanel p = new JPanel();
    p.add(easeIn);
    p.add(easeOut);
    p.add(easeInOut);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class SlideInNotification implements PropertyChangeListener, HierarchyListener {
  public static final int DELAY = 5;
  public static final int STEP = 3;
  private final JWindow dialog = new JWindow((Frame) null);
  private final Timer animator = new Timer(DELAY, null);
  private ActionListener listener;

  public void startSlideIn(SlideInAnimation slideInAnimation) {
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

    Dimension d = dialog.getContentPane().getPreferredSize();
    GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    Rectangle desktopBounds = env.getMaximumWindowBounds();
    int dx = desktopBounds.width - d.width;
    int dy = desktopBounds.height;
    dialog.setLocation(new Point(dx, dy));
    dialog.setVisible(true);

    animator.removeActionListener(listener);
    AtomicInteger count = new AtomicInteger();
    listener = e -> {
      double v = count.addAndGet(STEP) / (double) d.height;
      double a;
      if (slideInAnimation == SlideInAnimation.EASE_IN) {
        a = AnimationUtils.easeIn(v);
      } else if (slideInAnimation == SlideInAnimation.EASE_OUT) {
        a = AnimationUtils.easeOut(v);
      } else { // EASE_IN_OUT
        a = AnimationUtils.easeInOut(v);
      }
      int visibleHeight = (int) (.5 + a * d.height);
      if (visibleHeight >= d.height) {
        visibleHeight = d.height;
        animator.stop();
      }
      dialog.setLocation(new Point(dx, dy - visibleHeight));
    };
    animator.addActionListener(listener);
    animator.start();
  }

  @Override public void propertyChange(PropertyChangeEvent e) {
    if (dialog.isVisible() && !JOptionPane.UNINITIALIZED_VALUE.equals(e.getNewValue())) {
      dialog.setVisible(false);
      dialog.getContentPane().removeAll();
    }
  }

  @Override public void hierarchyChanged(HierarchyEvent e) {
    boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
    if (b && !e.getComponent().isDisplayable()) {
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
      Point pt = c.getLocation();
      c.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
    }
  }
}

enum SlideInAnimation {
  EASE_IN, EASE_OUT, EASE_IN_OUT
}

final class AnimationUtils {
  private static final int N = 3;

  private AnimationUtils() {
    /* Singleton */
  }

  // http://www.anima-entertainment.de/math-easein-easeout-easeinout-and-bezier-curves
  // Math: EaseIn EaseOut, EaseInOut and Bezier Curves | Anima Entertainment GmbH
  public static double easeIn(double t) {
    // range: 0.0 <= t <= 1.0
    return Math.pow(t, N);
  }

  public static double easeOut(double t) {
    return Math.pow(t - 1d, N) + 1d;
  }

  public static double easeInOut(double t) {
    boolean isFirstHalf = t < .5;
    return isFirstHalf ? .5 * intPow(t * 2d, N) : .5 * (intPow(t * 2d - 2d, N) + 2d);
  }

  // https://wiki.c2.com/?IntegerPowerAlgorithm
  public static double intPow(double da, int ib) {
    int b = ib;
    if (b < 0) {
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
}
