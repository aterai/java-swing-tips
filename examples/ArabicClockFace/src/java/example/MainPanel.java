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
    attr.put(TextAttribute.TRACKING, TextAttribute.TRACKING_TIGHT);
    clock.setFont(clock.getFont().deriveFont(32f).deriveFont(attr));

    JCheckBox check = new JCheckBox("rotate", true);
    check.addActionListener(e -> {
      Object src = e.getSource();
      if (src instanceof JCheckBox) {
        clock.setRotate(((JCheckBox) src).isSelected());
        clock.repaint();
      }
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
  private static final int MINUTES_PER_HOUR = 60;
  private static final int MIN_PER_MARKER = 5;
  private static final double RAD_PER_MIN = 2d * Math.PI / MINUTES_PER_HOUR;
  private static final float FONT_RATIO = .2f;
  private static final String[] ARABIC_NUMERALS = {
      "12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
  };
  // private static final String[] EARTHLY_BRANCHES = {
  //     "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥",
  // };
  private double secondRot;
  private double minuteRot;
  private double hourRot;
  private final Timer timer = new Timer(200, e -> {
    LocalTime time = LocalTime.now(ZoneId.systemDefault());
    // Calculate the angle of rotation
    secondRot = time.getSecond() * Math.PI / 30d;
    minuteRot = time.getMinute() * Math.PI / 30d + secondRot / 60d;
    hourRot = time.getHour() * Math.PI / 6d + minuteRot / 12d;
    repaint();
  });
  private transient HierarchyListener listener;
  private boolean isRotate = true;

  public void setRotate(boolean rotate) {
    isRotate = rotate;
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
    initRenderingHints(g2);
    Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
    paintBackground(g2, rect);
    double radius = Math.min(rect.width, rect.height) / 2d - 10d;
    g2.translate(rect.getCenterX(), rect.getCenterY());

    paintHourMarkers(g2, radius);
    paintClockNumbers(g2, radius);
    paintHourHand(g2, radius, hourRot);
    paintMinuteHand(g2, radius, minuteRot);
    paintSecondHand(g2, radius, secondRot);

    g2.dispose();
  }

  private static void initRenderingHints(Graphics2D g2) {
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
  }

  private static void paintBackground(Graphics2D g2, Rectangle rect) {
    g2.setColor(Color.BLACK);
    g2.fill(rect);
  }

  private static void paintHourMarkers(Graphics2D g2, double radius) {
    double hourMarkerLen = radius / 6d - 10d;
    Shape hourMarker = new Line2D.Double(0d, hourMarkerLen - radius, 0d, -radius);
    Shape minuteMarker = new Line2D.Double(0d, hourMarkerLen / 2d - radius, 0d, -radius);
    AffineTransform at = AffineTransform.getRotateInstance(0d);
    g2.setStroke(new BasicStroke(2f));
    g2.setColor(Color.WHITE);
    for (int i = 0; i < MINUTES_PER_HOUR; i++) {
      if (i % MIN_PER_MARKER == 0) {
        g2.draw(at.createTransformedShape(hourMarker));
      } else {
        g2.draw(at.createTransformedShape(minuteMarker));
      }
      at.rotate(RAD_PER_MIN);
    }
  }

  private void paintClockNumbers(Graphics2D g2, double radius) {
    float dynamicFontSize = Math.max((float) (radius * FONT_RATIO), 32f);
    Font font = g2.getFont().deriveFont(dynamicFontSize);
    FontRenderContext frc = g2.getFontRenderContext();

    double hourMarkerLen = radius / 6d - 10d;
    AffineTransform at = AffineTransform.getRotateInstance(0d);
    g2.setColor(Color.WHITE);
    if (isRotate) {
      for (String txt : ARABIC_NUMERALS) {
        double m00 = at.getScaleX();
        double d = m00 > 0d || Math.abs(m00) < 0.0001 ? 1d : -1d;
        AffineTransform si = AffineTransform.getScaleInstance(d, d);
        Shape s = getTextLayout(txt, font, frc).getOutline(si);
        Rectangle2D r = s.getBounds2D();
        double tx = r.getCenterX();
        double ty = radius - hourMarkerLen - r.getHeight() + r.getCenterY();
        Shape t = AffineTransform.getTranslateInstance(-tx, -ty).createTransformedShape(s);
        g2.fill(at.createTransformedShape(t));
        at.rotate(Math.PI / 6d);
      }
    } else {
      Point2D ptSrc = new Point2D.Double();
      for (String txt : ARABIC_NUMERALS) {
        Shape s = getTextLayout(txt, font, frc).getOutline(null);
        Rectangle2D r = s.getBounds2D();
        double ty = radius - hourMarkerLen - r.getHeight();
        ptSrc.setLocation(0d, -ty);
        Point2D pt = at.transform(ptSrc, null);
        double dx = pt.getX() - r.getCenterX();
        double dy = pt.getY() - r.getCenterY();
        g2.fill(AffineTransform.getTranslateInstance(dx, dy).createTransformedShape(s));
        at.rotate(Math.PI / 6d);
      }
    }
  }

  private static TextLayout getTextLayout(String txt, Font font, FontRenderContext frc) {
    return new TextLayout(txt, font, frc);
  }

  private static void paintHourHand(Graphics2D g2, double radius, double hourRot) {
    double hourHandLen = radius / 2d;
    Shape hourHand = new Line2D.Double(0d, 0d, 0d, -hourHandLen);
    paintHand(g2, hourHand, 8f, Color.LIGHT_GRAY, hourRot);
  }

  private static void paintMinuteHand(Graphics2D g2, double radius, double minuteRot) {
    double minuteHandLen = 5d * radius / 6d;
    Shape minuteHand = new Line2D.Double(0d, 0d, 0d, -minuteHandLen);
    paintHand(g2, minuteHand, 4f, Color.WHITE, minuteRot);
  }

  private static void paintSecondHand(Graphics2D g2, double radius, double secondRot) {
    double r = radius / 6d;
    double secondHandLen = radius - r;
    Shape secondHand = new Line2D.Double(0d, r, 0d, -secondHandLen);
    paintHand(g2, secondHand, 1f, Color.RED, secondRot);
    g2.fill(new Ellipse2D.Double(-r / 4d, -r / 4d, r / 2d, r / 2d));
  }

  private static void paintHand(
      Graphics2D g2, Shape hand, float strokeWidth, Color color, double rot) {
    g2.setStroke(new BasicStroke(strokeWidth));
    g2.setPaint(color);
    g2.draw(AffineTransform.getRotateInstance(rot).createTransformedShape(hand));
  }
}
