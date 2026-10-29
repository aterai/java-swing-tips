// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;
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
    slider.putClientProperty("Slider.paintThumbArrowShape", true);
    progress1.setModel(slider.getModel());

    JButton button = new JButton("start");
    button.addActionListener(e -> {
      int lengthOfTask = progress2.getMaximum() - progress2.getMinimum();
      SwingWorker<?, ?> worker = createWorker(button, lengthOfTask);
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

  private static SwingWorker<?, ?> createWorker(JButton button, int lengthOfTask) {
    button.setEnabled(false);
    return new BackgroundTask(lengthOfTask) {
      @Override protected void done() {
        if (button.isDisplayable()) {
          button.setEnabled(true);
        }
      }
    };
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

class SolidGaugeUI extends BasicProgressBarUI {
  private final int[] palette;
  private final double extent;

  protected SolidGaugeUI(int range, double extent) {
    super();
    this.palette = createGradientPalette(range);
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
    double outerRadius = Math.min(rect.width, rect.height);
    double innerRadius = outerRadius * .6;
    double cx = rect.getCenterX();
    double cy = rect.getMaxY();
    // Arc2D angles are counterclockwise from 3 o'clock,
    // so the gauge starts at the upper left and sweeps clockwise (negative extent)
    double start = 90d + extent * .5;
    double percent = progressBar.getPercentComplete();
    double degree = extent * percent;
    Area background = createArc(cx, cy, outerRadius, start, -extent);
    Area foreground = createArc(cx, cy, outerRadius, start, -degree);
    Area hole = createArc(cx, cy, innerRadius, start, -extent);
    background.subtract(hole);
    foreground.subtract(hole);

    // Draw the track
    g2.setPaint(new Color(0xDD_DD_DD));
    g2.fill(background);

    // Draw the circular sector
    g2.setPaint(getColorFromPalette(palette, percent));
    g2.fill(foreground);

    // Draw minimum, maximum
    g2.setPaint(progressBar.getForeground());
    drawMinMaxString(g2, cx, cy, outerRadius);

    // Deal with possible text painting
    if (progressBar.isStringPainted()) {
      drawProgressString(g2, (float) cx, (float) cy);
    }
    g2.dispose();
  }

  private static Area createArc(double cx, double cy, double r, double start, double extent) {
    double d = r * 2d;
    return new Area(new Arc2D.Double(cx - r, cy - r, d, d, start, extent, Arc2D.PIE));
  }

  private void drawMinMaxString(Graphics2D g2, double cx, double cy, double r) {
    String min = Objects.toString(progressBar.getMinimum());
    String max = Objects.toString(progressBar.getMaximum());
    double gap = 4d;
    // Right-align the minimum label to the left end of the gauge
    double minX = cx - r - gap - g2.getFontMetrics().stringWidth(min);
    double maxX = cx + r + gap;
    g2.drawString(min, (float) minX, (float) cy);
    g2.drawString(max, (float) maxX, (float) cy);
  }

  private void drawProgressString(Graphics2D g2, float x, float y) {
    Font font = progressBar.getFont();
    float fontSize = font.getSize2D();
    float baseline = y - fontSize;
    String value = Integer.toString(progressBar.getValue());
    float valueX = x - g2.getFontMetrics().stringWidth(value) * .5f;
    g2.drawString(value, valueX, baseline);
    // Draw the unit on the next line in a font 2/3 the size of the value
    float unitSize = fontSize * 2f / 3f;
    g2.setFont(font.deriveFont(unitSize));
    String unit = "㎞/h";
    float unitX = x - g2.getFontMetrics().stringWidth(unit) * .5f;
    g2.drawString(unit, unitX, baseline + unitSize);
  }

  private static int[] createGradientPalette(int range) {
    BufferedImage image = new BufferedImage(range, 1, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();
    Point2D start = new Point2D.Float();
    Point2D end = new Point2D.Float(range - 1f, 0f);
    float[] dist = {0f, .8f, .9f, 1f};
    Color[] colors = {Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED};
    g2.setPaint(new LinearGradientPaint(start, end, dist, colors));
    g2.fillRect(0, 0, range, 1);
    g2.dispose();
    // BufferedImage#getRGB(...) reads the pixels directly,
    // so PixelGrabber and its InterruptedException handling are unnecessary
    return image.getRGB(0, 0, range, 1, null, 0, range);
  }

  private static Color getColorFromPalette(int[] palette, double pos) {
    if (pos < 0d || pos > 1d) {
      throw new IllegalArgumentException("Parameter outside of expected range");
    }
    // pos == 1d gives palette.length, so clamp it to the last index
    int index = Math.min((int) (palette.length * pos), palette.length - 1);
    // Color(int) ignores the alpha bits of the TYPE_INT_RGB pixel
    return new Color(palette[index]);
  }

  // @Override protected Color getSelectionBackground() {
  //   return new Color(0xAA_75_FF_3C, true); // a progress string color
  // }
}

class BackgroundTask extends SwingWorker<String, Void> {
  private final Random rnd = new Random();
  private final int lengthOfTask;

  protected BackgroundTask(int lengthOfTask) {
    super();
    this.lengthOfTask = lengthOfTask;
  }

  @Override protected String doInBackground() throws InterruptedException {
    int current = 0;
    int total = 0;
    while (current <= lengthOfTask && !isCancelled()) {
      total += doSomething();
      setProgress(100 * current / lengthOfTask);
      current += 1;
    }
    return String.format("Done(%dms)", total);
  }

  protected int doSomething() throws InterruptedException {
    int iv = rnd.nextInt(10) + 1;
    Thread.sleep(iv);
    return iv;
  }
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
