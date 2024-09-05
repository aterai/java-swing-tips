// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
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
  private static final String IDEOGRAPHIC_SPACE = String.join("\n",
      "123456789012",
      "bbb2\u3000\u3000\u30001 3 ccc3\n",
      "\u300000000 \u300012345 ",
      "\u3000\u3000日本語\u3000");

  private MainPanel() {
    super(new BorderLayout());
    JTextPane editor = new JTextPane();
    editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    editor.setEditorKit(new CustomEditorKit());
    editor.setText(IDEOGRAPHIC_SPACE + TAB_TXT);
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

class CustomEditorKit extends StyledEditorKit {
  private static final MutableAttributeSet ATTRS = new SimpleAttributeSet();

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
    TabSet tabSet = new TabSet(tabs);
    StyleConstants.setTabSet(ATTRS, tabSet);
    super.install(c);
  }

  @Override public ViewFactory getViewFactory() {
    return new CustomViewFactory();
  }

  @Override public Document createDefaultDocument() {
    Document d = super.createDefaultDocument();
    if (d instanceof StyledDocument) {
      ((StyledDocument) d).setParagraphAttributes(0, d.getLength(), ATTRS, false);
    }
    return d;
  }
}

class CustomViewFactory implements ViewFactory {
  @SuppressWarnings("PMD.OnlyOneReturn")
  @Override public View create(Element elem) {
    switch (elem.getName()) {
      // case AbstractDocument.ContentElementName:
      //   return new WhitespaceLabelView(elem);
      case AbstractDocument.ParagraphElementName:
        return new ParagraphWithEopmView(elem);
      case AbstractDocument.SectionElementName:
        return new BoxView(elem, View.Y_AXIS);
      case StyleConstants.ComponentElementName:
        return new ComponentView(elem);
      case StyleConstants.IconElementName:
        return new IconView(elem);
      default:
        return new WhitespaceLabelView(elem);
    }
  }
}

class ParagraphWithEopmView extends ParagraphView {
  private static final Color MARK_COLOR = new Color(0x78_82_6E);

  protected ParagraphWithEopmView(Element elem) {
    super(elem);
  }

  @Override public void paint(Graphics g, Shape allocation) {
    super.paint(g, allocation);
    paintCustomParagraph(g, allocation);
  }

  private void paintCustomParagraph(Graphics g, Shape a) {
    try {
      Shape paragraph = modelToView(getEndOffset(), a, Position.Bias.Backward);
      Rectangle r = Objects.nonNull(paragraph) ? paragraph.getBounds() : a.getBounds();
      int x = r.x;
      int y = r.y;
      int h = r.height;
      Color old = g.getColor();
      g.setColor(MARK_COLOR);
      g.drawLine(x + 1, y + h / 2, x + 1, y + h - 4);
      g.drawLine(x + 2, y + h / 2, x + 2, y + h - 5);
      g.drawLine(x + 3, y + h - 6, x + 3, y + h - 6);
      g.setColor(old);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }
}

class WhitespaceLabelView extends LabelView {
  private static final String IDEOGRAPHIC_SPACE = "\u3000";
  private static final String TABULATION = "\t";
  private static final Color MARK_COLOR = new Color(0x78_82_6E);
  private static final BasicStroke DASHED = new BasicStroke(
      1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {1f}, 0f);

  protected WhitespaceLabelView(Element elem) {
    super(elem);
  }

  @Override public void paint(Graphics g, Shape a) {
    super.paint(g, a);
    Graphics2D g2 = (Graphics2D) g.create();
    Rectangle alloc = a instanceof Rectangle ? (Rectangle) a : a.getBounds();
    FontMetrics fontMetrics = g.getFontMetrics();
    int spaceWidth = fontMetrics.stringWidth(IDEOGRAPHIC_SPACE);
    int sumOfTabs = 0;
    String text = getText(getStartOffset(), getEndOffset()).toString();
    for (int i = 0; i < text.length(); i++) {
      String s = text.substring(i, i + 1);
      int prevStrWidth = fontMetrics.stringWidth(text.substring(0, i)) + sumOfTabs;
      int sx = alloc.x + prevStrWidth;
      int sy = alloc.y + alloc.height - fontMetrics.getDescent();
      if (IDEOGRAPHIC_SPACE.equals(s)) {
        g2.setStroke(DASHED);
        g2.setPaint(MARK_COLOR);
        g2.drawLine(sx + 1, sy - 1, sx + spaceWidth - 2, sy - 1);
        g2.drawLine(sx + 2, sy, sx + spaceWidth - 2, sy);
      } else if (TABULATION.equals(s)) {
        int tabWidth = (int) getTabExpander().nextTabStop(sx, i) - sx;
        g2.setPaint(MARK_COLOR);
        g2.drawLine(sx + 2, sy, sx + 2 + 2, sy);
        g2.drawLine(sx + 2, sy - 1, sx + 2 + 1, sy - 1);
        g2.drawLine(sx + 2, sy - 2, sx + 2, sy - 2);
        g2.setStroke(DASHED);
        g2.drawLine(sx + 2, sy, sx + tabWidth - 2, sy);
        sumOfTabs += tabWidth;
      }
    }
    g2.dispose();
  }
}
