// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 3));
    add(makeTitledPanel("GeneralPath", new StarPanel1()));
    add(makeTitledPanel("Polygon", new StarPanel2()));
    add(makeTitledPanel("Font(Outline)", new StarPanel3()));
    add(makeTitledPanel("Icon", new JLabel(new StarIcon0())));
    add(makeTitledPanel("Icon(R=40)", new JLabel(new StarIcon1())));
    add(makeTitledPanel("Icon(R=20,40)", new JLabel(new StarIcon2())));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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

class StarPanel1 extends JPanel {
  @Override protected void paintComponent(Graphics g) {
    int w = getWidth();
    int h = getHeight();
    // <blockquote cite="%JAVA_HOME%/demo/jfc/Java2D/src/java2d/demos/Lines/Joins.java">
    GeneralPath p = new GeneralPath();
    p.moveTo(-w / 4f, -h / 12f);
    p.lineTo(w / 4f, -h / 12f);
    p.lineTo(-w / 6f, h / 4f);
    p.lineTo(0, -h / 4f);
    p.lineTo(w / 6f, h / 4f);
    p.closePath();
    // </blockquote>

    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(w / 2, h / 2);
    g2.setPaint(Color.YELLOW);
    g2.fill(p);
    g2.setPaint(Color.BLACK);
    g2.draw(p);
    g2.dispose();
  }
}

class StarPanel2 extends JPanel {
  @Override protected void paintComponent(Graphics g) {
    int w = getWidth();
    int h = getHeight();
    Polygon p = new Polygon();
    p.addPoint(Math.round(-w / 4f), Math.round(-h / 12f));
    p.addPoint(Math.round(w / 4f), Math.round(-h / 12f));
    p.addPoint(Math.round(-w / 6f), Math.round(h / 4f));
    p.addPoint(0, Math.round(-h / 4f));
    p.addPoint(Math.round(w / 6f), Math.round(h / 4f));

    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(w / 2, h / 2);
    g2.setPaint(Color.YELLOW);
    g2.fill(p);
    g2.setPaint(Color.BLACK);
    g2.draw(p);
    g2.dispose();
  }
}

class StarPanel3 extends JPanel {
  private static final int FONT_SIZE = 80;
  private final Font font = new Font(Font.SERIF, Font.PLAIN, FONT_SIZE);

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(0, FONT_SIZE);
    FontRenderContext frc = g2.getFontRenderContext();
    Shape shape = new TextLayout("★", font, frc).getOutline(null);
    g2.setPaint(Color.YELLOW);
    g2.fill(shape);
    g2.setPaint(Color.BLACK);
    g2.draw(shape);
    g2.dispose();
  }
}

// class StarPanel4 extends JPanel {
//   @Override protected void paintComponent(Graphics g) {
//     Graphics2D g2 = (Graphics2D) g.create();
//     int w = getWidth();
//     int h = getHeight();
//     Path2D p = new Path2D.Double();
//     p.moveTo(-w / 4.0, -h / 12.0);
//     p.quadTo(0.0, -h / 4.0, w / 4.0, -h / 12.0);
//     p.quadTo(w / 6.0, h / 4.0, -w / 6.0, h / 4.0);
//     p.quadTo(-w / 4.0, -h / 12.0, 0.0, -h / 4.0);
//     p.quadTo(w / 4.0, -h / 12.0, w / 6.0, h / 4.0);
//     p.quadTo(-w / 6.0, h / 4.0, -w / 4.0, -h / 12.0);
//     p.closePath();
//     g2.translate(w / 2, h / 2);
//     g2.setPaint(Color.YELLOW);
//     g2.fill(p);
//     g2.setPaint(Color.BLACK);
//     g2.draw(p);
//     g2.dispose();
//   }
// }

class StarIcon0 implements Icon {
  private static final double SCALE = .8;
  private final GeneralPath path = new GeneralPath();

  protected StarIcon0() {
    // <blockquote cite="https://gihyo.jp/dev/serial/01/javafx/0009?page=2">
    path.moveTo(50.000, 0.0000);
    path.lineTo(61.803, 38.196);
    path.lineTo(100.00, 38.196);
    path.lineTo(69.098, 61.804);
    path.lineTo(80.902, 100.00);
    path.lineTo(50.000, 76.394);
    path.lineTo(19.098, 100.00);
    path.lineTo(30.902, 61.804);
    path.lineTo(0.0000, 38.196);
    path.lineTo(38.197, 38.196);
    path.closePath();
    // </blockquote>
    // Scale the 100x100 path down to 80x80 at once instead of scaling each coordinate
    path.transform(AffineTransform.getScaleInstance(SCALE, SCALE));
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.YELLOW);
    g2.fill(path);
    g2.setPaint(Color.BLACK);
    g2.draw(path);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 80;
  }

  @Override public int getIconHeight() {
    return 80;
  }
}

