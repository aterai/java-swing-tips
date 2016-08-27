package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final JTextArea textArea = new JTextArea();
    private final Timer timer = new Timer(200, e -> {
        String s = new Date().toString();
        textArea.append(textArea.getDocument().getLength() > 0 ? "\n" + s : s);
    });

    public MainPanel() {
        super(new BorderLayout());
        //TEST: ((AbstractDocument) textArea.getDocument()).setDocumentFilter(new FIFODocumentFilter());
        textArea.getDocument().addDocumentListener(new FIFODocumentListener(textArea));
        textArea.setEditable(false);

        JButton start = new JButton("Start");
        start.addActionListener(e -> {
            if (!timer.isRunning()) {
                timer.start();
            }
        });

        JButton stop  = new JButton("Stop");
        stop.addActionListener(e -> timer.stop());

        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> textArea.setText(""));

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable()) {
                timer.stop();
            }
        });

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(start);
        box.add(stop);
        box.add(Box.createHorizontalStrut(5));
        box.add(clear);

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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class FIFODocumentListener implements DocumentListener {
    private static final int MAX_LINES = 10;
    private final JTextComponent textComponent;
    protected FIFODocumentListener(JTextComponent textComponent) {
        this.textComponent = textComponent;
    }
    @Override public void insertUpdate(DocumentEvent e) {
        final Document doc = e.getDocument();
        final Element root = doc.getDefaultRootElement();
        if (root.getElementCount() <= MAX_LINES) {
            return;
        }
        EventQueue.invokeLater(() -> removeLines(doc, root));
        textComponent.setCaretPosition(doc.getLength());
    }
    private static void removeLines(Document doc, Element root) {
        Element fl = root.getElement(0);
        try {
            doc.remove(0, fl.getEndOffset());
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
    }
    @Override public void removeUpdate(DocumentEvent e)  { /* not needed */ }
    @Override public void changedUpdate(DocumentEvent e) { /* not needed */ }
}

class FIFODocumentFilter extends DocumentFilter {
    private static final int MAX_LINES = 10;
    @Override public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
        fb.insertString(offset, text, attr);
        Element root = fb.getDocument().getDefaultRootElement();
        if (root.getElementCount() > MAX_LINES) {
            fb.remove(0, root.getElement(0).getEndOffset());
        }
    }
//     @Override public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
//         fb.remove(offset, length);
//     }
//     @Override public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
//         fb.replace(offset, length, text, attrs);
//     }
}
