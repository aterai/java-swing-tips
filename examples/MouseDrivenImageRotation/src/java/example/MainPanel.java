// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final transient DraggableImageMouseListener listener;

  private MainPanel() {
    super();
    String path = "example/test.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    BufferedImage image = Optional.ofNullable(cl.getResource(path))
        .map(MainPanel::readImage)
        .orElseGet(MainPanel::createMissingImage);
    listener = new DraggableImageMouseListener(image);
    addMouseListener(listener);
    addMouseMotionListener(listener);
    setPreferredSize(new Dimension(320, 240));
  }

  private static BufferedImage readImage(URL url) {
    Optional<BufferedImage> image;
    try (InputStream s = url.openStream()) {
      image = Optional.ofNullable(ImageIO.read(s));
    } catch (IOException ex) {
      image = Optional.empty();
    }
    return image.orElseGet(MainPanel::createMissingImage);
  }

  private static BufferedImage createMissingImage() {
    Icon missingIcon = new MissingIcon();
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
  }

  @Override protected void paintComponent(Graphics g) {
    // super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getWidth();
    int h = getHeight();
    g2.setPaint(new GradientPaint(50f, 0f, Color.GRAY, w, h, Color.DARK_GRAY, true));
    g2.fillRect(0, 0, w, h);
    g2.dispose();
    listener.paint(g);
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

class DraggableImageMouseListener extends MouseAdapter {
  private static final BasicStroke BORDER_STROKE = new BasicStroke(4f);
  private static final Color BORDER_COLOR = Color.WHITE;
  private static final Color HOVER_COLOR = new Color(0x64_64_FF_C8, true);
  private static final int INNER_RADIUS = 20;
  private static final int OUTER_RADIUS = INNER_RADIUS * 3;
  private final Shape imageBorder;
  private final Shape polaroid;
  private final RectangularShape innerCircle = new Ellipse2D.Double();
  private final RectangularShape outerCircle = new Ellipse2D.Double();
  private final Point2D dragStart = new Point2D.Double();
  private final Point2D center = new Point2D.Double(100d, 100d); // center of the image
  private final BufferedImage image;
  private double angle = Math.toRadians(45d); // rotation angle in radians
  private double angleOffset; // angle - pointer angle at the start of a rotation drag
  private Handle activeHandle = Handle.NONE;
  private boolean dragging;

  private enum Handle { NONE, MOVER, ROTATOR }

  protected DraggableImageMouseListener(BufferedImage image) {
    super();
    this.image = image;
    int width = image.getWidth();
    int height = image.getHeight();
    imageBorder = new RoundRectangle2D.Double(0d, 0d, width, height, 10d, 10d);
    polaroid = new Rectangle2D.Double(-2d, -2d, width + 4d, height + 20d);
    setCirclesCenter(center);
  }

  private void setCirclesCenter(Point2D pt) {
    double cx = pt.getX();
    double cy = pt.getY();
    innerCircle.setFrameFromCenter(cx, cy, cx + INNER_RADIUS, cy + INNER_RADIUS);
    outerCircle.setFrameFromCenter(cx, cy, cx + OUTER_RADIUS, cy + OUTER_RADIUS);
  }

  private Handle getHandleAt(Point2D pt) {
    Handle handle;
    if (innerCircle.contains(pt)) {
      handle = Handle.MOVER;
    } else if (outerCircle.contains(pt)) {
      handle = Handle.ROTATOR;
    } else {
      handle = Handle.NONE;
    }
    return handle;
  }

  private void setActiveHandle(Handle handle, Component c) {
    if (activeHandle != handle) {
      activeHandle = handle;
      c.setCursor(Cursor.getPredefinedCursor(getCursorType(handle)));
      c.repaint();
    }
  }

  private static int getCursorType(Handle handle) {
    int type;
    switch (handle) {
      case MOVER:
        type = Cursor.MOVE_CURSOR;
        break;
      case ROTATOR:
        type = Cursor.HAND_CURSOR;
        break;
      default:
        type = Cursor.DEFAULT_CURSOR;
        break;
    }
    return type;
  }

  private double getPointerAngle(MouseEvent e) {
    return Math.atan2(e.getY() - center.getY(), e.getX() - center.getX());
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    double w2 = image.getWidth() / 2d;
    double h2 = image.getHeight() / 2d;
    double tx = center.getX() - w2;
    double ty = center.getY() - h2;
    AffineTransform at = AffineTransform.getTranslateInstance(tx, ty);
    at.rotate(angle, w2, h2);

    g2.setPaint(BORDER_COLOR);
    g2.setStroke(BORDER_STROKE);
    Shape s = at.createTransformedShape(polaroid);
    g2.fill(s);
    g2.draw(s);

    g2.drawImage(image, at, null);

    if (activeHandle == Handle.ROTATOR) {
      Area donut = new Area(outerCircle);
      donut.subtract(new Area(innerCircle));
      g2.setPaint(HOVER_COLOR);
      g2.fill(donut);
    } else if (activeHandle == Handle.MOVER) {
      g2.setPaint(HOVER_COLOR);
      g2.fill(innerCircle);
    }

    g2.setPaint(BORDER_COLOR);
    g2.setStroke(BORDER_STROKE);
    g2.draw(at.createTransformedShape(imageBorder));
    g2.dispose();
  }

  @Override public void mouseMoved(MouseEvent e) {
    setActiveHandle(getHandleAt(e.getPoint()), e.getComponent());
  }

  @Override public void mouseExited(MouseEvent e) {
    if (!dragging) {
      setActiveHandle(Handle.NONE, e.getComponent());
    }
  }

  @Override public void mousePressed(MouseEvent e) {
    if (!SwingUtilities.isLeftMouseButton(e)) {
      return;
    }
    Handle handle = getHandleAt(e.getPoint());
    setActiveHandle(handle, e.getComponent());
    dragging = handle != Handle.NONE;
    if (handle == Handle.ROTATOR) {
      angleOffset = angle - getPointerAngle(e);
    } else if (handle == Handle.MOVER) {
      dragStart.setLocation(e.getPoint());
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    if (!dragging) {
      return;
    }
    if (activeHandle == Handle.ROTATOR) {
      angle = angleOffset + getPointerAngle(e);
    } else if (activeHandle == Handle.MOVER) {
      double dx = e.getX() - dragStart.getX();
      double dy = e.getY() - dragStart.getY();
      center.setLocation(center.getX() + dx, center.getY() + dy);
      setCirclesCenter(center);
      dragStart.setLocation(e.getPoint());
    }
    e.getComponent().repaint();
  }

  @Override public void mouseReleased(MouseEvent e) {
    if (dragging && SwingUtilities.isLeftMouseButton(e)) {
      dragging = false;
      setActiveHandle(getHandleAt(e.getPoint()), e.getComponent());
    }
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
    return 320;
  }

  @Override public int getIconHeight() {
    return 240;
  }
}
