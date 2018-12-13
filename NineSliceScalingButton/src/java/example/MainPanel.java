package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    BufferedImage bi = null;
    try {
      // symbol_scale_2.jpg: Real World Illustrator: Understanding 9-Slice Scaling
      // https://rwillustrator.blogspot.jp/2007/04/understanding-9-slice-scaling.html
      bi = ImageIO.read(getClass().getResource("symbol_scale_2.jpg"));
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    JButton b1 = new ScalingButton("Scaling", bi);
    JButton b2 = new NineSliceScalingButton("9-Slice Scaling", bi);

    JPanel p1 = new JPanel(new GridLayout(1, 2, 5, 5));
    p1.add(b1);
    p1.add(b2);

    try {
      bi = ImageIO.read(getClass().getResource("blue.png"));
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    JButton b3 = new JButton("Scaling Icon", new NineSliceScalingIcon(bi, 0, 0, 0, 0));
    b3.setContentAreaFilled(false);
    b3.setBorder(BorderFactory.createEmptyBorder());
    b3.setForeground(Color.WHITE);
    b3.setHorizontalTextPosition(SwingConstants.CENTER);
    b3.setPressedIcon(new NineSliceScalingIcon(makeFilteredImage(bi, new PressedImageFilter()), 0, 0, 0, 0));
    b3.setRolloverIcon(new NineSliceScalingIcon(makeFilteredImage(bi, new RolloverImageFilter()), 0, 0, 0, 0));

    JButton b4 = new JButton("9-Slice Scaling Icon", new NineSliceScalingIcon(bi, 8, 8, 8, 8));
    b4.setContentAreaFilled(false);
    b4.setBorder(BorderFactory.createEmptyBorder());
    b4.setForeground(Color.WHITE);
    b4.setHorizontalTextPosition(SwingConstants.CENTER);
    b4.setPressedIcon(new NineSliceScalingIcon(makeFilteredImage(bi, new PressedImageFilter()), 8, 8, 8, 8));
    b4.setRolloverIcon(new NineSliceScalingIcon(makeFilteredImage(bi, new RolloverImageFilter()), 8, 8, 8, 8));

    JPanel p2 = new JPanel(new GridLayout(1, 2, 5, 5));
    p2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p2.add(b3);
    p2.add(b4);

    add(p1);
    add(p2, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static BufferedImage makeFilteredImage(BufferedImage src, ImageFilter filter) {
    ImageProducer ip = src.getSource();
    Image img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(ip, filter));
    BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    Graphics g = bi.createGraphics();
    g.drawImage(img, 0, 0, null);
    g.dispose();
    return bi;
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
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
    super();
    this.image = image;
    setModel(new DefaultButtonModel());
    init(title, null);
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
    super();
    this.image = image;
    setModel(new DefaultButtonModel());
    init(title, null);
    setContentAreaFilled(false);
  }

  // @Override public Dimension getPreferredSize() {
  //   Dimension dim = super.getPreferredSize();
  //   return new Dimension(dim.width + leftw + rightw, dim.height + toph + bottomh);
  // }

  // @Override public Dimension getMinimumSize() {
  //   return getPreferredSize();
  // }

  @SuppressWarnings("checkstyle:linelength")
  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int iw = image.getWidth(this);
    int ih = image.getHeight(this);
    int bw = getWidth();
    int bh = getHeight();

    int leftw = 37;
    int rightw = 36;
    int toph = 36;
    int bottomh = 36;

    g2.drawImage(image.getSubimage(leftw, toph, iw - leftw - rightw, ih - toph - bottomh), leftw, toph, bw - leftw - rightw, bh - toph - bottomh, this);

    g2.drawImage(image.getSubimage(leftw, 0, iw - leftw - rightw, toph), leftw, 0, bw - leftw - rightw, toph, this);
    g2.drawImage(image.getSubimage(leftw, ih - bottomh, iw - leftw - rightw, bottomh), leftw, bh - bottomh, bw - leftw - rightw, bottomh, this);
    g2.drawImage(image.getSubimage(0, toph, leftw, ih - toph - bottomh), 0, toph, leftw, bh - toph - bottomh, this);
    g2.drawImage(image.getSubimage(iw - rightw, toph, rightw, ih - toph - bottomh), bw - rightw, toph, rightw, bh - toph - bottomh, this);

    g2.drawImage(image.getSubimage(0, 0, leftw, toph), 0, 0, this);
    g2.drawImage(image.getSubimage(iw - rightw, 0, rightw, toph), bw - rightw, 0, this);
    g2.drawImage(image.getSubimage(0, ih - bottomh, leftw, bottomh), 0, bh - bottomh, this);
    g2.drawImage(image.getSubimage(iw - rightw, ih - bottomh, rightw, bottomh), bw - rightw, bh - bottomh, this);

    g2.dispose();
    super.paintComponent(g);
  }
}

