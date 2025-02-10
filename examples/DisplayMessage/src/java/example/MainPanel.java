// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String path = "example/16x16.png";
    Image img = ImageUtils.getImage(path);
    TrayIcon icon = new TrayIcon(img, "TRAY", makePopupMenu());
    // icon.addActionListener(e -> log.append(e.toString() + "\n"));
    try {
      SystemTray.getSystemTray().add(icon);
    } catch (AWTException ex) {
      throw new IllegalStateException(ex);
    }
    add(makeTestPanel(), BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makeTestPanel() {
    // ERROR, WARNING, INFO, NONE
    TrayIcon.MessageType[] values = TrayIcon.MessageType.values();
    JComboBox<TrayIcon.MessageType> msgType = new JComboBox<>(values);
    JButton msgButton = new JButton("TrayIcon#displayMessage()");
    msgButton.addActionListener(e -> {
      TrayIcon[] icons = SystemTray.getSystemTray().getTrayIcons();
      if (icons.length > 0) {
        TrayIcon.MessageType type = msgType.getItemAt(msgType.getSelectedIndex());
        icons[0].displayMessage("caption", "text text", type);
      }
    });
    JPanel p = new JPanel();
    p.add(msgType);
    p.add(msgButton);
    return p;
  }

  private PopupMenu makePopupMenu() {
    MenuItem openItem = new MenuItem("OPEN");
    openItem.addActionListener(e -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Window) {
        c.setVisible(true);
      }
    });
    MenuItem exitItem = new MenuItem("EXIT");
    exitItem.addActionListener(e -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Window) {
        ((Window) c).dispose();
      }
      SystemTray tray = SystemTray.getSystemTray();
      for (TrayIcon icon : tray.getTrayIcons()) {
        tray.remove(icon);
      }
    });
    PopupMenu popup = new PopupMenu();
    popup.add(openItem);
    popup.add(exitItem);
    return popup;
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
      frame.addWindowListener(new WindowAdapter() {
        @Override public void windowIconified(WindowEvent e) {
          e.getWindow().dispose();
        }
      });
    } else {
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    frame.getContentPane().add(new MainPanel());
    frame.setResizable(false);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class ImageUtils {
  private ImageUtils() {
    /* Singleton */
  }

  public static Image getImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeDefaultTrayImage();
      }
    }).orElseGet(ImageUtils::makeDefaultTrayImage);
  }

  public static Image makeDefaultTrayImage() {
    Icon icon = UIManager.getIcon("InternalFrame.icon");
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    icon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
  }
}
