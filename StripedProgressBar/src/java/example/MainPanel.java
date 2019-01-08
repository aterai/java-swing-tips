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
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;

public final class MainPanel extends JPanel implements HierarchyListener {
  private transient SwingWorker<String, Void> worker;

  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("ProgressBar.cycleTime", 1000);
    UIManager.put("ProgressBar.repaintInterval", 10);

    BoundedRangeModel model = new DefaultBoundedRangeModel();
    JProgressBar progressBar1 = new JProgressBar(model);
    progressBar1.setUI(new StripedProgressBarUI(true, true));

    JProgressBar progressBar2 = new JProgressBar(model);
    progressBar2.setUI(new StripedProgressBarUI(true, false));

    JProgressBar progressBar3 = new JProgressBar(model);
    progressBar3.setUI(new StripedProgressBarUI(false, true));

    JProgressBar progressBar4 = new JProgressBar(model);
    progressBar4.setUI(new StripedProgressBarUI(false, false));

    List<JProgressBar> list = Arrays.asList(new JProgressBar(model), progressBar1, progressBar2, progressBar3, progressBar4);

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
    boolean isDisplayableChanged = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
    if (isDisplayableChanged && !e.getComponent().isDisplayable() && Objects.nonNull(worker)) {
      System.out.println("DISPOSE_ON_CLOSE");
      worker.cancel(true);
      worker = null;
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
  protected final boolean dir;
  protected final boolean slope;

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

    Insets b = progressBar.getInsets(); // area for border
    int barRectWidth = progressBar.getWidth() - b.right - b.left;
    int barRectHeight = progressBar.getHeight() - b.top - b.bottom;

    if (barRectWidth <= 0 || barRectHeight <= 0) {
      return;
    }

    // Paint the striped box.
    boxRect = getBox(boxRect);
    if (Objects.nonNull(boxRect)) {
      int w = 10;
      int x = getAnimationIndex();
      GeneralPath p = new GeneralPath();
      if (dir) {
        p.moveTo(boxRect.x, boxRect.y);
        p.lineTo(boxRect.x + w * .5f, boxRect.y + boxRect.height);
        p.lineTo(boxRect.x + w, boxRect.y + boxRect.height);
        p.lineTo(boxRect.x + w * .5f, boxRect.y);
      } else {
        p.moveTo(boxRect.x, boxRect.y + boxRect.height);
        p.lineTo(boxRect.x + w * .5f, boxRect.y + boxRect.height);
        p.lineTo(boxRect.x + w, boxRect.y);
        p.lineTo(boxRect.x + w * .5f, boxRect.y);
      }
      p.closePath();

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
}

class BackgroundTask extends SwingWorker<String, Void> {
  @Override public String doInBackground() {
    try { // dummy task
      Thread.sleep(5000);
    } catch (InterruptedException ex) {
      return "Interrupted";
    }
    int current = 0;
    int lengthOfTask = 100;
    while (current <= lengthOfTask && !isCancelled()) {
      try { // dummy task
        Thread.sleep(50);
      } catch (InterruptedException ex) {
        return "Interrupted";
      }
      setProgress(100 * current / lengthOfTask);
      current++;
    }
    return "Done";
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
