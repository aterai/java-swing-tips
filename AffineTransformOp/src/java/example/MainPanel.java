// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MainPanel extends JPanel {
  protected Flip mode;
  protected final transient BufferedImage image;
  protected final ButtonGroup bg = new ButtonGroup();
  protected final JPanel panel = new JPanel() {
    @Override protected void paintComponent(Graphics g) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
      int w = image.getWidth(this);
      int h = image.getHeight(this);
      if (mode == Flip.VERTICAL) {
        AffineTransform at = AffineTransform.getScaleInstance(1d, -1d);
        at.translate(0, -h);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.drawImage(image, at, this);
        g2.dispose();
      } else if (mode == Flip.HORIZONTAL) {
        AffineTransform at = AffineTransform.getScaleInstance(-1d, 1d);
        at.translate(-w, 0);
        AffineTransformOp atOp = new AffineTransformOp(at, null);
        g.drawImage(atOp.filter(image, null), 0, 0, w, h, this);
      } else { // if (mode == Flip.NONE) {
        g.drawImage(image, 0, 0, w, h, this);
      }
    }
  };

  public MainPanel() {
    super(new BorderLayout());

    image = Optional.ofNullable(MainPanel.class.getResource("test.jpg"))
        .map(url -> {
          try {
            return ImageIO.read(url);
          } catch (IOException ex) {
            return makeMissingImage();
          }
        }).orElseGet(MainPanel::makeMissingImage);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(new JLabel("Flip: "));
    Stream.of(Flip.values()).map(this::makeRadioButton).forEach(rb -> {
      box.add(rb);
      bg.add(rb);
      box.add(Box.createHorizontalStrut(5));
    });
    add(panel);
    add(box, BorderLayout.SOUTH);
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
  }

  private JRadioButton makeRadioButton(Flip f) {
    JRadioButton rb = new JRadioButton(f.toString(), f == Flip.NONE);
    rb.addActionListener(e -> {
      mode = f;
      panel.repaint();
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

enum Flip { NONE, VERTICAL, HORIZONTAL }

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
    g2.drawLine(x + gap, y + gap, x + w - gap, y + gap);

    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 320;
  }

  @Override public int getIconHeight() {
    return 240;
  }
}
