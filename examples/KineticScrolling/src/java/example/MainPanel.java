// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
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

    JViewport viewport = new ViewPositionViewport();
    scroll.setViewport(viewport);
    // JViewport viewport = scroll.getViewport(); // JDK 1.6.0

    String path = "example/GIANT_TCR1_2013.jpg";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return new MissingIcon();
      }
    }).orElseGet(MissingIcon::new);

    JLabel label = new JLabel(icon);
    viewport.add(label);
    KineticScrollingListener1 l1 = new KineticScrollingListener1(label);
    KineticScrollingListener2 l2 = new KineticScrollingListener2(label);

    JRadioButton r1 = new JRadioButton("scrollRectToVisible", true);
    r1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        viewport.removeMouseListener(l2);
        viewport.removeMouseMotionListener(l2);
        viewport.removeHierarchyListener(l2);
        viewport.addMouseMotionListener(l1);
        viewport.addMouseListener(l1);
        viewport.addHierarchyListener(l1);
      }
    });

    JRadioButton r2 = new JRadioButton("setViewPosition");
    r2.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        viewport.removeMouseListener(l1);
        viewport.removeMouseMotionListener(l1);
        viewport.removeHierarchyListener(l1);
        viewport.addMouseMotionListener(l2);
        viewport.addMouseListener(l2);
        viewport.addHierarchyListener(l2);
      }
    });

    Box box = Box.createHorizontalBox();
    ButtonGroup bg = new ButtonGroup();
    Stream.of(r1, r2).forEach(r -> {
      box.add(r);
      bg.add(r);
    });

    viewport.addMouseMotionListener(l1);
    viewport.addMouseListener(l1);
    viewport.addHierarchyListener(l1);

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

class ViewPositionViewport extends JViewport {
  private static final boolean MIDDLEWEIGHT = false;
  private final AtomicBoolean adjusting = new AtomicBoolean();

  @Override public void revalidate() {
    if (!MIDDLEWEIGHT && adjusting != null && adjusting.get()) {
      return;
    }
    super.revalidate();
  }

  @Override public void setViewPosition(Point p) {
    if (MIDDLEWEIGHT) {
      super.setViewPosition(p);
    } else {
      adjusting.set(true);
      super.setViewPosition(p);
      adjusting.set(false);
    }
  }
}

class KineticScrollingListener1 extends MouseAdapter implements HierarchyListener {
  protected static final int SPEED = 4;
  protected static final int DELAY = 10;
  protected static final double D = .8;
  protected final Cursor dc;
  protected final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  protected final Timer scroller;
  protected final JComponent label;
  protected final Point startPt = new Point();
  protected final Point delta = new Point();

  protected KineticScrollingListener1(JComponent comp) {
    super();
    this.label = comp;
    this.dc = comp.getCursor();
    this.scroller = new Timer(DELAY, this::scroll);
  }

  private void scroll(ActionEvent e) {
    JViewport viewport = (JViewport) SwingUtilities.getUnwrappedParent(label);
    Point vp = viewport.getViewPosition();
    vp.translate(-delta.x, -delta.y);
    label.scrollRectToVisible(new Rectangle(vp, viewport.getSize()));
    if (Math.abs(delta.x) > 0 || Math.abs(delta.y) > 0) {
      delta.setLocation((int) (delta.x * D), (int) (delta.y * D));
    } else {
      ((Timer) e.getSource()).stop();
    }
  }

  @Override public void mousePressed(MouseEvent e) {
    e.getComponent().setCursor(hc);
    startPt.setLocation(e.getPoint());
    scroller.stop();
  }

  @Override public void mouseDragged(MouseEvent e) {
    Point pt = e.getPoint();
    JViewport viewport = (JViewport) e.getComponent(); // label.getParent();
    Point vp = viewport.getViewPosition(); // SwingUtilities.convertPoint(viewport, 0, 0, label);
    vp.translate(startPt.x - pt.x, startPt.y - pt.y);
    delta.setLocation(SPEED * (pt.x - startPt.x), SPEED * (pt.y - startPt.y));
    label.scrollRectToVisible(new Rectangle(vp, viewport.getSize()));
    startPt.setLocation(pt);
  }

  @Override public void mouseReleased(MouseEvent e) {
    e.getComponent().setCursor(dc);
    scroller.start();
  }

