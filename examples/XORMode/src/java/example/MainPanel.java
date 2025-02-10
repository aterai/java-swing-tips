// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
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

    add(new ImagePanel(img));
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

class ImagePanel extends JPanel {
  private transient RubberBandingListener rbl;
  private final transient BasicStroke stroke = new BasicStroke(2f);
  private final Path2D rubberBand = new Path2D.Double();
  private final transient Image image;

  protected ImagePanel(Image image) {
    super();
    this.image = image;
  }

  @Override public void updateUI() {
    removeMouseListener(rbl);
    removeMouseMotionListener(rbl);
    super.updateUI();
    rbl = new RubberBandingListener();
    addMouseMotionListener(rbl);
    addMouseListener(rbl);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    int iw = image.getWidth(this);
    int ih = image.getHeight(this);
    Dimension dim = getSize();
    int x = (dim.width - iw) / 2;
    int y = (dim.height - ih) / 2;
    g.drawImage(image, x, y, iw, ih, this);

    g2.setPaint(Color.RED);
    g2.fillOval(10, 10, 32, 32);

    g2.setPaint(Color.GREEN);
    g2.fillOval(50, 10, 32, 32);

    g2.setPaint(Color.BLUE);
    g2.fillOval(90, 10, 32, 32);

    g2.setPaint(Color.PINK);
    g2.fillOval(130, 10, 32, 32);

    g2.setPaint(Color.CYAN);
    g2.fillOval(170, 10, 32, 32);

    g2.setPaint(Color.ORANGE);
    g2.fillOval(210, 10, 32, 32);

    g2.setXORMode(Color.PINK);
    g2.fill(rubberBand);

    g2.setPaintMode();
    g2.setStroke(stroke);
    g2.setPaint(Color.WHITE);
    g2.draw(rubberBand);
    g2.dispose();
  }

  protected Path2D getRubberBand() {
    return rubberBand;
  }

  private final class RubberBandingListener extends MouseAdapter {
    private final Point srcPoint = new Point();

    @Override public void mouseDragged(MouseEvent e) {
      Point dstPoint = e.getPoint();
      Path2D rb = getRubberBand();
      rb.reset();
      rb.moveTo(srcPoint.x, srcPoint.y);
      rb.lineTo(dstPoint.x, srcPoint.y);
      rb.lineTo(dstPoint.x, dstPoint.y);
      rb.lineTo(srcPoint.x, dstPoint.y);
      rb.closePath();
      e.getComponent().repaint();
    }

    @Override public void mouseReleased(MouseEvent e) {
      e.getComponent().repaint();
    }

    @Override public void mousePressed(MouseEvent e) {
      getRubberBand().reset();
      srcPoint.setLocation(e.getPoint());
      e.getComponent().repaint();
    }
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
