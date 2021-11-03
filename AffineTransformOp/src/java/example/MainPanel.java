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
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private Flip mode;

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

    String path = "example/test.jpg";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    BufferedImage img = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);

    JPanel p = new JPanel() {
      @Override protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        int w = img.getWidth(this);
        int h = img.getHeight(this);
        if (getMode() == Flip.VERTICAL) {
          AffineTransform at = AffineTransform.getScaleInstance(1d, -1d);
          at.translate(0, -h);
          Graphics2D g2 = (Graphics2D) g.create();
          g2.drawImage(img, at, this);
          g2.dispose();
        } else if (getMode() == Flip.HORIZONTAL) {
          AffineTransform at = AffineTransform.getScaleInstance(-1d, 1d);
          at.translate(-w, 0);
          AffineTransformOp atOp = new AffineTransformOp(at, null);
          g.drawImage(atOp.filter(img, null), 0, 0, w, h, this);
        } else { // if (getMode() == Flip.NONE) {
          g.drawImage(img, 0, 0, w, h, this);
        }
      }
    };

    add(p);
    add(box, BorderLayout.SOUTH);
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
  }

  public Flip getMode() {
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
    g2.fillRect(x, y, w, h);

    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(x + gap, y + gap, x + w - gap, y + h - gap);
    g2.drawLine(x + gap, y + h - gap, x + w - gap, y + gap);

    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 320;
  }

  @Override public int getIconHeight() {
    return 240;
  }
}
