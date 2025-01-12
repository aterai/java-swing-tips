// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea area = new JTextArea();
  private final JProgressBar bar = new JProgressBar();
  private final JPanel statusPanel = new JPanel(new BorderLayout());
  private final JButton runButton = new JButton("run");
  private final JButton cancelButton = new JButton("cancel");
  private final LoadingLabel loadingLabel = new LoadingLabel();
  private transient BackgroundTask worker;

  private MainPanel() {
    super(new BorderLayout());
    area.setEditable(false);
    area.setLineWrap(true);
    runButton.addActionListener(e -> executeWorker());
    cancelButton.addActionListener(e -> Optional.ofNullable(worker)
        .filter(w -> !w.isDone())
        .ifPresent(w -> w.cancel(true)));

    Box box = Box.createHorizontalBox();
    box.add(loadingLabel);
    box.add(Box.createHorizontalGlue());
    box.add(runButton);
    box.add(cancelButton);
    add(box, BorderLayout.NORTH);
    add(statusPanel, BorderLayout.SOUTH);
    add(new JScrollPane(area));
    setPreferredSize(new Dimension(320, 240));
  }

  public void executeWorker() {
    runButton.setEnabled(false);
    cancelButton.setEnabled(true);
    loadingLabel.startAnimation();
    statusPanel.removeAll();
    statusPanel.add(bar);
    statusPanel.revalidate();
    bar.setIndeterminate(true);
    worker = new BackgroundTask() {
      @Override protected void process(List<String> chunks) {
        // System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
        if (isDisplayable() && !isCancelled()) {
          chunks.forEach(MainPanel.this::appendLine);
        } else {
          cancel(true);
        }
      }

      @Override protected void done() {
        // System.out.println("done() is EDT?: " + EventQueue.isDispatchThread());
        if (!isDisplayable()) {
          cancel(true);
          return;
        }
        loadingLabel.stopAnimation();
        runButton.setEnabled(true);
        cancelButton.setEnabled(false);
        statusPanel.remove(bar);
        statusPanel.revalidate();
        appendLine("\n");
        try {
          appendLine(isCancelled() ? "Cancelled" : get());
        } catch (InterruptedException ex) {
          appendLine("Interrupted");
          Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
          appendLine("Error");
        }
        appendLine("\n\n");
      }
    };
    worker.addPropertyChangeListener(new ProgressListener(bar));
    worker.execute();
  }

  public void appendLine(String str) {
    area.append(str);
    area.setCaretPosition(area.getDocument().getLength());
  }

  // private boolean isCancelled() {
  //   return Objects.isNull(worker) && worker.isCancelled();
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class BackgroundTask extends SwingWorker<String, String> {
  @Override protected String doInBackground() throws InterruptedException {
    // System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
    Thread.sleep(1000);
    int current = 0;
    int lengthOfTask = 120; // list.size();
    publish("Length Of Task: " + lengthOfTask);
    publish("\n------------------------------\n");
    while (current < lengthOfTask && !isCancelled()) {
      publish(".");
      setProgress(100 * current / lengthOfTask);
      doSomething();
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

class LoadingLabel extends JLabel {
  private transient HierarchyListener listener;
  private final Timer animator = new Timer(100, null);

  protected LoadingLabel() {
    super(new AnimeIcon());
    animator.addActionListener(e -> {
      Icon icon = getIcon();
      if (icon instanceof AnimeIcon) {
        ((AnimeIcon) icon).next();
      }
      repaint();
    });
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
    if (icon instanceof AnimeIcon) {
      ((AnimeIcon) icon).setRunning(true);
    }
    animator.start();
    repaint();
  }

  public void stopAnimation() {
    Icon icon = getIcon();
    if (icon instanceof AnimeIcon) {
      ((AnimeIcon) icon).setRunning(false);
    }
    animator.stop();
    repaint();
  }
}

// // TEST: 1
// class AnimeIcon implements Icon {
//   private static final Color ELLIPSE_COLOR = new Color(0x80_80_80);
//   private static final double R = 2d;
//   private static final double SX = 1d;
//   private static final double SY = 1d;
//   private static final int WIDTH = (int) (R * 8 + SX * 2);
//   private static final int HEIGHT = (int) (R * 8 + SY * 2);
//   private final List<Shape> list = new ArrayList<>(Arrays.asList(
//     new Ellipse2D.Double(SX + 3 * R, SY + 0 * R, 2 * R, 2 * R),
//     new Ellipse2D.Double(SX + 5 * R, SY + 1 * R, 2 * R, 2 * R),
//     new Ellipse2D.Double(SX + 6 * R, SY + 3 * R, 2 * R, 2 * R),
//     new Ellipse2D.Double(SX + 5 * R, SY + 5 * R, 2 * R, 2 * R),
//     new Ellipse2D.Double(SX + 3 * R, SY + 6 * R, 2 * R, 2 * R),
//     new Ellipse2D.Double(SX + 1 * R, SY + 5 * R, 2 * R, 2 * R),
//     new Ellipse2D.Double(SX + 0 * R, SY + 3 * R, 2 * R, 2 * R),
//     new Ellipse2D.Double(SX + 1 * R, SY + 1 * R, 2 * R, 2 * R)));
//
//   private boolean running;
//   public void next() {
//     if (running) {
//       // list.add(list.remove(0));
//       Collections.rotate(list, 1);
//     }
//   }
//
//   public void setRunning(boolean isRunning) {
//     running = isRunning;
//   }
//
//   @Override public void paintIcon(Component c, Graphics g, int x, int y) {
//     Graphics2D g2 = (Graphics2D) g.create();
//     g2.setPaint(Optional.ofNullable(c).map(Component::getBackground).orElse(Color.WHITE));
//     g2.fillRect(x, y, getIconWidth(), getIconHeight());
//     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//     g2.setPaint(ELLIPSE_COLOR);
//     g2.translate(x, y);
//     int size = list.size();
//     for (int i = 0; i < size; i++) {
//       float alpha = running ? (i + 1) / (float) size : .5f;
//       g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
//       g2.fill(list.get(i));
//     }
//     g2.dispose();
//   }
//
//   @Override public int getIconWidth() {
//     return WIDTH;
//   }
//
//   @Override public int getIconHeight() {
//     return HEIGHT;
//   }
// }

// // TEST: 2
// class AnimeIcon implements Icon {
//   private static final Color ELLIPSE_COLOR = new Color(0xE6_B3_B3);
//   private final List<Shape> list = new ArrayList<>();
//   private final Dimension dim;
//   private boolean running;
//   protected AnimeIcon() {
//     super();
//     int r = 4;
//     Shape s = new Ellipse2D.Double(0, 0, 2 * r, 2 * r);
//     for (int i = 0; i < 8; i++) {
//       AffineTransform at = AffineTransform.getRotateInstance(i * 2 * Math.PI / 8);
//       at.concatenate(AffineTransform.getTranslateInstance(r, r));
//       list.add(at.createTransformedShape(s));
//     }
//     // int d = (int) (r * 2 * (1 + 2 * Math.sqrt(2)));
//     int d = (int) r * 2 * (1 + 3); // 2 * Math.sqrt(2) is nearly equal to 3.
//     dim = new Dimension(d, d);
//   }
//
//   @Override public int getIconWidth() {
//     return dim.width;
//   }
//
//   @Override public int getIconHeight() {
//     return dim.height;
//   }
//
//   @Override public void paintIcon(Component c, Graphics g, int x, int y) {
//     Graphics2D g2 = (Graphics2D) g.create();
//     g2.setPaint(Optional.ofNullable(c).map(Component::getBackground).orElse(Color.WHITE));
//     g2.fillRect(x, y, getIconWidth(), getIconHeight());
//     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//     g2.setPaint(ELLIPSE_COLOR);
//     int xx = x + dim.width / 2;
//     int yy = y + dim.height / 2;
//     g2.translate(xx, yy);
//     int size = list.size();
//     for (int i = 0; i < size; i++) {
//       float alpha = running ? (i + 1) / (float) size : .5f;
//       g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
//       g2.fill(list.get(i));
//     }
//     g2.dispose();
//   }
//
//   public void next() {
//     if (running) {
//       // list.add(list.remove(0));
//       Collections.rotate(list, 1);
//     }
//   }
//
//   public void setRunning(boolean isRunning) {
//     running = isRunning;
//   }
// }

// TEST: 3
class AnimeIcon implements Icon {
  private static final Color ELLIPSE_COLOR = new Color(0xE6_B3_B3);
  private final List<Shape> list = new ArrayList<>();
  private final Dimension dim;
  private boolean running;
  private int rotate = 45;

  protected AnimeIcon() {
    super();
    int r = 4;
    Shape s = new Ellipse2D.Double(0d, 0d, 2d * r, 2d * r);
    for (int i = 0; i < 8; i++) {
      AffineTransform at = AffineTransform.getRotateInstance(i * 2 * Math.PI / 8);
      at.concatenate(AffineTransform.getTranslateInstance(r, r));
      list.add(at.createTransformedShape(s));
    }
    int d = r * 2 * (1 + 3);
    dim = new Dimension(d, d);
  }

  @Override public int getIconWidth() {
    return dim.width;
  }

  @Override public int getIconHeight() {
    return dim.height;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(Optional.ofNullable(c).map(Component::getBackground).orElse(Color.WHITE));
    g2.fillRect(x, y, getIconWidth(), getIconHeight());
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(ELLIPSE_COLOR);
    int xx = x + dim.width / 2;
    int yy = y + dim.height / 2;
    AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(rotate), xx, yy);
    at.concatenate(AffineTransform.getTranslateInstance(xx, yy));
    int size = list.size();
    for (int i = 0; i < size; i++) {
      float alpha = running ? (i + 1) / (float) size : .5f;
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
      g2.fill(at.createTransformedShape(list.get(i)));
    }
    g2.dispose();
  }

  public void next() {
    if (running) {
      rotate = (rotate + 45) % 360; // 45 = 360 / 8
    }
  }

  public void setRunning(boolean isRunning) {
    running = isRunning;
  }
}

// // TEST: 4
// class AnimeIcon implements Icon {
//   private static final int R = 4;
//   private static final Color ELLIPSE_COLOR = new Color(0xE6_B3_B3);
//   private final Dimension dim;
//   private boolean running;
//   private final List<Shape> list = new ArrayList<>();
//   protected AnimeIcon() {
//     super();
//     int d = (int) R * 2 * (1 + 3);
//     dim = new Dimension(d, d);
//
//     Ellipse2D circle = new Ellipse2D.Double(R, R, d - 2 * R, d - 2 * R);
//     PathIterator i = new FlatteningPathIterator(circle.getPathIterator(null), R);
//     double[] coords = new double[6];
//     int idx = 0;
//     while (!i.isDone()) {
//       i.currentSegment(coords);
//       if (idx < 8) { // XXX
//         list.add(new Ellipse2D.Double(coords[0] - R, coords[1] - R, 2 * R, 2 * R));
//         idx++;
//       }
//       i.next();
//     }
//   }
//
//   @Override public int getIconWidth() {
//     return dim.width;
//   }
//
//   @Override public int getIconHeight() {
//     return dim.height;
//   }
//
//   @Override public void paintIcon(Component c, Graphics g, int x, int y) {
//     Graphics2D g2 = (Graphics2D) g.create();
//     g2.setPaint(Optional.ofNullable(c).map(Component::getBackground).orElse(Color.WHITE));
//     g2.fillRect(x, y, getIconWidth(), getIconHeight());
//     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//     g2.setPaint(ELLIPSE_COLOR);
//     int size = list.size();
//     for (int i = 0; i < size; i++) {
//       float alpha = running ? (i + 1) / (float) size : .5f;
//       g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
//       g2.fill(list.get(i));
//     }
//     g2.dispose();
//   }
//
//   public void next() {
//     if (running) {
//       // list.add(list.remove(0));
//       Collections.rotate(list, 1);
//     }
//   }
//
//   public void setRunning(boolean isRunning) {
//     running = isRunning;
//   }
// }
