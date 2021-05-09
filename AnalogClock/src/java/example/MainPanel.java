// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.time.LocalTime;
import java.time.ZoneId;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public LocalTime time = LocalTime.now(ZoneId.systemDefault());

  private MainPanel() {
    super(new BorderLayout());
    JPanel clock = new JPanel() {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
        g2.setColor(Color.BLACK);
        g2.fill(rect);
        g2.setColor(Color.WHITE);
        g2.translate(rect.getCenterX(), rect.getCenterY());
        float radius = Math.min(rect.width, rect.height) / 2f - 10f;

        // Drawing the hour markers
        float hourMarkerLen = radius / 6f - 10f;
        Shape hourMarker = new Line2D.Float(0f, hourMarkerLen - radius, 0f, -radius);
        AffineTransform at = AffineTransform.getRotateInstance(0d);
        for (int i = 0; i < 12; i++) {
          at.rotate(Math.PI / 6d);
          g2.draw(at.createTransformedShape(hourMarker));
        }

        // Drawing the minute hand
        float minuteHandLen = 5f * radius / 6f;
        Shape minuteHand = new Line2D.Float(0f, 0f, 0f, -minuteHandLen);
        AffineTransform at2 = AffineTransform.getRotateInstance(time.getMinute() * Math.PI / 30d);
        g2.setStroke(new BasicStroke(4f));
        g2.setPaint(Color.WHITE);
        g2.draw(at2.createTransformedShape(minuteHand));

        // Drawing the hour hand
        float hourHandLen = radius / 3f;
        Shape hourHand = new Line2D.Float(0f, 0f, 0f, -hourHandLen);
        AffineTransform at3 = AffineTransform.getRotateInstance(time.getHour() * Math.PI / 6d);
        g2.setStroke(new BasicStroke(8f));
        g2.draw(at3.createTransformedShape(hourHand));

        // Drawing the second hand
        float r = radius / 6f;
        float secondHandLen = radius - r;
        Shape secondHand = new Line2D.Float(0f, r, 0f, -secondHandLen);
        AffineTransform at1 = AffineTransform.getRotateInstance(time.getSecond() * Math.PI / 30d);
        g2.setPaint(Color.RED);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(at1.createTransformedShape(secondHand));
        g2.fill(new Ellipse2D.Float(-r / 4f, -r / 4f, r / 2f, r / 2f));

        g2.dispose();
      }
    };

    new Timer(200, e -> {
      time = LocalTime.now(ZoneId.systemDefault());
      clock.repaint();
    }).start();

    add(clock);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
