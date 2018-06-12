package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.logging.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private final JTextArea log = new JTextArea();
    private MainPanel() {
        super(new BorderLayout());
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(new TextAreaHandler(new TextAreaOutputStream(log)));

        JTextField field = new JTextField(20);
        field.getActionMap().put("beep", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                Toolkit.getDefaultToolkit().beep();
            }
        });
        field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.SHIFT_DOWN_MASK), "beep");

        field.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                // InputEvent.SHIFT_MASK @Deprecated(since="9")
                // boolean shiftActive = (e.getModifiers() & InputEvent.SHIFT_MASK) != 0;
                boolean shiftActive = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
                if (e.getKeyCode() == KeyEvent.VK_N && shiftActive) { // or: if (e.getKeyCode() == KeyEvent.VK_N && e.isShiftDown()) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        JButton button = new JButton("TEST: ActionEvent#getModifiers()");
        button.addActionListener(e -> {
            // BAD EXAMPLE: boolean isShiftDown = (e.getModifiers() & InputEvent.SHIFT_MASK) != 0;
            // Always use ActionEvent.*_MASK instead of InputEvent.*_MASK in ActionListener
            boolean isShiftDown = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
            if (isShiftDown) {
                LOGGER.info("JButton: Shift is Down");
            } else {
                LOGGER.info("JButton: Shift is Up");
            }
            if ((e.getModifiers() & AWTEvent.MOUSE_EVENT_MASK) != 0) {
                LOGGER.info("JButton: Mouse event mask");
            }
        });

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = menuBar.add(new JMenu("Test"));
        menu.setMnemonic(KeyEvent.VK_T);
        JMenuItem item = menu.add(new AbstractAction("beep") {
            @Override public void actionPerformed(ActionEvent e) {
                Toolkit.getDefaultToolkit().beep();
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.SHIFT_DOWN_MASK));
        item.setMnemonic(KeyEvent.VK_I);
        item.addActionListener(e -> {
            boolean isShiftDown = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
            if (isShiftDown) {
                LOGGER.info("JMenuItem: Shift is Down");
            } else {
                LOGGER.info("JMenuItem: Shift is Up");
            }
            if ((e.getModifiers() & AWTEvent.MOUSE_EVENT_MASK) != 0) {
                LOGGER.info("JMenuItem: Mouse event mask");
            }
        });

        EventQueue.invokeLater(() -> {
            JRootPane root = getRootPane();
            root.setJMenuBar(menuBar);
            root.setDefaultButton(button);
        });

        JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(field);
        p.add(button);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(log));
        setPreferredSize(new Dimension(320, 240));
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
