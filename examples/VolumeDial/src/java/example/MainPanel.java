// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // A single BoundedRangeModel is shared by the dial and the slider,
    // so they stay in sync without any extra listener code.
    BoundedRangeModel model = new DefaultBoundedRangeModel(3, 0, 0, 10);

    JSlider slider = new JSlider(model);
    slider.setMajorTickSpacing(1);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);
    slider.setSnapToTicks(true);

    JPanel p = new JPanel(new BorderLayout());
    p.add(slider);
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(p, BorderLayout.NORTH);
    add(new VolumeDial(model, "VOLUME"));
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
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// A volume-knob style dial: the model range is spread over a 270-degree arc
// that starts at the lower left (minimum) and sweeps clockwise to the lower
// right (maximum). The knob never wraps around: turning it counterclockwise
// at the minimum or clockwise at the maximum leaves the value unchanged.
// An LED-style dot sits at every tick outside the ring; the dots up to the
// current value are lit.
@SuppressWarnings("PMD.TooManyMethods")
class VolumeDial extends JPanel {
  // Direction of the minimum value: degrees clockwise from 12 o'clock.
  private static final double START_ANGLE = 225d;
  private static final double SWEEP_ANGLE = 270d;
  private static final double MARGIN = 36d; // room for the LEDs and labels outside the ring
  private static final double KNOB_RATIO = .62; // knob radius relative to the ring radius
  private static final float KNOB_BORDER_WIDTH = 3f;
  private static final float INDICATOR_WIDTH = 4f;
  private static final double INDICATOR_GAP = 4d; // gap between the indicator tip and the rim
  private static final double LED_OFFSET = 7d; // distance from the rim to the LED center
  private static final double LED_RADIUS = 1.75;
  private static final Color LED_COLOR = new Color(0x3C_A0_FF); // light blue
  private static final double LABEL_GAP = 4d; // gap between the LEDs and the label bounds

  private final BoundedRangeModel model;
  private final String title;
  private transient MouseAdapter listener;
  private Color labelColor;
  private boolean dialHovered;
  // Rotation of the knob in degrees within [0, SWEEP_ANGLE], accumulated
  // from pointer movement while dragging so it cannot jump across the gap
  // at the bottom of the dial.
  private double dragAngle;
  private double prevPointerAngle;

  protected VolumeDial(BoundedRangeModel model, String title) {
    super();
    this.model = model;
    this.title = title;
    model.addChangeListener(e -> repaint());
  }

  @Override public void updateUI() {
    removeMouseListener(listener);
    removeMouseMotionListener(listener);
    removeMouseWheelListener(listener);
    super.updateUI();
    // Follow the current LookAndFeel (e.g. dark themes) for the text color.
    labelColor = new JLabel().getForeground();
    listener = new DragMouseListener();
    addMouseListener(listener);
    addMouseMotionListener(listener);
    addMouseWheelListener(listener);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(240, 240);
  }

  public BoundedRangeModel getModel() {
    return model;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g); // clears the background with the theme color
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
    g2.translate(rect.getCenterX(), rect.getCenterY());
    double radius = getRadius(rect);

    // The background is painted by super.paintComponent(...) so the dial
    // follows light/dark themes; derive the other colors from it.
    Color bg = getBackground();
    Color fg = labelColor;

    // Ring: a filled disc behind the knob
    g2.setColor(mix(bg, fg, .55));
    g2.fill(createCircle(new Point2D.Double(), radius));

