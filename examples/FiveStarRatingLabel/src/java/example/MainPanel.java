// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1));
    Font font = new Font(Font.SERIF, Font.PLAIN, 24);
    JComponent label1 = new FiveStarRatingLabel("3.5");
    label1.setBorder(BorderFactory.createTitledBorder("3.5"));
    label1.setFont(font);

    JComponent label2 = new FiveStarRatingLabel("4.3");
    label2.setBorder(BorderFactory.createTitledBorder("4.3"));
    label2.setFont(font);

    JComponent label3 = new FiveStarRatingLabel("5");
    label3.setBorder(BorderFactory.createTitledBorder("5"));
    label3.setFont(font);

    add(label1);
    add(label2);
    add(label3);
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

class FiveStarRatingLabel extends JComponent {
  private static final String STAR = "★★★★★";
  private final int ip;
  private final int fp;

  protected FiveStarRatingLabel(String rating) {
    super();
    BigDecimal bd = new BigDecimal(rating);
    ip = bd.intValue();
    fp = bd.subtract(new BigDecimal(ip)).multiply(BigDecimal.TEN).intValue();
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    int w = getWidth();
    int h = getHeight();
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, w, h);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    FontRenderContext frc = g2.getFontRenderContext();
    GlyphVector gv = getFont().createGlyphVector(frc, STAR);
    Rectangle2D r = gv.getVisualBounds();

    double cx = w / 2d - r.getCenterX();
    double cy = h / 2d - r.getCenterY();
    AffineTransform toCenterAt = AffineTransform.getTranslateInstance(cx, cy);

    double point = 0d;
    for (int i = 0; i < gv.getNumGlyphs(); i++) {
      GlyphMetrics gm = gv.getGlyphMetrics(i);
      if (i <= ip - 1) {
        point += gm.getAdvance();
      } else if (i <= ip) {
        point += gm.getBounds2D().getWidth() * fp / 10d;
      }
    }
    g2.setPaint(Color.GREEN);
    Shape s = toCenterAt.createTransformedShape(gv.getOutline());
    g2.draw(s);
    Rectangle2D clip = new Rectangle2D.Double(r.getX(), r.getY(), point, r.getHeight());
    g2.setClip(toCenterAt.createTransformedShape(clip));
    g2.fill(s);
    g2.dispose();
  }
}
