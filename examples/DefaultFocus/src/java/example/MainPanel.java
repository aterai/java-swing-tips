// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public static final String LOGGER_NAME = MethodHandles.lookup().lookupClass().getName();
  public static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    log.setEditable(false);
    LOGGER.addHandler(new TextAreaHandler(new TextAreaOutputStream(log)));

    JTextField field = new JTextField();
    JButton nb = new JButton("NORTH");
    JButton sb = new JButton("SOUTH");
    JButton wb = new JButton("WEST");
    JButton eb = new JButton("EAST");

    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(nb, BorderLayout.NORTH);
    p.add(sb, BorderLayout.SOUTH);
    p.add(wb, BorderLayout.WEST);
    p.add(eb, BorderLayout.EAST);
    p.add(field);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));

    // frame.addWindowListener(new WindowAdapter() {
    //   @Override public void windowOpened(WindowEvent e) {
    //     LOGGER.info(() -> "windowOpened");
    //     field.requestFocus();
    //   }
    // });

    // frame.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
    //   @Override public Component getInitialComponent(Window w) {
    //     LOGGER.info(() -> "getInitialComponent");
    //     return field;
    //   }
    // });

    // frame.addComponentListener(new ComponentAdapter() {
    //   @Override public void componentShown(ComponentEvent e) {
    //     LOGGER.info(() -> "componentShown");
    //     field.requestFocusInWindow();
    //   }
    // });

    // KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    // focusManager.addPropertyChangeListener(new PropertyChangeListener() {
    //   @Override public void propertyChange(PropertyChangeEvent e) {
    //     String prop = e.getPropertyName();
    //     if ("activeWindow".equals(prop) && e.getNewValue() != null) {
    //       LOGGER.info(() -> "activeWindow");
    //       field.requestFocusInWindow();
    //     }
    //   }
    // });

    EventQueue.invokeLater(() -> {
      LOGGER.info(() -> "invokeLater");
      field.requestFocusInWindow();
      LOGGER.info(() -> "getRootPane().setDefaultButton(eb)");
      getRootPane().setDefaultButton(eb);
    });

    LOGGER.info(() -> "this");
    // field.requestFocusInWindow();
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    LOGGER.info(() -> "frame.pack();");
    frame.pack();
    frame.setLocationRelativeTo(null);
    LOGGER.info(() -> "frame.setVisible(true);");
    frame.setVisible(true);
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
