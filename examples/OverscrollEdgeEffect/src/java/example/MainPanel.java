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
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JLabel label = new JLabel(new ImageIcon(makeMissingImage()));

    JScrollPane scroll = new JScrollPane(label);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JViewport viewport = scroll.getViewport();
    KineticScrollingListener l1 = new KineticScrollingListener(label);
    viewport.addMouseMotionListener(l1);
    viewport.addMouseListener(l1);
    viewport.addHierarchyListener(l1);

    add(new JLayer<>(scroll, new OverscrollEdgeEffectLayerUI()));
    scroll.setPreferredSize(new Dimension(320, 240));
  }

  private static Image makeMissingImage() {
    Icon missingIcon = new MissingIcon();
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class KineticScrollingListener extends MouseAdapter implements HierarchyListener {
  protected static final int SPEED = 4;
  protected static final int DELAY = 10;
  protected static final double D = .8;
  protected final Cursor dc;
  protected final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  protected final Timer scroller;
  protected final JComponent label;
  protected final Point startPt = new Point();
  protected final Point delta = new Point();

  protected KineticScrollingListener(JComponent comp) {
    super();
    this.label = comp;
    this.dc = comp.getCursor();
    this.scroller = new Timer(DELAY, e -> {
      JViewport viewport = (JViewport) SwingUtilities.getUnwrappedParent(label);
      Point vp = viewport.getViewPosition();
      vp.translate(-delta.x, -delta.y);
      label.scrollRectToVisible(new Rectangle(vp, viewport.getSize()));
      if (Math.abs(delta.x) > 0 || Math.abs(delta.y) > 0) {
        delta.setLocation((int) (delta.x * D), (int) (delta.y * D));
      } else {
        ((Timer) e.getSource()).stop();
      }
    });
  }

  @Override public void mousePressed(MouseEvent e) {
    e.getComponent().setCursor(hc);
    startPt.setLocation(e.getPoint());
    scroller.stop();
  }

  @Override public void mouseDragged(MouseEvent e) {
    Point pt = e.getPoint();
    JViewport view = (JViewport) e.getComponent(); // label.getParent();
    Point vp = view.getViewPosition(); // SwingUtilities.convertPoint(view, 0, 0, label);
    vp.translate(startPt.x - pt.x, startPt.y - pt.y);
    delta.setLocation(SPEED * (pt.x - startPt.x), SPEED * (pt.y - startPt.y));
    label.scrollRectToVisible(new Rectangle(vp, view.getSize()));
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

class OverscrollEdgeEffectLayerUI extends LayerUI<JScrollPane> {
  private final Color color = new Color(0xAA_AA_EE_FF, true);
  private final Point mousePt = new Point();
  private final Timer animator = new Timer(20, null);
  private final Ellipse2D oval = new Ellipse2D.Double();
  private double ovalHeight;
  private int delta;

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer && ovalHeight > 0d) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
      Rectangle r = scroll.getViewport().getViewRect();
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(color);
      if (oval.getY() < 0) {
        oval.setFrame(oval.getX(), -ovalHeight, oval.getWidth(), ovalHeight * 2d);
      } else { // if (r.height < oval.getY() + oval.getHeight()) {
        oval.setFrame(oval.getX(), r.getHeight() - ovalHeight, oval.getWidth(), ovalHeight * 2d);
      }
      g2.fill(oval);
      g2.dispose();
    }
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    if (e.getComponent() instanceof JViewport) {
      int id = e.getID();
      if (id == MouseEvent.MOUSE_PRESSED) {
        mousePt.setLocation(e.getPoint());
      } else if (ovalHeight > 0d && id == MouseEvent.MOUSE_RELEASED) {
        ovalShrinking(l);
      }
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    Component c = e.getComponent();
    int id = e.getID();
    if (c instanceof JViewport && id == MouseEvent.MOUSE_DRAGGED && !animator.isRunning()) {
      JViewport viewport = l.getView().getViewport();
      Dimension d = viewport.getView().getSize();
      Rectangle r = viewport.getViewRect();
      Point p = SwingUtilities.convertPoint(c, e.getPoint(), l.getView());
      double ow = Math.max(p.getX(), r.getWidth() - p.getX());
      double ox = p.getX() - ow;
      int dy = e.getPoint().y - mousePt.y;
      if (isDragReversed(dy)) {
        // The y-axis drag direction has been reversed
        ovalShrinking(l);
      } else if (r.y == 0 && dy >= 0) {
        // top edge
        ovalHeight = Math.min(r.getHeight() / 8d, p.getY() / 8d);
        oval.setFrame(ox, -ovalHeight, ow * 2.2, ovalHeight * 2d);
      } else if (d.height == r.y + r.height && dy <= 0) {
        // bottom edge
        ovalHeight = Math.min(r.getHeight() / 8d, (r.getHeight() - p.getY()) / 8d);
        oval.setFrame(ox, r.getHeight() - ovalHeight, ow * 2.2, ovalHeight * 2d);
      }
      mousePt.setLocation(e.getPoint());
      delta = dy;
      l.repaint();
    }
  }

  private boolean isDragReversed(int dy) {
    boolean b1 = delta > 0 && dy < 0;
    boolean b2 = delta < 0 && dy > 0;
    return b1 || b2;
  }

  private void ovalShrinking(JLayer<? extends JScrollPane> l) {
    if (ovalHeight > 0d && !animator.isRunning()) {
      ActionListener handler = e -> {
        if (ovalHeight > 0d && animator.isRunning()) {
          ovalHeight = Math.max(ovalHeight * .67 - .5, 0d);
          l.repaint();
        } else {
          animator.stop();
          for (ActionListener a : animator.getActionListeners()) {
            animator.removeActionListener(a);
          }
        }
      };
      animator.addActionListener(handler);
      animator.start();
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
    return 316;
  }

  @Override public int getIconHeight() {
    return 1024;
  }
}
