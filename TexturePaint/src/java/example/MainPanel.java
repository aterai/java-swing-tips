// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final transient TexturePaint texture;

  public MainPanel() {
    super(new BorderLayout());

    BufferedImage bi = Optional.ofNullable(getClass().getResource("16x16.png"))
        .map(url -> {
          try {
            return ImageIO.read(url);
          } catch (IOException ex) {
            return makeMissingImage();
          }
        }).orElseGet(() -> makeMissingImage());
    texture = new TexturePaint(bi, new Rectangle(bi.getWidth(), bi.getHeight()));

    add(new JLabel("@title@"));
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(texture);
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
    super.paintComponent(g);
  }

  private static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
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
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
