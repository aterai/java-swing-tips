// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JProgressBar progress1 = createProgressBar();
    progress1.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
    progress1.setForeground(new Color(0xAA_FF_AA_AA, true));

    JProgressBar progress2 = createProgressBar();
    progress2.setStringPainted(true);
    progress2.setFont(progress2.getFont().deriveFont(24f));
    progress2.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

    String key = "Slider.clockwise";
    progress1.putClientProperty(key, true);
    progress2.putClientProperty(key, true);
    JCheckBox check = new JCheckBox("Clockwise", true);
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      for (JProgressBar bar : Arrays.asList(progress1, progress2)) {
        bar.putClientProperty(key, b);
        bar.repaint();
      }
    });

    JSlider slider = new JSlider();
    slider.putClientProperty("Slider.paintThumbArrowShape", true);
    progress1.setModel(slider.getModel());

    JButton button = new JButton("start");
    button.addActionListener(e -> start((JButton) e.getSource(), progress2));

    JPanel p = new JPanel(new GridLayout(1, 2));
    p.add(progress1);
    p.add(progress2);

    Box box = Box.createHorizontalBox();
    box.add(check);
    box.add(Box.createHorizontalGlue());
    box.add(button);

    add(slider, BorderLayout.NORTH);
    add(p);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JProgressBar createProgressBar() {
    return new JProgressBar() {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new ProgressCircleUI());
      }
    };
  }

  private static void start(JButton button, JProgressBar progress) {
    button.setEnabled(false);
    progress.setIndeterminate(true);
    SwingWorker<String, Void> worker = new BackgroundTask() {
      @Override protected void done() {
        button.setEnabled(true);
      }
    };
    worker.addPropertyChangeListener(new ProgressListener(progress));
    worker.execute();
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
    // Repaint the indeterminate spinner at about 60fps (default: 50ms)
    UIManager.put("ProgressBar.repaintInterval", 16);
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ProgressCircleUI extends BasicProgressBarUI {
  private static final double START_ANGLE = 90d; // 12 o'clock
  private final long startTime = System.currentTimeMillis();

  @Override public Dimension getPreferredSize(JComponent c) {
    Dimension d = super.getPreferredSize(c);
    int v = Math.max(d.width, d.height);
    d.setSize(v, v);
    return d;
  }

  @Override public void paint(Graphics g, JComponent c) {
    Rectangle rect = SwingUtilities.calculateInnerArea(progressBar, null);
    if (rect.isEmpty()) {
      return;
    }
    // Arc2D angles increase counterclockwise, so a negative extent draws clockwise
    Object o = progressBar.getClientProperty("Slider.clockwise");
    int dir = Boolean.TRUE.equals(o) ? -1 : 1;
    if (progressBar.isIndeterminate()) {
      // Draw the track and the stretching/contracting rotating arc
      long elapsed = System.currentTimeMillis() - startTime;
      ArcAngles arc = MaterialSpinner.computeArcAngles(elapsed);
      double start = START_ANGLE - dir * arc.getStartAngle();
      double extent = -dir * arc.getSweepAngle();
      paintProgressCircle(g, rect, start, extent);
    } else {
      // Draw the track and the circular sector
      double extent = dir * 360d * progressBar.getPercentComplete();
      paintProgressCircle(g, rect, START_ANGLE, extent);

      // Deal with possible text painting
      if (progressBar.isStringPainted()) {
        Insets ins = progressBar.getInsets();
        paintString(g, rect.x, rect.y, rect.width, rect.height, 0, ins);
      }
    }
  }

  // Repaint the whole circle on each animation frame instead of the bouncing box area
  @Override protected Rectangle getBox(Rectangle r) {
    return null;
  }

  private void paintProgressCircle(Graphics g, Rectangle rect, double start, double extent) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    double size = Math.min(rect.width, rect.height);
    double cx = rect.getCenterX();
    double cy = rect.getCenterY();
    double outerRadius = size * .5;
    double innerRadius = outerRadius * .5;
    double ox = cx - outerRadius;
    double oy = cy - outerRadius;
    double innerSize = innerRadius * 2d;
    Shape inner = new Ellipse2D.Double(
        cx - innerRadius, cy - innerRadius, innerSize, innerSize);
    Shape outer = new Ellipse2D.Double(
        ox, oy, size, size);
    Shape sector = new Arc2D.Double(
        ox, oy, size, size, start, extent, Arc2D.PIE);

    Area foreground = new Area(sector);
    Area background = new Area(outer);
    Area hole = new Area(inner);

    foreground.subtract(hole);
    background.subtract(hole);

    // Draw the track
    g2.setPaint(new Color(0xDD_DD_DD));
    g2.fill(background);

    // Draw the circular sector
    g2.setPaint(progressBar.getForeground());
    g2.fill(foreground);
    g2.dispose();
  }

  // https://ateraimemo.com/Swing/ProgressBarSelectionColor.html
  // @Override protected Color getSelectionForeground() {
  //   return Color.GREEN; // Not used in ProgressCircleUI
  // }

  // @Override protected Color getSelectionBackground() {
  //   return Color.RED; // a progress string color
  // }
}

