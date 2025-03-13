// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    setPreferredSize(new Dimension(320, 240));
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    String txt = "protected void paint";
    Font font = g2.getFont().deriveFont(32f);
    GlyphVector gv = font.createGlyphVector(g2.getFontRenderContext(), txt);
    if (gv.getNumGlyphs() > 0) {
      Arc2D path = new Arc2D.Double(50d, 50d, 200d, 150, 180d, -180d, Arc2D.OPEN);
      g2.setPaint(Color.RED);
      g2.draw(path);
      g2.setPaint(Color.BLACK);
      g2.fill(createTextOnPath(path, gv));
    }
    g2.dispose();
  }

  // [Fun with Java2D - Strokes](http://www.jhlabs.com/java/java2d/strokes/)
  // http://www.jhlabs.com/java/java2d/strokes/ShapeStroke.java
  public static Shape createTextOnPath(Shape shape, GlyphVector gv) {
    double[] points = new double[6];
    Point2D prevPt = new Point2D.Double();
    double nextAdvance = 0d;
    double next = 0d;
    Path2D result = new Path2D.Double();
    int length = gv.getNumGlyphs();
    int idx = 0;
    PathIterator pi = new FlatteningPathIterator(shape.getPathIterator(null), 1d);
    while (idx < length && !pi.isDone()) {
      switch (pi.currentSegment(points)) {
        case PathIterator.SEG_MOVETO:
          result.moveTo(points[0], points[1]);
          prevPt.setLocation(points[0], points[1]);
          nextAdvance = gv.getGlyphMetrics(idx).getAdvance() * .5;
          next = nextAdvance;
          break;

        case PathIterator.SEG_LINETO:
          double dx = points[0] - prevPt.getX();
          double dy = points[1] - prevPt.getY();
          double distance = Math.hypot(dx, dy);
          if (distance >= next) {
            double r = 1d / distance;
            double angle = Math.atan2(dy, dx);
            while (idx < length && distance >= next) {
              double x = prevPt.getX() + next * dx * r;
              double y = prevPt.getY() + next * dy * r;
              double advance = nextAdvance;
              nextAdvance = getNextAdvance(gv, idx, length);
              AffineTransform at = AffineTransform.getTranslateInstance(x, y);
              at.rotate(angle);
              Point2D pt = gv.getGlyphPosition(idx);
              at.translate(-pt.getX() - advance, -pt.getY());
              result.append(at.createTransformedShape(gv.getGlyphOutline(idx)), false);
              next += advance + nextAdvance;
              idx++;
            }
          }
          next -= distance;
          prevPt.setLocation(points[0], points[1]);
          break;

        default:
      }
      pi.next();
    }
    return result;
  }

  private static double getNextAdvance(GlyphVector gv, int idx, int length) {
    double na;
    if (idx < length - 1) {
      na = gv.getGlyphMetrics(idx + 1).getAdvance() * .5;
    } else {
      na = 0d;
    }
    return na;
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
