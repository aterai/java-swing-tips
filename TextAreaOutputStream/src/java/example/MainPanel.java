// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final Logger LOGGER = Logger.getLogger(TextAreaLogger.class.getName());

  private MainPanel() {
    super(new BorderLayout());

    JTextArea textArea = new JTextArea();
    // TEST: textArea.getDocument().addDocumentListener(new FIFODocumentListener(textArea));
    textArea.setEditable(false);

    LOGGER.setUseParentHandlers(false);
    LOGGER.setLevel(Level.ALL);
    LOGGER.addHandler(new TextAreaHandler(new TextAreaOutputStream(textArea)));

    // // TEST:
    // try {
    //   OutputStream os = new TextAreaOutputStream(textArea);
    //   System.setOut(new PrintStream(os, true, "UTF-8"));
    //   FileHandler fh = new FileHandler("test.log");
    //   fh.setEncoding("UTF-8");
    //   LOGGER.addHandler(fh);
    // } catch (IOException ex) {
    //   // os.close();
    //   ex.printStackTrace();
    // }

    LOGGER.info("test, TEST");

    JButton button = new JButton("Clear");
    button.addActionListener(e -> textArea.setText(""));
    JTextField textField = new JTextField("aaa");

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(Box.createHorizontalGlue());
    box.add(textField);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JButton(new EnterAction(textField)));
    box.add(Box.createHorizontalStrut(5));
    box.add(button);

    add(new JScrollPane(textArea));
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TextAreaLogger {
}

class EnterAction extends AbstractAction {
  private static final Logger LOGGER = Logger.getLogger(TextAreaLogger.class.getName());
  private final JTextField textField;

  protected EnterAction(JTextField textField) {
    super("Enter");
    this.textField = textField;
  }

  @Override public void actionPerformed(ActionEvent e) {
    LOGGER.info(String.format("%s%n  %s%n", LocalDateTime.now(ZoneId.systemDefault()), textField.getText()));
  }
}

// class FIFODocumentListener implements DocumentListener {
//   private static final int MAX_LINES = 100;
//   private final JTextComponent textComponent;
//   protected FIFODocumentListener(JTextComponent textComponent) {
//     this.textComponent = textComponent;
//   }
//   @Override public void insertUpdate(DocumentEvent e) {
//     Document doc = e.getDocument();
//     Element root = doc.getDefaultRootElement();
//     if (root.getElementCount() <= MAX_LINES) {
//       return;
//     }
//     EventQueue.invokeLater(new Runnable() {
//       @Override public void run() {
//         removeLines(doc, root);
//       }
//     });
//     textComponent.setCaretPosition(doc.getLength());
//   }
//   private static void removeLines(Document doc, Element root) {
//     Element fl = root.getElement(0);
//     try {
//       doc.remove(0, fl.getEndOffset());
//     } catch (BadLocationException ex) {
//       throw new RuntimeException(ex); // should never happen
//     }
//   }
//   @Override public void removeUpdate(DocumentEvent e) {
//     /* not needed */
//   }
//   @Override public void changedUpdate(DocumentEvent e) {
//     /* not needed */
//   }
// }

// class TextAreaOutputStream extends OutputStream {
//   private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
//   private final JTextArea textArea;
//   protected TextAreaOutputStream(JTextArea textArea) {
//     super();
//     this.textArea = textArea;
//   }
//   @Override public void flush() throws IOException {
//     super.flush();
//     buf.flush();
//   }
//   @Override public void close() throws IOException {
//     super.close();
//     buf.close();
//   }
//   @Override public void write(int b) throws IOException {
//     if (b == '\r') {
//       return;
//     }
//     if (b == '\n') {
//       String text = buf.toString("UTF-8");
//       buf.reset();
//       EventQueue.invokeLater(new Runnable() {
//         @Override public void run() {
//           textArea.append(text + '\n');
//           textArea.setCaretPosition(textArea.getDocument().getLength());
//         }
//       });
//       return;
//     }
//     buf.write(b);
//   }
// }

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

