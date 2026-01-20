// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    GridPanel gp = new GridPanel(4, 3);
    for (int i = 0; i < gp.getColumns() * gp.getRows(); i++) {
      gp.add(makeSampleComponent(i));
    }
    JScrollPane scroll = new JScrollPane(gp);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    // scroll.getVerticalScrollBar().setEnabled(false);
    // scroll.getHorizontalScrollBar().setEnabled(false);
    // scroll.getVerticalScrollBar().setPreferredSize(new Dimension());
    // scroll.getHorizontalScrollBar().setPreferredSize(new Dimension());
    JPanel p = new JPanel();
    p.add(scroll);
    add(p);
    add(new JButton(new ScrollAction("right", scroll, new Point(1, 0))), BorderLayout.EAST);
    add(new JButton(new ScrollAction("left", scroll, new Point(-1, 0))), BorderLayout.WEST);
    add(new JButton(new ScrollAction("bottom", scroll, new Point(0, 1))), BorderLayout.SOUTH);
    add(new JButton(new ScrollAction("top", scroll, new Point(0, -1))), BorderLayout.NORTH);
  }

  private static Component makeSampleComponent(int idx) {
    return idx % 2 == 0 ? new JButton("button" + idx) : new JScrollPane(new JTree());
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
  private final Dimension size;

  protected GridPanel(int rows, int cols) {
    super(new GridLayout(rows, cols, 0, 0));
    // putClientProperty("JScrollBar.fastWheelScrolling", Boolean.FALSE);
    this.size = new Dimension(160 * cols, 120 * rows);
  }

  public int getRows() {
    return ((GridLayout) getLayout()).getRows();
  }

  public int getColumns() {
    return ((GridLayout) getLayout()).getColumns();
  }

  @Override public Dimension getPreferredScrollableViewportSize() {
    Dimension d = getPreferredSize();
    return new Dimension(d.width / getColumns(), d.height / getRows());
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
  private static final double SIZE = 32d;
  private final Point vec;
  private final JScrollPane scrollPane;
  private final Timer scroller = new Timer(5, null);
  private transient ActionListener listener;

  protected ScrollAction(String name, JScrollPane scrollPane, Point vec) {
    super(name);
    this.scrollPane = scrollPane;
    this.vec = vec;
  }

  @Override public void actionPerformed(ActionEvent e) {
    start();
  }

  protected void start() {
    if (!scroller.isRunning()) {
      JViewport viewport = scrollPane.getViewport();
      JComponent v = (JComponent) viewport.getView();
      int w = viewport.getWidth();
      int h = viewport.getHeight();
      int sx = viewport.getViewPosition().x;
      int sy = viewport.getViewPosition().y;
      Rectangle rect = new Rectangle(w, h);
      scroller.removeActionListener(listener);
      AtomicInteger counter = new AtomicInteger((int) SIZE);
      listener = e -> {
        double a = easeInOut(counter.getAndDecrement() / SIZE);
        Point d = new Point((int) (w - a * w + .5), (int) (h - a * h + .5));
        if (counter.get() <= 0) {
          d.setLocation(w, h);
          scroller.stop();
        }
        rect.setLocation(sx + vec.x * d.x, sy + vec.y * d.y);
        v.scrollRectToVisible(rect);
      };
      scroller.addActionListener(listener);
      scroller.start();
    }
  }

  protected static double easeInOut(double t) {
    // range: 0.0 <= t <= 1.0
    boolean isFirstHalf = t < .5;
    return isFirstHalf ? .5 * pow3(t * 2d) : .5 * (pow3(t * 2d - 2d) + 2d);
  }

  protected static double pow3(double a) {
    // return Math.pow(a, 3d);
    return a * a * a;
  }
}
