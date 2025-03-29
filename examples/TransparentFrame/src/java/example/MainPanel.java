// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  public static final Paint TEXTURE = ImageUtils.makeTexturePaint();
  private static int openFrameCount;

  private MainPanel() {
    super(new BorderLayout());
    JPanel p1 = new JPanel();
    p1.setOpaque(false);

    JPanel p2 = new JPanel() {
      @Override protected void paintComponent(Graphics g) {
        // super.paintComponent(g);
        g.setColor(new Color(0x64_64_32_32, true));
        g.fillRect(0, 0, getWidth(), getHeight());
      }
    };

    JPanel p3 = new JPanel() {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(TEXTURE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
      }
    };

    JDesktopPane desktop = new JDesktopPane();
    desktop.add(createFrame(p1));
    desktop.add(createFrame(p2));
    desktop.add(createFrame(p3));

    String path = "example/GIANT_TCR1_2013.jpg";
    desktop.setBorder(new CentredBackgroundBorder(ImageUtils.makeImage(path)));
    // D3D/OGL: Window translucency doesn't work with accelerated pipelines
    // https://bugs.openjdk.org/browse/JDK-6655001
    // desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

    JMenu menu = new JMenu("Frame");
    menu.setMnemonic(KeyEvent.VK_D);

    JMenuItem menuItem = menu.add("New Frame");
    menuItem.setMnemonic(KeyEvent.VK_N);
    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
    menuItem.setActionCommand("new");
    menuItem.addActionListener(e -> desktop.add(createFrame(null)));

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(menu);

    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));
    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JInternalFrame createFrame(JComponent c) {
    String title = String.format("Frame #%s", ++openFrameCount);
    JInternalFrame frame = new JInternalFrame(title, true, true, true, true);
    if (c instanceof JPanel) {
      JPanel p = (JPanel) c;
      p.add(new JLabel("label"));
      p.add(new JButton("button"));
      frame.setContentPane(p);
    }
    frame.setSize(160, 100);
    frame.setLocation(30 * openFrameCount, 30 * openFrameCount);
    frame.setOpaque(false);
    EventQueue.invokeLater(() -> frame.setVisible(true));
    // desktop.getDesktopManager().activateFrame(frame);
    return frame;
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class ImageUtils {
  private ImageUtils() {
    /* Singleton */
  }

  public static BufferedImage makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(ImageUtils::makeMissingImage);
  }

  public static TexturePaint makeTexturePaint() {
    BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setPaint(new Color(0x64_64_78_64, true));
    g2.fillRect(0, 0, 16, 16);
    int cs = 4;
    for (int i = 0; i * cs < 16; i++) {
      for (int j = 0; j * cs < 16; j++) {
        if ((i + j) % 2 == 0) {
          g2.fillRect(i * cs, j * cs, cs, cs);
        }
      }
    }
    g2.dispose();
    return new TexturePaint(img, new Rectangle(16, 16));
  }

  public static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
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
