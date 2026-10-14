// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public final class MainPanel extends JPanel {
  private static final String TAB_TXT = "\n1\taaa\n12\taaa\n123\taaa\n1234\taaa\t\t\t\t\t\t\n";
  private static final String FULL_SPACE_TXT = String.join("\n",
      "123456789012",
      "bbb2　　　1 3 ccc3\n",
      "　00000 　12345 ",
      "　　日本語　");

  private MainPanel() {
    super(new BorderLayout());
    JTextPane editor = new JTextPane();
    editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    editor.setEditorKit(new WhitespaceEditorKit());
    editor.setText(FULL_SPACE_TXT + TAB_TXT);
    add(new JScrollPane(editor));
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

class WhitespaceEditorKit extends StyledEditorKit {
  private final MutableAttributeSet tabAttrs = new SimpleAttributeSet();

  @Override public void install(JEditorPane c) {
    FontMetrics fm = c.getFontMetrics(c.getFont());
    int tabLength = fm.charWidth('m') * 4;
    // TabStop[] tabs = new TabStop[100];
    // for (int j = 0; j < tabs.length; j++) {
    //   tabs[j] = new TabStop((j + 1f) * tabLength);
    // }
    TabStop[] tabs = IntStream.range(0, 100)
        .mapToObj(i -> (i + 1f) * tabLength)
        .map(TabStop::new)
        .toArray(TabStop[]::new);
    StyleConstants.setTabSet(tabAttrs, new TabSet(tabs));
    super.install(c);
  }

  @Override public ViewFactory getViewFactory() {
    // Replace only the paragraph and content views and delegate the other elements
    // to the default StyledEditorKit factory
    ViewFactory defaultFactory = super.getViewFactory();
    return elem -> createView(elem, defaultFactory);
  }

  private static View createView(Element elem, ViewFactory defaultFactory) {
    String name = elem.getName();
    View view;
    if (AbstractDocument.ParagraphElementName.equals(name)) {
      view = new ParagraphMarkView(elem);
    } else if (AbstractDocument.ContentElementName.equals(name)) {
      view = new WhitespaceLabelView(elem);
    } else {
      view = defaultFactory.create(elem);
    }
    return view;
  }

  @Override public Document createDefaultDocument() {
    Document d = super.createDefaultDocument();
    if (d instanceof StyledDocument) {
      ((StyledDocument) d).setParagraphAttributes(0, d.getLength(), tabAttrs, false);
    }
    return d;
  }
}

class ParagraphMarkView extends ParagraphView {
  private static final Color MARK_COLOR = new Color(0x78_82_6E);

  protected ParagraphMarkView(Element elem) {
    super(elem);
  }

  @Override public void paint(Graphics g, Shape allocation) {
    super.paint(g, allocation);
    paintParagraphMark(g, allocation);
  }

  private void paintParagraphMark(Graphics g, Shape a) {
    try {
      // Backward bias keeps the mark on the last row of a wrapped paragraph
      Shape eop = modelToView(getEndOffset(), a, Position.Bias.Backward);
      Rectangle r = Objects.nonNull(eop) ? eop.getBounds() : a.getBounds();
      int x = r.x;
      int y = r.y;
      int h = r.height;
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(MARK_COLOR);
      g2.drawLine(x + 1, y + h / 2, x + 1, y + h - 4);
      g2.drawLine(x + 2, y + h / 2, x + 2, y + h - 5);
      g2.drawLine(x + 3, y + h - 6, x + 3, y + h - 6);
      g2.dispose();
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }
}

class WhitespaceLabelView extends LabelView {
  private static final char IDEOGRAPHIC_SPACE = '　'; // U+3000 IDEOGRAPHIC SPACE
  private static final char TAB = '\t';
  private static final Color MARK_COLOR = new Color(0x78_82_6E);
  private static final Stroke SOLID = new BasicStroke(1f);
  private static final Stroke DASHED = new BasicStroke(
      1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {1f}, 0f);

  protected WhitespaceLabelView(Element elem) {
    super(elem);
  }

  @Override public void paint(Graphics g, Shape a) {
    super.paint(g, a);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(MARK_COLOR);
    Rectangle alloc = a instanceof Rectangle ? (Rectangle) a : a.getBounds();
    int sy = alloc.y + alloc.height - g2.getFontMetrics(getFont()).getDescent();
    int p0 = getStartOffset();
    String text = getText(p0, getEndOffset()).toString();
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == IDEOGRAPHIC_SPACE) {
        Rectangle r = getCharBounds(p0 + i, a);
        g2.setStroke(DASHED);
        g2.drawLine(r.x + 1, sy - 1, r.x + r.width - 2, sy - 1);
        g2.drawLine(r.x + 2, sy, r.x + r.width - 2, sy);
      } else if (c == TAB) {
        Rectangle r = getCharBounds(p0 + i, a);
        int sx = r.x + 2;
        // the arrowhead must be solid even after a dashed full-width space mark
        g2.setStroke(SOLID);
        g2.drawLine(sx, sy, sx + 2, sy);
        g2.drawLine(sx, sy - 1, sx + 1, sy - 1);
        g2.drawLine(sx, sy - 2, sx, sy - 2);
        g2.setStroke(DASHED);
        g2.drawLine(sx, sy, r.x + r.width - 2, sy);
      }
    }
    g2.dispose();
  }

  // The bounds returned by modelToView already include the expanded tab width
  // and the actual glyph widths, so there is no need to accumulate them manually.
  private Rectangle getCharBounds(int pos, Shape a) {
    try {
      Rectangle r0 = modelToView(pos, a, Position.Bias.Forward).getBounds();
      Rectangle r1 = modelToView(pos + 1, a, Position.Bias.Forward).getBounds();
      return new Rectangle(r0.x, r0.y, r1.x - r0.x, r0.height);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }
}
