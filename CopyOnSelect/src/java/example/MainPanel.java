package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(CopyOnSelectListener.class.getName());
    private MainPanel() {
        super(new GridLayout(2, 1));
        JTextArea log = new JTextArea();
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(new TextAreaHandler(new TextAreaOutputStream(log)));

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

        add(makeTitledPanel("Copy On Select", new JScrollPane(textArea)));
        add(makeTitledPanel("log", new JScrollPane(log)));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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
    private static final Logger LOGGER = Logger.getLogger(CopyOnSelectListener.class.getName());
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
        shiftActive = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
    }
    @Override public void keyReleased(KeyEvent e) {
        shiftActive = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
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
            // LOGGER.info(m + " / " + d);
            if (d != m && (dot != d || mark != m)) {
                Optional.ofNullable(tc.getSelectedText()).ifPresent(str -> {
                    LOGGER.info(str);
                    // StringSelection data = new StringSelection(str);
                    // Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);
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
