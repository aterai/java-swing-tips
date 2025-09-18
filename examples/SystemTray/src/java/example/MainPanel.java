// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
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

    Image image = makeImage("example/16x16.png");
    PopupMenu popup = makePopupMenu(frame);
    return new TrayIcon(image, "TRAY", popup);
  }

  private static PopupMenu makePopupMenu(JFrame frame) {
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
    return popup;
  }

  public static Image makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(u -> {
      Image img;
      try (InputStream s = u.openStream()) {
        img = ImageIO.read(s);
      } catch (IOException ex) {
        img = makeDefaultTrayImage();
      }
      return img;
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
      Logger.getGlobal().severe(ex::getMessage);
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
