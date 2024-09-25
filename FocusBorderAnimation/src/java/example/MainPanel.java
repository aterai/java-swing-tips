// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextField field1 = makeTextField("Animated Border 1");
    field1.setBorder(new AnimatedBorder(field1));

    JTextField field2 = makeTextField("Animated Border 2");
    field2.setBorder(new AnimatedBorder(field2));

    JTextField field3 = makeTextField("Animated Border 3");
    field3.setEnabled(false);
    field3.setBorder(new AnimatedBorder(field3));

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    box.add(makeTextField("Default Border"));
    box.add(Box.createVerticalStrut(30));
    box.add(field1);
    box.add(Box.createVerticalStrut(10));
    box.add(field2);
    box.add(Box.createVerticalStrut(10));
    box.add(field3);
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTextField makeTextField(String text) {
    JTextField field = new JTextField(text, 32);
    field.setOpaque(false);
    return field;
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

class AnimatedBorder extends EmptyBorder {
  private static final int BOTTOM_SPACE = 20;
  private static final double PLAY_TIME = 300d;
  private static final int BORDER = 4;
  private final Timer animator = new Timer(10, null);
  private final transient Stroke stroke = new BasicStroke(BORDER);
  private final transient Stroke bottomStroke = new BasicStroke(BORDER / 2f);
  private long startTime = -1L;
  private final transient List<Point2D> points = new ArrayList<>();
  private final Path2D borderPath = new Path2D.Double();

  protected AnimatedBorder(JComponent c) {
    super(BORDER, BORDER, BORDER + BOTTOM_SPACE, BORDER);
    animator.addActionListener(e -> {
      if (startTime < 0) {
        startTime = System.currentTimeMillis();
      }
      long playTime = System.currentTimeMillis() - startTime;
      double progress = playTime / PLAY_TIME;
      boolean stop = progress > 1d || points.isEmpty();
      if (stop) {
        startTime = -1L;
        ((Timer) e.getSource()).stop();
        c.repaint();
        return;
      }
      Point2D pos = new Point2D.Double();
      pos.setLocation(points.get(0));
      borderPath.reset();
      borderPath.moveTo(pos.getX(), pos.getY());
      int idx = Math.min(Math.max(0, (int) (points.size() * progress)), points.size() - 1);
      for (int i = 0; i <= idx; i++) {
        pos.setLocation(points.get(i));
        borderPath.lineTo(pos.getX(), pos.getY());
        borderPath.moveTo(pos.getX(), pos.getY());
      }
      borderPath.closePath();
      c.repaint();
    });
    c.addFocusListener(new FocusListener() {
      @Override public void focusGained(FocusEvent e) {
        Rectangle r = c.getBounds();
        r.height -= BOTTOM_SPACE + 1;
        Path2D p = new Path2D.Double();
        p.moveTo(r.getWidth(), r.getHeight());
        p.lineTo(r.getWidth(), 0d);
        p.lineTo(0d, 0d);
        p.lineTo(0d, r.getHeight());
        p.closePath();
        makePointList(p, points);
        animator.start();
      }

      @Override public void focusLost(FocusEvent e) {
        points.clear();
        borderPath.reset();
        c.repaint();
      }
    });
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    super.paintBorder(c, g, x, y, w, h);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(c.isEnabled() ? Color.ORANGE : Color.GRAY);
    g2.translate(x, y);
    g2.setStroke(bottomStroke);
    g2.drawLine(0, h - BOTTOM_SPACE, w - 1, h - BOTTOM_SPACE);
    g2.setStroke(stroke);
    g2.draw(borderPath);
    g2.dispose();
  }

  public static void makePointList(Shape shape, List<Point2D> points) {
    points.clear();
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
}
