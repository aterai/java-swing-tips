// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
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
    p.add(makeZipPanel());
    p.add(makeUnzipPanel());
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeZipPanel() {
    JTextField field = new JTextField(20);
    JButton button = new JButton("select directory");
    button.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int ret = fileChooser.showOpenDialog(button.getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        field.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button1 = new JButton("zip");
    button1.addActionListener(e -> {
      String str = field.getText();
      Path path = Paths.get(str);
      // noticeably poor performance in JDK 8
      // if (str.isEmpty() || Files.notExists(path)) {
      if (str.isEmpty() || !path.toFile().exists()) {
        return;
      }
      String name = path.getFileName() + ".zip";
      Path tgt = path.resolveSibling(name);
      // noticeably poor performance in JDK 8
      // if (Files.exists(tgt)) {
      JComponent c = (JComponent) e.getSource();
      if (tgt.toFile().exists()) {
        String s1 = String.format("%s already exists.", tgt);
        String s2 = "Do you want to overwrite it?";
        String m = String.format("<html>%s<br>%s", s1, s2);
        Component p = c.getRootPane();
        int rv = JOptionPane.showConfirmDialog(p, m, "Zip", JOptionPane.YES_NO_OPTION);
        if (rv != JOptionPane.YES_OPTION) {
          return;
        }
      }
      try {
        ZipUtil.zip(path, tgt);
      } catch (IOException ex) {
        LOGGER.info(() -> String.format("Cant zip! : %s", path));
        UIManager.getLookAndFeel().provideErrorFeedback(c);
      }
    });

    JPanel p = new JPanel(new BorderLayout(5, 2));
    p.setBorder(BorderFactory.createTitledBorder("Zip"));
    p.add(field);
    p.add(button, BorderLayout.EAST);
    p.add(button1, BorderLayout.SOUTH);
    return p;
  }

  private static Component makeUnzipPanel() {
    JTextField field = new JTextField(20);
    JButton button = new JButton("select .zip file");
    button.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int ret = fileChooser.showOpenDialog(button.getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        field.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button1 = new JButton("unzip");
    button1.addActionListener(e -> {
      String str = field.getText();
      makeDestDirPath(str).ifPresent(dir -> {
        Path path = Paths.get(str);
        try {
          // noticeably poor performance in JDK 8
          // if (Files.exists(dir)) {
          if (dir.toFile().exists()) {
            String s1 = String.format("%s already exists.", dir);
            String s2 = "Do you want to overwrite it?";
            String m = String.format("<html>%s<br>%s", s1, s2);
            Component p = button1.getRootPane();
            int rv = JOptionPane.showConfirmDialog(p, m, "Unzip", JOptionPane.YES_NO_OPTION);
            if (rv != JOptionPane.YES_OPTION) {
              return;
            }
          } else {
            LOGGER.info(() -> String.format("mkdir0: %s", dir));
            Files.createDirectories(dir);
          }
          ZipUtil.unzip(path, dir);
        } catch (IOException ex) {
          // ex.printStackTrace();
          LOGGER.info(() -> String.format("Cant unzip! : %s", path));
          UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
        }
      });
    });

    JPanel p = new JPanel(new BorderLayout(5, 2));
    p.setBorder(BorderFactory.createTitledBorder("Unzip"));
    p.add(field);
    p.add(button, BorderLayout.EAST);
    p.add(button1, BorderLayout.SOUTH);
    return p;
  }

  private static Optional<Path> makeDestDirPath(String text) {
    Path path = Paths.get(text);
    // noticeably poor performance in JDK 8
    // if (str.isEmpty() || Files.notExists(path)) {
    if (text.isEmpty() || !path.toFile().exists()) {
      return Optional.empty();
    }
    String name = Objects.toString(path.getFileName());
    int lastDotPos = name.lastIndexOf('.');
    if (lastDotPos > 0) {
      name = name.substring(0, lastDotPos);
    }
    return Optional.of(path.resolveSibling(name));
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class ZipUtil {
  private static final Logger LOGGER = Logger.getLogger(MainPanel.LOGGER_NAME);

  private ZipUtil() {
    /* HideUtilityClassConstructor */
  }

  public static void zip(Path srcDir, Path zip) throws IOException {
    // noticeably poor performance in JDK 8
    // try (Stream<Path> s = Files.walk(srcDir).filter(Files::isRegularFile)) {
    try (Stream<Path> s = Files.walk(srcDir).filter(f -> f.toFile().isFile())) {
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

  public static void unzip(Path zipFilePath, Path destDir) throws IOException {
    try (ZipFile zipFile = new ZipFile(zipFilePath.toString())) {
      for (ZipEntry zipEntry : Collections.list(zipFile.entries())) {
        String name = zipEntry.getName();
        Path path = destDir.resolve(name);
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
          Files.copy(zipFile.getInputStream(zipEntry), path, StandardCopyOption.REPLACE_EXISTING);
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
    super();
    configureEncoding();
    setOutputStream(os);
  }

  private void configureEncoding() {
    setFormatter(new SimpleFormatter());
    try {
      setEncoding("UTF-8");
    } catch (IOException ex) {
      try {
        setEncoding(null);
      } catch (IOException ex2) {
        // doing a setEncoding with null should always work.
        assert false;
      }
    }
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
