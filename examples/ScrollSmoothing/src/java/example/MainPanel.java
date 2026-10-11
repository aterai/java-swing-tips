// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0));
    JScrollPane scroll0 = new JScrollPane(new JList<>(createModel()));
    scroll0.setBorder(BorderFactory.createTitledBorder("Default"));
    add(scroll0);

    JScrollPane scroll1 = new ScrollSmoothingPane(new JList<>(createModel()));
    scroll1.setBorder(BorderFactory.createTitledBorder("ScrollSmoothingPane"));
    add(scroll1);

    JScrollPane scroll2 = new PaintOffsetScrollPane(new JList<>(createModel()));
    scroll2.setBorder(BorderFactory.createTitledBorder("PaintOffsetScrollPane"));
    add(scroll2);
    setPreferredSize(new Dimension(320, 240));
  }

  private static ListModel<String> createModel() {
    DefaultListModel<String> model = new DefaultListModel<>();
    IntStream.range(0, 5000).mapToObj(Objects::toString).forEach(model::addElement);
    return model;
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
      Logger.getGlobal().severe(ex::getMessage);
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ScrollSmoothingPane extends JScrollPane {
  protected ScrollSmoothingPane(Component view) {
    super(view);
  }

  @Override protected JViewport createViewport() {
    return new SmoothViewport();
  }
}

// Approach 1: delay the view position itself. Because JScrollPane keeps the
// scrollbar and the viewport in sync, every intermediate position painted here
// comes back as a new request and must be filtered out.
class SmoothViewport extends JViewport {
  private static final int DURATION_MS = 200;
  private static final int FRAME_INTERVAL_MS = 1000 / 60;
  // A position that JScrollPane never requests, so the first scroll is not
  // mistaken for a repeat of the current target.
  private static final int UNSET_XY = -100;
  private final Timer animator = new Timer(FRAME_INTERVAL_MS, null);
  private final Point from = new Point(UNSET_XY, UNSET_XY);
  private final Point target = new Point(UNSET_XY, UNSET_XY);
  private long startTime;
  private boolean adjusting;

  protected SmoothViewport() {
    super();
    animator.addActionListener(e -> animate());
  }

  @Override public void setViewPosition(Point p) {
    // Ignore the echo of an intermediate position this viewport itself has just
    // painted: JScrollPane synchronizes the scrollbar with the viewport, and the
    // scrollbar then requests that same intermediate position back, which would
    // otherwise overwrite the animation target.
    Point viewPosition = super.getViewPosition();
    boolean echo = adjusting || animator.isRunning() && viewPosition.equals(p);
    if (!echo && !target.equals(p)) {
      from.setLocation(viewPosition);
      target.setLocation(p);
      startTime = System.currentTimeMillis();
      animator.start(); // Timer#start() is ignored while the Timer is running
    }
  }

  private void animate() {
    long time = System.currentTimeMillis() - startTime;
    double t = Math.min(Easing.COMPLETE, time / (double) DURATION_MS);
    double e = Easing.easeOutCubic(t);
    int x = (int) Math.round(from.x + (target.x - from.x) * e);
    int y = (int) Math.round(from.y + (target.y - from.y) * e);
    adjusting = true;
    try {
      super.setViewPosition(new Point(x, y));
    } finally {
      adjusting = false;
    }
    if (t >= Easing.COMPLETE) {
      animator.stop();
    }
  }
}

class PaintOffsetScrollPane extends JScrollPane {
  protected PaintOffsetScrollPane(Component view) {
    super(view);
  }

  @Override protected JViewport createViewport() {
    return new PaintOffsetViewport();
  }
}

// Approach 2: keep the view position always up to date and delay only the
// painting. A scroll request is immediately applied to the logical position and
// canceled out by an equal Graphics#translate(...) offset, which then decays to
// zero. Swing never sees a stale position, so no feedback filtering is needed.
class PaintOffsetViewport extends JViewport {
  private static final int DURATION_MS = 200;
  private static final int FRAME_INTERVAL_MS = 1000 / 60;
  private final Timer animator = new Timer(FRAME_INTERVAL_MS, null);
  private double offsetX;
  private double offsetY;
  private double startOffsetX;
  private double startOffsetY;
  private long startTime;

  protected PaintOffsetViewport() {
    super();
    animator.addActionListener(e -> animate());
  }

  @Override public int getScrollMode() {
    // BLIT_SCROLL_MODE and BACKINGSTORE_SCROLL_MODE copy pixels on the
    // assumption that the view is painted without any offset.
    return SIMPLE_SCROLL_MODE;
  }

  @Override protected boolean isPaintingOrigin() {
    // While the offset is decaying, a repaint requested by the view itself
    // (e.g. a selection change in the JList) must also go through paint(...)
    // below, otherwise the view is painted directly without the offset.
    return animator.isRunning() || super.isPaintingOrigin();
  }

  @Override public void setViewPosition(Point p) {
    Point prev = getViewPosition();
    super.setViewPosition(p);
    Point next = getViewPosition();
    int dx = next.x - prev.x;
    int dy = next.y - prev.y;
    if (dx != 0 || dy != 0) {
      // Cancel out the jump that has just been applied to the logical position.
      offsetX += dx;
      offsetY += dy;
      startOffsetX = offsetX;
      startOffsetY = offsetY;
      startTime = System.currentTimeMillis();
      if (!animator.isRunning()) {
        animator.start();
      }
      repaint();
    }
  }

  private void animate() {
    long time = System.currentTimeMillis() - startTime;
    double t = Math.min(Easing.COMPLETE, time / (double) DURATION_MS);
    double e = Easing.COMPLETE - Easing.easeOutCubic(t);
    offsetX = startOffsetX * e;
    offsetY = startOffsetY * e;
    if (t >= Easing.COMPLETE) {
      offsetX = 0d;
      offsetY = 0d;
      animator.stop();
    }
    repaint();
  }

  @Override public void paint(Graphics g) {
    int ox = (int) Math.round(offsetX);
    int oy = (int) Math.round(offsetY);
    if (ox == 0 && oy == 0) {
      super.paint(g);
    } else {
      // Both ends of the view are shifted away from the viewport bounds, so the
      // background must be filled before translating.
      Component view = getView();
      g.setColor(view == null ? getBackground() : view.getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
      Graphics g2 = g.create();
      g2.translate(ox, oy);
      super.paint(g2);
      g2.dispose();
    }
  }
}

final class Easing {
  public static final double COMPLETE = 1d;

  private Easing() {
    /* HideUtilityClassConstructor */
  }

  public static double easeOutCubic(double t) {
    double u = COMPLETE - t;
    return COMPLETE - u * u * u;
  }
}
