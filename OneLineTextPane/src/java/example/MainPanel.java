// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    String str = "red green blue aaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    JTextPane textPane = new JTextPane() {
      @Override public void scrollRectToVisible(Rectangle rect) {
        rect.grow(getInsets().right, 0);
        super.scrollRectToVisible(rect);
      }
    };

    // @see https://ateraimemo.com/Swing/NoWrapTextPane.html
    textPane.setEditorKit(new NoWrapEditorKit());

    AbstractDocument doc = new SimpleSyntaxDocument();
    textPane.setDocument(doc);
    try {
      doc.insertString(0, str, null);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
    String key = "Do-Nothing";
    InputMap im = textPane.getInputMap(JComponent.WHEN_FOCUSED);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), key);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), key);
    textPane.getActionMap().put(key, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        // Do nothing
      }
    });

    // @see https://ateraimemo.com/Swing/FocusTraversalKeys.html
    int ftk = KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS;
    Set<AWTKeyStroke> forwardKeys = new HashSet<>(textPane.getFocusTraversalKeys(ftk));
    forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
    forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK));
    textPane.setFocusTraversalKeys(ftk, forwardKeys);

    // // @see https://tips4java.wordpress.com/2009/01/25/no-wrap-text-pane/
    // textPane.addCaretListener(new VisibleCaretListener());

    JScrollPane scrollPane = new JScrollPane(textPane) {
      @Override public void updateUI() {
        super.updateUI();
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setViewportBorder(BorderFactory.createEmptyBorder());
      }

      @Override public Dimension getMinimumSize() {
        return super.getPreferredSize();
      }
    };

    add(makeTitledPanel("JTextField", new JTextField(str)));
    add(makeTitledPanel("JTextPane+StyledDocument+JScrollPane", scrollPane));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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
    return Character.isWhitespace(character.charAt(0)) || OPERANDS.contains(character);
  }
}

class NoWrapParagraphView extends ParagraphView {
  protected NoWrapParagraphView(Element elem) {
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
    if (Objects.nonNull(kind)) {
      if (kind.equals(AbstractDocument.ContentElementName)) {
        return new LabelView(elem);
      } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
        return new NoWrapParagraphView(elem);
      } else if (kind.equals(AbstractDocument.SectionElementName)) {
        return new BoxView(elem, View.Y_AXIS);
      } else if (kind.equals(StyleConstants.ComponentElementName)) {
        return new ComponentView(elem);
      } else if (kind.equals(StyleConstants.IconElementName)) {
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
