// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JProgressBar progress1 = new JProgressBar(0, 200) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new SolidGaugeUI(getMaximum() - getMinimum(), 180d));
      }
    };

    JProgressBar progress2 = new JProgressBar(0, 200) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new SolidGaugeUI(getMaximum() - getMinimum(), 160d));
      }
    };

    Stream.of(progress1, progress2).forEach(p -> {
      p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      p.setFont(p.getFont().deriveFont(18f));
      p.setStringPainted(true);
      // p.addChangeListener(e -> p.setString(getString(p)));
      // p.setString(getString(p));
    });

    JSlider slider = new JSlider(0, 200, 0);
    slider.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);
    progress1.setModel(slider.getModel());

    JButton button = new JButton("start");
    button.addActionListener(e -> {
      JButton b = (JButton) e.getSource();
      b.setEnabled(false);
      int lengthOfTask = progress2.getMaximum() - progress2.getMinimum();
      SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
        private final Random rnd = new Random();

        @Override protected String doInBackground() throws InterruptedException {
          int current = 0;
          int total = 0;
          while (current <= lengthOfTask && !isCancelled()) {
            total += doSomething();
            setProgress(100 * current++ / lengthOfTask);
          }
          return String.format("Done(%dms)", total);
        }

        @Override protected void done() {
          if (b.isDisplayable()) {
            b.setEnabled(true);
          }
        }

        protected int doSomething() throws InterruptedException {
          int iv = rnd.nextInt(10) + 1;
          Thread.sleep(iv);
          return iv;
        }
      };
      worker.addPropertyChangeListener(new ProgressListener(progress2));
      worker.execute();
    });

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(progress1);
    p.add(progress2);

    add(slider, BorderLayout.NORTH);
    add(p);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  // private static String getString(JProgressBar progress) {
  //   return String.format("%dkm/h", progress.getValue());
  // }

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

class SolidGaugeUI extends BasicProgressBarUI {
  private final int[] pallet;
  private final double extent;

  protected SolidGaugeUI(int range, double extent) {
    super();
    this.pallet = makeGradientPallet(range);
    this.extent = extent;
  }

  @Override public void paint(Graphics g, JComponent c) {
    Rectangle rect = SwingUtilities.calculateInnerArea(progressBar, null);
    if (rect.isEmpty()) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // double extent = -150d;
    double start = 90d + extent * .5;
    double degree = extent * progressBar.getPercentComplete();
    double or = Math.min(rect.width, rect.height);
    double cx = rect.getCenterX();
    double cy = rect.getMaxY();
    double sz = or * 2d;
    double ir = or * .6;
    Shape inner = new Arc2D.Double(cx - ir, cy - ir, ir * 2d, ir * 2d, start, -extent, Arc2D.PIE);
    Shape outer = new Arc2D.Double(cx - or, cy - or, sz, sz, start, -extent, Arc2D.PIE);
    Shape sector = new Arc2D.Double(cx - or, cy - or, sz, sz, start, -degree, Arc2D.PIE);

    Area foreground = new Area(sector);
    Area background = new Area(outer);
    Area hole = new Area(inner);

    foreground.subtract(hole);
    background.subtract(hole);

    // Draw the track
    g2.setPaint(new Color(0xDD_DD_DD));
    g2.fill(background);

    // Draw the circular sector
    g2.setPaint(getColorFromPallet(pallet, progressBar.getPercentComplete()));
    g2.fill(foreground);

    // Draw minimum, maximum
    Font font = progressBar.getFont();
    float fsz = font.getSize2D();
    float min = (float) (cx - or - fsz);
    float max = (float) (cx + or + 4d);
    g2.setPaint(progressBar.getForeground());
    g2.drawString(Objects.toString(progressBar.getMinimum()), min, (float) cy);
    g2.drawString(Objects.toString(progressBar.getMaximum()), max, (float) cy);

    // Deal with possible text painting
    if (progressBar.isStringPainted()) {
      float h = (float) cy - fsz;
      String str = String.format("%d", progressBar.getValue());
      float vx = (float) cx - g2.getFontMetrics().stringWidth(str) * .5f;
      g2.drawString(str, vx, h);
      float ksz = fsz * 2f / 3f;
      g2.setFont(font.deriveFont(ksz));
      String kmh = "㎞/h";
      float tx = (float) cx - g2.getFontMetrics().stringWidth(kmh) * .5f;
      g2.drawString(kmh, tx, h + ksz);
    }
    g2.dispose();
  }

  private static int[] makeGradientPallet(int range) {
    BufferedImage image = new BufferedImage(range, 1, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();
    Point2D start = new Point2D.Float();
    Point2D end = new Point2D.Float(range - 1f, 0f);
    float[] dist = {0f, .8f, .9f, 1f};
    Color[] colors = {Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED};
    g2.setPaint(new LinearGradientPaint(start, end, dist, colors));
    g2.fillRect(0, 0, range, 1);
    g2.dispose();

    int width = image.getWidth(null);
    int[] pallet = new int[width];
    PixelGrabber pg = new PixelGrabber(image, 0, 0, width, 1, pallet, 0, width);
    try {
      pg.grabPixels();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
      Thread.currentThread().interrupt();
    }
    return pallet;
  }

  private static Color getColorFromPallet(int[] pallet, double pos) {
    if (pos < 0d || pos > 1d) {
      throw new IllegalArgumentException("Parameter outside of expected range");
    }
    int i = (int) (pallet.length * pos);
    int max = pallet.length - 1;
    int index = Math.min(Math.max(i, 0), max);
    return new Color(pallet[index] & 0x00_FF_FF_FF);
  }

  // @Override protected Color getSelectionBackground() {
  //   return new Color(0xAA_75_FF_3C, true); // a progress string color
  // }
}

class ProgressListener implements PropertyChangeListener {
  private final JProgressBar progressBar;

  protected ProgressListener(JProgressBar progressBar) {
    this.progressBar = progressBar;
    this.progressBar.setValue(progressBar.getMinimum());
  }

  @Override public void propertyChange(PropertyChangeEvent e) {
    boolean isProgress = "progress".equals(e.getPropertyName());
    if (isProgress) {
      progressBar.setIndeterminate(false);
      int range = progressBar.getMaximum() - progressBar.getMinimum();
      int iv = (int) (range * .01 * (int) e.getNewValue());
      progressBar.setValue(iv);
    }
  }
}
