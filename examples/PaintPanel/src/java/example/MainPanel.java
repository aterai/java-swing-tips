// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new PaintPanel());
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
    frame.setResizable(false);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class PaintPanel extends JPanel {
  private static final float STROKE_WIDTH = 3f;
  private static final Stroke STROKE = new BasicStroke(
      STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
  // Half of the stroke width plus a margin for rounding
  private static final int PADDING = (int) Math.ceil(STROKE_WIDTH / 2f) + 1;
  private transient MouseInputListener handler;
  private transient List<Shape> list;

  // updateUI() is called from the JPanel constructor before the field initializers run
  protected List<Shape> getList() {
    if (Objects.isNull(list)) {
      list = new ArrayList<>();
    }
    return list;
  }

  @Override public void updateUI() {
    removeMouseMotionListener(handler);
    removeMouseListener(handler);
    super.updateUI();
    handler = new MouseHandler();
    addMouseMotionListener(handler);
    addMouseListener(handler);
  }

  // Repaints only the area covered by the stroke of the segment from p0 to p1
  private void repaintSegment(Point p0, Point p1) {
    Rectangle r = new Rectangle(p0);
    r.add(p1);
    r.grow(PADDING, PADDING);
    repaint(r);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(Color.BLACK);
    g2.setStroke(STROKE);
    getList().forEach(g2::draw);
    g2.dispose();
  }

  private final class MouseHandler extends MouseInputAdapter {
    private final Point prevPoint = new Point();
    private Path2D path;

    @Override public void mousePressed(MouseEvent e) {
      Point pt = e.getPoint();
      path = new Path2D.Double();
      path.moveTo(pt.x, pt.y);
      // A zero-length segment is needed to draw a dot with a round cap
      path.lineTo(pt.x, pt.y);
      getList().add(path);
      prevPoint.setLocation(pt);
      repaintSegment(pt, pt);
    }

    @Override public void mouseDragged(MouseEvent e) {
      if (path != null) {
        Point pt = e.getPoint();
        path.lineTo(pt.x, pt.y);
        repaintSegment(prevPoint, pt);
        prevPoint.setLocation(pt);
      }
    }
  }
}
