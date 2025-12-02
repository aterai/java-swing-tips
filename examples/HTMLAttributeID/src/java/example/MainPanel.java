// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public final class MainPanel extends JPanel {
  public static final String LOGGER_NAME = MethodHandles.lookup().lookupClass().getName();
  public static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);
  public static final String SEPARATOR = "----%n%s%n";
  public final JEditorPane editorPane = new JEditorPane();
  public final JTextField field = new JTextField("3");
  public final Action elementIdAction = new AbstractAction("Element#getElement(id)") {
    @Override public void actionPerformed(ActionEvent e) {
      LOGGER.info(() -> String.format(SEPARATOR, getValue(NAME)));
      String id = field.getText().trim();
      HTMLDocument doc = (HTMLDocument) editorPane.getDocument();
      Element element = doc.getElement(id);
      if (Objects.nonNull(element)) {
        LOGGER.info(() -> String.format("found: %s%n", element));
        editorPane.requestFocusInWindow();
        editorPane.select(element.getStartOffset(), element.getEndOffset());
      }
    }
  };
  public final Action highlightAction = new AbstractAction("Highlight Element[@id]") {
    @Override public void actionPerformed(ActionEvent e) {
      LOGGER.info(() -> String.format(SEPARATOR, getValue(NAME)));
      JToggleButton b = (JToggleButton) e.getSource();
      if (b.isSelected()) {
        for (Element root : editorPane.getDocument().getRootElements()) {
          EditorPaneUtils.traverseElementById(editorPane, root);
        }
      } else {
        editorPane.getHighlighter().removeAllHighlights();
      }
    }
  };
  public final Action parserAction = new AbstractAction("ParserDelegator") {
    @Override public void actionPerformed(ActionEvent e) {
      LOGGER.info(() -> String.format(SEPARATOR, getValue(NAME)));
      String id = field.getText().trim();
      String text = editorPane.getText();
      EditorPaneUtils.parser(text, id);
    }
  };

  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    LOGGER.setUseParentHandlers(false);
    LOGGER.setLevel(Level.ALL);
    LOGGER.addHandler(new TextAreaHandler(new TextAreaOutputStream(textArea)));

    editorPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
    String span2 = "<span id='2'>345678</span>";
    String span0 = "<span class='insert' id='0'>6</span>";
    String span1 = "<span id='1'>8</span>";
    String div3 = "<div class='fff' id='3'>123</div>";
    String html = "<html>12%s90<p>1<a href='..'>23</a>45%s7%s90%s4567890</p>";
    editorPane.setText(String.format(html, span2, span0, span1, div3));
    DefaultHighlighter dh = (DefaultHighlighter) editorPane.getHighlighter();
    dh.setDrawsLayeredHighlights(false);

    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setTopComponent(new JScrollPane(editorPane));
    sp.setBottomComponent(new JScrollPane(textArea));

    JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(field);
    p.add(new JButton(elementIdAction));
    p.add(new JToggleButton(highlightAction));
    p.add(new JButton(parserAction));
    add(sp);
    add(p, BorderLayout.SOUTH);
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

class TextAreaOutputStream extends OutputStream {
  private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  private final JTextArea textArea;

  protected TextAreaOutputStream(JTextArea textArea) {
    super();
    this.textArea = textArea;
  }

  // // Java 10:
  // @Override public void flush() {
  //   textArea.append(buffer.toString(StandardCharsets.UTF_8));
  //   buffer.reset();
  // }

  @Override public void flush() throws IOException {
    textArea.append(buffer.toString("UTF-8"));
    buffer.reset();
  }

  @Override public void write(int b) {
    buffer.write(b);
  }

  @Override public void write(byte[] b, int off, int len) {
    buffer.write(b, off, len);
  }
}

class TextAreaHandler extends StreamHandler {
  protected TextAreaHandler(OutputStream os) {
    super(os, new SimpleFormatter());
  }

  @Override public String getEncoding() {
    return StandardCharsets.UTF_8.name();
  }

  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @Override public synchronized void publish(LogRecord logRecord) {
    super.publish(logRecord);
    flush();
  }

  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @Override public synchronized void close() {
    flush();
  }
}

