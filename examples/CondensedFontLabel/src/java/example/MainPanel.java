// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private static final String TEXT = "The quick brown fox jumps over the lazy dog.";

  private MainPanel() {
    super(new GridLayout(0, 1));
    JTextArea textArea = new JTextArea(TEXT) {
      @Override public void updateUI() {
        super.updateUI();
        setEditable(false);
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(false);
        setBackground(new Color(0x0, true));
      }
    };
    JLabel lbl1 = new WrappedLabel(TEXT);
    JLabel lbl2 = new WrappingLabel(TEXT);

    Border b = BorderFactory.createLineBorder(Color.GRAY, 5);
    textArea.setBorder(BorderFactory.createTitledBorder(b, "JTextArea(condensed: 0.9)"));
    lbl1.setBorder(BorderFactory.createTitledBorder(b, "GlyphVector(condensed: 0.9)"));
    lbl2.setBorder(BorderFactory.createTitledBorder(b, "LineBreakMeasurer(condensed: 0.9)"));

    AffineTransform at = AffineTransform.getScaleInstance(.9, 1d);
    Font font = new Font(Font.MONOSPACED, Font.PLAIN, 18).deriveFont(at);
    // // TEST:
    // Font font = new Font(Font.MONOSPACED, Font.PLAIN, 18);
    // if (font.isTransformed()) {
    //   font = font.deriveFont(AffineTransform.getScaleInstance(.9, 1d));
    // }
    Stream.of(textArea, lbl1, lbl2).forEach(c -> {
      c.setFont(font);
      add(c);
    });
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

class WrappingLabel extends JLabel {
  private static final Rectangle RECT = new Rectangle();
  // TEST: private AffineTransform at = AffineTransform.getScaleInstance(.9, 1d);

  protected WrappingLabel(String text) {
    super(text);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(getForeground());
    g2.setFont(getFont());

    SwingUtilities.calculateInnerArea(this, RECT);
    float x = RECT.x;
    float y = RECT.y;
    int w = RECT.width;

    AttributedString as = new AttributedString(getText());
    as.addAttribute(TextAttribute.FONT, getFont()); // TEST: .deriveFont(at));
    // TEST: as.addAttribute(TextAttribute.TRANSFORM, at);
    AttributedCharacterIterator aci = as.getIterator();
    FontRenderContext frc = g2.getFontRenderContext();
    LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);

    while (lbm.getPosition() < aci.getEndIndex()) {
      TextLayout tl = lbm.nextLayout(w);
      tl.draw(g2, x, y + tl.getAscent());
      y += tl.getDescent() + tl.getLeading() + tl.getAscent();
    }
    g2.dispose();
  }
}

class WrappedLabel extends JLabel {
  private static final Rectangle RECT = new Rectangle();
  private transient GlyphVector gvText;
  private int prevWidth = -1;
  // TEST: private AffineTransform at = AffineTransform.getScaleInstance(.9, 1d);

  protected WrappedLabel(String str) {
    super(str);
  }

  @Override public void doLayout() {
    // Insets i = getInsets();
    // int w = getWidth() - i.left - i.right;
    int w = SwingUtilities.calculateInnerArea(this, RECT).width;
    if (w != prevWidth) {
      Font font = getFont();
      FontMetrics fm = getFontMetrics(font);
      FontRenderContext frc = fm.getFontRenderContext();
      GlyphVector gv = font.createGlyphVector(frc, getText());
      gvText = getWrappedGlyphVector(gv, w);
      prevWidth = w;
    }
    super.doLayout();
  }

  @Override protected void paintComponent(Graphics g) {
    if (Objects.nonNull(gvText)) {
      Insets i = getInsets();
      Graphics2D g2 = (Graphics2D) g.create();
      g2.drawGlyphVector(gvText, i.left, getFont().getSize2D() + i.top);
      g2.dispose();
    } else {
      super.paintComponent(g);
    }
  }

  private static GlyphVector getWrappedGlyphVector(GlyphVector gv, double width) {
    Point2D gmPos = new Point2D.Float();
    float lineHeight = (float) gv.getLogicalBounds().getHeight();
    float pos = 0f;
    int lineCount = 0;
    GlyphMetrics gm;

    for (int i = 0; i < gv.getNumGlyphs(); i++) {
      gm = gv.getGlyphMetrics(i);
      float advance = gm.getAdvance();
      if (pos < width && width <= pos + advance) {
        lineCount++;
        pos = 0f;
      }
      gmPos.setLocation(pos, lineHeight * lineCount);
      gv.setGlyphPosition(i, gmPos);
      pos += advance;
    }
    return gv;
  }
}
