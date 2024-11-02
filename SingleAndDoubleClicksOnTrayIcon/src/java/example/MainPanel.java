// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private final JPopupMenu popup = new JPopupMenu();

  private MainPanel() {
    super(new BorderLayout());
    boolean supported = SystemTray.isSupported();
    add(new JScrollPane(new JTextArea("SystemTray.isSupported(): " + supported)));
    setPreferredSize(new Dimension(320, 240));
    EventQueue.invokeLater(() -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Frame) {
        JPopupMenu dialog = new JPopupMenu();
        dialog.setLayout(new BorderLayout());
        dialog.add(makeLookAndFeelBox());
        dialog.pack();
        initPopupMenu(dialog, (Frame) c);
      }
    });
  }

  private Component makeLookAndFeelBox() {
    ButtonGroup bg = new ButtonGroup();
    Box box = Box.createVerticalBox();
    String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    Stream.of(UIManager.getInstalledLookAndFeels())
        .forEach(info -> {
          boolean selected = info.getClassName().equals(lookAndFeel);
          AbstractButton rb = new JRadioButton(info.getName(), selected);
          LookAndFeelUtils.initLookAndFeelAction(info, rb);
          rb.addActionListener(e -> EventQueue.invokeLater(() -> {
            SwingUtilities.updateComponentTreeUI(popup);
            popup.pack();
          }));
          bg.add(rb);
          box.add(rb);
        });
    box.add(Box.createVerticalGlue());
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    return box;
  }

  private void openLookAndFeelBox(JPopupMenu lnf, JDialog tmp, Point pt) {
    Point p = TrayIconPopupMenuUtils.adjustPopupLocation(lnf, pt);
    p.move(p.x - 20, p.y - 20);
    tmp.setLocation(p);
    tmp.setVisible(true);
    tmp.toFront();
    lnf.show(tmp, 0, 0);
  }

  private void initPopupMenu(JPopupMenu lnf, Frame frame) {
    JDialog tmp = new JDialog();
    tmp.setUndecorated(true);
    tmp.setAlwaysOnTop(true);
    Point loc = new Point();
    Image image = makeDefaultTrayImage();
    TrayIcon icon = new TrayIcon(image, "TRAY", null);
    icon.addMouseListener(new TrayIconPopupMenuHandler(popup, tmp));
    icon.addMouseListener(new MouseAdapter() {
      private final Timer timer = new Timer(500, e -> {
        ((Timer) e.getSource()).stop();
        openLookAndFeelBox(lnf, tmp, loc);
      });

      @Override public void mousePressed(MouseEvent e) {
        loc.setLocation(e.getPoint());
        if (SwingUtilities.isLeftMouseButton(e)) {
          timer.setDelay(500);
          timer.setRepeats(false);
          timer.start();
        }
      }

      @Override public void mouseClicked(MouseEvent e) {
        boolean isDoubleClick = e.getClickCount() >= 2;
        if (SwingUtilities.isLeftMouseButton(e) && isDoubleClick) {
          timer.stop();
          lnf.setVisible(false);
          frame.setVisible(true);
        }
      }
    });
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
        tmp.setVisible(false);
      }

      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        tmp.setVisible(false);
      }
    });
    popup.add(makeLabel("Quick access", "Left-click")).addActionListener(e ->
        EventQueue.invokeLater(() -> openLookAndFeelBox(lnf, tmp, loc)));
    popup.add(makeLabel("Settings", "Double-click")).addActionListener(e -> {
      frame.setExtendedState(Frame.NORMAL);
      frame.setVisible(true);
    });
    popup.addSeparator();
    popup.add(makeLabel("Documentation", null));
    popup.add(makeLabel("Report bug", null));
    popup.addSeparator();
    popup.add(makeLabel("Exit", "")).addActionListener(e -> {
      SystemTray tray = SystemTray.getSystemTray();
      for (TrayIcon ti : tray.getTrayIcons()) {
        tray.remove(ti);
      }
      for (Frame f : Frame.getFrames()) {
        f.dispose();
      }
    });
  }

  private static String makeLabel(String title, String help) {
    int width = 150;
    String table = String.format("<html><table width='%d'>", width);
    String left = Objects.toString(title, "");
    String td1 = String.format("<td style='text-align:left'>%s</td>", left);
    String right = Objects.toString(help, "");
    String td2 = String.format("<td style='text-align:right'>%s</td>", right);
    return table + td1 + td2;
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
    String oldLookAndFeel = LookAndFeelUtils.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      try {
        UIManager.setLookAndFeel(lookAndFeel);
        LookAndFeelUtils.lookAndFeel = lookAndFeel;
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