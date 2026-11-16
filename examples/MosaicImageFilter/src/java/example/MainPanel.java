// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageFilter;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    split.setContinuousLayout(true);
    split.setResizeWeight(.5);

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Image image = Optional.ofNullable(cl.getResource("example/test.jpg")).map(u -> {
      Image img;
      try (InputStream is = u.openStream()) {
        img = ImageIO.read(is);
      } catch (IOException ex) {
        img = createMissingImage();
      }
      return img;
    }).orElseGet(MainPanel::createMissingImage);
    Icon sourceIcon = new ImageIcon(image);

    Component beforeCanvas = new JComponent() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        sourceIcon.paintIcon(this, g, 0, 0);
      }
    };
    split.setLeftComponent(beforeCanvas);

    ImageFilter filter = new BufferedImageFilter(new MosaicImageFilter(16));
    ImageProducer producer = new FilteredImageSource(image.getSource(), filter);
    Image mosaicImage = Toolkit.getDefaultToolkit().createImage(producer);
    Icon mosaicIcon = new ImageIcon(mosaicImage);
    Component afterCanvas = new JComponent() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.translate(-getLocation().x + split.getInsets().left, 0);
        mosaicIcon.paintIcon(this, g, 0, 0);
      }
    };
    split.setRightComponent(afterCanvas);

    add(split);
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Image createMissingImage() {
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

class MosaicImageFilter implements BufferedImageOp {
  private final int blockSize;

  /* default */ MosaicImageFilter(int blockSize) {
    this.blockSize = blockSize;
  }

  @Override public BufferedImage filter(BufferedImage src, BufferedImage dst) {
    int width = src.getWidth();
    int height = src.getHeight();
    BufferedImage dest = dst == null ? createCompatibleDestImage(src, null) : dst;
    int[] pixels = new int[blockSize * blockSize];
    for (int y = 0; y < height; y += blockSize) {
      for (int x = 0; x < width; x += blockSize) {
        int w = Math.min(blockSize, width - x);
        int h = Math.min(blockSize, height - y);
        // getRGB/setRGB always use the default ARGB color model, so this works
        // for any image type (Raster#getDataElements(...) would require an
        // int-packed raster and could lose the alpha channel of TYPE_INT_RGB)
        src.getRGB(x, y, w, h, pixels, 0, w);
        updatePixels(pixels, w, h);
        dest.setRGB(x, y, w, h, pixels, 0, w);
      }
    }
    return dest;
  }

  public static int getBlockRgb(int[] pixels, int w, int h) {
    int r = 0;
    int g = 0;
    int b = 0;
    int size = w * h;
    for (int i = 0; i < size; i++) {
      int argb = pixels[i];
      r += (argb >> 16) & 0xFF;
      g += (argb >> 8) & 0xFF;
      b += argb & 0xFF;
    }
    return (r / size) << 16 | (g / size) << 8 | (b / size);
  }

  public static void updatePixels(int[] pixels, int w, int h) {
    int rgb = getBlockRgb(pixels, w, h);
    // the block is stored contiguously (scansize == w), so a single loop is enough
    for (int i = 0; i < w * h; i++) {
      pixels[i] = (pixels[i] & 0xFF_00_00_00) | rgb;
    }
  }

  @Override public Rectangle2D getBounds2D(BufferedImage src) {
    // the mosaic does not change the geometry of the image
    return src.getRaster().getBounds();
  }

  @Override public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCm) {
    return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
  }

  @Override public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
    Point2D pt = dstPt == null ? (Point2D) srcPt.clone() : dstPt;
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
