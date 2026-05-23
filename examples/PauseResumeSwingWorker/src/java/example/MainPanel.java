// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public final class MainPanel extends JPanel {
  private static final String TXT_PAUSE = "pause";
  private static final String TXT_RESUME = "resume";
  private final JTextArea area = new JTextArea();
  private final JPanel statusPanel = new JPanel(new BorderLayout());
  private final JButton runButton = new JButton("run");
  private final JButton cancelButton = new JButton("cancel");
  private final JButton pauseButton = new JButton(TXT_PAUSE);
  private final JProgressBar bar1 = new JProgressBar();
  private final JProgressBar bar2 = new JProgressBar();
  private transient BackgroundTask worker;

  private MainPanel() {
    super(new BorderLayout(5, 5));
    area.setEditable(false);

    runButton.addActionListener(e -> {
      updateButtonsAndStatusPanel(true);
      worker = new ProgressTask();
      worker.execute();
    });

    pauseButton.setEnabled(false);
    pauseButton.addActionListener(e -> {
      JButton b = (JButton) e.getSource();
      if (Objects.nonNull(worker)) {
        b.setText(worker.isCancelled() || worker.isPaused() ? TXT_PAUSE : TXT_RESUME);
        worker.toggle();
      } else {
        b.setText(TXT_PAUSE);
      }
    });

    cancelButton.setEnabled(false);
    cancelButton.addActionListener(e -> {
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
      }
      pauseButton.setText(TXT_PAUSE);
      pauseButton.setEnabled(false);
    });
    List<Component> buttons = Arrays.asList(pauseButton, cancelButton, runButton);
    Component box = createRightAlignBox(buttons, 80, 5);
    add(new JScrollPane(area));
    add(box, BorderLayout.NORTH);
    add(statusPanel, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private final class ProgressTask extends BackgroundTask {
    @Override protected void process(List<Progress> chunks) {
      if (isDisplayable() && !isCancelled()) {
        chunks.forEach(MainPanel.this::updateProgress);
      } else {
        cancel(true);
      }
    }

    @Override protected void done() {
      if (isDisplayable()) {
        updateButtonsAndStatusPanel(false);
        appendLine(String.format("%n%s%n", getDoneMessage()));
      }
    }
  }

  private void updateProgress(Progress progress) {
    progress.getType().update(this, progress.getValue());
  }

  /* default */ void updateTotalProgress(int value) {
    bar1.setValue(value);
  }

  /* default */ void updateFileProgress(int value) {
    bar2.setValue(value);
  }

  /* default */ void appendLog(Object value) {
    area.append(Objects.toString(value));
  }

  /* default */ void updatePauseMarker(boolean append) {
    if (append) {
      area.append("*");
    } else {
      try {
        Document doc = area.getDocument();
        doc.remove(doc.getLength() - 1, 1);
      } catch (BadLocationException ex) {
        // should never happen
        RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
        wrap.initCause(ex);
        throw wrap;
      }
    }
  }

  private void updateButtonsAndStatusPanel(boolean running) {
    runButton.setEnabled(!running);
    cancelButton.setEnabled(running);
    pauseButton.setEnabled(running);
    if (running) {
      bar1.setValue(0);
      bar2.setValue(0);
      statusPanel.add(bar1, BorderLayout.NORTH);
      statusPanel.add(bar2, BorderLayout.SOUTH);
    } else {
      runButton.requestFocusInWindow();
      statusPanel.removeAll();
    }
    statusPanel.revalidate();
  }

  // @see https://ateraimemo.com/Swing/ButtonWidth.html
  public static Component createRightAlignBox(List<Component> list, int width, int gap) {
    SpringLayout layout = new SpringLayout();
    JPanel p = new JPanel(layout) {
      @Override public Dimension getPreferredSize() {
        int maxHeight = list.stream()
            .mapToInt(c -> c.getPreferredSize().height)
            .max()
            .orElse(0);
        return new Dimension(width * list.size() + gap * 2, maxHeight + gap * 2);
      }
    };
    Spring x = layout.getConstraint(SpringLayout.WIDTH, p);
    Spring y = Spring.constant(gap);
    Spring g = Spring.minus(Spring.constant(gap));
    Spring w = Spring.constant(width);
    for (Component button : list) {
      SpringLayout.Constraints constraints = layout.getConstraints(button);
      x = Spring.sum(x, g);
      constraints.setConstraint(SpringLayout.EAST, x);
      constraints.setY(y);
      constraints.setWidth(w);
      p.add(button);
      x = Spring.sum(x, Spring.minus(w));
    }
    return p;
  }

  public void appendLine(String str) {
    area.append(str);
    area.setCaretPosition(area.getDocument().getLength());
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

enum ProgressType {
  TOTAL {
    @Override /* default */ void update(MainPanel panel, Object value) {
      panel.updateTotalProgress((Integer) value);
    }
  },
  FILE {
    @Override /* default */ void update(MainPanel panel, Object value) {
      panel.updateFileProgress((Integer) value);
    }
  },
  LOG {
    @Override /* default */ void update(MainPanel panel, Object value) {
      panel.appendLog(value);
    }
  },
  PAUSE {
    @Override /* default */ void update(MainPanel panel, Object value) {
      panel.updatePauseMarker((Boolean) value);
    }
  };

  /* default */ abstract void update(MainPanel panel, Object value);
}

final class Progress {
  private final ProgressType type;
  private final Object value;

  /* default */ Progress(ProgressType type, Object value) {
    this.type = type;
    this.value = value;
  }

  public ProgressType getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }
}

abstract class BackgroundTask extends SwingWorker<String, Progress> {
  private boolean paused;
  private final Random random = new Random();

  @Override protected String doInBackground() throws InterruptedException {
    int current = 0;
    int lengthOfTask = 12;
    publish(new Progress(ProgressType.LOG, "Length Of Task: " + lengthOfTask));
    publish(new Progress(ProgressType.LOG, "\n------------------------------\n"));
    while (current < lengthOfTask && !isCancelled()) {
      convertFileToSomething(100 * current / lengthOfTask);
      current++;
    }
    publish(new Progress(ProgressType.LOG, "\n"));
    return "Done";
  }

  protected void convertFileToSomething(int progress) throws InterruptedException {
    boolean blinking = false;
    int current = 0;
    int lengthOfTask = 10 + random.nextInt(50);
    publish(new Progress(ProgressType.TOTAL, progress));
    publish(new Progress(ProgressType.LOG, "*"));
    while (current <= lengthOfTask && !isCancelled()) {
      if (paused) {
        pause(blinking);
        blinking = !blinking;
        continue;
      }
      doSomething(100 * current / lengthOfTask);
      current++;
    }
  }

  public boolean isPaused() {
    return paused;
  }

  public void toggle() {
    paused = !paused;
  }

  private void pause(boolean blinking) throws InterruptedException {
    Thread.sleep(500);
    publish(new Progress(ProgressType.PAUSE, blinking));
  }

  protected void doSomething(int progress) throws InterruptedException {
    Thread.sleep(20);
    publish(new Progress(ProgressType.FILE, progress + 1));
  }

  protected String getDoneMessage() {
    String message;
    try {
      message = isCancelled() ? "Cancelled" : get();
    } catch (InterruptedException ex) {
      message = "Interrupted";
      Thread.currentThread().interrupt();
    } catch (ExecutionException ex) {
      message = "ExecutionException";
    }
    return message;
  }
}

// public final class MainPanel extends JPanel {
//   private final JTextArea area = new JTextArea();
//   private final JPanel statusPanel = new JPanel(new BorderLayout());
//   private final JButton runButton = new JButton(new RunAction());
//   private final JButton cancelButton = new JButton(new CancelAction());
//   private SwingWorker<String, String> worker;
//
//   public MainPanel() {
//     super(new BorderLayout(5, 5));
//     area.setEditable(false);
//     Box box = Box.createHorizontalBox();
//     box.add(Box.createHorizontalGlue());
//     box.add(runButton);
//     box.add(Box.createHorizontalStrut(2));
//     box.add(cancelButton);
//     add(new JScrollPane(area));
//     add(box, BorderLayout.NORTH);
//     add(statusPanel, BorderLayout.SOUTH);
//     setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//     setPreferredSize(new Dimension(320, 240));
//   }
//
//   class RunAction extends AbstractAction {
//     protected RunAction() {
//       super("run");
//     }
//
//     @Override public void actionPerformed(ActionEvent e) {
//       // System.out.println("actionPerformed() is EDT?: " + EventQueue.isDispatchThread());
//       JProgressBar bar1 = new JProgressBar();
//       JProgressBar bar2 = new JProgressBar();
//       runButton.setEnabled(false);
//       cancelButton.setEnabled(true);
//       statusPanel.removeAll();
//       statusPanel.add(bar1, BorderLayout.NORTH);
//       statusPanel.add(bar2, BorderLayout.SOUTH);
//       statusPanel.revalidate();
//       // bar1.setIndeterminate(true);
//
//       worker = new SwingWorker<String, String>() {
//         private final Random r = new Random();
//
//         @Override protected String doInBackground() {
//           // System.out.println("EDT?: " + EventQueue.isDispatchThread());
//           int current = 0;
//           int lengthOfTask = 12; // fileList.size();
//           publish("Length Of Task: " + lengthOfTask);
//           publish("\n------------------------------\n");
//           setProgress(0);
//           while (current < lengthOfTask && !isCancelled()) {
//             if (!bar1.isDisplayable()) {
//               return "Disposed";
//             }
//             try {
//               convertFileToSomething();
//             } catch (InterruptedException ex) {
//               return "Interrupted";
//             }
//             publish("*");
//             setProgress(100 * current / lengthOfTask);
//             current++;
//           }
//           publish("\n");
//           return "Done";
//         }
//
//         private void convertFileToSomething() throws InterruptedException {
//           int current = 0;
//           int lengthOfTask = 10 + r.nextInt(50); // long lengthOfTask = file.length();
//           while (current <= lengthOfTask && !isCancelled()) {
//             int iv = 100 * current / lengthOfTask;
//             Thread.sleep(20);
//             firePropertyChange("progress2", iv, iv + 1);
//             current++;
//           }
//         }
//
//         @Override protected void process(List<String> chunks) {
//           // System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
//           for (String message : chunks) {
//             appendLine(message);
//           }
//         }
//
//         @Override protected void done() {
//           // System.out.println("done() is EDT?: " + EventQueue.isDispatchThread());
//           runButton.setEnabled(true);
//           cancelButton.setEnabled(false);
//           statusPanel.remove(bar1);
//           statusPanel.remove(bar2);
//           statusPanel.revalidate();
//           String text = null;
//           if (isCancelled()) {
//             text = "Cancelled";
//           } else {
//             try {
//               text = get();
//             } catch (Exception ex) {
//               text = "Exception";
//             }
//           }
//           // System.out.println(text);
//           appendLine(text);
//         }
//       };
//       worker.addPropertyChangeListener(new MainProgressListener(bar1));
//       worker.addPropertyChangeListener(new SubProgressListener(bar2));
//       worker.execute();
//     }
//   }
//
//   class CancelAction extends AbstractAction {
//     protected CancelAction() {
//       super("cancel");
//     }
//
//     @Override public void actionPerformed(ActionEvent e) {
//       if (Objects.nonNull(worker) && !worker.isDone()) {
//         worker.cancel(true);
//       }
//       // worker = null;
//     }
//   }
//
//   private void appendLine(String str) {
//     area.append(str);
//     area.setCaretPosition(area.getDocument().getLength());
//   }
//
//   public static void main(String[] args) {
//     EventQueue.invokeLater(MainPanel::createAndShowGui);
//   }
//
//   private static void createAndShowGui() {
//     try {
//       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//     } catch (UnsupportedLookAndFeelException ignored) {
//       Toolkit.getDefaultToolkit().beep();
//     } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
//       Logger.getGlobal().severe(ex::getMessage);
//       return;
//     }
//     JFrame frame = new JFrame("@title@");
//     // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//     frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//     frame.getContentPane().add(new MainPanel());
//     frame.pack();
//     frame.setLocationRelativeTo(null);
//     frame.setVisible(true);
//   }
// }
//
// class MainProgressListener implements PropertyChangeListener {
//   protected final JProgressBar progressBar;
//   protected MainProgressListener(JProgressBar progressBar) {
//     this.progressBar = progressBar;
//     this.progressBar.setValue(0);
//   }
//
//   @Override public void propertyChange(PropertyChangeEvent e) {
//     String strPropertyName = e.getPropertyName();
//     if ("progress".equals(strPropertyName)) {
//       progressBar.setIndeterminate(false);
//       int progress = (Integer) e.getNewValue();
//       progressBar.setValue(progress);
//     }
//   }
// }
//
// class SubProgressListener implements PropertyChangeListener {
//   private final JProgressBar progressBar;
//   protected SubProgressListener(JProgressBar progressBar) {
//     this.progressBar = progressBar;
//     this.progressBar.setValue(0);
//   }
//
//   @Override public void propertyChange(PropertyChangeEvent e) {
//     String strPropertyName = e.getPropertyName();
//     if ("progress2".equals(strPropertyName)) {
//       int progress = (Integer) e.getNewValue();
//       progressBar.setValue(progress);
//     }
//   }
// }
