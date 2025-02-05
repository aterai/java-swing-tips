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
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public final class MainPanel extends JPanel {
  private static final String PAUSE = "pause";
  private static final String RESUME = "resume";
  private final JTextArea area = new JTextArea();
  private final JPanel statusPanel = new JPanel(new BorderLayout());
  private final JButton runButton = new JButton("run");
  private final JButton cancelButton = new JButton("cancel");
  private final JButton pauseButton = new JButton(PAUSE);
  private final JProgressBar bar1 = new JProgressBar();
  private final JProgressBar bar2 = new JProgressBar();
  private transient BackgroundTask worker;

  private MainPanel() {
    super(new BorderLayout(5, 5));
    area.setEditable(false);

    runButton.addActionListener(e -> {
      // System.out.println("actionPerformed() is EDT?: " + EventQueue.isDispatchThread());
      runButton.setEnabled(false);
      cancelButton.setEnabled(true);
      pauseButton.setEnabled(true);
      bar1.setValue(0);
      bar2.setValue(0);
      statusPanel.add(bar1, BorderLayout.NORTH);
      statusPanel.add(bar2, BorderLayout.SOUTH);
      statusPanel.revalidate();
      // bar1.setIndeterminate(true);
      worker = new ProgressTask();
      worker.execute();
    });

    pauseButton.setEnabled(false);
    pauseButton.addActionListener(e -> {
      JButton b = (JButton) e.getSource();
      // String pause = (String) getValue(Action.NAME);
      if (Objects.nonNull(worker)) {
        if (worker.isCancelled() || worker.isPaused) {
          b.setText(PAUSE);
        } else {
          b.setText(RESUME);
        }
        worker.isPaused ^= true;
      } else {
        b.setText(PAUSE);
      }
    });

    cancelButton.setEnabled(false);
    cancelButton.addActionListener(e -> {
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
      }
      // worker = null;
      pauseButton.setText(PAUSE);
      pauseButton.setEnabled(false);
    });

    Component box = makeRightAlignBox(Arrays.asList(pauseButton, cancelButton, runButton), 80, 5);
    add(new JScrollPane(area));
    add(box, BorderLayout.NORTH);
    add(statusPanel, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private final class ProgressTask extends BackgroundTask {
    @Override protected void process(List<Progress> chunks) {
      // System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
      if (isDisplayable() && !isCancelled()) {
        chunks.forEach(MainPanel.this::updateProgress);
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
      // System.out.println("done() is EDT?: " + EventQueue.isDispatchThread());
      updateComponentDone();
      String message;
      try {
        message = isCancelled() ? "Cancelled" : get();
      } catch (InterruptedException ex) {
        message = "Interrupted";
        Thread.currentThread().interrupt();
      } catch (ExecutionException ex) {
        message = "ExecutionException";
      }
      appendLine(String.format("%n%s%n", message));
    }
  }

  public void updateComponentDone() {
    runButton.requestFocusInWindow();
    runButton.setEnabled(true);
    cancelButton.setEnabled(false);
    pauseButton.setEnabled(false);
    statusPanel.removeAll();
    statusPanel.revalidate();
  }

  public void updateProgress(Progress s) {
    switch (s.getComponent()) {
      case TOTAL:
        bar1.setValue((Integer) s.getValue());
        break;
      case FILE:
        bar2.setValue((Integer) s.getValue());
        break;
      case LOG:
        area.append(Objects.toString(s.getValue()));
        break;
      case PAUSE:
        textProgress((Boolean) s.getValue());
        break;
      default:
        throw new AssertionError("Unknown Progress");
    }
  }

  public void textProgress(boolean append) {
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

  // @see https://ateraimemo.com/Swing/ButtonWidth.html
  public static Component makeRightAlignBox(List<? extends Component> list, int width, int gap) {
    SpringLayout layout = new SpringLayout();
    JPanel p = new JPanel(layout) {
      @Override public Dimension getPreferredSize() {
        int maxHeight = list.stream()
            .map(c -> c.getPreferredSize().height)
            .max(Integer::compare)
            .orElse(0);
        return new Dimension(width * list.size() + gap + gap, maxHeight + gap + gap);
      }
    };
    Spring x = layout.getConstraint(SpringLayout.WIDTH, p);
    Spring y = Spring.constant(gap);
    Spring g = Spring.minus(Spring.constant(gap));
    Spring w = Spring.constant(width);
    for (Component b : list) {
      SpringLayout.Constraints constraints = layout.getConstraints(b);
      x = Spring.sum(x, g);
      constraints.setConstraint(SpringLayout.EAST, x);
      constraints.setY(y);
      constraints.setWidth(w);
      p.add(b);
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

enum ProgressType {
  TOTAL, FILE, LOG, PAUSE
}

class Progress {
  private final Object value;
  private final ProgressType component;

  protected Progress(ProgressType component, Object value) {
    this.component = component;
    this.value = value;
  }

  public Object getValue() {
    return value;
  }

  public ProgressType getComponent() {
    return component;
  }
}

class BackgroundTask extends SwingWorker<String, Progress> {
  protected boolean isPaused;
  private final Random rnd = new Random();

  @Override protected String doInBackground() throws InterruptedException {
    // System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
    int current = 0;
    int lengthOfTask = 12; // fileList.size();
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
    int lengthOfTask = 10 + rnd.nextInt(50); // long lengthOfTask = file.length();

    publish(new Progress(ProgressType.TOTAL, progress));
    publish(new Progress(ProgressType.LOG, "*"));

    while (current <= lengthOfTask && !isCancelled()) {
      if (isPaused) {
        pause(blinking);
        blinking ^= true;
        continue;
      }
      doSomething(100 * current / lengthOfTask);
      current++;
    }
  }

  private void pause(boolean blinking) throws InterruptedException {
    Thread.sleep(500);
    publish(new Progress(ProgressType.PAUSE, blinking));
  }

  protected void doSomething(int progress) throws InterruptedException {
    Thread.sleep(20);
    publish(new Progress(ProgressType.FILE, progress + 1));
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
//           int lengthOfTask = 10 + rnd.nextInt(50); // long lengthOfTask = file.length();
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
