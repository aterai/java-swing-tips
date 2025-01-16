// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.MinimalHTMLWriter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    StyledDocument doc = new SimpleSyntaxDocument();
    Style def = doc.getStyle(StyleContext.DEFAULT_STYLE);
    StyleConstants.setForeground(doc.addStyle("red", def), Color.RED);
    StyleConstants.setForeground(doc.addStyle("green", def), Color.GREEN);
    StyleConstants.setForeground(doc.addStyle("blue", def), Color.BLUE);

    JTextPane textPane = new JTextPane(doc);
    textPane.setText("JTextPane(StyledDocument)\nred green, blue.");
    textPane.setSelectedTextColor(null);
    textPane.setSelectionColor(new Color(0x64_32_64_FF, true));
    textPane.setComponentPopupMenu(new TextComponentPopupMenu());

    add(new JScrollPane(textPane));
    add(new JScrollPane(new JEditorPane("text/html", "JEditorPane")));
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

class SimpleSyntaxDocument extends DefaultStyledDocument {
  private static final String OPERANDS = ".,";

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

final class TextComponentPopupMenu extends JPopupMenu {
  /* default */ TextComponentPopupMenu() {
    super();
    add(new DefaultEditorKit.CutAction());
    add(new DefaultEditorKit.CopyAction());
    add(new DefaultEditorKit.PasteAction());
    add("delete").addActionListener(e -> {
      Component c = getInvoker();
      if (c instanceof JTextComponent) {
        ((JTextComponent) c).replaceSelection(null);
      }
    });
    addSeparator();
    add("copy-html-and-text-to-clipboard").addActionListener(e -> {
      Component c = getInvoker();
      if (c instanceof JTextPane) {
        copyHtmlTextToClipboard((JTextPane) c);
      }
    });
    add("copy-html-to-clipboard").addActionListener(e -> {
      Component c = getInvoker();
      if (c instanceof JTextPane) {
        copyHtmlToClipboard((JTextPane) c);
      }
    });
  }

  public void copyHtmlTextToClipboard(JTextPane textPane) {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    int start = textPane.getSelectionStart();
    int end = textPane.getSelectionEnd();
    int length = end - start;
    StyledDocument doc = textPane.getStyledDocument();
    try (OutputStream os = new ByteArrayOutputStream();
         OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
      MinimalHTMLWriter w = new MinimalHTMLWriter(writer, doc, start, length);
      w.write();
      writer.flush();
      String contents = os.toString();
      // ParserDelegator delegator = new ParserDelegator();
      // StringBuilder plainBuf = new StringBuilder();
      // delegator.parse(new StringReader(contents), new HTMLEditorKit.ParserCallback() {
      //   @Override public void handleText(char[] text, int pos) {
      //     plainBuf.append(text);
      //   }
      // }, Boolean.TRUE);
      String str = doc.getText(start, length);
      Transferable transferable = new BasicTransferable(str, contents);
      clipboard.setContents(transferable, null);
    } catch (IOException | BadLocationException ex) {
      // Logger.getGlobal().severe(ex::getMessage);
      UIManager.getLookAndFeel().provideErrorFeedback(textPane);
    }
  }

  public void copyHtmlToClipboard(JTextPane textPane) {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    int start = textPane.getSelectionStart();
    int end = textPane.getSelectionEnd();
    int length = end - start;
    StyledDocument styledDocument = textPane.getStyledDocument();
    try (OutputStream os = new ByteArrayOutputStream();
         OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
      MinimalHTMLWriter w = new MinimalHTMLWriter(writer, styledDocument, start, length);
      w.write();
      writer.flush();
      String contents = os.toString();
      Transferable htmlTransferable = new HtmlTransferable(contents);
      clipboard.setContents(htmlTransferable, null);
    } catch (IOException | BadLocationException ex) {
      // Logger.getGlobal().severe(ex::getMessage);
      UIManager.getLookAndFeel().provideErrorFeedback(textPane);
    }
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTextComponent) {
      JTextComponent tc = (JTextComponent) c;
      boolean hasSelectedText = Objects.nonNull(tc.getSelectedText());
      for (MenuElement menuElement : getSubElements()) {
        Component m = menuElement.getComponent();
        Action a = m instanceof JMenuItem ? ((JMenuItem) m).getAction() : null;
        if (a instanceof DefaultEditorKit.PasteAction) {
          continue;
        }
        m.setEnabled(hasSelectedText);
      }
      super.show(c, x, y);
    }
  }
}

class HtmlTransferable implements Transferable {
  private final String htmlFormattedText;

  protected HtmlTransferable(String htmlFormattedText) {
    this.htmlFormattedText = htmlFormattedText;
  }

  @Override public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {
        DataFlavor.allHtmlFlavor
    };
  }

  @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
    return Arrays.asList(getTransferDataFlavors()).contains(flavor);
  }

  @Override public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException {
    if (Objects.equals(flavor, DataFlavor.allHtmlFlavor)) {
      return htmlFormattedText;
    }
    throw new UnsupportedFlavorException(flavor);
  }
}
