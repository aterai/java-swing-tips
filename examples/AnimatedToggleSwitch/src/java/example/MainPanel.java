// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 2));
    UIManager.put("ToggleSwitchSlider.onColor", new Color(0x00_64_E4));
    UIManager.put("ToggleSwitchSlider.offColor", new Color(0x80_80_80));
    UIManager.put("ToggleSwitchSlider.disabledColor", new Color(0xB4_B4_B4));
    UIManager.put("ToggleSwitchSlider.borderColor", new Color(0x64_64_64));
    UIManager.put("ToggleSwitchSlider.thumbColor", Color.WHITE);
    UIManager.put("ToggleSwitchSlider.disabledThumbColor", new Color(0xEE_EE_EE));

    JSlider disabled = createToggleSwitch(1);
    disabled.setEnabled(false);
    add(createTitledPanel("Default", new JSlider(0, 1, 0)));
    add(createTitledPanel("ToggleSwitch: Off", createToggleSwitch(0)));
    add(createTitledPanel("ToggleSwitch: On", createToggleSwitch(1)));
    add(createTitledPanel("setEnabled(false)", disabled));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JSlider createToggleSwitch(int value) {
    JSlider slider = new JSlider(0, 1, value) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new SliderToggleSwitchUI(this));
        setOpaque(false);
      }
    };
    // Font is not a UIResource, so it is kept after a LookAndFeel change
    slider.setFont(slider.getFont().deriveFont(Font.BOLD, 14f));
    return slider;
  }

  private static Component createTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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

class SliderToggleSwitchUI extends BasicSliderUI {
  private static final int THUMB_SIZE = 20;
  private static final int THUMB_MARGIN = 2;
  private static final int TRACK_WIDTH = THUMB_SIZE * 2;
  private static final String ON_TEXT = "✓";
  private static final String OFF_TEXT = "";
  private static final SwitchLabels LABELS = new SwitchLabels(ON_TEXT, OFF_TEXT);

  private final ThumbAnimator animator;

  protected SliderToggleSwitchUI(JSlider slider) {
    super(slider);
    // The field BasicSliderUI#slider is not assigned until installUI(...)
    animator = new ThumbAnimator(slider);
  }

  @Override public void uninstallUI(JComponent c) {
    animator.stop();
    super.uninstallUI(c);
  }

  @Override protected void installDefaults(JSlider slider) {
    super.installDefaults(slider);
    // The track of a toggle switch fills the whole component
    focusInsets = new Insets(0, 0, 0, 0);
  }

  @Override public Dimension getPreferredHorizontalSize() {
    return new Dimension(TRACK_WIDTH, THUMB_SIZE);
  }

  @Override public Dimension getMinimumHorizontalSize() {
    return new Dimension(THUMB_SIZE * 2, THUMB_SIZE);
  }

  @Override protected Dimension getThumbSize() {
    return new Dimension(THUMB_SIZE, THUMB_SIZE);
  }

  @Override public void setThumbLocation(int x, int y) {
    super.setThumbLocation(x, y);
    // While dragging, the thumb follows the mouse pointer without any animation
    animator.jumpTo(x);
    // The track color and the On/Off label depend on the thumb location,
    // so the partial repaint of the super method is not enough
    slider.repaint();
  }

  @Override protected void calculateThumbLocation() {
    super.calculateThumbLocation();
    // BasicSliderUI#ChangeHandler skips this method while the thumb is dragged,
    // so a value change by a click, a key stroke or setValue(...) is animated here
    if (animator.isInitialized() && slider.isShowing()) {
      animator.startTo(thumbRect.x);
    } else {
      // The very first layout and a layout before the switch is shown must not animate
      animator.jumpTo(thumbRect.x);
    }
  }

  @Override public void paintFocus(Graphics g) {
    // The focus is painted as a part of the track border in paintTrack(...)
  }

  @Override public void paintTrack(Graphics g) {
    // trackRect is inset by trackBuffer(= thumbWidth / 2) on both sides
    Rectangle r = new Rectangle(trackRect);
    r.grow(thumbRect.width / 2, 0);
    // A stroke is centered on the shape outline, so the track must be inset by
    // half of the line width. Otherwise the outer half of the border sticks out
    // of the component and its edge pixels are left unpainted
    float lw = 1f; // slider.hasFocus() ? 2f : 1f;
    double half = lw / 2d;
    Shape track = new RoundRectangle2D.Double(
        r.x + half, r.y + half, r.width - lw, r.height - lw, r.height, r.height);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(getTrackColor());
    g2.fill(track);
    g2.setPaint(UIManager.getColor("ToggleSwitchSlider.borderColor"));
    g2.setStroke(new BasicStroke(lw));
    g2.draw(track);
    g2.clip(track);
    // thumbRect includes THUMB_MARGIN, so shrink it to the visible thumb size
    double left = animator.getX() + THUMB_MARGIN;
    double right = animator.getX() + thumbRect.width - THUMB_MARGIN;
    LABELS.paint(g2, r, left, right, getThumbFraction());
    g2.dispose();
  }

  @Override public void paintThumb(Graphics g) {
    double d = Math.min(thumbRect.width, thumbRect.height) - THUMB_MARGIN * 2d - 1d;
    Shape thumb = new Ellipse2D.Double(
        animator.getX() + THUMB_MARGIN + .5, thumbRect.y + THUMB_MARGIN + .5, d, d);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    String thumbKey = slider.isEnabled()
        ? "ToggleSwitchSlider.thumbColor"
        : "ToggleSwitchSlider.disabledThumbColor";
    g2.setPaint(UIManager.getColor(thumbKey));
    g2.fill(thumb);
    g2.setPaint(UIManager.getColor("ToggleSwitchSlider.borderColor"));
    g2.draw(thumb);
    g2.dispose();
  }

