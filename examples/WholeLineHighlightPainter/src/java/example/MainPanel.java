// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.Caret;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.IconView;
import javax.swing.text.JTextComponent;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public final class MainPanel extends JPanel {
  private static final Color FOCUSED_COLOR = new Color(0xAA_CC_DD_FF, true);
  private static final Color UNFOCUSED_COLOR = new Color(0xEE_EE_EE_EE, true);

  private MainPanel() {
    super(new GridLayout(0, 1));
    JEditorPane defaultEditor = createEditorPane("DefaultHighlightPainter");
    Caret defaultCaret = new FocusCaret(
        new DefaultHighlightPainter(FOCUSED_COLOR),
        new DefaultHighlightPainter(UNFOCUSED_COLOR));
    defaultCaret.setBlinkRate(defaultEditor.getCaret().getBlinkRate());
    defaultEditor.setCaret(defaultCaret);

    JEditorPane paragraphEditor = createEditorPane("ParagraphMarkHighlightPainter");
    Caret paragraphCaret = new FocusCaret(
        new ParagraphMarkHighlightPainter(FOCUSED_COLOR),
        new ParagraphMarkHighlightPainter(UNFOCUSED_COLOR));
    paragraphCaret.setBlinkRate(paragraphEditor.getCaret().getBlinkRate());
    paragraphEditor.setCaret(paragraphCaret);

    JEditorPane wholeLineEditor = createEditorPane("WholeLineHighlightPainter");
    Caret wholeLineCaret = new FocusCaret(
        new WholeLineHighlightPainter(FOCUSED_COLOR),
        new WholeLineHighlightPainter(UNFOCUSED_COLOR));
    wholeLineCaret.setBlinkRate(wholeLineEditor.getCaret().getBlinkRate());
    wholeLineEditor.setCaret(wholeLineCaret);

    add(new JScrollPane(defaultEditor));
    add(new JScrollPane(paragraphEditor));
    add(new JScrollPane(wholeLineEditor));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JEditorPane createEditorPane(String text) {
    JEditorPane editor = new JEditorPane();
    editor.setEditorKit(new ParagraphMarkEditorKit());
    editor.setText(text + "\n\n123432543543\n");
    editor.setSelectionColor(FOCUSED_COLOR);
    // editor.setSelectedTextColor(null);
    return editor;
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

class ParagraphMarkHighlightPainter extends DefaultHighlightPainter {
  protected ParagraphMarkHighlightPainter(Color color) {
    super(color);
  }

  @Override public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
    Shape s = super.paintLayer(g, offs0, offs1, bounds, c, view);
    Rectangle r = s.getBounds();
    if (r.width - 1 <= 0) {
      g.fillRect(r.x + r.width, r.y, r.width + r.height / 2, r.height);
    }
    return s;
  }
}

class WholeLineHighlightPainter extends DefaultHighlightPainter {
  protected WholeLineHighlightPainter(Color color) {
    super(color);
  }

  @Override public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
    Rectangle rect = bounds.getBounds();
    rect.width = c.getSize().width;
    return super.paintLayer(g, offs0, offs1, rect, c, view);
  }
}

class FocusCaret extends DefaultCaret {
  private final transient HighlightPainter focusedPainter;
  private final transient HighlightPainter unfocusedPainter;

  protected FocusCaret(HighlightPainter focusedPainter, HighlightPainter unfocusedPainter) {
    super();
    this.focusedPainter = focusedPainter;
    this.unfocusedPainter = unfocusedPainter;
  }

  @Override public void focusLost(FocusEvent e) {
    super.focusLost(e);
    setSelectionVisible(true);
  }

  @Override public void focusGained(FocusEvent e) {
    super.focusGained(e);
    setSelectionVisible(false); // removeHighlight
    setSelectionVisible(true); // addHighlight
  }

  @Override protected HighlightPainter getSelectionPainter() {
    return getComponent().hasFocus() ? focusedPainter : unfocusedPainter;
  }

  @Override public boolean equals(Object o) {
    return this == o || o instanceof FocusCaret && isSameState((FocusCaret) o);
  }

  private boolean isSameState(FocusCaret that) {
    boolean unfocusedEquals = Objects.equals(unfocusedPainter, that.unfocusedPainter);
    boolean focusedEquals = Objects.equals(getSelectionPainter(), that.getSelectionPainter());
    return super.equals(that) && unfocusedEquals && focusedEquals;
  }

  @Override public int hashCode() {
    return Objects.hash(super.hashCode(), unfocusedPainter, getSelectionPainter());
  }

  @Override public String toString() {
    return String.format(
        "FocusCaret{unfocusedPainter=%s, focusedPainter=%s}",
        unfocusedPainter,
        focusedPainter);
  }
}

class ParagraphMarkEditorKit extends StyledEditorKit implements ViewFactory {
  @Override public ViewFactory getViewFactory() {
    return this;
  }

  @SuppressWarnings({"PMD.OnlyOneReturn", "ReturnCount"})
  @Override public View create(Element elem) {
    switch (elem.getName()) {
      // case AbstractDocument.ContentElementName:
      //   return new LabelView(elem);
      case AbstractDocument.ParagraphElementName:
        return new ParagraphWithEndMarkView(elem);
      case AbstractDocument.SectionElementName:
        return new BoxView(elem, View.Y_AXIS);
      case StyleConstants.ComponentElementName:
        return new ComponentView(elem);
      case StyleConstants.IconElementName:
        return new IconView(elem);
      default:
        return new LabelView(elem);
    }
  }

  // Java 12:
  // @Override public View create(Element elem) {
  //   return switch (elem.getName()) {
  //     case AbstractDocument.ParagraphElementName -> new ParagraphWithEndMarkView(elem);
  //     case AbstractDocument.SectionElementName -> new BoxView(elem, View.Y_AXIS);
  //     case StyleConstants.ComponentElementName -> new ComponentView(elem);
  //     case StyleConstants.IconElementName -> new IconView(elem);
  //     default -> new LabelView(elem);
  //   };
  // }
}

class ParagraphWithEndMarkView extends ParagraphView {
  private static final Icon PARAGRAPH_MARK = new ParagraphMarkIcon();

  protected ParagraphWithEndMarkView(Element elem) {
    super(elem);
  }

  @Override public void paint(Graphics g, Shape allocation) {
    super.paint(g, allocation);
    try {
      Shape para = modelToView(getEndOffset(), allocation, Bias.Backward);
      // Rectangle r = Objects.nonNull(para)
      //     ? para.getBounds()
      //     : allocation.getBounds();
      Rectangle r = Optional.ofNullable(para)
          .map(Shape::getBounds)
          .orElseGet(allocation::getBounds);
      PARAGRAPH_MARK.paintIcon(null, g, r.x, r.y);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }
}

class ParagraphMarkIcon implements Icon {
  private static final Color MARK_COLOR = new Color(0x78_82_6E);
  private final Polygon paragraphMark = new Polygon();

  protected ParagraphMarkIcon() {
    paragraphMark.addPoint(1, 7);
    paragraphMark.addPoint(3, 7);
    paragraphMark.addPoint(3, 11);
    paragraphMark.addPoint(4, 11);
    paragraphMark.addPoint(1, 14);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(MARK_COLOR);
    g2.translate(x, y);
    g2.draw(paragraphMark);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 3;
  }

  @Override public int getIconHeight() {
    return 7;
  }
}
