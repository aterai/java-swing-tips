// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JScrollPane scroll = new JScrollPane();
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JViewport viewport = new OverscrollViewport();
    scroll.setViewport(viewport);
    // JViewport viewport = scroll.getViewport(); // JDK 1.6.0

    String path = "example/GIANT_TCR1_2013.jpg";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(url -> {
      Icon icn;
      try (InputStream s = url.openStream()) {
        icn = new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        icn = new MissingIcon();
      }
      return icn;
    }).orElseGet(MissingIcon::new);

    JLabel label = new JLabel(icon);
    viewport.add(label);
    KineticScrollingListener l1 = new ScrollRectToVisibleListener(label);
    KineticScrollingListener l2 = new SetViewPositionListener(label);
    l1.install(viewport);

    JRadioButton r1 = new JRadioButton("scrollRectToVisible", true);
    r1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        l2.uninstall(viewport);
        l1.install(viewport);
      }
    });

    JRadioButton r2 = new JRadioButton("setViewPosition");
    r2.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        l1.uninstall(viewport);
        l2.install(viewport);
      }
    });

    Box box = Box.createHorizontalBox();
    ButtonGroup bg = new ButtonGroup();
    Stream.of(r1, r2).forEach(r -> {
      box.add(r);
      bg.add(r);
    });

    add(scroll);
    add(box, BorderLayout.NORTH);
    scroll.setPreferredSize(new Dimension(320, 240));
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    // frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// JViewport#setViewPosition(Point) calls revalidate() since JDK 1.7.0 (to keep
// heavyweight/lightweight mixing consistent), and ViewportLayout then clamps
// the view position back inside the view bounds. Skip that revalidate() while
// the position is being set so the view can be scrolled beyond its edges.
class OverscrollViewport extends JViewport {
  private static final boolean WEIGHT_MIXING = false;
  private boolean adjusting;

  @Override public void revalidate() {
    if (WEIGHT_MIXING || !adjusting) {
      super.revalidate();
    }
  }

  @Override public void setViewPosition(Point p) {
    adjusting = true;
    super.setViewPosition(p);
    adjusting = false;
  }
}

abstract class KineticScrollingListener extends MouseAdapter implements HierarchyListener {
  protected static final int SPEED = 4;
  protected static final int DELAY = 10;
  protected static final double DAMPING = .8;
  // Velocity of the view position in pixels per timer tick
  private final Point velocity = new Point();
  private final Cursor defaultCursor;
  private final Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  private final JComponent view;
  private final Point prevPt = new Point();

  protected KineticScrollingListener(JComponent view) {
    super();
    this.view = view;
    this.defaultCursor = view.getCursor();
  }

  public void install(JComponent c) {
    c.addMouseListener(this);
    c.addMouseMotionListener(this);
    c.addHierarchyListener(this);
  }

  public void uninstall(JComponent c) {
    c.removeMouseListener(this);
    c.removeMouseMotionListener(this);
    c.removeHierarchyListener(this);
  }

  protected JComponent getView() {
    return view;
  }

  protected JViewport getViewport() {
    return (JViewport) SwingUtilities.getUnwrappedParent(view);
  }

  protected Point getVelocity() {
    return velocity;
  }

  // Returns true when the velocity has decayed to zero
  protected boolean decelerate() {
    velocity.setLocation((int) (velocity.x * DAMPING), (int) (velocity.y * DAMPING));
    return velocity.x == 0 && velocity.y == 0;
  }

  protected abstract void drag(JViewport viewport, int dx, int dy);

  protected abstract void startScrolling(JViewport viewport);

  protected abstract void stopScrolling();

  @Override public void mousePressed(MouseEvent e) {
    e.getComponent().setCursor(handCursor);
    prevPt.setLocation(e.getPoint());
    velocity.setLocation(0, 0);
    stopScrolling();
  }

  @Override public void mouseDragged(MouseEvent e) {
    Point pt = e.getPoint();
    int dx = prevPt.x - pt.x;
    int dy = prevPt.y - pt.y;
    drag((JViewport) e.getComponent(), dx, dy);
    velocity.setLocation(SPEED * dx, SPEED * dy);
    prevPt.setLocation(pt);
  }

