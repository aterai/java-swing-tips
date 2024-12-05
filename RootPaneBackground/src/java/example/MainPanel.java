// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  public static final Paint TEXTURE = ImageUtils.makeCheckerTexture();

  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel() {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(TEXTURE);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
      }
    };
    p.add(new JButton("button"));

    JInternalFrame frame = new JInternalFrame("InternalFrame", true, true, true, true);
    frame.setContentPane(p);
    frame.setSize(160, 80);
    frame.setLocation(10, 10);
    frame.setOpaque(false);
    EventQueue.invokeLater(() -> frame.setVisible(true));

    JDesktopPane desktop = new JDesktopPane() {
      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
      }
      // @Override protected void paintComponent(Graphics g) {
      //   super.paintComponent(g);
      //   Graphics2D g2 = (Graphics2D) g.create();
      //   g2.setPaint(new Color(100, 100, 100, 100));
      //   g2.fillRect(0, 0, getWidth(), getHeight());
      //   g2.dispose();
      // }
    };
    desktop.add(frame);

    add(desktop);
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
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
    PopupFactory.setSharedInstance(new TranslucentPopupFactory());
    JFrame frame = new JFrame("@title@") {
      @Override protected JRootPane createRootPane() {
        return new JRootPane() {
          // private final Paint texture = makeCheckerTexture();
          @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(TEXTURE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
          }

          @Override public void updateUI() {
            super.updateUI();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL url = cl.getResource("example/test.jpg");
            BufferedImage bi = ImageUtils.getFilteredImage(url);
            setBorder(new CentredBackgroundBorder(bi));
            setOpaque(false);
          }
        };
      }
    };
    // frame.getRootPane().setBackground(Color.BLUE);
    // frame.getLayeredPane().setBackground(Color.GREEN);
    // frame.getContentPane().setBackground(Color.RED);
    Container contentPane = frame.getContentPane();
    if (contentPane instanceof JComponent) {
      ((JComponent) contentPane).setOpaque(false);
    }
    frame.setJMenuBar(ImageUtils.createMenuBar());
    frame.getContentPane().add(new MainPanel());
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class ImageUtils {
  private ImageUtils() {
    /* Singleton */
  }

  public static JMenuBar createMenuBar() {
    UIManager.put("Menu.background", new Color(200, 0, 0, 0));
    UIManager.put("Menu.selectionBackground", new Color(100, 100, 255, 100));
    UIManager.put("Menu.selectionForeground", new Color(200, 200, 200));
    UIManager.put("Menu.useMenuBarBackgroundForTopLevel", Boolean.TRUE);
    JMenuBar mb = new JMenuBar() {
      @Override protected void paintComponent(Graphics g) {
        // super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new Color(100, 100, 100, 100));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
      }
    };
    mb.setOpaque(false);
    Stream.of("File", "Edit", "Help").map(ImageUtils::createMenu).forEach(mb::add);
    return mb;
  }

  private static JMenu createMenu(String key) {
    JMenu menu = new TransparentMenu(key);
    menu.setForeground(new Color(200, 200, 200));
    menu.setOpaque(false); // Motif lnf
    JMenu sub = new TransparentMenu("Submenu");
    sub.add("JMenuItem");
    sub.add("Looooooooooooooooooooong");
    menu.add(sub);
    menu.add("JMenuItem1");
    menu.add("JMenuItem2");
    return menu;
  }

  public static BufferedImage getFilteredImage(URL url) {
    BufferedImage img = Optional.ofNullable(url).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(ImageUtils::makeMissingImage);

    int w = img.getWidth();
    int h = img.getHeight();
    BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    byte[] b = new byte[256];
    IntStream.range(0, b.length).forEach(i -> b[i] = (byte) (i * .5));
    BufferedImageOp op = new LookupOp(new ByteLookupTable(0, b), null);
    op.filter(img, dst);
    return dst;
  }

  private static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
  }

  public static TexturePaint makeCheckerTexture() {
    int cs = 6;
    int sz = cs * cs;
    BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setPaint(new Color(120, 120, 120));
    g2.fillRect(0, 0, sz, sz);
    g2.setPaint(new Color(200, 200, 200, 20));
    for (int i = 0; i * cs < sz; i++) {
      for (int j = 0; j * cs < sz; j++) {
        if ((i + j) % 2 == 0) {
          g2.fillRect(i * cs, j * cs, cs, cs);
        }
      }
    }
    g2.dispose();
    return new TexturePaint(img, new Rectangle(sz, sz));
  }
}

// https://community.oracle.com/thread/1395763 How can I use TextArea with Background Picture ?
// https://ateraimemo.com/Swing/CentredBackgroundBorder.html
class CentredBackgroundBorder implements Border {
  private final BufferedImage image;

  protected CentredBackgroundBorder(BufferedImage image) {
    this.image = image;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    int cx = (width - image.getWidth()) / 2;
    int cy = (height - image.getHeight()) / 2;
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.drawRenderedImage(image, AffineTransform.getTranslateInstance(cx, cy));
    g2.dispose();
  }

  @Override public Insets getBorderInsets(Component c) {
    return new Insets(0, 0, 0, 0);
  }

  @Override public boolean isBorderOpaque() {
    return true;
  }
}

