// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int TOTAL_IMAGES = 5;
  private final List<ImageIcon> images = new ArrayList<>();
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
    overlay.add(makeArrowButton(true), BorderLayout.WEST);
    overlay.add(makeArrowButton(false), BorderLayout.EAST);

    layeredPane.add(overlay, JLayeredPane.PALETTE_LAYER);

    NavigateHandler handler = new NavigateHandler();
    addKeyListener(handler);
    addMouseWheelListener(handler);
    setFocusable(true);
    add(layeredPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private JButton makeArrowButton(boolean moveRight) {
    JButton button = new HoverArrowButton(moveRight);
    button.addActionListener(e -> {
      int totalImages = images.size();
      if (moveRight) {
        navigateTo((currentIndex - 1 + totalImages) % totalImages, false);
      } else {
        navigateTo((currentIndex + 1) % totalImages, true);
      }
    });
    return button;
  }

  private void setupDots() {
    int totalImages = images.size();
    ButtonGroup group = new ButtonGroup();
    for (int i = 0; i < totalImages; i++) {
      JToggleButton dot = makeDotButton(i);
      group.add(dot);
      dotPanel.add(dot);
    }
  }

  private JToggleButton makeDotButton(int index) {
    JToggleButton dot = new JToggleButton(new DotIcon(), index == 0);
    dot.setBorderPainted(false);
    dot.setContentAreaFilled(false);
    dot.setFocusPainted(false);
    dot.setBorder(BorderFactory.createEmptyBorder());
    dot.setCursor(new Cursor(Cursor.HAND_CURSOR));
    dot.addActionListener(e -> {
      if (index != currentIndex) {
        navigateTo(index, index > currentIndex);
      }
    });
    // dot.addKeyListener(new NavigateHandler());
    return dot;
  }

  private void navigateTo(int index, boolean moveRight) {
    Component c = dotPanel.getComponent(index);
    if (c instanceof JToggleButton && !animationPanel.isAnimating()) {
      ((JToggleButton) c).setSelected(true);
      ImageIcon nextImage = images.get(index);
      currentIndex = index;
      animationPanel.startAnimation(nextImage, moveRight);
      requestFocusInWindow();
    }
  }

  private final class NavigateHandler extends KeyAdapter implements MouseWheelListener {
    @Override public void keyPressed(KeyEvent e) {
      int totalImages = images.size();
      int keyCode = e.getKeyCode();
      if (keyCode == KeyEvent.VK_LEFT) {
        navigateTo((currentIndex - 1 + totalImages) % totalImages, false);
      } else if (keyCode == KeyEvent.VK_RIGHT) {
        navigateTo((currentIndex + 1) % totalImages, true);
      }
    }

    @Override public void mouseWheelMoved(MouseWheelEvent e) {
      int totalImages = images.size();
      int rotation = e.getWheelRotation();
      if (rotation < 0) {
        navigateTo((currentIndex - 1 + totalImages) % totalImages, false);
      } else {
        navigateTo((currentIndex + 1) % totalImages, true);
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
  private boolean moveRight = true;

  protected AnimationPanel(ImageIcon initialImage) {
    super();
    this.currentImage = initialImage.getImage();
    int delay = 1000 / FRAME_RATE;
    timer = new Timer(delay, new ActionListener() {
      private long startTime;

      @Override public void actionPerformed(ActionEvent e) {
        boolean b0 = animationProgress == 0d;
        if (b0) {
          startTime = System.currentTimeMillis();
          animationProgress = .001; // Start
        } else {
          long elapsed = System.currentTimeMillis() - startTime;
          animationProgress = (double) elapsed / DURATION;
          boolean b1 = animationProgress >= 1d;
          if (b1) {
            animationProgress = 1d;
            completeAnimation();
          }
        }
        repaint();
      }
    });
    timer.setInitialDelay(0);
  }

  public void startAnimation(ImageIcon nextImageIcon, boolean isMoveRight) {
    this.nextImage = nextImageIcon.getImage();
    this.moveRight = isMoveRight;
    this.animationProgress = 0d;
    timer.start();
  }

  private void completeAnimation() {
    timer.stop();
    currentImage = nextImage; // Swap images
    // nextImage = null;
    animationProgress = 0d;
  }

  public boolean isAnimating() {
    return timer.isRunning();
  }

  private double easeOut(double t) {
    return t * (2d - t);
  }

  @SuppressWarnings("ReturnCount")
  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (currentImage == null) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    int width = getWidth();
    int height = getHeight();
    if (isAnimating() && nextImage != null) {
      double easedProgress = easeOut(animationProgress);
      int offsetX = (int) (width * easedProgress);
      if (moveRight) {
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
  private static final Color SEL_COLOR = Color.WHITE;
  private static final Color DEF_COLOR = new Color(0x64_FF_FF_FF, true);

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int gap;
    if (c instanceof AbstractButton && ((AbstractButton) c).getModel().isSelected()) {
      g2.setColor(SEL_COLOR);
      gap = 0;
    } else {
      g2.setColor(DEF_COLOR);
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
  private final boolean isLeft;
  private boolean isHovered;
  private transient MouseAdapter mouseAdapter;

  protected HoverArrowButton(boolean isLeft) {
    super();
    this.isLeft = isLeft;
  }

  @Override public void updateUI() {
    removeMouseListener(mouseAdapter);
    super.updateUI();
    setContentAreaFilled(false);
    setFocusPainted(false);
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setCursor(new Cursor(Cursor.HAND_CURSOR));
    mouseAdapter = new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        isHovered = true;
        repaint();
      }

      @Override public void mouseExited(MouseEvent e) {
        isHovered = false;
        repaint();
      }
    };
    addMouseListener(mouseAdapter);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.width = 50;
    return d;
  }

  @Override protected void paintComponent(Graphics g) {
    if (isHovered) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
      int arrowHeight = Math.min(25, rect.height / 2);
      int arrowWidth = arrowHeight / 2;
      int centerX = (int) rect.getCenterX();
      int centerY = (int) rect.getCenterY();
      int startX = centerX - (arrowWidth / 2);

      int[] xpt;
      int[] ypt;
      if (isLeft) { // <
        xpt = new int[] {startX + arrowWidth, startX, startX + arrowWidth};
      } else { // >
        xpt = new int[] {startX, startX + arrowWidth, startX};
      }
      ypt = new int[] {centerY - arrowHeight / 2, centerY, centerY + arrowHeight / 2};

      g2.setColor(Color.WHITE);
      g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2.drawPolyline(xpt, ypt, 3);
      g2.dispose();
    }
  }
}
