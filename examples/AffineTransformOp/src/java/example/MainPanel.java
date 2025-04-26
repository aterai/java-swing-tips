// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private Flip mode = Flip.NONE;

  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(new JLabel("Flip: "));
    ButtonGroup bg = new ButtonGroup();
    Stream.of(Flip.values()).map(this::makeRadioButton).forEach(rb -> {
      box.add(rb);
      bg.add(rb);
      box.add(Box.createHorizontalStrut(5));
    });
    box.add(Box.createHorizontalGlue());
    String path = "example/test.jpg";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    BufferedImage img = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    add(new ImageFlipPanel(img));
    add(box, BorderLayout.SOUTH);
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
  }

  private Flip getMode() {
    return mode;
  }

  private JRadioButton makeRadioButton(Flip f) {
    JRadioButton rb = new JRadioButton(f.toString(), f == Flip.NONE);
    rb.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        mode = f;
        rb.getRootPane().repaint();
      }
    });
    return rb;
  }

  private static BufferedImage makeMissingImage() {
    Icon missingIcon = new MissingIcon();
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
  }

  private final class ImageFlipPanel extends JPanel {
    private final transient BufferedImage image;

    private ImageFlipPanel(BufferedImage image) {
      super();
      this.image = image;
    }

    @Override protected void paintComponent(Graphics g) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
      int w = image.getWidth();
      int h = image.getHeight();
      switch (getMode()) {
        case VERTICAL:
          AffineTransform at1 = AffineTransform.getScaleInstance(1d, -1d);
          at1.translate(0, -h);
          // AffineTransform at1 = new AffineTransform(1d, 0d, 0d, -1d, 0d, h);
          Graphics2D g2 = (Graphics2D) g.create();
          g2.drawImage(image, at1, this);
          g2.dispose();
          break;

        case HORIZONTAL:
          AffineTransform at2 = AffineTransform.getScaleInstance(-1d, 1d);
          at2.translate(-w, 0);
          // AffineTransform at2 = new AffineTransform(-1d, 0d, 0d, 1d, w, 0d);
          AffineTransformOp atOp = new AffineTransformOp(at2, null);
          g.drawImage(atOp.filter(image, null), 0, 0, w, h, this);
          break;

        default:
          g.drawImage(image, 0, 0, w, h, this);
      }
    }
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

enum Flip {
  NONE, VERTICAL, HORIZONTAL
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;
    g2.setColor(Color.WHITE);
    g2.translate(x, y);
    g2.fillRect(0, 0, w, h);
    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(gap, gap, gap, h - gap);
    g2.drawLine(gap, h - gap, w - gap, gap);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 320;
  }

  @Override public int getIconHeight() {
    return 240;
  }
}
