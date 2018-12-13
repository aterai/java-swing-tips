package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JButton button = new JButton("RoundedCornerButtonUI") {
    @Override public void updateUI() {
      // IGNORE LnF change: super.updateUI();
      setUI(new RoundedCornerButtonUI());
    }
  };

  public MainPanel() {
    super();
    add(new JButton("Default JButton"));
    // button.setUI(new RoundedCornerButtonUI());
    add(button);
    add(new RoundedCornerButton("Rounded Corner Button"));
    add(new RoundButton(new ImageIcon(getClass().getResource("16x16.png"))) {
      @Override public Dimension getPreferredSize() {
        int r = 16 + (FOCUS_STROKE + 4) * 2; // test margin = 4
        return new Dimension(r, r);
      }
    });
    add(new ShapeButton(makeStar(25, 30, 20)));
    add(new RoundButton("Round Button"));
    setPreferredSize(new Dimension(320, 240));
  }

  private Path2D makeStar(int r1, int r2, int vc) {
    int or = Math.max(r1, r2);
    int ir = Math.min(r1, r2);
    double agl = 0d;
    double add = 2 * Math.PI / (vc * 2);
    Path2D p = new Path2D.Double();
    p.moveTo(or * 1, or * 0);
    for (int i = 0; i < vc * 2 - 1; i++) {
      agl += add;
      int r = i % 2 == 0 ? ir : or;
      p.lineTo(r * Math.cos(agl), r * Math.sin(agl));
    }
    p.closePath();
    AffineTransform at = AffineTransform.getRotateInstance(-Math.PI / 2, or, 0);
    return new Path2D.Double(p, at);
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

class RoundedCornerButton extends JButton {
  private static final double ARC_WIDTH = 16d;
  private static final double ARC_HEIGHT = 16d;
  protected static final int FOCUS_STROKE = 2;
  protected final Color fc = new Color(100, 150, 255, 200);
  protected final Color ac = new Color(230, 230, 230);
  protected final Color rc = Color.ORANGE;
  protected Shape shape;
  protected Shape border;
  protected Shape base;

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
    // setBackground(new Color(250, 250, 250));
    // initShape();
  }

  @Override public void updateUI() {
    super.updateUI();
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBackground(new Color(250, 250, 250));
    initShape();
  }

  protected void initShape() {
    if (!getBounds().equals(base)) {
      base = getBounds();
      shape = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, ARC_WIDTH, ARC_HEIGHT);
      border = new RoundRectangle2D.Double(FOCUS_STROKE, FOCUS_STROKE,
                         getWidth() - 1 - FOCUS_STROKE * 2,
                         getHeight() - 1 - FOCUS_STROKE * 2,
                         ARC_WIDTH, ARC_HEIGHT);
    }
  }

  private void paintFocusAndRollover(Graphics2D g2, Color color) {
    g2.setPaint(new GradientPaint(0, 0, color, getWidth() - 1, getHeight() - 1, color.brighter(), true));
    g2.fill(shape);
    g2.setPaint(getBackground());
    g2.fill(border);
  }

  @Override protected void paintComponent(Graphics g) {
    initShape();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (getModel().isArmed()) {
      g2.setPaint(ac);
      g2.fill(shape);
    } else if (isRolloverEnabled() && getModel().isRollover()) {
      paintFocusAndRollover(g2, rc);
    } else if (hasFocus()) {
      paintFocusAndRollover(g2, fc);
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
    // return Optional.ofNullable(shape).filter(s -> s.contains(x, y)).isPresent();
    return Optional.ofNullable(shape).map(s -> s.contains(x, y)).orElse(false);
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
      shape = new Ellipse2D.Double(0, 0, getWidth() - 1, getHeight() - 1);
      border = new Ellipse2D.Double(FOCUS_STROKE, FOCUS_STROKE,
                      getWidth() - 1 - FOCUS_STROKE * 2,
                      getHeight() - 1 - FOCUS_STROKE * 2);
    }
  }
}

class ShapeButton extends JButton {
  protected final Color fc = new Color(100, 150, 255, 200);
  protected final Color ac = new Color(230, 230, 230);
  protected final Color rc = Color.ORANGE;
  protected final Shape shape;

  protected ShapeButton(Shape s) {
    super();
    shape = s;
    setModel(new DefaultButtonModel());
    init("Shape", new DummySizeIcon(s));
    setVerticalAlignment(SwingConstants.CENTER);
    setVerticalTextPosition(SwingConstants.CENTER);
    setHorizontalAlignment(SwingConstants.CENTER);
    setHorizontalTextPosition(SwingConstants.CENTER);
    setBorder(BorderFactory.createEmptyBorder());
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBackground(new Color(250, 250, 250));
  }

  private void paintFocusAndRollover(Graphics2D g2, Color color) {
    g2.setPaint(new GradientPaint(0, 0, color, getWidth() - 1, getHeight() - 1, color.brighter(), true));
    g2.fill(shape);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (getModel().isArmed()) {
      g2.setPaint(ac);
      g2.fill(shape);
    } else if (isRolloverEnabled() && getModel().isRollover()) {
      paintFocusAndRollover(g2, rc);
    } else if (hasFocus()) {
      paintFocusAndRollover(g2, fc);
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
    return shape.contains(x, y);
  }
  // // TEST:
  // @Override public Dimension getPreferredSize() {
  //   Rectangle r = shape.getBounds();
  //   return new Dimension(r.width, r.height);
  // }
}

class DummySizeIcon implements Icon {
  private final Shape shape;

  protected DummySizeIcon(Shape s) {
    shape = s;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* Empty icon */ }

  @Override public int getIconWidth() {
    return shape.getBounds().width;
  }

  @Override public int getIconHeight() {
    return shape.getBounds().height;
  }
}
