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
    JProgressBar progressBar = new JProgressBar() {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new GradientPalletProgressBarUI());
        setOpaque(false);
      }
    };

    JButton button = new JButton("Start");
    button.addActionListener(e -> {
      JButton b = (JButton) e.getSource();
      b.setEnabled(false);
      SwingWorker<Void, Void> worker = new BackgroundTask() {
        @Override protected void done() {
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
    Point2D end = new Point2D.Float(99f, 0f);
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
      // ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
      Thread.currentThread().interrupt();
    }
    return pallet;
  }

  private static Color getColorFromPallet(int[] pallet, float pos) {
    if (pos < 0f || pos > 1f) {
      throw new IllegalArgumentException("Parameter outside of expected range");
    }
    int i = (int) (pallet.length * pos);
    int max = pallet.length - 1;
    int index = Math.min(Math.max(i, 0), max);
    return new Color(pallet[index] & 0x00_FF_FF_FF);
    // translucent
    // int pix = pallet[index] & 0x00_FF_FF_FF | (0x64 << 24);
    // return new Color(pix), true);
  }

  @Override public void paintDeterminate(Graphics g, JComponent c) {
    Insets b = progressBar.getInsets(); // area for border
    // int barRectWidth = progressBar.getWidth() - b.right - b.left;
    // int barRectHeight = progressBar.getHeight() - b.top - b.bottom;
    // if (barRectWidth <= 0 || barRectHeight <= 0) {
    //   return;
    // }
    Rectangle r = SwingUtilities.calculateInnerArea(progressBar, null);
    if (r.isEmpty()) {
      return;
    }
    // int cellLength = getCellLength();
    // int cellSpacing = getCellSpacing();
    // amount of progress to draw
    int amountFull = getAmountFull(b, r.width, r.height);

    // draw the cells
    if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
      float x = amountFull / (float) r.width;
      g.setColor(getColorFromPallet(pallet, x));
      g.fillRect(r.x, r.y, amountFull, r.height);
    } else { // VERTICAL
      float y = amountFull / (float) r.height;
      g.setColor(getColorFromPallet(pallet, y));
      // g.fillRect(b.left, barRectHeight + b.bottom - amountFull, r.width, amountFull);
      g.fillRect(r.x, r.y + r.height - amountFull, r.width, amountFull);
    }

    // Deal with possible text painting
    if (progressBar.isStringPainted()) {
      paintString(g, r.x, r.y, r.width, r.height, amountFull, b);
    }
  }
}
