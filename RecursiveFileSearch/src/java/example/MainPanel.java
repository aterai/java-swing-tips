// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JComboBox<String> dirCombo = new JComboBox<>();
  private final JFileChooser fileChooser = new JFileChooser();
  private final JTextArea textArea = new JTextArea();
  private final JProgressBar progress = new JProgressBar();
  private final JPanel statusPanel = new JPanel(new BorderLayout());
  private final JButton searchButton = new JButton("Search");
  private final JButton cancelButton = new JButton("Cancel");
  private final JButton chooseButton = new JButton("Choose...");
  private transient SwingWorker<String, Message> worker;

  private MainPanel() {
    super(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement(System.getProperty("user.dir"));
    dirCombo.setModel(model);
    dirCombo.setFocusable(false);
    textArea.setEditable(false);
    statusPanel.add(progress);
    statusPanel.setVisible(false);

    searchButton.addActionListener(e -> searchActionPerformed());
    cancelButton.addActionListener(e -> cancelActionPerformed());
    chooseButton.addActionListener(e -> chooseActionPerformed());

    JPanel box1 = new JPanel(new BorderLayout(5, 5));
    box1.add(new JLabel("Search folder:"), BorderLayout.WEST);
    box1.add(dirCombo);
    box1.add(chooseButton, BorderLayout.EAST);

    Box box2 = Box.createHorizontalBox();
    box2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box2.add(Box.createHorizontalGlue());
    box2.add(searchButton);
    box2.add(Box.createHorizontalStrut(2));
    box2.add(cancelButton);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(box1, BorderLayout.NORTH);
    panel.add(box2, BorderLayout.SOUTH);

    add(new JScrollPane(textArea));
    add(panel, BorderLayout.NORTH);
    add(statusPanel, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void searchActionPerformed() {
    updateComponentStatus(true);
    File dir = new File(dirCombo.getItemAt(dirCombo.getSelectedIndex()));
    if (dir.exists()) {
      executeWorker(dir);
    } else {
      textArea.setText("The directory does not exist.");
    }
  }

  private void cancelActionPerformed() {
    if (Objects.nonNull(worker) && !worker.isDone()) {
      worker.cancel(true);
    }
    // worker = null;
  }

  private void chooseActionPerformed() {
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setSelectedFile(new File(Objects.toString(dirCombo.getEditor().getItem())));
    Component c = dirCombo.getRootPane();
    int fcSelected = fileChooser.showOpenDialog(c);
    if (fcSelected == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      if (Objects.isNull(file) || !file.isDirectory()) {
        textArea.setText("Please select directory.");
      } else {
        String path = file.getAbsolutePath();
        textArea.setText(path);
        addItem(dirCombo, path, 4);
        repaint();
      }
    } else if (fcSelected == JFileChooser.CANCEL_OPTION) {
      textArea.setText("JFileChooser cancelled.");
    } else {
      UIManager.getLookAndFeel().provideErrorFeedback(c);
      textArea.setText("JFileChooser error.");
    }
  }

  private final class FileSearchTask extends RecursiveFileSearchTask {
    protected FileSearchTask(File dir) {
      super(dir);
    }

    @Override protected void process(List<Message> chunks) {
      // System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
      if (isDisplayable() && !isCancelled()) {
        chunks.forEach(MainPanel.this::updateMessage);
      } else {
        // System.out.println("process: DISPOSE_ON_CLOSE");
        cancel(true);
      }
    }

    @Override protected void done() {
      // System.out.println("done() is EDT?: " + EventQueue.isDispatchThread());
      if (!isDisplayable()) {
        // System.out.println("done: DISPOSE_ON_CLOSE");
        cancel(true);
        return;
      }
      updateComponentStatus(false);
      String text;
      if (isCancelled()) {
        text = "Cancelled";
      } else {
        try {
          text = get();
        } catch (InterruptedException | ExecutionException ex) {
          text = "Interrupted";
          Thread.currentThread().interrupt();
        }
      }
      appendLine("----------------");
      appendLine(text);
    }
  }

  public void updateComponentStatus(boolean start) {
    if (start) {
      addItem(dirCombo, Objects.toString(dirCombo.getEditor().getItem()), 4);
      statusPanel.setVisible(true);
      dirCombo.setEnabled(false);
      chooseButton.setEnabled(false);
      searchButton.setEnabled(false);
      cancelButton.setEnabled(true);
      progress.setIndeterminate(true);
      textArea.setText("");
    } else {
      dirCombo.setEnabled(true);
      chooseButton.setEnabled(true);
      searchButton.setEnabled(true);
      cancelButton.setEnabled(false);
      statusPanel.setVisible(false);
    }
  }

  public static void addItem(JComboBox<String> dirCombo, String str, int max) {
    if (Objects.isNull(str) || str.isEmpty()) {
      return;
    }
    dirCombo.setVisible(false);
    DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) dirCombo.getModel();
    model.removeElement(str);
    model.insertElementAt(str, 0);
    if (model.getSize() > max) {
      model.removeElementAt(max);
    }
    dirCombo.setSelectedIndex(0);
    dirCombo.setVisible(true);
  }

  public void executeWorker(File dir) {
    worker = new FileSearchTask(dir);
    worker.addPropertyChangeListener(new ProgressListener(progress));
    worker.execute();
  }

  private void updateMessage(Message m) {
    if (m.isAppend()) {
      appendLine(m.getText());
    } else {
      textArea.setText(m.getText() + "\n");
    }
  }

  public void appendLine(String str) {
    textArea.append(str + "\n");
    textArea.setCaretPosition(textArea.getDocument().getLength());
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

// class RecursiveFileSearchTask extends SwingWorker<String, Message> {
//   private int counter;
//   private final File dir;
//   protected RecursiveFileSearchTask(File dir) {
//     super();
//     this.dir = dir;
//   }
//
//   @Override protected String doInBackground() {
//     if (Objects.isNull(dir) || !dir.exists()) {
//       publish(new Message("The directory does not exist.", true));
//       return "Error";
//     }
//     List<File> list = new ArrayList<>();
//     // ArrayList<Path> list = new ArrayList<>();
//     try {
//       counter = 0;
//       recursiveSearch(dir, list);
//     } catch (InterruptedException ex) {
//       // recursiveSearch(dir.toPath(), list);
//       // } catch (Exception ex) {
//       publish(new Message("The search was canceled", true));
//       return "Interrupted1";
//     }
//     firePropertyChange("clear-JTextArea", "", "");
//
//     int lengthOfTask = list.size();
//     publish(new Message("Length Of Task: " + lengthOfTask, false));
//     publish(new Message("----------------", true));
//
//     try {
//       int current = 0;
//       while (current < lengthOfTask && !isCancelled()) {
//         // if (!progress.isDisplayable()) {
//         //   return "Disposed";
//         // }
//         File file = list.get(current);
//         // Path path = list.get(current);
//         Thread.sleep(50);
//         setProgress(100 * current / lengthOfTask);
//         current++;
//         String path = file.getAbsolutePath();
//         publish(new Message(current + "/" + lengthOfTask + ", " + path, true));
//       }
//     } catch (InterruptedException ex) {
//       return "Interrupted";
//     }
//     return "Done";
//   }
//
//   private void recursiveSearch(File dir, List<File> list) throws InterruptedException {
//     // System.out.println("recursiveSearch() is EDT?: " + EventQueue.isDispatchThread());
//     for (String name : dir.list()) {
//       if (Thread.interrupted()) {
//         throw new InterruptedException();
//       }
//       File dir = new File(dir, name);
//       if (dir.isDirectory()) {
//         recursiveSearch(dir, list);
//       } else {
//         counter++;
//         if (counter % 100 == 0) {
//           publish(new Message("Results:" + counter + "\n", false));
//         }
//         list.add(dir);
//       }
//     }
//   }
// }

class RecursiveFileSearchTask extends SwingWorker<String, Message> {
  private int counter;
  private final File dir;

  protected RecursiveFileSearchTask(File dir) {
    super();
    this.dir = dir;
  }

  @Override protected String doInBackground() throws InterruptedException {
    // if (Objects.isNull(dir) || !dir.exists()) {
    //   publish(new Message("The directory does not exist.", true));
    //   return "Error";
    // }
    String msg;
    List<Path> list = getPathList(dir.toPath());
    if (list.isEmpty()) {
      msg = "Interrupted1";
    } else {
      firePropertyChange("clear-JTextArea", "", "");
      int lengthOfTask = list.size();
      publish(new Message("Length Of Task: " + lengthOfTask, false));
      publish(new Message("----------------", true));
      int idx = 0;
      while (idx < lengthOfTask && !isCancelled()) {
        doSomething(list, idx);
        idx++;
      }
      msg = "Done";
    }
    return msg;
  }

  private List<Path> getPathList(Path dirPath) {
    List<Path> list = new ArrayList<>();
    try {
      counter = 0;
      recursiveSearch(dirPath, list);
    } catch (IOException ex) {
      publish(new Message("The search was canceled", true));
      list.clear();
    }
    return list;
  }

  protected void doSomething(List<Path> list, int idx) throws InterruptedException {
    int lengthOfTask = list.size();
    setProgress(100 * idx / lengthOfTask);
    Thread.sleep(10);
    Path path = list.get(idx);
    int current = idx + 1;
    publish(new Message(current + "/" + lengthOfTask + ", " + path, true));
  }

  // Walking the File Tree (The Java™ Tutorials > Essential Classes > Basic I/O)
  // https://docs.oracle.com/javase/tutorial/essential/io/walk.html
  private void recursiveSearch(Path dirPath, List<Path> list) throws IOException {
    Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
      @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (Thread.interrupted()) {
          throw new IOException();
        }
        if (attrs.isRegularFile()) {
          counter++;
          if (counter % 100 == 0) {
            publish(new Message("Results:" + counter + "\n", false));
          }
          list.add(file);
        }
        return FileVisitResult.CONTINUE;
      }
    });
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

class Message {
  private final String text;
  private final boolean append;

  protected Message(String text, boolean append) {
    this.text = text;
    this.append = append;
  }

  public String getText() {
    return text;
  }

  public boolean isAppend() {
    return append;
  }
}
