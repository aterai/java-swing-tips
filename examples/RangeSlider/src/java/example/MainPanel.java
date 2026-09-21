// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridBagLayout());
    add(new RangeSliderPanel(0, 100, 25, 75));
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

/**
 * A slider UI that paints only a small triangular thumb pointing up or down.
 * The track, ticks and labels are painted by the {@link RangeBar} instead.
 */
class TriangleSliderUI extends BasicSliderUI {
  private static final Color THUMB_COLOR = new Color(0x28_2C_34);
  private static final int THUMB_WIDTH = 11;
  private static final int THUMB_HEIGHT = 10;
  private static final int TRIANGLE_HEIGHT = 8;
  private final boolean upward;

  protected TriangleSliderUI(JSlider slider, boolean upward) {
    super(slider);
    this.upward = upward;
  }

  @Override protected void installDefaults(JSlider slider) {
    super.installDefaults(slider);
    // Some LookAndFeels (e.g. Windows) reserve 2px focus insets, which would
    // shift the thumb positions away from the values painted on the RangeBar.
    focusInsets = new Insets(0, 0, 0, 0);
  }

  @Override protected Dimension getThumbSize() {
    return new Dimension(THUMB_WIDTH, THUMB_HEIGHT);
  }

  @Override protected void calculateTrackBuffer() {
    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      // Share the horizontal track range with the RangeBar
      trackBuffer = RangeBar.TRACK_PADDING;
    } else {
      super.calculateTrackBuffer();
    }
  }

  @Override public void paintTrack(Graphics g) {
    // nothing to paint
  }

  @Override public void paintFocus(Graphics g) {
    // nothing to paint
  }

  @Override public void paintThumb(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(THUMB_COLOR);
    // Anchor the apex to the edge of the thumb rectangle facing the RangeBar
    double apexY = upward ? thumbRect.getMinY() : thumbRect.getMaxY();
    double baseY = upward ? apexY + TRIANGLE_HEIGHT : apexY - TRIANGLE_HEIGHT;
    Path2D triangle = new Path2D.Double();
    triangle.moveTo(thumbRect.getMinX(), baseY);
    triangle.lineTo(thumbRect.getCenterX(), apexY);
    triangle.lineTo(thumbRect.getMaxX(), baseY);
    triangle.closePath();
    g2.fill(triangle);
    g2.dispose();
  }
}

class RangeSliderPanel extends JPanel {
  private final JSlider lowerSlider;
  private final JSlider upperSlider;

  protected RangeSliderPanel(int min, int max, int lowerValue, int upperValue) {
    super(new BorderLayout());
    lowerSlider = createSlider(min, max, lowerValue, true);
    upperSlider = createSlider(min, max, upperValue, false);
    RangeBar rangeBar = new RangeBar(lowerSlider, upperSlider);

    ChangeListener listener = e -> {
      clampToOtherSlider(e.getSource());
      rangeBar.repaint();
    };
    lowerSlider.addChangeListener(listener);
    upperSlider.addChangeListener(listener);

    add(upperSlider, BorderLayout.NORTH);
    add(rangeBar);
    add(lowerSlider, BorderLayout.SOUTH);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  // Keep lower <= upper: the slider being moved stops at the other one
  private void clampToOtherSlider(Object source) {
    int lower = lowerSlider.getValue();
    int upper = upperSlider.getValue();
    if (lower > upper) {
      if (Objects.equals(source, lowerSlider)) {
        lowerSlider.setValue(upper);
      } else {
        upperSlider.setValue(lower);
      }
    }
  }

  private static JSlider createSlider(int min, int max, int value, boolean upward) {
    return new JSlider(min, max, value) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new TriangleSliderUI(this, upward));
        setOpaque(false);
      }
    };
  }
}

/**
 * Paints the track, the tick marks, the selected range and its values
 * between the two sliders, and lets the user drag the whole range.
 */
class RangeBar extends JLabel {
  public static final int BAR_HEIGHT = 24;
  public static final int TRACK_PADDING = 20;
  private static final int MAJOR_TICK_STEP = 10;
  private static final int MINOR_TICK_STEP = 2;
  private static final int MINOR_TICK_LENGTH = 8;
  private static final int TEXT_GAP = 2;
  private static final float ARC = 4f;
  private static final Color MAJOR_TICK_COLOR = new Color(0xB4_B4_B9);
  private static final Color MINOR_TICK_COLOR = new Color(0xD2_D2_D7);
  private static final Color TRACK_COLOR = new Color(0xE6_E6_EB);
  private static final Color RANGE_COLOR = new Color(0x78_00_B4_FF, true);
  private final JSlider lowerSlider;
  private final JSlider upperSlider;
  private transient MouseAdapter mouseListener;
  private boolean dragging;
  private boolean hovering;
  private int dragStartX;
  private int lowerAtDragStart;
  private int upperAtDragStart;

  protected RangeBar(JSlider lowerSlider, JSlider upperSlider) {
    super();
    this.lowerSlider = lowerSlider;
    this.upperSlider = upperSlider;
  }

  @Override public void updateUI() {
    removeMouseListener(mouseListener);
    removeMouseMotionListener(mouseListener);
    super.updateUI();
    mouseListener = new RangeMouseListener();
    addMouseListener(mouseListener);
    addMouseMotionListener(mouseListener);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(300, BAR_HEIGHT);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Rectangle track = getTrackBounds();
    paintTrack(g2, track);
    paintTicks(g2, track);
    Rectangle range = getRangeBounds();
    paintRange(g2, range);
    paintValues(g2, range);
    g2.dispose();
  }

