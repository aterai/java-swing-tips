// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
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
      Logger.getGlobal().severe(ex::getMessage);
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
  private static final String DELIMITERS = ".,";

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
    int startLine = root.getElementIndex(offset);
    int endLine = root.getElementIndex(offset + length);
    for (int i = startLine; i <= endLine; i++) {
      applyHighlighting(root.getElement(i));
    }
  }

  private void applyHighlighting(Element line) throws BadLocationException {
    int startOffset = line.getStartOffset();
    // Exclude the trailing newline; the last line element also ends with
    // an implied newline at getLength() + 1, so this never exceeds the length
    int endOffset = line.getEndOffset() - 1;
    // Fetch only the changed line instead of the whole document text
    String text = getText(startOffset, endOffset - startOffset);
    setCharacterAttributes(startOffset, text.length(), getStyle(StyleContext.DEFAULT_STYLE), true);
    highlightKeywords(text, startOffset);
  }

  private void highlightKeywords(String text, int lineOffset) {
    int length = text.length();
    int index = 0;
    while (index < length) {
      int tokenStart = index;
      while (index < length && !isDelimiter(text.charAt(index))) {
        index++;
      }
      if (index > tokenStart) {
        Style style = getStyle(text.substring(tokenStart, index));
        if (Objects.nonNull(style)) {
          setCharacterAttributes(lineOffset + tokenStart, index - tokenStart, style, false);
        }
      }
      index++; // skip the delimiter
    }
  }

  protected boolean isDelimiter(char ch) {
    return Character.isWhitespace(ch) || DELIMITERS.indexOf(ch) >= 0;
  }
}
