// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String path = "example/screenshot.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Image img = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    ImageIcon icon = new ImageIcon(img);
    int width = icon.getIconWidth();
    int height = icon.getIconHeight();

    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    makeRoundedMemoryImageSource(img, width, height).ifPresent(producer -> {
      Graphics g = bi.createGraphics();
      g.drawImage(createImage(producer), 0, 0, this);
      g.dispose();
    });

    // BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    // Graphics2D g2 = bi.createGraphics();
    // g2.drawImage(image, 0, 0, null);
    // g2.setComposite(AlphaComposite.Clear);
    // g2.setPaint(new Color(255, 255, 255, 0));
    // // NW
    // g2.drawLine(0, 0, 4, 0);
    // g2.drawLine(0, 1, 2, 1);
    // g2.drawLine(0, 2, 1, 2);
    // g2.drawLine(0, 3, 0, 4);
    // // NE
    // g2.drawLine(width - 5, 0, width - 1, 0);
    // g2.drawLine(width - 3, 1, width - 1, 1);
    // g2.drawLine(width - 2, 2, width - 1, 2);
    // g2.drawLine(width - 1, 3, width - 1, 4);
    // g2.dispose();

    // try {
    //   ImageIO.write(bi, "png", File.createTempFile("screenshot", ".png"));
    // } catch (IOException ex) {
    //   ex.printStackTrace();
    // }

    CardLayout cardLayout = new CardLayout();
    JPanel p = new JPanel(cardLayout);
    p.add(new JLabel(new ImageIcon(img)), "original");
    p.add(new JLabel(new ImageIcon(bi)), "rounded");

    JCheckBox check = new JCheckBox("transparency at the rounded windows corners");
    check.addActionListener(e -> cardLayout.show(p, check.isSelected() ? "rounded" : "original"));

    add(check, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Area makeNorthWestCorner() {
    Area area = new Area();
    area.add(new Area(new Rectangle(0, 0, 5, 1)));
    area.add(new Area(new Rectangle(0, 1, 3, 1)));
    area.add(new Area(new Rectangle(0, 2, 2, 1)));
    area.add(new Area(new Rectangle(0, 3, 1, 2)));
    return area;
  }

  private static Optional<MemoryImageSource> makeRoundedMemoryImageSource(Image img, int w, int h) {
    int[] pix = new int[h * w];
    PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pix, 0, w);
    try {
      pg.grabPixels();
    } catch (InterruptedException ex) {
      // System.err.println("interrupted waiting for pixels!");
      ex.printStackTrace();
      Thread.currentThread().interrupt();
      return Optional.empty();
    }
    if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
      // System.err.println("image fetch aborted or error");
      return Optional.empty();
    }

    Area area = makeNorthWestCorner();
    Rectangle r = area.getBounds();

    Shape s = area; // NW
    for (int y = 0; y < r.height; y++) {
      for (int x = 0; x < r.width; x++) {
        if (s.contains(x, y)) {
          pix[x + y * w] = 0x0;
        }
      }
    }
    AffineTransform at = AffineTransform.getScaleInstance(-1d, 1d);
    at.translate(-w, 0);
    s = at.createTransformedShape(area); // NE
    for (int y = 0; y < r.height; y++) {
      for (int x = w - r.width; x < w; x++) {
        if (s.contains(x, y)) {
          pix[x + y * w] = 0x0;
        }
      }
    }

    // at = AffineTransform.getScaleInstance(1.0, -1.0);
    // at.translate(0, -height);
    // s = at.createTransformedShape(area); // SE
    // for (int x = 0; x < r.width; x++) {
    //   for (int y = height - r.height; y < height; y++) {
    //     if (s.contains(x, y)) {
    //       pix[y * width + x] = 0x0;
    //     }
    //   }
    // }

    // // NW
    // for (int y = 0; y < 5; y++) {
    //   for (int x = 0; x < 5; x++) {
    //     if (y == 0 && x < 5 || y == 1 && x < 3 ||
    //       y == 2 && x < 2 || y == 3 && x < 1 ||
    //       y == 4 && x < 1) {
    //       pix[y * width + x] = 0x0;
    //     }
    //   }
    // }
    // // NE
    // for (int y = 0; y < 5; y++) {
    //   for (int x = width - 5; x < width; x++) {
    //     if (y == 0 && x >= width - 5 || y == 1 && x >= width - 3 ||
    //       y == 2 && x >= width - 2 || y == 3 && x >= width - 1 ||
    //       y == 4 && x >= width - 1) {
    //       pix[y * width + x] = 0x0;
    //     }
    //   }
    // }
    // int n=0;
    // for (int y = 0; y < 5; y++) {
    //   for (int x = 0; x < width; x++) {
    //     n = y * width + x;
    //     if (x >= 5 && x < width - 5) { continue; }
    //     else if (y == 0 && (x < 5 || x >= width - 5)) { pix[n] = 0x0; }
    //     else if (y == 1 && (x < 3 || x >= width - 3)) { pix[n] = 0x0; }
    //     else if (y == 2 && (x < 2 || x >= width - 2)) { pix[n] = 0x0; }
    //     else if (y == 3 && (x < 1 || x >= width - 1)) { pix[n] = 0x0; }
    //     else if (y == 4 && (x < 1 || x >= width - 1)) { pix[n] = 0x0; }
    //   }
    // }

    return Optional.of(new MemoryImageSource(w, h, pix, 0, w));
  }

  private static Image makeMissingImage() {
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

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();

    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;

    g2.setColor(Color.GRAY);
    g2.fillRect(x, y, w, h);

    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(x + gap, y + gap, x + w - gap, y + h - gap);
    g2.drawLine(x + gap, y + h - gap, x + w - gap, y + gap);

    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 240;
  }

  @Override public int getIconHeight() {
    return 160;
  }
}
