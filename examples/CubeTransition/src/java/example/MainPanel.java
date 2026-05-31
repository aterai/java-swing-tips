// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new CubeTransitionPanel());
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
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

/**
 * Pseudo 3D cube transition panel. (Page transition by clicking or dragging)
 * - Click on the right half of the screen: go to the next page
 * - Click on the left half of the screen: go to the previous page
 * - Dragging with the mouse: manual interactive transition
 */
class CubeTransitionPanel extends JPanel {
  private static final int IMG_WIDTH = 240;
  private static final int IMG_HEIGHT = 160;
  private static final double PERSPECTIVE = 800d;
  private static final double CLICK_VELOCITY = 8d;
  private static final int DRAG_THRESHOLD = 5;
  private static final float MAX_SHADE_ALPHA = .5f;

  private final double[] screenArrX = new double[IMG_WIDTH + 1];
  private final double[] screenArrY = new double[IMG_WIDTH + 1]; // Top-left Y of each slice
  private final double[] drawArrH = new double[IMG_WIDTH + 1]; // Height of each slice

  private final List<BufferedImage> images = new ArrayList<>();
  private int currentIndex;
  private int nextIndex = 1;
  private double angle; // Current rotation angle in degrees: -90..90
  private double velocity;
  private boolean isDragging;
  private final Point pressedPt = new Point();
  private boolean movedWhilePressed;
  private int lastMouseX;

  // Offscreen Buffers
  private transient BufferedImage faceBufferA; // Side A (currentIndex or nextIndex)
  private transient BufferedImage faceBufferB; // Side B
  private transient BufferedImage finalBuffer; // Final compositing buffer

  private transient MouseAdapter handler;

  protected CubeTransitionPanel() {
    super();
    images.add(createSampleImage(Color.BLUE, "A"));
    images.add(createSampleImage(Color.RED, "B"));
    images.add(createSampleImage(Color.GREEN, "C"));
    images.add(createSampleImage(Color.ORANGE, "D"));

    // Animation timer targeting approximately 60 FPS (16ms interval)
    Timer timer = new Timer(16, e -> updateTransition());
    timer.start();
  }

  /**
   * Updates the transition state (angle, velocity, indexes) and schedules a repaint.
   */
  private void updateTransition() {
    if (!isDragging) {
      angle += velocity;

      // Decelerate and snap to target if velocity is low
      boolean isVelocitySmall = Math.abs(velocity) < .1;
      if (isVelocitySmall) {
        double target = Math.abs(angle) > 45 ? angle > 0 ? 90 : -90 : 0;
        angle += (target - angle) * .2;
      }

      boolean isOverPos = angle >= 90;
      boolean isOverNeg = angle <= -90;
      if (isOverPos) {
        currentIndex = nextIndex;
        angle = 0;
        velocity = 0;
        nextIndex = (currentIndex + 1) % images.size();
      } else if (isOverNeg) {
        currentIndex = nextIndex;
        angle = 0;
        velocity = 0;
        nextIndex = (currentIndex - 1 + images.size()) % images.size();
      }
    }
    repaint();
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    super.updateUI();
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    handler = new TransitionClickHandler();
    addMouseListener(handler);
    addMouseMotionListener(handler);
  }

