// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;

public final class MainPanel extends JPanel implements HierarchyListener {
  private transient SwingWorker<String, Void> worker;

  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("ProgressBar.cycleTime", 1000);
    UIManager.put("ProgressBar.repaintInterval", 10);
    BoundedRangeModel model = new DefaultBoundedRangeModel();
    JProgressBar progress1 = new JProgressBar(model) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new StripedProgressBarUI(true, true));
      }
    };

    JProgressBar progress2 = new JProgressBar(model) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new StripedProgressBarUI(true, false));
      }
    };

    JProgressBar progress3 = new JProgressBar(model) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new StripedProgressBarUI(false, true));
      }
    };

    JProgressBar progress4 = new JProgressBar(model) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new StripedProgressBarUI(false, false));
      }
    };

    List<JProgressBar> list = Arrays.asList(
        new JProgressBar(model), progress1, progress2, progress3, progress4);

    JPanel p = new JPanel(new GridLayout(5, 1));
    list.forEach(bar -> p.add(makePanel(bar)));

    JButton button = new JButton("Test start");
    button.addActionListener(e -> {
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
      }
      worker = new BackgroundTask();
      list.forEach(bar -> {
        bar.setIndeterminate(true);
        worker.addPropertyChangeListener(new ProgressListener(bar));
      });
      worker.execute();
    });

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(button);
    box.add(Box.createHorizontalStrut(5));

    addHierarchyListener(this);
    add(p);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void hierarchyChanged(HierarchyEvent e) {
    boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
    if (b && !e.getComponent().isDisplayable() && Objects.nonNull(worker)) {
      // System.out.println("DISPOSE_ON_CLOSE");
      worker.cancel(true);
      // worker = null;
    }
  }

  private static Component makePanel(Component cmp) {
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 1d;

    JPanel p = new JPanel(new GridBagLayout());
    p.add(cmp, c);
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class StripedProgressBarUI extends BasicProgressBarUI {
  private final boolean dir;
  private final boolean slope;

  protected StripedProgressBarUI(boolean dir, boolean slope) {
    super();
    this.dir = dir;
    this.slope = slope;
  }

  @Override protected int getBoxLength(int availableLength, int otherDimension) {
    return availableLength; // (int) Math.round(availableLength / 6d);
  }

  @Override public void paintIndeterminate(Graphics g, JComponent c) {
    // if (!(g instanceof Graphics2D)) {
    //   return;
    // }
    Rectangle barRect = SwingUtilities.calculateInnerArea(progressBar, null);
    if (barRect.isEmpty()) {
      return;
    }

    // Paint the striped box.
    boxRect = getBox(boxRect);
    if (Objects.nonNull(boxRect)) {
      int w = 10;
      int x = getAnimationIndex();
      Shape p = makeIndeterminateBox(w);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(progressBar.getForeground());
      if (slope) {
        for (int i = boxRect.width + x; i > -w; i -= w) {
          g2.fill(AffineTransform.getTranslateInstance(i, 0).createTransformedShape(p));
        }
      } else {
        for (int i = -x; i < boxRect.width; i += w) {
          g2.fill(AffineTransform.getTranslateInstance(i, 0).createTransformedShape(p));
        }
      }
      g2.dispose();
    }
  }

  private Shape makeIndeterminateBox(int w) {
    GeneralPath p = new GeneralPath();
    if (dir) {
      p.moveTo(boxRect.x, boxRect.y);
      p.lineTo(boxRect.x + w * .5f, boxRect.getMaxY());
      p.lineTo(boxRect.x + (float) w, boxRect.getMaxY());
    } else {
      p.moveTo(boxRect.x, boxRect.getMaxY());
      p.lineTo(boxRect.x + w * .5f, boxRect.getMaxY());
      p.lineTo(boxRect.x + (float) w, boxRect.y);
    }
    p.lineTo(boxRect.x + w * .5f, boxRect.y);
    p.closePath();
    return p;
  }
}

class BackgroundTask extends SwingWorker<String, Void> {
  private final Random rnd = new Random();

  @Override protected String doInBackground() throws InterruptedException {
    Thread.sleep(5000);
    int current = 0;
    int lengthOfTask = 100;
    int total = 0;
    while (current <= lengthOfTask && !isCancelled()) {
      total += doSomething();
      setProgress(100 * current++ / lengthOfTask);
    }
    return String.format("Done(%dms)", total);
  }

  protected int doSomething() throws InterruptedException {
    int iv = rnd.nextInt(50) + 1;
    Thread.sleep(iv);
    return iv;
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
