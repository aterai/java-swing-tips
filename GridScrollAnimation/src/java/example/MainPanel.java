// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    GridPanel gp = new GridPanel(4, 3);
    for (int i = 0; i < gp.cols * gp.rows; i++) {
      gp.add(makeDummyComponent(i));
    }
    JScrollPane scrollPane = new JScrollPane(gp);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    // scrollPane.getVerticalScrollBar().setEnabled(false);
    // scrollPane.getHorizontalScrollBar().setEnabled(false);
    // scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension());
    // scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension());
    JPanel p = new JPanel();
    p.add(scrollPane);
    add(p);
    add(new JButton(new ScrollAction("right", scrollPane, new Point(1, 0))), BorderLayout.EAST);
    add(new JButton(new ScrollAction("left", scrollPane, new Point(-1, 0))), BorderLayout.WEST);
    add(new JButton(new ScrollAction("bottom", scrollPane, new Point(0, 1))), BorderLayout.SOUTH);
    add(new JButton(new ScrollAction("top", scrollPane, new Point(0, -1))), BorderLayout.NORTH);
  }

  private static Component makeDummyComponent(int idx) {
    return idx % 2 == 0 ? new JButton("button" + idx) : new JScrollPane(new JTree());
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
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
  public final int rows;
  public final int cols;
  public final Dimension size;

  protected GridPanel(int rows, int cols) {
    super(new GridLayout(rows, cols, 0, 0));
    // putClientProperty("JScrollBar.fastWheelScrolling", Boolean.FALSE);
    this.rows = rows;
    this.cols = cols;
    this.size = new Dimension(160 * cols, 120 * rows);
  }

  @Override public Dimension getPreferredScrollableViewportSize() {
    Dimension d = getPreferredSize();
    return new Dimension(d.width / cols, d.height / rows);
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

  @Override public Dimension getPreferredSize() {
    return size;
  }
}

class ScrollAction extends AbstractAction {
  protected static final double SIZE = 100d;
  protected final Point vec;
  protected final JScrollPane scrollPane;
  protected final Timer scroller = new Timer(5, null);
  protected transient ActionListener listener;
  protected int count;

  protected ScrollAction(String name, JScrollPane scrollPane, Point vec) {
    super(name);
    this.scrollPane = scrollPane;
    this.vec = vec;
  }

  @Override public void actionPerformed(ActionEvent e) {
    start();
  }

  protected void start() {
    if (scroller.isRunning()) {
      return;
    }
    JViewport vport = scrollPane.getViewport();
    JComponent v = (JComponent) vport.getView();
    int w = vport.getWidth();
    int h = vport.getHeight();
    int sx = vport.getViewPosition().x;
    int sy = vport.getViewPosition().y;
    Rectangle rect = new Rectangle(w, h);
    scroller.removeActionListener(listener);
    count = (int) SIZE;
    listener = e -> {
      double a = easeInOut(--count / SIZE);
      int dx = (int) (w - a * w + .5);
      int dy = (int) (h - a * h + .5);
      if (count <= 0) {
        dx = w;
        dy = h;
        scroller.stop();
      }
      rect.setLocation(sx + vec.x * dx, sy + vec.y * dy);
      v.scrollRectToVisible(rect);
    };
    scroller.addActionListener(listener);
    scroller.start();
  }

  protected static double easeInOut(double t) {
    // range: 0.0 <= t <= 1.0
    boolean isFirstHalf = t < .5;
    if (isFirstHalf) {
      return .5 * pow3(t * 2d);
    } else {
      return .5 * (pow3(t * 2d - 2d) + 2d);
    }
  }

  protected static double pow3(double a) {
    // return Math.pow(a, 3d);
    return a * a * a;
  }
}
