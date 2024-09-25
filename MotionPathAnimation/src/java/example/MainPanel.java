// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final double PLAY_TIME = 5000d;
  private long startTime = -1L;
  private final transient Shape shape;
  private final Point2D pos = new Point2D.Double();
  private final List<Point2D> points = new ArrayList<>();
  private final JButton button = new JButton("start");

  private MainPanel() {
    super(new BorderLayout());
    // Area area = new Area(new RoundRectangle2D.Double(0d, 0d, 100d, 80d, 50d, 30d));
    // area.subtract(new Area(new Ellipse2D.Double(-20d, 20d, 40d, 40d)));
    // area.add(new Area(new Ellipse2D.Double(30d, -20d, 40d, 40d)));
    // shape = area;
    shape = new RoundRectangle2D.Double(0d, 0d, 100d, 80d, 50d, 30d);
    makePointList(shape, points);
    pos.setLocation(points.get(0));
    Timer timer = new Timer(50, e -> {
      if (startTime < 0) {
        startTime = System.currentTimeMillis();
      }
      long playTime = System.currentTimeMillis() - startTime;
      double progress = playTime / PLAY_TIME;
      boolean stop = progress > 1d;
      if (stop) {
        progress = 1d;
        ((Timer) e.getSource()).stop();
        startTime = -1L;
        button.setEnabled(true);
      }
      int index = Math.min(Math.max(0, (int) (points.size() * progress)), points.size() - 1);
      pos.setLocation(points.get(index));
      repaint();
    });

    button.addActionListener(e -> {
      if (!timer.isRunning()) {
        timer.start();
        button.setEnabled(false);
      }
    });
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void makePointList(Shape shape, List<Point2D> points) {
    PathIterator pi = shape.getPathIterator(null, .01);
    Point2D prev = new Point2D.Double();
    double delta = .02;
    double threshold = 2d;
    double[] coords = new double[6];
    while (!pi.isDone()) {
      int pathSegmentType = pi.currentSegment(coords);
      Point2D current = createPoint(coords[0], coords[1]);
      if (pathSegmentType == PathIterator.SEG_MOVETO) {
        points.add(current);
        prev.setLocation(current);
      } else if (pathSegmentType == PathIterator.SEG_LINETO) {
        double distance = prev.distance(current);
        double fraction = delta;
        if (distance > threshold) {
          Point2D p = interpolate(prev, current, fraction);
          while (distance > prev.distance(p)) {
            points.add(p);
            fraction += delta;
            p = interpolate(prev, current, fraction);
          }
        } else {
          points.add(current);
        }
        prev.setLocation(current);
      }
      pi.next();
    }
  }

  private static Point2D createPoint(double x, double y) {
    return new Point2D.Double(x, y);
  }

  private static Point2D interpolate(Point2D start, Point2D end, double fraction) {
    double dx = end.getX() - start.getX();
    double dy = end.getY() - start.getY();
    double nx = start.getX() + dx * fraction;
    double ny = start.getY() + dy * fraction;
    return new Point2D.Double(nx, ny);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    applyQualityRenderingHints(g2);
    Rectangle r = shape.getBounds();
    int x = (getWidth() - r.width) / 2;
    int y = (getHeight() - r.height) / 2;
    g2.translate(x, y);
    g2.draw(shape);
    Ellipse2D ellipse = new Ellipse2D.Double();
    for (int i = 0; i < points.size(); i += 10) {
      Point2D p = points.get(i);
      ellipse.setFrame(p.getX() - 2d, p.getY() - 2d, 4d, 4d);
      g2.draw(ellipse);
    }
    g2.setColor(Color.RED);
    ellipse.setFrame(pos.getX() - 4d, pos.getY() - 4d, 8d, 8d);
    g2.draw(ellipse);
    g2.dispose();
  }

  public static void applyQualityRenderingHints(Graphics2D g2) {
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    g2.setRenderingHint(
        RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    // g2.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
    // g2.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY);
    // g2.setRenderingHint(KEY_DITHERING, VALUE_DITHER_ENABLE);
    // g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
    // g2.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
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
