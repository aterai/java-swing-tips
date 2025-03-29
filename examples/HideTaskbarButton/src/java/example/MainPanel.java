// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String HELP = "Hide the TaskBar button when JFrame is minimized";
  private final JCheckBox check = new JCheckBox(HELP);

  private MainPanel() {
    super();
    add(check);
    setPreferredSize(new Dimension(320, 240));

    EventQueue.invokeLater(() -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Window) {
        ((Window) c).addWindowStateListener(e -> {
          if (check.isSelected() && e.getNewState() == Frame.ICONIFIED) {
            e.getWindow().dispose();
          }
        });
        // or
        // ((Window) c).addWindowListener(new WindowAdapter() {
        //   @Override public void windowIconified(WindowEvent e) {
        //     if (check.isSelected()) {
        //       e.getWindow().dispose();
        //     }
        //   }
        // });
      }
    });

    MenuItem item1 = new MenuItem("OPEN");
    item1.addActionListener(e -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Frame) {
        Frame f = (Frame) c;
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);
      }
    });

    MenuItem item2 = new MenuItem("EXIT");
    item2.addActionListener(e -> {
      SystemTray tray = SystemTray.getSystemTray();
      for (TrayIcon icon : tray.getTrayIcons()) {
        tray.remove(icon);
      }
      for (Frame frame : Frame.getFrames()) {
        frame.dispose();
        // frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
      }
    });

    PopupMenu popup = new PopupMenu();
    popup.add(item1);
    popup.add(item2);

    Dimension d = SystemTray.getSystemTray().getTrayIconSize();
    Image image = makePreferredSizeImage(new StarIcon(), d.width, d.height);
    TrayIcon icon = new TrayIcon(image, "TRAY", popup);
    try {
      SystemTray.getSystemTray().add(icon);
    } catch (AWTException ex) {
      throw new IllegalStateException(ex);
    }
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
    frame.setIconImages(Arrays.asList(
        makePreferredSizeImage(new StarIcon(), 16, 16),
        makePreferredSizeImage(new StarIcon(16, 8, 5), 40, 40)));
    if (SystemTray.isSupported()) {
      // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      // frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    } else {
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class StarIcon implements Icon {
  private final Shape star;

  protected StarIcon() {
    star = makeStar(8, 4, 5);
  }

  protected StarIcon(int r1, int r2, int vc) {
    star = makeStar(r1, r2, vc);
  }

  private Path2D makeStar(int r1, int r2, int vc) {
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
    g2.setPaint(Color.ORANGE);
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
