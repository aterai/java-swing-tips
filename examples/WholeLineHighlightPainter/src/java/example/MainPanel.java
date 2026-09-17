// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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
    add(new JScrollPane(createEditorPane(
        "DefaultHighlightPainter", DefaultHighlightPainter::new)));
    add(new JScrollPane(createEditorPane(
        "ParagraphMarkHighlightPainter", ParagraphMarkHighlightPainter::new)));
    add(new JScrollPane(createEditorPane(
        "WholeLineHighlightPainter", WholeLineHighlightPainter::new)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JEditorPane createEditorPane(
      String title, Function<Color, HighlightPainter> painterFactory) {
    JEditorPane editor = new JEditorPane();
    editor.setEditorKit(new ParagraphMarkEditorKit());
    editor.setText(title + "\n\n123432543543\n");
    // editor.setSelectedTextColor(null);
    Caret caret = new FocusCaret(
        painterFactory.apply(FOCUSED_COLOR),
        painterFactory.apply(UNFOCUSED_COLOR));
    caret.setBlinkRate(editor.getCaret().getBlinkRate());
    editor.setCaret(caret);
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
    Rectangle rect = bounds.getBounds();
    // A view that contains only a line break has a width of at most 1px
    boolean lineBreakOnly = rect.width <= 1;
    if (lineBreakOnly) {
      // Widen the highlight by half the line height so that the selection is visible
      rect.width += rect.height / 2;
    }
    return super.paintLayer(g, offs0, offs1, rect, c, view);
  }
}

class WholeLineHighlightPainter extends DefaultHighlightPainter {
  protected WholeLineHighlightPainter(Color color) {
    super(color);
  }

  @Override public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
    // Extend the view bounds to the right edge of the text component so that
    // the highlight of a selection that continues past the line break fills the whole line
    Rectangle rect = bounds.getBounds();
    rect.width = c.getWidth() - c.getInsets().right - rect.x;
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
    return super.equals(that)
        && Objects.equals(focusedPainter, that.focusedPainter)
        && Objects.equals(unfocusedPainter, that.unfocusedPainter);
  }

  @Override public int hashCode() {
    return Objects.hash(super.hashCode(), focusedPainter, unfocusedPainter);
  }

  @Override public String toString() {
    return String.format(
        "FocusCaret{focusedPainter=%s, unfocusedPainter=%s}",
        focusedPainter,
        unfocusedPainter);
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
