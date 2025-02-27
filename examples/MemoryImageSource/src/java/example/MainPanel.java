// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new PaintPanel());
    setPreferredSize(new Dimension(320, 240));
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
    frame.setResizable(false);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class PaintPanel extends JPanel {
  private static final Paint TEXTURE = createCheckerTexture(6, new Color(0x32_C8_96_64, true));
  private final Point startPoint = new Point();
  private final transient BufferedImage backImage;
  private final Rectangle rect = new Rectangle(320, 240);
  private final int[] pixels;
  private final transient ImageProducer src;
  private int penColor;
  private transient MouseAdapter handler;

  protected PaintPanel() {
    super();
    pixels = new int[rect.width * rect.height];
    src = new MemoryImageSource(rect.width, rect.height, pixels, 0, rect.width);
    backImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = backImage.createGraphics();
    g2.setPaint(TEXTURE);
    g2.fill(rect);
    g2.dispose();
  }

  @Override public void updateUI() {
    removeMouseMotionListener(handler);
    removeMouseListener(handler);
    super.updateUI();
    handler = new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        startPoint.setLocation(e.getPoint());
        penColor = SwingUtilities.isLeftMouseButton(e) ? 0xFF_00_00_00 : 0x0;
      }

      @Override public void mouseDragged(MouseEvent e) {
        double dx = e.getX() - startPoint.getX();
        double dy = e.getY() - startPoint.getY();
        double delta = Math.max(Math.abs(dx), Math.abs(dy));

        double ix = dx / delta;
        double iy = dy / delta;
        double sx = startPoint.x;
        double sy = startPoint.y;
        Point2D pt = new Point2D.Double();
        for (int i = 0; i < delta; i++) {
          pt.setLocation(sx, sy);
          if (!rect.contains(pt)) {
            break;
          }
          paintStamp(pt, penColor);
          // src.newPixels(pt.x - 2, pt.y - 2, 4, 4);
          sx += ix;
          sy += iy;
        }
        startPoint.setLocation(e.getPoint());
      }
    };
    addMouseMotionListener(handler);
    addMouseListener(handler);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.drawImage(backImage, 0, 0, this);
    g2.drawImage(createImage(src), 0, 0, this);
    g2.dispose();
  }

  private void paintStamp(Point2D pt, int color) {
    int px = (int) pt.getX();
    int py = (int) pt.getY();
    // 1 x 1:
    // pixels[px + py * 320] = color;
    // 3 x 3 square:
    for (int n = -1; n <= 1; n++) {
      for (int m = -1; m <= 1; m++) {
        int t = px + n + (py + m) * rect.width;
        if (t >= 0 && t < rect.width * rect.height) {
          pixels[t] = color;
        }
      }
    }
    repaint(px - 2, py - 2, 4, 4);
  }

  public static TexturePaint createCheckerTexture(int cs, Color color) {
    int size = cs * cs;
    BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setPaint(color);
    g2.fillRect(0, 0, size, size);
    for (int i = 0; i * cs < size; i++) {
      for (int j = 0; j * cs < size; j++) {
        if ((i + j) % 2 == 0) {
          g2.fillRect(i * cs, j * cs, cs, cs);
        }
      }
    }
    g2.dispose();
    return new TexturePaint(img, new Rectangle(size, size));
  }
}

// class PaintPanel2 extends JPanel implements MouseMotionListener, MouseListener {
//   private static final Color ERASER = new Color(0x0, true);
//   private boolean isPen = true;
//   private final Point startPoint = new Point();
//   private final transient BufferedImage currentImage;
//   private final transient BufferedImage backImage;
//
//   protected PaintPanel2() {
//     super();
//     addMouseMotionListener(this);
//     addMouseListener(this);
//     currentImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
//     backImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
//     Graphics2D g2 = backImage.createGraphics();
//     g2.setPaint(makeTexturePaint());
//     g2.fillRect(0, 0, 320, 240);
//     g2.dispose();
//   }
//
//   private static BufferedImage makeBGImage() {
//     Color color = new Color(0x32_C8_96_64, true);
//     int cs = 6;
//     int sz = cs * cs;
//     BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
//     Graphics2D g2 = img.createGraphics();
//     g2.setPaint(color);
//     g2.fillRect(0, 0, sz, sz);
//     for (int i = 0; i * cs < sz; i++) {
//       for (int j = 0; j * cs < sz; j++) {
//         if ((i + j) % 2 == 0) {
//           g2.fillRect(i * cs, j * cs, cs, cs);
//         }
//       }
//     }
//     g2.dispose();
//     return img;
//   }
//
//   private static TexturePaint makeTexturePaint() {
//     BufferedImage img = makeBGImage();
//     return new TexturePaint(img, new Rectangle(img.getWidth(), img.getHeight()));
//   }
//
//   @Override protected void paintComponent(Graphics g) {
//     super.paintComponent(g);
//     // if (Objects.nonNull(backImage)) {
//     g.drawImage(backImage, 0, 0, this);
//     // if (Objects.nonNull(currentImage)) {
//     g.drawImage(currentImage, 0, 0, this);
//   }
//
//   @Override public void mouseDragged(MouseEvent e) {
//     Point pt = e.getPoint();
//     Graphics2D g2 = currentImage.createGraphics();
//     g2.setStroke(new BasicStroke(3f));
//     if (isPen) {
//       g2.setPaint(Color.BLACK);
//     } else {
//       g2.setComposite(AlphaComposite.Clear);
//       g2.setPaint(ERASER);
//     }
//     g2.drawLine(startPoint.x, startPoint.y, pt.x, pt.y);
//     g2.dispose();
//     startPoint.setLocation(pt);
//     repaint();
//   }
//
//   @Override public void mousePressed(MouseEvent e) {
//     startPoint.setLocation(e.getPoint());
//     isPen = e.getButton() == MouseEvent.BUTTON1;
//   }
//
//   @Override public void mouseMoved(MouseEvent e) {
//     /* not needed */
//   }
//
//   @Override public void mouseExited(MouseEvent e) {
//     /* not needed */
//   }
//
//   @Override public void mouseEntered(MouseEvent e) {
//     /* not needed */
//   }
//
//   @Override public void mouseReleased(MouseEvent e) {
//     /* not needed */
//   }
//
//   @Override public void mouseClicked(MouseEvent e) {
//     /* not needed */
//   }
// }
