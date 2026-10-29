// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(3, 1));
    p.add(createProgressBarBox());
    p.add(createProgressBarBox());
    p.add(createProgressBarBox());
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component createProgressBarBox() {
    JProgressBar progressBar = new JProgressBar() {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new GradientPaletteProgressBarUI());
        setOpaque(false);
      }
    };

    JButton button = new JButton("Start");
    button.addActionListener(e -> {
      JButton b = (JButton) e.getSource();
      b.setEnabled(false);
      SwingWorker<?, ?> worker = createTask(b);
      worker.addPropertyChangeListener(new ProgressListener(progressBar));
      worker.execute();
    });

    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createEmptyBorder(32, 8, 0, 8));
    GridBagConstraints c = new GridBagConstraints();

    c.insets = new Insets(0, 0, 0, 4);
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    p.add(progressBar, c);

    c.weightx = 0d;
    p.add(button, c);
    return p;
  }

  private static SwingWorker<?, ?> createTask(JButton b) {
    return new BackgroundTask() {
      @Override protected void done() {
        if (b.isDisplayable()) {
          b.setEnabled(true);
        }
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
}

class BackgroundTask extends SwingWorker<Void, Void> {
  @Override protected Void doInBackground() throws InterruptedException {
    int current = 0;
    int lengthOfTask = 100;
    while (current <= lengthOfTask && !isCancelled()) {
      doSomething(100 * current / lengthOfTask);
      current++;
    }
    return null;
  }

  protected void doSomething(int progress) throws InterruptedException {
    Thread.sleep(50);
    setProgress(progress);
  }
}

class ProgressListener implements PropertyChangeListener {
  private final JProgressBar progressBar;

  protected ProgressListener(JProgressBar progressBar) {
    this.progressBar = progressBar;
    this.progressBar.setValue(0);
  }

  @Override public void propertyChange(PropertyChangeEvent e) {
    boolean isProgress = "progress".equals(e.getPropertyName());
    if (isProgress) {
      progressBar.setIndeterminate(false);
      int progress = (Integer) e.getNewValue();
      progressBar.setValue(progress);
    }
  }
}

class GradientPaletteProgressBarUI extends BasicProgressBarUI {
  private final int[] palette = createGradientPalette();

  private static int[] createGradientPalette() {
    int width = 100;
    BufferedImage image = new BufferedImage(width, 1, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();
    Point2D start = new Point2D.Float();
    Point2D end = new Point2D.Float(width - 1f, 0f);
    float[] dist = {0f, .5f, 1f};
    Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN};
    g2.setPaint(new LinearGradientPaint(start, end, dist, colors));
    g2.fillRect(0, 0, width, 1);
    g2.dispose();
    // BufferedImage#getRGB(...) reads the pixels directly,
    // so PixelGrabber and its InterruptedException handling are unnecessary
    return image.getRGB(0, 0, width, 1, null, 0, width);
  }

  private static Color getColorFromPalette(int[] palette, float pos) {
    if (pos < 0f || pos > 1f) {
      throw new IllegalArgumentException("Parameter outside of expected range");
    }
    // pos == 1f gives palette.length, so clamp it to the last index
    int index = Math.min((int) (palette.length * pos), palette.length - 1);
    // Color(int) ignores the alpha bits of the TYPE_INT_RGB pixel
    return new Color(palette[index]);
    // translucent
    // int pix = palette[index] & 0x00_FF_FF_FF | (0x64 << 24);
    // return new Color(pix, true);
  }

  @Override public void paintDeterminate(Graphics g, JComponent c) {
    Rectangle r = SwingUtilities.calculateInnerArea(progressBar, null);
    if (r != null && !r.isEmpty()) {
      Insets b = progressBar.getInsets(); // area for border
      // amount of progress to draw
      int amountFull = getAmountFull(b, r.width, r.height);
      if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
        g.setColor(getColorFromPalette(palette, amountFull / (float) r.width));
        // fill from the right edge in a right-to-left orientation,
        // same as BasicProgressBarUI
        boolean ltr = progressBar.getComponentOrientation().isLeftToRight();
        int x = ltr ? r.x : r.x + r.width - amountFull;
        g.fillRect(x, r.y, amountFull, r.height);
      } else { // VERTICAL
        g.setColor(getColorFromPalette(palette, amountFull / (float) r.height));
        g.fillRect(r.x, r.y + r.height - amountFull, r.width, amountFull);
      }
      // Deal with possible text painting
      if (progressBar.isStringPainted()) {
        paintString(g, r.x, r.y, r.width, r.height, amountFull, b);
      }
    }
  }
}
