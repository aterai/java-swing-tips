// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;
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
    addComponent(box, new JLayer<>(label, new ShimmerLayerUI()));

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addComponent(Box box, JComponent component) {
    component.setAlignmentX(LEFT_ALIGNMENT);
    box.add(component);
    box.add(Box.createVerticalStrut(20));
  }

  private static void addLabel(Box box, JLabel label) {
    label.setIcon(UIManager.getIcon("InternalFrame.icon"));
    addComponent(box, label);
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

  // Returns the relative luminance of the foreground color as 0.0～1.0
  // (sRGB -> linear, ITU-R BT.709).
  // The higher the luminance (≧0.5), the more likely it is that
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
  public static Color highlightColor(Color fg, int alpha) {
    boolean isDarkMode = luminance(fg) >= 0.5;
    int r = isDarkMode ? blend(fg.getRed(), 255, 0.6) : 255;
    int g = isDarkMode ? blend(fg.getGreen(), 255, 0.6) : 255;
    int b = isDarkMode ? blend(fg.getBlue(), 255, 0.6) : 255;
    return new Color(r, g, b, alpha);
  }

  // Returns the gradient colors of the shimmer band: transparent, highlight, transparent.
  // LinearGradientPaint interpolates non-premultiplied colors, so the transparent ends
  // keep the RGB of the highlight; fading to transparent black would darken the band edges.
  public static Color[] gradientColors(Color fg, int alpha) {
    Color highlight = highlightColor(fg, alpha);
    Color transparent = new Color(highlight.getRGB() & 0xFF_FF_FF, true);
    return new Color[] {transparent, highlight, transparent};
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

  // TextLayout rejects a null or empty string, so check this before creating one.
  public static boolean hasText(JLabel label) {
    String text = label.getText();
    return text != null && !text.isEmpty();
  }

  // Computes the text bounds of the given label using the Graphics2D context.
  public static Rectangle getTextRect(JLabel label, Graphics2D g2) {
    Rectangle area = SwingUtilities.calculateInnerArea(label, null);
    FontMetrics fm = g2.getFontMetrics(label.getFont());
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
        new Rectangle(),
        textRect,
        label.getIconTextGap());
    return textRect;
  }
}

// Abstract base class for shimmer JLabel variants
abstract class AbstractShimmerLabel extends JLabel {
  protected static final int ALPHA = 200;
  protected static final float[] FRACTIONS = {0f, .5f, 1f};
  private final Timer shimmerTimer;
  private float animX;

  protected AbstractShimmerLabel(String text, int delay, float speed, int bandWidth) {
    super(text);
    shimmerTimer = new Timer(delay, e -> {
      animX += speed;
      if (animX > getWidth() + bandWidth) {
        animX = -bandWidth;
      }
      repaint();
    });
  }

  @Override public boolean isOpaque() {
    return false;
  }

  // Run the timer only while displayable so that the animation
  // also resumes when the label is added to a container again.
  @Override public void addNotify() {
    super.addNotify();
    shimmerTimer.start();
  }

  @Override public void removeNotify() {
    shimmerTimer.stop();
    super.removeNotify();
  }

  protected float getAnimX() {
    return animX;
  }

  protected void clearTextRect(Graphics2D g2, Rectangle textRect) {
    g2.setPaint(getBackground());
    g2.fill(textRect);
  }

  public static void applyRenderingHints(JLabel label, Graphics2D g2) {
    boolean isWindows = label.getUI().getClass().getName().contains("Windows");
    if (!isWindows) {
      // g2.setRenderingHint(
      //     RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      // g2.setRenderingHint(
      //     RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Object hints = toolkit.getDesktopProperty("awt.font.desktophints");
      if (hints instanceof Map<?, ?>) {
        g2.addRenderingHints((Map<?, ?>) hints);
      }
    }
  }
}

// ShimmerLabel: Shimmer that sweeps the entire component rectangle.
class ShimmerLabel extends AbstractShimmerLabel {
  private static final int DELAY = 15;
  private static final int BAND_WIDTH = 80;
  private static final float SPEED = 4f;
  private static final Color SHIMMER_BASE = new Color(0x00_FF_FF_FF, true);
  private static final Color SHIMMER_BRIGHT = new Color(0xC8_FF_FF_FF, true);
  private static final Color[] COLORS = {SHIMMER_BASE, SHIMMER_BRIGHT, SHIMMER_BASE};

  protected ShimmerLabel(String text) {
    super(text, DELAY, SPEED, BAND_WIDTH);
  }

  @Override public boolean isOpaque() {
    // ShimmerLabel fills its own background, so it IS opaque.
    return true;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    float startX = getAnimX();
    float endX = startX + BAND_WIDTH;
    g2.setPaint(new LinearGradientPaint(startX, 0f, endX, 0f, FRACTIONS, COLORS));
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
  }
}

// TextShimmerLabel: Shimmer that glints only the text using clipping.
class TextShimmerLabel extends AbstractShimmerLabel {
  private static final int DELAY = 15;
  // NOTE: BAND_WIDTH intentionally differs from other variants.
  // (narrower than the composite variant = sharper glint)
  private static final int BAND_WIDTH = 100;
  private static final float SPEED = 4f;

  protected TextShimmerLabel(String text) {
    super(text, DELAY, SPEED, BAND_WIDTH);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (ShimmerLayout.hasText(this)) {
      paintShimmerText(g);
    }
  }

  private void paintShimmerText(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    applyRenderingHints(this, g2);

    Rectangle textRect = ShimmerLayout.getTextRect(this, g2);
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
    Color[] colors = ShimmerColors.gradientColors(getForeground(), ALPHA);
    float startX = getAnimX();
    float endX = startX + BAND_WIDTH;
    g2.setPaint(new LinearGradientPaint(startX, 0f, endX, 0f, FRACTIONS, colors));
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
  }
}

