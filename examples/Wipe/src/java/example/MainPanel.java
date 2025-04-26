// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Timer animator = new Timer(5, null);
    Image image = ImageUtils.makeImage("example/test.png");
    WipePanel wipe = new WipePanel(image, animator);
    wipe.setBackground(Color.BLACK);
    animator.addActionListener(e -> wipe.repaint());

    JButton button1 = new JButton("Wipe In");
    button1.addActionListener(e -> {
      wipe.setWipeMode(Wipe.IN);
      animator.start();
    });

    JButton button2 = new JButton("Wipe Out");
    button2.addActionListener(e -> {
      wipe.setWipeMode(Wipe.OUT);
      animator.start();
    });

    add(wipe);
    add(button1, BorderLayout.SOUTH);
    add(button2, BorderLayout.NORTH);
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
    animator.start();
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

enum Wipe {
  IN, OUT
}

class WipePanel extends JComponent {
  private final Image image;
  private final Timer animator;
  private Wipe mode = Wipe.IN;
  private int ww;

  protected WipePanel(Image image, Timer animator) {
    super();
    this.image = image;
    this.animator = animator;
  }

  @Override protected void paintComponent(Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    int iw = image.getWidth(this);
    int ih = image.getHeight(this);
    if (getWipeMode() == Wipe.IN) {
      if (ww < iw) {
        ww += 10;
      } else {
        animator.stop();
      }
    } else { // Wipe.OUT:
      if (ww > 0) {
        ww -= 10;
      } else {
        animator.stop();
      }
    }
    g.drawImage(image, 0, 0, iw, ih, this);
    g.fillRect(ww, 0, iw, ih);
  }

  public void setWipeMode(Wipe wipeMode) {
    this.mode = wipeMode;
  }

  public Wipe getWipeMode() {
    return mode;
  }
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
    g2.drawLine(gap, gap, w - gap, h - gap);
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

final class ImageUtils {
  private ImageUtils() {
    /* Singleton */
  }

  public static Image makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(ImageUtils::makeMissingImage);
  }

  public static Image makeMissingImage() {
    Icon missingIcon = new MissingIcon();
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
  }
}
