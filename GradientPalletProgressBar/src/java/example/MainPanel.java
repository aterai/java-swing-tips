// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JPanel p = new JPanel(new GridLayout(3, 1));
    p.add(makeUI());
    p.add(makeUI());
    p.add(makeUI());
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeUI() {
    JProgressBar progressBar = new JProgressBar();
    progressBar.setOpaque(false);
    progressBar.setUI(new GradientPalletProgressBarUI());

    JButton button = new JButton("Start");
    button.addActionListener(e -> {
      JButton b = (JButton) e.getSource();
      b.setEnabled(false);
      SwingWorker<Void, Void> worker = new BackgroundTask() {
        @Override public void done() {
          if (b.isDisplayable()) {
            b.setEnabled(true);
          }
        }
      };
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

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class BackgroundTask extends SwingWorker<Void, Void> {
  @Override public Void doInBackground() {
    int current = 0;
    int lengthOfTask = 100;
    while (current <= lengthOfTask && !isCancelled()) {
      try { // dummy task
        Thread.sleep(50);
      } catch (InterruptedException ex) {
        return null;
      }
      setProgress(100 * current / lengthOfTask);
      current++;
    }
    return null;
  }
}

class ProgressListener implements PropertyChangeListener {
  private final JProgressBar progressBar;

  protected ProgressListener(JProgressBar progressBar) {
    this.progressBar = progressBar;
    this.progressBar.setValue(0);
  }

  @Override public void propertyChange(PropertyChangeEvent e) {
    String strPropertyName = e.getPropertyName();
    if ("progress".equals(strPropertyName)) {
      progressBar.setIndeterminate(false);
      int progress = (Integer) e.getNewValue();
      progressBar.setValue(progress);
    }
  }
}

class GradientPalletProgressBarUI extends BasicProgressBarUI {
  private final int[] pallet;

  protected GradientPalletProgressBarUI() {
    super();
    this.pallet = makeGradientPallet();
  }

  private static int[] makeGradientPallet() {
    BufferedImage image = new BufferedImage(100, 1, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();
    Point2D start = new Point2D.Float();
    Point2D end = new Point2D.Float(99, 0);
    float[] dist = {0f, .5f, 1f};
    Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN};
    g2.setPaint(new LinearGradientPaint(start, end, dist, colors));
    g2.fillRect(0, 0, 100, 1);
    g2.dispose();

    int width = image.getWidth(null);
    int[] pallet = new int[width];
    PixelGrabber pg = new PixelGrabber(image, 0, 0, width, 1, pallet, 0, width);
    try {
      pg.grabPixels();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    return pallet;
  }

  private static Color getColorFromPallet(int[] pallet, float x) {
    if (x < 0f || x > 1f) {
      throw new IllegalArgumentException("Parameter outside of expected range");
    }
    int i = (int) (pallet.length * x);
    int max = pallet.length - 1;
    int index = Math.min(Math.max(i, 0), max);
    return new Color(pallet[index] & 0x00_FF_FF_FF);
    // translucent
    // int pix = pallet[index] & 0x00_FF_FF_FF | (0x64 << 24);
    // return new Color(pix), true);
  }

  @Override public void paintDeterminate(Graphics g, JComponent c) {
    Insets b = progressBar.getInsets(); // area for border
    int barRectWidth = progressBar.getWidth() - b.right - b.left;
    int barRectHeight = progressBar.getHeight() - b.top - b.bottom;
    if (barRectWidth <= 0 || barRectHeight <= 0) {
      return;
    }
    // int cellLength = getCellLength();
    // int cellSpacing = getCellSpacing();
    // amount of progress to draw
    int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

    // draw the cells
    if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
      float x = amountFull / (float) barRectWidth;
      g.setColor(getColorFromPallet(pallet, x));
      g.fillRect(b.left, b.top, amountFull, barRectHeight);
    } else { // VERTICAL
      float y = amountFull / (float) barRectHeight;
      g.setColor(getColorFromPallet(pallet, y));
      g.fillRect(b.left, barRectHeight + b.bottom - amountFull, barRectWidth, amountFull);
    }
    // Deal with possible text painting
    if (progressBar.isStringPainted()) {
      paintString(g, b.left, b.top, barRectWidth, barRectHeight, amountFull, b);
    }
  }
}
