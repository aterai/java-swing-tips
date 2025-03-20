// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea area = new JTextArea();
  private final JPanel statusPanel = new JPanel(new BorderLayout());
  private final JButton runButton = new JButton("run");
  private final JButton cancelButton = new JButton("cancel");
  private final JProgressBar bar = new JProgressBar();
  private final LoadingLabel loadingLabel = new LoadingLabel();
  private transient SwingWorker<String, String> worker;

  private MainPanel() {
    super(new BorderLayout(5, 5));
    area.setEditable(false);
    area.setLineWrap(true);

    runButton.addActionListener(e -> executeWorker());
    cancelButton.addActionListener(e ->
        Optional.ofNullable(worker).filter(w -> !w.isDone()).ifPresent(w -> w.cancel(true)));

    Box box = Box.createHorizontalBox();
    box.add(loadingLabel);
    box.add(Box.createHorizontalGlue());
    box.add(runButton);
    box.add(Box.createHorizontalStrut(2));
    box.add(cancelButton);
    add(new JScrollPane(area));
    add(box, BorderLayout.NORTH);
    add(statusPanel, BorderLayout.SOUTH);
    statusPanel.add(bar);
    statusPanel.setVisible(false);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private final class AnimationTask extends BackgroundTask {
    @Override protected void process(List<String> chunks) {
      // System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
      if (isDisplayable() && !isCancelled()) {
        chunks.forEach(MainPanel.this::appendText);
      } else {
        // System.out.println("process: DISPOSE_ON_CLOSE");
        cancel(true);
      }
    }

    @Override protected void done() {
      // System.out.println("done() is EDT?: " + EventQueue.isDispatchThread());
      if (!isDisplayable()) {
        // System.out.println("done: DISPOSE_ON_CLOSE");
        cancel(true);
        return;
      }
      updateComponentDone();
      String msg;
      try {
        msg = isCancelled() ? "Cancelled" : get();
      } catch (InterruptedException ex) {
        msg = "Interrupted";
        Thread.currentThread().interrupt();
      } catch (ExecutionException ex) {
        // ex.printStackTrace();
        msg = "Error: " + ex.getMessage();
      }
      appendText("\n" + msg + "\n");
    }
  }

  public void executeWorker() {
    // System.out.println("actionPerformed() is EDT?: " + EventQueue.isDispatchThread());
    runButton.setEnabled(false);
    cancelButton.setEnabled(true);
    loadingLabel.startAnimation();
    statusPanel.setVisible(true);
    bar.setIndeterminate(true);
    worker = new AnimationTask();
    worker.addPropertyChangeListener(new ProgressListener(bar));
    worker.execute();
  }

  public void updateComponentDone() {
    loadingLabel.stopAnimation();
    runButton.setEnabled(true);
    cancelButton.setEnabled(false);
    statusPanel.setVisible(false);
  }

  public void appendText(String str) {
    area.append(str);
    area.setCaretPosition(area.getDocument().getLength());
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class BackgroundTask extends SwingWorker<String, String> {
  private final Random rnd = new Random();

  @Override protected String doInBackground() throws InterruptedException {
    // System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
    Thread.sleep(2000);
    int current = 0;
    int total = 0;
    int lengthOfTask = 120; // list.size();
    publish("Length Of Task: " + lengthOfTask);
    publish("\n------------------------------\n");
    while (current < lengthOfTask && !isCancelled()) {
      total += doSomething(100 * current / lengthOfTask);
      current++;
    }
    return String.format("Done(%dms)", total);
  }

  protected int doSomething(int progress) throws InterruptedException {
    int iv = rnd.nextInt(100) + 1;
    Thread.sleep(iv);
    setProgress(progress);
    publish(".");
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
    if (!progressBar.isDisplayable() && e.getSource() instanceof SwingWorker) {
      // System.out.println("progress: DISPOSE_ON_CLOSE");
      ((SwingWorker<?, ?>) e.getSource()).cancel(true);
    }
    boolean isProgress = "progress".equals(e.getPropertyName());
    if (isProgress) {
      progressBar.setIndeterminate(false);
      int progress = (Integer) e.getNewValue();
      progressBar.setValue(progress);
    }
  }
}

class LoadingLabel extends JLabel {
  private final Timer animator = new Timer(100, e -> {
    Icon icon = getIcon();
    if (icon instanceof LoadingIcon) {
      ((LoadingIcon) icon).next();
    }
    repaint();
  });
  private transient HierarchyListener listener;

  protected LoadingLabel() {
    super(new LoadingIcon());
  }

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    listener = e -> {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable()) {
        animator.stop();
      }
    };
    addHierarchyListener(listener);
  }

  public void startAnimation() {
    Icon icon = getIcon();
    if (icon instanceof LoadingIcon) {
      ((LoadingIcon) icon).setRunning(true);
    }
    animator.start();
  }

  public void stopAnimation() {
    Icon icon = getIcon();
    if (icon instanceof LoadingIcon) {
      ((LoadingIcon) icon).setRunning(false);
    }
    animator.stop();
  }
}

class LoadingIcon implements Icon {
  private static final Color ELLIPSE_COLOR = new Color(0x80_80_80);
  private static final double R = 2d;
  private static final double SX = 1d;
  private static final double SY = 1d;
  private static final int WIDTH = (int) (R * 8 + SX * 2);
  private static final int HEIGHT = (int) (R * 8 + SY * 2);
  private final List<Shape> list = new ArrayList<>(Arrays.asList(
      new Ellipse2D.Double(SX + 3 * R, SY + 0 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 5 * R, SY + 1 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 6 * R, SY + 3 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 5 * R, SY + 5 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 3 * R, SY + 6 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 1 * R, SY + 5 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 0 * R, SY + 3 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 1 * R, SY + 1 * R, 2 * R, 2 * R)));
  private boolean running;

  public void next() {
    if (running) {
      // list.add(list.remove(0));
      Collections.rotate(list, 1);
    }
  }

  public void setRunning(boolean isRunning) {
    running = isRunning;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    // g2.setPaint(Objects.nonNull(c) ? c.getBackground() : Color.WHITE);
    g2.setPaint(Optional.ofNullable(c).map(Component::getBackground).orElse(Color.WHITE));
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(ELLIPSE_COLOR);
    list.forEach(s -> {
      float alpha = running ? (list.indexOf(s) + 1f) / list.size() : .5f;
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
      g2.fill(s);
    });
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return WIDTH;
  }

  @Override public int getIconHeight() {
    return HEIGHT;
  }
}
