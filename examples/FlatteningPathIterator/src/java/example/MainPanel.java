// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Ellipse2D circle = new Ellipse2D.Double(0d, 0d, 100d, 100d);
    Ellipse2D ellipse = new Ellipse2D.Double(0d, 0d, 128d, 100d);

    JPanel p = new JPanel();
    p.add(makeLabel("Ellipse2D", circle));
    p.add(makeLabel("Polygon", convertEllipse2Polygon(ellipse)));
    p.add(makeLabel("Polygon", convertEllipse2Polygon(circle)));
    p.add(makeLabel("FlatteningPathIterator", convertEllipse2Polygon(ellipse)));
    // TEST: p.add(makeLabel("convertShape2Polygon", convertShape2Polygon(ellipse)));
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  public static Polygon convertEllipse2Polygon(Ellipse2D e) {
    Rectangle b = e.getBounds();
    int r1 = b.width / 2;
    int r2 = b.height / 2;
    int x0 = b.x + r1;
    int y0 = b.y + r2;
    int v = 60;
    double a = 0d;
    double d = 2 * Math.PI / v;
    Polygon polygon = new Polygon();
    for (int i = 0; i < v; i++) {
      polygon.addPoint((int) (r1 * Math.cos(a) + x0), (int) (r2 * Math.sin(a) + y0));
      a += d;
    }
    return polygon;
  }

  // http://java-sl.com/tip_flatteningpathiterator_moving_shape.html
  // via: https://stackoverflow.com/questions/17272912/converting-an-ellipse2d-to-polygon
  // public static Polygon convertShape2Polygon(Shape s) {
  //   PathIterator i = new FlatteningPathIterator(s.getPathIterator(null), 1d);
  //   Polygon polygon = new Polygon();
  //   float[] coords = new float[6];
  //   while (!i.isDone()) {
  //     i.currentSegment(coords);
  //     polygon.addPoint((int) coords[0], (int) coords[1]);
  //     i.next();
  //   }
  //   return polygon;
  // }

  private static JLabel makeLabel(String title, Shape shape) {
    JLabel l = new JLabel(title, new ShapeIcon(shape), SwingConstants.CENTER);
    l.setHorizontalTextPosition(SwingConstants.CENTER);
    return l;
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

class ShapeIcon implements Icon {
  private final Shape shape;

  protected ShapeIcon(Shape s) {
    shape = s;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.BLACK);
    g2.draw(shape);
    g2.setPaint(Color.RED);

    PathIterator i = new FlatteningPathIterator(shape.getPathIterator(null), 1d);
    double[] coords = new double[6];
    while (!i.isDone()) {
      i.currentSegment(coords);
      g2.fillRect((int) (coords[0] - .5), (int) (coords[1] - .5), 2, 2);
      i.next();
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return shape.getBounds().width + 1;
  }

  @Override public int getIconHeight() {
    return shape.getBounds().height + 1;
  }
}