// TextCompositeShimmerLabel: Shimmer that glints text with high quality using AlphaComposite.
class TextCompositeShimmerLabel extends AbstractShimmerLabel {
  private static final int DELAY = 15;
  // NOTE: BAND_WIDTH intentionally wider here for a softer, more diffuse glow.
  private static final int BAND_WIDTH = 160;
  private static final float SPEED = 4f;
  private transient BufferedImage buffer;

  protected TextCompositeShimmerLabel(String text) {
    super(text, DELAY, SPEED, BAND_WIDTH);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (ShimmerLayout.hasText(this)) {
      Graphics2D g2 = (Graphics2D) g.create();
      Rectangle textRect = ShimmerLayout.getTextRect(this, g2);

      // Clear the text area painted by super.paintComponent to avoid double-draw.
      clearTextRect(g2, textRect);

      updateBuffer(textRect);
      g2.drawImage(buffer, 0, 0, this);
      g2.dispose();
    }
  }

  private void updateBuffer(Rectangle textRect) {
    int w = Math.max(1, getWidth());
    int h = Math.max(1, getHeight());
    if (buffer == null || buffer.getWidth() != w || buffer.getHeight() != h) {
      buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }
    Graphics2D g2 = buffer.createGraphics();
    applyRenderingHints(this, g2);
    g2.setComposite(AlphaComposite.Clear);
    g2.fillRect(0, 0, w, h);
    g2.setComposite(AlphaComposite.SrcOver);

    FontMetrics fm = g2.getFontMetrics(getFont());
    float x = textRect.x;
    float y = textRect.y + fm.getAscent();

    // 1. Draw base text into buffer.
    g2.setPaint(getForeground());
    FontRenderContext frc = g2.getFontRenderContext();
    new TextLayout(getText(), getFont(), frc).draw(g2, x, y);

    // 2. Composite shimmer gradient over text pixels only (SrcAtop).
    g2.setComposite(AlphaComposite.SrcAtop);
    Color[] colors = ShimmerColors.gradientColors(getForeground(), ALPHA);
    float startX = getAnimX();
    float endX = startX + BAND_WIDTH;
    g2.setPaint(new LinearGradientPaint(startX, 0f, endX, 0f, FRACTIONS, colors));
    g2.fillRect(0, 0, w, h);
    g2.dispose();
  }
}

// ShimmerLayerUI: JLayer-based shimmer using AlphaComposite.
class ShimmerLayerUI extends LayerUI<JLabel> {
  private static final int DELAY = 16;
  // NOTE: BAND_WIDTH intentionally narrower than the composite variant
  // for a tighter, sharper highlight on the layer variant.
  private static final int BAND_WIDTH = 100;
  private static final float SPEED = 4f;
  private static final int ALPHA = 180;
  private static final float[] FRACTIONS = {0f, .5f, 1f};
  private final Timer timer = new Timer(DELAY, null);
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
    // Otherwise reinstalling this UI would add a second listener and double the speed.
    Stream.of(timer.getActionListeners()).forEach(timer::removeActionListener);
    super.uninstallUI(c);
  }

  // `super.paint()` draws the `JLabel` including its plain text, so the text area is
  // cleared and then overlaid with the text-only shimmer buffer using SrcOver.
  // Applying SrcAtop to the whole layer would cause the shimmer to affect the icon as well,
  // so the text shimmer is composited in a separate buffer before being transferred.
  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    @SuppressWarnings("unchecked")
    JLayer<? extends JLabel> layer = (JLayer<? extends JLabel>) c;
    JLabel label = layer.getView();
    if (label != null && ShimmerLayout.hasText(label)) {
      Graphics2D g2 = (Graphics2D) g.create();
      Rectangle textRect = ShimmerLayout.getTextRect(label, g2);
      Point labelOrigin = SwingUtilities.convertPoint(label, 0, 0, c);
      textRect.translate(labelOrigin.x, labelOrigin.y);

      g2.setPaint(label.getBackground());
      g2.fill(textRect);

      updateBuffer(label, textRect, c.getSize());
      g2.drawImage(buffer, 0, 0, c);
      g2.dispose();
    }
  }

  private void updateBuffer(JLabel label, Rectangle textRect, Dimension size) {
    int w = Math.max(1, size.width);
    int h = Math.max(1, size.height);
    if (buffer == null || buffer.getWidth() != w || buffer.getHeight() != h) {
      buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }
    Graphics2D g2 = buffer.createGraphics();
    AbstractShimmerLabel.applyRenderingHints(label, g2);
    g2.setComposite(AlphaComposite.Clear);
    g2.fillRect(0, 0, w, h);
    g2.setComposite(AlphaComposite.SrcOver);

    FontMetrics fm = g2.getFontMetrics(label.getFont());
    float x = textRect.x;
    float y = textRect.y + fm.getAscent();

    // 1. Draw the text outline to the shimmer buffer (to serve as a mask).
    g2.setPaint(label.getForeground());
    FontRenderContext frc = g2.getFontRenderContext();
    new TextLayout(label.getText(), label.getFont(), frc).draw(g2, x, y);

    // 2. Compose the gradient using SrcAtop (masked by the text's alpha channel).
    g2.setComposite(AlphaComposite.SrcAtop);
    Color[] colors = ShimmerColors.gradientColors(label.getForeground(), ALPHA);
    float endX = animX + BAND_WIDTH;
    g2.setPaint(new LinearGradientPaint(animX, 0f, endX, 0f, FRACTIONS, colors));
    g2.fillRect(0, 0, w, h);
    g2.dispose();
  }
}
