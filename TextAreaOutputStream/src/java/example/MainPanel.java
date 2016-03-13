package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Date;
import java.util.logging.*;
import javax.swing.*;
// import javax.swing.event.*;
// import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final transient Logger logger = Logger.getLogger(TextAreaLogger.TEST.getClass().getName());
    private final JTextArea textArea = new JTextArea();
    private final JTextField textField = new JTextField(TextAreaLogger.TEST.getClass().getName());

    public MainPanel() {
        super(new BorderLayout());
        //TEST: textArea.getDocument().addDocumentListener(new FIFODocumentListener(textArea));
        textArea.setEditable(false);

        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);

        OutputStream os = new TextAreaOutputStream(textArea);
        logger.addHandler(new TextAreaHandler(os));

//         //TEST:
//         try {
//             //System.setOut(new PrintStream(os, true, "UTF-8"));
//             FileHandler fh = new FileHandler("test.log");
//             fh.setEncoding("UTF-8");
//             logger.addHandler(fh);
//         } catch (IOException ex) {
//             ex.printStackTrace();
//         }

        logger.info("test, TEST");

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(Box.createHorizontalGlue());
        box.add(textField);
        box.add(Box.createHorizontalStrut(5));
        box.add(new JButton(new EnterAction(textField)));
        box.add(Box.createHorizontalStrut(5));
        box.add(new JButton(new AbstractAction("Clear") {
            @Override public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        }));

        add(new JScrollPane(textArea));
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

enum TextAreaLogger {
    TEST;
}

class EnterAction extends AbstractAction {
    private final transient Logger logger = Logger.getLogger(TextAreaLogger.TEST.getClass().getName());
    private final JTextField textField;
    protected EnterAction(JTextField textField) {
        super("Enter");
        this.textField = textField;
    }
    @Override public void actionPerformed(ActionEvent e) {
        String s = new Date().toString();
        logger.info(s + "\n    " + textField.getText());
    }
}

// class FIFODocumentListener implements DocumentListener {
//     private static final int MAX_LINES = 100;
//     private final JTextComponent textComponent;
//     protected FIFODocumentListener(JTextComponent textComponent) {
//         this.textComponent = textComponent;
//     }
//     @Override public void insertUpdate(DocumentEvent e) {
//         final Document doc = e.getDocument();
//         final Element root = doc.getDefaultRootElement();
//         if (root.getElementCount() <= MAX_LINES) {
//             return;
//         }
//         EventQueue.invokeLater(new Runnable() {
//             @Override public void run() {
//                 removeLines(doc, root);
//             }
//         });
//         textComponent.setCaretPosition(doc.getLength());
//     }
//     private static void removeLines(Document doc, Element root) {
//         Element fl = root.getElement(0);
//         try {
//             doc.remove(0, fl.getEndOffset());
//         } catch (BadLocationException ble) {
//             ble.printStackTrace();
//         }
//     }
//     @Override public void removeUpdate(DocumentEvent e)  {
//         /* not needed */
//     }
//     @Override public void changedUpdate(DocumentEvent e) {
//         /* not needed */
//     }
// }

// class TextAreaOutputStream extends OutputStream {
//     private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
//     private final JTextArea textArea;
//     protected TextAreaOutputStream(JTextArea textArea) {
//         super();
//         this.textArea = textArea;
//     }
//     @Override public void flush() throws IOException {
//         super.flush();
//         buf.flush();
//     }
//     @Override public void close() throws IOException {
//         super.close();
//         buf.close();
//     }
//     @Override public void write(int b) throws IOException {
//         if (b == '\r') {
//             return;
//         }
//         if (b == '\n') {
//             final String text = buf.toString("UTF-8");
//             buf.reset();
//             EventQueue.invokeLater(new Runnable() {
//                 @Override public void run() {
//                     textArea.append(text + '\n');
//                     textArea.setCaretPosition(textArea.getDocument().getLength());
//                 }
//             });
//             return;
//         }
//         buf.write(b);
//     }
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
    @Override public void write(int b) throws IOException {
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
                // assert false;
                ex2.printStackTrace();
            }
        }
    }
    protected TextAreaHandler(OutputStream os) {
        super();
        configure();
        setOutputStream(os);
    }
    @Override public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }
    @Override public void close() {
        flush();
    }
}

