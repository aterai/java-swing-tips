// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 2));
    int size = 32;
    float stroke = 4f;
    add(new SimpleStrokeSpinner(size, stroke));
    add(new SimpleAreaSpinner(size, stroke));
    add(new MaterialStrokeSpinner(size, stroke));
    add(new MaterialAreaSpinner(size, stroke));
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

// A value object that only holds the starting angle
// and sweep angle of the arc for one frame.
// record ArcAngles(float startAngle, float sweepAngle) {}
class ArcAngles {
  private final float startAngle;
  private final float sweepAngle;

  protected ArcAngles(float startAngle, float sweepAngle) {
    this.startAngle = startAngle;
    this.sweepAngle = sweepAngle;
  }

  public float getStartAngle() {
    return startAngle;
  }

  public float getSweepAngle() {
    return sweepAngle;
  }
}

// An abstract class that integrates size management
// and timer management common to the four spinners.
abstract class AbstractCircularSpinner extends JComponent {
  private final float size;
  private final float stroke;
  private final long startTime;
  private final Timer timer = new Timer(16, e -> repaint());

  protected AbstractCircularSpinner(float size, float stroke) {
    super();
    this.size = size;
    this.stroke = stroke;
    this.startTime = System.currentTimeMillis();
    this.timer.start();
  }

  @Override public Dimension getPreferredSize() {
    int totalSize = (int) Math.ceil(size + stroke);
    return new Dimension(totalSize, totalSize);
  }

  @Override public void removeNotify() {
    super.removeNotify();
    timer.stop();
  }

  protected float getDiameter() {
    return size;
  }

  protected float getStroke() {
    return stroke;
  }

  // Template method body
  // Only two functions, 'finding the angle' and 'drawing based on the angle',
  // are delegated to the subclass.
  @Override protected final void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = createValidatedGraphics2D(g);
    float x = (getWidth() - size) / 2f;
    float y = (getHeight() - size) / 2f;
    long elapsed = System.currentTimeMillis() - startTime;
    ArcAngles arc = computeArcAngles(elapsed);
    paintArc(g2, x, y, arc);
    g2.dispose();
  }

  // Template method 1:
  // Find the angle of the arc from the elapsed time
  protected abstract ArcAngles computeArcAngles(long elapsedMillis);

  // Template method 2:
  // Actual drawing after determining the angle
  protected abstract void paintArc(Graphics2D g2, float x, float y, ArcAngles arc);

  // Common Graphics2D initialization process
  protected Graphics2D createValidatedGraphics2D(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2.setRenderingHint(
        RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    return g2;
  }

  // Common ring generation used in Area drawing system
  protected Area createRing(float x, float y) {
    Area outer = new Area(new Ellipse2D.Float(x, y, size, size));
    float innerSize = size - stroke * 2f;
    float innerOffset = (size - innerSize) / 2f;
    float ax = x + innerOffset;
    float ay = y + innerOffset;
    Area inner = new Area(new Ellipse2D.Float(ax, ay, innerSize, innerSize));
    outer.subtract(inner);
    return outer;
  }

  protected Area createArcArea(Area ring, float startAngle, float sweepAngle) {
    float x = (getWidth() - size) / 2f;
    float y = (getHeight() - size) / 2f;
    float cx = x + size / 2f;
    float cy = y + size / 2f;
    float r = size / 2f + 1f;
    Arc2D arc = new Arc2D.Float(
        cx - r, cy - r, r * 2f, r * 2f, startAngle, sweepAngle, Arc2D.PIE);
    Area arcSector = new Area(arc);
    Area arcArea = new Area(ring);
    arcArea.intersect(arcSector);
    return arcArea;
  }
}

// Intermediate abstract class that summarizes angle calculations
// for simple systems (uniform rotation)
abstract class AbstractSimpleSpinner extends AbstractCircularSpinner {
  protected AbstractSimpleSpinner(float size, float stroke) {
    super(size, stroke);
  }

  @Override protected final ArcAngles computeArcAngles(long elapsedMillis) {
    float angle = elapsedMillis / 16f * 10f % 360f;
    return new ArcAngles(-angle, 90f);
  }
}

