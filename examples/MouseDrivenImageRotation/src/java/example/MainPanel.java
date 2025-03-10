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
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final transient DraggableImageMouseListener di;

  private MainPanel() {
    super();
    String path = "example/test.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Image img = Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);

    di = new DraggableImageMouseListener(new ImageIcon(img));
    addMouseListener(di);
    addMouseMotionListener(di);
    setPreferredSize(new Dimension(320, 240));
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

  @Override protected void paintComponent(Graphics g) {
    // super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getWidth();
    int h = getHeight();
    g2.setPaint(new GradientPaint(50f, 0f, Color.GRAY, w, h, Color.DARK_GRAY, true));
    g2.fillRect(0, 0, w, h);
    g2.dispose();
    di.paint(g, this);
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

class DraggableImageMouseListener extends MouseAdapter {
  private static final BasicStroke BORDER_STROKE = new BasicStroke(4f);
  private static final Color BORDER_COLOR = Color.WHITE;
  private static final Color HOVER_COLOR = new Color(0x64_64_FF_C8, true);
  private static final int IR = 40;
  private static final int OR = IR * 3;
  private final Shape border;
  private final Shape polaroid;
  private final RectangularShape inner = new Ellipse2D.Double(0d, 0d, IR, IR);
  private final RectangularShape outer = new Ellipse2D.Double(0d, 0d, OR, OR);
  private final Point2D startPt = new Point2D.Double(); // drag start point
  private final Point2D centerPt = new Point2D.Double(100d, 100d); // center of Image
  private final Dimension imageSz;
  private final Image image;
  private double radian = Math.toRadians(45d); // 45d / 180d * Math.PI;
  private double startRadian; // drag start radian
  private boolean moverHover;
  private boolean rotatorHover;

  protected DraggableImageMouseListener(ImageIcon ii) {
    super();
    image = ii.getImage();
    int width = ii.getIconWidth();
    int height = ii.getIconHeight();
    imageSz = new Dimension(width, height);
    border = new RoundRectangle2D.Double(0d, 0d, width, height, 10d, 10d);
    polaroid = new Rectangle2D.Double(-2d, -2d, width + 4d, height + 20d);
    setCirclesLocation(centerPt);
  }

  private void setCirclesLocation(Point2D center) {
    double cx = center.getX();
    double cy = center.getY();
    inner.setFrameFromCenter(cx, cy, cx + IR / 2d, cy - IR / 2d);
    outer.setFrameFromCenter(cx, cy, cx + OR / 2d, cy - OR / 2d);
  }

  public void paint(Graphics g, ImageObserver observer) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    double w2 = imageSz.width / 2d;
    double h2 = imageSz.height / 2d;
    double tx = centerPt.getX() - w2;
    double ty = centerPt.getY() - h2;
    AffineTransform at = AffineTransform.getTranslateInstance(tx, ty);
    at.rotate(radian, w2, h2);

    g2.setPaint(BORDER_COLOR);
    g2.setStroke(BORDER_STROKE);
    Shape s = at.createTransformedShape(polaroid);
    g2.fill(s);
    g2.draw(s);

    g2.drawImage(image, at, observer);

    if (rotatorHover) {
      Area donut = new Area(outer);
      donut.subtract(new Area(inner));
      g2.setPaint(HOVER_COLOR);
      g2.fill(donut);
    } else if (moverHover) {
      g2.setPaint(HOVER_COLOR);
      g2.fill(inner);
    }

    g2.setPaint(BORDER_COLOR);
    g2.setStroke(BORDER_STROKE);
    g2.draw(at.createTransformedShape(border));
    g2.dispose();
  }

  @Override public void mouseMoved(MouseEvent e) {
    Point pt = e.getPoint();
    if (outer.contains(pt) && !inner.contains(pt)) {
      moverHover = false;
      rotatorHover = true;
    } else if (inner.contains(pt)) {
      moverHover = true;
      rotatorHover = false;
    } else {
      moverHover = false;
      rotatorHover = false;
    }
    e.getComponent().repaint();
  }

  @Override public void mouseReleased(MouseEvent e) {
    rotatorHover = false;
    moverHover = false;
    e.getComponent().repaint();
  }

  @Override public void mousePressed(MouseEvent e) {
    Point pt = e.getPoint();
    if (outer.contains(pt) && !inner.contains(pt)) {
      rotatorHover = true;
      startRadian = radian - Math.atan2(e.getY() - centerPt.getY(), e.getX() - centerPt.getX());
      e.getComponent().repaint();
    } else if (inner.contains(pt)) {
      moverHover = true;
      startPt.setLocation(pt);
      e.getComponent().repaint();
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    if (rotatorHover) {
      double y = e.getY() - centerPt.getY();
      double x = e.getX() - centerPt.getX();
      radian = startRadian + Math.atan2(y, x);
      e.getComponent().repaint();
    } else if (moverHover) {
      double x = centerPt.getX() + e.getX() - startPt.getX();
      double y = centerPt.getY() + e.getY() - startPt.getY();
      centerPt.setLocation(x, y);
      setCirclesLocation(centerPt);
      startPt.setLocation(e.getPoint());
      e.getComponent().repaint();
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