// //TEST:
// //http://www.dreamincode.net/forums/topic/117537-external-program-output-to-jtextarea/
// class TextAreaOutputStream extends OutputStream {
//     private final JTextArea textArea;
//     private final StringBuilder sb = new StringBuilder();
//
//     protected TextAreaOutputStream(JTextArea textArea) {
//         super();
//         this.textArea = textArea;
//     }
//
//     @Override public void flush() {}
//
//     @Override public void close() {}
//
//     @Override public void write(int b) throws IOException {
//         if (b == '\r') {
//            return;
//         }
//         if (b == '\n') {
//             textArea.append(sb.toString());
//             sb.setLength(0);
//         }
//         //sb.append((char) b);
//         String s;
//         if (Character.charCount(b) == 1) {
//             s = String.valueOf((char) b);
//         } else {
//             s = new String(Character.toChars(b));
//         }
//         sb.append(s);
//     }
// }

// //http://d.hatena.ne.jp/altcla/20091029/1256824750
// class TextAreaOutputStream extends OutputStream {
//     private JTextArea textArea;
//     private ByteArrayOutputStream buf;
//
//     protected TextAreaOutputStream(JTextArea area) {
//         textArea = area;
//         buf = new ByteArrayOutputStream();
//     }
//
//     @Override public void write(int i) throws IOException {
//         buf.write(i);
//     }
//     @Override public void flush() throws IOException {
//         EventQueue.invokeLater(new Runnable() {
//             @Override public void run() {
//                 try {
//                     textArea.append(buf.toString("UTF-8"));
//                     textArea.setCaretPosition(textArea.getDocument().getLength());
//                     buf.reset();
//                 } catch (UnsupportedEncodingException ex) {}
//             }
//         });
//     }
// }

// class TextAreaOutputStream extends OutputStream {
//     private final JTextArea textArea;
//     protected TextAreaOutputStream(JTextArea textArea) {
//         super();
//         this.textArea = textArea;
//     }
//     @Override public void write(int i) throws IOException {
//         String s;
//         if (Character.charCount(i) == 1) {
//             s = String.valueOf((char) i);
//         } else {
//             s = new String(Character.toChars(i));
//         }
//         textArea.append(s);
//         //textArea.append(new String(Character.toChars(i)));
//         //textArea.setCaretPosition(textArea.getDocument().getLength());
//     }
// }

// class TextAreaOutputStream extends OutputStream {
//     private final JTextArea textArea;
//     protected TextAreaOutputStream(JTextArea textArea) {
//         super();
//         this.textArea = textArea;
//     }
//     @Override public void write(int i) throws IOException {
//         textArea.append(String.valueOf((char) i));
//         textArea.setCaretPosition(textArea.getDocument().getLength());
//     }
// }

// class TextAreaOutputStream extends OutputStream {
//     private final AbstractDocument doc;
//     protected TextAreaOutputStream(AbstractDocument doc) {
//         super();
//         this.doc = doc;
//     }
//     @Override public void write(int i) throws IOException {
//         try {
//             doc.replace(doc.getLength(), 0, new String(Character.toChars(i)), null);
//             //doc.createPosition(doc.getLength());
//         } catch (BadLocationException ex) {
//             throw new IOException(ex);
//         }
//     }
// }

// //https://tips4java.wordpress.com/2008/11/08/message-console/
// try {
//     PipedOutputStream pos = new PipedOutputStream();
//     PipedInputStream  pis = new PipedInputStream(pos);
//     final BufferedReader reader = new BufferedReader(new InputStreamReader(pis, "UTF-8"));
//     System.setOut(new PrintStream(pos, true, "UTF-8"));
//
//     new Thread(new Runnable() {
//         @Override public void run() {
//             String line = null;
//             try {
//                 while ((line = reader.readLine()) != null) {
//                     displayPane.append(line + "\n");
//                     displayPane.setCaretPosition(displayPane.getDocument().getLength());
//                 }
//             } catch (IOException ioe) {
//                 //JOptionPane.showMessageDialog(null, "Error redirecting output : " + ioe.getMessage());
//             }
//         }
//     }).start();
// } catch (IOException ex) {}
