// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1, 20, 20));
    JLabel label1 = new AnimatedLabel("LinearGradient");
    label1.setBorder(new RotatingRainbowBorder(16, 16, 16, 16));
    add(label1);
    JLabel label2 = new AnimatedLabel("ConicGradient");
    label2.setBorder(new RotatingConicRainbowBorder(16, 16, 16, 16));
    add(label2);
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
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

class AnimatedLabel extends JLabel {
  private float rotation;
  private final Timer timer = new Timer(16, e -> {
    Border border = getBorder();
    if (border instanceof RotatingRainbowBorder) {
      rotation += .08f;
      ((RotatingRainbowBorder) border).setRotation(rotation);
      repaint();
    }
  });

  protected AnimatedLabel(String text) {
    super(text);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(240, 100);
  }

  @Override public void addNotify() {
    super.addNotify();
    timer.start();
  }

  @Override public void removeNotify() {
    super.removeNotify();
    timer.stop();
  }
}

class RotatingRainbowBorder extends EmptyBorder {
  protected static final float BORDER_WIDTH = 2f;
  protected static final float CORNER_RADIUS = 12f;
  protected static final int HUE_STEPS = 24;
  protected static final float[] FRACTIONS = new float[HUE_STEPS + 1];
  protected static final Color[] COLORS = new Color[HUE_STEPS + 1];

  static {
    for (int i = 0; i <= HUE_STEPS; i++) {
      float hue = (float) i / HUE_STEPS;
      FRACTIONS[i] = hue;
      // COLORS[i] = Color.getHSBColor(hue, 1f, 1f);
      COLORS[i] = Color.getHSBColor(hue, .8f, 1f);
    }
  }

  private float rotation;

  protected RotatingRainbowBorder(int top, int left, int bottom, int right) {
    super(top, left, bottom, right);
  }

  public void setRotation(float newRotation) {
    this.rotation = newRotation;
  }

  protected final float getRotation() {
    return rotation;
  }

  // Generate an Area for the border area (outer frame - inner frame).
  protected static Area createBorderArea(int x, int y, int width, int height) {
    Shape outer = new RoundRectangle2D.Float(
        x, y,
        width - 1f, height - 1f,
        CORNER_RADIUS, CORNER_RADIUS);
    Shape inner = new RoundRectangle2D.Float(
        x + BORDER_WIDTH,
        y + BORDER_WIDTH,
        width - BORDER_WIDTH * 2f - 1f,
        height - BORDER_WIDTH * 2f - 1f,
        CORNER_RADIUS - BORDER_WIDTH * 2f,
        CORNER_RADIUS - BORDER_WIDTH * 2f);
    Area area = new Area(outer);
    area.subtract(new Area(inner));
    return area;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    float cx = x + width * .5f;
    float cy = y + height * .5f;

    // By setting half of the diagonal (distance to the corner) as radius,
    // the entire gradient will fit within the component and all colors will
    // be displayed evenly.
    float gradientRadius = (float) Math.hypot(width, height) * .5f;
    float dx = (float) (Math.cos(getRotation()) * gradientRadius);
    float dy = (float) (Math.sin(getRotation()) * gradientRadius);
    g2.setPaint(new LinearGradientPaint(
        new Point2D.Float(cx - dx, cy - dy),
        new Point2D.Float(cx + dx, cy + dy),
        FRACTIONS, COLORS));
    g2.fill(createBorderArea(x, y, width, height));
    g2.dispose();
  }
}

class RotatingConicRainbowBorder extends RotatingRainbowBorder {
  // Offscreen buffer that regenerates only on resize
  private transient BufferedImage offscreen;
  private int cachedWidth;
  private int cachedHeight;

  protected RotatingConicRainbowBorder(int top, int left, int bottom, int right) {
    super(top, left, bottom, right);
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    // Regenerate offscreen buffer only if size changes
    if (offscreen == null || cachedWidth != width || cachedHeight != height) {
      offscreen = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      cachedWidth = width;
      cachedHeight = height;
    }
    Area borderArea = createBorderArea(x, y, width, height);

    Graphics2D g2 = offscreen.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setComposite(AlphaComposite.Clear);
    g2.fillRect(0, 0, width, height);
    // Pass 1: Draw border shape with AA enabled → Edge alpha is smooth
    g2.setComposite(AlphaComposite.Src);
    g2.fill(borderArea);
    g2.dispose();

    // Pass 2: Pixel-direct conic gradient while preserving AA alpha from pass 1
    renderConicGradient(offscreen, borderArea, getRotation());

    g.drawImage(offscreen, 0, 0, null);
  }

  // Draw a conic gradient only on pixels in the border area.
  // Pre-filter with borderArea.contains() to avoid scanning the entire rectangle.
  private static void renderConicGradient(
      BufferedImage offscreen, Area borderArea, float rotation) {
    int width = offscreen.getWidth();
    int height = offscreen.getHeight();
    double cx = width * .5;
    double cy = height * .5;
    int[] pixels = ((DataBufferInt) offscreen.getRaster().getDataBuffer()).getData();
    Rectangle bounds = borderArea.getBounds();
    for (int py = bounds.y; py < bounds.y + bounds.height; py++) {
      for (int px = bounds.x; px < bounds.x + bounds.width; px++) {
        int dstAlpha = (pixels[py * width + px] >>> 24) & 0xFF;
        if (dstAlpha == 0) {
          continue;
        }
        double angle = Math.atan2(py - cy, px - cx) - rotation;
        double v = angle / (2d * Math.PI);
        double t = (v % 1d + 1d) % 1d;
        int rgb = interpolateColorRgb(t) & 0x00_FF_FF_FF;
        pixels[py * width + px] = (dstAlpha << 24) | rgb;
      }
    }
  }

  // FRACTIONS is evenly spaced, so the segment index is computable directly
  private static int interpolateColorRgb(double t) {
    double pos = Math.min(Math.max(t, 0d), 1d) * HUE_STEPS;
    // Java 21: double pos = Math.clamp(t, 0d, 1d) * HUE_STEPS;
    int idx = Math.min((int) pos, HUE_STEPS - 1);
    float ratio = (float) (pos - idx);
    return interpolateArgb(COLORS[idx].getRGB(), COLORS[idx + 1].getRGB(), ratio);
  }

  private static int interpolateArgb(int c0, int c1, float t) {
    int a = lerp((c0 >>> 24) & 0xFF, (c1 >>> 24) & 0xFF, t);
    int r = lerp((c0 >>> 16) & 0xFF, (c1 >>> 16) & 0xFF, t);
    int g = lerp((c0 >>> 8) & 0xFF, (c1 >>> 8) & 0xFF, t);
    int b = lerp(c0 & 0xFF, c1 & 0xFF, t);
    return (a << 24) | (r << 16) | (g << 8) | b;
  }

  // lerp: Linear Interpolation
  private static int lerp(int a, int b, float t) {
    return Math.round(a + (b - a) * t);
    // Java 21: return Math.round(Math.fma(b - a, t, a));
  }
}
