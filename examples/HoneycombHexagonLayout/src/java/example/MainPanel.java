// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  // n determines button count per row
  private static final int N = 2;
  private static final int TOTAL_ROWS = 3;
  // Number of buttons in the flower(1 center + 6 around) arrangement
  private static final int FLOWER_SIZE = 7;
  // Gap between adjacent hexagon edges in pixels
  // 0 = perfectly touching, positive = gap
  private static final int BTN_GAP = 6;
  private static final Color BTN_BGC = new Color(70, 130, 180); // Steel blue
  private static final Color PANEL_BGC = new Color(45, 45, 45);

  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("PointyTopped: Rows", createRowsPanel());
    tabbedPane.addTab("FlatTopped: Flower", createFlowerPanel());
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  // Pointy-topped hexagons laid out row by row
  private static Component createRowsPanel() {
    int evenCount = 2 * N - 1; // Buttons in even rows
    int oddCount = 2 * N; // Buttons in odd  rows
    LayoutManager layout = new HoneycombRowsLayout(TOTAL_ROWS, evenCount, oddCount, BTN_GAP);

    // Calculate total button count and add them
    int totalButtons = 0;
    for (int r = 0; r < TOTAL_ROWS; r++) {
      totalButtons += r % 2 == 0 ? evenCount : oddCount;
    }
    return createHexagonPanel(layout, totalButtons, HexagonOrientation.POINTY_TOPPED);
  }

  // Flat-topped hexagons laid out in a flower pattern: 1 center + 6 around
  private static Component createFlowerPanel() {
    LayoutManager layout = new HoneycombFlowerLayout(BTN_GAP);
    return createHexagonPanel(layout, FLOWER_SIZE, HexagonOrientation.FLAT_TOPPED);
  }

  private static Component createHexagonPanel(
      LayoutManager layout, int buttonCount, HexagonOrientation orientation) {
    JPanel p = new JPanel(layout);
    p.setBackground(PANEL_BGC);
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    for (int i = 0; i < buttonCount; i++) {
      p.add(createHexagonButton(i, orientation));
    }
    return p;
  }

  private static HexagonButton createHexagonButton(int i, HexagonOrientation orientation) {
    HexagonButton btn = new HexagonButton("ID: " + i, orientation);
    btn.setBackground(BTN_BGC);
    btn.setForeground(Color.WHITE);
    return btn;
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

// Hexagon orientation
// The circumradius R is always half of the longer side of the bounding box,
// so only the angle of the first vertex differs between the two orientations.
// PointyTopped: W = R * sqrt(3), H = R * 2 -> W < H (always), R = H / 2
// FlatTopped:   W = R * 2, H = R * sqrt(3) -> W > H (always), R = W / 2
enum HexagonOrientation {
  POINTY_TOPPED(-Math.PI / 2d), // Start at 12 oclock
  FLAT_TOPPED(0d); // Start at 3 oclock

  private final double startAngle;

  HexagonOrientation(double startAngle) {
    this.startAngle = startAngle;
  }

  public double getStartAngle() {
    return startAngle;
  }
}

// Hexagon button component
class HexagonButton extends JButton {
  private static final int VERTICES = 6;
  private final HexagonOrientation orientation;
  private Polygon hexagon;
  private boolean isHovered;
  private transient MouseListener hoverHandler;

  protected HexagonButton(String text, HexagonOrientation orientation) {
    super(text);
    this.orientation = orientation;
  }

  @Override public void updateUI() {
    removeMouseListener(hoverHandler);
    super.updateUI();
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBorderPainted(false);
    setOpaque(false);
    hoverHandler = new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        isHovered = true;
        repaint();
      }

      @Override public void mouseExited(MouseEvent e) {
        isHovered = false;
        repaint();
      }
    };
    addMouseListener(hoverHandler);
  }

  // Recalculate the hexagon polygon to fill the component bounds exactly.
  // The circumradius R equals half of the longer side of the bounding box,
  // so Math.max(cx, cy) is used: Math.min(cx, cy) would shrink the hexagon
  // and leave gaps on all sides.
  private void calculateHexagon() {
    int cx = getWidth() / 2;
    int cy = getHeight() / 2;
    // int radius = Math.min(cx, cy);
    int radius = Math.max(cx, cy);
    hexagon = new Polygon();
    for (int i = 0; i < VERTICES; i++) {
      // Start at the orientation angle, step by 60 degrees(PI/3)
      double angle = orientation.getStartAngle() + i * Math.PI / 3d;
      hexagon.addPoint(
          (int) Math.round(cx + radius * Math.cos(angle)),
          (int) Math.round(cy + radius * Math.sin(angle)));
    }
  }

  // Hit-test against the hexagon shape, not the bounding rectangle.
  @Override public boolean contains(int x, int y) {
    if (hexagon == null || hexagon.getBounds().width != getWidth()) {
      calculateHexagon();
    }
    return hexagon.contains(x, y);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    calculateHexagon();

    // Choose fill color based on interaction state
    Color bg = getBackground();
    if (getModel().isArmed()) {
      g2.setColor(bg.darker());
    } else if (isHovered) {
      g2.setColor(bg.brighter());
    } else {
      g2.setColor(bg);
    }
    g2.fillPolygon(hexagon); // Always fill

    // Draw border; glow effect on hover
    if (isHovered) {
      g2.setStroke(new BasicStroke(3f));
      g2.setColor(new Color(255, 255, 255, 100));
      g2.drawPolygon(hexagon);
      g2.setStroke(new BasicStroke(1.5f));
      g2.setColor(Color.WHITE);
      g2.drawPolygon(hexagon);
    } else {
      g2.setStroke(new BasicStroke(1f));
      g2.setColor(bg.darker());
      g2.drawPolygon(hexagon);
    }

    g2.dispose();
    super.paintComponent(g); // Draw label text
  }
}

