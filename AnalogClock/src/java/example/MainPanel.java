// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.time.LocalTime;
import java.time.ZoneId;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new AnalogClock());
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class AnalogClock extends JPanel {
  protected LocalTime time = LocalTime.now(ZoneId.systemDefault());
  protected Timer timer = new Timer(200, e -> {
    time = LocalTime.now(ZoneId.systemDefault());
    repaint();
  });
  private transient HierarchyListener listener;

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    listener = e -> {
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
        if (e.getComponent().isShowing()) {
          // System.out.println("start");
          timer.start();
        } else {
          // System.out.println("stop");
          timer.stop();
        }
      }
    };
    addHierarchyListener(listener);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
    g2.setColor(Color.BLACK);
    g2.fill(rect);
    float radius = Math.min(rect.width, rect.height) / 2f - 10f;
    // g2.fill(new Ellipse2D.Double(rect.getCenterX() - r, rect.getCenterY() - r, r * 2f, r * 2f));
    g2.translate(rect.getCenterX(), rect.getCenterY());

    // Drawing the hour markers
    float hourMarkerLen = radius / 6f - 10f;
    Shape hourMarker = new Line2D.Float(0f, hourMarkerLen - radius, 0f, -radius);
    Shape minuteMarker = new Line2D.Float(0f, hourMarkerLen / 2f - radius, 0f, -radius);
    AffineTransform at = AffineTransform.getRotateInstance(0d);
    g2.setStroke(new BasicStroke(2f));
    g2.setColor(Color.WHITE);
    for (int i = 0; i < 60; i++) {
      if (i % 5 == 0) {
        g2.draw(at.createTransformedShape(hourMarker));
      } else {
        g2.draw(at.createTransformedShape(minuteMarker));
      }
      at.rotate(Math.PI / 30d);
    }

    // Drawing the hour hand
    float hourHandLen = radius / 2f;
    Shape hourHand = new Line2D.Float(0f, 0f, 0f, -hourHandLen);
    double minuteRot = time.getMinute() * Math.PI / 30d;
    double hourRot = time.getHour() * Math.PI / 6d + minuteRot / 12d;
    g2.setStroke(new BasicStroke(8f));
    g2.setPaint(Color.LIGHT_GRAY);
    g2.draw(AffineTransform.getRotateInstance(hourRot).createTransformedShape(hourHand));

    // Drawing the minute hand
    float minuteHandLen = 5f * radius / 6f;
    Shape minuteHand = new Line2D.Float(0f, 0f, 0f, -minuteHandLen);
    g2.setStroke(new BasicStroke(4f));
    g2.setPaint(Color.WHITE);
    g2.draw(AffineTransform.getRotateInstance(minuteRot).createTransformedShape(minuteHand));

    // Drawing the second hand
    float r = radius / 6f;
    float secondHandLen = radius - r;
    Shape secondHand = new Line2D.Float(0f, r, 0f, -secondHandLen);
    double secondRot = time.getSecond() * Math.PI / 30d;
    g2.setPaint(Color.RED);
    g2.setStroke(new BasicStroke(1f));
    g2.draw(AffineTransform.getRotateInstance(secondRot).createTransformedShape(secondHand));
    g2.fill(new Ellipse2D.Float(-r / 4f, -r / 4f, r / 2f, r / 2f));

    g2.dispose();
  }
}
