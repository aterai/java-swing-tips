// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int THUMB_SIZE = 24;
  private static final Color THUMB_COLOR = new Color(0x21_98_F6);

  private MainPanel() {
    super(new BorderLayout());
    UIDefaults overrides = new UIDefaults();
    overrides.put("Slider.thumbWidth", THUMB_SIZE);
    overrides.put("Slider.thumbHeight", THUMB_SIZE);
    // Paint a filled circle with the current value drawn on top as the slider thumb.
    Painter<JSlider> thumbPainter = (g, c, w, h) -> {
      g.setPaint(THUMB_COLOR);
      g.fillOval(0, 0, w, h);
      NumberIcon icon = new NumberIcon(c.getValue());
      int iconX = (w - icon.getIconWidth()) / 2;
      int iconY = (h - icon.getIconHeight()) / 2;
      icon.paintIcon(c, g, iconX, iconY);
    };
    overrides.put("Slider:SliderThumb[Disabled].backgroundPainter", thumbPainter);
    overrides.put("Slider:SliderThumb[Enabled].backgroundPainter", thumbPainter);
    overrides.put("Slider:SliderThumb[Focused+MouseOver].backgroundPainter", thumbPainter);
    overrides.put("Slider:SliderThumb[Focused+Pressed].backgroundPainter", thumbPainter);
    overrides.put("Slider:SliderThumb[Focused].backgroundPainter", thumbPainter);
    overrides.put("Slider:SliderThumb[MouseOver].backgroundPainter", thumbPainter);
    overrides.put("Slider:SliderThumb[Pressed].backgroundPainter", thumbPainter);
    overrides.put(
        "Slider:SliderTrack[Enabled].backgroundPainter", new SliderTrackPainter(THUMB_SIZE));

    JSlider slider = new JSlider();
    slider.setSnapToTicks(true);
    slider.setMajorTickSpacing(10);
    // While dragging, the UI repaints only the thumb area, so repaint the whole
    // slider whenever the value changes to keep the filled track in sync.
    slider.addChangeListener(e -> ((JComponent) e.getSource()).repaint());
    slider.putClientProperty("Nimbus.Overrides", overrides);

    Box box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(5));
    box.add(createTitledPanel("Default", new JSlider()));
    box.add(Box.createVerticalStrut(5));
    box.add(createTitledPanel("Paint major tick marks on the track", slider));
    box.add(Box.createVerticalGlue());
    add(box);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component createTitledPanel(String title, Component c) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder(title));
    panel.add(c);
    return panel;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      // UIManager.put("JSlider.isFilled", Boolean.TRUE);
      // UIManager.put("Slider.paintValue", Boolean.TRUE);
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

class SliderTrackPainter implements Painter<JSlider> {
  private static final Color TRACK_COLOR = new Color(0xC6_E4_FC);
  private static final Color TICK_COLOR = new Color(0x31_A8_F8);
  private static final Color FILL_COLOR = new Color(0x21_98_F6);
  private static final int TRACK_HEIGHT = 8;
  private static final int TICK_SIZE = 4;
  private static final int ARC = 10;
  private final int thumbSize;

  protected SliderTrackPainter(int thumbSize) {
    this.thumbSize = thumbSize;
  }

  @Override public void paint(Graphics2D g, JSlider slider, int width, int height) {
    // The track starts and ends half a thumb inside the painted area.
    Rectangle trackRect = new Rectangle(
        thumbSize / 2, (thumbSize - TRACK_HEIGHT) / 2, width - thumbSize, TRACK_HEIGHT);

    // Paint a thin track
    g.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(TRACK_COLOR);
    g.fillRoundRect(
        trackRect.x, trackRect.y + 2, trackRect.width, trackRect.height - 4, ARC, ARC);

    // Paint the major tick marks on the track
    int spacing = slider.getMajorTickSpacing();
    if (spacing > 0) { // a spacing of 0 or less would loop forever
      g.setColor(TICK_COLOR);
      int tickY = (int) trackRect.getCenterY() - TICK_SIZE / 2;
      int max = slider.getMaximum();
      for (int value = slider.getMinimum(); value <= max; value += spacing) {
        // Center the tick mark on the value position, like the thumb
        int tickX = getPositionForValue(slider, trackRect, value) - TICK_SIZE / 2;
        g.fillOval(tickX, tickY, TICK_SIZE, TICK_SIZE);
        // Overflow checking
        if (Integer.MAX_VALUE - spacing < value) {
          break;
        }
      }
    }

    // Fill the track up to the current value (like JSlider.isFilled),
    // hiding the tick marks below the current value
    int fillRight = getPositionForValue(slider, trackRect, slider.getValue());
    g.setColor(FILL_COLOR);
    g.fillRoundRect(
        trackRect.x, trackRect.y, fillRight - trackRect.x, trackRect.height, ARC, ARC);
  }

  // @see javax/swing/plaf/basic/BasicSliderUI#xPositionForValue(int value)
  private int getPositionForValue(JSlider slider, Rectangle trackRect, float value) {
    float min = slider.getMinimum();
    float max = slider.getMaximum();
    float pixelsPerValue = trackRect.width / (max - min);
    int trackLeft = trackRect.x;
    int trackRight = trackRect.x + trackRect.width - 1;
    int pos = trackLeft + Math.round(pixelsPerValue * (value - min));
    return Math.min(Math.max(pos, trackLeft), trackRight);
    // Java 21: return Math.clamp(pos, trackLeft, trackRight);
  }
}

class NumberIcon implements Icon {
  private static final int ICON_SIZE = 20;
  private static final int MAX_DISPLAY_VALUE = 999;
  private static final double NARROW_SCALE_X = .66;

  private final int value;

  protected NumberIcon(int value) {
    this.value = value;
  }

  protected Shape getTextShape(Graphics2D g2) {
    // Java 12:
    // NumberFormat fmt = NumberFormat.getCompactNumberInstance(
    //    Locale.US, NumberFormat.Style.SHORT);
    // String txt = fmt.format(value);
    String txt = value > MAX_DISPLAY_VALUE ? "1K" : Integer.toString(value);
    // Squeeze 3+ character labels horizontally so they still fit inside the icon.
    AffineTransform at = txt.length() < 3
        ? null : AffineTransform.getScaleInstance(NARROW_SCALE_X, 1d);
    return new TextLayout(txt, g2.getFont(), g2.getFontRenderContext()).getOutline(at);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);

    Shape shape = getTextShape(g2);
    Rectangle2D b = shape.getBounds2D();
    double tx = getIconWidth() / 2d - b.getCenterX();
    double ty = getIconHeight() / 2d - b.getCenterY();
    AffineTransform toCenterAt = AffineTransform.getTranslateInstance(tx, ty);
    g2.setPaint(Color.WHITE);
    g2.fill(toCenterAt.createTransformedShape(shape));
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return ICON_SIZE;
  }

  @Override public int getIconHeight() {
    return ICON_SIZE;
  }
}
