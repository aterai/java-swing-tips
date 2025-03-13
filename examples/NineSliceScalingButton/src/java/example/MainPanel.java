// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // symbol_scale_2.jpg: Real World Illustrator: Understanding 9-Slice Scaling
    // https://rwillustrator.blogspot.jp/2007/04/understanding-9-slice-scaling.html
    BufferedImage img = makeBufferedImage("example/symbol_scale_2.jpg");
    JButton b1 = new ScalingButton("Scaling", img);
    JButton b2 = new NineSliceScalingButton("9-Slice Scaling", img);

    JPanel p1 = new JPanel(new GridLayout(1, 2, 5, 5));
    p1.add(b1);
    p1.add(b2);

    BufferedImage bi = makeBufferedImage("example/blue.png");
    JButton b3 = new JButton("Scaling Icon", new NineSliceScalingIcon(bi, 0, 0, 0, 0));
    b3.setContentAreaFilled(false);
    b3.setBorder(BorderFactory.createEmptyBorder());
    b3.setForeground(Color.WHITE);
    b3.setHorizontalTextPosition(SwingConstants.CENTER);
    b3.setPressedIcon(new NineSliceScalingIcon(makeImage(bi, new PressedFilter()), 0, 0, 0, 0));
    b3.setRolloverIcon(new NineSliceScalingIcon(makeImage(bi, new RolloverFilter()), 0, 0, 0, 0));

    JButton b4 = new JButton("9-Slice Scaling Icon", new NineSliceScalingIcon(bi, 8, 8, 8, 8));
    b4.setContentAreaFilled(false);
    b4.setBorder(BorderFactory.createEmptyBorder());
    b4.setForeground(Color.WHITE);
    b4.setHorizontalTextPosition(SwingConstants.CENTER);
    b4.setPressedIcon(new NineSliceScalingIcon(makeImage(bi, new PressedFilter()), 8, 8, 8, 8));
    b4.setRolloverIcon(new NineSliceScalingIcon(makeImage(bi, new RolloverFilter()), 8, 8, 8, 8));

    JPanel p2 = new JPanel(new GridLayout(1, 2, 5, 5));
    p2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p2.add(b3);
    p2.add(b4);

    add(p1);
    add(p2, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private BufferedImage makeBufferedImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    // ImageIcon ic = new ImageIcon(url);
    // int w = ic.getIconWidth();
    // int h = ic.getIconHeight();
    // BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    // Graphics2D g2 = bi.createGraphics();
    // ic.paintIcon(this, g2, 0, 0);
    // g2.dispose();
    // return bi;
  }

  private static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(124, 124, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (124 - iw) / 2, (124 - ih) / 2);
    g2.dispose();
    return bi;
  }

  private static BufferedImage makeImage(BufferedImage src, ImageFilter filter) {
    ImageProducer ip = src.getSource();
    Image img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(ip, filter));
    int w = img.getWidth(null);
    int h = img.getHeight(null);
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics g = bi.createGraphics();
    g.drawImage(img, 0, 0, null);
    g.dispose();
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
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ScalingButton extends JButton {
  private final transient BufferedImage image;

  protected ScalingButton(String title, BufferedImage image) {
    super(title, null);
    this.image = image;
  }

  @Override public void updateUI() {
    super.updateUI();
    setContentAreaFilled(false);
  }

  // @Override public Dimension getPreferredSize() {
  //   Insets i = getInsets();
  //   return new Dimension(image.getWidth(this) + i.right + i.left, 80);
  // }

  // @Override public Dimension getMinimumSize() {
  //   return getPreferredSize();
  // }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int bw = getWidth();
    int bh = getHeight();
    g2.drawImage(image, 0, 0, bw, bh, this);
    g2.dispose();
    super.paintComponent(g);
  }
}

class NineSliceScalingButton extends JButton {
  private final transient BufferedImage image;

  protected NineSliceScalingButton(String title, BufferedImage image) {
    super(title, null);
    this.image = image;
  }

  @Override public void updateUI() {
    super.updateUI();
    setContentAreaFilled(false);
  }

  // @Override public Dimension getPreferredSize() {
  //   Dimension dim = super.getPreferredSize();
  //   return new Dimension(dim.width + lw + rw, dim.height + th + bh);
  // }

  // @Override public Dimension getMinimumSize() {
  //   return getPreferredSize();
  // }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int iw = image.getWidth(this);
    int ih = image.getHeight(this);
    int ww = getWidth();
    int hh = getHeight();

