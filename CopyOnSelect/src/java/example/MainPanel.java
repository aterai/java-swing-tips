package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
// import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.Optional;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final transient Logger logger = Logger.getLogger(CopyOnSelectListener.class.getName());
    private final JTextArea log = new JTextArea();
    public MainPanel() {
        super(new GridLayout(2, 1));
        logger.setUseParentHandlers(false);
        logger.addHandler(new TextAreaHandler(new TextAreaOutputStream(log)));

        JTextArea textArea = new JTextArea("abcdefg hijklmn opqrstu") {
            private transient CopyOnSelectListener handler;
            @Override public void updateUI() {
                removeCaretListener(handler);
                removeMouseListener(handler);
                removeKeyListener(handler);
                super.updateUI();
                handler = new CopyOnSelectListener();
                addCaretListener(handler);
                addMouseListener(handler);
                addKeyListener(handler);
            }
        };

        add(makeTitledPane("Copy On Select", new JScrollPane(textArea)));
        add(makeTitledPane("log", new JScrollPane(log)));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeTitledPane(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class CopyOnSelectListener extends MouseAdapter implements CaretListener, KeyListener {
    private final transient Logger logger = Logger.getLogger(getClass().getName());
    private boolean dragActive;
    private boolean shiftActive;
    private int dot;
    private int mark;
    @Override public void caretUpdate(CaretEvent e) {
        if (!dragActive && !shiftActive) {
            fire(e.getSource());
        }
    }
    @Override public void mousePressed(MouseEvent e) {
        dragActive = true;
    }
    @Override public void mouseReleased(MouseEvent e) {
        dragActive = false;
        fire(e.getSource());
    }
    @Override public void keyPressed(KeyEvent e) {
        shiftActive = (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
    }
    @Override public void keyReleased(KeyEvent e) {
        shiftActive = (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
        if (!shiftActive) {
            fire(e.getSource());
        }
    }
    @Override public void keyTyped(KeyEvent e) { /* empty */ }
    private void fire(Object c) {
        if (c instanceof JTextComponent) {
            JTextComponent tc = (JTextComponent) c;
            Caret caret = tc.getCaret();
            int d = caret.getDot();
            int m = caret.getMark();
            //logger.info(m + " / " + d);
            if (d != m && (dot != d || mark != m)) {
                Optional.ofNullable(tc.getSelectedText()).ifPresent(str -> {
                    logger.info(str);
                    //StringSelection data = new StringSelection(str);
                    //Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);
                    tc.copy();
                });
            }
            dot = d;
            mark = m;
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
