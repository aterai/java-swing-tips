// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final transient ProgressMonitor monitor;
  private final JTextArea area = new JTextArea();
  private final JButton runButton = new JButton("run");

  private MainPanel() {
    super(new BorderLayout(5, 5));
    area.setEditable(false);
    monitor = new ProgressMonitor(this, "message", "note", 0, 100);
    runButton.addActionListener(e -> {
      runButton.setEnabled(false);
      monitor.setProgress(0);
      SwingWorker<String, String> worker = new ProgressMonitorTask();
      worker.addPropertyChangeListener(new ProgressListener(monitor));
      worker.execute();
    });
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(runButton);
    add(new JScrollPane(area));
    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private final class ProgressMonitorTask extends BackgroundTask {
    @Override protected void process(List<String> chunks) {
      chunks.forEach(monitor::setNote);
    }

    @Override protected void done() {
      runButton.setEnabled(true);
      monitor.close();
      area.append(getDoneMessage() + "\n");
      area.setCaretPosition(area.getDocument().getLength());
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class BackgroundTask extends SwingWorker<String, String> {
  @Override protected String doInBackground() throws InterruptedException {
    // System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
    int current = 0;
    int lengthOfTask = 120; // list.size();
    while (current < lengthOfTask && !isCancelled()) {
      doSomething();
      current++;
      setProgress(100 * current / lengthOfTask);
      publish(current + "/" + lengthOfTask);
    }
    return "Done";
  }

  protected void doSomething() throws InterruptedException {
    Thread.sleep(50);
  }

  protected String getDoneMessage() {
    String msg;
    try {
      msg = isCancelled() ? "Cancelled" : get();
    } catch (InterruptedException ex) {
      msg = "Interrupted";
      Thread.currentThread().interrupt();
    } catch (ExecutionException ex) {
      msg = "ExecutionException: " + ex.getMessage();
    }
    return msg;
  }
}

class ProgressListener implements PropertyChangeListener {
  private final ProgressMonitor monitor;

  protected ProgressListener(ProgressMonitor monitor) {
    this.monitor = monitor;
    this.monitor.setProgress(0);
  }

  @Override public void propertyChange(PropertyChangeEvent e) {
    boolean isProgress = "progress".equals(e.getPropertyName());
    if (isProgress) {
      monitor.setProgress((Integer) e.getNewValue());
      Optional.ofNullable(e.getSource())
          .filter(SwingWorker.class::isInstance)
          .map(SwingWorker.class::cast)
          .filter(task -> task.isDone() || monitor.isCanceled())
          .ifPresent(task -> task.cancel(true));
    }
  }
}
