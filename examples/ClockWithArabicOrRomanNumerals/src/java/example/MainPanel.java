// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    AnalogClock clock = new AnalogClock();
    Map<TextAttribute, Object> attr = new ConcurrentHashMap<>();
    // attr.put(TextAttribute.TRACKING, TextAttribute.TRACKING_TIGHT);
    attr.put(TextAttribute.TRACKING, -.08f);
    // AffineTransform at = AffineTransform.getScaleInstance(1d, 2d);
    // clock.setFont(clock.getFont().deriveFont(20f).deriveFont(attr).deriveFont(at));
    clock.setFont(clock.getFont().deriveFont(20f).deriveFont(attr));

    JCheckBox check = new JCheckBox("roman", true);
    check.addActionListener(e -> {
      clock.setRomanNumerals(((JCheckBox) e.getSource()).isSelected());
      clock.repaint();
    });

    add(clock);
    add(check, BorderLayout.SOUTH);
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
      Logger.getGlobal().severe(ex::getMessage);
      return;
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
  private final String[] arabicNumerals = {
      "12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"
  };
  private final String[] romanNumerals = {
      "XII", "I", "II", "III", "IIII", "V", "VI", "VII", "VIII", "IX", "X", "XI"
  };
  private LocalTime time = LocalTime.now(ZoneId.systemDefault());
  private final Timer timer = new Timer(200, e -> {
    time = LocalTime.now(ZoneId.systemDefault());
    repaint();
  });
  private transient HierarchyListener listener;
  private boolean isRomanNumerals = true;

  // public boolean isRomanNumerals() {
  //   return isRomanNumerals;
  // }

  public void setRomanNumerals(boolean b) {
    isRomanNumerals = b;
  }

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    listener = e -> {
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
        if (e.getComponent().isShowing()) {
          timer.start();
        } else {
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
    double radius = Math.min(rect.width, rect.height) / 2d - 10d;
    g2.translate(rect.getCenterX(), rect.getCenterY());

    // Drawing the hour markers
    double hourMarkerLen = radius / 6d - 10d;
    Shape hourMarker = new Line2D.Double(0d, hourMarkerLen - radius, 0d, -radius);
    Shape minuteMarker = new Line2D.Double(0d, hourMarkerLen / 2d - radius, 0d, -radius);
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

    // Drawing the clock numbers
    paintClockNumbers(g2, radius, hourMarkerLen);

    // Calculate the angle of rotation
    double secondRot = time.getSecond() * Math.PI / 30d;
    double minuteRot = time.getMinute() * Math.PI / 30d + secondRot / 60d;
    double hourRot = time.getHour() * Math.PI / 6d + minuteRot / 12d;

    // Drawing the hour hand
    double hourHandLen = radius / 2d;
    Shape hourHand = new Line2D.Double(0d, 0d, 0d, -hourHandLen);
    g2.setStroke(new BasicStroke(8f));
    g2.setPaint(Color.LIGHT_GRAY);
    g2.draw(AffineTransform.getRotateInstance(hourRot).createTransformedShape(hourHand));

    // Drawing the minute hand
    double minuteHandLen = 5d * radius / 6d;
    Shape minuteHand = new Line2D.Double(0d, 0d, 0d, -minuteHandLen);
    g2.setStroke(new BasicStroke(4f));
    g2.setPaint(Color.WHITE);
    g2.draw(AffineTransform.getRotateInstance(minuteRot).createTransformedShape(minuteHand));

    // Drawing the second hand
    double r = radius / 6d;
    double secondHandLen = radius - r;
    Shape secondHand = new Line2D.Double(0d, r, 0d, -secondHandLen);
    g2.setPaint(Color.RED);
    g2.setStroke(new BasicStroke(1f));
    g2.draw(AffineTransform.getRotateInstance(secondRot).createTransformedShape(secondHand));
    g2.fill(new Ellipse2D.Double(-r / 4d, -r / 4d, r / 2d, r / 2d));

    g2.dispose();
  }

  private void paintClockNumbers(Graphics2D g2, double radius, double hourMarkerLen) {
    AffineTransform at = AffineTransform.getRotateInstance(0d);
    g2.setColor(Color.WHITE);
    Font font = g2.getFont();
    FontRenderContext frc = g2.getFontRenderContext();
    if (isRomanNumerals) {
      AffineTransform si = AffineTransform.getScaleInstance(1d, 2d);
      for (String txt : romanNumerals) {
        Shape s = getTextLayout(txt, font, frc).getOutline(si);
        Rectangle2D r = s.getBounds2D();
        double tx = r.getCenterX();
        double ty = radius - hourMarkerLen - r.getHeight() + r.getCenterY() * .5;
        Shape t = AffineTransform.getTranslateInstance(-tx, -ty).createTransformedShape(s);
        g2.fill(at.createTransformedShape(t));
        at.rotate(Math.PI / 6d);
      }
    } else {
      Point2D ptSrc = new Point2D.Double();
      for (String txt : arabicNumerals) {
        Shape s = getTextLayout(txt, font, frc).getOutline(null);
        Rectangle2D r = s.getBounds2D();
        double ty = radius - hourMarkerLen - r.getHeight() - r.getCenterY() * .5;
        ptSrc.setLocation(0d, -ty);
        Point2D pt = at.transform(ptSrc, null);
        double dx = pt.getX() - r.getCenterX();
        double dy = pt.getY() - r.getCenterY();
        // g2.drawString(txt, (float) dx, (float) dy);
        g2.fill(AffineTransform.getTranslateInstance(dx, dy).createTransformedShape(s));
        at.rotate(Math.PI / 6d);
      }
    }
  }

  private static TextLayout getTextLayout(String txt, Font font, FontRenderContext frc) {
    return new TextLayout(txt, font, frc);
  }
}
