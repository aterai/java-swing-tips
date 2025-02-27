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
    new SwingWorker<Void, Void>() {
      @Override protected Void doInBackground() throws InterruptedException {
        Thread.sleep(3000);
        return null;
      }
    }.execute();

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
    // System.out.println("main start / EDT: " + EventQueue.isDispatchThread());
    createAndShowGui();
    // System.out.println("main end");
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
    JDialog splashScreen = new JDialog(frame, Dialog.ModalityType.DOCUMENT_MODAL);
    JProgressBar progress = new JProgressBar();

    // System.out.println(splashScreen.getModalityType());

    EventQueue.invokeLater(() -> {
      String path = "example/splash.png";
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Image img = Optional.ofNullable(cl.getResource(path)).map(url -> {
        try (InputStream s = url.openStream()) {
          return ImageIO.read(s);
        } catch (IOException ex) {
          return makeMissingImage();
        }
      }).orElseGet(MainPanel::makeMissingImage);
      splashScreen.setUndecorated(true);
      splashScreen.getContentPane().add(new JLabel(new ImageIcon(img)));
      splashScreen.getContentPane().add(progress, BorderLayout.SOUTH);
      splashScreen.pack();
      splashScreen.setLocationRelativeTo(null);
      splashScreen.setVisible(true);
    });
    SwingWorker<Void, Void> worker = new BackgroundTask() {
      @Override protected void done() {
        splashScreen.dispose();
      }
    };
    worker.addPropertyChangeListener(e -> {
      boolean isProgress = "progress".equals(e.getPropertyName());
      if (isProgress) {
        progress.setValue((Integer) e.getNewValue());
      }
    });
    worker.execute();

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    EventQueue.invokeLater(() -> frame.setVisible(true));
  }
}

class BackgroundTask extends SwingWorker<Void, Void> {
  @Override protected Void doInBackground() throws InterruptedException {
    int current = 0;
    int lengthOfTask = 120;
    while (current < lengthOfTask && !isCancelled()) {
      doSomething(100 * current++ / lengthOfTask);
    }
    return null;
  }

  protected void doSomething(int progress) throws InterruptedException {
    Thread.sleep(50);
    setProgress(progress);
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;
    g2.setColor(Color.LIGHT_GRAY);
    g2.translate(x, y);
    g2.fillRect(0, 0, w, h);
    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(gap, gap, w - gap, h - gap);
    g2.drawLine(gap, h - gap, w - gap, gap);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 240;
  }

  @Override public int getIconHeight() {
    return 160;
  }
}
