// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    GridPanel grid = new GridPanel(4, 3, new Dimension(160, 120));
    for (int i = 0; i < grid.getRows() * grid.getColumns(); i++) {
      grid.add(createSampleComponent(i));
    }
    JScrollPane scroll = new JScrollPane(grid);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    JPanel p = new JPanel();
    p.add(scroll);
    add(p);
    // All buttons share one animator so that only one animation runs at a time
    GridScrollAnimator animator = new GridScrollAnimator(scroll.getViewport());
    add(createScrollButton("right", animator, 1, 0), BorderLayout.EAST);
    add(createScrollButton("left", animator, -1, 0), BorderLayout.WEST);
    add(createScrollButton("bottom", animator, 0, 1), BorderLayout.SOUTH);
    add(createScrollButton("top", animator, 0, -1), BorderLayout.NORTH);
  }

  private static Component createSampleComponent(int idx) {
    return idx % 2 == 0 ? new JButton("button" + idx) : new JScrollPane(new JTree());
  }

  private static JButton createScrollButton(String title, GridScrollAnimator a, int dx, int dy) {
    JButton button = new JButton(title);
    button.addActionListener(e -> a.scrollBy(dx, dy));
    return button;
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
    frame.setSize(320, 240);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class GridPanel extends JPanel implements Scrollable {
  private final Dimension cellSize;

  protected GridPanel(int rows, int cols, Dimension cellSize) {
    super(new GridLayout(rows, cols, 0, 0));
    this.cellSize = new Dimension(cellSize);
  }

  public int getRows() {
    return ((GridLayout) getLayout()).getRows();
  }

  public int getColumns() {
    return ((GridLayout) getLayout()).getColumns();
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(cellSize.width * getColumns(), cellSize.height * getRows());
  }

  @Override public Dimension getPreferredScrollableViewportSize() {
    // Show one cell of the grid at a time
    return new Dimension(cellSize);
  }

  @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return orientation == SwingConstants.HORIZONTAL ? visibleRect.width : visibleRect.height;
  }

  @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return orientation == SwingConstants.HORIZONTAL ? visibleRect.width : visibleRect.height;
  }

  @Override public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  @Override public boolean getScrollableTracksViewportHeight() {
    return false;
  }
}

class GridScrollAnimator {
  private static final int STEPS = 32;
  private final Timer timer = new Timer(5, e -> step());
  private final JViewport viewport;
  private final Point start = new Point();
  private final Point end = new Point();
  private int count;

  protected GridScrollAnimator(JViewport viewport) {
    this.viewport = viewport;
  }

  public void scrollBy(int dx, int dy) {
    if (timer.isRunning() || viewport.getView() == null) {
      return;
    }
    Dimension extent = viewport.getExtentSize();
    Dimension viewSize = viewport.getViewSize();
    start.setLocation(viewport.getViewPosition());
    end.setLocation(
        clamp(start.x + dx * extent.width, viewSize.width - extent.width),
        clamp(start.y + dy * extent.height, viewSize.height - extent.height));
    if (!end.equals(start)) {
      count = 0;
      timer.start();
    }
  }

  private void step() {
    count++;
    double a = easeInOut(count / (double) STEPS);
    if (count >= STEPS) {
      a = 1d;
      timer.stop();
    }
    int x = start.x + (int) Math.round(a * (end.x - start.x));
    int y = start.y + (int) Math.round(a * (end.y - start.y));
    viewport.setViewPosition(new Point(x, y));
  }

  private static int clamp(int value, int max) {
    // Java 21: return Math.clamp(value, 0, max);
    return Math.min(Math.max(value, 0), max);
  }

  public static double easeInOut(double t) {
    // range: 0.0 <= t <= 1.0
    boolean isFirstHalf = t < .5;
    return isFirstHalf ? .5 * pow3(t * 2d) : .5 * (pow3(t * 2d - 2d) + 2d);
  }

  private static double pow3(double a) {
    return a * a * a;
  }
}
