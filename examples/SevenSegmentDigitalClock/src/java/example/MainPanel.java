// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.Timer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    add(new DigitalClock());
    add(new SegmentLegendPanel());
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

class DigitalClock extends JPanel {
  private static final double SIZE = 16d;
  private final transient DigitalNumber h1;
  private final transient DigitalNumber h2;
  private final transient DigitalNumber m1;
  private final transient DigitalNumber m2;
  private final transient DigitalNumber s1;
  private final transient DigitalNumber s2;
  private final transient Shape dot1;
  private final transient Shape dot2;
  private boolean pulse;
  private final Timer timer = new Timer(250, e -> {
    updateTime();
    pulse = !pulse;
    repaint();
  });
  private transient HierarchyListener listener;

  protected DigitalClock() {
    super();
    double x = SIZE * 3d;
    double y = SIZE * 8d;
    double gap = SIZE * 1.5;
    h1 = new DigitalNumber(x, y, SIZE);
    Rectangle r = h1.getBounds();
    x += r.width + gap;
    h2 = new DigitalNumber(x, y, SIZE);
    x += r.width;
    double sz = SIZE * 1.5d;
    dot1 = new Ellipse2D.Double(x, r.getCenterY() - gap, sz, sz);
    dot2 = new Ellipse2D.Double(x, r.getCenterY() + gap, sz, sz);
    x += sz + gap;
    m1 = new DigitalNumber(x, y, SIZE);
    x += r.width + gap;
    m2 = new DigitalNumber(x, y, SIZE);
    x += r.width + gap;
    double hs = SIZE / 2d;
    double y2 = y + r.height / 4d;
    s1 = new DigitalNumber(x, y2, hs);
    x += s1.getBounds().width + gap / 2d;
    s2 = new DigitalNumber(x, y2, hs);
  }

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    listener = e -> {
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
        if (e.getComponent().isShowing()) {
          timer.start();
        } else {
          timer.stop();
        }
      }
    };
    addHierarchyListener(listener);
    setBackground(DigitalNumber.BACKGROUND);
  }

  private void updateTime() {
    int ten = 10;
    LocalTime time = LocalTime.now(ZoneId.systemDefault());
    int hours = time.getHour();
    if (hours < ten) {
      h1.turnOff(); // suppress the leading zero
    } else {
      h1.setNumber(hours / ten);
    }
    h2.setNumber(hours % ten);
    int minutes = time.getMinute();
    m1.setNumber(minutes / ten);
    m2.setNumber(minutes % ten);
    int seconds = time.getSecond();
    s1.setNumber(seconds / ten);
    s2.setNumber(seconds % ten);
  }

  @Override public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setStroke(new BasicStroke(3f));
    g2.shear(-.1, 0d);
    double scale = getWidth() / (h1.getBounds().width * 8d);
    g2.scale(scale, scale);
    h1.draw(g2);
    h2.draw(g2);
    g2.setColor(pulse ? DigitalNumber.ON : DigitalNumber.OFF);
    g2.fill(dot1);
    g2.fill(dot2);
    m1.draw(g2);
    m2.draw(g2);
    s1.draw(g2);
    s2.draw(g2);
    g2.dispose();
  }
}

class DigitalNumber {
  public static final Color OFF = new Color(0xCC_CC_CC);
  public static final Color ON = Color.DARK_GRAY;
  public static final Color BACKGROUND = Color.LIGHT_GRAY;
  private static final List<Set<Segment>> NUMBERS = Arrays.asList(
      EnumSet.of(Segment.A, Segment.B, Segment.C, Segment.D, Segment.E, Segment.F),
      EnumSet.of(Segment.B, Segment.C),
      EnumSet.of(Segment.A, Segment.B, Segment.D, Segment.E, Segment.G),
      EnumSet.of(Segment.A, Segment.B, Segment.C, Segment.D, Segment.G),
      EnumSet.of(Segment.B, Segment.C, Segment.F, Segment.G),
      EnumSet.of(Segment.A, Segment.C, Segment.D, Segment.F, Segment.G),
      EnumSet.of(Segment.A, Segment.C, Segment.D, Segment.E, Segment.F, Segment.G),
      EnumSet.of(Segment.A, Segment.B, Segment.C),
      EnumSet.allOf(Segment.class),
      EnumSet.of(Segment.A, Segment.B, Segment.C, Segment.D, Segment.F, Segment.G));
  @SuppressWarnings("PMD.UseConcurrentHashMap")
  private final Map<Segment, Shape> segments = new EnumMap<>(Segment.class);
  private final Rectangle bounds;
  private Set<Segment> lit = EnumSet.noneOf(Segment.class);

  protected DigitalNumber(double x, double y, double isosceles) {
    double width = 2d * isosceles;
    double height = width + isosceles;
    for (Segment s : Segment.values()) {
      segments.put(s, s.getShape(x, y, width, height, isosceles));
    }
    bounds = segments.values().stream()
        .map(Shape::getBounds)
        .reduce(Rectangle::union)
        .orElseGet(Rectangle::new);
  }

  public Rectangle getBounds() {
    return bounds;
  }

  public void setNumber(int number) {
    lit = NUMBERS.get(number);
  }

