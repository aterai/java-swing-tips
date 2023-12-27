// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/duke.running.gif");
    Icon icon = Optional.ofNullable(url).<Icon>map(ImageIcon::new)
        .orElseGet(() -> UIManager.getIcon("html.missingImage"));
    JLabel label = new JLabel(icon);
    label.setBorder(BorderFactory.createTitledBorder("duke.running.gif"));

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createTitledBorder("Extract frames from Animated GIF"));

    // [JDK-8080225]
    // FileInput/OutputStream/FileChannel cleanup should be improved - Java Bug System
    // https://bugs.openjdk.org/browse/JDK-8080225
    // try (InputStream is = getClass().getResourceAsStream("duke.running.gif");
    //      ImageInputStream iis = ImageIO.createImageInputStream(is)) {
    // URL url = getClass().getResource("duke.running.gif");
    // try (InputStream is = Files.newInputStream(Paths.get(url.toURI()));
    //      ImageInputStream iis = ImageIO.createImageInputStream(is)) {
    if (url != null) {
      try (ImageInputStream iis = ImageIO.createImageInputStream(url.openStream())) {
        loadFromStream(iis).stream().map(ImageIcon::new).map(JLabel::new).forEach(box::add);
      } catch (IOException ex) {
        ex.printStackTrace();
        label.setText(ex.getMessage());
      }
    }
    add(label, BorderLayout.WEST);
    add(new JScrollPane(box));
    setPreferredSize(new Dimension(320, 240));
  }
  // // https://community.oracle.com/thread/1271862 Reading gif animation frame rates and such?
  // private List<BufferedImage> loadFromStream(ImageInputStream imageStream) throws IOException {
  //   String format = "gif";
  //   String meta = "javax_imageio_gif_image_1.0";
  //   ImageReader reader = null;
  //   Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
  //   while (readers.hasNext()) {
  //     reader = readers.next();
  //     String metaFormat = reader.getOriginatingProvider().getNativeImageMetadataFormatName();
  //     if (format.equalsIgnoreCase(reader.getFormatName()) && meta.equals(metaFormat)) {
  //       break;
  //     }
  //   }
  //   ImageReader imageReader = Objects.requireNonNull(reader, "Can not read image format!");
  //   boolean isGif = format.equalsIgnoreCase(imageReader.getFormatName());
  //   imageReader.setInput(imageStream, false, !isGif);
  //   List<BufferedImage> list = new ArrayList<>();
  //   for (int i = 0; i < imageReader.getNumImages(true); i++) {
  //     IIOImage frame = imageReader.readAll(i, null);
  //     list.add((BufferedImage) frame.getRenderedImage());
  //   }
  //   imageReader.dispose();
  //   return list;
  // }

  private static List<Image> loadFromStream(ImageInputStream stream) throws IOException {
    Iterable<ImageReader> iterable = () -> ImageIO.getImageReaders(stream);
    ImageReader reader = StreamSupport.stream(iterable.spliterator(), false)
        .filter(MainPanel::checkGifFormat)
        .findFirst()
        .orElseThrow(() -> new IOException("Can not read image format!"));
    reader.setInput(stream, false, false);
    List<Image> list = new ArrayList<>();
    for (int i = 0; i < reader.getNumImages(true); i++) {
      RenderedImage img = reader.readAll(i, null).getRenderedImage();
      if (img instanceof Image) {
        list.add((Image) img);
      }
    }
    reader.dispose();
    return list;
  }

  private static boolean checkGifFormat(ImageReader reader) {
    String metaFormat = reader.getOriginatingProvider().getNativeImageMetadataFormatName();
    String name;
    try {
      name = reader.getFormatName();
    } catch (IOException ex) {
      name = "";
    }
    return "gif".equalsIgnoreCase(name) && "javax_imageio_gif_image_1.0".equals(metaFormat);
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
