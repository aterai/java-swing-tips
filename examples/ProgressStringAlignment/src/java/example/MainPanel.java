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
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.ChangeListener;

public final class MainPanel extends JPanel implements HierarchyListener {
  private transient SwingWorker<String, Void> worker;

  private MainPanel() {
    super(new BorderLayout());
    BoundedRangeModel model = new DefaultBoundedRangeModel();
    JProgressBar progressBar1 = new StringAlignmentProgressBar(model, SwingConstants.RIGHT);
    JProgressBar progressBar2 = new StringAlignmentProgressBar(model, SwingConstants.LEFT);
    progressBar2.setBorder(BorderFactory.createTitledBorder("TitledBorder"));

    JCheckBox check = new JCheckBox("setStringPainted");
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      Stream.of(progressBar1, progressBar2).forEach(bar -> bar.setStringPainted(b));
    });

    JButton button = new JButton("Test");
    button.addActionListener(e -> {
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
      }
      worker = new BackgroundTask();
      worker.addPropertyChangeListener(new ProgressListener(progressBar1));
      worker.execute();
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    p.add(progressBar1);
    p.add(progressBar2);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(check);
    box.add(Box.createHorizontalStrut(5));
    box.add(button);
    box.add(Box.createHorizontalStrut(5));

    addHierarchyListener(this);
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

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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

class StringAlignmentProgressBar extends JProgressBar {
  private final JLabel label;
  // private transient ChangeListener changeListener;

  protected StringAlignmentProgressBar(BoundedRangeModel model, int horizAlignment) {
    super(model);
    label = new JLabel(" ", horizAlignment);
  }

  @Override public void updateUI() {
    removeAll();
    // removeChangeListener(changeListener);
    super.updateUI();
    setLayout(new BorderLayout());
    // changeListener = e -> label.setText(getString());
    // addChangeListener(changeListener);
    EventQueue.invokeLater(() -> {
      add(label);
      label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    });
  }

  @Override protected ChangeListener createChangeListener() {
    return e -> label.setText(getString());
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