  private static void paintTrack(Graphics2D g2, Rectangle track) {
    Shape shape = new RoundRectangle2D.Double(
        track.x, track.y, track.width, track.height, ARC, ARC);
    g2.setColor(TRACK_COLOR);
    g2.fill(shape);
    g2.setColor(TRACK_COLOR.darker());
    g2.draw(shape);
  }

  private void paintTicks(Graphics2D g2, Rectangle track) {
    int min = lowerSlider.getMinimum();
    int max = lowerSlider.getMaximum();
    int minorTop = track.y + (track.height - MINOR_TICK_LENGTH) / 2;
    for (int value = min; value <= max; value += MINOR_TICK_STEP) {
      int x = valueToX(value);
      if ((value - min) % MAJOR_TICK_STEP == 0) {
        g2.setColor(MAJOR_TICK_COLOR);
        g2.drawLine(x, track.y, x, track.y + track.height);
      } else {
        g2.setColor(MINOR_TICK_COLOR);
        g2.drawLine(x, minorTop, x, minorTop + MINOR_TICK_LENGTH);
      }
    }
  }

  private static void paintRange(Graphics2D g2, Rectangle range) {
    Shape shape = new RoundRectangle2D.Double(
        range.x, range.y, range.width, range.height, ARC, ARC);
    g2.setColor(RANGE_COLOR);
    g2.fill(shape);
    g2.setColor(RANGE_COLOR.darker());
    g2.draw(shape);
  }

  private void paintValues(Graphics2D g2, Rectangle range) {
    g2.setColor(getForeground());
    String lowerText = String.valueOf(lowerSlider.getValue());
    String upperText = String.valueOf(upperSlider.getValue());
    FontMetrics fm = g2.getFontMetrics();
    // Center the text vertically on the bar
    double baseline = range.getCenterY() + (fm.getAscent() - fm.getDescent()) / 2d;
    int y = (int) Math.round(baseline);
    g2.drawString(lowerText, range.x - fm.stringWidth(lowerText) - TEXT_GAP, y);
    g2.drawString(upperText, range.x + range.width + TEXT_GAP, y);
  }

  // The same width as the slider tracks, which use TRACK_PADDING as their
  // trackBuffer (see TriangleSliderUI#calculateTrackBuffer()).
  private int getTrackWidth() {
    return getWidth() - TRACK_PADDING * 2;
  }

  private Rectangle getTrackBounds() {
    int height = BAR_HEIGHT - 1;
    int y = (getHeight() - height) / 2;
    return new Rectangle(TRACK_PADDING, y, getTrackWidth() - 1, height);
  }

  private Rectangle getRangeBounds() {
    Rectangle track = getTrackBounds();
    int lowerX = valueToX(lowerSlider.getValue());
    int upperX = valueToX(upperSlider.getValue());
    return new Rectangle(lowerX, track.y, upperX - lowerX, track.height);
  }

  // Same mapping as BasicSliderUI#xPositionForValue(int) so that the values
  // painted here line up with the slider thumbs.
  private int valueToX(int value) {
    int min = lowerSlider.getMinimum();
    int max = lowerSlider.getMaximum();
    int trackWidth = getTrackWidth();
    double pixelsPerValue = (double) trackWidth / (max - min);
    int x = (int) Math.round(pixelsPerValue * (value - min));
    return TRACK_PADDING + Math.min(x, trackWidth - 1);
  }

  // Slide the whole range by the horizontal drag distance, keeping its width.
  private void moveRange(int dx) {
    int min = lowerSlider.getMinimum();
    int max = lowerSlider.getMaximum();
    double valuesPerPixel = (max - min) / (double) getTrackWidth();
    int delta = (int) Math.round(dx * valuesPerPixel);
    // Stop at the ends of the track instead of ignoring the drag
    delta = Math.max(min - lowerAtDragStart, Math.min(max - upperAtDragStart, delta));
    int lower = lowerAtDragStart + delta;
    int upper = upperAtDragStart + delta;
    // RangeSliderPanel clamps lower <= upper on every change, so when moving
    // to the right the upper value has to be raised before the lower one.
    if (lower > lowerSlider.getValue()) {
      upperSlider.setValue(upper);
      lowerSlider.setValue(lower);
    } else {
      lowerSlider.setValue(lower);
      upperSlider.setValue(upper);
    }
  }

  private void setRangeHovered(boolean hovered) {
    if (hovering != hovered) {
      hovering = hovered;
      setCursor(Cursor.getPredefinedCursor(
          hovered ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }
  }

  private final class RangeMouseListener extends MouseAdapter {
    @Override public void mouseEntered(MouseEvent e) {
      mouseMoved(e);
    }

    @Override public void mouseMoved(MouseEvent e) {
      setRangeHovered(getRangeBounds().contains(e.getPoint()));
    }

    @Override public void mousePressed(MouseEvent e) {
      if (SwingUtilities.isLeftMouseButton(e) && getRangeBounds().contains(e.getPoint())) {
        dragging = true;
        setRangeHovered(true);
        dragStartX = e.getX();
        lowerAtDragStart = lowerSlider.getValue();
        upperAtDragStart = upperSlider.getValue();
      }
    }

    @Override public void mouseDragged(MouseEvent e) {
      if (dragging) {
        moveRange(e.getX() - dragStartX);
      }
    }

    @Override public void mouseReleased(MouseEvent e) {
      if (dragging && SwingUtilities.isLeftMouseButton(e)) {
        dragging = false;
      }
      mouseMoved(e);
    }

    @Override public void mouseExited(MouseEvent e) {
      if (!dragging) {
        setRangeHovered(false);
      }
    }
  }
}
