// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import org.w3c.dom.Node;

public final class MainPanel extends JPanel {
  private static final int DELAY = 10;
  private static final Color ELLIPSE_COLOR = new Color(0x80_80_80);
  private static final double R = 20d;
  private static final double SX = 20d;
  private static final double SY = 20d;
  private final Dimension size = new Dimension((int) (R * 8 + SX * 2), (int) (R * 8 + SY * 2));
  private final List<Shape> list = new ArrayList<>(Arrays.asList(
      new Ellipse2D.Double(SX + 3 * R, SY + 0 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 5 * R, SY + 1 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 6 * R, SY + 3 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 5 * R, SY + 5 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 3 * R, SY + 6 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 1 * R, SY + 5 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 0 * R, SY + 3 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 1 * R, SY + 1 * R, 2 * R, 2 * R)));

  private MainPanel() {
    super(new BorderLayout());
    JLabel label = new JLabel();
    label.setOpaque(true);
    label.setBackground(Color.WHITE);
    label.setVerticalTextPosition(SwingConstants.TOP);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setHorizontalTextPosition(SwingConstants.CENTER);

    JButton button = new JButton("make");
    button.addActionListener(e -> {
      try {
        File file = createAnimatedGifFile(list, size);
        String path = file.getAbsolutePath();
        label.setText(path);
        label.setIcon(new ImageIcon(path));
      } catch (IOException ex) {
        ex.printStackTrace();
        label.setText(ex.getMessage());
        label.setIcon(null);
      }
    });

    add(label);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static File createAnimatedGifFile(List<Shape> list, Dimension size) throws IOException {
    Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("gif");
    ImageWriter writer = it.hasNext() ? it.next() : null;
    if (Objects.isNull(writer)) {
      throw new IOException();
    }
    File file = File.createTempFile("anime", ".gif");
    file.deleteOnExit();
    try (ImageOutputStream stream = ImageIO.createImageOutputStream(file)) {
      writer.setOutput(stream);
      writer.prepareWriteSequence(null);

      IIOMetadataNode gce = new IIOMetadataNode("GraphicControlExtension");
      gce.setAttribute("disposalMethod", "none");
      gce.setAttribute("userInputFlag", "FALSE");
      gce.setAttribute("transparentColorFlag", "FALSE");
      gce.setAttribute("transparentColorIndex", "0");
      gce.setAttribute("delayTime", Integer.toString(DELAY));

      IIOMetadataNode ae = new IIOMetadataNode("ApplicationExtension");
      ae.setAttribute("applicationID", "NETSCAPE");
      ae.setAttribute("authenticationCode", "2.0");
      // last two bytes is an unsigned short (little endian) that
      // indicates the number of times to loop.
      // 0 means loop forever.
      ae.setUserObject(new byte[]{0x1, 0x0, 0x0});

      IIOMetadataNode aes = new IIOMetadataNode("ApplicationExtensions");
      aes.appendChild(ae);

      // Create animated GIF using imageio | Oracle Community
      // https://community.oracle.com/thread/1264385
      BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
      ImageWriteParam iwp = writer.getDefaultWriteParam();
      IIOMetadata metadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(img), iwp);
      String metaFormat = metadata.getNativeMetadataFormatName();
      Node root = metadata.getAsTree(metaFormat);
      root.appendChild(gce);
      root.appendChild(aes);
      metadata.setFromTree(metaFormat, root);

      // make frame
      for (int i = 0; i < list.size() * DELAY; i++) {
        paintFrame(img, list);
        Collections.rotate(list, 1);
        IIOMetadata meta = i == 0 ? metadata : null;
        writer.writeToSequence(makeIioImage(img, meta), null);
      }
      writer.endWriteSequence();
    }
    return file;
  }

  private static void paintFrame(BufferedImage image, List<Shape> list) {
    Graphics2D g2 = image.createGraphics();
    g2.setPaint(Color.WHITE);
    g2.fillRect(0, 0, image.getWidth(), image.getHeight());
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(ELLIPSE_COLOR);
    float size = list.size();
    list.forEach(s -> {
      float alpha = (list.indexOf(s) + 1) / size;
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
      g2.fill(s);
    });
    g2.dispose();
  }

  private static IIOImage makeIioImage(BufferedImage image, IIOMetadata meta) {
    return new IIOImage(image, null, meta);
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
