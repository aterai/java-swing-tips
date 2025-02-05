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
    super();
    setPreferredSize(new Dimension(320, 240));
  }

  private static TrayIcon makeTrayIcon(JFrame frame) {
    // SystemTray tray = SystemTray.getSystemTray();
    // Dimension d = tray.getTrayIconSize();
    // BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
    // ImageIcon i = new ImageIcon(getClass().getResource("16x16.png"));
    // Graphics2D g2 = image.createGraphics();
    // g2.setBackground(new Color(0x0, true));
    // g2.clearRect(0, 0, d.width, d.height);
    // i.paintIcon(null, g2, (d.width - i.getIconWidth()) / 2, (d.height - i.getIconWidth()) / 2);
    // g2.dispose();

    MenuItem item1 = new MenuItem("OPEN");
    item1.addActionListener(e -> {
      frame.setExtendedState(Frame.NORMAL);
      frame.setVisible(true);
    });

    MenuItem item2 = new MenuItem("EXIT");
    item2.addActionListener(e -> {
      SystemTray tray = SystemTray.getSystemTray();
      for (TrayIcon icon : tray.getTrayIcons()) {
        tray.remove(icon);
      }
      frame.dispose();
      // System.exit(0);
    });

    PopupMenu popup = new PopupMenu();
    popup.add(item1);
    popup.add(item2);

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Image image = Optional.ofNullable(cl.getResource("example/16x16.png")).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeDefaultTrayImage();
      }
    }).orElseGet(MainPanel::makeDefaultTrayImage);
    return new TrayIcon(image, "TRAY", popup);
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
    if (SystemTray.isSupported()) {
      frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
      frame.addWindowStateListener(e -> {
        if (e.getNewState() == Frame.ICONIFIED) {
          e.getWindow().dispose();
        }
      });
      try {
        SystemTray.getSystemTray().add(makeTrayIcon(frame));
      } catch (AWTException ex) {
        throw new IllegalStateException(ex);
      }
    } else {
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