  @Override protected TrackListener createTrackListener(JSlider slider) {
    return new ToggleTrackListener();
  }

  // Extracted from an anonymous class to keep it under Checkstyle's AnonInnerLength limit
  private final class ToggleTrackListener extends TrackListener {
    private boolean thumbPressed;
    private int pressedValue;

    @SuppressWarnings("ReturnCount")
    @Override public void mousePressed(MouseEvent e) {
      if (!slider.isEnabled() || !SwingUtilities.isLeftMouseButton(e)) {
        return;
      }
      thumbPressed = thumbRect.contains(e.getX(), e.getY());
      if (thumbPressed) {
        pressedValue = slider.getValue();
        super.mousePressed(e); // Start dragging the thumb
      } else {
        // Pressing the track toggles the value
        // instead of BasicSliderUI#scrollDueToClickInTrack(...)
        if (slider.isRequestFocusEnabled()) {
          slider.requestFocus();
        }
        toggle();
      }
    }

    @Override public void mouseReleased(MouseEvent e) {
      super.mouseReleased(e);
      if (thumbPressed && pressedValue == slider.getValue()) {
        toggle(); // Clicking the thumb without dragging also toggles the value
      }
      thumbPressed = false;
    }

    private void toggle() {
      int min = slider.getMinimum();
      slider.setValue(slider.getValue() == min ? slider.getMaximum() : min);
    }
  }

  // The ratio of the current thumb position to the whole travel: 0d = Off, 1d = On
  private double getThumbFraction() {
    int half = thumbRect.width / 2;
    int minX = xPositionForValue(slider.getMinimum()) - half;
    int maxX = xPositionForValue(slider.getMaximum()) - half;
    double f = minX == maxX ? 0d : (animator.getX() - minX) / (maxX - minX);
    return Math.max(0d, Math.min(1d, f));
  }

  private Color getTrackColor() {
    Color color;
    if (slider.isEnabled()) {
      // The track color is blended so that it changes while the thumb is moving
      Color c0 = UIManager.getColor("ToggleSwitchSlider.offColor");
      Color c1 = UIManager.getColor("ToggleSwitchSlider.onColor");
      double t = getThumbFraction();
      double u = 1d - t;
      color = new Color(
          (int) Math.round(c0.getRed() * u + c1.getRed() * t),
          (int) Math.round(c0.getGreen() * u + c1.getGreen() * t),
          (int) Math.round(c0.getBlue() * u + c1.getBlue() * t));
    } else {
      color = UIManager.getColor("ToggleSwitchSlider.disabledColor");
    }
    return color;
  }
}

// Paints the On/Off labels in the part of the track that is not covered by the thumb
class SwitchLabels {
  private final String onText;
  private final String offText;

  protected SwitchLabels(String onText, String offText) {
    this.onText = onText;
    this.offText = offText;
  }

  // The fraction is 0d when the switch is Off and 1d when it is On,
  // so the two labels cross-fade while the thumb is moving
  public void paint(
      Graphics2D g2, Rectangle track, double thumbLeft, double thumbRight, double fraction) {
    double y = track.y;
    double h = track.height;
    paintLabel(g2, onText,
        new Rectangle2D.Double(track.x, y, thumbLeft - track.x, h), fraction);
    paintLabel(g2, offText,
        new Rectangle2D.Double(thumbRight, y, track.getMaxX() - thumbRight, h), 1d - fraction);
  }

  private static void paintLabel(Graphics2D g2, String txt, Rectangle2D free, double alpha) {
    if (!txt.isEmpty() && alpha > 0d) {
      FontMetrics fm = g2.getFontMetrics();
      double tx = free.getX() + (free.getWidth() - fm.stringWidth(txt)) / 2d;
      double ty = free.getY() + (free.getHeight() - fm.getHeight()) / 2d + fm.getAscent();
      g2.setPaint(new Color(1f, 1f, 1f, (float) alpha));
      g2.drawString(txt, (float) tx, (float) ty);
    }
  }
}

// Slides the painted thumb position towards its target with an ease-out curve
class ThumbAnimator {
  private static final int DELAY = 10;
  private static final int DURATION = 120;

  private final JComponent view;
  private final Timer timer;
  // The thumb position being painted, which lags behind the model position
  private double posX = Double.NaN;
  private double fromX;
  private double toX;
  private long startTime;

  protected ThumbAnimator(JComponent view) {
    this.view = view;
    this.timer = new Timer(DELAY, e -> update());
  }

  public double getX() {
    return posX;
  }

  public boolean isInitialized() {
    return !Double.isNaN(posX);
  }

  // The position the thumb is heading for, or the current one if it is not moving
  public double getTargetX() {
    return timer.isRunning() ? toX : posX;
  }

  // Move to the target immediately, cancelling any animation in progress
  public void jumpTo(double target) {
    timer.stop();
    posX = target;
  }

  public void startTo(double target) {
    if (Double.compare(getTargetX(), target) != 0) {
      fromX = posX;
      toX = target;
      startTime = System.currentTimeMillis();
      timer.restart();
    }
  }

  public void stop() {
    timer.stop();
  }

  private void update() {
    long elapsed = System.currentTimeMillis() - startTime;
    if (elapsed < DURATION) {
      posX = fromX + (toX - fromX) * easeOutCubic(elapsed / (double) DURATION);
    } else {
      posX = toX;
      timer.stop();
    }
    view.repaint();
  }

  private static double easeOutCubic(double t) {
    double u = 1d - t;
    return 1d - u * u * u;
  }
}