// https://ateraimemo.com/Swing/TranslucentPopupMenu.html
final class TranslucentPopupMenu extends JPopupMenu {
  private static final Paint POPUP_BACK = new Color(250, 250, 250, 100);
  private static final Paint POPUP_LEFT = new Color(230, 230, 230, 100);
  private static final int LEFT_WIDTH = 24;

  @Override public boolean isOpaque() {
    return false;
  }

  @Override public Component add(Component c) {
    if (c instanceof JComponent) {
      ((JComponent) c).setOpaque(false);
    }
    return c;
  }

  @Override public JMenuItem add(JMenuItem menuItem) {
    menuItem.setOpaque(false);
    return super.add(menuItem);
  }

  // private static final Color ALPHA_ZERO = new Color(0x0, true);
  // @Override public void show(Component c, int x, int y) {
  //   EventQueue.invokeLater(() -> {
  //     Container p = getTopLevelAncestor();
  //     if (p instanceof JWindow && ((JWindow) p).getType() == Window.Type.POPUP) {
  //       // Popup$HeavyWeightWindow
  //       p.setBackground(ALPHA_ZERO);
  //     }
  //   });
  //   super.show(c, x, y);
  // }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(POPUP_LEFT);
    g2.fillRect(0, 0, LEFT_WIDTH, getHeight());
    g2.setPaint(POPUP_BACK);
    g2.fillRect(LEFT_WIDTH, 0, getWidth(), getHeight());
    g2.dispose();
  }
}

class TransparentMenu extends JMenu {
  private JPopupMenu popupMenu;

  protected TransparentMenu(String title) {
    super(title);
  }

  // [JDK-4688783] JPopupMenu hardcoded i JMenu - Java Bug System
  // https://bugs.openjdk.org/browse/JDK-4688783
  private void ensurePopupMenuCreated2() {
    if (Objects.isNull(popupMenu)) {
      this.popupMenu = new TranslucentPopupMenu();
      popupMenu.setInvoker(this);
      popupListener = createWinListener(popupMenu);
    }
  }

  @Override public JPopupMenu getPopupMenu() {
    ensurePopupMenuCreated2();
    return popupMenu;
  }

  @Override public JMenuItem add(JMenuItem menuItem) {
    ensurePopupMenuCreated2();
    menuItem.setOpaque(false);
    return popupMenu.add(menuItem);
  }

  @Override public Component add(Component c) {
    ensurePopupMenuCreated2();
    if (c instanceof JComponent) {
      ((JComponent) c).setOpaque(false);
    }
    popupMenu.add(c);
    return c;
  }

  @Override public void addSeparator() {
    ensurePopupMenuCreated2();
    popupMenu.addSeparator();
  }

  @Override public void insert(String s, int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }
    ensurePopupMenuCreated2();
    popupMenu.insert(new JMenuItem(s), pos);
  }

  @Override public JMenuItem insert(JMenuItem mi, int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }
    ensurePopupMenuCreated2();
    popupMenu.insert(mi, pos);
    return mi;
  }

  @Override public void insertSeparator(int index) {
    if (index < 0) {
      throw new IllegalArgumentException("Separator index less than zero.");
    }
    ensurePopupMenuCreated2();
    popupMenu.insert(new JPopupMenu.Separator(), index);
  }

  @Override public boolean isPopupMenuVisible() {
    ensurePopupMenuCreated2();
    return popupMenu.isVisible();
  }
}

/*
<a href="http://today.java.net/pub/a/today/2008/03/18/translucent-and-shaped-swing-windows.html">
Translucent and Shaped Swing Windows | Java.net
</a>
*/
class TranslucentPopupFactory extends PopupFactory {
  @Override public Popup getPopup(Component owner, Component contents, int x, int y) {
    return new TranslucentPopup(owner, contents, x, y);
  }
}

class TranslucentPopup extends Popup {
  private final JWindow popupWindow;

  protected TranslucentPopup(Component owner, Component contents, int ownerX, int ownerY) {
    super(owner, contents, ownerX, ownerY);
    // create a new heavyweight window
    this.popupWindow = new JWindow();
    // mark the popup with partial opacity
    // AWTUtilities.setWindowOpacity(popupWindow, (contents instanceof JToolTip) ? .8f : .95f);
    // popupWindow.setOpacity(.5f);
    // AWTUtilities.setWindowOpaque(popupWindow, false); // Java 1.6.0_10
    GraphicsConfiguration gc = popupWindow.getGraphicsConfiguration();
    if (gc != null && gc.isTranslucencyCapable()) {
      popupWindow.setBackground(new Color(0x0, true)); // Java 1.7.0
    }
    // determine the popup location
    popupWindow.setLocation(ownerX, ownerY);
    // add the contents to the popup
    popupWindow.getContentPane().add(contents);
    contents.invalidate();
    // JComponent parent = (JComponent) contents.getParent();
    // set the shadow border
    // parent.setBorder(new ShadowPopupBorder());
  }

  @Override public void show() {
    // System.out.println("Always Heavy weight!");
    this.popupWindow.setVisible(true);
    this.popupWindow.pack();
    // mark the window as non-opaque, so that the
    // shadow border pixels take on the per-pixel
    // translucency
    // AWTUtilities.setWindowOpaque(this.popupWindow, false);
  }

  @Override public void hide() {
    this.popupWindow.setVisible(false);
    this.popupWindow.removeAll();
    this.popupWindow.dispose();
  }
}
