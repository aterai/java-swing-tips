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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // A single BoundedRangeModel is shared by the dial and the slider,
    // so they stay in sync without any extra listener code.
    BoundedRangeModel model = new DefaultBoundedRangeModel(90, 0, 0, 360);
    final AngleDial dial = new AngleDial(model);

    JSlider slider = new JSlider(model);
    slider.setMajorTickSpacing(90);
    slider.setMinorTickSpacing(30);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);

    SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
        model.getValue(), model.getMinimum(), model.getMaximum(), 1);
    final JSpinner spinner = new JSpinner(spinnerModel);
    // Bridge the BoundedRangeModel and the SpinnerNumberModel in both directions.
    // Setting an unchanged value does not fire an event, so this never loops.
    model.addChangeListener(e -> spinnerModel.setValue(model.getValue()));
    spinnerModel.addChangeListener(
        e -> model.setValue(spinnerModel.getNumber().intValue()));

    JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
    controlPanel.add(slider);
    controlPanel.add(spinner, BorderLayout.EAST);
    controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(controlPanel, BorderLayout.NORTH);
    add(dial);
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

// A dial that selects an angle in degrees (0 at the top, clockwise).
// The value is stored in a BoundedRangeModel so it can be shared with a JSlider.
@SuppressWarnings("PMD.TooManyMethods")
class AngleDial extends JPanel {
  private static final double DIAL_MARGIN = 40d; // room for the dots and labels outside the ring
  private static final double HANDLE_RADIUS = 8d;
  private static final double HANDLE_GAP = 4d; // gap between the ring and the handle
  private static final double DOT_GAP = 6d; // distance from the ring to the dot centers
  private static final double MAJOR_DOT_RADIUS = 3d;
  private static final double MINOR_DOT_RADIUS = 1.5;
  private static final double LABEL_GAP = 4d; // gap between the major dots and the labels
  private static final double CENTER_DOT_RADIUS = 2d;
  private static final int TICK_STEP = 30;
  private static final int LABEL_STEP = 90;

  private final BoundedRangeModel model;
  private transient MouseAdapter mouseListener;
  private Color labelColor;
  private boolean handleHovered;
  private boolean dragging;
  private double dragOffset; // degrees: model value minus pointer angle at mousePressed

  protected AngleDial(BoundedRangeModel model) {
    super();
    this.model = model;
    model.addChangeListener(e -> repaint());
  }

  @Override public void updateUI() {
    if (mouseListener != null) {
      removeMouseListener(mouseListener);
      removeMouseMotionListener(mouseListener);
      removeMouseWheelListener(mouseListener);
    }
    super.updateUI();
    // Follow the current LookAndFeel (e.g. dark themes) for the text color.
    labelColor = UIManager.getColor("Label.foreground");
    mouseListener = new DialMouseListener();
    addMouseListener(mouseListener);
    addMouseMotionListener(mouseListener);
    addMouseWheelListener(mouseListener);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(240, 240);
  }

