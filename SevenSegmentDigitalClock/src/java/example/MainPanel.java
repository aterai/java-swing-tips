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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.Timer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    add(new DigitalClock());
    add(new HelpPanel());
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
    dot1 = new Ellipse2D.Double(x, (float) r.getCenterY() - gap, sz, sz);
    dot2 = new Ellipse2D.Double(x, (float) r.getCenterY() + gap, sz, sz);
    x += sz + gap;
    m1 = new DigitalNumber(x, y, SIZE);
    x += r.width + gap;
    m2 = new DigitalNumber(x, y, SIZE);
    x += r.width + gap;
    double hs = SIZE / 2d;
    double y2 = y + h1.getBounds().height / 4d;
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
    setBackground(DigitalNumber.BGC);
  }

  private void updateTime() {
    int ten = 10;
    LocalTime time = LocalTime.now(ZoneId.systemDefault());
    // set Hours
    int hours = time.getHour();
    if (hours < ten) {
      h1.turnOffNumber();
      h2.setNumber(hours);
    } else {
      int dh = hours / ten;
      h1.setNumber(dh);
      h2.setNumber(hours - dh * ten);
    }
    // set Minutes
    int minutes = time.getMinute();
    int dm = minutes / ten;
    m1.setNumber(dm);
    m2.setNumber(minutes - dm * ten);

    // set Seconds
    int seconds = time.getSecond();
    int ds = seconds / ten;
    s1.setNumber(ds);
    s2.setNumber(seconds - ds * ten);
  }

  @Override public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setStroke(new BasicStroke(3f));
    g2.shear(-.1, 0d);
    double sv = getWidth() / (h1.getBounds().width * 8d);
    g2.scale(sv, sv);
    h1.drawNumber(g2);
    h2.drawNumber(g2);
    g2.setColor(pulse ? DigitalNumber.ON : DigitalNumber.OFF);
    g2.fill(dot1);
    g2.fill(dot2);
    m1.drawNumber(g2);
    m2.drawNumber(g2);
    s1.drawNumber(g2);
    s2.drawNumber(g2);
    g2.dispose();
  }
}

class DigitalNumber {
  public static final Color OFF = new Color(0xCC_CC_CC);
  public static final Color ON = Color.DARK_GRAY;
  public static final Color BGC = Color.LIGHT_GRAY;
  private final double isosceles;
  private final double dx;
  private final double dy;
  private final double width;
  private final double height;
  private final Rectangle rect = new Rectangle();
  private final List<Set<Seg>> numbers = Arrays.asList(
      EnumSet.of(Seg.A, Seg.B, Seg.C, Seg.D, Seg.E, Seg.F),
      EnumSet.of(Seg.B, Seg.C),
      EnumSet.of(Seg.A, Seg.B, Seg.D, Seg.E, Seg.G),
      EnumSet.of(Seg.A, Seg.B, Seg.C, Seg.D, Seg.G),
      EnumSet.of(Seg.B, Seg.C, Seg.F, Seg.G),
      EnumSet.of(Seg.A, Seg.C, Seg.D, Seg.F, Seg.G),
      EnumSet.of(Seg.A, Seg.C, Seg.D, Seg.E, Seg.F, Seg.G),
      EnumSet.of(Seg.A, Seg.B, Seg.C),
      EnumSet.of(Seg.A, Seg.B, Seg.C, Seg.D, Seg.E, Seg.F, Seg.G),
      EnumSet.of(Seg.A, Seg.B, Seg.C, Seg.D, Seg.F, Seg.G));
  private Set<Seg> led = EnumSet.noneOf(Seg.class);

  protected DigitalNumber(double dx, double dy, double isosceles) {
    this.isosceles = isosceles;
    this.dx = dx;
    this.dy = dy;
    this.width = 2d * isosceles;
    this.height = width + isosceles;
    rect.setLocation((int) (dx - isosceles), (int) (dy - height * 2d));
    rect.setSize((int) (width + 4d * isosceles), (int) (height * 4d));
  }

  public Rectangle getBounds() {
    return rect;
  }

  public void setNumber(int num) {
    led = numbers.get(num);
  }

  public void turnOffNumber() {
    led.clear();
  }

