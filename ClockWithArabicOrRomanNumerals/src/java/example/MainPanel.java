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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    AnalogClock clock = new AnalogClock();
    Map<TextAttribute, Object> attr = new ConcurrentHashMap<>();
    attr.put(TextAttribute.TRACKING, -.08f);
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
  private final String[] romanNumerals = {
      "XII", "I", "II", "III", "IIII", "V", "VI", "VII", "VIII", "IX", "X", "XI"
  };
  protected LocalTime time = LocalTime.now(ZoneId.systemDefault());
  protected final Timer timer = new Timer(200, e -> {
    time = LocalTime.now(ZoneId.systemDefault());
    repaint();
  });
  private transient HierarchyListener listener;
  private boolean isRomanNumerals = true;

  // public boolean isRomanNumerals() {
  //   return isRomanNumerals;
  // }

  public void setRomanNumerals(boolean romanNumerals) {
    isRomanNumerals = romanNumerals;
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
    float radius = Math.min(rect.width, rect.height) / 2f - 10f;
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

    // Drawing the clock numbers
    paintClockNumbers(g2, radius, hourMarkerLen);

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

  private void paintClockNumbers(Graphics2D g2, float radius, float hourMarkerLen) {
    AffineTransform at = AffineTransform.getRotateInstance(0d);
    g2.setColor(Color.WHITE);
    Font font = g2.getFont();
    FontRenderContext frc = g2.getFontRenderContext();
    if (isRomanNumerals) {
      for (String txt : romanNumerals) {
        Shape s = moveTo12o(getOutline(txt, font, frc), radius, hourMarkerLen);
        g2.fill(at.createTransformedShape(s));
        at.rotate(Math.PI / 6d);
      }
    } else {
      Point ptSrc = new Point();
      for (int i = 0; i < 12; i++) {
        double ty = radius - hourMarkerLen - font.getSize2D() * .6d;
        AffineTransform at2 = AffineTransform.getTranslateInstance(0d, -ty);
        Point2D pt = at.transform(at2.transform(ptSrc, null), null);
        String txt = i == 0 ? "12" : Objects.toString(i);
        Rectangle r = getOutline(txt, font, frc).getBounds();
        double dx = pt.getX() - r.getCenterX();
        double dy = pt.getY() - r.getCenterY();
        g2.drawString(txt, (float) dx, (float) dy);
        at.rotate(Math.PI / 6d);
      }
    }
  }

  protected Shape moveTo12o(Shape s, double radius, double tick) {
    Rectangle2D r = s.getBounds2D();
    double ty = radius - tick * 2d - r.getHeight();
    AffineTransform at = AffineTransform.getTranslateInstance(-r.getCenterX(), -ty);
    return at.createTransformedShape(s);
  }

  private static Shape getOutline(String txt, Font font, FontRenderContext frc) {
    return new TextLayout(txt, font, frc).getOutline(null);
  }
}
