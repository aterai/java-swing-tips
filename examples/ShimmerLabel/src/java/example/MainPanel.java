// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    addLabel(box, new ShimmerLabel("Standard Area Shimmer"));
    addLabel(box, new TextShimmerLabel("Text-Only Shimmer (Clipping)"));
    addLabel(box, new TextCompositeShimmerLabel("Text-Only Shimmer (Composite)"));
    String text = "JLayer Shimmer Label";
    Icon icon = UIManager.getIcon("InternalFrame.icon");
    JLabel label = new JLabel(text, icon, JLabel.LEADING);
    addLabel(box, new JLayer<>(label, new ShimmerLayerUI()));

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addLabel(Box box, JComponent component) {
    component.setAlignmentX(LEFT_ALIGNMENT);
    box.add(component);
    box.add(Box.createVerticalStrut(20));
  }

  private static void addLabel(Box box, JLabel label) {
    label.setIcon(UIManager.getIcon("InternalFrame.icon"));
    addLabel(box, (JComponent) label);
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

// Utility: shimmer effect color calculations
final class ShimmerColors {
  private ShimmerColors() {
    /* Utility class: do not instantiate */
  }

  // Returns the relative brightness of the foreground color as 0.0～1.0
  // (sRGB -> linear, ITU-R BT.709).
  // The higher the brightness (≧0.5), the more likely it is that
  // a "brighter foreground color = dark mode" is being detected.
  public static double luminance(Color c) {
    double r = srgbToLinear(c.getRed() / 255d);
    double g = srgbToLinear(c.getGreen() / 255d);
    double b = srgbToLinear(c.getBlue() / 255d);
    return .2126 * r + .7152 * g + .0722 * b;
  }

  @SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
  public static double srgbToLinear(double v) {
    return v <= .04045 ? v / 12.92 : Math.pow((v + .055) / 1.055, 2.4);
  }

  // Determines the shimmer highlight color based on the foreground color.
  // - Dark Mode (Bright Foreground):
  //     - Blends the foreground color further towards white to enhance the shimmer
  // - Light Mode (Dark Foreground):
  //     - Uses white as the highlight
  // - @param fg Foreground color of the label (value of {@code getForeground()})
  // - @param alpha Alpha value of the highlight color (0～255)
  public static Color shimmerBright(Color fg, int alpha) {
    boolean isDarkMode = luminance(fg) >= 0.5;
    int r = isDarkMode ? blend(fg.getRed(), 255, 0.6) : 255;
    int g = isDarkMode ? blend(fg.getGreen(), 255, 0.6) : 255;
    int b = isDarkMode ? blend(fg.getBlue(), 255, 0.6) : 255;
    return new Color(r, g, b, alpha);
  }

  // Linearly interpolates src and dst by t (0.0～1.0) and returns an integer.
  public static int blend(int src, int dst, double t) {
    return (int) Math.round(src + (dst - src) * t);
  }
}

// Utility: shared layout helper
final class ShimmerLayout {
  private ShimmerLayout() {
    /* Utility class: do not instantiate */
  }

  // Computes iconRect and textRect for the given label using the Graphics2D context.
  // Returns a two-element array: [iconRect, textRect].
  public static Rectangle[] layoutLabel(JLabel label, Graphics2D g2) {
    Rectangle area = SwingUtilities.calculateInnerArea(label, null);
    FontMetrics fm = g2.getFontMetrics(label.getFont());
    Rectangle iconRect = new Rectangle();
    Rectangle textRect = new Rectangle();
    SwingUtilities.layoutCompoundLabel(
        label,
        fm,
        label.getText(),
        label.getIcon(),
        label.getVerticalAlignment(),
        label.getHorizontalAlignment(),
        label.getVerticalTextPosition(),
        label.getHorizontalTextPosition(),
        area,
        iconRect,
        textRect,
        label.getIconTextGap());
    return new Rectangle[] {iconRect, textRect};
  }
}

// Abstract base class for shimmer JLabel variants
abstract class AbstractShimmerLabel extends JLabel {
  protected static final Color TRANSPARENT = new Color(0x0, true);
  protected static final int ALPHA = 200;

  protected final float[] fractions = {0f, .5f, 1f};
  protected float animX;

  private final Timer shimmerTimer;

  protected AbstractShimmerLabel(String text, int fps, float speed, int bandWidth) {
    super(text);
    shimmerTimer = new Timer(fps, e -> {
      animX += speed;
      if (animX > getWidth() + bandWidth) {
        animX = -bandWidth;
      }
      repaint();
    });
    shimmerTimer.start();
  }

  @Override public boolean isOpaque() {
    return false;
  }

  @Override public void removeNotify() {
    shimmerTimer.stop();
    super.removeNotify();
  }

  protected void clearTextRect(Graphics2D g2, Rectangle textRect) {
    Paint oldPaint = g2.getPaint();
    g2.setPaint(getBackground());
    g2.fillRect(textRect.x, textRect.y, textRect.width, textRect.height);
    g2.setPaint(oldPaint);
  }

  public static void applyRenderingHints(Graphics2D g2) {
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
  }
}

// ShimmerLabel: Shimmer that sweeps the entire component rectangle.
class ShimmerLabel extends AbstractShimmerLabel {
  private static final int FPS = 15;
  private static final int BAND_WIDTH = 80;
  private static final float SPEED = 4f;
  private static final Color SHIMMER_BASE = new Color(0x00_FF_FF_FF, true);
  private static final Color SHIMMER_BRIGHT = new Color(0xC8_FF_FF_FF, true);
  private final Color[] colors = {SHIMMER_BASE, SHIMMER_BRIGHT, SHIMMER_BASE};

  protected ShimmerLabel(String text) {
    super(text, FPS, SPEED, BAND_WIDTH);
  }

  @Override public boolean isOpaque() {
    // ShimmerLabel fills its own background, so it IS opaque.
    return true;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    float endX = animX + BAND_WIDTH;
    g2.setPaint(new LinearGradientPaint(animX, 0, endX, 0, fractions, colors));
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
  }
}

// TextShimmerLabel: Shimmer that glints only the text using clipping.
class TextShimmerLabel extends AbstractShimmerLabel {
  private static final int FPS = 15;
  // NOTE: BAND_WIDTH intentionally differs from other variants.
  // (narrower band = sharper glint)
  private static final int BAND_WIDTH = 100;
  private static final float SPEED = 4f;

  protected TextShimmerLabel(String text) {
    super(text, FPS, SPEED, BAND_WIDTH);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    applyRenderingHints(g2);

    Rectangle[] rects = ShimmerLayout.layoutLabel(this, g2);
    Rectangle textRect = rects[1];
    FontMetrics fm = g2.getFontMetrics(getFont());

    float x = textRect.x;
    float y = textRect.y + fm.getAscent();

    // Clear the text area painted by super.paintComponent to avoid double-draw.
    clearTextRect(g2, textRect);

    // 1. Base text
    g2.setPaint(getForeground());
    FontRenderContext frc = g2.getFontRenderContext();
    TextLayout layout = new TextLayout(getText(), getFont(), frc);
    layout.draw(g2, x, y);

    // 2. Clip to text outline
    g2.clip(layout.getOutline(AffineTransform.getTranslateInstance(x, y)));

    // 3. Draw gradient inside clip
    Color[] colors = {
      TRANSPARENT,
      ShimmerColors.shimmerBright(getForeground(), ALPHA),
      TRANSPARENT,
    };
    float endX = animX + BAND_WIDTH;
    g2.setPaint(new LinearGradientPaint(animX, 0f, endX, 0f, fractions, colors));
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
  }
}

// TextCompositeShimmerLabel: Shimmer that glints text with high quality using AlphaComposite.
class TextCompositeShimmerLabel extends AbstractShimmerLabel {
  private static final int FPS = 15;
  // NOTE: BAND_WIDTH intentionally wider here for a softer, more diffuse glow.
  private static final int BAND_WIDTH = 160;
  private static final float SPEED = 4f;

  private transient BufferedImage buffer;

  public TextCompositeShimmerLabel(String text) {
    super(text, FPS, SPEED, BAND_WIDTH);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    applyRenderingHints(g2);

    Rectangle[] rects = ShimmerLayout.layoutLabel(this, g2);
    Rectangle textRect = rects[1];

    // Clear the text area painted by super.paintComponent to avoid double-draw.
    clearTextRect(g2, textRect);

    updateBuffer();
    Optional.ofNullable(buffer).ifPresent(img -> g2.drawImage(img, 0, 0, this));
    g2.dispose();
  }

  private void updateBuffer() {
    int w = Math.max(1, getWidth());
    int h = Math.max(1, getHeight());
    if (buffer == null || buffer.getWidth() != w || buffer.getHeight() != h) {
      buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }
    Graphics2D g2 = buffer.createGraphics();
    g2.setComposite(AlphaComposite.Clear);
    g2.fillRect(0, 0, w, h);
    g2.setComposite(AlphaComposite.SrcOver);
    applyRenderingHints(g2);

    Rectangle[] rects = ShimmerLayout.layoutLabel(this, g2);
    Rectangle textRect = rects[1];
    FontMetrics fm = g2.getFontMetrics(getFont());

    float x = textRect.x;
    float y = textRect.y + fm.getAscent();

    // 1. Draw base text into buffer.
    g2.setPaint(getForeground());
    FontRenderContext frc = g2.getFontRenderContext();
    new TextLayout(getText(), getFont(), frc).draw(g2, x, y);

    // 2. Composite shimmer gradient over text pixels only (SrcAtop).
    g2.setComposite(AlphaComposite.SrcAtop);
    Color[] colors = {
        TRANSPARENT,
        ShimmerColors.shimmerBright(getForeground(), ALPHA),
        TRANSPARENT,
    };
    float endX = animX + BAND_WIDTH;
    g2.setPaint(new LinearGradientPaint(animX, 0f, endX, 0f, fractions, colors));
    g2.fillRect(0, 0, w, h);
    g2.dispose();
  }
}

// ShimmerLayerUI: JLayer-based shimmer using AlphaComposite.
class ShimmerLayerUI extends LayerUI<JLabel> {
  private static final int FPS = 16;
  // NOTE: BAND_WIDTH intentionally narrower for a tighter,
  // sharper highlight on the layer variant.
  private static final int BAND_WIDTH = 100;
  private static final float SPEED = 4f;
  private static final int ALPHA = 180;
  private static final Color TRANSPARENT = new Color(0x0, true);
  private final Timer timer = new Timer(FPS, null);
  private final float[] fractions = {0f, .5f, 1f};
  private float animX;
  private transient BufferedImage buffer;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    timer.addActionListener(ev -> {
      animX += SPEED;
      if (animX > c.getWidth() + BAND_WIDTH) {
        animX = -BAND_WIDTH;
      }
      c.repaint();
    });
    timer.start();
  }

  @Override public void uninstallUI(JComponent c) {
    timer.stop();
    super.uninstallUI(c);
  }

  // Since `super.paint()` draws the `JLabel` directly to the screen,
  // it results in duplicate text with the text in the buffer that is later
  // drawn using `drawImage()`.
  // Instead, the `JLabel` is drawn entirely within the buffer, and the buffer
  // is transferred to the screen only once after shader compositing.
  @Override public void paint(Graphics g, JComponent c) {
    // super.paint(g, c);
    @SuppressWarnings("unchecked")
    JLayer<? extends JLabel> layer = (JLayer<? extends JLabel>) c;
    JLabel label = layer.getView();
    if (label == null || label.getText() == null || label.getText().isEmpty()) {
      super.paint(g, c);
    } else {
      int w = Math.max(1, c.getWidth());
      int h = Math.max(1, c.getHeight());
      ensureBuffer(w, h);
      paintLayerToBuffer(c, w, h);
      BufferedImage shimBuf = createShimmerBuffer(label, c, w, h);
      paintShimmerBuffer(shimBuf);
      g.drawImage(buffer, 0, 0, c);
    }
  }

  private void ensureBuffer(int w, int h) {
    if (buffer == null || buffer.getWidth() != w || buffer.getHeight() != h) {
      buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }
  }

  private void paintLayerToBuffer(JComponent c, int w, int h) {
    Graphics2D buf = buffer.createGraphics();
    buf.setComposite(AlphaComposite.Clear);
    buf.fillRect(0, 0, w, h);
    buf.setComposite(AlphaComposite.SrcOver);
    c.paint(buf);
    buf.dispose();
  }

  // Generate a separate text-only shimmer buffer and overlay it using SrcOver.
  // Applying SrcAtop to the entire buffer causes the shimmer to affect the icons as well,
  // so the text shimmer is composited in a separate buffer before being transferred.
  private BufferedImage createShimmerBuffer(JLabel label, JComponent c, int w, int h) {
    BufferedImage shimBuf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D sg = shimBuf.createGraphics();

    AbstractShimmerLabel.applyRenderingHints(sg);
    Rectangle[] rects = ShimmerLayout.layoutLabel(label, sg);
    Rectangle textRect = rects[1];
    FontMetrics fm = sg.getFontMetrics(label.getFont());
    Point labelOrigin = SwingUtilities.convertPoint(label, 0, 0, c);
    float x = labelOrigin.x + textRect.x;
    float y = labelOrigin.y + textRect.y + fm.getAscent();

    // 1. Draw the text outline to the shimmer buffer (to serve as a mask).
    sg.setPaint(label.getForeground());
    FontRenderContext frc = sg.getFontRenderContext();
    new TextLayout(label.getText(), label.getFont(), frc).draw(sg, x, y);

    // 2. Compose the gradient using SrcAtop (masked by the text's alpha channel).
    sg.setComposite(AlphaComposite.SrcAtop);
    Color[] colors = {
        TRANSPARENT,
        ShimmerColors.shimmerBright(label.getForeground(), ALPHA),
        TRANSPARENT,
    };
    float endX = animX + BAND_WIDTH;
    sg.setPaint(new LinearGradientPaint(animX, 0f, endX, 0f, fractions, colors));
    sg.fillRect(0, 0, w, h);
    sg.dispose();

    // Clear the text area painted
    Graphics2D buf = buffer.createGraphics();
    // buf.setComposite(AlphaComposite.DstOut);
    buf.setPaint(label.getBackground());
    buf.fill(textRect);
    buf.dispose();

    return shimBuf;
  }

  private void paintShimmerBuffer(BufferedImage shimBuf) {
    Graphics2D buf = buffer.createGraphics();
    buf.setComposite(AlphaComposite.SrcOver);
    buf.drawImage(shimBuf, 0, 0, null);
    buf.dispose();
  }
}
