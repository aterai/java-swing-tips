// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
  private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

  private MainPanel() {
    super(new BorderLayout());
    LOGGER.setUseParentHandlers(false);
    JTextArea textArea = new JTextArea();
    LOGGER.addHandler(new TextAreaHandler(new TextAreaOutputStream(textArea)));
    add(new JScrollPane(textArea));

    JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(makeTextField());
    JButton button = new JButton("TEST: ActionEvent#getModifiers()");
    button.addActionListener(MainPanel::info);
    p.add(button);
    EventQueue.invokeLater(() -> {
      JRootPane root = getRootPane();
      root.setJMenuBar(makeMenuBar());
      root.setDefaultButton(button);
    });
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTextField makeTextField() {
    JTextField field = new JTextField(20);
    // KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.SHIFT_DOWN_MASK);
    String key = "beep";
    field.getInputMap().put(KeyStroke.getKeyStroke("shift B"), key);
    field.getActionMap().put(key, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        Toolkit.getDefaultToolkit().beep();
        // UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    });
    field.addKeyListener(new KeyAdapter() {
      @Override public void keyPressed(KeyEvent e) {
        // InputEvent.SHIFT_MASK @Deprecated(since="9")
        // boolean shiftActive = (e.getModifiers() & InputEvent.SHIFT_MASK) != 0;
        boolean shiftActive = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
        if (e.getKeyCode() == KeyEvent.VK_N && shiftActive) {
          // or: if (e.getKeyCode() == KeyEvent.VK_N && e.isShiftDown()) {
          UIManager.getLookAndFeel().provideErrorFeedback(e.getComponent());
        }
      }
    });
    return field;
  }

  private static void info(ActionEvent e) {
    String name = e.getSource().getClass().getSimpleName();
    // BAD EXAMPLE: boolean isShiftDown = (e.getModifiers() & InputEvent.SHIFT_MASK) != 0;
    // Always use ActionEvent.*_MASK instead of InputEvent.*_MASK in ActionListener
    boolean isShiftDown = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
    if (isShiftDown) {
      LOGGER.info(() -> name + ": Shift is Down");
    } else {
      LOGGER.info(() -> name + ": Shift is Up");
    }
    if ((e.getModifiers() & AWTEvent.MOUSE_EVENT_MASK) != 0) {
      LOGGER.info(() -> name + ": Mouse event mask");
    }
  }

  private static JMenuBar makeMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = menuBar.add(new JMenu("Test"));
    menu.setMnemonic(KeyEvent.VK_T);
    // JMenuItem item = menu.add(new AbstractAction("beep") {
    JMenuItem item = new JMenuItem(new AbstractAction("beep") {
      @Override public void actionPerformed(ActionEvent e) {
        Toolkit.getDefaultToolkit().beep();
      }
    });
    menu.add(item).addActionListener(MainPanel::info);
    item.setAccelerator(KeyStroke.getKeyStroke("shift 1"));
    item.setMnemonic(KeyEvent.VK_I);
    // item.addActionListener(MainPanel::info);
    return menuBar;
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
