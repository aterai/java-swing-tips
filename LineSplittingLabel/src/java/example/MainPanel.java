// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    add(new LineSplittingLabel("ABC"));
    add(new TricoloreLabel("DEF"));
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class TricoloreLabel extends JComponent {
  private final Font font = new Font(Font.SERIF, Font.PLAIN, 64);
  private final String text;

  protected TricoloreLabel(String str) {
    super();
    this.text = str;
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
    GlyphVector gv = font.createGlyphVector(frc, text);
    Rectangle2D b = gv.getVisualBounds();
    double cx = w / 2d - b.getCenterX();
    double cy = h / 2d - b.getCenterY();
    AffineTransform toCenterAtf = AffineTransform.getTranslateInstance(cx, cy);

    double d = b.getHeight() / 3d;
    Rectangle2D clip = new Rectangle2D.Double(b.getX(), b.getY(), b.getWidth(), b.getHeight());
    Rectangle2D clip1 = new Rectangle2D.Double(b.getX(), b.getY(), b.getWidth(), d);
    Rectangle2D clip2 = new Rectangle2D.Double(b.getX(), b.getY() + 2 * d, b.getWidth(), d);

    Shape s = toCenterAtf.createTransformedShape(gv.getOutline());

    g2.setClip(toCenterAtf.createTransformedShape(clip1));
    g2.setPaint(Color.BLUE);
    g2.fill(s);

    g2.setClip(toCenterAtf.createTransformedShape(clip2));
    g2.setPaint(Color.RED);
    g2.fill(s);

    g2.setClip(toCenterAtf.createTransformedShape(clip));
    g2.setPaint(Color.BLACK);
    g2.draw(s);
    g2.dispose();
  }
}

class LineSplittingLabel extends JComponent {
  private final Font font = new Font(Font.SERIF, Font.PLAIN, 64);
  private final String text;

  protected LineSplittingLabel(String str) {
    super();
    this.text = str;
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
    Shape shape = new TextLayout(text, font, frc).getOutline(null);
    Rectangle2D b = shape.getBounds2D();
    double cx = w / 2d - b.getCenterX();
    double cy = h / 2d - b.getCenterY();
    AffineTransform toCenterAtf = AffineTransform.getTranslateInstance(cx, cy);

    Shape s = toCenterAtf.createTransformedShape(shape);
    g2.setPaint(Color.BLACK);
    g2.fill(s);
    Rectangle2D clip = new Rectangle2D.Double(b.getX(), b.getY(), b.getWidth(), b.getHeight() / 2d);
    g2.setClip(toCenterAtf.createTransformedShape(clip));
    g2.setPaint(Color.RED);
    g2.fill(s);
    g2.dispose();
  }
}
