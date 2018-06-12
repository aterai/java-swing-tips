package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox("LineWrap");
    private final HighlightCursorTextArea textArea = new HighlightCursorTextArea();
    public MainPanel() {
        super(new BorderLayout());
        textArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        textArea.setText("Highlight Cursor Test\n\naaaaaaaaaaaasdfasdfasdfasdfsadffasdfas");

        check.addActionListener(e -> {
            textArea.setLineWrap(check.isSelected());
            textArea.requestFocusInWindow();
        });
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.getViewport().setBackground(Color.WHITE);
        add(check, BorderLayout.NORTH);
        add(scroll);
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

class HighlightCursorTextArea extends JTextArea {
    private static final Color LINE_COLOR = new Color(250, 250, 220);
    @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        Caret caret = new DefaultCaret() {
            // [UnsynchronizedOverridesSynchronized] Unsynchronized method damage overrides synchronized method in DefaultCaret
            @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
            @Override protected synchronized void damage(Rectangle r) {
                if (Objects.nonNull(r)) {
                    JTextComponent c = getComponent();
                    x = 0;
                    y = r.y;
                    width = c.getSize().width;
                    height = r.height;
                    c.repaint();
                }
            }
        };
        // caret.setBlinkRate(getCaret().getBlinkRate());
        caret.setBlinkRate(UIManager.getInt("TextArea.caretBlinkRate"));
        setCaret(caret);
    }
    @Override protected void paintComponent(Graphics g) {
        Caret c = getCaret();
        if (c instanceof DefaultCaret) {
            Graphics2D g2 = (Graphics2D) g.create();
            Insets i = getInsets();
            DefaultCaret caret = (DefaultCaret) c;
            int h = caret.height;
            int y = caret.y;
            g2.setPaint(LINE_COLOR);
            g2.fillRect(i.left, y, getSize().width - i.left - i.right, h);
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
