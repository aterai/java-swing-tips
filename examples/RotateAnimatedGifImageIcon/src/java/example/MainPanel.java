// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 2));
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/duke.running.gif");
    ImageIcon imageIcon = Optional.ofNullable(url)
        .map(ImageIcon::new)
        .orElseGet(() -> new ImageIcon(makeMissingImage()));

    JLabel label0 = new JLabel(imageIcon);
    label0.setBorder(BorderFactory.createTitledBorder("Default ImageIcon"));
    add(label0);

    JLabel label1 = new JLabel(new ClockwiseRotateIcon(imageIcon));
    label1.setBorder(BorderFactory.createTitledBorder("Wrapping with another Icon"));
    add(label1);

    JPanel label2 = new ImagePanel(imageIcon.getImage());
    label2.setBorder(BorderFactory.createTitledBorder("Override JPanel#paintComponent(...)"));
    add(label2);

    if (url != null) {
      JLabel label3 = new JLabel(new RotateImageIcon(url));
      label3.setBorder(BorderFactory.createTitledBorder("Override ImageIcon#paintIcon(...)"));
      add(label3);
    }
    setPreferredSize(new Dimension(320, 240));
  }

  private static Image makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
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

class ClockwiseRotateIcon implements Icon {
  private final Icon icon;

  protected ClockwiseRotateIcon(Icon icon) {
    this.icon = icon;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x + icon.getIconHeight(), y);
    g2.transform(AffineTransform.getQuadrantRotateInstance(1));
    icon.paintIcon(c, g2, 0, 0);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return icon.getIconHeight();
  }

  @Override public int getIconHeight() {
    return icon.getIconWidth();
  }
}

class ImagePanel extends JPanel {
  private final Image image;

  protected ImagePanel(Image image) {
    super();
    this.image = image;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    int x = getWidth() / 2;
    int y = getHeight() / 2;
    g2.setTransform(AffineTransform.getQuadrantRotateInstance(1, x, y));
    int x2 = x - image.getWidth(this) / 2;
    int y2 = y - image.getHeight(this) / 2;
    // imageIcon.paintIcon(this, g2, x2, y2);
    g2.drawImage(image, x2, y2, this);
    g2.dispose();
  }
}

class RotateImageIcon extends ImageIcon {
  protected RotateImageIcon(URL url) {
    super(url);
  }

  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @Override public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x + getIconHeight(), y);
    g2.transform(AffineTransform.getQuadrantRotateInstance(1));
    super.paintIcon(c, g2, 0, 0);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return super.getIconHeight();
  }

  @Override public int getIconHeight() {
    return super.getIconWidth();
  }
}
