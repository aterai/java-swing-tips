// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    List<Arrow> arrows = Arrays.asList(
        new Arrow(new Point(50, 50), new Point(100, 150)),
        new Arrow(new Point(250, 50), new Point(150, 50)));

    JPanel p = new JPanel() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(4));
        g2.setColor(Color.BLACK);
        for (Arrow a : arrows) {
          a.draw(g2);
        }
        g2.dispose();
      }
    };
    add(p);
    setPreferredSize(new Dimension(320, 240));
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

class Arrow {
  private final Point start = new Point();
  private final Point end = new Point();
  private final Path2D arrowHead = makeArrowHead(new Dimension(8, 8));

  protected Arrow(Point start, Point end) {
    this.start.setLocation(start);
    this.end.setLocation(end);
  }

  protected Path2D makeArrowHead(Dimension size) {
    Path2D path = new Path2D.Double();
    double w = size.width * .5;
    double h = size.height;
    path.moveTo(0d, -w);
    path.lineTo(h, 0d);
    path.lineTo(0d, w);
    path.closePath();
    return path;
  }

  public void draw(Graphics2D g2) {
    g2.drawLine(start.x, start.y, end.x, end.y);
    // arrowHead.transform(AffineTransform.getRotateInstance(end.x - start.x, end.y - start.y));
    // arrowHead.transform(AffineTransform.getTranslateInstance(end.x, end.y));
    AffineTransform at = AffineTransform.getTranslateInstance(end.getX(), end.getY());
    at.rotate(end.getX() - start.getX(), end.getY() - start.getY());
    arrowHead.transform(at);
    g2.fill(arrowHead);
    g2.draw(arrowHead);
  }
}
