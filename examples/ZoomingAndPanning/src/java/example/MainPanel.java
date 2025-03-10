// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private transient ZoomAndPanHandler zoomAndPanHandler;
  private final transient Icon icon;

  private MainPanel() {
    super(new BorderLayout());
    String path = "example/CRW_3857_JFR.jpg"; // https://sozai-free.com/
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    icon = Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return new MissingIcon();
      }
    }).orElseGet(MissingIcon::new);
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void updateUI() {
    removeMouseListener(zoomAndPanHandler);
    removeMouseMotionListener(zoomAndPanHandler);
    removeMouseWheelListener(zoomAndPanHandler);
    super.updateUI();
    DefaultBoundedRangeModel range = new DefaultBoundedRangeModel(0, 1, -10, 11);
    zoomAndPanHandler = new ZoomAndPanHandler(1.2, range);
    addMouseListener(zoomAndPanHandler);
    addMouseMotionListener(zoomAndPanHandler);
    addMouseWheelListener(zoomAndPanHandler);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.clearRect(0, 0, getWidth(), getHeight());
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setTransform(zoomAndPanHandler.getCoordAndZoomTransform());
    icon.paintIcon(this, g2, 0, 0);
    g2.dispose();
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

class ZoomAndPanHandler extends MouseAdapter {
  private final double zoomFactor;
  private final BoundedRangeModel zoomRange;
  private final AffineTransform coordAndZoomAtf = new AffineTransform();
  private final Point2D dragStartPoint = new Point();

  protected ZoomAndPanHandler(double zoomFactor, BoundedRangeModel zoomRange) {
    super();
    this.zoomFactor = zoomFactor;
    this.zoomRange = zoomRange;
  }

  @Override public void mousePressed(MouseEvent e) {
    dragStartPoint.setLocation(e.getPoint());
  }

  @Override public void mouseDragged(MouseEvent e) {
    Point2D dragEndPoint = e.getPoint();
    Point2D dragStart = transformPoint(dragStartPoint);
    Point2D dragEnd = transformPoint(dragEndPoint);
    double tx = dragEnd.getX() - dragStart.getX();
    double ty = dragEnd.getY() - dragStart.getY();
    coordAndZoomAtf.translate(tx, ty);
    dragStartPoint.setLocation(dragEndPoint);
    e.getComponent().repaint();
  }

  @Override public void mouseWheelMoved(MouseWheelEvent e) {
    int dir = e.getWheelRotation();
    int z = zoomRange.getValue();
    int ext = zoomRange.getExtent();
    zoomRange.setValue(z + ext * (dir > 0 ? -1 : 1));
    if (z != zoomRange.getValue()) {
      double scale = dir > 0 ? 1d / zoomFactor : zoomFactor;
      Point2D pt;
      if (e.isControlDown()) {
        Rectangle r = e.getComponent().getBounds();
        pt = new Point2D.Double(r.getCenterX(), r.getCenterY());
      } else {
        pt = transformPoint(e.getPoint());
      }
      coordAndZoomAtf.translate(pt.getX(), pt.getY());
      coordAndZoomAtf.scale(scale, scale);
      coordAndZoomAtf.translate(-pt.getX(), -pt.getY());
      e.getComponent().repaint();
    }
  }

  // https://community.oracle.com/thread/1263955
  // How to implement Zoom & Pan in Java using Graphics2D
  private Point2D transformPoint(Point2D p1) {
    AffineTransform inverse = coordAndZoomAtf;
    boolean hasInverse = coordAndZoomAtf.getDeterminant() != 0d;
    if (hasInverse) {
      try {
        inverse = coordAndZoomAtf.createInverse();
      } catch (NoninvertibleTransformException ex) {
        // should never happen
        assert false;
      }
    }
    Point2D p2 = new Point();
    inverse.transform(p1, p2);
    return p2;
  }

  public AffineTransform getCoordAndZoomTransform() {
    return coordAndZoomAtf;
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
    return 1000;
  }

  @Override public int getIconHeight() {
    return 1000;
  }
}
