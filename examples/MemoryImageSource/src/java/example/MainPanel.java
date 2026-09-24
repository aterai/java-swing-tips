// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.logging.Logger;
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
      Logger.getGlobal().severe(ex::getMessage);
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
  private static final int PEN_COLOR = 0xFF_00_00_00;
  private static final int ERASER_COLOR = 0x0;
  private final transient BufferedImage backImage;
  private final Rectangle imageRect = new Rectangle(320, 240);
  private final int[] pixels;
  private final transient MemoryImageSource source;
  private final transient Image image;
  private int penColor;
  private transient MouseAdapter handler;

  protected PaintPanel() {
    super();
    pixels = new int[imageRect.width * imageRect.height];
    source = new MemoryImageSource(imageRect.width, imageRect.height, pixels, 0, imageRect.width);
    // Reuse a single Image and send only the changed area with newPixels(...)
    source.setAnimated(true);
    image = Toolkit.getDefaultToolkit().createImage(source);
    backImage = new BufferedImage(imageRect.width, imageRect.height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = backImage.createGraphics();
    g2.setPaint(TEXTURE);
    g2.fill(imageRect);
    g2.dispose();
  }

  @Override public void updateUI() {
    removeMouseMotionListener(handler);
    removeMouseListener(handler);
    super.updateUI();
    handler = new MouseHandler();
    addMouseMotionListener(handler);
    addMouseListener(handler);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.drawImage(backImage, 0, 0, this);
    g2.drawImage(image, 0, 0, this);
    g2.dispose();
  }

  // Draws a line of 3 x 3 stamps from p0 to p1 into the pixel array
  private void drawLine(Point p0, Point p1) {
    int dx = p1.x - p0.x;
    int dy = p1.y - p0.y;
    int steps = Math.max(Math.abs(dx), Math.abs(dy));
    Rectangle dirty = null;
    for (int i = 0; i <= steps; i++) {
      int px = steps == 0 ? p0.x : p0.x + Math.round(dx * i / (float) steps);
      int py = steps == 0 ? p0.y : p0.y + Math.round(dy * i / (float) steps);
      Rectangle r = paintStamp(px, py);
      if (!r.isEmpty()) {
        dirty = dirty == null ? r : dirty.union(r);
      }
    }
    if (dirty != null) {
      source.newPixels(dirty.x, dirty.y, dirty.width, dirty.height);
      repaint(dirty);
    }
  }

  // Fills a 3 x 3 square centered on (px, py), clipped to the image bounds
  private Rectangle paintStamp(int px, int py) {
    Rectangle r = new Rectangle(px - 1, py - 1, 3, 3).intersection(imageRect);
    for (int y = r.y; y < r.y + r.height; y++) {
      int offset = y * imageRect.width;
      Arrays.fill(pixels, offset + r.x, offset + r.x + r.width, penColor);
    }
    return r;
  }

  public static TexturePaint createCheckerTexture(int cellSize, Color color) {
    int size = cellSize * cellSize;
    BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setPaint(color);
    g2.fillRect(0, 0, size, size);
    for (int i = 0; i * cellSize < size; i++) {
      for (int j = 0; j * cellSize < size; j++) {
        if ((i + j) % 2 == 0) {
          g2.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
        }
      }
    }
    g2.dispose();
    return new TexturePaint(img, new Rectangle(size, size));
  }

  private final class MouseHandler extends MouseAdapter {
    private final Point startPoint = new Point();

    @Override public void mousePressed(MouseEvent e) {
      startPoint.setLocation(e.getPoint());
      penColor = SwingUtilities.isLeftMouseButton(e) ? PEN_COLOR : ERASER_COLOR;
      drawLine(startPoint, startPoint);
    }

    @Override public void mouseDragged(MouseEvent e) {
      Point pt = e.getPoint();
      drawLine(startPoint, pt);
      startPoint.setLocation(pt);
    }
  }
}
