package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    add(new TextLayoutPanel());
    add(new GlyphVectorPanel());
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TextLayoutPanel extends JComponent {
  private static final String TEXT = "abcdefthijklmnopqrstuvwxyz";
  private static final Font FONT = new Font(Font.SERIF, Font.ITALIC, 64);
  private static final TextLayout TEXT_LAYOUT = new TextLayout(TEXT, FONT, new FontRenderContext(null, true, true));

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getWidth();
    float baseline = getHeight() / 2f;

    g2.setPaint(Color.RED);
    g2.draw(new Line2D.Double(0, baseline, w, baseline));

    g2.setPaint(Color.GREEN);
    float ascent = baseline - TEXT_LAYOUT.getAscent();
    g2.draw(new Line2D.Double(0, ascent, w, ascent));

    g2.setPaint(Color.BLUE);
    float descent = baseline + TEXT_LAYOUT.getDescent();
    g2.draw(new Line2D.Double(0, descent, w, descent));

    g2.setPaint(Color.ORANGE);
    float leading = baseline + TEXT_LAYOUT.getDescent() + TEXT_LAYOUT.getLeading();
    g2.draw(new Line2D.Double(0, leading, w, leading));

    g2.setPaint(Color.CYAN);
    float xheight = baseline - (float) TEXT_LAYOUT.getBlackBoxBounds(23, 24).getBounds().getHeight();
    g2.draw(new Line2D.Double(0, xheight, w, xheight));

    g2.setPaint(Color.BLACK);
    TEXT_LAYOUT.draw(g2, 0f, baseline);
    g2.dispose();
  }
}

class GlyphVectorPanel extends JComponent {
  private static final String TEXT = "abcdefthijklmnopqrstuvwxyz";
  private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
  private final Font font = new Font(Font.SERIF, Font.ITALIC, 64);
  private final GlyphVector gv = font.createGlyphVector(FRC, TEXT);
  private final LineMetrics lm = font.getLineMetrics(TEXT, FRC);

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getWidth();
    float baseline = getHeight() / 2f;

    g2.setPaint(Color.RED);
    g2.draw(new Line2D.Double(0, baseline, w, baseline));

    g2.setPaint(Color.GREEN);
    float ascent = baseline - lm.getAscent();
    g2.draw(new Line2D.Double(0, ascent, w, ascent));

    g2.setPaint(Color.BLUE);
    float descent = baseline + lm.getDescent();
    g2.draw(new Line2D.Double(0, descent, w, descent));

    g2.setPaint(Color.ORANGE);
    float leading = baseline + lm.getDescent() + lm.getLeading();
    g2.draw(new Line2D.Double(0, leading, w, leading));

    g2.setPaint(Color.CYAN);
    float xheight = baseline - (float) gv.getGlyphMetrics(23).getBounds2D().getHeight();
    g2.draw(new Line2D.Double(0, xheight, w, xheight));

    g2.setPaint(Color.BLACK);
    g2.drawGlyphVector(gv, 0f, baseline);
    g2.dispose();
  }
}
