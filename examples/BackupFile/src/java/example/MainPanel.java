// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public final class MainPanel extends JPanel {
  private static final String FILE_NAME = "example.txt";
  private final SpinnerNumberModel keepModel = new SpinnerNumberModel(0, 0, 6, 1);
  private final SpinnerNumberModel rotationModel = new SpinnerNumberModel(2, 0, 6, 1);
  private final JSpinner keepSpinner = new JSpinner(keepModel);
  private final JSpinner rotationSpinner = new JSpinner(rotationModel);
  private final JLabel totalLabel = new JLabel("2", SwingConstants.RIGHT);
  private final LoggingTextPane log = new LoggingTextPane();

  private MainPanel() {
    super(new BorderLayout());
    JButton createButton = new JButton("Create new " + FILE_NAME);
    createButton.addActionListener(e -> {
      File file = new File(System.getProperty("java.io.tmpdir"), FILE_NAME);
      int keepCount = keepModel.getNumber().intValue();
      int rotationCount = rotationModel.getNumber().intValue();
      new BackupTask(file, keepCount, rotationCount).execute();
    });

    JButton clearButton = new JButton("clear");
    clearButton.addActionListener(e -> log.setText(""));

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    box.add(Box.createHorizontalGlue());
    box.add(createButton);
    box.add(Box.createHorizontalStrut(5));
    box.add(clearButton);

    JSpinner.NumberEditor keepEditor = new JSpinner.NumberEditor(keepSpinner, "0");
    keepEditor.getTextField().setEditable(false);
    keepSpinner.setEditor(keepEditor);

    JSpinner.NumberEditor rotationEditor = new JSpinner.NumberEditor(rotationSpinner, "0");
    rotationEditor.getTextField().setEditable(false);
    rotationSpinner.setEditor(rotationEditor);

    ChangeListener cl = e -> {
      int i = keepModel.getNumber().intValue() + rotationModel.getNumber().intValue();
      totalLabel.setText(Integer.toString(i));
    };
    keepModel.addChangeListener(cl);
    rotationModel.addChangeListener(cl);

    totalLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));

    JScrollPane scroll = new JScrollPane(log);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(25);

    add(createSettingsPanel(), BorderLayout.NORTH);
    add(scroll);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private Component createSettingsPanel() {
    JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
    panel.add(new JLabel("Number of backups to keep:", SwingConstants.RIGHT));
    panel.add(keepSpinner);
    panel.add(new JLabel("Number of backups to rotate:", SwingConstants.RIGHT));
    panel.add(rotationSpinner);
    panel.add(new JLabel("Total number of backups:", SwingConstants.RIGHT));
    panel.add(totalLabel);
    return panel;
  }

  private final class BackupTask extends BackgroundTask {
    private BackupTask(File file, int keepCount, int rotationCount) {
      super(file, keepCount, rotationCount);
    }

    @Override protected void process(List<Message> chunks) {
      if (isDisplayable() && !isCancelled()) {
        chunks.forEach(log::append);
      } else {
        cancel(true);
      }
    }

    @Override protected void done() {
      log.append(getDoneMessage());
      log.append(makeMessage("----------------------------------", MessageType.REGULAR));
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class LoggingTextPane extends JTextPane {
  protected LoggingTextPane() {
    super();
  }

  @Override public void updateUI() {
    super.updateUI();
    setEditable(false);
    EventQueue.invokeLater(this::initStyles);
  }

  public void append(Message msg) {
    StyledDocument doc = getStyledDocument();
    int len = doc.getLength();
    String txt = msg.getText() + "\n";
    Style style = doc.getStyle(msg.getType().toString());
    try {
      doc.insertString(len, txt, style);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }

  private void initStyles() {
    StyledDocument doc = getStyledDocument();
    // Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    Style def = doc.getStyle(StyleContext.DEFAULT_STYLE);
    // Style regular = doc.addStyle(MessageType.REGULAR.toString(), def);
    // StyleConstants.setForeground(error, Color.BLACK);
    // Style error = doc.addStyle(ERROR, regular);
    StyleConstants.setForeground(doc.addStyle(MessageType.ERROR.toString(), def), Color.RED);
    StyleConstants.setForeground(doc.addStyle(MessageType.DETAIL.toString(), def), Color.BLUE);
  }
}

enum MessageType {
  REGULAR, ERROR, DETAIL
}

class Message {
  private final String text;
  private final MessageType type;

  protected Message(String text, MessageType type) {
    this.text = text;
    this.type = type;
  }

  public String getText() {
    return text;
  }

  public MessageType getType() {
    return type;
  }
}

class BackgroundTask extends SwingWorker<File, Message> {
  private final File originalFile;
  private final int keepCount;
  private final int rotationCount;

  protected BackgroundTask(File file, int keepCount, int rotationCount) {
    super();
    this.originalFile = file;
    this.keepCount = keepCount;
    this.rotationCount = rotationCount;
  }

  @Override public File doInBackground() {
    File result = null;
    try {
      if (originalFile.exists()) {
        if (keepCount == 0 && rotationCount == 0) { // = backup off
          Files.delete(originalFile.toPath());
        } else {
          createBackup(originalFile);
        }
      }
      result = originalFile;
    } catch (IOException ex) {
      publish(makeMessage(ex.getMessage(), MessageType.ERROR));
    }
    return result;
  }

  private void createBackup(File file) throws IOException {
    Optional<File> unusedBackup = findUnusedBackupFile(file);
    if (unusedBackup.isPresent()) {
      renameFile(file, unusedBackup.get());
    } else {
      deleteOldestRotatingBackup(file);
      shiftBackupFileNumbers(file);
      renameFile(file, makeBackupFile(file, keepCount + rotationCount));
    }
  }

  private Optional<File> findUnusedBackupFile(File file) {
    return IntStream.rangeClosed(1, keepCount + rotationCount)
        .mapToObj(i -> makeBackupFile(file, i))
        .filter(f -> !f.exists())
        .findFirst();
  }

  private void deleteOldestRotatingBackup(File file) throws IOException {
    File oldest = makeBackupFile(file, keepCount + 1);
    publish(makeMessage("Delete old backup file", MessageType.REGULAR));
    publish(makeMessage("  del:" + oldest.getAbsolutePath(), MessageType.DETAIL));
    Files.delete(oldest.toPath());
  }

  private void shiftBackupFileNumbers(File file) throws IOException {
    for (int i = keepCount + 2; i <= keepCount + rotationCount; i++) {
      File src = makeBackupFile(file, i);
      File dst = makeBackupFile(file, i - 1);
      Path path = src.toPath();
      Files.move(path, path.resolveSibling(dst.getName()));
      publish(makeMessage("Update old backup file numbers", MessageType.REGULAR));
      publish(makeMessage("  " + src.getName() + " -> " + dst.getName(), MessageType.DETAIL));
    }
  }

  private void renameFile(File src, File dst) throws IOException {
    publish(makeMessage("Rename the original file", MessageType.REGULAR));
    String msg = String.format("  %s -> %s", src.getName(), dst.getName());
    publish(makeMessage(msg, MessageType.DETAIL));
    Path path = src.toPath();
    Files.move(path, path.resolveSibling(dst.getName()));
  }

  protected Message getDoneMessage() {
    Message msg;
    try {
      File newFile = get();
      if (Objects.isNull(newFile)) {
        msg = makeMessage("Failed to create backup file.", MessageType.ERROR);
      } else if (newFile.createNewFile()) {
        msg = makeMessage("Generated " + newFile.getName() + ".", MessageType.REGULAR);
      } else {
        msg = makeMessage("Failed to generate " + newFile.getName() + ".", MessageType.ERROR);
      }
    } catch (InterruptedException ex) {
      msg = makeMessage(ex.getMessage(), MessageType.ERROR);
      Thread.currentThread().interrupt();
    } catch (ExecutionException | IOException ex) {
      msg = makeMessage(ex.getMessage(), MessageType.ERROR);
    }
    return msg;
  }

  protected static Message makeMessage(String text, MessageType type) {
    return new Message(text, type);
  }

  private static String makeBackupFileName(String name, int num) {
    return String.format("%s.%d~", name, num);
  }

  private static File makeBackupFile(File file, int idx) {
    return new File(file.getParentFile(), makeBackupFileName(file.getName(), idx));
  }
}
