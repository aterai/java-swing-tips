// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel implements HierarchyListener {
  private transient SwingWorker<String, Void> worker;

  private MainPanel() {
    super(new BorderLayout());
    BoundedRangeModel m = new DefaultBoundedRangeModel();
    JProgressBar progress0 = new JProgressBar(m);
    progress0.setStringPainted(false);

    JProgressBar progress1 = new JProgressBar(m);
    progress1.setStringPainted(true);

    JProgressBar progress2 = new JProgressBar(m);
    progress2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    JProgressBar progress3 = new JProgressBar(m);
    progress3.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    progress3.setStringPainted(true);

    JPanel p = new JPanel(new GridLayout(2, 2, 10, 10));
    p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    Stream.of(progress0, progress1, progress2, progress3).forEach(p::add);

    addHierarchyListener(this);
    add(p, BorderLayout.NORTH);
    add(makeVerticalProgress(m), BorderLayout.WEST);
    add(makeBox(progress0), BorderLayout.EAST);
    setPreferredSize(new Dimension(320, 240));
  }

  private Box makeVerticalProgress(BoundedRangeModel m) {
    JProgressBar progress0 = new JProgressBar(m);
    progress0.setOrientation(SwingConstants.VERTICAL);

    JProgressBar progress1 = new JProgressBar(m);
    progress1.setOrientation(SwingConstants.VERTICAL);
    progress1.setStringPainted(true);

    JProgressBar progress2 = new JProgressBar(m);
    progress2.setOrientation(SwingConstants.VERTICAL);
    progress2.setStringPainted(true);
    progress2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

    JProgressBar progress3 = new JProgressBar(m) {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.scale(1, -1);
        g2.translate(0, -getHeight());
        super.paintComponent(g2);
        g2.dispose();
      }
    };
    progress3.setOrientation(SwingConstants.VERTICAL);
    progress3.setStringPainted(true);

    JProgressBar progress4 = new JProgressBar(m);
    progress4.setOrientation(SwingConstants.VERTICAL);
    JLayer<JProgressBar> layer = new JLayer<>(progress4, new VerticalFlipLayerUI<>());

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    box.add(Box.createHorizontalGlue());
    Stream.of(progress0, progress1, progress2, progress3, layer).forEach(c -> {
      box.add(c);
      box.add(Box.createHorizontalStrut(25));
    });
    return box;
  }

  private Box makeBox(JProgressBar progress0) {
    JButton button = new JButton("Test");
    button.addActionListener(e -> {
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
      }
      worker = new BackgroundTask();
      worker.addPropertyChangeListener(new ProgressListener(progress0));
      worker.execute();
    });
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    box.add(Box.createVerticalGlue());
    box.add(button);
    return box;
  }

  @Override public void hierarchyChanged(HierarchyEvent e) {
    boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
    if (b && !e.getComponent().isDisplayable() && Objects.nonNull(worker)) {
      // System.out.println("DISPOSE_ON_CLOSE");
      worker.cancel(true);
      // worker = null;
    }
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

class BackgroundTask extends SwingWorker<String, Void> {
  @Override protected String doInBackground() throws InterruptedException {
    int current = 0;
    int lengthOfTask = 200;
    while (current <= lengthOfTask && !isCancelled()) {
      doSomething(100 * current / lengthOfTask);
      current++;
    }
    return "Done";
  }

  protected void doSomething(int progress) throws InterruptedException {
    Thread.sleep(10);
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

class VerticalFlipLayerUI<V extends Component> extends LayerUI<V> {
  private transient BufferedImage buf;

  @Override public void paint(Graphics g, JComponent c) {
    if (c instanceof JLayer) {
      Component view = ((JLayer<?>) c).getView();
      Dimension d = view.getSize();
      BufferedImage img = Optional.ofNullable(buf)
          .filter(bi -> bi.getWidth() == d.width && bi.getHeight() == d.height)
          .orElseGet(() -> new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB));
      Graphics2D g2 = img.createGraphics();
      g2.scale(1, -1);
      g2.translate(0, -d.height);
      // super.paint(g2, c);
      view.paint(g2);
      g2.dispose();
      g.drawImage(img, 0, 0, view);
      buf = img;
    } else {
      super.paint(g, c);
    }
  }
}
