// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.MaskFormatter;

public final class MainPanel extends JPanel {
  private final TimePicker hourPicker;
  private final TimePicker minutePicker;
  private final JRadioButton amButton;
  private final JRadioButton pmButton;
  private final transient DateTimeFormatter timeFormatter;
  private LocalTime currentTime = LocalTime.now(ZoneId.systemDefault());
  private JFormattedTextField timeField;
  private boolean isSyncing;

  private MainPanel() {
    super(new BorderLayout());
    hourPicker = new TimePicker(); // 0=12 o'clock, 1=1 o'clock
    minutePicker = new TimePicker(TimePicker.MINUTE_LABELS, Math.PI / 30d);
    timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    String[] amPmStrings = DateFormatSymbols.getInstance().getAmPmStrings();
    amButton = new JRadioButton(amPmStrings[0]);
    pmButton = new JRadioButton(amPmStrings[1]);

    try {
      MaskFormatter mask = new MaskFormatter("##:##");
      mask.setPlaceholderCharacter('0');
      timeField = new JFormattedTextField(mask);
    } catch (ParseException ex) {
      timeField = new JFormattedTextField();
    }
    timeField.setFont(timeField.getFont().deriveFont(Font.BOLD, 32f));
    timeField.setHorizontalAlignment(JTextField.CENTER);
    timeField.setFocusable(true);

    JPanel p = new JPanel(new GridLayout(1, 2));
    p.add(hourPicker);
    p.add(minutePicker);

    Box box = Box.createHorizontalBox();
    box.add(timeField);
    box.add(createAmPmBox());

    add(box, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));

    hourPicker.addChangeListener(e -> onHourOrAmPmChanged());
    minutePicker.addChangeListener(
        e -> applyTime(currentTime.withMinute(minutePicker.getSelectedIndex())));
    ActionListener amPmListener = e -> onHourOrAmPmChanged();
    amButton.addActionListener(amPmListener);
    pmButton.addActionListener(amPmListener);
    timeField.addPropertyChangeListener("value", e -> onTimeFieldValueChanged());