// Base class of the honeycomb hexagon button layout managers
abstract class AbstractHoneycombLayout implements LayoutManager {
  // Bounding box ratio of a regular hexagon: sqrt(3) / 2
  protected static final double RATIO = Math.sqrt(3d) / 2d;
  // Visual gap between adjacent hexagon edges, in pixels.
  // gap = 0 : edges touch perfectly
  // gap > 0 : uniform spacing
  private final int gap;

  protected AbstractHoneycombLayout(int gap) {
    this.gap = gap;
  }

  protected final int getGap() {
    return gap;
  }

  // Calculate the hexagon bounding box size that fits in the given area
  protected abstract Dimension getButtonSize(int width, int height);

  // Place all components in the given area using the given hexagon size
  protected abstract void layoutHexagons(
      Container parent, Rectangle area, Dimension buttonSize);

  @Override public void layoutContainer(Container parent) {
    if (parent.getComponentCount() > 0 && parent instanceof JComponent) {
      Rectangle area = SwingUtilities.calculateInnerArea((JComponent) parent, null);
      layoutHexagons(parent, area, getButtonSize(area.width, area.height));
      // Insets insets = parent.getInsets();
      // int w = parent.getWidth() - insets.left - insets.right;
      // int h = parent.getHeight() - insets.top - insets.bottom;
      // Rectangle area = new Rectangle(insets.left, insets.top, w, h);
      // layoutHexagons(parent, area, getButtonSize(w, h));
    }
  }

  @Override public Dimension preferredLayoutSize(Container parent) {
    return new Dimension(500, 400);
  }

  @Override public Dimension minimumLayoutSize(Container parent) {
    return new Dimension(200, 150);
  }

  @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
  @Override public void addLayoutComponent(String name, Component comp) {
    // not needed
  }

  @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
  @Override public void removeLayoutComponent(Component comp) {
    // not needed
  }
}

// Pointy-topped hexagon button layout manager
// Row pattern
// Even rows (0, 2, ...): 2n-1 buttons, offset right by half cell width
// Odd  rows (1, 3, ...): 2n buttons, flush left
class HoneycombRowsLayout extends AbstractHoneycombLayout {
  private final int rows;
  private final int evenCols; // Button count for even rows (2n-1)
  private final int oddCols; // Button count for odd rows (2n)

  protected HoneycombRowsLayout(int rows, int evenCols, int oddCols, int gap) {
    super(gap);
    this.rows = rows;
    this.evenCols = evenCols;
    this.oddCols = oddCols;
  }

