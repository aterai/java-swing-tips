// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.FilteredImageSource;
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
    super(new GridLayout(0, 1));
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    String path = "example/i03-10.gif";
    Image image = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    Icon icon = new ImageIcon(image);

    JPanel p1 = new JPanel(new GridLayout(1, 2));
    p1.add(makeLabel(makeGrayIcon1(image), icon, "ColorConvertOp"));
    p1.add(makeLabel(makeGrayIcon2(image), icon, "TYPE_BYTE_GRAY"));
    add(p1);
    add(makeLabel(makeGrayIcon3(image), icon, "GrayFilter.createDisabledImage"));
    JPanel p3 = new JPanel(new GridLayout(1, 2));
    p3.add(makeLabel(makeGrayIcon4(image), icon, "GrayFilter(true, 50)"));
    p3.add(makeLabel(makeGrayIcon5(image), icon, "GrayImageFilter"));
    add(p3);

    p1.setBackground(Color.WHITE);
    p3.setBackground(Color.WHITE);
    setBackground(Color.WHITE);
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

  private static JLabel makeLabel(Icon image, Icon orgImage, String str) {
    JLabel label = new JLabel(str, image, SwingConstants.LEFT);
    label.addMouseListener(new MouseAdapter() {
      private boolean isGray;
      @Override public void mouseClicked(MouseEvent e) {
        JLabel l = (JLabel) e.getComponent();
        l.setIcon(isGray ? image : orgImage);
        isGray ^= true;
      }
    });
    return label;
  }

  private static Icon makeGrayIcon1(Image img) {
    int w = img.getWidth(null);
    int h = img.getHeight(null);
    BufferedImage source = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics g = source.createGraphics();
    g.drawImage(img, 0, 0, null);
    g.dispose();
    ColorConvertOp ccOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    return new ImageIcon(ccOp.filter(source, null));
  }

  private static Icon makeGrayIcon2(Image img) {
    int w = img.getWidth(null);
    int h = img.getHeight(null);
    BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
    Graphics g = dst.createGraphics();
    // // g.setColor(Color.WHITE);
    // https://community.oracle.com/thread/1373262 Color to Gray scale to Binary
    // g.fillRect(0, 0, w, h); // need to pre-fill(alpha?)
    g.drawImage(img, 0, 0, null);
    g.dispose();
    return new ImageIcon(dst);
  }

  private static Icon makeGrayIcon3(Image img) {
    // GrayFilter1
    return new ImageIcon(GrayFilter.createDisabledImage(img));
  }

  private static Icon makeGrayIcon4(Image img) {
    // GrayFilter2
    ImageProducer ip = new FilteredImageSource(img.getSource(), new GrayFilter(true, 50));
    return new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
  }

  private static Icon makeGrayIcon5(Image img) {
    // RGBImageFilter
    ImageProducer ip = new FilteredImageSource(img.getSource(), new GrayImageFilter());
    return new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
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

class GrayImageFilter extends RGBImageFilter {
  // public GrayImageFilter() {
  //   canFilterIndexColorModel = false;
  // }

  @Override public int filterRGB(int x, int y, int argb) {
    // int a = (argb >> 24) & 0xFF;
    int r = (argb >> 16) & 0xFF;
    int g = (argb >> 8) & 0xFF;
    int b = argb & 0xFF;
    // NTSC Coefficients
    int m = (2 * r + 4 * g + b) / 7;
    // return new Color(m, m, m, a).getRGB();
    return argb & 0xFF_00_00_00 | m << 16 | m << 8 | m;
  }
}