  public void turnOff() {
    lit = EnumSet.noneOf(Segment.class);
  }

  public void draw(Graphics2D g2) {
    segments.forEach((segment, shape) -> {
      g2.setColor(lit.contains(segment) ? ON : OFF);
      g2.fill(shape);
      g2.setColor(BACKGROUND);
      g2.draw(shape);
    });
  }
}

enum Segment {
  A {
    @Override public Shape getShape(
        double x, double y, double w, double h, double isosceles) {
      AffineTransform at = AffineTransform.getTranslateInstance(x, y - h - isosceles * 2d);
      return at.createTransformedShape(createTrapezoid(w, isosceles));
    }
  },
  B {
    @Override public Shape getShape(
        double x, double y, double w, double h, double isosceles) {
      AffineTransform at = AffineTransform.getTranslateInstance(x + w + isosceles * 2d, y);
      at.scale(-1d, 1d);
      return at.createTransformedShape(createPentagon(h, isosceles));
    }
  },
  C {
    @Override public Shape getShape(
        double x, double y, double w, double h, double isosceles) {
      AffineTransform at = AffineTransform.getTranslateInstance(x + w + isosceles * 2d, y);
      at.scale(-1d, -1d);
      return at.createTransformedShape(createPentagon(h, isosceles));
    }
  },
  D {
    @Override public Shape getShape(
        double x, double y, double w, double h, double isosceles) {
      AffineTransform at = AffineTransform.getTranslateInstance(x, y + h + isosceles * 2d);
      at.scale(1d, -1d);
      return at.createTransformedShape(createTrapezoid(w, isosceles));
    }
  },
  E {
    @Override public Shape getShape(
        double x, double y, double w, double h, double isosceles) {
      AffineTransform at = AffineTransform.getTranslateInstance(x, y);
      at.scale(1d, -1d);
      return at.createTransformedShape(createPentagon(h, isosceles));
    }
  },
  F {
    @Override public Shape getShape(
        double x, double y, double w, double h, double isosceles) {
      AffineTransform at = AffineTransform.getTranslateInstance(x, y);
      return at.createTransformedShape(createPentagon(h, isosceles));
    }
  },
  G {
    @Override public Shape getShape(
        double x, double y, double w, double h, double isosceles) {
      AffineTransform at = AffineTransform.getTranslateInstance(x, y);
      return at.createTransformedShape(createHexagon(w, isosceles));
    }
  };

  public abstract Shape getShape(
      double x, double y, double w, double h, double isosceles);

  // Vertical segment (F): the origin is the bottom vertex
  private static Path2D createPentagon(double height, double isosceles) {
    Path2D path = new Path2D.Double();
    path.moveTo(0d, 0d);
    path.lineTo(isosceles, -isosceles);
    path.lineTo(isosceles, -isosceles - height);
    path.lineTo(-isosceles, -isosceles - height - isosceles * 2d);
    path.lineTo(-isosceles, -isosceles);
    path.closePath();
    return path;
  }

  // Middle segment (G): the origin is the left vertex
  private static Path2D createHexagon(double width, double isosceles) {
    Path2D path = new Path2D.Double();
    path.moveTo(0d, 0d);
    path.lineTo(isosceles, isosceles);
    path.lineTo(isosceles + width, isosceles);
    path.lineTo(isosceles + width + isosceles, 0d);
    path.lineTo(isosceles + width, -isosceles);
    path.lineTo(isosceles, -isosceles);
    path.closePath();
    return path;
  }

  // Top segment (A): the origin is the midpoint of the left edge
  private static Path2D createTrapezoid(double width, double isosceles) {
    Path2D path = new Path2D.Double();
    path.moveTo(isosceles, isosceles);
    path.lineTo(isosceles + width, isosceles);
    path.lineTo(3d * isosceles + width, -isosceles);
    path.lineTo(-isosceles, -isosceles);
    path.closePath();
    return path;
  }
}

class SegmentLegendPanel extends JPanel {
  private static final double SIZE = 16d;
  private final transient DigitalNumber digit = new DigitalNumber(
      SIZE * 3d, SIZE * 8d, SIZE);

  protected SegmentLegendPanel() {
    super();
    digit.setNumber(8);
  }

  @Override public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setStroke(new BasicStroke(3f));
    g2.shear(-.1, 0d);
    Rectangle r = digit.getBounds();
    double scale = getWidth() / (r.width * 8d);
    g2.scale(scale, scale);
    digit.draw(g2);
    g2.setPaint(Color.RED);
    g2.setFont(getFont().deriveFont(32f));
    float fw = r.width;
    float fh = r.height;
    g2.drawString("A", r.x + fw * .5f, r.y);
    g2.drawString("B", r.x + fw * .75f, r.y + fh * .25f);
    g2.drawString("C", r.x + fw * .75f, r.y + fh * .75f);
    g2.drawString("D", r.x + fw * .5f, r.y + fh);
    g2.drawString("E", r.x, r.y + fh * .75f);
    g2.drawString("F", r.x, r.y + fh * .25f);
    g2.drawString("G", r.x + fw * .5f, r.y + fh * .5f);
    g2.dispose();
  }
}
