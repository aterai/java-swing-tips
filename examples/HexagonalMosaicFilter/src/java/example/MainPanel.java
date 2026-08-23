// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int MIN_RADIUS = 2;
  private static final int MAX_RADIUS = 40;
  private static final int DEFAULT_RADIUS = 16;

  private MainPanel() {
    super(new BorderLayout(5, 5));
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    split.setContinuousLayout(true);
    // Remove the 1px SplitPaneBorder (SplitPane.darkShadow) that would otherwise
    // draw a line over the top and left edges of both images.
    split.setBorder(BorderFactory.createEmptyBorder());
    split.setResizeWeight(.5);

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    BufferedImage image = Optional.ofNullable(cl.getResource("example/test.jpg")).map(u -> {
      BufferedImage img;
      try (InputStream s = u.openStream()) {
        img = ImageIO.read(s);
      } catch (IOException ex) {
        img = createMissingImage();
      }
      return img;
    }).orElseGet(MainPanel::createMissingImage);
    Icon imageIcon = new ImageIcon(image);

    Component beforeCanvas = new JComponent() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        imageIcon.paintIcon(this, g, 0, 0);
      }
    };
    split.setLeftComponent(beforeCanvas);

    FilteredCanvas afterCanvas = new FilteredCanvas(image);
    afterCanvas.setRadius(DEFAULT_RADIUS);
    split.setRightComponent(afterCanvas);

    JSlider slider = new JSlider(MIN_RADIUS, MAX_RADIUS, DEFAULT_RADIUS);
    slider.addChangeListener(e ->
        afterCanvas.setRadius(((JSlider) e.getSource()).getValue()));

    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.add(new JLabel("Radius:"), BorderLayout.WEST);
    p.add(slider);

    add(split);
    add(p, BorderLayout.SOUTH);
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
  }

  private static BufferedImage createMissingImage() {
    Icon missingIcon = new MissingIcon();
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
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
    frame.setResizable(false);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// Right half of the JSplitPane: shows the filtered image and rebuilds it
// whenever the hexagon radius changes.
class FilteredCanvas extends JComponent {
  private final transient BufferedImage image;
  private transient BufferedImage filtered;

  protected FilteredCanvas(BufferedImage image) {
    super();
    this.image = image;
  }

  public final void setRadius(int radius) {
    // Call BufferedImageOp#filter(...) directly instead of wrapping the op in a
    // BufferedImageFilter: FilteredImageSource loads its result asynchronously,
    // so every slider step would block the EDT in MediaTracker.
    BufferedImageOp op = new HexagonalMosaicFilter(radius);
    filtered = op.filter(image, null);
    repaint();
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (filtered != null) {
      // Draw the filtered image at the same position as the original in the
      // left component, so both halves join into a single seamless picture.
      g.translate(-getLocation().x + getParent().getInsets().left, 0);
      g.drawImage(filtered, 0, 0, this);
    }
  }
}

// Flat-top hexagonal tiling.
// `radius` is the circumradius of a hexagon, so its bounding box satisfies:
// W = radius * 2, H = radius * sqrt(3)
class HexagonalMosaicFilter implements BufferedImageOp {
  private static final double SQRT3 = Math.sqrt(3d);
  // Horizontal distance between the centers of two adjacent columns: radius * 3 / 2
  private static final double COLUMN_PITCH = 3d / 2d;
  // Pixel to axial coordinates conversion factors for a flat-top layout
  private static final double Q_SCALE = 2d / 3d;
  private static final double R_SCALE = 1d / 3d;
  // Hexagons along the border are only partly visible, so the grid is
  // one cell wider and taller on each side than the image itself
  private static final int MARGIN = 1;
  private final int radius;

  protected HexagonalMosaicFilter(int radius) {
    this.radius = radius;
  }

  @Override public BufferedImage filter(BufferedImage src, BufferedImage dst) {
    int width = src.getWidth();
    int height = src.getHeight();
    BufferedImage img = dst == null ? createCompatibleDestImage(src, null) : dst;
    if (img.getWidth() != width || img.getHeight() != height) {
      throw new IllegalArgumentException("src and dst must have the same size");
    }
    int columns = getGridSize(width, radius * COLUMN_PITCH);
    int rows = getGridSize(height, radius * SQRT3);
    int[] colors = getCellColors(src, columns, rows);
    int[] line = new int[width];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        // Every pixel belongs to exactly one hexagon, so the destination is
        // filled completely: no antialiased seams remain between the tiles.
        line[x] = colors[getCellIndex(x, y, columns, rows)];
      }
      img.setRGB(0, y, width, 1, line, 0, width);
    }
    return img;
  }

  private static int getGridSize(int length, double pitch) {
    return (int) Math.ceil(length / pitch) + MARGIN * 2 + 1;
  }

  // Average the source pixels per hexagon in a single pass.
  private int[] getCellColors(BufferedImage src, int columns, int rows) {
    int width = src.getWidth();
    int height = src.getHeight();
    int size = columns * rows;
    long[] sumA = new long[size];
    long[] sumR = new long[size];
    long[] sumG = new long[size];
    long[] sumB = new long[size];
    int[] count = new int[size];
    int[] line = new int[width];
    for (int y = 0; y < height; y++) {
      src.getRGB(0, y, width, 1, line, 0, width);
      for (int x = 0; x < width; x++) {
        int i = getCellIndex(x, y, columns, rows);
        int argb = line[x];
        long a = (argb >>> 24) & 0xFF;
        // Weight each color by its alpha, otherwise transparent pixels
        // would tint the average with their meaningless color values.
        sumA[i] += a;
        sumR[i] += ((argb >>> 16) & 0xFF) * a;
        sumG[i] += ((argb >>> 8) & 0xFF) * a;
        sumB[i] += (argb & 0xFF) * a;
        count[i]++;
      }
    }
    int[] colors = new int[size];
    for (int i = 0; i < size; i++) {
      long a = sumA[i];
      if (a > 0) {
        colors[i] = (int) (a / count[i]) << 24
            | (int) (sumR[i] / a) << 16
            | (int) (sumG[i] / a) << 8
            | (int) (sumB[i] / a);
      }
    }
    return colors;
  }

  // Look up the hexagon that contains the given pixel:
  // pixel -> axial coordinates -> cube rounding -> odd-q offset coordinates
  // https://www.redblobgames.com/grids/hexagons/
  private int getCellIndex(int x, int y, int columns, int rows) {
    double q = Q_SCALE * x / radius;
    double r = R_SCALE * (SQRT3 * y - x) / radius;
    double s = -q - r;
    long cq = Math.round(q);
    long cr = Math.round(r);
    long cs = Math.round(s);
    double dq = Math.abs(cq - q);
    double dr = Math.abs(cr - r);
    double ds = Math.abs(cs - s);
    // The component with the largest rounding error is recomputed
    // from the other two so that q + r + s == 0 still holds.
    if (dq > dr && dq > ds) {
      cq = -cr - cs;
    } else if (dr > ds) {
      cr = -cq - cs;
    }
    int col = (int) cq + MARGIN;
    int row = (int) (cr + ((cq - (cq & 1L)) >> 1)) + MARGIN;
    return clamp(col, columns) + clamp(row, rows) * columns;
  }

  private static int clamp(int value, int size) {
    return Math.min(Math.max(value, 0), size - 1);
  }

  @Override public Rectangle2D getBounds2D(BufferedImage src) {
    return new Rectangle2D.Double(
        0, 0, src.getWidth(), src.getHeight());
  }

  @Override public BufferedImage createCompatibleDestImage(
      BufferedImage src, ColorModel dstCm) {
    return new BufferedImage(
        src.getWidth(),
        src.getHeight(),
        BufferedImage.TYPE_INT_ARGB);
  }

  @Override public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
    Point2D pt = dstPt == null ? new Point2D.Double() : dstPt;
    pt.setLocation(srcPt);
    return pt;
  }

  @Override public RenderingHints getRenderingHints() {
    return new RenderingHints(Collections.emptyMap());
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;
    g2.setColor(Color.WHITE);
    g2.translate(x, y);
    g2.fillRect(0, 0, w, h);
    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(gap, gap, w - gap, h - gap);
    g2.drawLine(gap, h - gap, w - gap, gap);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 320;
  }

  @Override public int getIconHeight() {
    return 240;
  }
}
