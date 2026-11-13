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
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int MAX_HISTORY = 4;
  private final JComboBox<String> dirCombo = new JComboBox<>();
  private final JFileChooser fileChooser = new JFileChooser();
  private final JTextArea textArea = new JTextArea();
  private final JProgressBar progressBar = new JProgressBar();
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
    statusPanel.add(progressBar);
    statusPanel.setVisible(false);
    cancelButton.setEnabled(false);

    searchButton.addActionListener(e -> searchActionPerformed());
    cancelButton.addActionListener(e -> cancelActionPerformed());
    chooseButton.addActionListener(e -> chooseActionPerformed());

    JPanel dirPanel = new JPanel(new BorderLayout(5, 5));
    dirPanel.add(new JLabel("Search folder:"), BorderLayout.WEST);
    dirPanel.add(dirCombo);
    dirPanel.add(chooseButton, BorderLayout.EAST);

    Box buttonBox = Box.createHorizontalBox();
    buttonBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    buttonBox.add(Box.createHorizontalGlue());
    buttonBox.add(searchButton);
    buttonBox.add(Box.createHorizontalStrut(2));
    buttonBox.add(cancelButton);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(dirPanel, BorderLayout.NORTH);
    panel.add(buttonBox, BorderLayout.SOUTH);

    add(new JScrollPane(textArea));
    add(panel, BorderLayout.NORTH);
    add(statusPanel, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private String getSelectedDirectory() {
    return Objects.toString(dirCombo.getSelectedItem(), "");
  }

  private void searchActionPerformed() {
    File dir = new File(getSelectedDirectory());
    // Check the directory before disabling the components,
    // otherwise they will never be re-enabled because no worker is executed
    if (dir.isDirectory()) {
      updateComponentStatus(true);
      executeWorker(dir.toPath());
    } else {
      textArea.setText("The directory does not exist.");
    }
  }

  private void cancelActionPerformed() {
    if (Objects.nonNull(worker) && !worker.isDone()) {
      worker.cancel(true);
    }
  }

  private void chooseActionPerformed() {
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setSelectedFile(new File(getSelectedDirectory()));
    Component c = dirCombo.getRootPane();
    int result = fileChooser.showOpenDialog(c);
    if (result == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      if (Objects.isNull(file) || !file.isDirectory()) {
        textArea.setText("Please select directory.");
      } else {
        String path = file.getAbsolutePath();
        textArea.setText(path);
        addItem(dirCombo, path, MAX_HISTORY);
      }
    } else if (result == JFileChooser.CANCEL_OPTION) {
      textArea.setText("JFileChooser cancelled.");
    } else {
      UIManager.getLookAndFeel().provideErrorFeedback(c);
      textArea.setText("JFileChooser error.");
    }
  }

  private void updateComponentStatus(boolean start) {
    if (start) {
      addItem(dirCombo, getSelectedDirectory(), MAX_HISTORY);
      progressBar.setIndeterminate(true);
      textArea.setText("");
    }
    statusPanel.setVisible(start);
    dirCombo.setEnabled(!start);
    chooseButton.setEnabled(!start);
    searchButton.setEnabled(!start);
    cancelButton.setEnabled(start);
  }

  private static void addItem(JComboBox<String> combo, String str, int max) {
    ComboBoxModel<String> m = combo.getModel();
    if (str != null && !str.isEmpty() && m instanceof DefaultComboBoxModel) {
      DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) m;
      combo.setVisible(false);
      model.removeElement(str);
      model.insertElementAt(str, 0);
      if (model.getSize() > max) {
        model.removeElementAt(max);
      }
      combo.setSelectedIndex(0);
      combo.setVisible(true);
    }
  }

  private void executeWorker(Path dir) {
    worker = new RecursiveFileSearchTask(dir) {
      @Override protected void process(List<Message> chunks) {
        if (isDisplayable() && !isCancelled()) {
          chunks.forEach(MainPanel.this::updateMessage);
        } else {
          cancel(true);
        }
      }

      @Override protected void done() {
        if (isDisplayable()) {
          updateComponentStatus(false);
          appendLine("----------------");
          appendLine(getDoneMessage());
        }
      }
    };
    worker.addPropertyChangeListener(new ProgressListener(progressBar));
    worker.execute();
  }

  private void updateMessage(Message msg) {
    if (msg.isAppend()) {
      appendLine(msg.getText());
    } else {
      textArea.setText(msg.getText() + "\n");
    }
  }

  private void appendLine(String str) {
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
      Logger.getGlobal().severe(ex::getMessage);
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class RecursiveFileSearchTask extends SwingWorker<String, Message> {
  private final Path dir;

  protected RecursiveFileSearchTask(Path dir) {
    super();
    this.dir = dir;
  }

  @Override protected String doInBackground() throws IOException, InterruptedException {
    List<Path> list = searchFiles(dir);
    int lengthOfTask = list.size();
    publish(new Message("Length Of Task: " + lengthOfTask, false));
    publish(new Message("----------------", true));
    for (int i = 0; i < lengthOfTask && !isCancelled(); i++) {
      processFile(list, i);
    }
    return "Done";
  }

  protected void processFile(List<Path> list, int idx) throws InterruptedException {
    Thread.sleep(10);
    int lengthOfTask = list.size();
    int current = idx + 1;
    publish(new Message(current + "/" + lengthOfTask + ", " + list.get(idx), true));
    setProgress(100 * current / lengthOfTask);
  }

  // Walking the File Tree (The Java™ Tutorials > Essential Classes > Basic I/O)
  // https://docs.oracle.com/javase/tutorial/essential/io/walk.html
  private List<Path> searchFiles(Path start) throws IOException {
    List<Path> list = new ArrayList<>();
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (attrs.isRegularFile()) {
          list.add(file);
          if (list.size() % 100 == 0) {
            publish(new Message("Results: " + list.size(), false));
          }
        }
        return isCancelled() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
      }

      // Skip unreadable files or directories (e.g. AccessDeniedException)
      // instead of aborting the whole search
      @Override public FileVisitResult visitFileFailed(Path file, IOException ex) {
        return isCancelled() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
      }
    });
    return list;
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
  private final JProgressBar progressBar;

  protected ProgressListener(JProgressBar progressBar) {
    this.progressBar = progressBar;
    this.progressBar.setValue(0);
  }

  @Override public void propertyChange(PropertyChangeEvent e) {
    boolean isProgress = "progress".equals(e.getPropertyName());
    if (isProgress) {
      progressBar.setIndeterminate(false);
      progressBar.setValue((Integer) e.getNewValue());
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
