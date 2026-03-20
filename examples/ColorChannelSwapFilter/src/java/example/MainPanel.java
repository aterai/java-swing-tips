// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private static final Color PROGRESS_FGC = new Color(0x64_FF_00_00, true);
  private transient SwingWorker<String, Void> worker;

  private MainPanel() {
    super(new BorderLayout());
    BoundedRangeModel model = new DefaultBoundedRangeModel();

    JProgressBar pg1 = new JProgressBar(model);
    pg1.setStringPainted(true);

    JProgressBar pg2 = new JProgressBar(model);
    pg2.setStringPainted(true);

    JProgressBar pg3 = new JProgressBar(model);
    pg3.setOpaque(false);

    JProgressBar pg4 = new JProgressBar(model);
    pg4.setOpaque(true); // for NimbusLookAndFeel

    BlockedColorLayerUI<Component> layer = new BlockedColorLayerUI<>();
    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(makeTitledPanel("setStringPainted(true)", pg1, pg2));
    p.add(makeTitledPanel("setStringPainted(false)", pg3, new JLayer<>(pg4, layer)));

    JCheckBox check = new JCheckBox("Turn the progress bar red");
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      pg2.setForeground(b ? PROGRESS_FGC : pg1.getForeground());
      layer.setBlocking(b);
      p.repaint();
    });

    JButton button = new JButton("Start");
    button.addActionListener(e -> {
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
      }
      worker = new BackgroundTask();
      worker.addPropertyChangeListener(new ProgressListener(pg1));
      worker.execute();
    });

    JPanel box = new JPanel(new FlowLayout(FlowLayout.TRAILING, 2, 2));
    box.add(check);
    box.add(button);

    addHierarchyListener(e -> {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable() && Objects.nonNull(worker)) {
        worker.cancel(true);
        // worker = null;
      }
    });

    add(p);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component... list) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 1d;
    c.gridx = GridBagConstraints.REMAINDER;
    Stream.of(list).forEach(cmp -> p.add(cmp, c));
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

class BlockedColorLayerUI<V extends Component> extends LayerUI<V> {
  private boolean blocking;
  private transient BufferedImage buf;

  @Override public void paint(Graphics g, JComponent c) {
    if (blocking && c instanceof JLayer) {
      Component view = ((JLayer<?>) c).getView();
      Dimension d = view.getSize();
      BufferedImage img = Optional.ofNullable(buf)
          .filter(bi -> bi.getWidth() == d.width && bi.getHeight() == d.height)
          .orElseGet(() -> new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB));

      Graphics2D g2 = img.createGraphics();
      // super.paint(g2, c);
      view.paint(g2);
      g2.dispose();

      RGBImageFilter filter = new RedGreenChannelSwapFilter();
      Image image = c.createImage(new FilteredImageSource(img.getSource(), filter));
      g.drawImage(image, 0, 0, view);
      buf = img;
    } else {
      super.paint(g, c);
    }
  }

  public void setBlocking(boolean b) {
    this.blocking = b;
  }
}

class RedGreenChannelSwapFilter extends RGBImageFilter {
  @Override public int filterRGB(int x, int y, int argb) {
    int r = (argb >> 16) & 0xFF;
    int g = (argb >> 8) & 0xFF;
    return argb & 0xFF_00_00_FF | g << 16 | r << 8;
  }
}

class BackgroundTask extends SwingWorker<String, Void> {
  @Override protected String doInBackground() throws InterruptedException {
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
      progressBar.setValue((Integer) e.getNewValue());
    }
  }
}