  @Override protected void layoutHexagons(
      Container parent, Rectangle area, Dimension buttonSize) {
    int gap = getGap();
    int slotW = buttonSize.width + gap; // Horizontal pitch
    int slotH = buttonSize.height + gap; // Vertical base

    // Center the grid inside the panel
    int gridW = oddCols * slotW;
    int gridH = (int) (slotH * (.25 + .75 * rows));
    int marginX = area.x + (area.width - gridW) / 2;
    int marginY = area.y + (area.height - gridH) / 2;

    int compIdx = 0;
    for (int r = 0; r < rows; r++) {
      boolean isEvenRow = r % 2 == 0;
      int colsInRow = isEvenRow ? evenCols : oddCols;

      // Y position: step by 75% of slot height
      int y = marginY + (int) (r * slotH * .75 + gap / 2d);
      // Even rows shift right by half a slot
      int rowOffsetX = isEvenRow ? slotW / 2 : 0;

      for (int col = 0; col < colsInRow; col++) {
        if (compIdx >= parent.getComponentCount()) {
          break;
        }
        Component c = parent.getComponent(compIdx);
        int x = marginX + rowOffsetX + col * slotW + gap / 2;
        c.setBounds(x, y, buttonSize.width, buttonSize.height); // Set position and size
        compIdx += 1;
      }
    }
  }

  @Override protected Dimension getButtonSize(int width, int height) {
    int gap = getGap();
    // Derive cellW,cellH from horizontal constraint
    double cwFromWidth = (double) width / oddCols - gap;
    double chFromWidth = cwFromWidth / RATIO;

    // Derive cellW,cellH from vertical constraint
    double chFromHeight = height / (.25 + .75 * rows) - gap;
    double cwFromHeight = chFromHeight * RATIO;

    // Adopt the smaller to satisfy both constraints
    double cellW;
    double cellH;
    if (cwFromWidth <= cwFromHeight) {
      cellW = cwFromWidth;
      cellH = chFromWidth;
    } else {
      cellW = cwFromHeight;
      cellH = chFromHeight;
    }
    return new Dimension(Math.max(1, (int) cellW), Math.max(1, (int) cellH));
  }
}

// Flat-topped hexagon button layout manager
// Flower pattern: 1 hexagon in the center and 6 hexagons around it
class HoneycombFlowerLayout extends AbstractHoneycombLayout {
  // Hexagon widths that fit horizontally: 1 + 2 * .75 = 2.5
  private static final double COLUMNS = 2.5;
  // Hexagon heights that fit vertically: 3
  private static final double LINES = 3d;

  protected HoneycombFlowerLayout(int gap) {
    super(gap);
  }

  @Override protected void layoutHexagons(
      Container parent, Rectangle area, Dimension buttonSize) {
    int gap = getGap();
    int w = buttonSize.width;
    int h = buttonSize.height;
    // Neighbor center offsets: 75% of the width, 50% of the height
    double dx = w * .75 + gap * RATIO;
    double dy = h * .5 + gap * .5;
    double slotH = h + gap;

    int centerX = area.x + area.width / 2;
    int centerY = area.y + area.height / 2;

    double[][] positions = {
        {0d, 0d},
        {0d, -slotH},
        {dx, -dy},
        {dx, dy},
        {0d, slotH},
        {-dx, dy},
        {-dx, -dy},
    };

    int min = Math.min(parent.getComponentCount(), positions.length);
    for (int i = 0; i < min; i++) {
      Component c = parent.getComponent(i);
      int cx = (int) Math.round(centerX + positions[i][0]);
      int cy = (int) Math.round(centerY + positions[i][1]);
      c.setBounds(cx - w / 2, cy - h / 2, w, h);
    }
  }

  @Override protected Dimension getButtonSize(int width, int height) {
    int gap = getGap();
    double widFromWidth = (width - 2d * gap * RATIO) / COLUMNS;
    double widFromHeight = (height - 2d * gap) / (RATIO * LINES);
    double cellW = Math.min(widFromWidth, widFromHeight);
    double cellH = cellW * RATIO;
    return new Dimension(
        Math.max(1, (int) Math.round(cellW)),
        Math.max(1, (int) Math.round(cellH)));
  }
}
