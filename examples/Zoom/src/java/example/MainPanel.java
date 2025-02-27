// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseWheelListener;
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
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return new MissingIcon();
      }
    }).orElseGet(MissingIcon::new);
    ZoomImage zoom = new ZoomImage(icon);

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

class ZoomImage extends JPanel {
  private transient MouseWheelListener handler;
  private final transient Icon icon;
  private double scale = 1d;

  protected ZoomImage(Icon icon) {
    super();
    this.icon = icon;
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
    icon.paintIcon(this, g2, 0, 0);
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
