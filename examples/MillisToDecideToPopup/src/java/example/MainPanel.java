// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    JTextArea area = new JTextArea();
    area.setEditable(false);

    ProgressMonitor pm = new ProgressMonitor(null, "message", "note", 0, 100);
    int ms1 = pm.getMillisToDecideToPopup();
    SpinnerNumberModel msToDecide = new SpinnerNumberModel(ms1, 0, 5000, 100);

    int ms2 = pm.getMillisToPopup();
    SpinnerNumberModel msToPopup = new SpinnerNumberModel(ms2, 0, 5000, 100);

    JButton runButton = new JButton("run");
    runButton.addActionListener(e -> {
      Window w = SwingUtilities.getWindowAncestor(runButton);
      int toDecideToPopup = msToDecide.getNumber().intValue();
      int toPopup = msToPopup.getNumber().intValue();
      ProgressMonitor monitor = new ProgressMonitor(w, "message", "note", 0, 100);
      monitor.setMillisToDecideToPopup(toDecideToPopup);
      monitor.setMillisToPopup(toPopup);

      runButton.setEnabled(false);
      executeWorker(monitor, runButton, area);
    });

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.insets = new Insets(5, 5, 5, 0);
    c.anchor = GridBagConstraints.LINE_END;

    JPanel p = new JPanel(new GridBagLayout());
    p.add(new JLabel("MillisToDecideToPopup:"), c);
    p.add(new JLabel("MillisToPopup:"), c);

    c.gridx = 1;
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    p.add(new JSpinner(msToDecide), c);
    p.add(new JSpinner(msToPopup), c);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(runButton);

    add(new JScrollPane(area));
    add(p, BorderLayout.NORTH);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private void executeWorker(ProgressMonitor monitor, JButton button, JTextArea area) {
    SwingWorker<String, String> worker = new BackgroundTask() {
      @Override protected void process(List<String> chunks) {
        if (isDisplayable()) { //  && !isCancelled()) {
          chunks.forEach(monitor::setNote);
        } else {
          // System.out.println("process: DISPOSE_ON_CLOSE");
          cancel(true);
        }
      }

      @Override protected void done() {
        if (!isDisplayable()) {
          // System.out.println("done: DISPOSE_ON_CLOSE");
          cancel(true);
          return;
        }
        button.setEnabled(true);
        monitor.close();
        if (isCancelled()) {
          area.append("Cancelled\n");
        } else {
          try {
            String text = get();
            area.append(text + "\n");
          } catch (InterruptedException ex) {
            area.append("Interrupted\n");
            Thread.currentThread().interrupt();
          } catch (ExecutionException ex) {
            ex.printStackTrace();
            area.append(String.format("Error: %s%n", ex.getMessage()));
          }
        }
        area.setCaretPosition(area.getDocument().getLength());
      }
    };
    worker.addPropertyChangeListener(new ProgressListener(monitor));
    worker.execute();
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
  private final Random rnd = new Random();

  @Override protected String doInBackground() throws InterruptedException {
    int lengthOfTask = 1_000;
    int current = 0;
    int total = 0;
    while (current < lengthOfTask && !isCancelled()) {
      total += doSomething();
      int v = 100 * current++ / lengthOfTask;
      setProgress(v);
      publish(String.format("%d%%", v));
    }
    return String.format("Done(%dms)", total);
  }

  protected int doSomething() throws InterruptedException {
    int iv = rnd.nextInt(10) + 1;
    Thread.sleep(iv);
    return iv;
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
