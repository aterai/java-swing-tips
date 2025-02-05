// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    String path = "example/test.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    BufferedImage img = Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    EventQueue.invokeLater(() -> {
      getRootPane().setGlassPane(new LightboxGlassPane(img));
      getRootPane().getGlassPane().setVisible(false);
    });
    JButton button = new JButton("Open");
    button.addActionListener(e -> getRootPane().getGlassPane().setVisible(true));
    add(makeSamplePanel());
    add(button);
    setPreferredSize(new Dimension(320, 240));
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

  private JPanel makeSamplePanel() {
    JButton b = new JButton("Button & Mnemonic");
    b.setMnemonic(KeyEvent.VK_B);
    JTextField t = new JTextField("TextField & ToolTip");
    t.setToolTipText("ToolTip");
    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    p.add(b, BorderLayout.NORTH);
    p.add(t, BorderLayout.SOUTH);
    p.add(new JScrollPane(new JTree()));
    return p;
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class LightboxGlassPane extends JPanel {
  private static final int BW = 5;
  private final transient AnimeIcon animatedIcon = new AnimeIcon();
  private float alpha;
  private final Dimension currentSize = new Dimension();
  private final Rectangle rect = new Rectangle();
  private final Timer animator = new Timer(10, e -> {
    animatedIcon.next();
    repaint();
  });
  private transient Handler handler;
  private final transient BufferedImage image;

  protected LightboxGlassPane(BufferedImage image) {
    super();
    this.image = image;
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeHierarchyListener(handler);
    super.updateUI();
    setOpaque(false);
    super.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    handler = new Handler();
    addMouseListener(handler);
    addHierarchyListener(handler);
  }

  private final class Handler extends MouseAdapter implements HierarchyListener {
    @Override public void mouseClicked(MouseEvent e) {
      e.getComponent().setVisible(false);
    }

    @Override public void hierarchyChanged(HierarchyEvent e) {
      boolean displayability = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (displayability && !e.getComponent().isDisplayable()) {
        animator.stop();
      }
    }
  }

  @Override public void setVisible(boolean b) {
    boolean oldVisible = isVisible();
    super.setVisible(b);
    JRootPane rootPane = getRootPane();
    if (Objects.nonNull(rootPane) && b != oldVisible) {
      rootPane.getLayeredPane().setVisible(!b);
    }
    if (b && !animator.isRunning()) {
      currentSize.setSize(40, 40);
      alpha = 0f;
      animator.start();
    } else {
      animator.stop();
    }
    animatedIcon.setRunning(b);
  }

  @Override protected void paintComponent(Graphics g) {
    Optional.ofNullable(getRootPane()).ifPresent(r -> r.getLayeredPane().print(g));
    super.paintComponent(g);

    if (currentSize.height < image.getHeight() + BW + BW) {
      currentSize.height += image.getHeight() / 16;
    } else if (currentSize.width < image.getWidth() + BW + BW) {
      currentSize.height = image.getHeight() + BW + BW;
      currentSize.width += image.getWidth() / 16;
    } else if (1f - alpha > 0) {
      currentSize.width = image.getWidth() + BW + BW;
      alpha = alpha + .1f;
    } else {
      animatedIcon.setRunning(false);
      animator.stop();
    }
    rect.setSize(currentSize);
    Rectangle screen = getBounds();
    Point centerPt = new Point(screen.x + screen.width / 2, screen.y + screen.height / 2);
    rect.setLocation(centerPt.x - rect.width / 2, centerPt.y - rect.height / 2);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(new Color(0x64_64_64_64, true));
    g2.fill(screen);
    g2.setPaint(new Color(0xC8_FF_FF_FF, true));
    g2.fill(rect);

    if (alpha > 0) {
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(alpha, 1f)));
      g2.drawImage(image, rect.x + BW, rect.y + BW, image.getWidth(), image.getHeight(), this);
    } else {
      int cx = centerPt.x - animatedIcon.getIconWidth() / 2;
      int cy = centerPt.y - animatedIcon.getIconHeight() / 2;
      animatedIcon.paintIcon(this, g2, cx, cy);
    }
    g2.dispose();
  }
}

class AnimeIcon implements Icon {
  private static final Color ELLIPSE_COLOR = new Color(0x80_80_80);
  private static final double R = 2d;
  private static final double SX = 0d;
  private static final double SY = 0d;
  private static final int WIDTH = (int) (R * 8 + SX * 2);
  private static final int HEIGHT = (int) (R * 8 + SY * 2);
  private final List<Shape> list = new ArrayList<>(Arrays.asList(
      new Ellipse2D.Double(SX + 3 * R, SY + 0 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 5 * R, SY + 1 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 6 * R, SY + 3 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 5 * R, SY + 5 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 3 * R, SY + 6 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 1 * R, SY + 5 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 0 * R, SY + 3 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 1 * R, SY + 1 * R, 2 * R, 2 * R)));
  private boolean running;

  public void next() {
    if (running) {
      // list.add(list.remove(0));
      Collections.rotate(list, 1);
    }
  }

  public void setRunning(boolean isRunning) {
    running = isRunning;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(new Color(0x0, true));
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(ELLIPSE_COLOR);
    int size = list.size();
    for (int i = 0; i < size; i++) {
      float alpha = running ? (i + 1) / (float) size : .5f;
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
      g2.fill(list.get(i));
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return WIDTH;
  }

  @Override public int getIconHeight() {
    return HEIGHT;
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;
    g2.setStroke(new BasicStroke(w / 8f));
    g2.setColor(Color.RED);
    g2.translate(x, y);
    g2.drawLine(gap, gap, w - gap, h - gap);
    g2.drawLine(gap, h - gap, w - gap, gap);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 240;
  }

  @Override public int getIconHeight() {
    return 180;
  }
}
