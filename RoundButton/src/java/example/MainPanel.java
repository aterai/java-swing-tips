// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    add(new JButton("Default JButton"));
    // button.setUI(new RoundedCornerButtonUI());
    // IGNORE LnF change: super.updateUI();
    JButton button = new JButton("RoundedCornerButtonUI") {
      @Override public void updateUI() {
        // IGNORE LnF change: super.updateUI();
        setUI(new RoundedCornerButtonUI());
      }
    };
    add(button);
    add(new RoundedCornerButton("Rounded Corner Button"));

    URL url = Thread.currentThread().getContextClassLoader().getResource("example/16x16.png");
    Icon icon = url == null ? UIManager.getIcon("html.missingImage") : new ImageIcon(url);
    add(new RoundButton(icon) {
      @Override public Dimension getPreferredSize() {
        int r = 16 + (FOCUS_STROKE + 4) * 2; // test margin = 4
        return new Dimension(r, r);
      }
    });
    add(new ShapeButton(makeStar(25, 30, 20)));
    add(new RoundButton("Round Button"));
    setPreferredSize(new Dimension(320, 240));
  }

  public Path2D makeStar(int r1, int r2, int vc) {
    double or = Math.max(r1, r2);
    double ir = Math.min(r1, r2);
    double agl = 0d;
    double add = Math.PI / vc;
    Path2D p = new Path2D.Double();
    p.moveTo(or, 0d);
    for (int i = 0; i < vc * 2 - 1; i++) {
      agl += add;
      double r = i % 2 == 0 ? ir : or;
      p.lineTo(r * Math.cos(agl), r * Math.sin(agl));
    }
    p.closePath();
    AffineTransform at = AffineTransform.getRotateInstance(-Math.PI / 2d, or, 0d);
    return new Path2D.Double(p, at);
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

class RoundedCornerButton extends JButton {
  protected static final int FOCUS_STROKE = 2;
  protected static final Color FC = new Color(100, 150, 255, 200);
  protected static final Color AC = new Color(230, 230, 230);
  protected static final Color RC = Color.ORANGE;
  private static final double ARC = 16d;
  protected transient Shape shape;
  protected transient Shape border;
  protected transient Shape base;

  protected RoundedCornerButton() {
    super();
  }

  protected RoundedCornerButton(Icon icon) {
    super(icon);
  }

  protected RoundedCornerButton(String text) {
    super(text);
  }

  protected RoundedCornerButton(Action a) {
    super(a);
    // setAction(a);
  }

  protected RoundedCornerButton(String text, Icon icon) {
    super(text, icon);
    // setModel(new DefaultButtonModel());
    // init(text, icon);
    // setContentAreaFilled(false);
    // setBackground(new Color(0xFA_FA_FA));
    // initShape();
  }

  @Override public void updateUI() {
    super.updateUI();
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBackground(new Color(0xFA_FA_FA));
    initShape();
  }

  protected void initShape() {
    if (!getBounds().equals(base)) {
      base = getBounds();
      shape = new RoundRectangle2D.Double(0d, 0d, getWidth() - 1d, getHeight() - 1d, ARC, ARC);
      border = new RoundRectangle2D.Double(
          FOCUS_STROKE, FOCUS_STROKE,
          getWidth() - 1d - FOCUS_STROKE * 2d,
          getHeight() - 1d - FOCUS_STROKE * 2d, ARC, ARC);
    }
  }

  private void paintFocusAndRollover(Graphics2D g2, Color color) {
    float x2 = getWidth() - 1f;
    float y2 = getHeight() - 1f;
    g2.setPaint(new GradientPaint(0f, 0f, color, x2, y2, color.brighter(), true));
    g2.fill(shape);
    g2.setPaint(getBackground());
    g2.fill(border);
  }

  @Override protected void paintComponent(Graphics g) {
    initShape();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (getModel().isArmed()) {
      g2.setPaint(AC);
      g2.fill(shape);
    } else if (isRolloverEnabled() && getModel().isRollover()) {
      paintFocusAndRollover(g2, RC);
    } else if (hasFocus()) {
      paintFocusAndRollover(g2, FC);
    } else {
      g2.setPaint(getBackground());
      g2.fill(shape);
    }
    g2.dispose();
    super.paintComponent(g);
  }

  @Override protected void paintBorder(Graphics g) {
    initShape();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(getForeground());
    g2.draw(shape);
    g2.dispose();
  }

  @Override public boolean contains(int x, int y) {
    initShape();
    // return shape != null && shape.contains(x, y);
    return Optional.ofNullable(shape)
        .map(s -> s.contains(x, y))
        .orElseGet(() -> super.contains(x, y));
  }
}

class RoundButton extends RoundedCornerButton {
  protected RoundButton() {
    super();
  }

  protected RoundButton(Icon icon) {
    super(icon);
  }

  protected RoundButton(String text) {
    super(text);
  }

  protected RoundButton(Action a) {
    super(a);
    // setAction(a);
  }

  protected RoundButton(String text, Icon icon) {
    super(text, icon);
    // setModel(new DefaultButtonModel());
    // init(text, icon);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    int s = Math.max(d.width, d.height);
    d.setSize(s, s);
    return d;
  }

  @Override protected void initShape() {
    if (!getBounds().equals(base)) {
      base = getBounds();
      shape = new Ellipse2D.Double(0d, 0d, getWidth() - 1d, getHeight() - 1d);
      border = new Ellipse2D.Double(
          FOCUS_STROKE, FOCUS_STROKE,
          getWidth() - 1d - FOCUS_STROKE * 2d,
          getHeight() - 1d - FOCUS_STROKE * 2d);
    }
  }
}

class ShapeButton extends JButton {
  protected static final Color FC = new Color(100, 150, 255, 200);
  protected static final Color AC = new Color(230, 230, 230);
  protected static final Color RC = Color.ORANGE;
  protected final transient Shape shape;

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
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (getModel().isArmed()) {
      g2.setPaint(AC);
      g2.fill(shape);
    } else if (isRolloverEnabled() && getModel().isRollover()) {
      paintFocusAndRollover(g2, RC);
    } else if (hasFocus()) {
      paintFocusAndRollover(g2, FC);
    } else {
      g2.setPaint(getBackground());
      g2.fill(shape);
    }
    g2.dispose();
    super.paintComponent(g);
  }

  @Override protected void paintBorder(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(getForeground());
    g2.draw(shape);
    g2.dispose();
  }

  @Override public boolean contains(int x, int y) {
    // return shape != null && shape.contains(x, y);
    return Optional.ofNullable(shape)
        .map(s -> s.contains(x, y))
        .orElseGet(() -> super.contains(x, y));
  }

  // // TEST:
  // @Override public Dimension getPreferredSize() {
  //   Rectangle r = shape.getBounds();
  //   return new Dimension(r.width, r.height);
  // }
}

class ShapeSizeIcon implements Icon {
  private final Shape shape;

  protected ShapeSizeIcon(Shape s) {
    shape = s;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    /* Empty icon */
  }

  @Override public int getIconWidth() {
    return shape.getBounds().width;
  }

  @Override public int getIconHeight() {
    return shape.getBounds().height;
  }
}