  public void drawNumber(Graphics2D g2) {
    EnumSet.allOf(Seg.class).forEach(s -> {
      g2.setColor(led.contains(s) ? ON : OFF);
      Shape seg = s.getShape(dx, dy, width, height, isosceles);
      g2.fill(seg);
      g2.setColor(BGC);
      g2.draw(seg);
      // g2.setColor(Color.RED);
      // g2.draw(rect);
    });
  }
}

enum Seg {
  A() {
    @Override public Shape getShape(double x, double y, double w, double h, double i) {
      AffineTransform at = AffineTransform.getTranslateInstance(x, y - h - i * 2);
      return at.createTransformedShape(horiz2(w, i));
    }
  },
  B() {
    @Override public Shape getShape(double x, double y, double w, double h, double i) {
      AffineTransform at = AffineTransform.getTranslateInstance(x + w + i * 2, y);
      at.scale(-1, 1);
      return at.createTransformedShape(vert(h, i));
    }
  },
  C() {
    @Override public Shape getShape(double x, double y, double w, double h, double i) {
      AffineTransform at = AffineTransform.getTranslateInstance(x + w + i * 2, y);
      at.scale(-1, -1);
      return at.createTransformedShape(vert(h, i));
    }
  },
  D() {
    @Override public Shape getShape(double x, double y, double w, double h, double i) {
      AffineTransform at = AffineTransform.getTranslateInstance(x, y + h + i * 2);
      at.scale(1, -1);
      return at.createTransformedShape(horiz2(w, i));
    }
  },
  E() {
    @Override public Shape getShape(double x, double y, double w, double h, double i) {
      AffineTransform at = AffineTransform.getTranslateInstance(x, y);
      at.scale(1, -1);
      return at.createTransformedShape(vert(h, i));
    }
  },
  F() {
    @Override public Shape getShape(double x, double y, double w, double h, double i) {
      AffineTransform at = AffineTransform.getTranslateInstance(x, y);
      return at.createTransformedShape(vert(h, i));
    }
  },
  G() {
    @Override public Shape getShape(double x, double y, double w, double h, double i) {
      AffineTransform at = AffineTransform.getTranslateInstance(x, y);
      return at.createTransformedShape(horiz1(w, i));
    }
  };

  public abstract Shape getShape(double x, double y, double w, double h, double i);

  private static Path2D vert(double height, double isosceles) {
    Path2D path = new Path2D.Double();
    path.moveTo(0d, 0d);
    path.lineTo(isosceles, -isosceles);
    path.lineTo(isosceles, -isosceles - height);
    path.lineTo(-isosceles, -isosceles - height - isosceles * 2);
    path.lineTo(-isosceles, -isosceles);
    path.closePath();
    return path;
  }

  private static Path2D horiz1(double width, double isosceles) {
    Path2D path = new Path2D.Double();
    path.moveTo(0, 0);
    path.lineTo(isosceles, isosceles);
    path.lineTo(isosceles + width, isosceles);
    path.lineTo(isosceles + width + isosceles, 0);
    path.lineTo(isosceles + width, -isosceles);
    path.lineTo(isosceles, -isosceles);
    path.closePath();
    return path;
  }

  private static Path2D horiz2(double width, double isosceles) {
    Path2D path = new Path2D.Double();
    path.moveTo(isosceles, isosceles);
    path.lineTo(isosceles + width, isosceles);
    path.lineTo(3 * isosceles + width, -isosceles);
    path.lineTo(-isosceles, -isosceles);
    path.closePath();
    return path;
  }
}

class HelpPanel extends JPanel {
  private static final double SIZE = 16d;
  private final transient DigitalNumber help = new DigitalNumber(SIZE * 3d, SIZE * 8d, SIZE);

  protected HelpPanel() {
    super();
    help.setNumber(8);
  }

  @Override public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setStroke(new BasicStroke(3f));
    g2.shear(-.1, 0d);
    double sv = getWidth() / (help.getBounds().width * 8d);
    g2.scale(sv, sv);
    help.drawNumber(g2);
    g2.setPaint(Color.RED);
    g2.setFont(getFont().deriveFont(32f));
    Rectangle r = help.getBounds();
    float fw = help.getBounds().width;
    float fh = help.getBounds().height;
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
