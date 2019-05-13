// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new ColorWheel());
    setPreferredSize(new Dimension(320, 240));
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ColorWheel extends JPanel {
  private static final int SIZE = 180;
  private final transient BufferedImage image;

  public ColorWheel() {
    super();
    image = updateImage();
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void paintComponent(Graphics g) {
    super.paintComponent(g);

    // SIZE = 32 * 6; // Drawing breaks on Corretto 1.8.0_212
    int s = SIZE;
    Graphics2D g2 = (Graphics2D) g.create();

    // Soft Clipping
    GraphicsConfiguration gc = g2.getDeviceConfiguration();
    BufferedImage buf = gc.createCompatibleImage(s, s, Transparency.TRANSLUCENT);
    Graphics2D g2d = buf.createGraphics();

    g2d.setComposite(AlphaComposite.Src);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.fill(new Ellipse2D.Float(0f, 0f, s, s));

    g2d.setComposite(AlphaComposite.SrcAtop);
    g2d.drawImage(image, 0, 0, null);
    g2d.dispose();

    g2.drawImage(buf, null, (getWidth() - s) / 2, (getHeight() - s) / 2);
    g2.dispose();
  }

  // Colors: a Color Dialog | Java Graphics
  // https://javagraphics.blogspot.com/2007/04/jcolorchooser-making-alternative.html
  //   https://javagraphics.java.net/
  //   http://www.javased.com/index.php?source_dir=SPREAD/src/colorpicker/swing/ColorPickerPanel.java
  private BufferedImage updateImage() {
    BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
    int[] row = new int[SIZE];
    float size = (float) SIZE;
    float radius = size / 2f;

    for (int yidx = 0; yidx < SIZE; yidx++) {
      float y = yidx - size / 2f;
      for (int xidx = 0; xidx < SIZE; xidx++) {
        float x = xidx - size / 2f;
        double theta = Math.atan2(y, x) - 3d * Math.PI / 2d;
        if (theta < 0) {
          theta += 2d * Math.PI;
        }
        double r = Math.sqrt(x * x + y * y);
        float hue = (float) (theta / (2d * Math.PI));
        float sat = Math.min((float) (r / radius), 1f);
        float bri = 1f;
        row[xidx] = Color.HSBtoRGB(hue, sat, bri);
      }
      image.getRaster().setDataElements(0, yidx, SIZE, 1, row);
    }
    return image;
  }
}
