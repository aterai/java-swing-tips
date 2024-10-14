// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String TEXT = String.join("\n",
      "icon.addMouseListener(new MouseAdapter() {",
      "  public void mouseClicked(MouseEvent e) {",
      "    boolean doubleClick = e.getClickCount() >= 2;",
      "    if (SwingUtilities.isLeftMouseButton(e) && doubleClick) {",
      "      frame.setVisible(true);",
      "    } else if (frame.isVisible()) {",
      "      frame.setExtendedState(Frame.NORMAL);",
      "      frame.toFront();",
      "    }",
      "  }",
      "});"
  );

  private MainPanel() {
    super(new BorderLayout());
    add(new JScrollPane(new JTextArea(TEXT)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TrayIcon makeTrayIcon(JFrame frame) {
    MenuItem open = new MenuItem("Option");
    open.addActionListener(e -> frame.setVisible(true));

    MenuItem exit = new MenuItem("Exit");
    exit.addActionListener(e -> {
      SystemTray tray = SystemTray.getSystemTray();
      for (TrayIcon icon : tray.getTrayIcons()) {
        tray.remove(icon);
      }
      // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      // frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
      frame.dispose();
    });

    PopupMenu popup = new PopupMenu();
    popup.add(open);
    popup.add(exit);

    Dimension d = SystemTray.getSystemTray().getTrayIconSize();
    Image image = makePreferredSizeImage(new StarIcon(), d.width, d.height);
    TrayIcon icon = new TrayIcon(image, "Click Test", popup);
    icon.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        boolean isDoubleClick = e.getClickCount() >= 2;
        if (SwingUtilities.isLeftMouseButton(e) && isDoubleClick) {
          frame.setVisible(true);
        } else if (frame.isVisible()) {
          frame.setExtendedState(Frame.NORMAL);
          frame.toFront();
        }
      }
    });

    return icon;
  }

  private static Image makePreferredSizeImage(Icon icon, int w, int h) {
    int iw = icon.getIconWidth();
    int ih = icon.getIconHeight();
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    icon.paintIcon(null, g2, (w - iw) / 2, (h - ih) / 2);
    g2.dispose();
    return image;
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

    if (SystemTray.isSupported()) {
      frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
      try {
        SystemTray.getSystemTray().add(makeTrayIcon(frame));
      } catch (AWTException ex) {
        throw new IllegalStateException(ex);
      }
    }
  }
}

class StarIcon implements Icon {
  private final Shape star = makeStar(8, 4, 5);

  public Path2D makeStar(int r1, int r2, int vc) {
    double or = Math.max(r1, r2);
    double ir = Math.min(r1, r2);
    double agl = 0d;
    double add = Math.PI / vc;
    Path2D p = new Path2D.Double();
    p.moveTo(or, 0d);
    for (int i = 0; i < vc * 2 - 1; i++) {
      agl += add;
      double r = i % 2 == 0 ? ir : or;
      p.lineTo(r * Math.cos(agl), r * Math.sin(agl));
    }
    p.closePath();
    AffineTransform at = AffineTransform.getRotateInstance(-Math.PI / 2d, or, 0d);
    return new Path2D.Double(p, at);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(Color.PINK);
    g2.fill(star);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return star.getBounds().width;
  }

  @Override public int getIconHeight() {
    return star.getBounds().height;
  }
}
