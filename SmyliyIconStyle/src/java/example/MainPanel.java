package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        // String faceMark = "\uD83D\uDE10";
        String faceMark = ":)";

        JTextPane textPane = new JTextPane();
        textPane.setEditorKit(new StyledEditorKit());

        StyledDocument doc = textPane.getStyledDocument();
        doc.addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { /* not needed */ }
            @Override public void insertUpdate(DocumentEvent e) {
                update((DefaultStyledDocument) e.getDocument(), e.getOffset());
            }
            @Override public void removeUpdate(DocumentEvent e) {
                update((DefaultStyledDocument) e.getDocument(), e.getOffset());
            }
            private void update(DefaultStyledDocument doc, int offset) {
                Element elm = doc.getCharacterElement(offset);
                EventQueue.invokeLater(() -> {
                    try {
                        int start = elm.getStartOffset();
                        int end = elm.getEndOffset();
                        System.out.format("start: %d, end: %d%n", start, end);
                        String text = doc.getText(start, end - start);
                        // int pos = text.lastIndexOf(faceMark);
                        // while (pos > -1) {
                        //     Style face = doc.getStyle(faceMark);
                        //     doc.replace(start + pos, faceMark.length(), " ", face);
                        //     pos = text.lastIndexOf(faceMark, pos - 1);
                        //     textPane.getInputAttributes().removeAttributes(face);
                        // }
                        int pos = text.indexOf(faceMark);
                        while (pos > -1) {
                            Style face = doc.getStyle(faceMark);
                            doc.setCharacterAttributes(start + pos, faceMark.length(), face, false);
                            pos = text.indexOf(faceMark, pos + faceMark.length());
                            // textPane.getInputAttributes().removeAttributes(face);
                        }
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                    // MutableAttributeSet inputAttributes = textPane.getInputAttributes();
                    // inputAttributes.removeAttributes(inputAttributes);
                });
            }
        });
        Style face = doc.addStyle(faceMark, doc.getStyle(StyleContext.DEFAULT_STYLE));
        StyleConstants.setIcon(face, new FaceIcon());
        // StyleConstants.setForeground(face, Color.RED);

        // textPane.setText("aaaa \uD83D\uDE10 aaaaaa :) asdfa :-) aaaa\n");
        textPane.setText("aaaa 😐 aaaaaa :) asdfa :-) aaaa\n");

        add(new JScrollPane(textPane));
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

class FaceIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setColor(Color.RED);
        g2.drawOval(1,   1, 14, 14);
        g2.drawLine(5,  10,  6, 10);
        g2.drawLine(7,  11,  9, 11);
        g2.drawLine(10, 10, 11, 10);
        g2.drawOval(4,   5,  1,  1);
        g2.drawOval(10,  5,  1,  1);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 16;
    }
    @Override public int getIconHeight() {
        return 16;
    }
}
