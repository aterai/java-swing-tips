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
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private final JPopupMenu popup = new JPopupMenu();

  private MainPanel() {
    super(new BorderLayout());
    String msg = "SystemTray.isSupported(): " + SystemTray.isSupported();
    add(new JLabel(msg), BorderLayout.NORTH);
    add(LookAndFeelUtils.makeLookAndFeelBox(popup));
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

    // JWindow tmp = new JWindow(); // Ubuntu?
    JDialog tmp = new JDialog();
    tmp.setUndecorated(true);
    // tmp.setAlwaysOnTop(true);

    String path = "example/16x16.png";
    TrayIcon icon = new TrayIcon(TrayIconPopupMenuUtils.makeImage(path), "TRAY", null);
    icon.addMouseListener(new TrayIconPopupMenuHandler(popup, tmp));
    try {
      SystemTray.getSystemTray().add(icon);
    } catch (AWTException ex) {
      throw new IllegalStateException(ex);
    }

    // init JPopupMenu
    popup.addPopupMenuListener(new TemporaryParentPopupListener(tmp));
    popup.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
    popup.add(new JRadioButtonMenuItem("JRadioButtonMenuItem: 1"));
    popup.add(new JRadioButtonMenuItem("JRadioButtonMenuItem: 2"));
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
      // tray.remove(icon); frame.dispose(); tmp.dispose();
    });
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
    } else {
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class TrayIconPopupMenuUtils {
  private TrayIconPopupMenuUtils() {
    /* Singleton */
  }

  // Try to find GraphicsConfiguration, that includes mouse pointer position
  private static GraphicsConfiguration getGraphicsConfiguration(Point p) {
    GraphicsConfiguration configuration = null;
    GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    for (GraphicsDevice device : env.getScreenDevices()) {
      if (device.getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        if (gc.getBounds().contains(p)) {
          configuration = gc;
          break;
        }
      }
    }
    return configuration;
  }

  private static Rectangle getScreenBounds(Component c, Point pt) {
    Point p = new Point(pt);
    Rectangle screenBounds = new Rectangle();
    if (!GraphicsEnvironment.isHeadless()) {
      GraphicsConfiguration gc = getGraphicsConfiguration(p);
      // If not found and popup have invoker, ask invoker about his gc
      if (Objects.isNull(gc) && Objects.nonNull(c)) {
        gc = c.getGraphicsConfiguration();
      }
      if (Objects.isNull(gc)) {
        // If we don't have GraphicsConfiguration use primary screen
        screenBounds.setSize(Toolkit.getDefaultToolkit().getScreenSize());
      } else {
        // If we have GraphicsConfiguration use it to get
        // screen bounds
        screenBounds.setBounds(gc.getBounds());
      }
    }
    return screenBounds;
  }

  // Copied from JPopupMenu.java: JPopupMenu#adjustPopupLocationToFitScreen(...)
  public static Point adjustPopupLocation(JPopupMenu popup, Point pt) {
    Rectangle screenBounds = getScreenBounds(popup.getInvoker(), pt);
    Point p = new Point(pt);
    if (!screenBounds.isEmpty()) {
      Dimension size = popup.getPreferredSize();
      // Use long variables to prevent overflow
      long px = p.x;
      long py = p.y;
      long pw = px + size.width;
      long ph = py + size.height;
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
    }
    return p;
  }

  public static Image makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeDefaultTrayImage();
      }
    }).orElseGet(TrayIconPopupMenuUtils::makeDefaultTrayImage);
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
}

class TemporaryParentPopupListener implements PopupMenuListener {
  private final JDialog parent;

  protected TemporaryParentPopupListener(JDialog parent) {
    this.parent = parent;
  }

  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    /* not needed */
  }

  @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    parent.setVisible(false);
  }

  @Override public void popupMenuCanceled(PopupMenuEvent e) {
    parent.setVisible(false);
  }
}

class TrayIconPopupMenuHandler extends MouseAdapter {
  private final JPopupMenu popup;
  private final Window tmp;

  protected TrayIconPopupMenuHandler(JPopupMenu popup, Window tmp) {
    super();
    this.popup = popup;
    this.tmp = tmp;
  }

  private void showPopupMenu(MouseEvent e) {
    if (e.isPopupTrigger()) {
      Point p = TrayIconPopupMenuUtils.adjustPopupLocation(popup, e.getPoint());
      tmp.setLocation(p);
      tmp.setVisible(true);
      // tmp.toFront();
      popup.show(tmp, 0, 0);
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
final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  // public static JMenu createLookAndFeelMenu() {
  //   JMenu menu = new JMenu("LookAndFeel");
  //   ButtonGroup buttonGroup = new ButtonGroup();
  //   for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
  //     AbstractButton b = makeButton(info);
  //     initLookAndFeelAction(info, b);
  //     menu.add(b);
  //     buttonGroup.add(b);
  //   }
  //   return menu;
  // }

  // private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
  //   boolean selected = info.getClassName().equals(lookAndFeel);
  //   return new JRadioButtonMenuItem(info.getName(), selected);
  // }

  public static Component makeLookAndFeelBox(JPopupMenu popup) {
    ButtonGroup bg = new ButtonGroup();
    Box box = Box.createVerticalBox();
    String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    Stream.of(UIManager.getInstalledLookAndFeels())
        .forEach(info -> {
          AbstractButton rb = makeButton(info, lookAndFeel);
          rb.addActionListener(e -> EventQueue.invokeLater(() -> {
            SwingUtilities.updateComponentTreeUI(popup);
            popup.pack();
          }));
          bg.add(rb);
          box.add(rb);
        });
    box.add(Box.createVerticalGlue());
    box.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 25));
    return box;
  }

  private static JRadioButton makeButton(UIManager.LookAndFeelInfo info, String laf) {
    boolean selected = info.getClassName().equals(laf);
    JRadioButton rb = new JRadioButton(info.getName(), selected);
    initLookAndFeelAction(info, rb);
    return rb;
  }

  private static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
