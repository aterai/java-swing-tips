// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    String path = "example/wi0063-32.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Image image = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    Icon icon = new ImageIcon(image);
    ImageProducer ip = new FilteredImageSource(image.getSource(), new SelectedImageFilter());
    ImageIcon selectedIcon = new ImageIcon(createImage(ip));

    JToggleButton t1 = new JToggleButton(icon);
    t1.setSelectedIcon(selectedIcon);

    JToggleButton t2 = new JToggleButton(icon, true);
    t2.setSelectedIcon(selectedIcon);

    JRadioButton r1 = new JRadioButton("RadioButton1");

    JRadioButton r2 = new JRadioButton("RadioButton2");

    ButtonGroup bg = new ButtonGroup();
    JPanel p = new JPanel(new GridLayout(2, 2));
    p.setBorder(BorderFactory.createTitledBorder("ButtonGroup"));
    Stream.of(r1, r2, t1, t2).forEach(b -> {
      bg.add(b);
      p.add(b);
    });

    JButton clear = new JButton("clearSelection");
    clear.addActionListener(e -> bg.clearSelection());

    add(p, BorderLayout.NORTH);
    add(clear, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Image makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (32 - iw) / 2, (32 - ih) / 2);
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
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class SelectedImageFilter extends RGBImageFilter {
  // public SelectedImageFilter() {
  //   canFilterIndexColorModel = false;
  // }

  @Override public int filterRGB(int x, int y, int argb) {
    // Color color = new Color(argb, true);
    // float[] a = new float[4];
    // color.getComponents(a);
    // return new Color(a[0], a[1], a[2] * .5f, a[3]).getRGB();
    return argb & 0xFF_FF_FF_00 | (argb & 0xFF) >> 1;
  }
}