    paintScale(g2, radius, bg, fg);
    paintKnob(g2, radius, bg, fg);
    g2.dispose();
  }

  // LED-style dots, value labels and the title around the ring.
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private void paintScale(Graphics2D g2, double radius, Color bg, Color fg) {
    // LED-style dots outside the ring, one per step; the dots from the
    // minimum up to the current value are lit, the rest are dimmed.
    double ledDistance = radius + LED_OFFSET;
    Color unlitColor = mix(bg, fg, .25);
    for (int v = model.getMinimum(); v <= model.getMaximum(); v++) {
      g2.setColor(v <= model.getValue() ? LED_COLOR : unlitColor);
      g2.fill(createCircle(toPoint(toAngle(v), ledDistance), LED_RADIUS));
    }

    // Value labels outside the LEDs, one per step; labels up to the current
    // value share the lit LED color.
    FontRenderContext frc = g2.getFontRenderContext();
    double labelDistance = ledDistance + LED_RADIUS;
    for (int v = model.getMinimum(); v <= model.getMaximum(); v++) {
      double deg = toAngle(v);
      g2.setColor(v <= model.getValue() ? LED_COLOR : fg);
      Shape s = new TextLayout(Integer.toString(v), g2.getFont(), frc).getOutline(null);
      g2.fill(createShapeAtPolarPosition(s, deg, labelDistance + getLabelOffset(s, deg)));
    }

    // Title: centered just below the rim, in the gap between the first and
    // the last label.
    g2.setColor(fg);
    Shape ts = new TextLayout(title, g2.getFont(), frc).getOutline(null);
    g2.fill(createShapeAtPolarPosition(ts, 180d, radius + getLabelOffset(ts, 180d)));
  }

  // The indicator mark on the ring and the knob on top of it.
  private void paintKnob(Graphics2D g2, double radius, Color bg, Color fg) {
    // Indicator: a line from the center toward the rim; the knob painted
    // next covers the inner part, leaving a short mark on the ring.
    Point2D tip = toPoint(toAngle(model.getValue()), radius - INDICATOR_GAP);
    g2.setColor(bg);
    g2.setStroke(new BasicStroke(
        INDICATOR_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.draw(new Line2D.Double(0d, 0d, tip.getX(), tip.getY()));

    // Knob: a lighter disc with a border in the background color
    Shape knob = createCircle(new Point2D.Double(), radius * KNOB_RATIO);
    g2.setColor(mix(bg, fg, dialHovered ? .2 : .3));
    g2.fill(knob);
    g2.setColor(bg);
    g2.setStroke(new BasicStroke(KNOB_BORDER_WIDTH));
    g2.draw(knob);
  }

  private static double getRadius(Rectangle rect) {
    return Math.min(rect.width, rect.height) / 2d - MARGIN;
  }

  // Linear interpolation between two colors: t == 0 -> c1, t == 1 -> c2.
  private static Color mix(Color c1, Color c2, double t) {
    int r = (int) Math.round(c1.getRed() + (c2.getRed() - c1.getRed()) * t);
    int g = (int) Math.round(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
    int b = (int) Math.round(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t);
    return new Color(r, g, b);
  }

  private static Shape createCircle(Point2D center, double r) {
    return new Ellipse2D.Double(center.getX() - r, center.getY() - r, 2d * r, 2d * r);
  }

  // Distance from the LED edge to the label center so that the label's inner
  // edge clears the LEDs in the direction of `deg`.
  private static double getLabelOffset(Shape s, double deg) {
    Rectangle2D r = s.getBounds2D();
    double rad = Math.toRadians(deg);
    double half = Math.max(
        Math.abs(Math.cos(rad)) * r.getHeight(),
        Math.abs(Math.sin(rad)) * r.getWidth()) / 2d;
    return LABEL_GAP + half;
  }

  // Polar (degrees, 0 == straight up, clockwise) to Cartesian, origin at the center.
  private static Point2D toPoint(double degrees, double distance) {
    double rad = Math.toRadians(degrees);
    return new Point2D.Double(distance * Math.sin(rad), -distance * Math.cos(rad));
  }

  // Cartesian (origin at the center) to degrees in [0, 360).
  private static double toDegrees(Point2D pt) {
    double deg = Math.toDegrees(Math.atan2(pt.getX(), -pt.getY()));
    return normalize(deg);
  }

  private static double normalize(double degrees) {
    double d = degrees % 360d;
    return d < 0d ? d + 360d : d;
  }

  // Wraps a difference of two angles into [-180, 180) so that the pointer
  // crossing 12 o'clock does not produce a 360-degree jump.
  private static double wrapDelta(double degrees) {
    double d = normalize(degrees);
    return d >= 180d ? d - 360d : d;
  }

  // Translates shape so that its bounding-box center lands on the polar position.
  private static Shape createShapeAtPolarPosition(Shape s, double deg, double distance) {
    Rectangle2D r = s.getBounds2D();
    Point2D pt = toPoint(deg, distance);
    double dx = pt.getX() - r.getCenterX();
    double dy = pt.getY() - r.getCenterY();
    return AffineTransform.getTranslateInstance(dx, dy).createTransformedShape(s);
  }

  private double getDegreesPerStep() {
    return SWEEP_ANGLE / (model.getMaximum() - model.getMinimum());
  }

  // Direction of the given model value: degrees clockwise from 12 o'clock.
  private double toAngle(int value) {
    return START_ANGLE + (value - model.getMinimum()) * getDegreesPerStep();
  }

  // Current knob rotation from the minimum position in degrees, within [0, SWEEP_ANGLE].
  private double getKnobAngle() {
    return (model.getValue() - model.getMinimum()) * getDegreesPerStep();
  }

  // Point relative to the component's inner-area center
  // (used for hit-testing and angle calc).
  private Point2D toCenterRelativePoint(Point p) {
    Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
    return new Point2D.Double(p.getX() - rect.getCenterX(), p.getY() - rect.getCenterY());
  }

  // True if the center-relative point lies on the ring or the knob.
  private boolean isInsideDial(Point2D pt) {
    Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
    return pt.distance(0d, 0d) <= getRadius(rect);
  }

  private final class DragMouseListener extends MouseAdapter {
    @Override public void mouseMoved(MouseEvent e) {
      updateHover(e.getComponent(), isInsideDial(toCenterRelativePoint(e.getPoint())));
    }

    @Override public void mousePressed(MouseEvent e) {
      Point2D pt = toCenterRelativePoint(e.getPoint());
      if (isInsideDial(pt)) {
        updateHover(e.getComponent(), true);
        // Start from the current knob rotation and follow the pointer's
        // relative movement so the knob does not jump to the pointer.
        dragAngle = getKnobAngle();
        prevPointerAngle = toDegrees(pt);
        model.setValueIsAdjusting(true);
      }
    }

    @Override public void mouseDragged(MouseEvent e) {
      if (model.getValueIsAdjusting()) {
        double deg = toDegrees(toCenterRelativePoint(e.getPoint()));
        double delta = wrapDelta(deg - prevPointerAngle);
        prevPointerAngle = deg;
        // Clamping here is what stops the knob at both ends; the value can
        // never wrap from the minimum to the maximum or vice versa.
        dragAngle = Math.max(0d, Math.min(SWEEP_ANGLE, dragAngle + delta));
        // Snap to the nearest tick.
        int step = (int) Math.round(dragAngle / getDegreesPerStep());
        model.setValue(model.getMinimum() + step);
      }
    }

    @Override public void mouseReleased(MouseEvent e) {
      model.setValueIsAdjusting(false);
      mouseMoved(e);
    }

    @Override public void mouseExited(MouseEvent e) {
      if (!model.getValueIsAdjusting()) {
        updateHover(e.getComponent(), false);
      }
    }

    @Override public void mouseWheelMoved(MouseWheelEvent e) {
      model.setValue(model.getValue() - e.getWheelRotation());
      if (model.getValueIsAdjusting()) {
        // Keep an ongoing drag in step with the wheel; otherwise the next
        // drag event would move the knob back to the stale dragAngle.
        dragAngle = getKnobAngle();
      }
    }

    // Repaints and updates the cursor only when the hover state changes,
    // not on every mouse move.
    private void updateHover(Component c, boolean hovered) {
      if (dialHovered != hovered) {
        dialHovered = hovered;
        c.setCursor(hovered
            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            : Cursor.getDefaultCursor());
        c.repaint();
      }
    }
  }
}