// Intermediate abstract class that summarizes angle calculations
// for Material system (stretch/contract/rotate)
abstract class AbstractMaterialSpinner extends AbstractCircularSpinner {
  private static final float CYCLE_DURATION = 1332f;
  private static final float MAX_SWEEP = 270f;
  private static final float MIN_SWEEP = 15f;

  protected AbstractMaterialSpinner(float size, float stroke) {
    super(size, stroke);
  }

  @Override protected final ArcAngles computeArcAngles(long elapsedMillis) {
    float totalCycles = elapsedMillis / CYCLE_DURATION;
    int cycleIndex = (int) Math.floor(totalCycles);
    float t = totalCycles - cycleIndex;
    float sweepRange = MAX_SWEEP - MIN_SWEEP;
    float cycleOffset = cycleIndex * sweepRange;

    float head;
    float tail;
    boolean isFirstHalf = t < .5f;
    if (isFirstHalf) {
      head = cycleOffset + MIN_SWEEP + (sweepRange * easeInOutCubic(t / .5f));
      tail = cycleOffset;
    } else {
      head = cycleOffset + MAX_SWEEP;
      tail = cycleOffset + (sweepRange * easeInOutCubic((t - .5f) / .5f));
    }

    float baseSpin = -360f * (elapsedMillis / 3000f);
    float startAngle = baseSpin - tail;
    float sweepAngle = tail - head;
    return new ArcAngles(startAngle, sweepAngle);
  }

  private static float easeInOutCubic(float x) {
    return x < .5f ? 4f * x * x * x : 1f - (float) Math.pow(-2f * x + 2f, 3f) / 2f;
  }
}

// 1. Constant speed rotation × line drawing
class SimpleStrokeSpinner extends AbstractSimpleSpinner {
  protected SimpleStrokeSpinner(float size, float stroke) {
    super(size, stroke);
  }

  @Override protected void paintArc(Graphics2D g2, float x, float y, ArcAngles arc) {
    g2.setStroke(new BasicStroke(
        getStroke(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setColor(new Color(0x34_98_DB));
    g2.draw(new Arc2D.Float(
        x, y, getDiameter(), getDiameter(),
        arc.getStartAngle(), arc.getSweepAngle(), Arc2D.OPEN));
  }
}

// 2. Constant speed rotation × Area drawing
class SimpleAreaSpinner extends AbstractSimpleSpinner {
  protected SimpleAreaSpinner(float size, float stroke) {
    super(size, stroke);
  }

  @Override protected void paintArc(Graphics2D g2, float x, float y, ArcAngles arc) {
    Area ring = createRing(x, y);
    g2.setColor(Color.LIGHT_GRAY);
    g2.fill(ring);
    g2.setColor(new Color(0x34_98_DB));
    g2.fill(createArcArea(ring, arc.getStartAngle(), arc.getSweepAngle()));
  }
}

// 3. Stretch/contract rotation × line drawing
class MaterialStrokeSpinner extends AbstractMaterialSpinner {
  protected MaterialStrokeSpinner(float size, float stroke) {
    super(size, stroke);
  }

  @Override protected void paintArc(Graphics2D g2, float x, float y, ArcAngles arc) {
    g2.setStroke(new BasicStroke(
        getStroke(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setColor(new Color(0xFF_00_00));
    g2.draw(new Arc2D.Float(
        x, y, getDiameter(), getDiameter(),
        arc.getStartAngle(), arc.getSweepAngle(), Arc2D.OPEN));
  }
}

// 4. Stretch/contract rotation × Area drawing
class MaterialAreaSpinner extends AbstractMaterialSpinner {
  protected MaterialAreaSpinner(float size, float stroke) {
    super(size, stroke);
  }

  @Override protected void paintArc(Graphics2D g2, float x, float y, ArcAngles arc) {
    Area ring = createRing(x, y);
    g2.setColor(Color.LIGHT_GRAY);
    g2.fill(ring);
    g2.setColor(new Color(0xFF_00_00));
    g2.fill(createArcArea(ring, arc.getStartAngle(), arc.getSweepAngle()));
  }
}