    applyTime(currentTime);
  }

  private Container createAmPmBox() {
    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(amButton);
    p.add(pmButton);
    p.setBackground(timeField.getBackground());
    ButtonGroup buttonGroup = new ButtonGroup();
    for (JRadioButton radioButton : Arrays.asList(amButton, pmButton)) {
      buttonGroup.add(radioButton);
      radioButton.setOpaque(false);
    }
    return p;
  }

  // Combines hourPicker's 0..11 index with the AM/PM state to build the 24-hour hour.
  private void onHourOrAmPmChanged() {
    int hour24 = hourPicker.getSelectedIndex() + (pmButton.isSelected() ? 12 : 0);
    applyTime(currentTime.withHour(hour24));
  }

  private void onTimeFieldValueChanged() {
    if (!isSyncing) {
      try {
        applyTime(LocalTime.parse((String) timeField.getValue(), timeFormatter));
      } catch (DateTimeParseException ex) {
        // On invalid input, revert to the previous value.
        timeField.setValue(currentTime.format(timeFormatter));
      }
    }
  }

  // Reflects currentTime, the single source of truth, in all UI components
  // (hourPicker/minutePicker/AM-PM/timeField).
  private void applyTime(LocalTime time) {
    if (!isSyncing) {
      isSyncing = true;
      try {
        currentTime = time;
        hourPicker.setSelectedIndex(time.getHour() % 12);
        minutePicker.setSelectedIndex(time.getMinute());
        (time.getHour() < 12 ? amButton : pmButton).setSelected(true);
        timeField.setValue(time.format(timeFormatter));
      } finally {
        isSyncing = !isSyncing;
      }
    }
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

class TimePicker extends JPanel {
  protected static final String[] HOUR_LABELS = {
      "12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
  };
  // 5-minute increments so the 60 snap positions don't force 60 overlapping labels.
  protected static final String[] MINUTE_LABELS = {
      "0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55",
  };
  // Ratio of handle diameter to radius (radius / 4d)
  protected static final double HANDLE_SIZE_RATIO = .25d;

  private static final double MARGIN = 10d;
  private static final Color BORDER_CLR = Color.LIGHT_GRAY;
  private static final Color HOVER_BORDER_CLR = Color.WHITE;

  private final String[] labels;
  // Angle between adjacent snap positions, independent of how many labels are drawn.
  // e.g. hours: Math.PI / 6d (12 positions). minutes: Math.PI / 30d (60 positions).
  private final double stepAngle;

  private transient MouseAdapter listener;
  private double rotation = Math.toRadians(90d);
  private double dragStartAngle;
  private int dragStartIndex; // selected index captured at mousePressed
  private boolean handleHovered;
  private final RectangularShape handleShape = new Ellipse2D.Double();

  protected TimePicker() {
    this(HOUR_LABELS, Math.PI / 6d);
  }

  protected TimePicker(String[] labels, double stepAngle) {
    super();
    this.labels = labels.clone();
    this.stepAngle = stepAngle;
    EventQueue.invokeLater(() -> updateHandleShape(rotation));
  }

  @Override public void updateUI() {
    removeMouseListener(listener);
    removeMouseMotionListener(listener);
    super.updateUI();
    listener = new DragMouseListener();
    addMouseListener(listener);
    addMouseMotionListener(listener);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(200, 200);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
    g2.setColor(Color.DARK_GRAY);
    g2.fill(rect);

    g2.translate(rect.getCenterX(), rect.getCenterY());
    double radius = DialGeometry.getRadius(rect, MARGIN);

    // Drawing the clock numbers
    DialGeometry.paintClockNumbers(g2, radius, labels);

    Color borderColor = handleHovered ? HOVER_BORDER_CLR : BORDER_CLR;
    Point2D edge = getHandCircleEdgePoint();
    g2.setColor(borderColor);
    g2.draw(new Line2D.Double(0d, 0d, edge.getX(), edge.getY()));
    g2.fill(new Ellipse2D.Double(-2d, -2d, 4d, 4d));

    g2.setColor(new Color(0x64_AA_AA_FF, true));
    g2.fill(handleShape);
    g2.setColor(borderColor);
    g2.draw(handleShape);

    g2.dispose();
  }

  @Override public void doLayout() {
    super.doLayout();
    updateHandleShape(rotation);
  }

  // Returns the currently selected snap position, in [0, stepCount).
  public int getSelectedIndex() {
    int stepCount = DialGeometry.getStepCount(stepAngle);
    int index = (int) Math.round(DialGeometry.normalizeAngle(rotation) / stepAngle);
    return index % stepCount;
  }

  // Sets the selected snap position; index is wrapped into [0, stepCount).
  public void setSelectedIndex(int index) {
    int stepCount = DialGeometry.getStepCount(stepAngle);
    int normalizedIndex = ((index % stepCount) + stepCount) % stepCount;
    if (normalizedIndex != getSelectedIndex()) {
      rotation = normalizedIndex * stepAngle;
      updateHandleShape(rotation);
      repaint();
      fireStateChanged();
    }
  }

  public void addChangeListener(ChangeListener l) {
    listenerList.add(ChangeListener.class, l);
  }

  public void removeChangeListener(ChangeListener l) {
    listenerList.remove(ChangeListener.class, l);
  }

  protected void fireStateChanged() {
    ChangeEvent event = new ChangeEvent(this);
    for (ChangeListener l : listenerList.getListeners(ChangeListener.class)) {
      l.stateChanged(event);
    }
  }

  // Point where the segment from the clock center (origin) to handleShape's center
  // crosses handleShape's own boundary (i.e. the near edge of the circle).
  private Point2D getHandCircleEdgePoint() {
    double cx = handleShape.getCenterX();
    double cy = handleShape.getCenterY();
    double dist = Math.hypot(cx, cy);
    double r = handleShape.getWidth() / 2d;
    Point2D pt = new Point2D.Double(0d, 0d);
    if (dist > r) {
      double scale = (dist - r) / dist;
      pt.setLocation(cx * scale, cy * scale);
    }
    return pt;
  }

  private void updateHandleShape(double angle) {
    Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
    double radius = DialGeometry.getRadius(rect, MARGIN);

    // Calculates the diameter of the handle circle shape.
    double handSize = radius * HANDLE_SIZE_RATIO;

    // By creating a circle whose center is the origin (0,0) from the beginning,
    // we prevent the center from shifting when rotating or moving.
    Shape s = new Ellipse2D.Double(
        -handSize / 2d, -handSize / 2d, handSize, handSize);
    AffineTransform at = AffineTransform.getRotateInstance(angle);

    // Exact distance to align center points (radius minus 1/8 radius)
    double distance = DialGeometry.getHandleDistance(radius, HANDLE_SIZE_RATIO);

    Shape transformedShape = DialGeometry.createShapeAtPolarPosition(s, at, distance);
    handleShape.setFrame(transformedShape.getBounds2D());
  }

  private final class DragMouseListener extends MouseAdapter {
    @Override public void mouseMoved(MouseEvent e) {
      Component c = e.getComponent();
      if (c instanceof JComponent) {
        Point2D pt = DialGeometry.toCenterRelativePoint((JComponent) c, e.getPoint());
        handleHovered = handleShape.contains(pt);
      }
      c.repaint();
    }

    @Override public void mouseReleased(MouseEvent e) {
      if (handleHovered) {
        rotation = DialGeometry.snapToNearestStep(rotation, stepAngle);
        updateHandleShape(rotation);
        if (getSelectedIndex() != dragStartIndex) {
          fireStateChanged();
        }
      }
      handleHovered = false;
      e.getComponent().repaint();
    }

    @Override public void mousePressed(MouseEvent e) {
      JComponent c = (JComponent) e.getComponent();
      Point2D pt = DialGeometry.toCenterRelativePoint(c, e.getPoint());
      if (handleShape.contains(pt)) {
        handleHovered = true;
        dragStartIndex = getSelectedIndex();
        dragStartAngle = rotation - Math.atan2(pt.getY(), pt.getX());
        c.repaint();
      }
    }

    @Override public void mouseDragged(MouseEvent e) {
      if (handleHovered) {
        JComponent c = (JComponent) e.getComponent();
        Point2D pt = DialGeometry.toCenterRelativePoint(c, e.getPoint());
        rotation = dragStartAngle + Math.atan2(pt.getY(), pt.getX());
        updateHandleShape(rotation);
        c.repaint();
        // Fire on every drag move (not just when the snapped index changes)
        // so listeners can track the pointer's current position in real time,
        // e.g. a live preview label.
        fireStateChanged();
      }
    }
  }
}

// Stateless geometry/rendering helpers used by TimePicker, split out to keep
// TimePicker's own method count down.
final class DialGeometry {
  private static final float FONT_RATIO = .18f;

  private DialGeometry() {
    /* Utility class */
  }

  public static double getRadius(Rectangle rect, double margin) {
    return Math.min(rect.width, rect.height) / 2d - margin;
  }

  // Calculates the common distance from the clock center where the handle
  // and text centers are located.
  public static double getHandleDistance(double radius, double ratio) {
    return radius * (1d - ratio / 2d);
  }

  public static double normalizeAngle(double angle) {
    double twoPi = 2d * Math.PI;
    double a = angle % twoPi;
    return a < 0d ? a + twoPi : a;
  }

  // Rounds an angle (radians) to the nearest snap position, normalized to [0, 2π).
  public static double snapToNearestStep(double angle, double stepAngle) {
    double stepped = Math.round(angle / stepAngle) * stepAngle;
    return normalizeAngle(stepped);
  }

  // Number of discrete snap positions around the dial.
  // (12 for hours, 60 for minutes, ...)
  public static int getStepCount(double stepAngle) {
    return (int) Math.round(2d * Math.PI / stepAngle);
  }

  // Translates shape so that its bounding-box center lands on the point that is
  // `distance` away from the origin, rotated by `at` (0 rad == straight up).
  public static Shape createShapeAtPolarPosition(
      Shape s, AffineTransform at, double distance) {
    Rectangle2D r = s.getBounds2D();
    Point2D ptSrc = new Point2D.Double(0d, -distance);
    Point2D pt = at.transform(ptSrc, null);
    double dx = pt.getX() - r.getCenterX();
    double dy = pt.getY() - r.getCenterY();
    return AffineTransform.getTranslateInstance(dx, dy).createTransformedShape(s);
  }

  public static void paintClockNumbers(Graphics2D g2, double radius, String... labels) {
    g2.setColor(Color.WHITE);
    float dynamicFontSize = Math.max((float) (radius * FONT_RATIO), 10f);
    Font font = g2.getFont().deriveFont(dynamicFontSize);
    FontRenderContext frc = g2.getFontRenderContext();

    // Use the same radial distance as the handle's center (TimePicker.HANDLE_SIZE_RATIO
    // = 0.25) so the numbers align with it.
    double ty = getHandleDistance(radius, TimePicker.HANDLE_SIZE_RATIO);
    double labelAngle = 2d * Math.PI / labels.length;
    AffineTransform at = AffineTransform.getRotateInstance(0d);

    for (String txt : labels) {
      Shape s = getTextLayout(txt, font, frc).getOutline(null);
      // createShapeAtPolarPosition centers each glyph's bounding box at ty, so both
      // single- and double-digit numbers line up exactly with the handle's center.
      g2.fill(createShapeAtPolarPosition(s, at, ty));
      at.rotate(labelAngle);
    }
  }

  private static TextLayout getTextLayout(String txt, Font font, FontRenderContext frc) {
    return new TextLayout(txt, font, frc);
  }

  // Point relative to the component's inner-area center
  // (used for hit-testing and angle calc).
  public static Point2D toCenterRelativePoint(JComponent c, Point p) {
    Rectangle rect = SwingUtilities.calculateInnerArea(c, null);
    double x = p.getX() - rect.getCenterX();
    double y = p.getY() - rect.getCenterY();
    return new Point2D.Double(x, y);
  }
}
