// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    JLabel label0 = new JLabel("JLabel + MatteBorder(Gradient Icon)");
    Icon icon = new GradientIcon(label0.getPreferredSize());
    label0.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, icon));
    add(label0);

    JLabel label1 = new JLabel("JLabel + RoundGradientBorder");
    label1.setBorder(new RoundGradientBorder(5, 5, 5, 5));
    add(label1);

    JLabel label2 = new JLabel("JLabel(240x120) + RoundGradientBorder") {
      @Override public Dimension getPreferredSize() {
        return new Dimension(240, 120);
      }
    };
    label2.setBorder(new RoundGradientBorder(16, 16, 16, 16));
    add(label2);
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

class GradientIcon implements Icon {
  private static final float[] FRACTIONS = {0f, .25f, .5f, .75f, 1f};
  private static final Color[] COLORS = {
      new Color(0xD3_03_02),
      new Color(0xFF_51_56),
      new Color(0xFF_DB_4E),
      new Color(0x00_FE_9B),
      new Color(0x2D_D9_FE)
  };
  private final Dimension size;

  protected GradientIcon(Dimension size) {
    this.size = size;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    size.setSize(c.getSize(size));
    Graphics2D g2 = (Graphics2D) g.create();
    Point2D start = new Point2D.Float(0f, 0f);
    Point2D end = new Point2D.Float(c.getWidth(), 0f);
    g2.setPaint(new LinearGradientPaint(start, end, FRACTIONS, COLORS));
    g2.fillRect(x, y, c.getWidth(), c.getHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return size.width;
  }

  @Override public int getIconHeight() {
    return size.height;
  }
}

class RoundGradientBorder extends EmptyBorder {
  private static final float[] FRACTIONS = {0f, .25f, .5f, .75f, 1f};
  private static final Color[] COLORS = {
      new Color(0xD3_03_02),
      new Color(0xFF_51_56),
      new Color(0xFF_DB_4E),
      new Color(0x00_FE_9B),
      new Color(0x2D_D9_FE)
  };

  protected RoundGradientBorder(int top, int left, int bottom, int right) {
    super(top, left, bottom, right);
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Point2D start = new Point2D.Float(0f, 0f);
    Point2D end = new Point2D.Float(width, 0f);
    g2.setPaint(new LinearGradientPaint(start, end, FRACTIONS, COLORS));
    float stroke = 2f;
    float arc = 12f;
    Shape outer = new RoundRectangle2D.Float(x, y, width - 1f, height - 1f, arc, arc);
    Shape inner = new RoundRectangle2D.Float(
        x + stroke,
        y + stroke,
        width - stroke - stroke - 1f,
        height - stroke - stroke - 1f,
        arc - stroke - stroke,
        arc - stroke - stroke
    );
    Area rr = new Area(outer);
    rr.subtract(new Area(inner));
    g2.fill(rr);
    g2.dispose();
  }
}