class StarIcon1 implements Icon {
  private static final int RADIUS = 40;
  private final Shape star;

  protected StarIcon1() {
    // Connect every second vertex of a regular pentagon (step = 4 * PI / 5)
    double step = 4d * Math.PI / 5d;
    Path2D path = new Path2D.Double();
    path.moveTo(RADIUS, 0d);
    for (int i = 1; i < 5; i++) {
      double angle = step * i;
      path.lineTo(RADIUS * Math.cos(angle), RADIUS * Math.sin(angle));
    }
    path.closePath();
    // Rotate -90 degrees around (RADIUS, 0) so that the first vertex points up and
    // the center of the star moves from (0, 0) to (RADIUS, RADIUS)
    AffineTransform at = AffineTransform.getRotateInstance(-Math.PI / 2d, RADIUS, 0d);
    star = new Path2D.Double(path, at);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.YELLOW);
    g2.fill(star);
    g2.setPaint(Color.BLACK);
    g2.draw(star);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 2 * RADIUS;
  }

  @Override public int getIconHeight() {
    return 2 * RADIUS;
  }
}

class StarIcon2 implements Icon {
  private static final int OUTER_RADIUS = 40;
  private static final int INNER_RADIUS = 20;
  // Inner radius of a regular pentagram:
  // OUTER_RADIUS * Math.cos(2d * Math.PI / 5d) / Math.cos(Math.PI / 5d); // = 15.28...
  private static final int VERTEX_COUNT = 5; // 16;
  private final Shape star;

  protected StarIcon2() {
    double step = Math.PI / VERTEX_COUNT;
    Path2D path = new Path2D.Double();
    path.moveTo(OUTER_RADIUS, 0d);
    for (int i = 1; i < VERTEX_COUNT * 2; i++) {
      // odd indices are the inner (concave) vertices
      double r = i % 2 == 0 ? OUTER_RADIUS : INNER_RADIUS;
      double angle = step * i;
      path.lineTo(r * Math.cos(angle), r * Math.sin(angle));
    }
    path.closePath();
    AffineTransform at = AffineTransform.getRotateInstance(-Math.PI / 2d, OUTER_RADIUS, 0d);
    star = new Path2D.Double(path, at);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.YELLOW);
    g2.fill(star);
    g2.setPaint(Color.BLACK);
    g2.draw(star);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 2 * OUTER_RADIUS;
  }

  @Override public int getIconHeight() {
    return 2 * OUTER_RADIUS;
  }
}

// // Java2D Shapes project.
// // http://java-sl.com/shapes.html
// int[] getXCoordinates(int x, int y, int r, int innerR, int vertexCount, double startAngle) {
//   int[] res = new int[vertexCount * 2];
//   double addAngle = 2 * Math.PI / vertexCount;
//   double angle = startAngle;
//   double innerAngle = startAngle + Math.PI / vertexCount;
//   for (int i = 0; i < vertexCount; i++) {
//     res[i * 2] = (int) Math.round(r * Math.cos(angle)) + x;
//     angle += addAngle;
//     res[i * 2 + 1] = (int) Math.round(innerR * Math.cos(innerAngle)) + x;
//     innerAngle += addAngle;
//   }
//   return res;
// }
//
// int[] getYCoordinates(int x, int y, int r, int innerR, int vertexCount, double startAngle) {
//   int[] res = new int[vertexCount * 2];
//   double addAngle = 2 * Math.PI / vertexCount;
//   double angle = startAngle;
//   double innerAngle = startAngle + Math.PI / vertexCount;
//   for (int i = 0; i < vertexCount; i++) {
//     res[i * 2] = (int) Math.round(r * Math.sin(angle)) + y;
//     angle += addAngle;
//     res[i * 2 + 1] = (int) Math.round(innerR * Math.sin(innerAngle)) + y;
//     innerAngle += addAngle;
//   }
//   return res;
// }
