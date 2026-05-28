// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public static final String LOGGER_NAME = MethodHandles.lookup().lookupClass().getName();
  private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

  private MainPanel() {
    super(new BorderLayout());
    LOGGER.setUseParentHandlers(false);
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    LOGGER.addHandler(new TextAreaHandler(new TextAreaOutputStream(textArea)));

    JPanel p = new JPanel(new GridLayout(2, 1, 10, 10));
    p.add(createZipPanel());
    p.add(createUnzipPanel());
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private Component createZipPanel() {
    JTextField field = new JTextField(20);

    JButton selectButton = new JButton("select directory");
    selectButton.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int ret = fileChooser.showOpenDialog(selectButton.getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        field.setText(
            fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton zipButton = new JButton("zip");
    zipButton.addActionListener(e -> {
      String text = field.getText();
      Path path = Paths.get(text);

      // Files.notExists(path) noticeably poor performance in JDK 8
      if (!text.isEmpty() && path.toFile().exists()) {
        String name = path.getFileName() + ".zip";
        zip(path, path.resolveSibling(name));
      }
    });

    JPanel p = new JPanel(new BorderLayout(5, 2));
    p.setBorder(BorderFactory.createTitledBorder("Zip"));
    p.add(field);
    p.add(selectButton, BorderLayout.EAST);
    p.add(zipButton, BorderLayout.SOUTH);
    return p;
  }

  private void zip(Path srcDir, Path zipFile) {
    Component parent = getRootPane();
    try {
      if (canOverwrite(parent, zipFile, "Zip")) {
        ZipUtils.zip(srcDir, zipFile);
      }
    } catch (IOException ex) {
      LOGGER.info(() -> String.format("Cant zip! : %s", srcDir));
      UIManager.getLookAndFeel().provideErrorFeedback(parent);
    }
  }

  private Component createUnzipPanel() {
    JTextField field = new JTextField(20);
    JButton selectButton = new JButton("select .zip file");
    selectButton.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int ret = fileChooser.showOpenDialog(selectButton.getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        field.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton unzipButton = new JButton("unzip");
    unzipButton.addActionListener(e -> {
      String text = field.getText();
      createTargetDirPath(text).ifPresent(dir -> unzip(Paths.get(text), dir));
    });

    JPanel p = new JPanel(new BorderLayout(5, 2));
    p.setBorder(BorderFactory.createTitledBorder("Unzip"));
    p.add(field);
    p.add(selectButton, BorderLayout.EAST);
    p.add(unzipButton, BorderLayout.SOUTH);
    return p;
  }

  private void unzip(Path zipFile, Path targetDir) {
    Component parent = getRootPane();
    try {
      if (canOverwrite(parent, targetDir, "Unzip")) {
        createDirectoriesIfAbsent(targetDir);
        ZipUtils.unzip(zipFile, targetDir);
      }
    } catch (IOException ex) {
      LOGGER.info(() -> String.format("Cant unzip! : %s", zipFile));
      UIManager.getLookAndFeel().provideErrorFeedback(parent);
    }
  }

  private static boolean canOverwrite(Component parent, Path path, String title) {
    return !path.toFile().exists() || showOverwriteConfirm(parent, path, title);
  }

  private static boolean showOverwriteConfirm(Component parent, Path path, String title) {
    String s1 = String.format("%s already exists.", path);
    String s2 = "Do you want to overwrite it?";
    String msg = String.format("<html>%s<br>%s", s1, s2);
    int ret = JOptionPane.showConfirmDialog(
        parent,
        msg,
        title,
        JOptionPane.YES_NO_OPTION);
    return ret == JOptionPane.YES_OPTION;
  }

  private static void createDirectoriesIfAbsent(Path dir)
      throws IOException {

    // Files.notExists(dir) noticeably poor performance in JDK 8
    if (!dir.toFile().exists()) {
      LOGGER.info(() -> String.format("mkdir0: %s", dir));
      Files.createDirectories(dir);
    }
  }

  private static Optional<Path> createTargetDirPath(String text) {
    Optional<Path> op;
    Path path = Paths.get(text);

    // Files.notExists(path) noticeably poor performance in JDK 8
    if (text.isEmpty() || !path.toFile().exists()) {
      op = Optional.empty();
    } else {
      String name = Objects.toString(path.getFileName());
      int lastDotPos = name.lastIndexOf('.');
      if (lastDotPos > 0) {
        name = name.substring(0, lastDotPos);
      }
      op = Optional.of(path.resolveSibling(name));
    }
    return op;
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

final class ZipUtils {
  private static final Logger LOGGER = Logger.getLogger(MainPanel.LOGGER_NAME);

  private ZipUtils() {
    /* HideUtilityClassConstructor */
  }

  public static void zip(Path srcDir, Path zip) throws IOException {
    // noticeably poor performance in JDK 8
    // try (Stream<Path> s = Files.walk(srcDir).filter(Files::isRegularFile)) {
    try (Stream<Path> s = Files.walk(srcDir).filter(f -> f.toFile().isFile())) {
      // Java 16: List<Path> files = s.toList();
      List<Path> files = s.collect(Collectors.toList());
      try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip))) {
        for (Path path : files) {
          String relativePath = srcDir.relativize(path).toString().replace('\\', '/');
          LOGGER.info(() -> String.format("zip: %s", relativePath));
          zos.putNextEntry(createZipEntry(relativePath));
          Files.copy(path, zos);
          zos.closeEntry();
        }
      }
    }
  }

  private static ZipEntry createZipEntry(String name) {
    return new ZipEntry(name);
  }

  public static void unzip(Path zipFilePath, Path targetDir) throws IOException {
    try (ZipFile zipFile = new ZipFile(zipFilePath.toString())) {
      for (ZipEntry zipEntry : Collections.list(zipFile.entries())) {
        String name = zipEntry.getName();
        Path path = targetDir.resolve(name);
        if (name.endsWith("/")) { // if (Files.isDirectory(path)) {
          LOGGER.info(() -> String.format("mkdir1: %s", path));
          Files.createDirectories(path);
        } else {
          Path parent = path.getParent();
          // noticeably poor performance in JDK 8
          // if (Objects.nonNull(parent) && Files.notExists(parent)) {
          if (Objects.nonNull(parent) && !parent.toFile().exists()) {
            LOGGER.info(() -> String.format("mkdir2: %s", parent));
            Files.createDirectories(parent);
          }
          LOGGER.info(() -> String.format("copy: %s", path));
          try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
          }
        }
      }
    }
  }
}

class TextAreaOutputStream extends OutputStream {
  private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  private final JTextArea textArea;

  protected TextAreaOutputStream(JTextArea textArea) {
    super();
    this.textArea = textArea;
  }

  // // Java 10:
  // @Override public void flush() {
  //   textArea.append(buffer.toString(StandardCharsets.UTF_8));
  //   buffer.reset();
  // }

  @SuppressWarnings("JdkObsolete")
  @Override public void flush() throws IOException {
    textArea.append(buffer.toString("UTF-8"));
    buffer.reset();
  }

  @Override public void write(int b) {
    buffer.write(b);
  }

  @Override public void write(byte[] b, int off, int len) {
    buffer.write(b, off, len);
  }
}

class TextAreaHandler extends StreamHandler {
  protected TextAreaHandler(OutputStream os) {
    super(os, new SimpleFormatter());
  }

  @Override public String getEncoding() {
    return StandardCharsets.UTF_8.name();
  }

  // [UnsynchronizedOverridesSynchronized]
  // Unsynchronized method publish overrides synchronized method in StreamHandler
  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @Override public synchronized void publish(LogRecord logRecord) {
    super.publish(logRecord);
    flush();
  }

  // [UnsynchronizedOverridesSynchronized]
  // Unsynchronized method close overrides synchronized method in StreamHandler
  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @Override public synchronized void close() {
    flush();
  }
}