    int lw = 37;
    int rw = 36;
    int th = 36;
    int bh = 36;

    g2.drawImage(
        image.getSubimage(lw, th, iw - lw - rw, ih - th - bh),
        lw, th, ww - lw - rw, hh - th - bh, this);

    g2.drawImage(
        image.getSubimage(lw, 0, iw - lw - rw, th),
        lw, 0, ww - lw - rw, th, this);
    g2.drawImage(
        image.getSubimage(lw, ih - bh, iw - lw - rw, bh),
        lw, hh - bh, ww - lw - rw, bh, this);
    g2.drawImage(
        image.getSubimage(0, th, lw, ih - th - bh),
        0, th, lw, hh - th - bh, this);
    g2.drawImage(
        image.getSubimage(iw - rw, th, rw, ih - th - bh),
        ww - rw, th, rw, hh - th - bh, this);

    g2.drawImage(
        image.getSubimage(0, 0, lw, th), 0, 0, this);
    g2.drawImage(
        image.getSubimage(iw - rw, 0, rw, th),
        ww - rw, 0, this);
    g2.drawImage(
        image.getSubimage(0, ih - bh, lw, bh),
        0, hh - bh, this);
    g2.drawImage(
        image.getSubimage(iw - rw, ih - bh, rw, bh),
        ww - rw, hh - bh, this);

    g2.dispose();
    super.paintComponent(g);
  }
}

class NineSliceScalingIcon implements Icon {
  private static final Rectangle RECT = new Rectangle();
  private final BufferedImage image;
  private final int lw;
  private final int rw;
  private final int th;
  private final int bh;
  private int width;
  private int height;

  protected NineSliceScalingIcon(BufferedImage image, int lw, int rw, int th, int bh) {
    this.image = image;
    this.lw = lw;
    this.rw = rw;
    this.th = th;
    this.bh = bh;
  }

  @Override public int getIconWidth() {
    return width; // Math.max(image.getWidth(null), width);
  }

  @Override public int getIconHeight() {
    return Math.max(image.getHeight(null), height);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // g2.translate(x, y); // 1.8.0: work fine?

    JComponent jc = c instanceof JComponent ? (JComponent) c : null;
    RECT.setBounds(c.getBounds());
    SwingUtilities.calculateInnerArea(jc, RECT);
    width = RECT.width;
    height = RECT.height;

    int iw = image.getWidth(c);
    int ih = image.getHeight(c);

    g2.drawImage(
        image.getSubimage(lw, th, iw - lw - rw, ih - th - bh),
        lw, th, width - lw - rw, height - th - bh, c);

    if (lw > 0 && rw > 0 && th > 0 && bh > 0) {
      g2.drawImage(
          image.getSubimage(lw, 0, iw - lw - rw, th),
          lw, 0, width - lw - rw, th, c);
      g2.drawImage(
          image.getSubimage(lw, ih - bh, iw - lw - rw, bh),
          lw, height - bh, width - lw - rw, bh, c);
      g2.drawImage(
          image.getSubimage(0, th, lw, ih - th - bh),
          0, th, lw, height - th - bh, c);
      g2.drawImage(
          image.getSubimage(iw - rw, th, rw, ih - th - bh),
          width - rw, th, rw, height - th - bh, c);

      g2.drawImage(
          image.getSubimage(0, 0, lw, th),
          0, 0, c);
      g2.drawImage(
          image.getSubimage(iw - rw, 0, rw, th),
          width - rw, 0, c);
      g2.drawImage(
          image.getSubimage(0, ih - bh, lw, bh),
          0, height - bh, c);
      g2.drawImage(
          image.getSubimage(iw - rw, ih - bh, rw, bh),
          width - rw, height - bh, c);
    }

    g2.dispose();
  }
}

class PressedFilter extends RGBImageFilter {
  @Override public int filterRGB(int x, int y, int argb) {
    int r = Math.round(((argb >> 16) & 0xFF) * .6f);
    return argb & 0xFF_00_FF_FF | r << 16;
  }
}

class RolloverFilter extends RGBImageFilter {
  @Override public int filterRGB(int x, int y, int argb) {
    // int r = (argb >> 16) & 0xFF;
    int g = Math.min(0xFF, Math.round(((argb >> 8) & 0xFF) * 1.5f));
    int b = Math.min(0xFF, Math.round((argb & 0xFF) * 1.5f));
    return argb & 0xFF_FF_00_00 | g << 8 | b;
  }
}
