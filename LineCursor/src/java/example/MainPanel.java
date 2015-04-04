package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final JTextArea textArea = new LineCursorTextArea("Line Cursor Test\n\naaaaaaaaaaafasdfas");
    public MainPanel() {
        super(new BorderLayout());
        add(new JCheckBox(new AbstractAction("LineWrap") {
            @Override public void actionPerformed(ActionEvent e) {
                textArea.setLineWrap(((JCheckBox) e.getSource()).isSelected());
                textArea.requestFocusInWindow();
            }
        }), BorderLayout.NORTH);
        add(new JScrollPane(textArea));
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class LineCursorTextArea extends JTextArea {
    private static final Color LINE_COLOR = Color.BLUE;
    private DefaultCaret caret;

    public LineCursorTextArea() {
        super();
    }
    public LineCursorTextArea(Document doc) {
        super(doc);
    }
    public LineCursorTextArea(Document doc, String text, int rows, int columns) {
        super(doc, text, rows, columns);
    }
    public LineCursorTextArea(int rows, int columns) {
        super(rows, columns);
    }
    public LineCursorTextArea(String text) {
        super(text);
    }
    public LineCursorTextArea(String text, int rows, int columns) {
        super(text, rows, columns);
    }

    @Override public void updateUI() {
        //setCaret(null);
        super.updateUI();
        caret = new DefaultCaret() {
            @Override protected synchronized void damage(Rectangle r) {
                if (Objects.nonNull(r)) {
                    JTextComponent c = getComponent();
                    x = 0;
                    y = r.y;
                    width  = c.getSize().width;
                    height = r.height;
                    c.repaint();
                }
            }
        };
        caret.setBlinkRate(getCaret().getBlinkRate());
        setCaret(caret);
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Insets i = getInsets();
        //int y = g2.getFontMetrics().getHeight() * getLineAtCaret(this) + i.top;
        int y = caret.y + caret.height - 1;
        g2.setPaint(LINE_COLOR);
        g2.drawLine(i.left, y, getSize().width - i.left - i.right, y);
        g2.dispose();
    }
//     public static int getLineAtCaret(JTextComponent component) {
//         int caretPosition = component.getCaretPosition();
//         Element root = component.getDocument().getDefaultRootElement();
//         return root.getElementIndex(caretPosition) + 1;
//     }
}