final class EditorPaneUtils {
  private static final Logger LOGGER = MainPanel.LOGGER;

  private EditorPaneUtils() {
    /* Singleton */
  }

  public static void parser(String text, String id) {
    ParserDelegator delegator = new ParserDelegator();
    try {
      delegator.parse(new StringReader(text), new HTMLEditorKit.ParserCallback() {
        @Override public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
          Object attrId = a.getAttribute(HTML.Attribute.ID);
          LOGGER.info(() -> String.format("%s@id=%s%n", tag, attrId));
          if (id.equals(attrId)) {
            LOGGER.info(() -> String.format("found: pos=%d%n", pos));
            int endOffs = text.indexOf('>', pos);
            LOGGER.info(() -> String.format("%s%n", text.substring(pos, endOffs + 1)));
          }
        }
      }, Boolean.TRUE);
    } catch (IOException ex) {
      LOGGER.info(() -> String.format("%s%n", ex.getMessage()));
      // UIManager.getLookAndFeel().provideErrorFeedback(editorPane);
    }
  }

  public static void traverseElementById(JEditorPane editorPane, Element element) {
    if (element.isLeaf()) {
      checkId(editorPane, element);
    } else {
      for (int i = 0; i < element.getElementCount(); i++) {
        Element child = element.getElement(i);
        checkId(editorPane, child);
        if (!child.isLeaf()) {
          traverseElementById(editorPane, child);
        }
      }
    }
  }

  public static void addHighlight(JEditorPane editorPane, Element element, boolean isBlock) {
    // Highlighter highlighter = editorPane.getHighlighter();
    DefaultHighlightPainter painter = new DefaultHighlightPainter(Color.YELLOW);
    int start = element.getStartOffset();
    int lf = isBlock ? 1 : 0;
    int end = element.getEndOffset() - lf; // lf???, setDrawsLayeredHighlights(false) bug???
    try {
      editorPane.getHighlighter().addHighlight(start, end, painter);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }

  public static void checkId(JEditorPane editorPane, Element element) {
    AttributeSet attrs = element.getAttributes();
    Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
    Object name = elementName == null ? attrs.getAttribute(StyleConstants.NameAttribute) : null;
    HTML.Tag tag;
    if (name instanceof HTML.Tag) {
      tag = (HTML.Tag) name;
      LOGGER.info(() -> String.format("%s%n", tag));
      if (tag.isBlock()) { // block
        blockHighlight(editorPane, element, attrs);
      } else { // inline
        inlineHighlight(editorPane, element, attrs);
      }
    }
  }

  public static void blockHighlight(JEditorPane editorPane, Element element, AttributeSet attrs) {
    Optional.ofNullable(attrs.getAttribute(HTML.Attribute.ID))
        .ifPresent(id -> {
          LOGGER.info(() -> String.format("block: id=%s%n", id));
          addHighlight(editorPane, element, true);
        });
    // Object bid = attrs.getAttribute(HTML.Attribute.ID);
    // if (Objects.nonNull(bid)) {
    //   textArea.append(String.format("block: id=%s%n", bid));
    //   addHighlight(element, true);
    // }
  }

  public static void inlineHighlight(JEditorPane editor, Element elm, AttributeSet attrs) {
    Collections.list(attrs.getAttributeNames()).stream()
        .filter(AttributeSet.class::isInstance)
        .map(AttributeSet.class::cast)
        .map(a -> a.getAttribute(HTML.Attribute.ID))
        .filter(Objects::nonNull)
        .forEach(id -> {
          LOGGER.info(() -> String.format("inline: id=%s%n", id));
          addHighlight(editor, elm, false);
        });
    // Enumeration<?> e = attrs.getAttributeNames();
    // while (e.hasMoreElements()) {
    //   Object obj = attrs.getAttribute(e.nextElement());
    //   // System.out.println("AttributeNames: " + obj);
    //   if (obj instanceof AttributeSet) {
    //     AttributeSet a = (AttributeSet) obj;
    //     Object iid = a.getAttribute(HTML.Attribute.ID);
    //     if (Objects.nonNull(iid)) {
    //       LOGGER.info(() -> String.format("inline: id=%s%n", iid));
    //       addHighlight(elm, false);
    //     }
    //   }
    // }
  }
}
