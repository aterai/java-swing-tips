// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JButton button1 = new RadialGradientButton("JButton JButton JButton JButton");
    button1.setForeground(Color.WHITE);

    JButton button2 = new RadialGradientPaintButton("JButton JButton JButton JButton");
    button2.setForeground(Color.WHITE);

    JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 50)) {
      private final TexturePaint texture = TextureUtils.createCheckerTexture(16, new Color(0xEE_32_32_32, true));
      @Override public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(texture);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
      }
    };
    p.setOpaque(false);
    p.add(button1);
    p.add(button2);

    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
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

// Stunning hover effects with CSS variables ? Prototypr
// https://blog.prototypr.io/stunning-hover-effects-with-css-variables-f855e7b95330
class RadialGradientButton extends JButton {
  private static final int DELTA = 10;
  private static final double ARC_WIDTH = 32d;
  private static final double ARC_HEIGHT = 32d;
  private int radius;
  private final float[] dist = { 0f, 1f };
  private final Color[] colors = { new Color(0x64_44_05_F7, true), new Color(0x00_F7_23_59, true) };
  private final Timer timer1 = new Timer(10, e -> {
    radius = Math.min(200, radius + DELTA);
    repaint();
  });
  private final Timer timer2 = new Timer(10, e -> {
    radius = Math.max(0, radius - DELTA);
    repaint();
  });
  private final Point pt = new Point();
  private Shape shape;
  private Rectangle base;

  protected RadialGradientButton(String title) {
    super(title);

    MouseAdapter listener = new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        timer2.stop();
        if (!timer1.isRunning()) {
          timer1.start();
        }
      }

      @Override public void mouseExited(MouseEvent e) {
        timer1.stop();
        if (!timer2.isRunning()) {
          timer2.start();
        }
      }

      @Override public void mouseMoved(MouseEvent e) {
        pt.setLocation(e.getPoint());
        repaint();
      }

      @Override public void mouseDragged(MouseEvent e) {
        pt.setLocation(e.getPoint());
        repaint();
      }
    };
    addMouseListener(listener);
    addMouseMotionListener(listener);
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBackground(new Color(0xF7_23_59));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    update();
  }

  @Override public boolean contains(int x, int y) {
    update();
    return Optional.ofNullable(shape).map(s -> s.contains(x, y)).orElse(false);
  }

  protected void update() {
    if (!getBounds().equals(base)) {
      base = getBounds();
      shape = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, ARC_WIDTH, ARC_HEIGHT);
    }
  }

  // @Override protected void paintBorder(Graphics g) {
  //   update();
  //   Graphics2D g2 = (Graphics2D) g.create();
  //   g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  //   // g2.setStroke(new BasicStroke(2.5f));
  //   if (getModel().isArmed()) {
  //     g2.setPaint(new Color(0x64_44_05_F7, true));
  //   } else {
  //     g2.setPaint(new Color(0xF7_23_59).darker());
  //   }
  //   g2.draw(shape);
  //   g2.dispose();
  // }

  @Override public void paintComponent(Graphics g) {
    update();

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // g2.setComposite(AlphaComposite.Clear);
    // g2.setPaint(new Color(0x0, true));
    // g2.fillRect(0, 0, getWidth(), getHeight());

    g2.setComposite(AlphaComposite.Src);
    g2.setPaint(new Color(getModel().isArmed() ? 0xFF_AA_AA : 0xF7_23_59));
    g2.fill(shape);

    if (radius > 0) {
      int r2 = radius + radius;
      g2.setPaint(new RadialGradientPaint(pt, r2, dist, colors));
      g2.setComposite(AlphaComposite.SrcAtop);
      g2.setClip(shape);
      g2.fill(new Ellipse2D.Double(pt.x - radius, pt.y - radius, r2, r2));
    }
    g2.dispose();

    super.paintComponent(g);
  }
}

class RadialGradientPaintButton extends JButton {
  private static final int DELTA = 10;
  private static final double ARC_WIDTH = 32d;
  private static final double ARC_HEIGHT = 32d;
  private int radius;
  private final float[] dist = { 0f, 1f };
  private final Color[] colors = { new Color(0x64_44_05_F7, true), new Color(0x00_F7_23_59, true) };
  private final Timer timer1 = new Timer(10, e -> {
    radius = Math.min(200, radius + DELTA);
    repaint();
  });
  private final Timer timer2 = new Timer(10, e -> {
    radius = Math.max(0, radius - DELTA);
    repaint();
  });
  private final Point pt = new Point();
  private Shape shape;
  private Rectangle base;
  private transient BufferedImage buf;

  protected RadialGradientPaintButton(String title) {
    super(title);

    MouseAdapter listener = new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        timer2.stop();
        if (!timer1.isRunning()) {
          timer1.start();
        }
      }

      @Override public void mouseExited(MouseEvent e) {
        timer1.stop();
        if (!timer2.isRunning()) {
          timer2.start();
        }
      }

      @Override public void mouseMoved(MouseEvent e) {
        pt.setLocation(e.getPoint());
        repaint();
      }

      @Override public void mouseDragged(MouseEvent e) {
        pt.setLocation(e.getPoint());
        repaint();
      }
    };
    addMouseListener(listener);
    addMouseMotionListener(listener);
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBackground(new Color(0xF7_23_59));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    update();
  }

  @Override public boolean contains(int x, int y) {
    update();
    return Optional.ofNullable(shape).map(s -> s.contains(x, y)).orElse(false);
  }

  protected void update() {
    if (!getBounds().equals(base)) {
      base = getBounds();
      int w = getWidth();
      int h = getHeight();
      if (w > 0 && h > 0) {
        buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      }
      shape = new RoundRectangle2D.Double(0, 0, w - 1, h - 1, ARC_WIDTH, ARC_HEIGHT);
    }
    if (buf == null) {
      return;
    }

    Graphics2D g2 = buf.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setComposite(AlphaComposite.Clear);
    g2.fillRect(0, 0, getWidth(), getHeight());

    g2.setComposite(AlphaComposite.Src);
    g2.setPaint(new Color(getModel().isArmed() ? 0xFF_AA_AA : 0xF7_23_59));
    g2.fill(shape);

    if (radius > 0) {
      int r2 = radius + radius;
      g2.setPaint(new RadialGradientPaint(pt, r2, dist, colors));
      g2.setComposite(AlphaComposite.SrcAtop);
      // g2.setClip(shape);
      g2.fill(new Ellipse2D.Double(pt.x - radius, pt.y - radius, r2, r2));
    }
    g2.dispose();
  }

  @Override public void paintComponent(Graphics g) {
    update();
    g.drawImage(buf, 0, 0, this);
    super.paintComponent(g);
  }
}

final class TextureUtils {
  private TextureUtils() { /* HideUtilityClassConstructor */ }

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
