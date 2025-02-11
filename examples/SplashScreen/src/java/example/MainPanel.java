// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public static final String LOGGER_NAME = MethodHandles.lookup().lookupClass().getName();
  private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

  private MainPanel() {
    super(new BorderLayout());
    LOGGER.addHandler(new ConsoleHandler());
    try {
      Thread.sleep(5000);
    } catch (InterruptedException ex) {
      // Logger.getGlobal().severe(ex::getMessage);
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
    LOGGER.info(() -> "main start / EDT: " + EventQueue.isDispatchThread());
    createAndShowGui();
    LOGGER.info(() -> "main end");
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
    JWindow splashScreen = new JWindow();
    EventQueue.invokeLater(() -> openSplashScreen(splashScreen));
    LOGGER.info(() -> "createGUI start / EDT: " + EventQueue.isDispatchThread());
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel()); // new MainPanel() take long time
    frame.pack();
    frame.setLocationRelativeTo(null);
    LOGGER.info(() -> "createGUI end");
    EventQueue.invokeLater(() -> closeSplashScreen(splashScreen, frame));
  }

  private static void openSplashScreen(JWindow splashScreen) {
    LOGGER.info(() -> "splashScreen show start / EDT: " + EventQueue.isDispatchThread());
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
    LOGGER.info(() -> "splashScreen show end");
  }

  private static void closeSplashScreen(JWindow splashScreen, JFrame frame) {
    LOGGER.info(() -> "  splashScreen dispose start / EDT: " + EventQueue.isDispatchThread());
    // splashScreen.setVisible(false);
    splashScreen.dispose();
    LOGGER.info(() -> "  splashScreen dispose end");
    LOGGER.info(() -> "  frame show start / EDT: " + EventQueue.isDispatchThread());
    frame.setVisible(true);
    LOGGER.info(() -> "  frame show end");
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
//     } catch (UnsupportedLookAndFeelException ignored) {
//       Toolkit.getDefaultToolkit().beep();
//     } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
//       ex.printStackTrace();
//       return;
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
//         System.out.println("frame make start/EDT: " + EventQueue.isDispatchThread());
//         frame.getContentPane().add(new MainPanel()); // new MainPanel() take long time
//         System.out.println("frame make end");
//         return "Done";
//       }
//
//       @Override protected void done() {
//         System.out.println("splashScreen dispose start/EDT: " + EventQueue.isDispatchThread());
//         splashScreen.dispose();
//         System.out.println("splashScreen dispose end");
//         System.out.println("frame show start/EDT: " + EventQueue.isDispatchThread());
//         frame.pack();
//         frame.setLocationRelativeTo(null);
//         frame.setVisible(true);
//         System.out.println("frame show end");
//       }
//     }.execute();
//   }
// }
