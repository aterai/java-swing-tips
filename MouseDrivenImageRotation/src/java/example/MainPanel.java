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
import java.awt.image.ImageObserver;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final transient DraggableImageMouseListener di;

  public MainPanel() {
    super();
    di = new DraggableImageMouseListener(new ImageIcon(getClass().getResource("test.png")));
    addMouseListener(di);
    addMouseMotionListener(di);
    setPreferredSize(new Dimension(320, 240));
  }

  @Override protected void paintComponent(Graphics g) {
    // super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(new GradientPaint(50, 0, new Color(200, 200, 200), getWidth(), getHeight(), new Color(100, 100, 100), true));
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
    di.paint(g, this);
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
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
  private static final Color HOVER_COLOR = new Color(100, 255, 200, 100);
  private static final int IR = 40;
  private static final int OR = IR * 3;
  private final Shape border;
  private final Shape polaroid;
  private final RectangularShape inner = new Ellipse2D.Double(0, 0, IR, IR);
  private final RectangularShape outer = new Ellipse2D.Double(0, 0, OR, OR);
  private final Point2D startPt = new Point2D.Double(); // drag start point
  private final Point2D centerPt = new Point2D.Double(100d, 100d); // center of Image
  private final Dimension imageSz;
  private final Image image;
  private double radian = 45d * (Math.PI / 180d);
  private double startRadian; // drag start radian
  private boolean moverHover;
  private boolean rotatorHover;

  protected DraggableImageMouseListener(ImageIcon ii) {
    super();
    image = ii.getImage();
    int width = ii.getIconWidth();
    int height = ii.getIconHeight();
    imageSz = new Dimension(width, height);
    border = new RoundRectangle2D.Double(0, 0, width, height, 10, 10);
    polaroid = new Rectangle2D.Double(-2, -2, width + 4, height + 20);
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
    AffineTransform at = AffineTransform.getTranslateInstance(centerPt.getX() - w2, centerPt.getY() - h2);
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
    if (outer.contains(e.getX(), e.getY()) && !inner.contains(e.getX(), e.getY())) {
      moverHover = false;
      rotatorHover = true;
    } else if (inner.contains(e.getX(), e.getY())) {
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
    if (outer.contains(e.getX(), e.getY()) && !inner.contains(e.getX(), e.getY())) {
      rotatorHover = true;
      startRadian = radian - Math.atan2(e.getY() - centerPt.getY(), e.getX() - centerPt.getX());
      e.getComponent().repaint();
    } else if (inner.contains(e.getX(), e.getY())) {
      moverHover = true;
      startPt.setLocation(e.getPoint());
      e.getComponent().repaint();
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    if (rotatorHover) {
      radian = startRadian + Math.atan2(e.getY() - centerPt.getY(), e.getX() - centerPt.getX());
      e.getComponent().repaint();
    } else if (moverHover) {
      centerPt.setLocation(centerPt.getX() + e.getX() - startPt.getX(), centerPt.getY() + e.getY() - startPt.getY());
      setCirclesLocation(centerPt);
      startPt.setLocation(e.getPoint());
      e.getComponent().repaint();
    }
  }
}
