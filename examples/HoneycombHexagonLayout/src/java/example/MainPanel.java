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
  // Gap between adjacent hexagon edges in pixels
  // 0 = perfectly touching, positive = gap
  private static final int BTN_GAP = 6;
  private static final Color BTN_BGC = new Color(70, 130, 180); // Steel blue

  private MainPanel() {
    super(new BorderLayout());

    int evenCount = 2 * N - 1; // Buttons in even rows
    int oddCount = 2 * N; // Buttons in odd  rows
    JPanel p = new JPanel(new HoneycombLayout(TOTAL_ROWS, evenCount, oddCount, BTN_GAP));
    p.setBackground(new Color(45, 45, 45));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Calculate total button count and add them
    int totalButtons = 0;
    for (int r = 0; r < TOTAL_ROWS; r++) {
      int count = r % 2 == 0 ? evenCount : oddCount;
      totalButtons += count;
    }

    for (int i = 0; i < totalButtons; i++) {
      p.add(createHexagonButton(i));
    }
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static HexagonButton createHexagonButton(int i) {
    HexagonButton btn = new HexagonButton("ID: " + i);
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

// Hexagon button component
class HexagonButton extends JButton {
  private Polygon hexagon;
  private boolean isHovered;
  private transient MouseListener hoverHandler;

  protected HexagonButton(String text) {
    super(text);
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
  // For a pointy-top regular hexagon, the bounding box satisfies:
  // W = R * sqrt(3), H = R * 2 -> W < H (always)
  // Therefore the circumradius R equals H/2 (= cy), NOT W/2 (= cx).
  // Using Math.min(cx, cy) would pick cx = W/2 < R, shrinking the hexagon
  // and leaving gaps on all sides.
  // Using Math.max(cx, cy) correctly picks cy = R, filling the bounds.
  private void calculateHexagon() {
    int cx = getWidth() / 2;
    int cy = getHeight() / 2;
    // int radius = Math.min(cx, cy);
    int radius = Math.max(cx, cy);
    hexagon = new Polygon();
    for (int i = 0; i < 6; i++) {
      // Start at -PI/2 (12 o'clock), step by 60°(PI/3)
      double angle = -Math.PI / 2 + i * Math.PI / 3;
      hexagon.addPoint(
          (int) (cx + radius * Math.cos(angle)),
          (int) (cy + radius * Math.sin(angle)));
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

// Honeycomb hexagon button layout manager
// Row pattern
// Even rows (0, 2, ...): 2n-1 buttons, offset right by half cell width
// Odd  rows (1, 3, ...): 2n buttons, flush left
class HoneycombLayout implements LayoutManager {
  private static final double RATIO = Math.sqrt(3d) / 2d;
  private final int rows;
  private final int evenCols; // Button count for even rows (2n-1)
  private final int oddCols; // Button count for odd rows (2n)
  // Visual gap between adjacent hexagon edges, in pixels.
  // gap = 0 : edges touch perfectly
  // gap > 0 : uniform spacing
  private final int gap;

  protected HoneycombLayout(int rows, int evenCols, int oddCols, int gap) {
    this.rows = rows;
    this.evenCols = evenCols;
    this.oddCols = oddCols;
    this.gap = gap;
  }

  @Override public void layoutContainer(Container parent) {
    Insets insets = parent.getInsets();
    int maxWidth = parent.getWidth() - insets.left - insets.right;
    int maxHeight = parent.getHeight() - insets.top - insets.bottom;

    Dimension buttonSize = getButtonSize(maxWidth, maxHeight);
    int slotW = buttonSize.width + gap; // Horizontal pitch
    int slotH = buttonSize.height + gap; // Vertical base

    // Center the grid inside the panel
    int gridW = oddCols * slotW;
    int gridH = (int) (slotH * (.25 + .75 * rows));
    int marginX = insets.left + (maxWidth - gridW) / 2;
    int marginY = insets.top + (maxHeight - gridH) / 2;

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

  private Dimension getButtonSize(int maxWidth, int maxHeight) {
    // Derive cellW,cellH from horizontal constraint
    double cwFromWidth = (double) maxWidth / oddCols - gap;
    double chFromWidth = cwFromWidth / RATIO;

    // Derive cellW,cellH from vertical constraint
    double chFromHeight = maxHeight / (.25 + .75 * rows) - gap;
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
    int buttonW = Math.max(1, (int) cellW);
    int buttonH = Math.max(1, (int) cellH);
    return new Dimension(buttonW, buttonH);
  }

  @Override public Dimension preferredLayoutSize(Container parent) {
    return new Dimension(500, 400);
  }

  @Override public Dimension minimumLayoutSize(Container parent) {
    return new Dimension(200, 150);
  }

  @Override public void addLayoutComponent(String name, Component comp) {
    // not needed
  }

  @Override public void removeLayoutComponent(Component comp) {
    // not needed
  }
}