class NineSliceScalingIcon implements Icon {
  private final BufferedImage image;
  private final int leftw;
  private final int rightw;
  private final int toph;
  private final int bottomh;
  private int width;
  private int height;

  protected NineSliceScalingIcon(BufferedImage image, int leftw, int rightw, int toph, int bottomh) {
    this.image = image;
    this.leftw = leftw;
    this.rightw = rightw;
    this.toph = toph;
    this.bottomh = bottomh;
  }

  @Override public int getIconWidth() {
    return width; // Math.max(image.getWidth(null), width);
  }

  @Override public int getIconHeight() {
    return Math.max(image.getHeight(null), height);
  }

  @SuppressWarnings("checkstyle:linelength")
  @Override public void paintIcon(Component cmp, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Insets i;
    if (cmp instanceof JComponent) {
      i = ((JComponent) cmp).getBorder().getBorderInsets(cmp);
    } else {
      i = new Insets(0, 0, 0, 0);
    }

    // g2.translate(x, y); // 1.8.0: work fine?

    int iw = image.getWidth(cmp);
    int ih = image.getHeight(cmp);
    width = cmp.getWidth() - i.left - i.right;
    height = cmp.getHeight() - i.top - i.bottom;

    g2.drawImage(image.getSubimage(leftw, toph, iw - leftw - rightw, ih - toph - bottomh), leftw, toph, width - leftw - rightw, height - toph - bottomh, cmp);

    if (leftw > 0 && rightw > 0 && toph > 0 && bottomh > 0) {
      g2.drawImage(image.getSubimage(leftw, 0, iw - leftw - rightw, toph), leftw, 0, width - leftw - rightw, toph, cmp);
      g2.drawImage(image.getSubimage(leftw, ih - bottomh, iw - leftw - rightw, bottomh), leftw, height - bottomh, width - leftw - rightw, bottomh, cmp);
      g2.drawImage(image.getSubimage(0, toph, leftw, ih - toph - bottomh), 0, toph, leftw, height - toph - bottomh, cmp);
      g2.drawImage(image.getSubimage(iw - rightw, toph, rightw, ih - toph - bottomh), width - rightw, toph, rightw, height - toph - bottomh, cmp);

      g2.drawImage(image.getSubimage(0, 0, leftw, toph), 0, 0, cmp);
      g2.drawImage(image.getSubimage(iw - rightw, 0, rightw, toph), width - rightw, 0, cmp);
      g2.drawImage(image.getSubimage(0, ih - bottomh, leftw, bottomh), 0, height - bottomh, cmp);
      g2.drawImage(image.getSubimage(iw - rightw, ih - bottomh, rightw, bottomh), width - rightw, height - bottomh, cmp);
    }

    g2.dispose();
  }
}

class PressedImageFilter extends RGBImageFilter {
  @Override public int filterRGB(int x, int y, int argb) {
    int r = (int) (((argb >> 16) & 0xFF) * .6);
    int g = (int) (((argb >> 8) & 0xFF) * 1d);
    int b = (int) ((argb & 0xFF) * 1d);
    return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
  }
}

class RolloverImageFilter extends RGBImageFilter {
  @Override public int filterRGB(int x, int y, int argb) {
    int r = (int) Math.min(0xFF, ((argb >> 16) & 0xFF) * 1d);
    int g = (int) Math.min(0xFF, ((argb >> 8) & 0xFF) * 1.5);
    int b = (int) Math.min(0xFF, (argb & 0xFF) * 1.5);
    return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
  }
}