  public BoundedRangeModel getModel() {
    return model;
  }

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g); // clears the background with the theme color
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    Rectangle innerArea = SwingUtilities.calculateInnerArea(this, null);
    // The background is painted by super.paintComponent(...) so the dial
    // follows light/dark themes; derive the other colors from it.
    Color background = getBackground();
    Color foreground = labelColor;
    Color dialColor = interpolateColor(background, foreground, .5);

    g2.translate(innerArea.getCenterX(), innerArea.getCenterY());
    double radius = calculateDialRadius(innerArea);

    // Dial face: a filled disc in the mid color
    g2.setColor(dialColor);
    g2.fill(createCircle(0d, 0d, radius));

    // Tick marks: dots outside the ring, larger at 0/90/180/270
    g2.setColor(foreground);
    for (int degrees = 0; degrees < 360; degrees += TICK_STEP) {
      double dotRadius = degrees % LABEL_STEP == 0 ? MAJOR_DOT_RADIUS : MINOR_DOT_RADIUS;
      Point2D center = polarToCartesian(degrees, radius + DOT_GAP);
      g2.fill(createCircle(center.getX(), center.getY(), dotRadius));
    }

    // Degree labels outside the dots
    FontRenderContext frc = g2.getFontRenderContext();
    for (int degrees = 0; degrees < 360; degrees += LABEL_STEP) {
      Shape labelShape = new TextLayout(degrees + "°", g2.getFont(), frc).getOutline(null);
      g2.fill(positionShapeAtAngle(
          labelShape, degrees, radius + calculateLabelOffset(labelShape, degrees)));
    }

    // Center dot
    g2.setColor(background);
    g2.fill(createCircle(0d, 0d, CENTER_DOT_RADIUS));

    // Handle: a dimple pressed into the dial face, shadow at the top-left,
    // highlight at the bottom-right.
    Ellipse2D handleShape = createHandleShape(innerArea);
    Rectangle2D handleBounds = handleShape.getBounds2D();
    g2.setPaint(new LinearGradientPaint(
        new Point2D.Double(handleBounds.getMinX(), handleBounds.getMinY()),
        new Point2D.Double(handleBounds.getMaxX(), handleBounds.getMaxY()),
        new float[] {0f, .5f, 1f},
        new Color[] {
            interpolateColor(dialColor, Color.BLACK, .45), dialColor,
            interpolateColor(dialColor, Color.WHITE, .45),
        }));
    g2.fill(handleShape);
    g2.setPaint(handleHovered
        ? foreground : interpolateColor(dialColor, Color.BLACK, .3));
    g2.draw(handleShape);

    g2.dispose();
  }

  private static double calculateDialRadius(Rectangle innerArea) {
    return Math.min(innerArea.width, innerArea.height) / 2d - DIAL_MARGIN;
  }

  private static Ellipse2D createCircle(double cx, double cy, double radius) {
    return new Ellipse2D.Double(cx - radius, cy - radius, 2d * radius, 2d * radius);
  }

  // Linear interpolation between two colors: ratio == 0 -> first, ratio == 1 -> second.
  private static Color interpolateColor(Color first, Color second, double ratio) {
    int red = (int) Math.round(
        first.getRed() + (second.getRed() - first.getRed()) * ratio);
    int green = (int) Math.round(
        first.getGreen() + (second.getGreen() - first.getGreen()) * ratio);
    int blue = (int) Math.round(
        first.getBlue() + (second.getBlue() - first.getBlue()) * ratio);
    return new Color(red, green, blue);
  }

  // Distance from the ring to the label center so that the label's inner edge
  // clears the tick dots in the direction of `degrees`.
  private static double calculateLabelOffset(Shape labelShape, double degrees) {
    Rectangle2D bounds = labelShape.getBounds2D();
    double radians = Math.toRadians(degrees);
    double half = Math.max(
        Math.abs(Math.cos(radians)) * bounds.getHeight(),
        Math.abs(Math.sin(radians)) * bounds.getWidth()) / 2d;
    return DOT_GAP + MAJOR_DOT_RADIUS + LABEL_GAP + half;
  }

  // Polar (degrees, 0 == straight up, clockwise) to Cartesian, origin at the center.
  private static Point2D polarToCartesian(double degrees, double distance) {
    double radians = Math.toRadians(degrees);
    return new Point2D.Double(distance * Math.sin(radians), -distance * Math.cos(radians));
  }

  // Cartesian (origin at the center) to degrees in [0, 360).
  private static double cartesianToDegrees(Point2D point) {
    double degrees = Math.toDegrees(Math.atan2(point.getX(), -point.getY()));
    return normalizeDegrees(degrees);
  }

  private static double normalizeDegrees(double degrees) {
    double normalized = degrees % 360d;
    return normalized < 0d ? normalized + 360d : normalized;
  }

  // Translates shape so that its bounding-box center lands on the polar position.
  private static Shape positionShapeAtAngle(Shape shape, double degrees, double distance) {
    Rectangle2D bounds = shape.getBounds2D();
    Point2D point = polarToCartesian(degrees, distance);
    double dx = point.getX() - bounds.getCenterX();
    double dy = point.getY() - bounds.getCenterY();
    return AffineTransform.getTranslateInstance(dx, dy).createTransformedShape(shape);
  }

  // Handle circle for the current model value, relative to the inner-area center.
  // Built on demand so painting and hit-testing never see a stale shape.
  private Ellipse2D createHandleShape(Rectangle innerArea) {
    double distance = calculateDialRadius(innerArea) - HANDLE_GAP - HANDLE_RADIUS;
    Point2D center = polarToCartesian(model.getValue(), distance);
    return createCircle(center.getX(), center.getY(), HANDLE_RADIUS);
  }

  // Hit-test with a point relative to the inner-area center.
  private boolean isOnHandle(Point2D point) {
    Rectangle innerArea = SwingUtilities.calculateInnerArea(this, null);
    return createHandleShape(innerArea).contains(point);
  }

  // Point relative to the component's inner-area center
  // (used for hit-testing and angle calc).
  private Point2D toCenterRelative(Point point) {
    Rectangle innerArea = SwingUtilities.calculateInnerArea(this, null);
    return new Point2D.Double(
        point.getX() - innerArea.getCenterX(), point.getY() - innerArea.getCenterY());
  }

  // Updates the hover state, cursor and painting only when the state changes.
  private void setHandleHovered(boolean hovered) {
    if (handleHovered != hovered) {
      handleHovered = hovered;
      setCursor(hovered
          ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
          : Cursor.getDefaultCursor());
      repaint();
    }
  }

  private final class DialMouseListener extends MouseAdapter {
    @Override public void mouseEntered(MouseEvent e) {
      mouseMoved(e);
    }

    @Override public void mouseMoved(MouseEvent e) {
      setHandleHovered(isOnHandle(toCenterRelative(e.getPoint())));
    }

    @Override public void mousePressed(MouseEvent e) {
      Point2D point = toCenterRelative(e.getPoint());
      if (SwingUtilities.isLeftMouseButton(e) && isOnHandle(point)) {
        dragging = true;
        setHandleHovered(true);
        // Remember the offset so the handle does not jump to the pointer.
        dragOffset = model.getValue() - cartesianToDegrees(point);
        model.setValueIsAdjusting(true);
      }
    }

    @Override public void mouseDragged(MouseEvent e) {
      if (dragging) {
        Point2D point = toCenterRelative(e.getPoint());
        double degrees = normalizeDegrees(cartesianToDegrees(point) + dragOffset);
        model.setValue((int) Math.round(degrees));
      }
    }

    @Override public void mouseReleased(MouseEvent e) {
      if (dragging && SwingUtilities.isLeftMouseButton(e)) {
        dragging = false;
        model.setValueIsAdjusting(false);
      }
      mouseMoved(e);
    }

    @Override public void mouseExited(MouseEvent e) {
      if (!dragging) {
        setHandleHovered(false);
      }
    }

    @Override public void mouseWheelMoved(MouseWheelEvent e) {
      // Wrap around like dragging does: 359 -> 0 and 0 -> 359.
      double degrees = normalizeDegrees(model.getValue() - e.getWheelRotation());
      model.setValue((int) degrees);
    }
  }
}
