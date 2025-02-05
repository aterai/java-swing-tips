// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    String path = "example/wi0124-48.png";
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    Icon icon = Optional.ofNullable(url).map(u -> {
      try (InputStream s = u.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return UIManager.getIcon("OptionPane.errorIcon");
      }
    }).orElse(UIManager.getIcon("OptionPane.errorIcon"));

    JTabbedPane tabbedPane = new TabThumbnailTabbedPane();
    tabbedPane.addTab("wi0124-48.png", null, new JLabel(icon), "wi0124-48");
    addImageTab(tabbedPane, "example/GIANT_TCR1_2013.jpg");
    addImageTab(tabbedPane, "example/CRW_3857_JFR.jpg"); // https://sozai-free.com/
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addImageTab(JTabbedPane tabbedPane, String path) {
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    Image img = Optional.ofNullable(url).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    JScrollPane scroll = new JScrollPane(new JLabel(new ImageIcon(img)));
    File f = new File(path);
    tabbedPane.addTab(f.getName(), null, scroll, "tooltip");
  }

  private static Image makeMissingImage() {
    Icon missingIcon = new MissingIcon();
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TabThumbnailTabbedPane extends JTabbedPane {
  private static final double SCALE = .15;
  private int current = -1;

  private Component getTabThumbnail(int index) {
    Component c = getComponentAt(index);
    Icon icon = null;
    if (c instanceof JScrollPane) {
      c = ((JScrollPane) c).getViewport().getView();
      Dimension d = c.getPreferredSize();
      int newW = (int) (d.width * SCALE);
      int newH = (int) (d.height * SCALE);
      BufferedImage image = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = image.createGraphics();
      g2.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2.scale(SCALE, SCALE);
      c.print(g2);
      g2.dispose();
      icon = new ImageIcon(image);
    } else if (c instanceof JLabel) {
      icon = ((JLabel) c).getIcon();
    }
    return new JLabel(icon);
  }

  @Override public JToolTip createToolTip() {
    return current >= 0 ? makeToolTip(current) : null;
  }

  protected JToolTip makeToolTip(int index) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createEmptyBorder());
    p.add(new JLabel(getTitleAt(index)), BorderLayout.NORTH);
    p.add(getTabThumbnail(index));
    JToolTip tip = new JToolTip() {
      @Override public Dimension getPreferredSize() {
        Insets i = getInsets();
        Dimension d = p.getPreferredSize();
        return new Dimension(d.width + i.left + i.right, d.height + i.top + i.bottom);
      }
    };
    tip.setComponent(this);
    LookAndFeel.installColorsAndFont(
        p, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
    tip.setLayout(new BorderLayout());
    tip.add(p);
    return tip;
  }

  @Override public String getToolTipText(MouseEvent e) {
    String str = null;
    int index = indexAtLocation(e.getX(), e.getY());
    if (current == index) {
      str = super.getToolTipText(e);
    }
    current = index;
    return str;
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;
    g2.setColor(Color.WHITE);
    g2.translate(x, y);
    g2.fillRect(0, 0, w, h);
    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(gap, gap, w - gap, h - gap);
    g2.drawLine(gap, h - gap, w - gap, gap);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 1000;
  }

  @Override public int getIconHeight() {
    return 1000;
  }
}
