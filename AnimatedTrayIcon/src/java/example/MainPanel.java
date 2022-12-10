// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JDialog dialog = new JDialog();
  private final Timer animator = new Timer(100, null);
  private int idx;

  private MainPanel() {
    super();
    setPreferredSize(new Dimension(320, 240));

    if (!SystemTray.isSupported()) {
      throw new UnsupportedOperationException("SystemTray is not supported");
    }

    dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    dialog.setSize(new Dimension(120, 100));
    dialog.setLocationRelativeTo(null);
    dialog.setTitle("TEST: JDialog");

    // TEST:
    // Image img = new ImageIcon(getClass().getResource("animated.gif")).getImage();
    // TrayIcon icon = new TrayIcon(img, "TRAY", popup);
    TrayIcon icon = makeTrayIcon();
    try {
      SystemTray.getSystemTray().add(icon);
    } catch (AWTException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private TrayIcon makeTrayIcon() {
    Image[] images = new Image[4];
    images[0] = makeImage("example/16x16.png");
    images[1] = makeImage("example/16x16l.png");
    images[2] = images[0];
    images[3] = makeImage("example/16x16r.png");

    MenuItem item1 = new MenuItem("Open:Frame");
    item1.addActionListener(e -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Window) {
        c.setVisible(true);
      }
    });

    MenuItem item2 = new MenuItem("Open:Dialog");
    item2.addActionListener(e -> dialog.setVisible(true));

    MenuItem item3 = new MenuItem("Animation:Start");
    item3.addActionListener(e -> animator.start());

    MenuItem item4 = new MenuItem("Animation:Stop");
    item4.addActionListener(e -> {
      animator.stop();
      SystemTray tray = SystemTray.getSystemTray();
      Stream.of(tray.getTrayIcons()).forEach(i -> i.setImage(images[0]));
    });

    MenuItem item5 = new MenuItem("Exit");
    item5.addActionListener(e -> {
      animator.stop();
      SystemTray tray = SystemTray.getSystemTray();
      Stream.of(tray.getTrayIcons()).forEach(tray::remove);
      Stream.of(Frame.getFrames()).forEach(Frame::dispose);
    });

    PopupMenu popup = new PopupMenu();
    popup.add(item1);
    popup.add(item2);
    popup.addSeparator();
    popup.add(item3);
    popup.add(item4);
    popup.addSeparator();
    popup.add(item5);

    TrayIcon icon = new TrayIcon(images[0], "TRAY", popup);
    animator.addActionListener(e -> {
      icon.setImage(images[idx]);
      idx = (idx + 1) % images.length;
    });
    return icon;
  }

  private static Image makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeDefaultTrayImage();
      }
    }).orElseGet(MainPanel::makeDefaultTrayImage);
  }

  private static Image makeDefaultTrayImage() {
    Icon icon = UIManager.getIcon("InternalFrame.icon");
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    icon.paintIcon(null, g2, 0, 0);
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
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
