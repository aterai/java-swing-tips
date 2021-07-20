// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String path = "example/test.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Image img = Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    ZoomImage zoom = new ZoomImage(img);

    JButton button1 = new JButton("Zoom In");
    button1.addActionListener(e -> zoom.changeScale(-5d));

    JButton button2 = new JButton("Zoom Out");
    button2.addActionListener(e -> zoom.changeScale(5d));

    JButton button3 = new JButton("Original size");
    button3.addActionListener(e -> zoom.initScale());

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(button1);
    box.add(button2);
    box.add(button3);

    add(zoom);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Image makeMissingImage() {
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

class ZoomImage extends JPanel {
  private transient MouseWheelListener handler;
  private final transient Image image;
  private final int iw;
  private final int ih;
  private double scale = 1d;

  protected ZoomImage(Image image) {
    super();
    this.image = image;
    iw = image.getWidth(this);
    ih = image.getHeight(this);
  }

  @Override public void updateUI() {
    removeMouseWheelListener(handler);
    super.updateUI();
    // handler = new MouseWheelListener() {
    //   @Override public void mouseWheelMoved(MouseWheelEvent e) {
    //     changeScale(e.getWheelRotation());
    //   }
    // };
    handler = e -> changeScale(e.getPreciseWheelRotation());
    addMouseWheelListener(handler);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.scale(scale, scale);
    g2.drawImage(image, 0, 0, iw, ih, this);
    g2.dispose();
  }

  public void initScale() {
    scale = 1d;
    repaint();
  }

  public void changeScale(double dv) {
    scale = Math.max(.05, Math.min(5d, scale - dv * .05));
    repaint();
    // double v = scale - dv * .1;
    // if (v - 1d > -1.0e-2) {
    //   scale = Math.min(10d, v);
    // } else {
    //   scale = Math.max(.01, scale - dv * .01);
    // }
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
