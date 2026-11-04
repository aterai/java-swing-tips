// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private static final String TEXT = "あいうえお かきくけこ さしすせそ たちつてと なにぬねの はひふへほ まみむめも";

  private MainPanel() {
    super(new GridLayout(4, 1, 0, 0));
    JLabel label1 = new JLabel(TEXT);
    label1.setBorder(createTitledColorBorder("JLabel", Color.YELLOW));

    JLabel label2 = new GlyphVectorWrapLabel(TEXT);
    label2.setBorder(createTitledColorBorder("GlyphVector", Color.GREEN));

    JLabel label3 = new LineBreakMeasurerWrapLabel(TEXT);
    label3.setBorder(createTitledColorBorder("LineBreakMeasurer", Color.CYAN));

    JTextArea textArea = new JTextArea(TEXT);
    textArea.setBorder(createTitledColorBorder("JTextArea", Color.ORANGE));
    textArea.setFont(label1.getFont());
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setBackground(label1.getBackground());

    add(label1);
    add(label2);
    add(label3);
    add(textArea);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Border createTitledColorBorder(String title, Color color) {
    return BorderFactory.createTitledBorder(BorderFactory.createLineBorder(color, 5), title);
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

class LineBreakMeasurerWrapLabel extends JLabel {
  protected LineBreakMeasurerWrapLabel(String text) {
    super(text);
  }

  @Override protected void paintComponent(Graphics g) {
    String text = getText();
    if (Objects.isNull(text) || text.isEmpty()) {
      // AttributedString#addAttribute(...) throws for 0-length text
      super.paintComponent(g);
      return;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(getForeground());
    AttributedString as = new AttributedString(text);
    as.addAttribute(TextAttribute.FONT, getFont());
    AttributedCharacterIterator aci = as.getIterator();
    FontRenderContext frc = g2.getFontRenderContext();
    LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
    Rectangle r = SwingUtilities.calculateInnerArea(this, null);
    float x = r.x;
    float y = r.y;
    float bottom = r.y + r.height;
    // Stop once the lines go below the inner area since they are invisible
    while (lbm.getPosition() < aci.getEndIndex() && y < bottom) {
      TextLayout tl = lbm.nextLayout(r.width);
      tl.draw(g2, x, y + tl.getAscent());
      y += tl.getAscent() + tl.getDescent() + tl.getLeading();
    }
    g2.dispose();
  }
}

class GlyphVectorWrapLabel extends JLabel {
  private transient GlyphVector wrappedGlyphs;
  private int cachedWidth = -1;
  private String cachedText;
  private Font cachedFont;

  protected GlyphVectorWrapLabel(String text) {
    super(text);
  }

  @Override protected void paintComponent(Graphics g) {
    String text = getText();
    if (Objects.isNull(text) || text.isEmpty()) {
      super.paintComponent(g);
      return;
    }
    Rectangle r = SwingUtilities.calculateInnerArea(this, null);
    Font font = getFont();
    // Rebuild the glyphs only if the width, text, or font has changed
    if (r.width != cachedWidth || !text.equals(cachedText) || !font.equals(cachedFont)) {
      FontRenderContext frc = getFontMetrics(font).getFontRenderContext();
      wrappedGlyphs = createWrappedGlyphVector(text, font, frc, r.width);
      cachedWidth = r.width;
      cachedText = text;
      cachedFont = font;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(Color.RED);
    g2.drawGlyphVector(wrappedGlyphs, r.x, r.y);
    g2.dispose();
  }

  private static GlyphVector createWrappedGlyphVector(
      String text, Font font, FontRenderContext frc, float width) {
    // Font#layoutGlyphVector(...) applies kerning and ligatures,
    // unlike Font#createGlyphVector(...) which maps chars one-to-one
    char[] chars = text.toCharArray();
    GlyphVector gv = font.layoutGlyphVector(
        frc, chars, 0, chars.length, Font.LAYOUT_LEFT_TO_RIGHT);
    LineMetrics lm = font.getLineMetrics(text, frc);
    float ascent = lm.getAscent();
    float lineHeight = lm.getHeight();
    Point2D glyphPos = new Point2D.Float();
    float x = 0f;
    int lineCount = 0;
    for (int i = 0; i < gv.getNumGlyphs(); i++) {
      float advance = gv.getGlyphMetrics(i).getAdvance();
      // Wrap only when the glyph overflows the width, and never before the
      // first glyph of a line to avoid a blank line for a too wide glyph
      if (x > 0f && x + advance > width) {
        lineCount++;
        x = 0f;
      }
      // Place each glyph on its baseline so that the vector is drawn from the top-left
      glyphPos.setLocation(x, ascent + lineHeight * lineCount);
      gv.setGlyphPosition(i, glyphPos);
      x += advance;
    }
    return gv;
  }
}
