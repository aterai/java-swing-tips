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
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private static final String TEXT = "あいうえお かきくけこ さしすせそ たちつてと なにぬねの はひふへほ まみむめも";

  private MainPanel() {
    super(new GridLayout(4, 1, 0, 0));
    JLabel lbl1 = new JLabel(TEXT);
    lbl1.setBorder(makeTitledColorBorder("JLabel", Color.YELLOW));

    JLabel lbl2 = new WrappedLabel(TEXT);
    lbl2.setBorder(makeTitledColorBorder("GlyphVector", Color.GREEN));

    JLabel lbl3 = new WrappingLabel(TEXT);
    lbl3.setBorder(makeTitledColorBorder("LineBreakMeasurer", Color.CYAN));

    JTextArea lbl4 = new JTextArea(TEXT);
    lbl4.setBorder(makeTitledColorBorder("JTextArea", Color.ORANGE));

    // lbl2.setFont(new Font(Font.SERIF, Font.TRUETYPE_FONT, 20));
    lbl4.setFont(lbl1.getFont());
    lbl4.setEditable(false);
    lbl4.setLineWrap(true);
    lbl4.setBackground(lbl1.getBackground());

    add(lbl1);
    add(lbl2);
    add(lbl3);
    add(lbl4);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Border makeTitledColorBorder(String title, Color color) {
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
  protected WrappingLabel(String text) {
    super(text);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(getForeground());
    Rectangle r = SwingUtilities.calculateInnerArea(this, null);
    float x = r.x;
    float y = r.y;
    int w = r.width;
    AttributedString as = new AttributedString(getText());
    as.addAttribute(TextAttribute.FONT, getFont());
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
  private transient GlyphVector gvText;
  private int prevWidth = -1;

  // protected WrappedLabel() {
  //   super();
  // }

  protected WrappedLabel(String str) {
    super(str);
  }

  @Override public void doLayout() {
    Rectangle r = SwingUtilities.calculateInnerArea(this, null);
    if (r.width != prevWidth) {
      Font font = getFont();
      FontMetrics fm = getFontMetrics(font);
      FontRenderContext frc = fm.getFontRenderContext();
      GlyphVector gv = font.createGlyphVector(frc, getText());
      gvText = getWrappedGlyphVector(gv, r.getWidth());
      prevWidth = r.width;
    }
    super.doLayout();
  }

  @Override protected void paintComponent(Graphics g) {
    if (Objects.nonNull(gvText)) {
      Insets i = getInsets();
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(Color.RED);
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
      // TEST: gv.setGlyphTransform(i, at);
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
