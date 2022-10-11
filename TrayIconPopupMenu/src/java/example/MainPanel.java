// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
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

    ButtonGroup group = new ButtonGroup();
    Box box = Box.createVerticalBox();
    Stream.of(LookAndFeelEnum.values())
        .map(lnf -> new ChangeLookAndFeelAction(lnf, Collections.singletonList(popup)))
        .map(JRadioButton::new)
        .forEach(rb -> {
          group.add(rb);
          box.add(rb);
        });
    // for (LookAndFeelEnum lnf : LookAndFeelEnum.values()) {
    //   JRadioButton rb = new JRadioButton(new ChangeLookAndFeelAction(lnf, Arrays.asList(popup)));
    //   group.add(rb);
    //   box.add(rb);
    // }
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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

enum LookAndFeelEnum {
  METAL("javax.swing.plaf.metal.MetalLookAndFeel"),
  MAC("com.sun.java.swing.plaf.mac.MacLookAndFeel"),
  MOTIF("com.sun.java.swing.plaf.motif.MotifLookAndFeel"),
  WINDOWS("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"),
  GTK("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"),
  NIMBUS("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
  private final String clazz;

  LookAndFeelEnum(String clazz) {
    this.clazz = clazz;
  }

  public String getClassName() {
    return clazz;
  }
}

class ChangeLookAndFeelAction extends AbstractAction {
  private final String lnf;
  private final List<? extends Component> list;

  protected ChangeLookAndFeelAction(LookAndFeelEnum lookAndFeels, List<? extends Component> list) {
    super(lookAndFeels.toString());
    this.list = list;
    this.lnf = lookAndFeels.getClassName();
    this.setEnabled(isAvailableLookAndFeel(lnf));
  }

  private static boolean isAvailableLookAndFeel(String laf) {
    Class<?> lnfClass;
    try {
      lnfClass = Class.forName(laf);
    } catch (ClassNotFoundException ex) {
      return false;
    }
    try {
      LookAndFeel newLnF = (LookAndFeel) lnfClass.getConstructor().newInstance();
      return newLnF.isSupportedLookAndFeel();
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
      return false;
    }
  }

  @Override public void actionPerformed(ActionEvent e) {
    try {
      UIManager.setLookAndFeel(lnf);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      // System.out.println("Failed loading L&F: " + lnf);
      ex.printStackTrace();
      UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
    }
    for (Frame f : Frame.getFrames()) {
      SwingUtilities.updateComponentTreeUI(f);
      f.pack();
    }
    list.forEach(SwingUtilities::updateComponentTreeUI);
  }
}
