// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    try {
      Thread.sleep(5000);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
      UIManager.getLookAndFeel().provideErrorFeedback(this);
      Thread.currentThread().interrupt();
    }
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
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
    System.out.println("main start / EDT: " + EventQueue.isDispatchThread());
    createAndShowGui();
    System.out.println("main end");
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JWindow splashScreen = new JWindow();
    EventQueue.invokeLater(() -> {
      System.out.println("splashScreen show start / EDT: " + EventQueue.isDispatchThread());
      String path = "example/splash.png";
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Image img = Optional.ofNullable(cl.getResource(path)).map(url -> {
        try (InputStream s = url.openStream()) {
          return ImageIO.read(s);
        } catch (IOException ex) {
          return makeMissingImage();
        }
      }).orElseGet(MainPanel::makeMissingImage);
      splashScreen.getContentPane().add(new JLabel(new ImageIcon(img)));
      splashScreen.pack();
      splashScreen.setLocationRelativeTo(null);
      splashScreen.setVisible(true);
      System.out.println("splashScreen show end");
    });

    System.out.println("createGUI start / EDT: " + EventQueue.isDispatchThread());
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel()); // new MainPanel() take long time
    frame.pack();
    frame.setLocationRelativeTo(null);
    System.out.println("createGUI end");

    EventQueue.invokeLater(() -> {
      System.out.println("  splashScreen dispose start / EDT: " + EventQueue.isDispatchThread());
      // splashScreen.setVisible(false);
      splashScreen.dispose();
      System.out.println("  splashScreen dispose end");

      System.out.println("  frame show start / EDT: " + EventQueue.isDispatchThread());
      frame.setVisible(true);
      System.out.println("  frame show end");
    });
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();

    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;

    g2.setColor(Color.WHITE);
    g2.fillRect(x, y, w, h);

    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(x + gap, y + gap, x + w - gap, y + h - gap);
    g2.drawLine(x + gap, y + h - gap, x + w - gap, y + gap);

    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 320;
  }

  @Override public int getIconHeight() {
    return 240;
  }
}

//   public static void main(String[] args) {
//     System.out.println("main start / EDT: " + EventQueue.isDispatchThread());
//     EventQueue.invokeLater(MainPanel::createAndShowGui);
//     System.out.println("main end");
//   }
//
//   println static void createAndShowGui() {
//     try {
//       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//     } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//       ex.printStackTrace();
//     }
//     System.out.println("splashScreen show start / EDT: " + EventQueue.isDispatchThread());
//     JWindow splashScreen = new JWindow();
//     Image img = ...
//     splashScreen.getContentPane().add(new JLabel(new ImageIcon(img)));
//     splashScreen.pack();
//     splashScreen.setLocationRelativeTo(null);
//     splashScreen.setVisible(true);
//     System.out.println("splashScreen show end");
//
//     JFrame frame = new JFrame("@title@");
//     frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//     new SwingWorker() {
//       @Override protected Object doInBackground() {
//         System.out.println("frame make start / EDT: " + EventQueue.isDispatchThread());
//         frame.getContentPane().add(new MainPanel()); // new MainPanel() take long time
//         System.out.println("frame make end");
//         return "Done";
//       }
//
//       @Override protected void done() {
//         System.out.println("splashScreen dispose start / EDT: " + EventQueue.isDispatchThread());
//         splashScreen.dispose();
//         System.out.println("splashScreen dispose end");
//         System.out.println("frame show start / EDT: " + EventQueue.isDispatchThread());
//         frame.pack();
//         frame.setLocationRelativeTo(null);
//         frame.setVisible(true);
//         System.out.println("frame show end");
//       }
//     }.execute();
//   }
// }