// A value object that only holds the starting angle
// and sweep angle of the arc for one frame.
// record ArcAngles(float startAngle, float sweepAngle) {}
class ArcAngles {
  private final float startAngle;
  private final float sweepAngle;

  protected ArcAngles(float startAngle, float sweepAngle) {
    this.startAngle = startAngle;
    this.sweepAngle = sweepAngle;
  }

  public float getStartAngle() {
    return startAngle;
  }

  public float getSweepAngle() {
    return sweepAngle;
  }
}

// Angle calculations for the Material style spinner (stretch/contract/rotate)
// https://ateraimemo.com/Swing/CircularSpinner.html
final class MaterialSpinner {
  private static final float CYCLE_DURATION = 1332f;
  private static final float MAX_SWEEP = 270f;
  private static final float MIN_SWEEP = 15f;

  private MaterialSpinner() {
    /* Singleton */
  }

  // The returned angles rotate clockwise (negative values)
  public static ArcAngles computeArcAngles(long elapsedMillis) {
    float totalCycles = elapsedMillis / CYCLE_DURATION;
    int cycleIndex = (int) Math.floor(totalCycles);
    float t = totalCycles - cycleIndex;
    float sweepRange = MAX_SWEEP - MIN_SWEEP;
    // Keep the angles small to avoid losing float precision over time
    float cycleOffset = cycleIndex * sweepRange % 360f;

    float head;
    float tail;
    boolean isFirstHalf = t < .5f;
    if (isFirstHalf) {
      head = cycleOffset + MIN_SWEEP + (sweepRange * easeInOutCubic(t / .5f));
      tail = cycleOffset;
    } else {
      head = cycleOffset + MAX_SWEEP;
      tail = cycleOffset + (sweepRange * easeInOutCubic((t - .5f) / .5f));
    }

    float baseSpin = -360f * (elapsedMillis % 3000L / 3000f);
    float startAngle = baseSpin - tail;
    float sweepAngle = tail - head;
    return new ArcAngles(startAngle, sweepAngle);
  }

  private static float easeInOutCubic(float x) {
    return x < .5f ? 4f * x * x * x : 1f - (float) Math.pow(-2f * x + 2f, 3f) / 2f;
  }
}

class BackgroundTask extends SwingWorker<String, Void> {
  @Override protected String doInBackground() throws InterruptedException {
    // Keep the progress bar indeterminate while preparing
    Thread.sleep(3000);
    int current = 0;
    int lengthOfTask = 100;
    while (current <= lengthOfTask && !isCancelled()) {
      doSomething();
      setProgress(100 * current / lengthOfTask);
      current++;
    }
    return "Done";
  }

  protected void doSomething() throws InterruptedException {
    Thread.sleep(50);
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