  @Override public void hierarchyChanged(HierarchyEvent e) {
    boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
    if (b && !e.getComponent().isDisplayable()) {
      scroller.stop();
    }
  }
}

class KineticScrollingListener2 extends MouseAdapter implements HierarchyListener {
  protected static final int SPEED = 4;
  protected static final int DELAY = 10;
  protected static final double D = .8;
  protected final JComponent label;
  protected final Point startPt = new Point();
  protected final Point delta = new Point();
  protected final Cursor dc;
  protected final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  protected final Timer inside = new Timer(DELAY, null);
  protected final Timer outside = new Timer(DELAY, null);

  protected KineticScrollingListener2(JComponent comp) {
    super();
    this.label = comp;
    this.dc = comp.getCursor();
    inside.addActionListener(e -> dragInside());
    outside.addActionListener(e -> dragOutside());
  }

  protected static boolean isInside(JViewport viewport, JComponent comp) {
    Point vp = viewport.getViewPosition();
    return vp.x >= 0 && vp.x + viewport.getWidth() - comp.getWidth() <= 0
        && vp.y >= 0 && vp.y + viewport.getHeight() - comp.getHeight() <= 0;
  }

  private void dragInside() {
    JViewport viewport = (JViewport) SwingUtilities.getUnwrappedParent(label);
    Point vp = viewport.getViewPosition();
    // System.out.format("s: %s, %s%n", delta, vp);
    vp.translate(-delta.x, -delta.y);
    viewport.setViewPosition(vp);
    if (Math.abs(delta.x) > 0 || Math.abs(delta.y) > 0) {
      delta.setLocation((int) (delta.x * D), (int) (delta.y * D));
      // Outside
      if (vp.x < 0 || vp.x + viewport.getWidth() - label.getWidth() > 0) {
        delta.x = (int) (delta.x * D);
      }
      if (vp.y < 0 || vp.y + viewport.getHeight() - label.getHeight() > 0) {
        delta.y = (int) (delta.y * D);
      }
    } else {
      inside.stop();
      if (!isInside(viewport, label)) {
        outside.start();
      }
    }
  }

  private void dragOutside() {
    JViewport viewport = (JViewport) SwingUtilities.getUnwrappedParent(label);
    Point vp = viewport.getViewPosition();
    // System.out.format("r: %s%n", vp);
    if (vp.x < 0) {
      vp.x = (int) (vp.x * D);
    }
    if (vp.y < 0) {
      vp.y = (int) (vp.y * D);
    }
    if (vp.x + viewport.getWidth() - label.getWidth() > 0) {
      vp.x = (int) (vp.x - (vp.x + viewport.getWidth() - label.getWidth()) * (1d - D));
    }
    if (vp.y + viewport.getHeight() > label.getHeight()) {
      vp.y = (int) (vp.y - (vp.y + viewport.getHeight() - label.getHeight()) * (1d - D));
    }
    viewport.setViewPosition(vp);
    if (isInside(viewport, label)) {
      outside.stop();
    }
  }

  @Override public void mousePressed(MouseEvent e) {
    e.getComponent().setCursor(hc);
    startPt.setLocation(e.getPoint());
    inside.stop();
    outside.stop();
  }

  @Override public void mouseDragged(MouseEvent e) {
    Point pt = e.getPoint();
    JViewport viewport = (JViewport) SwingUtilities.getUnwrappedParent(label);
    Point vp = viewport.getViewPosition();
    vp.translate(startPt.x - pt.x, startPt.y - pt.y);
    viewport.setViewPosition(vp);
    delta.setLocation(SPEED * (pt.x - startPt.x), SPEED * (pt.y - startPt.y));
    startPt.setLocation(pt);
  }

  @Override public void mouseReleased(MouseEvent e) {
    e.getComponent().setCursor(dc);
    JViewport viewport = (JViewport) SwingUtilities.getUnwrappedParent(label);
    if (isInside(viewport, label)) {
      inside.start();
    } else {
      outside.start();
    }
  }

  @Override public void hierarchyChanged(HierarchyEvent e) {
    boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
    if (b && !e.getComponent().isDisplayable()) {
      inside.stop();
      outside.stop();
    }
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
