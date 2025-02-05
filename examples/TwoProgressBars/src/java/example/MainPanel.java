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
      // worker = null;
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

  public void executeWorker() {
    if (Objects.isNull(worker)) {
      worker = new ProgressTask();
    }
    worker.execute();
  }

  public void initStatusPanel(boolean start) {
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

  private final class ProgressTask extends BackgroundTask {
    @Override protected void process(List<Progress> chunks) {
      if (isDisplayable() && !isCancelled()) {
        chunks.forEach(MainPanel.this::updateProgress);
      } else {
        cancel(true);
      }
    }

    @Override protected void done() {
      if (!isDisplayable()) {
        cancel(true);
        return;
      }
      initStatusPanel(false);
      try {
        appendLine(isCancelled() ? "\nCancelled\n" : get() + "\n");
      } catch (InterruptedException ex) {
        appendLine("\nInterrupted\n");
        Thread.currentThread().interrupt();
      } catch (ExecutionException ex) {
        appendLine("\nException\n");
      }
    }
  }

  public void updateProgress(Progress s) {
    switch (s.getComponentType()) {
      case TOTAL:
        bar1.setValue((Integer) s.getValue());
        break;
      case FILE:
        bar2.setValue((Integer) s.getValue());
        break;
      case LOG:
        area.append(Objects.toString(s.getValue()));
        break;
      default:
        throw new AssertionError("Unknown Progress");
    }
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

enum ComponentType {
  TOTAL, FILE, LOG
}

class Progress {
  private final Object value;
  private final ComponentType componentType;

  protected Progress(ComponentType componentType, Object value) {
    this.componentType = componentType;
    this.value = value;
  }

  public Object getValue() {
    return value;
  }

  public ComponentType getComponentType() {
    return componentType;
  }
}

class BackgroundTask extends SwingWorker<String, Progress> {
  private final Random rnd = new Random();

  @Override protected String doInBackground() throws InterruptedException {
    // System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
    int current = 0;
    int numOfFiles = 12; // fileList.size();
    publish(new Progress(ComponentType.LOG, "Total number of files: " + numOfFiles));
    publish(new Progress(ComponentType.LOG, "\n------------------------------\n"));
    while (current < numOfFiles && !isCancelled()) {
      convertFileToSomething(100 * current / numOfFiles);
      current++;
    }
    publish(new Progress(ComponentType.LOG, "\n"));
    return "Done";
  }

  protected void convertFileToSomething(int iv) throws InterruptedException {
    int current = 0;
    int lengthOfFile = 10 + rnd.nextInt(50); // long lengthOfFile = file.length();
    publish(new Progress(ComponentType.LOG, "*"));
    publish(new Progress(ComponentType.TOTAL, iv));
    while (current <= lengthOfFile && !isCancelled()) {
      doSomething(100 * current / lengthOfFile);
      current++;
    }
  }

  protected void doSomething(int iv) throws InterruptedException {
    publish(new Progress(ComponentType.FILE, iv + 1));
    Thread.sleep(20);
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
//         @Override protected String doInBackground() {
//           // System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
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
//       ex.printStackTrace();
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
