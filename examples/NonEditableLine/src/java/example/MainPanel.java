// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    int maskRange = 2;
    HighlightPainter highlightPainter = new DefaultHighlightPainter(Color.GRAY);
    JTextArea textArea = new JTextArea();
    textArea.setText("Non editable lines\naaa bbb ccc ddd eee\n1234567890\n1234567890987654321");
    DocumentFilter filter = new NonEditableLineDocumentFilter(maskRange);
    Document doc = textArea.getDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).setDocumentFilter(filter);
    }
    Highlighter highlighter = textArea.getHighlighter();
    Element root = doc.getDefaultRootElement();
    try {
      for (int i = 0; i < maskRange; i++) { // root.getElementCount(); i++) {
        Element elm = root.getElement(i);
        highlighter.addHighlight(elm.getStartOffset(), elm.getEndOffset() - 1, highlightPainter);
      }
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
    add(new JScrollPane(textArea));
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

class NonEditableLineDocumentFilter extends DocumentFilter {
  private final int maskRange;

  protected NonEditableLineDocumentFilter(int maskRange) {
    super();
    this.maskRange = maskRange;
  }

  @Override public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
    if (Objects.nonNull(text)) {
      replace(fb, offset, 0, text, attr);
    }
  }

  @Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
    replace(fb, offset, length, "", null);
  }

  @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
    Document doc = fb.getDocument();
    if (doc.getDefaultRootElement().getElementIndex(offset) >= maskRange) {
      fb.replace(offset, length, text, attrs);
    }
  }
}
