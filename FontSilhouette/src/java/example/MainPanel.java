// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.Serializable;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int SIZE = 50;
  private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, SIZE);

  private MainPanel() {
    super(new GridLayout(4, 6, 0, 0));
    String[] pieces = {
      "♔", "♕", "♖", "♗", "♘", "♙", // "\u2654", "\u2655", "\u2656", "\u2657", "\u2658", "\u2659",
      "♚", "♛", "♜", "♝", "♞", "♟", // "\u265A", "\u265B", "\u265C", "\u265D", "\u265E", "\u265F",
    };
    for (int i = 0; i < pieces.length; i++) {
      add(initLabel(new JLabel(pieces[i], SwingConstants.CENTER), i));
    }
    for (int i = 0; i < pieces.length; i++) {
      add(initLabel(new JLabel(new SilhouetteIcon(FONT, pieces[i], SIZE)), i));
    }
    setPreferredSize(new Dimension(320, 240));
  }

  private static JLabel initLabel(JLabel l, int i) {
    l.setFont(FONT);
    l.setOpaque(true);
    boolean isFirstHalf = i < 6;
    boolean isEven = i % 2 == 0;
    if (isFirstHalf ? isEven : !isEven) {
      l.setForeground(Color.BLACK);
      l.setBackground(Color.WHITE);
    } else {
      l.setForeground(Color.WHITE);
      l.setBackground(Color.BLACK);
    }
    return l;
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

class SilhouetteIcon implements Icon, Serializable {
  private static final long serialVersionUID = 1L;
  private static final Color PIECE_PAINT = new Color(0x96_64_14);
  private final Font font;
  private final String str;
  private final int size;

  protected SilhouetteIcon(Font font, String str, int size) {
    this.font = font;
    this.str = str;
    this.size = size;
  }

  // Inspired from java - 'Fill' Unicode characters in labels - Stack Overflow
  // https://stackoverflow.com/questions/18686199/fill-unicode-characters-in-labels
  private static Area getOuterShape(Shape shape) {
    Area area = new Area();
    Path2D path = new Path2D.Double();
    PathIterator pi = shape.getPathIterator(null);
    double[] coords = new double[6];
    while (!pi.isDone()) {
      int pathSegmentType = pi.currentSegment(coords);
      switch (pathSegmentType) {
        case PathIterator.SEG_MOVETO:
          path.moveTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_LINETO:
          path.lineTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_QUADTO:
          path.quadTo(coords[0], coords[1], coords[2], coords[3]);
          break;
        case PathIterator.SEG_CUBICTO:
          path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
          break;
        case PathIterator.SEG_CLOSE:
          path.closePath();
          area.add(new Area(path));
          path.reset();
          break;
        default:
          System.err.println("Unexpected value! " + pathSegmentType);
          break;
      }
      pi.next();
    }
    return area;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    FontRenderContext frc = g2.getFontRenderContext();
    Shape shape = font.createGlyphVector(frc, str).getOutline();
    Rectangle r = shape.getBounds();
    int sx = getIconWidth() - r.width;
    int sy = getIconHeight() - r.height;
    AffineTransform at = AffineTransform.getTranslateInstance(-r.x + sx / 2, -r.y + sy / 2);
    Shape shapeCentered = at.createTransformedShape(shape);

    Shape silhouette = getOuterShape(shapeCentered);
    g2.setStroke(new BasicStroke(3));
    g2.setPaint(c.getForeground());
    g2.draw(silhouette);
    g2.setPaint(PIECE_PAINT);
    g2.fill(silhouette);

    g2.setStroke(new BasicStroke(1));
    g2.setPaint(c.getBackground());
    g2.fill(shapeCentered);
    // g2.setPaint(PIECE_PAINT.brighter());
    // g2.draw(shapeCentered);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return size;
  }

  @Override public int getIconHeight() {
    return size;
  }
}
