// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    RadarChartPanel chart = new RadarChartPanel();
    chart.setBackground(Color.WHITE);
    chart.setComponentPopupMenu(createPopupMenu(chart));
    add(chart);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPopupMenu createPopupMenu(RadarChartPanel chart) {
    JPopupMenu popup = new JPopupMenu();
    popup.add(createSidesMenu(chart));
    popup.add(createGridMenu(chart));
    JCheckBoxMenuItem check = new JCheckBoxMenuItem(
        "Show scale labels", chart.isScaleLabelVisible());
    check.addActionListener(e -> chart.setScaleLabelVisible(check.isSelected()));
    popup.add(check);
    popup.addSeparator();
    popup.add("Add series").addActionListener(e -> chart.addSeries());
    popup.add("Remove series").addActionListener(e -> chart.removeSeries());
    popup.add("Randomize values").addActionListener(e -> chart.randomize());
    return popup;
  }

  private static JMenu createSidesMenu(RadarChartPanel chart) {
    JMenu menu = new JMenu("Sides");
    ButtonGroup bg = new ButtonGroup();
    IntStream.of(5, 6, 7, 8, 10, 12).forEach(n -> {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem(
          Integer.toString(n), n == chart.getSides());
      item.addActionListener(e -> chart.setSides(n));
      bg.add(item);
      menu.add(item);
    });
    return menu;
  }

  private static JMenu createGridMenu(RadarChartPanel chart) {
    JMenu menu = new JMenu("Grid");
    ButtonGroup bg = new ButtonGroup();
    Stream.of(GridStyle.values()).forEach(style -> {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem(
          style.toString(), style == chart.getGridStyle());
      item.addActionListener(e -> chart.setGridStyle(style));
      bg.add(item);
      menu.add(item);
    });
    return menu;
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

enum GridStyle {
  NONE("None"), TICK("Tick marks"), POLYGON("Polygons"), CIRCLE("Circles");

  private final String label;

  GridStyle(String label) {
    this.label = label;
  }

  @Override public String toString() {
    return label;
  }
}

class RadarChartPanel extends JPanel {
  private static final int MAX_SERIES = 8;
  private final transient Random rnd = new Random();
  private final List<double[]> series = new ArrayList<>();
  private int sides = 6;
  private GridStyle gridStyle = GridStyle.POLYGON;
  private boolean scaleLabelVisible = true;

  protected RadarChartPanel() {
    super();
    for (int i = 0; i < 3; i++) {
      series.add(createRandomValues());
    }
  }

  public int getSides() {
    return sides;
  }

  public void setSides(int sides) {
    if (this.sides == sides) {
      return;
    }
    this.sides = sides;
    // The number of values per series changes, so regenerate all series
    randomize();
  }

  public GridStyle getGridStyle() {
    return gridStyle;
  }

  public void setGridStyle(GridStyle gridStyle) {
    this.gridStyle = gridStyle;
    repaint();
  }

  public boolean isScaleLabelVisible() {
    return scaleLabelVisible;
  }

  public void setScaleLabelVisible(boolean visible) {
    this.scaleLabelVisible = visible;
    repaint();
  }

  public void addSeries() {
    if (series.size() < MAX_SERIES) {
      series.add(createRandomValues());
      repaint();
    }
  }

  public void removeSeries() {
    if (!series.isEmpty()) {
      series.remove(series.size() - 1);
      repaint();
    }
  }

  public void randomize() {
    series.replaceAll(v -> createRandomValues());
    repaint();
  }

  private double[] createRandomValues() {
    return IntStream.range(0, sides)
        .mapToDouble(i -> 20d + rnd.nextInt(81))
        .toArray();
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Rectangle r = SwingUtilities.calculateInnerArea(this, null);
    if (r.isEmpty()) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    // Draw the chart in a 1000x1000 space and fit it into the panel,
    // keeping the aspect ratio and centering it
    double scale = Math.min(r.getWidth(), r.getHeight()) / RadarChart.SIZE;
    g2.translate(r.getCenterX(), r.getCenterY());
    g2.scale(scale, scale);
    g2.translate(-RadarChart.SIZE / 2d, -RadarChart.SIZE / 2d);
    RadarChart.drawGrid(g2, sides, gridStyle);
    if (scaleLabelVisible) {
      RadarChart.drawScaleLabels(g2);
    }
    for (int i = 0; i < series.size(); i++) {
      RadarChart.drawSeries(g2, series.get(i), RadarChart.getSeriesColor(i));
    }
    g2.dispose();
  }
}

final class RadarChart {
  public static final double SIZE = 1000d;
  private static final double CENTER = SIZE / 2d;
  private static final double RADIUS = 420d;
  private static final double MAX_VALUE = 100d;
  private static final int DIVISIONS = 10;
  private static final double TICK_LENGTH = 12d;
  private static final double LABEL_OFFSET = 8d;
  private static final int FILL_ALPHA = 0x4D; // .3
  private static final int LINE_ALPHA = 0xCC; // .8
  private static final Color GRID_COLOR = new Color(0x99_99_99);
  private static final Color AXIS_COLOR = new Color(0x66_66_66);
  private static final Stroke GRID_STROKE = new BasicStroke(3f);
  private static final Stroke SERIES_STROKE = new BasicStroke(4f);
  private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 28);

  private RadarChart() {
    /* Singleton */
  }

  private static double getAngle(int idx, int sides) {
    // The first vertex is at the 12 o'clock position
    return -Math.PI / 2d + 2d * Math.PI * idx / sides;
  }

  private static Point2D getPoint(int idx, int sides, double value) {
    double angle = getAngle(idx, sides);
    double r = RADIUS * value / MAX_VALUE;
    return new Point2D.Double(
        CENTER + r * Math.cos(angle), CENTER + r * Math.sin(angle));
  }

  private static Path2D createPolygon(double... values) {
    int sides = values.length;
    Path2D path = new Path2D.Double();
    for (int i = 0; i < sides; i++) {
      Point2D pt = getPoint(i, sides, values[i]);
      if (i == 0) {
        path.moveTo(pt.getX(), pt.getY());
      } else {
        path.lineTo(pt.getX(), pt.getY());
      }
    }
    path.closePath();
    return path;
  }

  private static Path2D createRegularPolygon(int sides, double value) {
    double[] values = new double[sides];
    Arrays.fill(values, value);
    return createPolygon(values);
  }

  private static Ellipse2D createCircle(double value) {
    double r = RADIUS * value / MAX_VALUE;
    return new Ellipse2D.Double(CENTER - r, CENTER - r, r * 2d, r * 2d);
  }

  // Circle for GridStyle.CIRCLE, regular polygon otherwise
  private static Shape createGridShape(int sides, double value, GridStyle style) {
    return style == GridStyle.CIRCLE
        ? createCircle(value)
        : createRegularPolygon(sides, value);
  }

  public static void drawGrid(Graphics2D g2, int sides, GridStyle style) {
    g2.setStroke(GRID_STROKE);
    if (style == GridStyle.POLYGON || style == GridStyle.CIRCLE) {
      g2.setColor(GRID_COLOR);
      for (int i = 1; i < DIVISIONS; i++) {
        g2.draw(createGridShape(sides, MAX_VALUE * i / DIVISIONS, style));
      }
    }
    // Axes and the outer frame are always drawn
    g2.setColor(AXIS_COLOR);
    Line2D axis = new Line2D.Double();
    for (int i = 0; i < sides; i++) {
      Point2D pt = getPoint(i, sides, MAX_VALUE);
      axis.setLine(CENTER, CENTER, pt.getX(), pt.getY());
      g2.draw(axis);
      if (style == GridStyle.TICK) {
        drawTicks(g2, i, sides);
      }
    }
    g2.draw(createGridShape(sides, MAX_VALUE, style));
  }

  // Short lines perpendicular to the axis at each division
  private static void drawTicks(Graphics2D g2, int idx, int sides) {
    double angle = getAngle(idx, sides);
    double dx = -Math.sin(angle) * TICK_LENGTH / 2d;
    double dy = Math.cos(angle) * TICK_LENGTH / 2d;
    Line2D tick = new Line2D.Double();
    for (int i = 1; i < DIVISIONS; i++) {
      Point2D pt = getPoint(idx, sides, MAX_VALUE * i / DIVISIONS);
      tick.setLine(pt.getX() - dx, pt.getY() - dy, pt.getX() + dx, pt.getY() + dy);
      g2.draw(tick);
    }
  }

  // Draw the scale values to the right of the 12 o'clock axis (0 is omitted)
  public static void drawScaleLabels(Graphics2D g2) {
    g2.setFont(LABEL_FONT);
    g2.setColor(AXIS_COLOR);
    FontMetrics fm = g2.getFontMetrics();
    // Offset from the tick position to the baseline to center the text vertically
    double baseline = (fm.getAscent() - fm.getDescent()) / 2d;
    float tx = (float) (CENTER + LABEL_OFFSET);
    for (int i = 1; i <= DIVISIONS; i++) {
      double y = CENTER - RADIUS * i / DIVISIONS;
      String label = Integer.toString((int) (MAX_VALUE * i / DIVISIONS));
      g2.drawString(label, tx, (float) (y + baseline));
    }
  }

  public static void drawSeries(Graphics2D g2, double[] values, Color color) {
    Path2D path = createPolygon(values);
    g2.setColor(withAlpha(color, FILL_ALPHA));
    g2.fill(path);
    g2.setStroke(SERIES_STROKE);
    g2.setColor(withAlpha(color, LINE_ALPHA));
    g2.draw(path);
  }

  public static Color getSeriesColor(int idx) {
    // Golden ratio steps keep neighboring hues well separated
    float hue = (float) (idx * .618 % 1d);
    return Color.getHSBColor(hue, .8f, .85f);
  }

  private static Color withAlpha(Color c, int alpha) {
    return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
  }
}
