// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
// import javax.swing.plaf.basic.BasicButtonUI;

public final class MainPanel extends JPanel {
  private static final Paint TEXTURE = ImageUtils.makeCheckerTexture();

  private MainPanel() {
    super();
    // ecqlipse 2 PNG by chrfb on DeviantArt
    // https://www.deviantart.com/chrfb/art/ecqlipse-2-PNG-59941546
    String path1 = "example/RECYCLE BIN - EMPTY_16x16-32.png";
    URL url = Thread.currentThread().getContextClassLoader().getResource(path1);
    add(makeButton(makeTitleWithIcon(url, "align=top", "top")));
    add(makeButton(makeTitleWithIcon(url, "align=middle", "middle")));
    add(makeButton(makeTitleWithIcon(url, "align=bottom", "bottom")));

    Icon icon = url == null ? UIManager.getIcon("html.missingImage") : new ImageIcon(url);
    JLabel label = new JLabel("JLabel", icon, SwingConstants.CENTER);
    label.setForeground(Color.WHITE);
    label.setAlignmentX(CENTER_ALIGNMENT);
    AbstractButton b = makeButton("");
    b.setAlignmentX(CENTER_ALIGNMENT);
    JPanel p = new JPanel();
    p.setLayout(new OverlayLayout(p));
    p.setOpaque(false);
    p.add(label);
    p.add(b);
    add(p);
    add(makeButton("☎ text"));
    add(new TranslucentButton("TranslucentButton", icon));
    add(makeButton("1"));
    add(makeButton("22222222"));
    add(makeButton("333333333333333333"));
    add(makeButton("44444444444444444444444444444"));

    BufferedImage bi = ImageUtils.getFilteredImage("example/test.jpg");
    setBorder(new CentredBackgroundBorder(bi));
    // setBackground(new Color(50, 50, 50));
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
  }

  private static String makeTitleWithIcon(URL url, String title, String align) {
    String html = "<html><p align='%s'><img src='%s' align='%s' />&nbsp;%s</p>";
    return String.format(html, align, url, align, title);
  }

  private static AbstractButton makeButton(String title) {
    return new JButton(title) {
      @Override public void updateUI() {
        super.updateUI();
        setVerticalAlignment(CENTER);
        setVerticalTextPosition(CENTER);
        setHorizontalAlignment(CENTER);
        setHorizontalTextPosition(CENTER);
        setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        setMargin(new Insets(2, 8, 2, 8));
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setIcon(new TranslucentButtonIcon(this));
      }
    };
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

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(TEXTURE);
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
    super.paintComponent(g);
  }
}

class TranslucentButton extends JButton {
  private static final Color TL = new Color(1f, 1f, 1f, .2f);
  private static final Color BR = new Color(0f, 0f, 0f, .4f);
  private static final Color ST = new Color(1f, 1f, 1f, .2f);
  private static final Color SB = new Color(1f, 1f, 1f, .1f);
  private static final float R = 8f;

  // protected TranslucentButton(String text) {
  //   super(text);
  // }

  protected TranslucentButton(String text, Icon icon) {
    super(text, icon);
  }

  @Override public void updateUI() {
    super.updateUI();
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBorderPainted(false);
    setOpaque(false);
    setForeground(Color.WHITE);
  }

  @Override protected void paintComponent(Graphics g) {
    float x = 0f;
    float y = 0f;
    float w = getWidth();
    float h = getHeight();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Shape area = new RoundRectangle2D.Float(x, y, w - 1f, h - 1f, R, R);
    Color ssc = TL;
    Color bgc = BR;
    ButtonModel m = getModel();
    if (m.isPressed()) {
      ssc = SB;
      bgc = ST;
    } else if (m.isRollover()) {
      ssc = ST;
      bgc = SB;
    }
    g2.setPaint(new GradientPaint(x, y, ssc, x, y + h, bgc, true));
    g2.fill(area);
    g2.setPaint(BR);
    g2.draw(area);
    g2.dispose();
    super.paintComponent(g);
  }
}

class TranslucentButtonIcon implements Icon {
  private static final Color TL = new Color(1f, 1f, 1f, .2f);
  private static final Color BR = new Color(0f, 0f, 0f, .4f);
  private static final Color ST = new Color(1f, 1f, 1f, .2f);
  private static final Color SB = new Color(1f, 1f, 1f, .1f);
  private static final float R = 8f;
  private int width;
  private int height;

  protected TranslucentButtonIcon(JComponent c) {
    Insets i = c.getInsets();
    Dimension d = c.getPreferredSize();
    width = d.width - i.left - i.right;
    height = d.height - i.top - i.bottom;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (c instanceof AbstractButton) {
      AbstractButton b = (AbstractButton) c;
      // XXX: Insets i = b.getMargin();
      Insets i = b.getInsets();
      int w = c.getWidth();
      int h = c.getHeight();
      width = w - i.left - i.right;
      height = h - i.top - i.bottom;
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int fx = x - i.left;
      int fy = y - i.top;
      Shape area = new RoundRectangle2D.Float(fx, fy, w - 1f, h - 1f, R, R);
      Color ssc = TL;
      Color bgc = BR;
      ButtonModel m = b.getModel();
      if (m.isPressed()) {
        ssc = SB;
        bgc = ST;
      } else if (m.isRollover()) {
        ssc = ST;
        bgc = SB;
      }
      g2.setPaint(new GradientPaint(0f, 0f, ssc, 0f, h, bgc, true));
      g2.fill(area);
      g2.setPaint(BR);
      g2.draw(area);
      g2.dispose();
    }
  }

  @Override public int getIconWidth() {
    return Math.max(width, 100);
  }

  @Override public int getIconHeight() {
    return Math.max(height, 20);
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

final class ImageUtils {
  private ImageUtils() {
    /* Singleton */
  }

  public static BufferedImage getFilteredImage(String path) {
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    BufferedImage image = Optional.ofNullable(url).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(ImageUtils::makeMissingImage);

    int w = image.getWidth();
    int h = image.getHeight();
    BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    byte[] b = new byte[256];
    for (int i = 0; i < b.length; i++) {
      b[i] = (byte) (i * .5);
    }
    BufferedImageOp op = new LookupOp(new ByteLookupTable(0, b), null);
    op.filter(image, dst);
    return dst;
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
