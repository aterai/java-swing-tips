package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTextPane textPane = new JTextPane(new SimpleSyntaxDocument());
        textPane.setText("red green, blue. red-green;bleu.");
        add(new JScrollPane(textPane));
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

//This code is taken from: SyntaxDocument.java, MultiSyntaxDocument.java
// Fast styled JTextPane editor | Oracle Community
// @author camickr
// @author David Underhill
// https://community.oracle.com/thread/2105230
// modified by aterai aterai@outlook.com
class SimpleSyntaxDocument extends DefaultStyledDocument {
    //HashMap<String, AttributeSet> keywords = new HashMap<>();
    private final Style normal; //MutableAttributeSet normal = new SimpleAttributeSet();
    private static final String OPERANDS = ".,";
    public SimpleSyntaxDocument() {
        super();
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        normal = addStyle("normal", def);
        StyleConstants.setForeground(normal, Color.BLACK);
        StyleConstants.setForeground(addStyle("red",   normal), Color.RED);
        StyleConstants.setForeground(addStyle("green", normal), Color.GREEN);
        StyleConstants.setForeground(addStyle("blue",  normal), Color.BLUE);
    }
    @Override public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offset, str, a);
        processChangedLines(offset, str.length());
    }
    @Override public void remove(int offset, int length) throws BadLocationException {
        super.remove(offset, length);
        processChangedLines(offset, 0);
    }
    private void processChangedLines(int offset, int length) throws BadLocationException {
        Element root = getDefaultRootElement();
        int startLine = root.getElementIndex(offset);
        int endLine = root.getElementIndex(offset + length);
        for (int line = startLine; line <= endLine; line++) {
            applyHighlighting(line);
        }
    }
    private void applyHighlighting(int line) throws BadLocationException {
        Element root = getDefaultRootElement();
        int startOffset   = root.getElement(line).getStartOffset();
        int endOffset     = root.getElement(line).getEndOffset() - 1;
        int lineLength    = endOffset - startOffset;
        int contentLength = getLength();
        if (endOffset >= contentLength) {
            endOffset = contentLength - 1;
        }
        setCharacterAttributes(startOffset, lineLength, normal, true);
        checkForTokens(startOffset, endOffset);
    }
    private void checkForTokens(int startOffset, int endOffset) throws BadLocationException {
        int index = startOffset;
        while (index <= endOffset) {
            while (isDelimiter(getText(index, 1))) {
                if (index < endOffset) {
                    index++;
                } else {
                    return;
                }
            }
            index = getOtherToken(index, endOffset);
        }
    }
    private int getOtherToken(int startOffset, int endOffset) throws BadLocationException {
        int endOfToken = startOffset + 1;
        while (endOfToken <= endOffset) {
            if (isDelimiter(getText(endOfToken, 1))) {
                break;
            }
            endOfToken++;
        }
        String token = getText(startOffset, endOfToken - startOffset);
        Style s = getStyle(token);
        //if (keywords.containsKey(token)) {
        //    setCharacterAttributes(startOffset, endOfToken - startOffset, keywords.get(token), false);
        if (Objects.nonNull(s)) {
            setCharacterAttributes(startOffset, endOfToken - startOffset, s, false);
        }
        return endOfToken + 1;
    }
    protected boolean isDelimiter(String character) {
        return Character.isWhitespace(character.charAt(0)) || OPERANDS.indexOf(character) != -1;
    }
}
