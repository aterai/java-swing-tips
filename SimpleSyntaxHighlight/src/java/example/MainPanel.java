// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

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

// This code is taken from: SyntaxDocument.java, MultiSyntaxDocument.java
// Fast styled JTextPane editor | Oracle Community
// @author camickr
// @author David Underhill
// https://community.oracle.com/thread/2105230
// modified by aterai aterai@outlook.com
class SimpleSyntaxDocument extends DefaultStyledDocument {
  private static final char LB = '\n';
  // HashMap<String, AttributeSet> keywords = new HashMap<>();
  private static final String OPERANDS = ".,";
  private final Style def = getStyle(StyleContext.DEFAULT_STYLE);

  protected SimpleSyntaxDocument() {
    super();
    // Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    StyleConstants.setForeground(addStyle("red", def), Color.RED);
    StyleConstants.setForeground(addStyle("green", def), Color.GREEN);
    StyleConstants.setForeground(addStyle("blue", def), Color.BLUE);
  }

  @Override public void insertString(int offset, String text, AttributeSet a) throws BadLocationException {
    // @see PlainDocument#insertString(...)
    int length = 0;
    String str = text;
    if (Objects.nonNull(str) && str.indexOf(LB) >= 0) {
      StringBuilder filtered = new StringBuilder(str);
      int n = filtered.length();
      for (int i = 0; i < n; i++) {
        if (filtered.charAt(i) == LB) {
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
    int endLine = root.getElementIndex(offset + length);
    for (int i = startLine; i <= endLine; i++) {
      applyHighlighting(content, i);
    }
  }

  private void applyHighlighting(String content, int line) throws BadLocationException {
    Element root = getDefaultRootElement();
    int startOffset = root.getElement(line).getStartOffset();
    int endOffset = root.getElement(line).getEndOffset() - 1;
    int lineLength = endOffset - startOffset;
    int contentLength = content.length();
    endOffset = endOffset >= contentLength ? contentLength - 1 : endOffset;
    setCharacterAttributes(startOffset, lineLength, def, true);
    checkForTokens(content, startOffset, endOffset);
  }

  private void checkForTokens(String content, int startOffset, int endOffset) {
    int index = startOffset;
    while (index <= endOffset) {
      while (isDelimiter(content.substring(index, index + 1))) {
        if (index < endOffset) {
          index++;
        } else {
          return;
        }
      }
      index = getOtherToken(content, index, endOffset);
    }
  }

  private int getOtherToken(String content, int startOffset, int endOffset) {
    int endOfToken = startOffset + 1;
    while (endOfToken <= endOffset) {
      if (isDelimiter(content.substring(endOfToken, endOfToken + 1))) {
        break;
      }
      endOfToken++;
    }
    String token = content.substring(startOffset, endOfToken);
    Style s = getStyle(token);
    // if (keywords.containsKey(token)) {
    //  setCharacterAttributes(startOffset, endOfToken - startOffset, keywords.get(token), false);
    if (Objects.nonNull(s)) {
      setCharacterAttributes(startOffset, endOfToken - startOffset, s, false);
    }
    return endOfToken + 1;
  }

  protected boolean isDelimiter(String character) {
    return Character.isWhitespace(character.charAt(0)) || OPERANDS.indexOf(character) != -1;
  }
}
