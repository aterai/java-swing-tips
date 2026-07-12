// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicProgressBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JProgressBar progress = new JProgressBar() {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new ProgressCircleUI());
        putClientProperty("Slider.clockwise", true);
      }

      // Show the eased percentage instead of the raw model value, so the
      // text stays in sync with the animated arc.
      @Override public String getString() {
        return Optional.ofNullable(getUI())
            .filter(AbstractEaseOutProgressBarUI.class::isInstance)
            .map(AbstractEaseOutProgressBarUI.class::cast)
            .map(AbstractEaseOutProgressBarUI::getAnimatedFraction)
            .map(fraction -> Math.round(fraction * 100d) + "%")
            .orElseGet(super::getString);
        // ProgressBarUI progressBarUI = getUI();
        // return progressBarUI instanceof AbstractEaseOutProgressBarUI it
        //     ? Math.round(it.getAnimatedFraction() * 100d) + "%" : super.getString();
      }
    };
    progress.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
    progress.setFont(getFont().deriveFont(24f));
    progress.setStringPainted(true);

    JProgressBar progress2 = new JProgressBar() {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new EaseOutProgressBarUI());
      }
    };

    JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
    progress.setModel(slider.getModel());
    progress2.setModel(slider.getModel());

    JPanel p = new JPanel();
    p.add(progress);

    add(slider, BorderLayout.NORTH);
    add(p);
    add(progress2, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
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

// Common ease-out animation plumbing shared by every eased BasicProgressBarUI
// variant in this demo. Subclasses only need to supply how the eased value
// is rendered (a circular sector, a horizontal fill amount, etc.).
abstract class AbstractEaseOutProgressBarUI extends BasicProgressBarUI {
  protected static final double COMPLETE = 1d;
  private static final int DURATION_MS = 400;
  private static final int FRAME_INTERVAL_MS = 1000 / 60;
  private static final double EPSILON = 1e-6;

  // The "apparent" percentage of progress during the animation (0.0-1.0).
  // Stored separately from actual model values. Only ever accessed on the EDT
  // (installUI, the ChangeListener, SwingWorker#process/#done and paint all
  // run on the EDT; doInBackground only touches the local from/target copies).
  private double animatedFraction;
  private SwingWorker<Void, Double> animator;
  private ChangeListener changeHandler;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    animatedFraction = progressBar.getPercentComplete();
  }

  @Override protected void installListeners() {
    super.installListeners();
    changeHandler = e -> startAnimation(progressBar.getPercentComplete());
    progressBar.addChangeListener(changeHandler);
  }

  @Override protected void uninstallListeners() {
    progressBar.removeChangeListener(changeHandler);
    cancelAnimation();
    super.uninstallListeners();
  }

  private void cancelAnimation() {
    if (animator != null && !animator.isDone()) {
      animator.cancel(true);
    }
  }

  private void startAnimation(double target) {
    cancelAnimation();
    double from = animatedFraction;
    if (Math.abs(target - from) < EPSILON) {
      return;
    }
    animator = new AnimationWorker(from, target);
    animator.execute();
  }

  private static double easeOutCubic(double t) {
    double u = COMPLETE - t;
    return COMPLETE - u * u * u;
  }

  // Exposes the eased value so subclasses (and JProgressBar#getString()
  // overrides in MainPanel) can render "NN%" text / fills in sync with the
  // animation.
  public double getAnimatedFraction() {
    return animatedFraction;
  }

  private final class AnimationWorker extends SwingWorker<Void, Double> {
    private final double from;
    private final double target;

    private AnimationWorker(double from, double target) {
      super();
      this.from = from;
      this.target = target;
    }

    @Override protected Void doInBackground() throws InterruptedException {
      long startTime = System.currentTimeMillis();
      while (!isCancelled()) {
        long time = System.currentTimeMillis() - startTime;
        double t = Math.min(COMPLETE, time / (double) DURATION_MS);
        publish(from + (target - from) * easeOutCubic(t));
        if (t >= COMPLETE) {
          break;
        }
        sleep();
      }
      return null;
    }

    private void sleep() throws InterruptedException {
      Thread.sleep(FRAME_INTERVAL_MS);
    }

    @Override protected void process(List<Double> chunks) {
      // It is enough to reflect only the latest value
      animatedFraction = chunks.get(chunks.size() - 1);
      // Java 21: animatedFraction = chunks.getLast();
      progressBar.repaint();
    }

    @Override protected void done() {
      if (!isCancelled()) {
        animatedFraction = target;
        progressBar.repaint();
      }
    }
  }
}

class ProgressCircleUI extends AbstractEaseOutProgressBarUI {
  @Override public Dimension getPreferredSize(JComponent c) {
    Dimension d = super.getPreferredSize(c);
    int v = Math.max(d.width, d.height);
    d.setSize(v, v);
    return d;
  }

  @Override public void paint(Graphics g, JComponent c) {
    Rectangle rect = SwingUtilities.calculateInnerArea(progressBar, null);
    if (!rect.isEmpty()) {
      // Draw the track and the circular sector
      paintProgressCircle(g, rect);

      // Deal with possible text painting
      if (progressBar.isStringPainted()) {
        Insets ins = progressBar.getInsets();
        paintString(g, rect.x, rect.y, rect.width, rect.height, 0, ins);
      }
    }
  }

  private void paintProgressCircle(Graphics g, Rectangle rect) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Object o = progressBar.getClientProperty("Slider.clockwise");
    int dir = Objects.equals(o, true) ? -1 : 1;
    double sz = Math.min(rect.width, rect.height);
    double cx = rect.getCenterX();
    double cy = rect.getCenterY();
    double or = sz * .5;
    double ir = or * .5; // .8;
    double start = 90d;

    // Draw using apparent proportions during animation instead of actual values
    double degree = dir * 360d * getAnimatedFraction();
    Shape inner = new Ellipse2D.Double(cx - ir, cy - ir, ir * 2d, ir * 2d);
    Shape outer = new Ellipse2D.Double(cx - or, cy - or, sz, sz);
    Shape sector = new Arc2D.Double(cx - or, cy - or, sz, sz, start, degree, Arc2D.PIE);

    Area foreground = new Area(sector);
    Area background = new Area(outer);
    Area hole = new Area(inner);

    foreground.subtract(hole);
    background.subtract(hole);

    // Draw the track
    g2.setPaint(new Color(0xDD_DD_DD));
    g2.fill(background);

    g2.setPaint(progressBar.getForeground());
    g2.fill(foreground);
    g2.dispose();
  }
}

class EaseOutProgressBarUI extends AbstractEaseOutProgressBarUI {
  @Override protected int getAmountFull(Insets b, int width, int height) {
    int amountFull = 0;
    BoundedRangeModel model = progressBar.getModel();
    if ((model.getMaximum() - model.getMinimum()) != 0) {
      // double percentComplete = progressBar.getPercentComplete();
      double percentComplete = getAnimatedFraction();
      if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
        amountFull = (int) Math.round(width * percentComplete);
      } else {
        amountFull = (int) Math.round(height * percentComplete);
      }
    }
    return amountFull;
  }
}
