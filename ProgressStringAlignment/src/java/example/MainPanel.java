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
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.ChangeListener;

public final class MainPanel extends JPanel implements HierarchyListener {
  private final BoundedRangeModel model = new DefaultBoundedRangeModel();
  private final JProgressBar progressBar1 = new StringAlignmentProgressBar(model, SwingConstants.RIGHT);
  private final JProgressBar progressBar2 = new StringAlignmentProgressBar(model, SwingConstants.LEFT);
  private transient SwingWorker<String, Void> worker;

  private MainPanel() {
    super(new BorderLayout());

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
    boolean isDisplayableChanged = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
    if (isDisplayableChanged && !e.getComponent().isDisplayable() && Objects.nonNull(worker)) {
      System.out.println("DISPOSE_ON_CLOSE");
      worker.cancel(true);
      worker = null;
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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

  protected StringAlignmentProgressBar(BoundedRangeModel model, int horizontalAlignment) {
    super(model);
    label = new JLabel(getString(), horizontalAlignment);
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
  @Override public String doInBackground() throws InterruptedException {
    int current = 0;
    int lengthOfTask = 100;
    while (current <= lengthOfTask && !isCancelled()) {
      Thread.sleep(50);
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
