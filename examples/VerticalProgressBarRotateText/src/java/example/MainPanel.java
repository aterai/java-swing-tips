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

public final class MainPanel extends JPanel implements HierarchyListener {
  private transient SwingWorker<String, Void> worker;

  private MainPanel() {
    super(new BorderLayout());
    // UIManager.put("ProgressBar.rotateText", Boolean.FALSE);
    JProgressBar progressBar1 = new JProgressBar(SwingConstants.VERTICAL);
    progressBar1.setStringPainted(true);

    JProgressBar progressBar2 = new JProgressBar(SwingConstants.VERTICAL);
    progressBar2.setStringPainted(true);
    UIDefaults d = new UIDefaults();
    d.put("ProgressBar.rotateText", Boolean.FALSE);
    // NimbusDefaults has a typo in a L&F property - Java Bug System
    // https://bugs.openjdk.org/browse/JDK-8285962
    // d.put("ProgressBar.vertictalSize", new Dimension(50, 150));
    d.put("ProgressBar.verticalSize", new Dimension(50, 150));
    progressBar2.putClientProperty("Nimbus.Overrides", d);
    progressBar2.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);

    JButton button = new JButton("Test");
    button.addActionListener(e -> {
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
      }
      worker = new BackgroundTask();
      worker.addPropertyChangeListener(new ProgressListener(progressBar1));
      worker.addPropertyChangeListener(new ProgressListener(progressBar2));
      worker.execute();
    });

    JPanel p = new JPanel();
    p.add(progressBar1);
    p.add(Box.createHorizontalStrut(16));
    p.add(progressBar2);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
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
      worker.cancel(true);
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
