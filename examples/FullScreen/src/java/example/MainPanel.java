// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    LOGGER.setUseParentHandlers(false);
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    LOGGER.addHandler(new TextAreaHandler(new TextAreaOutputStream(textArea)));

    setFocusable(true);
    addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        boolean isDoubleClick = e.getClickCount() >= 2;
        if (isDoubleClick) {
          toggleFullScreenWindow();
        }
      }
    });
    String key = "full-screen";
    InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), key);
    getActionMap().put(key, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        toggleFullScreenWindow();
      }
    });
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
    getActionMap().put("close", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        LOGGER.info(() -> "ESC KeyEvent:");
        Container c = getTopLevelAncestor();
        if (c instanceof Window) {
          Window d = (Window) c;
          d.dispatchEvent(new WindowEvent(d, WindowEvent.WINDOW_CLOSING));
        }
      }
    });
    String help1 = "F11 or Double Click: toggle full-screen";
    String help2 = "ESC: exit";
    JLabel label = new JLabel(String.format("<html>%s<br/>%s", help1, help2));
    label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(label, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public void toggleFullScreenWindow() {
    // Component c = SwingUtilities.getRoot(getRootPane());
    Container c = getTopLevelAncestor();
    if (c instanceof Dialog) {
      Dialog dialog = (Dialog) c;
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice gd = ge.getDefaultScreenDevice();
      if (gd.getFullScreenWindow() == null) {
        dialog.dispose(); // destroy the native resources
        dialog.setUndecorated(true);
        dialog.setVisible(true); // rebuilding the native resources
        gd.setFullScreenWindow(dialog);
      } else {
        gd.setFullScreenWindow(null);
        dialog.dispose();
        dialog.setUndecorated(false);
        dialog.setVisible(true);
        dialog.repaint();
      }
    }
    requestFocusInWindow(); // for Ubuntu
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
    JDialog dialog = new JDialog();
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.addWindowListener(new WindowAdapter() {
      @Override public void windowClosing(WindowEvent e) {
        LOGGER.info(() -> {
          String str1 = "windowClosing:";
          String str2 = "triggered only when you click on the X";
          String str3 = "or on the close menu item in the window's system menu.'";
          return String.join("\n  ", str1, str2, str3);
        });
      }

      @Override public void windowClosed(WindowEvent e) {
        LOGGER.info(() -> "windowClosed & rebuild:");
      }
    });
    dialog.getContentPane().add(new MainPanel());
    dialog.pack();
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
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
