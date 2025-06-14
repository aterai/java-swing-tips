// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout(5, 5));
    String key = "ProgressMonitor.progressText";
    log.setEditable(false);
    log.append(String.format("%s: %s%n", key, UIManager.getString(key)));

    JTextField textField = new JTextField("Title Progress...");
    JCheckBox check = new JCheckBox("Default");
    check.addActionListener(e -> textField.setEnabled(!((JCheckBox) e.getSource()).isSelected()));

    JButton runButton = new JButton("run");
    runButton.addActionListener(e -> {
      JButton b = (JButton) e.getSource();
      b.setEnabled(false);
      UIManager.put(key, check.isSelected() ? null : textField.getText());
      SwingWorker<String, String> worker = getWorker(b);
      worker.execute();
    });

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(check);
    box.add(Box.createHorizontalStrut(2));
    box.add(textField);
    box.add(Box.createHorizontalStrut(2));
    box.add(runButton);
    add(new JScrollPane(log));
    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private SwingWorker<String, String> getWorker(JButton runButton) {
    ProgressMonitor monitor = new ProgressMonitor(this, "message", "note", 0, 100);
    monitor.setProgress(0);
    SwingWorker<String, String> worker = new BackgroundTask() {
      @Override protected void process(List<String> chunks) {
        chunks.forEach(monitor::setNote);
      }

      @Override protected void done() {
        runButton.setEnabled(true);
        monitor.close();
        log.append(getDoneMessage() + "\n");
        log.setCaretPosition(log.getDocument().getLength());
      }
    };
    worker.addPropertyChangeListener(new ProgressListener(monitor));
    return worker;
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
  @Override protected String doInBackground() throws InterruptedException {
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
      msg = "ExecutionException";
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
    Object o = e.getSource();
    boolean isProgress = "progress".equals(e.getPropertyName());
    if (isProgress && o instanceof SwingWorker) {
      monitor.setProgress((Integer) e.getNewValue());
      SwingWorker<?, ?> task = (SwingWorker<?, ?>) o;
      if (task.isDone() || monitor.isCanceled()) {
        task.cancel(true);
      }
    }
  }
}
