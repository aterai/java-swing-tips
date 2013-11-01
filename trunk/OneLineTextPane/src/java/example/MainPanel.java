package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(2, 1));
        String str = "red green blue aaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        //JTextField textField = new JTextField(str);
        add(makeTitlePanel(new JTextField(str), "JTextField"));
        add(makeTitlePanel(makeOneLineTextPane(str), "JTextPane+StyledDocument+JScrollPane"));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setPreferredSize(new Dimension(320, 240));
    }

    private JComponent makeTitlePanel(JComponent cmp, String title) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        p.add(cmp, c);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
    }

    public JComponent makeOneLineTextPane(String text) {
        JTextPane textPane = new JTextPane() {
            @Override public void scrollRectToVisible(Rectangle rect) {
                int r = getBorder().getBorderInsets(this).right;
                rect.grow(r, 0);
                super.scrollRectToVisible(rect);
            }
        };

        // @see http://terai.xrea.jp/Swing/NoWrapTextPane.html
        textPane.setEditorKit(new NoWrapEditorKit());

        AbstractDocument doc = new SimpleSyntaxDocument();
        textPane.setDocument(doc);
        try{
            doc.insertString(0, text, null);
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        String KEY = "Do-Nothing";
        InputMap im = textPane.getInputMap(JComponent.WHEN_FOCUSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), KEY);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), key);
        textPane.getActionMap().put(key, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                // Do nothing
            }
        });

        // @see http://terai.xrea.jp/Swing/FocusTraversalKeys.html
        Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(textPane.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
        forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK));
        textPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);

        //// @see http://tips4java.wordpress.com/2009/01/25/no-wrap-text-pane/
        // textPane.addCaretListener(new VisibleCaretListener());

        JScrollPane scrollPane = new JScrollPane(
              textPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
              ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
            @Override public Dimension getMinimumSize() {
                return super.getPreferredSize();
            }
        };

        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        return scrollPane;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//@see http://www.discoverteenergy.com/files/SyntaxDocument.java
class SimpleSyntaxDocument extends DefaultStyledDocument {
    //HashMap<String,AttributeSet> keywords = new HashMap<String,AttributeSet>();
    private final Style normal; //MutableAttributeSet normal = new SimpleAttributeSet();
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
        // @see PlainDocument#insertString(...)
        int length = 0;
        if(str != null && str.indexOf('\n') >= 0) {
            StringBuilder filtered = new StringBuilder(str);
            int n = filtered.length();
            for(int i = 0; i < n; i++) {
                if(filtered.charAt(i) == '\n') {
                    filtered.setCharAt(i, ' ');
                }
            }
            str = filtered.toString();
            length = str.length();
        }
        super.insertString(offset, str, a);
        processChangedLines(offset, length);
    }
    @Override public void remove(int offset, int length) throws BadLocationException {
        super.remove(offset, length);
        processChangedLines(offset, 0);
    }
    private void processChangedLines(int offset, int length) throws BadLocationException {
        Element root = getDefaultRootElement();
        String content = getText(0, getLength());
        int startLine = root.getElementIndex(offset);
        int endLine = root.getElementIndex(offset+length);
        for(int i=startLine;i<=endLine;i++) {
            applyHighlighting(content, i);
        }
    }
    private void applyHighlighting(String content, int line) throws BadLocationException {
        Element root = getDefaultRootElement();
        int startOffset   = root.getElement(line).getStartOffset();
        int endOffset     = root.getElement(line).getEndOffset() - 1;
        int lineLength    = endOffset - startOffset;
        int contentLength = content.length();
        if(endOffset >= contentLength) endOffset = contentLength - 1;
        setCharacterAttributes(startOffset, lineLength, normal, true);
        checkForTokens(content, startOffset, endOffset);
    }
    private void checkForTokens(String content, int startOffset, int endOffset) {
        while(startOffset <= endOffset) {
            while(isDelimiter(content.substring(startOffset, startOffset+1))) {
                if(startOffset < endOffset) {
                    startOffset++;
                }else{
                    return;
                }
            }
            startOffset = getOtherToken(content, startOffset, endOffset);
        }
    }
    private int getOtherToken(String content, int startOffset, int endOffset) {
        int endOfToken = startOffset + 1;
        while(endOfToken <= endOffset) {
            if(isDelimiter(content.substring(endOfToken, endOfToken + 1) ) ) {
                break;
            }
            endOfToken++;
        }
        String token = content.substring(startOffset, endOfToken);
        Style s = getStyle(token);
        //if(keywords.containsKey(token)) {
        //    setCharacterAttributes(startOffset, endOfToken - startOffset, keywords.get(token), false);
        if(s!=null) {
            setCharacterAttributes(startOffset, endOfToken - startOffset, s, false);
        }
        return endOfToken + 1;
    }
    String operands = ".,";
    protected boolean isDelimiter(String character) {
        return Character.isWhitespace(character.charAt(0)) || operands.indexOf(character)!=-1;
    }
}

class NoWrapParagraphView extends ParagraphView {
    public NoWrapParagraphView(Element elem) {
        super(elem);
    }
    @Override protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
        SizeRequirements req = super.calculateMinorAxisRequirements(axis, r);
        req.minimum = req.preferred;
        return req;
    }
    @Override public int getFlowSpan(int index) {
        return Integer.MAX_VALUE;
    }
}

class NoWrapViewFactory implements ViewFactory {
    @Override public View create(Element elem) {
        String kind = elem.getName();
        if(kind != null) {
            if(kind.equals(AbstractDocument.ContentElementName)) {
                return new LabelView(elem);
            }else if(kind.equals(AbstractDocument.ParagraphElementName)) {
                return new NoWrapParagraphView(elem);
            }else if(kind.equals(AbstractDocument.SectionElementName)) {
                return new BoxView(elem, View.Y_AXIS);
            }else if(kind.equals(StyleConstants.ComponentElementName)) {
                return new ComponentView(elem);
            }else if(kind.equals(StyleConstants.IconElementName)) {
                return new IconView(elem);
            }
        }
        return new LabelView(elem);
    }
}

class NoWrapEditorKit extends StyledEditorKit {
    @Override public ViewFactory getViewFactory() {
        return new NoWrapViewFactory();
    }
}
