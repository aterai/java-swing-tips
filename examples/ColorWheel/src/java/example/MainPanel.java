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

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
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

class ColorWheel extends JPanel {
  private static final int SIZE = 180;
  private final Image image = makeColorWheelImage();

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
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
    g2d.drawImage(image, 0, 0, this);
    g2d.dispose();

    g2.drawImage(buf, (getWidth() - s) / 2, (getHeight() - s) / 2, this);
    g2.dispose();
  }

  // Colors: a Color Dialog | Java Graphics
  // https://javagraphics.blogspot.com/2007/04/jcolorchooser-making-alternative.html
  //   https://javagraphics.java.net/
  //   http://www.javased.com/index.php?source_dir=SPREAD/src/colorpicker/swing/ColorPickerPanel.java
  private Image makeColorWheelImage() {
    BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
    int[] row = new int[SIZE];
    float size = SIZE;
    float radius = size / 2f;
    for (int yi = 0; yi < SIZE; yi++) {
      float y = yi - radius;
      for (int xi = 0; xi < SIZE; xi++) {
        float x = xi - radius;
        double theta = Math.atan2(y, x) - 3d * Math.PI / 2d;
        if (theta < 0) {
          theta += 2d * Math.PI;
        }
        double r = Math.hypot(x, y); // Math.sqrt(x * x + y * y);
        float hue = (float) (theta / (2d * Math.PI));
        float sat = Math.min((float) (r / radius), 1f);
        float bri = 1f;
        row[xi] = Color.HSBtoRGB(hue, sat, bri);
      }
      img.getRaster().setDataElements(0, yi, SIZE, 1, row);
    }
    return img;
  }
}
