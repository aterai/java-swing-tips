// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
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

    JProgressBar progress = new JProgressBar() {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new OneDirectionIndeterminateProgressBarUI());
      }
    };
    // TEST: progress.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    List<JProgressBar> list = Arrays.asList(new JProgressBar(), progress);

    JPanel p = new JPanel(new GridLayout(2, 1));
    list.forEach(bar -> p.add(makePanel(bar)));

    JButton button = new JButton("Start");
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

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame frame = new JFrame("@title@");
    frame.setMinimumSize(new Dimension(256, 200));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class OneDirectionIndeterminateProgressBarUI extends BasicProgressBarUI {
  // @see com/sun/java/swing/plaf/windows/WindowsProgressBarUI.java
  @Override protected Rectangle getBox(Rectangle r) {
    Rectangle rect = super.getBox(r);
    int framecount = getFrameCount() / 2;
    int currentFrame = getAnimationIndex() % framecount;

    if (progressBar.getOrientation() == JProgressBar.VERTICAL) {
      int len = progressBar.getHeight();
      len += rect.height * 2; // add 2x for the trails
      double delta = len / (double) framecount;
      rect.y = (int) (delta * currentFrame);
    } else {
      int len = progressBar.getWidth();
      len += rect.width * 2; // add 2x for the trails
      double delta = len / (double) framecount;
      rect.x = (int) (delta * currentFrame);
    }
    return rect;
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
