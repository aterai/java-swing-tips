// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea area = new JTextArea();
  private final JPanel statusPanel = new JPanel(new BorderLayout());
  private final JButton runButton = new JButton("run");
  private final JButton cancelButton = new JButton("cancel");
  private final JProgressBar bar1 = new JProgressBar();
  private final JProgressBar bar2 = new JProgressBar();
  private transient SwingWorker<String, Progress> worker;

  private MainPanel() {
    super(new BorderLayout(5, 5));
    area.setEditable(false);
    runButton.addActionListener(e -> {
      initStatusPanel(true);
      executeWorker();
    });
    cancelButton.addActionListener(e -> {
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
      }
      worker = null;
    });
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(runButton);
    box.add(Box.createHorizontalStrut(2));
    box.add(cancelButton);
    add(new JScrollPane(area));
    add(box, BorderLayout.NORTH);
    add(statusPanel, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  protected void executeWorker() {
    if (Objects.isNull(worker)) {
      worker = new ProgressTask();
    }
    worker.execute();
  }

  protected void initStatusPanel(boolean start) {
    if (start) {
      runButton.setEnabled(false);
      cancelButton.setEnabled(true);
      bar1.setValue(0);
      bar2.setValue(0);
      statusPanel.add(bar1, BorderLayout.NORTH);
      statusPanel.add(bar2, BorderLayout.SOUTH);
    } else {
      runButton.setEnabled(true);
      cancelButton.setEnabled(false);
      statusPanel.removeAll();
    }
    statusPanel.revalidate();
  }

  private class ProgressTask extends BackgroundTask {
    @Override protected void process(List<Progress> chunks) {
      if (isCancelled()) {
        return;
      }
      if (!isDisplayable()) {
        cancel(true);
        return;
      }
      processChunks(chunks);
    }

    @Override public void done() {
      if (!isDisplayable()) {
        cancel(true);
        return;
      }
      initStatusPanel(false);
      try {
        appendLine(isCancelled() ? "\nCancelled\n" : get() + "\n");
      } catch (InterruptedException | ExecutionException ex) {
        appendLine("\nInterrupted\n");
      }
    }
  }

  protected void processChunks(List<Progress> chunks) {
    chunks.forEach(s -> {
      switch (s.componentType) {
        case TOTAL:
          bar1.setValue((Integer) s.value);
          break;
        case FILE:
          bar2.setValue((Integer) s.value);
          break;
        case LOG:
          area.append(Objects.toString(s.value));
          break;
        default:
          throw new AssertionError("Unknown Progress");
      }
    });
  }

  protected void appendLine(String str) {
    area.append(str);
    area.setCaretPosition(area.getDocument().getLength());
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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

enum ComponentType { TOTAL, FILE, LOG }

class Progress {
  public final Object value;
  public final ComponentType componentType;

  protected Progress(ComponentType componentType, Object value) {
    this.componentType = componentType;
    this.value = value;
  }
}

class BackgroundTask extends SwingWorker<String, Progress> {
  private final Random rnd = new Random();

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  @Override public String doInBackground() {
    // System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
    int current = 0;
    int lengthOfTask = 12; // filelist.size();
    publish(new Progress(ComponentType.LOG, "Length Of Task: " + lengthOfTask));
    publish(new Progress(ComponentType.LOG, "\n------------------------------\n"));
    while (current < lengthOfTask && !isCancelled()) {
      publish(new Progress(ComponentType.LOG, "*"));
      publish(new Progress(ComponentType.TOTAL, 100 * current / lengthOfTask));
      try {
        convertFileToSomething();
      } catch (InterruptedException ex) {
        return "Interrupted";
      }
      current++;
    }
    publish(new Progress(ComponentType.LOG, "\n"));
    return "Done";
  }

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private void convertFileToSomething() throws InterruptedException {
    int current = 0;
    int lengthOfTask = 10 + rnd.nextInt(50); // long lengthOfTask = file.length();
    while (current <= lengthOfTask && !isCancelled()) {
      int iv = 100 * current / lengthOfTask;
      Thread.sleep(20); // dummy
      publish(new Progress(ComponentType.FILE, iv + 1));
      current++;
    }
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
//         @Override public String doInBackground() {
//           // System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
//           int current = 0;
//           int lengthOfTask = 12; // filelist.size();
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
//         private final Random r = new Random();
//         private void convertFileToSomething() throws InterruptedException {
//           int current = 0;
//           int lengthOfTask = 10 + r.nextInt(50); // long lengthOfTask = file.length();
//           while (current <= lengthOfTask && !isCancelled()) {
//             int iv = 100 * current / lengthOfTask;
//             Thread.sleep(20); // dummy
//             firePropertyChange("progress2", iv, iv + 1);
//             current++;
//           }
//         }
//         @Override protected void process(List<String> chunks) {
//           // System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
//           for (String message: chunks) {
//             appendLine(message);
//           }
//         }
//         @Override public void done() {
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
//   class CancelAction extends AbstractAction {
//     protected CancelAction() {
//       super("cancel");
//     }
//     @Override public void actionPerformed(ActionEvent e) {
//       if (Objects.nonNull(worker) && !worker.isDone()) {
//         worker.cancel(true);
//       }
//       worker = null;
//     }
//   }
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
//     } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//       ex.printStackTrace();
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
