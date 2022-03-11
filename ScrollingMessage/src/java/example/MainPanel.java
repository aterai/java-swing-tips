// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Line2D;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new MarqueePanel());
    setPreferredSize(new Dimension(320, 240));
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

class MarqueePanel extends JComponent implements ActionListener {
  private final Timer animator = new Timer(10, this);
  private final GlyphVector gv;
  private final LineMetrics lm;
  private final float corpusSize; // the x-height
  private float xx;
  private float baseline;

  protected MarqueePanel() {
    super();
    addHierarchyListener(e -> {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable()) {
        animator.stop();
      }
    });

    String text = "abcdefghijklmnopqrstuvwxyz";
    Font font = new Font(Font.SERIF, Font.PLAIN, 100);
    FontRenderContext frc = new FontRenderContext(null, true, true);

    gv = font.createGlyphVector(frc, text);
    lm = font.getLineMetrics(text, frc);

    GlyphMetrics xgm = gv.getGlyphMetrics(23);
    corpusSize = (float) xgm.getBounds2D().getHeight();
    animator.start();
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
    // g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
    float w = getWidth();

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
    float xh = baseline - corpusSize;
    g2.draw(new Line2D.Float(0f, xh, w, xh));

    g2.setPaint(Color.BLACK);
    g2.drawGlyphVector(gv, w - xx, baseline);
    g2.dispose();
  }

  @Override public void actionPerformed(ActionEvent e) {
    xx = getWidth() + gv.getVisualBounds().getWidth() - xx > 0 ? xx + 2f : 0f;
    baseline = getHeight() / 2f;
    repaint();
  }
}