// // TEST:
// // http://www.dreamincode.net/forums/topic/117537-external-program-output-to-jtextarea/
// class TextAreaOutputStream extends OutputStream {
//   private final JTextArea textArea;
//   private final StringBuilder sb = new StringBuilder();
//
//   protected TextAreaOutputStream(JTextArea textArea) {
//     super();
//     this.textArea = textArea;
//   }
//
//   @Override public void flush() {}
//
//   @Override public void close() {}
//
//   @Override public void write(int b) throws IOException {
//     if (b == '\r') {
//      return;
//     }
//     if (b == '\n') {
//       textArea.append(sb.toString());
//       sb.setLength(0);
//     }
//     // sb.append((char) b);
//     String s;
//     if (Character.charCount(b) == 1) {
//       s = Objects.toString((char) b);
//     } else {
//       s = new String(Character.toChars(b));
//     }
//     sb.append(s);
//   }
// }

// // http://d.hatena.ne.jp/altcla/20091029/1256824750
// class TextAreaOutputStream extends OutputStream {
//   private JTextArea textArea;
//   private ByteArrayOutputStream buf;
//
//   protected TextAreaOutputStream(JTextArea area) {
//     textArea = area;
//     buf = new ByteArrayOutputStream();
//   }
//
//   @Override public void write(int i) throws IOException {
//     buf.write(i);
//   }
//   @Override public void flush() throws IOException {
//     EventQueue.invokeLater(new Runnable() {
//       @Override public void run() {
//         try {
//           textArea.append(buf.toString("UTF-8"));
//           textArea.setCaretPosition(textArea.getDocument().getLength());
//           buf.reset();
//         } catch (UnsupportedEncodingException ex) {}
//       }
//     });
//   }
// }

// class TextAreaOutputStream extends OutputStream {
//   private final JTextArea textArea;
//   protected TextAreaOutputStream(JTextArea textArea) {
//     super();
//     this.textArea = textArea;
//   }
//   @Override public void write(int i) throws IOException {
//     String s;
//     if (Character.charCount(i) == 1) {
//       s = Objects.toString((char) i);
//     } else {
//       s = new String(Character.toChars(i));
//     }
//     textArea.append(s);
//     // textArea.append(new String(Character.toChars(i)));
//     // textArea.setCaretPosition(textArea.getDocument().getLength());
//   }
// }

// class TextAreaOutputStream extends OutputStream {
//   private final JTextArea textArea;
//   protected TextAreaOutputStream(JTextArea textArea) {
//     super();
//     this.textArea = textArea;
//   }
//   @Override public void write(int i) throws IOException {
//     textArea.append(Objects.toString((char) i));
//     textArea.setCaretPosition(textArea.getDocument().getLength());
//   }
// }

// class TextAreaOutputStream extends OutputStream {
//   private final AbstractDocument doc;
//   protected TextAreaOutputStream(AbstractDocument doc) {
//     super();
//     this.doc = doc;
//   }
//   @Override public void write(int i) throws IOException {
//     try {
//       doc.replace(doc.getLength(), 0, new String(Character.toChars(i)), null);
//       // doc.createPosition(doc.getLength());
//     } catch (BadLocationException ex) {
//       throw new IOException(ex);
//     }
//   }
// }

// // https://tips4java.wordpress.com/2008/11/08/message-console/
// try {
//   PipedOutputStream pos = new PipedOutputStream();
//   PipedInputStream pis = new PipedInputStream(pos);
//   BufferedReader reader = new BufferedReader(new InputStreamReader(pis, "UTF-8"));
//   System.setOut(new PrintStream(pos, true, "UTF-8"));
//
//   new Thread(new Runnable() {
//     @Override public void run() {
//       String line = null;
//       try {
//         while ((line = reader.readLine()) != null) {
//           displayPane.append(line + "\n");
//           displayPane.setCaretPosition(displayPane.getDocument().getLength());
//         }
//       } catch (IOException ex) {
//         // JOptionPane.showMessageDialog(null, "Error redirecting output : " + ex.getMessage());
//       }
//     }
//   }).start();
// } catch (IOException ex) {}
