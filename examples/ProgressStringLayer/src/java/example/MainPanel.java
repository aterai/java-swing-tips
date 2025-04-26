// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.Random;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel implements HierarchyListener {
  private transient SwingWorker<String, Void> worker;

  private MainPanel() {
    super(new BorderLayout());
    BoundedRangeModel m = new DefaultBoundedRangeModel();
    JProgressBar progressBar = new JProgressBar(m);
    progressBar.setOrientation(SwingConstants.VERTICAL);

    JProgressBar progressBar0 = new JProgressBar(m);
    progressBar0.setOrientation(SwingConstants.VERTICAL);
    progressBar0.setStringPainted(false);
    progressBar0.setStringPainted(true);

    JButton button = new JButton("Test");
    button.addActionListener(e -> {
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
      }
      worker = new BackgroundTask();
      worker.addPropertyChangeListener(new ProgressListener(progressBar));
      worker.execute();
    });

    JPanel p = new JPanel();
    p.add(progressBar);
    p.add(Box.createHorizontalStrut(5));
    p.add(progressBar0);
    p.add(Box.createHorizontalStrut(5));
    p.add(makeProgressBar1(m));
    p.add(Box.createHorizontalStrut(5));
    p.add(makeProgressBar2(m));
    // p.add(new JButton(new AbstractAction("+10") {
    //   private int i = 0;
    //   @Override public void actionPerformed(ActionEvent e) {
    //     m.setValue(i = (i >= 100) ? 0 : i + 10);
    //   }
    // }));

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(button);
    box.add(Box.createHorizontalStrut(5));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    addHierarchyListener(this);
    add(new JProgressBar(m), BorderLayout.NORTH);
    add(p);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void hierarchyChanged(HierarchyEvent e) {
    boolean displayability = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
    if (displayability && !e.getComponent().isDisplayable() && Objects.nonNull(worker)) {
      // System.out.println("DISPOSE_ON_CLOSE");
      worker.cancel(true);
      // worker = null;
    }
  }

  private static Component makeProgressBar1(BoundedRangeModel model) {
    JProgressBar progressBar = new TextLabelProgressBar(model);
    progressBar.setOrientation(SwingConstants.VERTICAL);
    progressBar.setStringPainted(false);
    return progressBar;
  }

  private static Component makeProgressBar2(BoundedRangeModel model) {
    JLabel label = new JLabel("000/100");
    label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    JProgressBar progressBar = new JProgressBar(model) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        Insets i = label.getInsets();
        d.width = label.getPreferredSize().width + i.left + i.right;
        return d;
      }
    };
    progressBar.setOrientation(SwingConstants.VERTICAL);
    progressBar.setStringPainted(false);
    return new JLayer<>(progressBar, new ProgressBarLayerUI(label));
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class BackgroundTask extends SwingWorker<String, Void> {
  private final Random rnd = new Random();

  @Override protected String doInBackground() throws InterruptedException {
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

class TextLabelProgressBar extends JProgressBar {
  private final JLabel label = new JLabel("000/100", CENTER);
  // private transient ChangeListener changeListener;

  protected TextLabelProgressBar(BoundedRangeModel model) {
    super(model);
  }

  @Override public void updateUI() {
    removeAll();
    // removeChangeListener(changeListener);
    super.updateUI();
    setLayout(new BorderLayout());
    // changeListener = e -> {
    //   int iv = (int) (100 * getPercentComplete());
    //   label.setText(String.format("%03d/100", iv));
    //   // label.setText(getString());
    // };
    // addChangeListener(changeListener);
    EventQueue.invokeLater(() -> {
      SwingUtilities.updateComponentTreeUI(label);
      add(label);
      label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    });
  }

  @Override protected ChangeListener createChangeListener() {
    return e -> {
      int iv = (int) (100 * getPercentComplete());
      label.setText(String.format("%03d/100", iv));
      // label.setText(getString());
    };
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    Insets i = label.getInsets();
    d.width = label.getPreferredSize().width + i.left + i.right;
    return d;
  }
}

class ProgressBarLayerUI extends LayerUI<JProgressBar> {
  private final JPanel rubberStamp = new JPanel();
  private final JLabel label;

  protected ProgressBarLayerUI(JLabel label) {
    super();
    this.label = label;
  }

  @Override public void updateUI(JLayer<? extends JProgressBar> l) {
    super.updateUI(l);
    SwingUtilities.updateComponentTreeUI(label);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JProgressBar progress = (JProgressBar) ((JLayer<?>) c).getView();
      int iv = (int) (100 * progress.getPercentComplete());
      label.setText(String.format("%03d/100", iv));

      Dimension d = label.getPreferredSize();
      int x = (c.getWidth() - d.width) / 2;
      int y = (c.getHeight() - d.height) / 2;
      // label.setText(progress.getString());
      SwingUtilities.paintComponent(g, label, rubberStamp, x, y, d.width, d.height);
    }
  }
}
