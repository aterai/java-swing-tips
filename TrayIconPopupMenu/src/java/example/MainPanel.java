// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private final JPopupMenu popup = new JPopupMenu();

  private MainPanel() {
    super(new BorderLayout());
    add(new JLabel("SystemTray.isSupported(): " + SystemTray.isSupported()), BorderLayout.NORTH);

    ButtonGroup bg = new ButtonGroup();
    Box box = Box.createVerticalBox();
    String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    Stream.of(UIManager.getInstalledLookAndFeels())
        .forEach(info -> {
          boolean selected = info.getClassName().equals(lookAndFeel);
          AbstractButton rb = new JRadioButton(info.getName(), selected);
          LookAndFeelUtil.initLookAndFeelAction(info, rb);
          rb.addActionListener(e -> EventQueue.invokeLater(() -> {
            SwingUtilities.updateComponentTreeUI(popup);
            popup.pack();
          }));
          bg.add(rb);
          box.add(rb);
        });
    box.add(Box.createVerticalGlue());
    box.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 25));

    add(box);
    setPreferredSize(new Dimension(320, 240));

    EventQueue.invokeLater(() -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Frame) {
        initPopupMenu((Frame) c);
      }
    });
  }

  private void initPopupMenu(Frame frame) {
    // This code is inspired from:
    // https://web.archive.org/web/20090327054056/http://weblogs.java.net/blog/alexfromsun/archive/2008/02/jtrayicon_updat.html
    // https://github.com/floscher/swinghelper/blob/master/src/java/org/jdesktop/swinghelper/tray/JXTrayIcon.java

    // JWindow dummy = new JWindow(); // Ubuntu?
    JDialog dummy = new JDialog();
    dummy.setUndecorated(true);
    // dummy.setAlwaysOnTop(true);

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Image image = Optional.ofNullable(cl.getResource("example/16x16.png")).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeDefaultTrayImage();
      }
    }).orElseGet(MainPanel::makeDefaultTrayImage);
    TrayIcon icon = new TrayIcon(image, "TRAY", null);
    icon.addMouseListener(new TrayIconPopupMenuHandler(popup, dummy));
    try {
      SystemTray.getSystemTray().add(icon);
    } catch (AWTException ex) {
      throw new IllegalStateException(ex);
    }

    // init JPopupMenu
    popup.addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        /* not needed */
      }

      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        dummy.setVisible(false);
      }

      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        dummy.setVisible(false);
      }
    });
    popup.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
    popup.add(new JRadioButtonMenuItem("JRadioButtonMenuItem"));
    popup.add(new JRadioButtonMenuItem("JRadioButtonMenuItem: 1234567890"));
    popup.add("Open").addActionListener(e -> {
      frame.setExtendedState(Frame.NORMAL);
      frame.setVisible(true);
    });
    popup.add("Exit").addActionListener(e -> {
      SystemTray tray = SystemTray.getSystemTray();
      for (TrayIcon ti : tray.getTrayIcons()) {
        tray.remove(ti);
      }
      for (Frame f : Frame.getFrames()) {
        f.dispose();
      }
      // tray.remove(icon);
      // frame.dispose();
      // dummy.dispose();
    });
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
    } else {
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class TrayIconPopupMenuUtil {
  private TrayIconPopupMenuUtil() {
    /* Singleton */
  }

  // Try to find GraphicsConfiguration, that includes mouse pointer position
  private static GraphicsConfiguration getGraphicsConfiguration(Point p) {
    GraphicsConfiguration gc = null;
    for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
      if (gd.getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
        GraphicsConfiguration dgc = gd.getDefaultConfiguration();
        if (dgc.getBounds().contains(p)) {
          gc = dgc;
          break;
        }
      }
    }
    return gc;
  }

  // Copied from JPopupMenu.java: JPopupMenu#adjustPopupLocationToFitScreen(...)
  public static Point adjustPopupLocation(JPopupMenu popup, Point pt) {
    Point p = new Point(pt);
    if (GraphicsEnvironment.isHeadless()) {
      return p;
    }

    Rectangle screenBounds;
    GraphicsConfiguration gc = getGraphicsConfiguration(p);

    // If not found and popup have invoker, ask invoker about his gc
    if (Objects.isNull(gc) && Objects.nonNull(popup.getInvoker())) {
      gc = popup.getInvoker().getGraphicsConfiguration();
    }

    if (Objects.isNull(gc)) {
      // If we don't have GraphicsConfiguration use primary screen
      screenBounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    } else {
      // If we have GraphicsConfiguration use it to get
      // screen bounds
      screenBounds = gc.getBounds();
    }

    Dimension size = popup.getPreferredSize();

    // Use long variables to prevent overflow
    long pw = (long) p.x + (long) size.width;
    long ph = (long) p.y + (long) size.height;

    if (pw > screenBounds.x + screenBounds.width) {
      p.x -= size.width;
    }
    if (ph > screenBounds.y + screenBounds.height) {
      p.y -= size.height;
    }

    // Change is made to the desired (X, Y) values, when the
    // PopupMenu is too tall OR too wide for the screen
    p.x = Math.max(p.x, screenBounds.x);
    p.y = Math.max(p.y, screenBounds.y);
    return p;
  }
}

class TrayIconPopupMenuHandler extends MouseAdapter {
  private final JPopupMenu popup;
  private final Window dummy;

  protected TrayIconPopupMenuHandler(JPopupMenu popup, Window dummy) {
    super();
    this.popup = popup;
    this.dummy = dummy;
  }

  private void showPopupMenu(MouseEvent e) {
    if (e.isPopupTrigger()) {
      Point p = TrayIconPopupMenuUtil.adjustPopupLocation(popup, e.getPoint());
      dummy.setLocation(p);
      dummy.setVisible(true);
      // dummy.toFront();
      popup.show(dummy, 0, 0);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    showPopupMenu(e);
  }

  @Override public void mousePressed(MouseEvent e) {
    showPopupMenu(e);
  }
}

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() {
    /* Singleton */
  }

  // public static JMenu createLookAndFeelMenu() {
  //   JMenu menu = new JMenu("LookAndFeel");
  //   ButtonGroup lafGroup = new ButtonGroup();
  //   for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
  //     boolean selected = lafInfo.getClassName().equals(lookAndFeel);
  //     AbstractButton lafItem = new JRadioButtonMenuItem(lafInfo.getName(), selected);
  //     initLookAndFeelAction(lafInfo, lafItem);
  //     menu.add(lafItem);
  //     lafGroup.add(lafItem);
  //   }
  //   return menu;
  // }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String lookAndFeel) {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      try {
        UIManager.setLookAndFeel(lookAndFeel);
        LookAndFeelUtil.lookAndFeel = lookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
