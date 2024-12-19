// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Image ia = makeImage("example/a.png");
    Image ib = makeImage("example/b.png");
    Icon iia = new ImageIcon(ia);
    Icon iib = new ImageIcon(ib);
    JLabel label = new JLabel(iia);
    int w = iia.getIconWidth();
    int h = iia.getIconHeight();
    int[] pixelsA = getData(ia, w, h);
    int[] pixelsB = getData(ib, w, h);
    for (int i = 0; i < pixelsA.length; i++) {
      if (pixelsA[i] == pixelsB[i]) {
        pixelsA[i] = pixelsA[i] & 0x44_FF_FF_FF;
      }
    }

    JRadioButton ra = new JRadioButton("a.png", true);
    ra.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        label.setIcon(iia);
      }
    });

    JRadioButton rb = new JRadioButton("b.png");
    rb.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        label.setIcon(iib);
      }
    });

    MemoryImageSource source = new MemoryImageSource(w, h, pixelsA, 0, w);
    JRadioButton rr = new JRadioButton("diff");
    rr.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        label.setIcon(new ImageIcon(createImage(source)));
      }
    });

    JPanel p = new JPanel();
    ButtonGroup bg = new ButtonGroup();
    Stream.of(ra, rb, rr).forEach(r -> {
      bg.add(r);
      p.add(r);
    });

    add(label);
    add(p, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  // private BufferedImage makeBI(String str) {
  //   BufferedImage image;
  //   try {
  //     image = ImageIO.read(getClass().getResource(str));
  //   } catch (IOException ex) {
  //     ex.printStackTrace();
  //     return null;
  //   }
  //   return image;
  // }

  private static int[] getData(Image img, int w, int h) {
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics g = image.createGraphics();
    g.drawImage(img, 0, 0, null);
    g.dispose();
    // return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    int[] pixels = new int[w * h];
    EventQueue systemEventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
    SecondaryLoop loop = systemEventQueue.createSecondaryLoop();
    Thread worker = new Thread(() -> {
      try {
        new PixelGrabber(image, 0, 0, w, h, pixels, 0, w).grabPixels();
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
      loop.exit();
    });
    worker.start();
    loop.enter();
    return pixels;
  }

  private static Image makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
  }

  private static Image makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
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
