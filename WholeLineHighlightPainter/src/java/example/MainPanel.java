// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.util.Objects;
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
  private MainPanel() {
    super(new GridLayout(0, 1));

    JEditorPane editor0 = makeEditorPane("DefaultHighlightPainter");
    Caret caret0 = new FocusCaret(
        new DefaultHighlightPainter(new Color(0xAA_CC_DD_FF, true)),
        new DefaultHighlightPainter(new Color(0xEE_EE_EE_EE, true)));
    caret0.setBlinkRate(editor0.getCaret().getBlinkRate());
    editor0.setCaret(caret0);

    JEditorPane editor1 = makeEditorPane("ParagraphMarkHighlightPainter");
    Caret caret1 = new FocusCaret(
        new ParagraphMarkHighlightPainter(new Color(0xAA_CC_DD_FF, true)),
        new ParagraphMarkHighlightPainter(new Color(0xEE_EE_EE_EE, true)));
    caret1.setBlinkRate(editor1.getCaret().getBlinkRate());
    editor1.setCaret(caret1);

    JEditorPane editor2 = makeEditorPane("WholeLineHighlightPainter");
    Caret caret2 = new FocusCaret(
        new WholeLineHighlightPainter(new Color(0xAA_CC_DD_FF, true)),
        new WholeLineHighlightPainter(new Color(0xEE_EE_EE_EE, true)));
    caret2.setBlinkRate(editor2.getCaret().getBlinkRate());
    editor2.setCaret(caret2);

    add(new JScrollPane(editor0));
    add(new JScrollPane(editor1));
    add(new JScrollPane(editor2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JEditorPane makeEditorPane(String txt) {
    JEditorPane editor = new JEditorPane();
    editor.setEditorKit(new ParagraphMarkEditorKit());
    editor.setText(txt + "\n\n123432543543\n");
    editor.setSelectionColor(new Color(0xAA_CC_DD_FF, true));
    // editor.setSelectedTextColor(null);
    return editor;
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
  private final HighlightPainter nonFocusPainter;
  private final HighlightPainter selectionPainter;

  protected FocusCaret(HighlightPainter selectionPainter, HighlightPainter nonFocusPainter) {
    super();
    this.selectionPainter = selectionPainter;
    this.nonFocusPainter = nonFocusPainter;
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
    return getComponent().hasFocus() ? selectionPainter : nonFocusPainter;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FocusCaret)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    FocusCaret that = (FocusCaret) o;
    return Objects.equals(nonFocusPainter, that.nonFocusPainter)
        && Objects.equals(getSelectionPainter(), that.getSelectionPainter());
  }

  @Override public int hashCode() {
    return Objects.hash(super.hashCode(), nonFocusPainter, getSelectionPainter());
  }

  @Override public String toString() {
    return "FocusCaret{" + "nonFocusPainter=" + nonFocusPainter + ", selectionPainter=" + selectionPainter + '}';
  }
}

class ParagraphMarkEditorKit extends StyledEditorKit implements ViewFactory {
  @Override public ViewFactory getViewFactory() {
    return this;
  }

  @Override public View create(Element elem) {
    String kind = elem.getName();
    if (Objects.nonNull(kind)) {
      if (kind.equals(AbstractDocument.ContentElementName)) {
        return new LabelView(elem);
      } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
        return new ParagraphWithEndMarkView(elem);
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

class ParagraphWithEndMarkView extends ParagraphView {
  private static final Icon PARAGRAPH_MARK = new ParagraphMarkIcon();

  protected ParagraphWithEndMarkView(Element elem) {
    super(elem);
  }

  @Override public void paint(Graphics g, Shape allocation) {
    super.paint(g, allocation);
    try {
      Shape para = modelToView(getEndOffset(), allocation, Bias.Backward);
      Rectangle r = Objects.nonNull(para) ? para.getBounds() : allocation.getBounds();
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
