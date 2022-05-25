// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    String path = "example/test.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Image image = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    add(new JLabel(new ImageIcon(image)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar();
    JMenu menu = new JMenu("File");
    mb.add(menu);
    menu.add("Open");
    menu.add("Save");
    menu.add("Close");
    menu.add("Exit");
    menu = new JMenu("Edit");
    mb.add(menu);
    menu.add("Cut");
    menu.add("Copy");
    menu.add("Paste");
    JMenu sub = new JMenu("Edit");
    sub.add("Cut");
    sub.add("Copy");
    sub.add("Paste");
    menu.add(sub);
    return mb;
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    UIManager.put("PopupMenuUI", "example.CustomPopupMenuUI");
    // contrib.com.jgoodies.looks.common.ShadowPopupFactory.install();

    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();

    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;

    g2.setColor(Color.WHITE);
    g2.fillRect(x, y, w, h);

    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(x + gap, y + gap, x + w - gap, y + h - gap);
    g2.drawLine(x + gap, y + h - gap, x + w - gap, y + gap);

    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 320;
  }

  @Override public int getIconHeight() {
    return 240;
  }
}
