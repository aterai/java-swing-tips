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
import javax.swing.text.StyledDocument;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    StyledDocument doc = new SimpleSyntaxDocument();
    // Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    Style def = doc.getStyle(StyleContext.DEFAULT_STYLE);
    StyleConstants.setForeground(doc.addStyle("red", def), Color.RED);
    StyleConstants.setForeground(doc.addStyle("green", def), Color.GREEN);
    StyleConstants.setForeground(doc.addStyle("blue", def), Color.BLUE);
    JTextPane textPane = new JTextPane(doc);
    textPane.setText("red green, blue.\n  red-green;blue.");
    add(new JScrollPane(textPane));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
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
// modified by aterai@outlook.com
class SimpleSyntaxDocument extends DefaultStyledDocument {
  private static final String OPERANDS = ".,";
  // HashMap<String, AttributeSet> keywords = new HashMap<>();

  @Override public void insertString(int offset, String text, AttributeSet a) throws BadLocationException {
    super.insertString(offset, text, a);
    processChangedLines(offset, text.length());
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

  private void applyHighlighting(String content, int line) {
    Element root = getDefaultRootElement();
    int startOffset = root.getElement(line).getStartOffset();
    int endOffset = root.getElement(line).getEndOffset() - 1;
    int lineLength = endOffset - startOffset;
    int contentLength = content.length();
    endOffset = endOffset >= contentLength ? contentLength - 1 : endOffset;
    setCharacterAttributes(startOffset, lineLength, getStyle(StyleContext.DEFAULT_STYLE), true);
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
    return Character.isWhitespace(character.charAt(0)) || OPERANDS.contains(character);
  }
}
