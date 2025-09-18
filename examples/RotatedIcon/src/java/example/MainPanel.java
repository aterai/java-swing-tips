// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    String path = "example/duke.gif";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(url -> {
      Icon icn;
      try (InputStream s = url.openStream()) {
        icn = new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        icn = UIManager.getIcon("OptionPane.warningIcon");
      }
      return icn;
    }).orElseGet(() -> UIManager.getIcon("OptionPane.warningIcon"));

    add(makeLabel("Default", icon));
    add(makeLabel("Rotate: 180", new RotateIcon(icon, 180)));
    add(makeLabel("Rotate:  90", new RotateIcon(icon, 90)));
    add(makeLabel("Rotate: -90", new RotateIcon(icon, -90)));
    setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 32));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JLabel makeLabel(String title, Icon icon) {
    JLabel l = new JLabel(title, icon, SwingConstants.CENTER);
    l.setVerticalTextPosition(SwingConstants.BOTTOM);
    l.setHorizontalTextPosition(SwingConstants.CENTER);
    l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    return l;
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

class RotateIcon implements Icon {
  private final Dimension dim = new Dimension();
  private final Image image;
  private final AffineTransform trans;

  protected RotateIcon(Icon icon, int rotate) {
    if (rotate % 90 != 0) {
      throw new IllegalArgumentException(rotate + ": Rotate must be (rotate % 90 == 0)");
    }
    dim.setSize(icon.getIconWidth(), icon.getIconHeight());
    image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
    Graphics g = image.getGraphics();
    icon.paintIcon(null, g, 0, 0);
    g.dispose();

    int numQuadrants = rotate / 90 % 4;
    switch (numQuadrants) {
      case 3:
      case -1:
        trans = AffineTransform.getTranslateInstance(0, dim.width);
        dim.setSize(icon.getIconHeight(), icon.getIconWidth());
        break;
      case 1:
      case -3:
        trans = AffineTransform.getTranslateInstance(dim.height, 0);
        dim.setSize(icon.getIconHeight(), icon.getIconWidth());
        break;
      case 2:
        trans = AffineTransform.getTranslateInstance(dim.width, dim.height);
        break;
      default:
        trans = AffineTransform.getTranslateInstance(0, 0);
        break;
    }
    trans.rotate(Math.toRadians(90d * numQuadrants));
    // or: trans.rotate(Math.toRadians(degrees));
    // https://ateraimemo.com/Swing/QuadrantRotateIcon.html
    // or: trans.quadrantRotate(numQuadrants);
    // or: trans.concatenate(AffineTransform.getQuadrantRotateInstance(numQuadrants));
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.drawImage(image, trans, c);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return dim.width;
  }

  @Override public int getIconHeight() {
    return dim.height;
  }
}