  @Override public void mouseReleased(MouseEvent e) {
    e.getComponent().setCursor(defaultCursor);
    startScrolling((JViewport) e.getComponent());
  }

  @Override public void hierarchyChanged(HierarchyEvent e) {
    boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
    if (b && !e.getComponent().isDisplayable()) {
      stopScrolling();
    }
  }
}

class ScrollRectToVisibleListener extends KineticScrollingListener {
  private final Timer scrollTimer = new Timer(DELAY, e -> scroll());

  protected ScrollRectToVisibleListener(JComponent view) {
    super(view);
  }

  private void scroll() {
    Point velocity = getVelocity();
    drag(getViewport(), velocity.x, velocity.y);
    if (decelerate()) {
      scrollTimer.stop();
    }
  }

  @Override protected void drag(JViewport viewport, int dx, int dy) {
    Rectangle rect = viewport.getViewRect();
    rect.translate(dx, dy);
    getView().scrollRectToVisible(rect);
  }

  @Override protected void startScrolling(JViewport viewport) {
    scrollTimer.start();
  }

  @Override protected void stopScrolling() {
    scrollTimer.stop();
  }
}

class SetViewPositionListener extends KineticScrollingListener {
  private final Timer scrollTimer = new Timer(DELAY, e -> scroll());
  private final Timer springBackTimer = new Timer(DELAY, e -> springBack());

  protected SetViewPositionListener(JComponent view) {
    super(view);
  }

  // Returns the view position nearest to vp that keeps the viewport
  // within the view bounds
  private Point getNearestInsidePosition(JViewport viewport, Point vp) {
    int maxX = Math.max(0, getView().getWidth() - viewport.getWidth());
    int maxY = Math.max(0, getView().getHeight() - viewport.getHeight());
    int x = Math.max(0, Math.min(vp.x, maxX));
    int y = Math.max(0, Math.min(vp.y, maxY));
    return new Point(x, y);
  }

  private boolean isInside(JViewport viewport) {
    Point vp = viewport.getViewPosition();
    return vp.equals(getNearestInsidePosition(viewport, vp));
  }

  private void scroll() {
    JViewport viewport = getViewport();
    Point velocity = getVelocity();
    drag(viewport, velocity.x, velocity.y);
    Point vp = viewport.getViewPosition();
    Point inside = getNearestInsidePosition(viewport, vp);
    // Decelerate faster while the viewport is outside the view bounds
    if (vp.x != inside.x) {
      velocity.x = (int) (velocity.x * DAMPING);
    }
    if (vp.y != inside.y) {
      velocity.y = (int) (velocity.y * DAMPING);
    }
    if (decelerate()) {
      scrollTimer.stop();
      if (!vp.equals(inside)) {
        springBackTimer.start();
      }
    }
  }

  private void springBack() {
    JViewport viewport = getViewport();
    Point vp = viewport.getViewPosition();
    Point inside = getNearestInsidePosition(viewport, vp);
    // Ease the view position back toward the nearest inside position;
    // the int cast truncates toward zero, so it always reaches the target
    vp.x = inside.x + (int) ((vp.x - inside.x) * DAMPING);
    vp.y = inside.y + (int) ((vp.y - inside.y) * DAMPING);
    viewport.setViewPosition(vp);
    if (vp.equals(inside)) {
      springBackTimer.stop();
    }
  }

  @Override protected void drag(JViewport viewport, int dx, int dy) {
    Point vp = viewport.getViewPosition();
    vp.translate(dx, dy);
    viewport.setViewPosition(vp);
  }

  @Override protected void startScrolling(JViewport viewport) {
    if (isInside(viewport)) {
      scrollTimer.start();
    } else {
      springBackTimer.start();
    }
  }

  @Override protected void stopScrolling() {
    scrollTimer.stop();
    springBackTimer.stop();
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;
    g2.setColor(Color.WHITE);
    g2.translate(x, y);
    g2.fillRect(0, 0, w, h);
    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(gap, gap, w - gap, h - gap);
    g2.drawLine(gap, h - gap, w - gap, gap);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 1024;
  }

  @Override public int getIconHeight() {
    return 1024;
  }
}
