// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();

    BufferedImage img = null;
    try {
      img = ImageIO.read(getClass().getResource("test.jpg"));
    } catch (IOException ex) {
      ex.printStackTrace();
      img = makeMissingImage();
    }
    BufferedImage image = img;

    int width = image.getWidth();
    int height = image.getHeight();
    Shape shape = new RoundRectangle2D.Float(0f, 0f, width / 2f, height / 2f, 50f, 50f);

    BufferedImage clippedImage = makeClippedImage(image, shape);

    JButton button1 = new JButton("clipped window");
    button1.addActionListener(e -> {
      JWindow window = new JWindow();
      window.getContentPane().add(makePanel(image));
      window.setShape(shape);
      window.pack();
      window.setLocationRelativeTo(((AbstractButton) e.getSource()).getRootPane());
      window.setVisible(true);
    });

    JButton button2 = new JButton("soft clipped window");
    button2.addActionListener(e -> {
      JWindow window = new JWindow();
      window.setBackground(new Color(0x0, true));
      window.getContentPane().add(makePanel(clippedImage));
      window.pack();
      window.setLocationRelativeTo(((AbstractButton) e.getSource()).getRootPane());
      window.setVisible(true);
    });

    add(button1);
    add(button2);
    setPreferredSize(new Dimension(320, 240));
  }

  private Component makePanel(BufferedImage image) {
    JPanel panel = new JPanel(new BorderLayout()) {
      @Override public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(this) / 2, image.getHeight(this) / 2);
      }

      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.drawImage(image, 0, 0, this);
        g2.dispose();
        super.paintComponent(g);
      }
    };

    DragWindowListener dwl = new DragWindowListener();
    panel.addMouseListener(dwl);
    panel.addMouseMotionListener(dwl);

    JButton close = new JButton("close");
    close.addActionListener(e -> {
      Component c = (Component) e.getSource();
      Window window = SwingUtilities.getWindowAncestor(c);
      window.dispose();
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 0, 10, 30));
    box.add(Box.createHorizontalGlue());
    box.add(close);
    // box.setOpaque(false);

    panel.add(box, BorderLayout.SOUTH);
    panel.setOpaque(false);
    return panel;
  }

  // @see https://community.oracle.com/blogs/campbell/2006/07/19/java-2d-trickery-soft-clipping
  // campbell: Java 2D Trickery: Soft Clipping Blog | Oracle Community
  private static BufferedImage makeClippedImage(BufferedImage source, Shape shape) {
    int width = source.getWidth();
    int height = source.getHeight();

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    // g2.setComposite(AlphaComposite.Clear);
    // g2.fillRect(0, 0, width, height);

    g2.setComposite(AlphaComposite.Src);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // g2.setColor(Color.WHITE);
    g2.fill(shape);

    g2.setComposite(AlphaComposite.SrcAtop);
    g2.drawImage(source, 0, 0, null);
    g2.dispose();

    return image;
  }

  private static BufferedImage makeMissingImage() {
    BufferedImage image = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    g2.setPaint(Color.RED);
    g2.fillRect(0, 0, image.getWidth(), image.getHeight());
    g2.dispose();
    return image;
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

class DragWindowListener extends MouseAdapter {
  private final Point startPt = new Point();

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      startPt.setLocation(e.getPoint());
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    Component c = SwingUtilities.getRoot(e.getComponent());
    if (c instanceof Window && SwingUtilities.isLeftMouseButton(e)) {
      Window window = (Window) c;
      Point pt = window.getLocation();
      window.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
    }
  }
}
