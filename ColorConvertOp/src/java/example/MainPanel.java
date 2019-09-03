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
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1));
    ImageIcon orgImage = new ImageIcon(getClass().getResource("i03-10.gif"));

    JPanel p1 = new JPanel(new GridLayout(1, 2));
    p1.add(makeLabel(makeGrayImageIcon1(orgImage.getImage()), orgImage, "ColorConvertOp"));
    p1.add(makeLabel(makeGrayImageIcon2(orgImage.getImage()), orgImage, "TYPE_BYTE_GRAY"));
    add(p1);
    add(makeLabel(makeGrayImageIcon3(orgImage.getImage()), orgImage, "GrayFilter.createDisabledImage"));
    JPanel p3 = new JPanel(new GridLayout(1, 2));
    p3.add(makeLabel(makeGrayImageIcon4(orgImage.getImage()), orgImage, "GrayFilter(true, 50)"));
    p3.add(makeLabel(makeGrayImageIcon5(orgImage.getImage()), orgImage, "GrayImageFilter"));
    add(p3);

    p1.setBackground(Color.WHITE);
    p3.setBackground(Color.WHITE);
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JLabel makeLabel(ImageIcon image, ImageIcon orgImage, String str) {
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

  private static ImageIcon makeGrayImageIcon1(Image img) {
    int w = img.getWidth(null);
    int h = img.getHeight(null);
    BufferedImage source = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics g = source.createGraphics();
    g.drawImage(img, 0, 0, null);
    g.dispose();
    ColorConvertOp ccOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    return new ImageIcon(ccOp.filter(source, null));
  }

  private static ImageIcon makeGrayImageIcon2(Image img) {
    int w = img.getWidth(null);
    int h = img.getHeight(null);
    BufferedImage destination = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
    Graphics g = destination.createGraphics();
    // // g.setColor(Color.WHITE);
    // https://community.oracle.com/thread/1373262 Color to Grayscale to Binary
    // g.fillRect(0, 0, w, h); // need to pre-fill(alpha?)
    g.drawImage(img, 0, 0, null);
    g.dispose();
    return new ImageIcon(destination);
  }

  private static ImageIcon makeGrayImageIcon3(Image img) {
    // GrayFilter1
    return new ImageIcon(GrayFilter.createDisabledImage(img));
  }

  private static ImageIcon makeGrayImageIcon4(Image img) {
    // GrayFilter2
    ImageProducer ip = new FilteredImageSource(img.getSource(), new GrayFilter(true, 50));
    return new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
  }

  private static ImageIcon makeGrayImageIcon5(Image img) {
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

class GrayImageFilter extends RGBImageFilter {
  // public GrayImageFilter() {
  //   canFilterIndexColorModel = false;
  // }

  @Override public int filterRGB(int x, int y, int argb) {
    // int a = (argb >> 24) & 0xFF;
    int r = (argb >> 16) & 0xFF;
    int g = (argb >> 8) & 0xFF;
    int b = argb & 0xFF;
    int m = (2 * r + 4 * g + b) / 7; // NTSC Coefficients
    // return new Color(m, m, m, a).getRGB();
    return (argb & 0xFF_00_00_00) | (m << 16) | (m << 8) | m;
  }
}
