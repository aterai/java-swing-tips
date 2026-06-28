// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class MainPanel extends JPanel {
  private final Timer animationTimer = new Timer(16, null);

  private MainPanel() {
    super(new GridBagLayout());
    JLabel label = new JLabel("Glowing Border Animation") {
      @Override public Dimension getPreferredSize() {
        return new Dimension(260, 100);
      }
    };
    label.setForeground(new Color(0xCC_CC_DD));
    label.setHorizontalAlignment(SwingConstants.CENTER);

    GlowingBorder glowingBorder = new GlowingBorder(14, 24, 14, 24);
    label.setBorder(glowingBorder);

    animationTimer.addActionListener(e -> {
      glowingBorder.requestNextFrame();
      label.repaint();
    });
    animationTimer.start();

    add(label);
    setBackground(new Color(0x1E_1E_1E));
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void removeNotify() {
    animationTimer.stop();
    super.removeNotify();
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

class GlowingBorder extends EmptyBorder {
  private static final float STROKE = 1f;
  private static final float ARC = 16f;
  private static final float ORIGIN_X = .50f;
  private static final float ORIGIN_Y = .50f;
  private static final double ROTATION_SPEED = 1d / 120d;
  private static final float[] FRACTIONS = {0f, .04f, .13f, .22f, 1f};
  private static final Color[] COLORS = {
      new Color(0x00_00_7A_CC, true),
      new Color(0xBF_00_7A_CC, true),
      new Color(0xFF_4F_C1_FF, true),
      new Color(0xBF_00_5F_B8, true),
      new Color(0x00_00_5F_B8, true),
  };
  private final AtomicReference<BufferedImage> frameBuffer = new AtomicReference<>(null);
  private final AtomicBoolean isWorkerBusy = new AtomicBoolean(false);
  private final Rectangle cachedBounds = new Rectangle();
  private final Rectangle scanRect = new Rectangle();
  private double currentAngle;

  protected GlowingBorder(int top, int left, int bottom, int right) {
    super(top, left, bottom, right);
  }

  public void requestNextFrame() {
    currentAngle += ROTATION_SPEED * 2 * Math.PI;
    if (isWorkerBusy.compareAndSet(false, true)) {
      new SwingWorker<BufferedImage, Void>() {
        @Override protected BufferedImage doInBackground() {
          return cachedBounds.isEmpty() || scanRect.isEmpty()
              ? null
              : renderFrame(cachedBounds, scanRect, currentAngle);
        }

        @Override protected void done() {
          try {
            BufferedImage img = get();
            if (img != null) {
              frameBuffer.set(img);
            }
          } catch (InterruptedException | ExecutionException ignored) {
            Thread.currentThread().interrupt();
            Toolkit.getDefaultToolkit().beep();
          } finally {
            isWorkerBusy.set(false);
          }
        }
      }.execute();
    }
  }

  // Eliminate jaggies without clipping with two-stage synthesis of Src + SrcAtop.
  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Shape outer = new RoundRectangle2D.Float(x, y, width - 1f, height - 1f, ARC, ARC);
    Shape inner = new RoundRectangle2D.Float(
        x + STROKE,
        y + STROKE,
        width - STROKE * 2 - 1f,
        height - STROKE * 2 - 1f,
        Math.max(2f, ARC - STROKE * 2),
        Math.max(2f, ARC - STROKE * 2));
    Area borderMask = new Area(outer);
    borderMask.subtract(new Area(inner));

    // AABB cache update (only when size changes)
    if (width != cachedBounds.width || height != cachedBounds.height) {
      Rectangle2D bounds = borderMask.getBounds2D();
      // Add 1px margin to prevent AA edges from being clipped
      scanRect.x = Math.max(0, (int) Math.floor(bounds.getX()) - 1);
      scanRect.y = Math.max(0, (int) Math.floor(bounds.getY()) - 1);
      int scanX2 = Math.min(width, (int) Math.ceil(bounds.getMaxX()) + 1);
      int scanY2 = Math.min(height, (int) Math.ceil(bounds.getMaxY()) + 1);
      scanRect.width = scanX2 - scanRect.x;
      scanRect.height = scanY2 - scanRect.y;
      cachedBounds.width = width;
      cachedBounds.height = height;
      frameBuffer.set(null);
    }

    // [1] Paint borderMask on offscreen with Src
    // Src: dst = src (completely replaces existing content)
    // Inside the borderMask: white with alpha=255, outside the shape: leave alpha=0
    BufferedImage offscreen = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = offscreen.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setComposite(AlphaComposite.Src);
    g2.fill(borderMask);

    // [2] Overlay the base color and light with SrcAtop
    // SrcAtop: dst.alpha does not change, src rides only in the area where dst.alpha>0
    // → Draw only inside the borderMask (alpha=255),
    //   outside the shape (alpha=0) remains unchanged
    g2.setComposite(AlphaComposite.SrcAtop);
    // VS Code の入力フィールド背景色風 (#252526)
    g2.setColor(new Color(0x25_25_26));
    g2.fill(borderMask);

    // VS Code のデフォルトの境界線色風 (#3C3C3C)
    g2.setColor(new Color(0x3C_3C_3C));
    g2.draw(outer);
    g2.draw(inner);

    BufferedImage frame = frameBuffer.get();
    if (frame != null) {
      g2.drawImage(frame, x, y, null);
    }
    g2.dispose();

    // [3] Combine offscreen into original g using drawImage
    g.drawImage(offscreen, 0, 0, null);
  }

  // Scan a rectangle within AABB of Border.
  // The scan range `r` is the rectangle obtained from getBounds2D() of borderMask.
  // Perform conic calculation by scanning all pixels within this rectangle.
  private static BufferedImage renderFrame(Rectangle f, Rectangle r, double startAngle) {
    int fw = f.width;
    int fh = f.height;
    BufferedImage img = new BufferedImage(fw, fh, BufferedImage.TYPE_INT_ARGB);
    BufferedImage layer = new BufferedImage(fw, fh, BufferedImage.TYPE_INT_ARGB);
    int cx = (int) (ORIGIN_X * fw);
    int cy = (int) (ORIGIN_Y * fh);
    for (int py = r.y; py < r.y + r.height; py++) {
      for (int px = r.x; px < r.x + r.width; px++) {
        double angle = Math.atan2(py - cy, px - cx) - startAngle;
        double v = angle / (2 * Math.PI);
        double t = (v % 1.0 + 1.0) % 1.0;
        int layerArgb = interpolateColorRgb(t);
        if ((layerArgb >>> 24) == 0) {
          continue;
        }
        layer.setRGB(px, py, layerArgb);
      }
    }

    Graphics2D g2 = img.createGraphics();
    try {
      g2.setComposite(AlphaComposite.SrcOver);
      g2.drawImage(layer, 0, 0, null);
    } finally {
      g2.dispose();
    }
    return img;
  }

  @SuppressWarnings({"PMD.OnlyOneReturn", "ReturnCount"})
  private static int interpolateColorRgb(double t) {
    if (t <= FRACTIONS[0]) {
      return 0;
    }
    int last = FRACTIONS.length - 1;
    if (t >= FRACTIONS[last]) {
      return COLORS[last].getRGB();
    }
    for (int i = 0; i < last; i++) {
      if (t <= FRACTIONS[i + 1]) {
        float ratio = (float) ((t - FRACTIONS[i]) / (FRACTIONS[i + 1] - FRACTIONS[i]));
        return interpolateArgb(COLORS[i].getRGB(), COLORS[i + 1].getRGB(), ratio);
      }
    }
    return 0;
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