  private BufferedImage createSampleImage(Color color, String text) {
    BufferedImage img = new BufferedImage(
        IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = img.createGraphics();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(color);
    g2.fillRect(0, 0, IMG_WIDTH, IMG_HEIGHT);
    g2.setColor(Color.WHITE);
    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60f));
    FontMetrics fm = g2.getFontMetrics();
    int x = (IMG_WIDTH - fm.stringWidth(text)) / 2;
    int y = IMG_HEIGHT / 2;
    g2.drawString(text, x, y);
    g2.dispose();
    return img;
  }

  /**
   * Supports buffer initialization and resizing based on panel dimensions.
   */
  private void ensureBuffers(int w, int h) {
    if (finalBuffer == null
        || finalBuffer.getWidth() != w
        || finalBuffer.getHeight() != h) {
      faceBufferA = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      faceBufferB = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      finalBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    int w = getWidth();
    int h = getHeight();
    ensureBuffers(w, h);

    final double angCurr = angle;
    final double angNext = angle > 0 ? angle - 90 : angle + 90;
    boolean firstHalf = Math.abs(angle) < 45;
    int cx = w / 2;
    int cy = h / 2;

    BufferedImage imgCurr = images.get(currentIndex);
    BufferedImage imgNext = images.get(nextIndex);

    clearBuffer(finalBuffer, Color.BLACK);
    clearBuffer(faceBufferA, null);

    // Z-sorting: Draw the back-facing side first, then the front-facing side
    if (firstHalf) {
      // Back: next side
      renderFaceToBuffer(faceBufferA, imgNext, angNext, cx, cy);
      // Front: current side
      clearBuffer(faceBufferB, null);
      renderFaceToBuffer(faceBufferB, imgCurr, angCurr, cx, cy);
    } else {
      // Back: current side
      renderFaceToBuffer(faceBufferA, imgCurr, angCurr, cx, cy);
      // Front: next side
      clearBuffer(faceBufferB, null);
      renderFaceToBuffer(faceBufferB, imgNext, angNext, cx, cy);
    }

    // Composite back buffer and front buffer into the final buffer
    compositeBuffer(finalBuffer, faceBufferA);
    compositeBuffer(finalBuffer, faceBufferB);

    // Transfer finalBuffer to screen
    g.drawImage(finalBuffer, 0, 0, null);
  }

  private void clearBuffer(BufferedImage buf, Color color) {
    Graphics2D g2 = buf.createGraphics();
    g2.setComposite(AlphaComposite.Clear);
    g2.fillRect(0, 0, buf.getWidth(), buf.getHeight());
    // If color is null, clear with complete transparency (ARGB=0)
    if (color != null) {
      g2.setComposite(AlphaComposite.SrcOver);
      g2.setColor(color);
      g2.fillRect(0, 0, buf.getWidth(), buf.getHeight());
    }
    g2.dispose();
  }

  private void compositeBuffer(BufferedImage dst, BufferedImage src) {
    Graphics2D g2 = dst.createGraphics();
    g2.setComposite(AlphaComposite.SrcOver);
    g2.drawImage(src, 0, 0, null);
    g2.dispose();
  }

  /**
   * Draws a single cube face into the offscreen buffer using perspective slicing.
   */
  @SuppressWarnings("ReturnCount")
  private void renderFaceToBuffer(
      BufferedImage buf, BufferedImage img, double offsetAngle, int cx, int cy) {
    // 1. Calculate perspective projections for all X positions
    calculateProjection(offsetAngle, cx, cy);

    // Find the horizontal boundaries for clipping
    double minScreenX = Double.MAX_VALUE;
    double maxScreenX = -Double.MAX_VALUE;
    for (double sx : screenArrX) {
      minScreenX = Math.min(minScreenX, sx);
      maxScreenX = Math.max(maxScreenX, sx);
    }

    int clipX = Math.max(0, (int) Math.floor(minScreenX));
    int clipW = Math.min(buf.getWidth(), (int) Math.ceil(maxScreenX)) - clipX;
    if (clipW <= 0) {
      return;
    }

    // 2. Render each vertical slice onto the buffer
    Graphics2D g2 = buf.createGraphics();
    g2.setClip(clipX, 0, clipW, buf.getHeight());

    double rad = Math.toRadians(offsetAngle);
    double sin = Math.sin(rad);
    double cos = Math.cos(rad);
    double radius = IMG_WIDTH / 2d;

    Rectangle sliceRect = new Rectangle();
    for (int x = 0; x < IMG_WIDTH; x++) {
      int sx = (int) Math.round(screenArrX[x]);
      int sxNext = (int) Math.round(screenArrX[x + 1]);
      int sliceW = sxNext - sx;
      if (sliceW <= 0) {
        continue; // Skip back-facing or reversed slices
      }

      int sy = (int) Math.round(screenArrY[x]);
      int drawH = (int) Math.round(drawArrH[x]);
      if (drawH <= 0) {
        continue;
      }
      // Draws a single vertical slice of the image texture.
      g2.drawImage(
          img, sx, sy, sx + sliceW, sy + drawH, x, 0, x + 1, IMG_HEIGHT, null);

      sliceRect.setBounds(sx, sy, sliceW, drawH);
      applyShadingSlice(g2, sliceRect, radius, sin, cos, x);
    }
    g2.dispose();
  }

  /**
   * Projects 3D coordinates onto a 2D viewport for each vertical line of the image.
   */
  private void calculateProjection(double offsetAngle, int cx, int cy) {
    double rad = Math.toRadians(offsetAngle);
    double cos = Math.cos(rad);
    double sin = Math.sin(rad);
    double radius = IMG_WIDTH / 2d;

    for (int x = 0; x <= IMG_WIDTH; x++) {
      double localX = x - radius;
      double localZ = -radius;

      // Rotate coordinates around Y-axis
      double rx = localX * cos - localZ * sin;
      double rz = localX * sin + localZ * cos;

      // Calculate perspective scale factor
      double scale = PERSPECTIVE / (PERSPECTIVE + rz);
      screenArrX[x] = cx + rx * scale;
      screenArrY[x] = cy - IMG_HEIGHT / 2d * scale;
      drawArrH[x] = IMG_HEIGHT * scale;
    }
  }

  /**
   * Applies depth shading to a specific vertical slice to enhance the 3D effect.
   */
  private void applyShadingSlice(
      Graphics2D g2, Rectangle rect, double radius, double sin, double cos, int x) {
    double localX = x - radius;
    double localZ = -radius;
    double rz = localX * sin + localZ * cos;

    // Normalize shade value into a 0.0 - 1.0 range based on depth (Z)
    double shade = (rz + radius) / (radius * 2d);
    // Java 21: shade = Math.clamp(shade, 0d, 1d);
    shade = Math.min(Math.max(shade, 0d), 1d);

    float alpha = (float) shade * MAX_SHADE_ALPHA;
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    g2.setColor(Color.BLACK);
    g2.fill(rect);
    g2.setComposite(AlphaComposite.SrcOver); // Restore default composite
  }

  /**
   * Mouse listener and motion listener to handle transitions via clicking and dragging.
   */
  private final class TransitionClickHandler extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
      isDragging = false;
      movedWhilePressed = false;
      pressedPt.setLocation(e.getPoint());
      lastMouseX = e.getX();
      velocity = 0;
    }

    @Override public void mouseDragged(MouseEvent e) {
      int dx = e.getX() - lastMouseX;
      lastMouseX = e.getX();
      int ax = Math.abs(e.getX() - pressedPt.x);
      int ay = Math.abs(e.getY() - pressedPt.y);
      int totalMove = ax + ay;

      if (totalMove > DRAG_THRESHOLD) {
        movedWhilePressed = true;
        isDragging = true;
      }

      if (isDragging) {
        angle += dx * .5;
        velocity = dx * .5;
        int size = images.size();
        // Java 21: angle = Math.clamp(angle, -90, 90);
        angle = Math.min(Math.max(angle, -90), 90);
        if (angle > 0) {
          nextIndex = (currentIndex + 1) % size;
        } else {
          nextIndex = (currentIndex - 1 + size) % size;
        }
      }
    }

    @Override public void mouseReleased(MouseEvent e) {
      isDragging = false;
    }

    @Override public void mouseClicked(MouseEvent e) {
      if (!movedWhilePressed) {
        boolean goNext = e.getX() >= getWidth() / 2;
        if (goNext) {
          nextIndex = (currentIndex + 1) % images.size();
          velocity = CLICK_VELOCITY;
        } else {
          nextIndex = (currentIndex - 1 + images.size()) % images.size();
          velocity = -CLICK_VELOCITY;
        }
      }
    }
  }
}
