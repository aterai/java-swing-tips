// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private CrossFade mode = CrossFade.IN;

  private MainPanel() {
    super(new BorderLayout());
    JCheckBox check = new JCheckBox("CrossFade Type?", true);
    ImageIcon icon1 = new ImageIcon(makeImage("example/test.png"));
    ImageIcon icon2 = new ImageIcon(makeImage("example/test.jpg"));
    JButton button = new JButton("change");

    AtomicInteger alpha = new AtomicInteger(10);
    Component crossFade = new JComponent() {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        if (check.isSelected()) {
          float a1 = 1f - alpha.get() * .1f;
          g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a1));
        }
        icon1.paintIcon(this, g2, 0, 0);
        float a2 = alpha.get() * .1f;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a2));
        icon2.paintIcon(this, g2, 0, 0);
        g2.dispose();
      }
    };

    Timer animator = new Timer(50, e -> {
      if (mode == CrossFade.IN && alpha.get() < 10) {
        alpha.incrementAndGet(); // alpha += 1;
      } else if (mode == CrossFade.OUT && alpha.get() > 0) {
        alpha.decrementAndGet(); // alpha -= 1;
      } else {
        ((Timer) e.getSource()).stop();
      }
      crossFade.repaint();
    });

    button.addActionListener(e -> {
      mode = mode.toggle();
      animator.start();
    });

    add(crossFade);
    add(button, BorderLayout.NORTH);
    add(check, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Image makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
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

enum CrossFade {
  IN, OUT;
  public CrossFade toggle() {
    return this.equals(IN) ? OUT : IN;
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
