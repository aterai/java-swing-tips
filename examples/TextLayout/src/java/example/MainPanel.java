// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

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

class TextLayoutPanel extends JComponent {
  private static final String TEXT = "abcdefghijklmnopqrstuvwxyz";
  private static final Font FONT = new Font(Font.SERIF, Font.ITALIC, 64);
  private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
  private static final TextLayout TEXT_LAYOUT = new TextLayout(TEXT, FONT, FRC);

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    float w = getWidth();
    float baseline = getHeight() / 2f;

    g2.setPaint(Color.RED);
    g2.draw(new Line2D.Float(0f, baseline, w, baseline));

    g2.setPaint(Color.GREEN);
    float ascent = baseline - TEXT_LAYOUT.getAscent();
    g2.draw(new Line2D.Float(0f, ascent, w, ascent));

    g2.setPaint(Color.BLUE);
    float descent = baseline + TEXT_LAYOUT.getDescent();
    g2.draw(new Line2D.Float(0f, descent, w, descent));

    g2.setPaint(Color.ORANGE);
    float leading = baseline + TEXT_LAYOUT.getDescent() + TEXT_LAYOUT.getLeading();
    g2.draw(new Line2D.Float(0f, leading, w, leading));

    g2.setPaint(Color.CYAN);
    float xh = baseline - (float) TEXT_LAYOUT.getBlackBoxBounds(23, 24).getBounds().getHeight();
    g2.draw(new Line2D.Float(0f, xh, w, xh));

    g2.setPaint(Color.BLACK);
    TEXT_LAYOUT.draw(g2, 0f, baseline);
    g2.dispose();
  }
}

class GlyphVectorPanel extends JComponent {
  private static final String TEXT = "abcdefghijklmnopqrstuvwxyz";
  private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
  private static final Font FONT = new Font(Font.SERIF, Font.ITALIC, 64);
  private final transient GlyphVector gv = FONT.createGlyphVector(FRC, TEXT);
  private final transient LineMetrics lm = FONT.getLineMetrics(TEXT, FRC);

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    float w = getWidth();
    float baseline = getHeight() / 2f;

    g2.setPaint(Color.RED);
    g2.draw(new Line2D.Float(0f, baseline, w, baseline));

    g2.setPaint(Color.GREEN);
    float ascent = baseline - lm.getAscent();
    g2.draw(new Line2D.Float(0f, ascent, w, ascent));

    g2.setPaint(Color.BLUE);
    float descent = baseline + lm.getDescent();
    g2.draw(new Line2D.Float(0f, descent, w, descent));

    g2.setPaint(Color.ORANGE);
    float leading = baseline + lm.getDescent() + lm.getLeading();
    g2.draw(new Line2D.Float(0f, leading, w, leading));

    g2.setPaint(Color.CYAN);
    float xh = baseline - (float) gv.getGlyphMetrics(23).getBounds2D().getHeight();
    g2.draw(new Line2D.Float(0f, xh, w, xh));

    g2.setPaint(Color.BLACK);
    g2.drawGlyphVector(gv, 0f, baseline);
    g2.dispose();
  }
}
