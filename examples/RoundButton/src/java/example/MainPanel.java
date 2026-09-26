// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    add(new JButton("Default JButton"));
    JButton button = new JButton("RoundedCornerButtonUI") {
      @Override public void updateUI() {
        // IGNORE LnF change: super.updateUI();
        setUI(new RoundedCornerButtonUI());
      }
    };
    add(button);
    add(new RoundedCornerButton("Rounded Corner Button"));

    String path = "example/16x16.png";
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    Icon icon = url == null
        ? UIManager.getIcon("html.missingImage")
        : new ImageIcon(url);
    add(new RoundButton(icon) {
      @Override public Dimension getPreferredSize() {
        int margin = 4;
        int s = Math.max(icon.getIconWidth(), icon.getIconHeight());
        int size = s + (FOCUS_STROKE + margin) * 2;
        return new Dimension(size, size);
      }
    });
    add(new ShapeButton(createStar(30d, 25d, 20)));
    add(new RoundButton("Round Button"));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Shape createStar(double outerRadius, double innerRadius, int vertexCount) {
    double step = Math.PI / vertexCount;
    double angle = -Math.PI / 2d; // start from the top vertex
    Path2D p = new Path2D.Double();
    p.moveTo(outerRadius * Math.cos(angle), outerRadius * Math.sin(angle));
    for (int i = 1; i < vertexCount * 2; i++) {
      angle += step;
      double r = i % 2 == 0 ? outerRadius : innerRadius;
      p.lineTo(r * Math.cos(angle), r * Math.sin(angle));
    }
    p.closePath();
    Rectangle2D b = p.getBounds2D();
    AffineTransform at = AffineTransform.getTranslateInstance(-b.getX(), -b.getY());
    return at.createTransformedShape(p);
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

class RoundedCornerButton extends JButton {
  protected static final int FOCUS_STROKE = 2;
  protected static final Color FOCUS_COLOR = new Color(0xC8_64_96_FF, true);
  protected static final Color PRESSED_COLOR = new Color(0xE6_E6_E6);
  protected static final Color ROLLOVER_COLOR = Color.ORANGE;
  private static final double ARC = 16d;
  private final Dimension cachedSize = new Dimension();
  private transient Shape shape;
  private transient Shape innerShape;

  protected RoundedCornerButton(Icon icon) {
    super(icon);
  }

  protected RoundedCornerButton(String text) {
    super(text);
  }

  @Override public void updateUI() {
    super.updateUI();
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBackground(new Color(0xFA_FA_FA));
  }

  protected Shape createShape(double x, double y, double w, double h) {
    return new RoundRectangle2D.Double(x, y, w, h, ARC, ARC);
  }

  private void updateShapeIfResized() {
    if (shape == null || !cachedSize.equals(getSize())) {
      getSize(cachedSize);
      double w = getWidth() - 1d;
      double h = getHeight() - 1d;
      double s = FOCUS_STROKE;
      shape = createShape(0d, 0d, w, h);
      innerShape = createShape(s, s, w - s * 2d, h - s * 2d);
    }
  }

  private void paintFocusAndRollover(Graphics2D g2, Color color) {
    float x2 = getWidth() - 1f;
    float y2 = getHeight() - 1f;
    g2.setPaint(new GradientPaint(0f, 0f, color, x2, y2, color.brighter(), true));
    g2.fill(shape);
    g2.setPaint(getBackground());
    g2.fill(innerShape);
  }

  @Override protected void paintComponent(Graphics g) {
    updateShapeIfResized();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    ButtonModel m = getModel();
    if (m.isArmed()) {
      g2.setPaint(PRESSED_COLOR);
      g2.fill(shape);
    } else if (isRolloverEnabled() && m.isRollover()) {
      paintFocusAndRollover(g2, ROLLOVER_COLOR);
    } else if (hasFocus()) {
      paintFocusAndRollover(g2, FOCUS_COLOR);
    } else {
      g2.setPaint(getBackground());
      g2.fill(shape);
    }
    g2.dispose();
    super.paintComponent(g);
  }

  @Override protected void paintBorder(Graphics g) {
    updateShapeIfResized();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(getForeground());
    g2.draw(shape);
    g2.dispose();
  }

  @Override public boolean contains(int x, int y) {
    updateShapeIfResized();
    return shape.contains(x, y);
  }
}

class RoundButton extends RoundedCornerButton {
  protected RoundButton(Icon icon) {
    super(icon);
  }

  protected RoundButton(String text) {
    super(text);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    int s = Math.max(d.width, d.height);
    d.setSize(s, s);
    return d;
  }

  @Override protected Shape createShape(double x, double y, double w, double h) {
    return new Ellipse2D.Double(x, y, w, h);
  }
}

class ShapeButton extends JButton {
  protected static final Color FOCUS_COLOR = new Color(0xC8_64_96_FF, true);
  protected static final Color PRESSED_COLOR = new Color(0xE6_E6_E6);
  protected static final Color ROLLOVER_COLOR = Color.ORANGE;
  private final transient Shape shape;

  protected ShapeButton(Shape s) {
    super("Shape", new ShapeSizeIcon(s));
    shape = s;
  }

  @Override public void updateUI() {
    super.updateUI();
    setVerticalAlignment(CENTER);
    setVerticalTextPosition(CENTER);
    setHorizontalAlignment(CENTER);
    setHorizontalTextPosition(CENTER);
    setBorder(BorderFactory.createEmptyBorder());
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBackground(new Color(0xFA_FA_FA));
  }

  private void paintFocusAndRollover(Graphics2D g2, Color color) {
    float x2 = getWidth() - 1f;
    float y2 = getHeight() - 1f;
    g2.setPaint(new GradientPaint(0f, 0f, color, x2, y2, color.brighter(), true));
    g2.fill(shape);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    ButtonModel m = getModel();
    if (m.isArmed()) {
      g2.setPaint(PRESSED_COLOR);
      g2.fill(shape);
    } else if (isRolloverEnabled() && m.isRollover()) {
      paintFocusAndRollover(g2, ROLLOVER_COLOR);
    } else if (hasFocus()) {
      paintFocusAndRollover(g2, FOCUS_COLOR);
    } else {
      g2.setPaint(getBackground());
      g2.fill(shape);
    }
    g2.dispose();
    super.paintComponent(g);
  }

  @Override protected void paintBorder(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(getForeground());
    g2.draw(shape);
    g2.dispose();
  }

  @Override public boolean contains(int x, int y) {
    return Optional.ofNullable(shape)
        .map(s -> s.contains(x, y))
        .orElseGet(() -> super.contains(x, y));
  }
}

class ShapeSizeIcon implements Icon {
  private final Rectangle bounds;

  protected ShapeSizeIcon(Shape s) {
    bounds = s.getBounds();
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    /* Empty icon */
  }

  @Override public int getIconWidth() {
    return bounds.x + bounds.width + 1;
  }

  @Override public int getIconHeight() {
    return bounds.y + bounds.height + 1;
  }
}
