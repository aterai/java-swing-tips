// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
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
  private static final Logger LOGGER = Logger.getLogger(ZipUtil.class.getName());
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    LOGGER.setUseParentHandlers(false);
    LOGGER.addHandler(new TextAreaHandler(new TextAreaOutputStream(log)));
    log.setEditable(false);

    JPanel p = new JPanel(new GridLayout(2, 1, 10, 10));
    p.add(makeZipPanel());
    p.add(makeUnzipPanel());
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
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
      if (str.isEmpty() || Files.notExists(path)) {
        return;
      }
      String name = Objects.toString(path.getFileName()) + ".zip";
      Path tgt = path.resolveSibling(name);
      if (Files.exists(tgt)) {
        String m = String.format("<html>%s already exists.<br>Do you want to overwrite it?", tgt.toString());
        int rv = JOptionPane.showConfirmDialog(button1.getRootPane(), m, "Zip", JOptionPane.YES_NO_OPTION);
        if (rv != JOptionPane.YES_OPTION) {
          return;
        }
      }
      try {
        ZipUtil.zip(path, tgt);
      } catch (IOException ex) {
        ex.printStackTrace();
        Toolkit.getDefaultToolkit().beep();
        // LOGGER.info("Cant zip! : " + path.toString());
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
      Path path = Paths.get(str);
      if (str.isEmpty() || Files.notExists(path)) {
        return;
      }
      String name = Objects.toString(path.getFileName());
      int lastDotPos = name.lastIndexOf('.');
      if (lastDotPos > 0) {
        name = name.substring(0, lastDotPos);
      }
      Path destDir = path.resolveSibling(name);
      try {
        if (Files.exists(destDir)) {
          String m = String.format("<html>%s already exists.<br>Do you want to overwrite it?", destDir.toString());
          int rv = JOptionPane.showConfirmDialog(button1.getRootPane(), m, "Unzip", JOptionPane.YES_NO_OPTION);
          if (rv != JOptionPane.YES_OPTION) {
            return;
          }
        } else {
          if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("mkdir0: " + destDir.toString());
          }
          Files.createDirectories(destDir);
        }
        ZipUtil.unzip(path, destDir);
      } catch (IOException ex) {
        ex.printStackTrace();
        Toolkit.getDefaultToolkit().beep();
        // LOGGER.info("Cant unzip! : " + path.toString());
      }
    });

    JPanel p = new JPanel(new BorderLayout(5, 2));
    p.setBorder(BorderFactory.createTitledBorder("Unzip"));
    p.add(field);
    p.add(button, BorderLayout.EAST);
    p.add(button1, BorderLayout.SOUTH);
    return p;
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
  private static final Logger LOGGER = Logger.getLogger(ZipUtil.class.getName());

  private ZipUtil() {
    /* HideUtilityClassConstructor */
  }

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public static void zip(Path srcDir, Path zip) throws IOException {
    try (Stream<Path> s = Files.walk(srcDir).filter(Files::isRegularFile)) {
      List<Path> files = s.collect(Collectors.toList());
      try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip))) {
        for (Path path: files) {
          String relativePath = srcDir.relativize(path).toString().replace('\\', '/');
          log("zip: " + relativePath);
          zos.putNextEntry(new ZipEntry(relativePath));
          Files.copy(path, zos);
          zos.closeEntry();
        }
      }
    }
  }

  public static void unzip(Path zipFilePath, Path destDir) throws IOException {
    try (ZipFile zipFile = new ZipFile(zipFilePath.toString())) {
      Enumeration<? extends ZipEntry> e = zipFile.entries();
      while (e.hasMoreElements()) {
        ZipEntry zipEntry = e.nextElement();
        String name = zipEntry.getName();
        Path path = destDir.resolve(name);
        if (name.endsWith("/")) { // if (Files.isDirectory(path)) {
          log("mkdir1: " + path.toString());
          Files.createDirectories(path);
        } else {
          Path parent = path.getParent();
          if (Objects.nonNull(parent) && Files.notExists(parent)) {
            log("mkdir2: " + parent.toString());
            Files.createDirectories(parent);
          }
          log("copy: " + path.toString());
          Files.copy(zipFile.getInputStream(zipEntry), path, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  private static void log(String txt) {
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info(txt);
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
}

class TextAreaHandler extends StreamHandler {
  private void configure() {
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

  protected TextAreaHandler(OutputStream os) {
    super();
    configure();
    setOutputStream(os);
  }

  // [UnsynchronizedOverridesSynchronized] Unsynchronized method publish overrides synchronized method in StreamHandler
  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @Override public synchronized void publish(LogRecord record) {
    super.publish(record);
    flush();
  }

  // [UnsynchronizedOverridesSynchronized] Unsynchronized method close overrides synchronized method in StreamHandler
  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @Override public synchronized void close() {
    flush();
  }
}
