// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    String key = "ProgressMonitor.progressText";
    JTextArea area = new JTextArea();
    area.setEditable(false);
    area.append(String.format("%s: %s%n", key, UIManager.getString(key)));

    JTextField textField = new JTextField("Title Progress...");
    JCheckBox check = new JCheckBox("Default");
    check.addActionListener(e -> textField.setEnabled(!((JCheckBox) e.getSource()).isSelected()));

    ProgressMonitor monitor = new ProgressMonitor(this, "message", "note", 0, 100);
    JButton runButton = new JButton("run");
    runButton.addActionListener(e -> {
      runButton.setEnabled(false);
      String title = check.isSelected() ? null : textField.getText();
      UIManager.put(key, title);
      monitor.setProgress(0);
      SwingWorker<String, String> worker = new BackgroundTask() {
        @Override protected void process(List<String> chunks) {
          chunks.forEach(monitor::setNote);
        }

        @Override protected void done() {
          runButton.setEnabled(true);
          monitor.close();
          try {
            if (isCancelled()) {
              area.append("Cancelled\n");
            } else {
              area.append(get() + "\n");
            }
          } catch (InterruptedException ex) {
            area.append("Interrupted\n");
            Thread.currentThread().interrupt();
          } catch (ExecutionException ex) {
            area.append("ExecutionException\n");
          }
          area.setCaretPosition(area.getDocument().getLength());
        }
      };
      worker.addPropertyChangeListener(new ProgressListener(monitor));
      worker.execute();
    });

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(check);
    box.add(Box.createHorizontalStrut(2));
    box.add(textField);
    box.add(Box.createHorizontalStrut(2));
    box.add(runButton);
    add(new JScrollPane(area));
    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
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
      Object o = e.getSource();
      if (o instanceof SwingWorker) {
        SwingWorker<?, ?> task = (SwingWorker<?, ?>) o;
        if (task.isDone() || monitor.isCanceled()) {
          task.cancel(true);
        }
      }
    }
  }
}
