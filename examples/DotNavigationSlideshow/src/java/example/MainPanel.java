// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int TOTAL_IMAGES = 5;
  private final List<ImageIcon> images = new ArrayList<>();
  private final List<JToggleButton> dots = new ArrayList<>();
  private final AnimationPanel animationPanel;
  private final JPanel dotPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 16));
  private int currentIndex;

  private MainPanel() {
    super(new BorderLayout());
    for (int i = 0; i < TOTAL_IMAGES; i++) {
      images.add(createDemoImageIcon(i));
    }

    JLayeredPane layeredPane = new JLayeredPane();
    layeredPane.setLayout(new OverlayLayout(layeredPane));

    animationPanel = new AnimationPanel(images.get(0)); // Java 21: getFirst()
    layeredPane.add(animationPanel, JLayeredPane.DEFAULT_LAYER);

    dotPanel.setOpaque(false);
    setupDots();

    JPanel dotWrapper = new JPanel(new BorderLayout());
    dotWrapper.setOpaque(false);
    dotWrapper.add(dotPanel, BorderLayout.SOUTH);

    JPanel overlay = new JPanel(new BorderLayout());
    overlay.setOpaque(false);
    overlay.add(dotWrapper);
    overlay.add(createArrowButton(true), BorderLayout.WEST);
    overlay.add(createArrowButton(false), BorderLayout.EAST);

    layeredPane.add(overlay, JLayeredPane.PALETTE_LAYER);

    NavigateHandler handler = new NavigateHandler();
    addKeyListener(handler);
    addMouseWheelListener(handler);
    setFocusable(true);
    add(layeredPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private JButton createArrowButton(boolean left) {
    JButton button = new HoverArrowButton(left);
    button.addActionListener(e -> {
      if (left) {
        navigatePrevious();
      } else {
        navigateNext();
      }
    });
    return button;
  }

  private void setupDots() {
    int totalImages = images.size();
    ButtonGroup group = new ButtonGroup();
    for (int i = 0; i < totalImages; i++) {
      JToggleButton dot = createDotButton(i);
      group.add(dot);
      dots.add(dot);
      dotPanel.add(dot);
    }
  }

  private JToggleButton createDotButton(int index) {
    JToggleButton dot = new JToggleButton(new DotIcon(), index == 0);
    dot.setBorderPainted(false);
    dot.setContentAreaFilled(false);
    dot.setFocusPainted(false);
    dot.setBorder(BorderFactory.createEmptyBorder());
    dot.setCursor(new Cursor(Cursor.HAND_CURSOR));
    dot.addActionListener(e -> navigateTo(index, index > currentIndex));
    return dot;
  }

  private void navigatePrevious() {
    int totalImages = images.size();
    navigateTo((currentIndex - 1 + totalImages) % totalImages, false);
  }

  private void navigateNext() {
    navigateTo((currentIndex + 1) % images.size(), true);
  }

  private void navigateTo(int index, boolean forward) {
    requestFocusInWindow();
    if (index != currentIndex && !animationPanel.isAnimating()) {
      currentIndex = index;
      animationPanel.startAnimation(images.get(index), forward);
    }
    // A clicked dot is already selected by the ButtonGroup, so the selection
    // must be restored when the navigation is ignored during the animation
    dots.get(currentIndex).setSelected(true);
  }

  private final class NavigateHandler extends KeyAdapter implements MouseWheelListener {
    @Override public void keyPressed(KeyEvent e) {
      int keyCode = e.getKeyCode();
      if (keyCode == KeyEvent.VK_LEFT) {
        navigatePrevious();
      } else if (keyCode == KeyEvent.VK_RIGHT) {
        navigateNext();
      }
    }

    @Override public void mouseWheelMoved(MouseWheelEvent e) {
      // A high-resolution wheel may report a rotation of 0
      int rotation = e.getWheelRotation();
      if (rotation < 0) {
        navigatePrevious();
      } else if (rotation > 0) {
        navigateNext();
      }
    }
  }

  private static ImageIcon createDemoImageIcon(int index) {
    int width = 800;
    int height = 600;
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = img.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Background
    g2.setColor(Color.getHSBColor((float) index / TOTAL_IMAGES, .5f, .8f));
    g2.fillRect(0, 0, width, height);

    // Text
    g2.setColor(Color.WHITE);
    g2.setFont(g2.getFont().deriveFont(80f));
    String text = "Slide " + (index + 1);
    FontMetrics fm = g2.getFontMetrics();
    int x = (width - fm.stringWidth(text)) / 2;
    int y = (height - fm.getHeight()) / 2 + fm.getAscent();
    g2.drawString(text, x, y);
    g2.dispose();
    return new ImageIcon(img);
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
    JFrame frame = new JFrame("DotNavigationSlideshow");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class AnimationPanel extends JPanel {
  private static final int DURATION = 250; // ms
  private static final int FRAME_RATE = 60;
  private final Timer timer;
  private Image currentImage;
  private Image nextImage;
  private double animationProgress; // 0.0 to 1.0
  private boolean slideForward = true;
  private long startTime;

  protected AnimationPanel(ImageIcon initialImage) {
    super();
    this.currentImage = initialImage.getImage();
    timer = new Timer(1000 / FRAME_RATE, e -> updateAnimation());
    timer.setInitialDelay(0);
  }

  public void startAnimation(ImageIcon nextImageIcon, boolean forward) {
    this.nextImage = nextImageIcon.getImage();
    this.slideForward = forward;
    this.animationProgress = 0d;
    this.startTime = System.currentTimeMillis();
    timer.start();
  }

  private void updateAnimation() {
    long elapsed = System.currentTimeMillis() - startTime;
    animationProgress = (double) elapsed / DURATION;
    if (elapsed >= DURATION) {
      completeAnimation();
    }
    repaint();
  }

  private void completeAnimation() {
    timer.stop();
    currentImage = nextImage; // Swap images
  }

  public boolean isAnimating() {
    return timer.isRunning();
  }

  private static double easeOut(double t) {
    return t * (2d - t);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    int width = getWidth();
    int height = getHeight();
    if (isAnimating()) {
      double easedProgress = easeOut(animationProgress);
      int offsetX = (int) (width * easedProgress);
      if (slideForward) {
        // Slide to left (new image comes from right)
        g2.drawImage(currentImage, -offsetX, 0, width, height, this);
        g2.drawImage(nextImage, width - offsetX, 0, width, height, this);
      } else {
        // Slide to right (new image comes from left)
        g2.drawImage(currentImage, offsetX, 0, width, height, this);
        g2.drawImage(nextImage, -width + offsetX, 0, width, height, this);
      }
    } else {
      // Static state
      g2.drawImage(currentImage, 0, 0, width, height, this);
    }
    g2.dispose();
  }
}

class DotIcon implements Icon {
  private static final Color SELECTED_COLOR = Color.WHITE;
  private static final Color DEFAULT_COLOR = new Color(0x64_FF_FF_FF, true);

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int gap;
    if (c instanceof AbstractButton && ((AbstractButton) c).getModel().isSelected()) {
      g2.setColor(SELECTED_COLOR);
      gap = 0;
    } else {
      g2.setColor(DEFAULT_COLOR);
      gap = 2;
    }
    g2.fillOval(x + gap, y + gap, getIconWidth() - gap * 2, getIconHeight() - gap * 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 18;
  }

  @Override public int getIconHeight() {
    return 18;
  }
}

class HoverArrowButton extends JButton {
  private final boolean left;

  protected HoverArrowButton(boolean left) {
    super();
    this.left = left;
  }

  @Override public void updateUI() {
    super.updateUI();
    setContentAreaFilled(false);
    setFocusPainted(false);
    setOpaque(false);
    setRolloverEnabled(true);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.width = 50;
    return d;
  }

  @Override protected void paintComponent(Graphics g) {
    if (getModel().isRollover()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Color.WHITE);
      g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
      g2.draw(createArrowPath(rect, left));
      g2.dispose();
    }
  }

  private static Path2D createArrowPath(Rectangle rect, boolean leftArrow) {
    double arrowHeight = Math.min(25d, rect.height / 2d);
    double arrowWidth = arrowHeight / 2d;
    double centerX = rect.getCenterX();
    double centerY = rect.getCenterY();
    double startX = centerX - (arrowWidth / 2d);

    Path2D path = new Path2D.Double();
    if (leftArrow) { // <
      path.moveTo(startX + arrowWidth, centerY - arrowHeight / 2);
      path.lineTo(startX, centerY);
      path.lineTo(startX + arrowWidth, centerY + arrowHeight / 2);
    } else { // >
      path.moveTo(startX, centerY - arrowHeight / 2);
      path.lineTo(startX + arrowWidth, centerY);
      path.lineTo(startX, centerY + arrowHeight / 2);
    }
    return path;
  }
}
