// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final List<Vertex> cube = new ArrayList<>(8);

  private MainPanel() {
    super(new BorderLayout());
    double sideLength = 100;
    cube.add(new Vertex(sideLength, sideLength, sideLength));
    cube.add(new Vertex(sideLength, sideLength, -sideLength));
    cube.add(new Vertex(-sideLength, sideLength, -sideLength));
    cube.add(new Vertex(-sideLength, sideLength, sideLength));
    cube.add(new Vertex(sideLength, -sideLength, sideLength));
    cube.add(new Vertex(sideLength, -sideLength, -sideLength));
    cube.add(new Vertex(-sideLength, -sideLength, -sideLength));
    cube.add(new Vertex(-sideLength, -sideLength, sideLength));

    MouseAdapter handler = new DragRotateHandler();
    addMouseListener(handler);
    addMouseMotionListener(handler);

    setPreferredSize(new Dimension(320, 240));
  }

  private final class DragRotateHandler extends MouseAdapter {
    private final Cursor defCursor = Cursor.getDefaultCursor();
    private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final Point pp = new Point();

    @Override public void mouseDragged(MouseEvent e) {
      Point pt = e.getPoint();
      double rotY = (pt.x - pp.x) * .03;
      double rotX = (pt.y - pp.y) * .03;
      double rotZ = 0d;
      for (Vertex v : cube) {
        v.rotateTransformation(rotX, rotY, rotZ);
      }
      pp.setLocation(pt);
      e.getComponent().repaint();
    }

    @Override public void mousePressed(MouseEvent e) {
      e.getComponent().setCursor(hndCursor);
      pp.setLocation(e.getPoint());
    }

    @Override public void mouseReleased(MouseEvent e) {
      e.getComponent().setCursor(defCursor);
    }
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Path2D path = new Path2D.Double();
    path.moveTo(cube.get(0).vx, cube.get(0).vy);
    path.lineTo(cube.get(1).vx, cube.get(1).vy);
    path.lineTo(cube.get(2).vx, cube.get(2).vy);
    path.lineTo(cube.get(3).vx, cube.get(3).vy);
    path.lineTo(cube.get(0).vx, cube.get(0).vy);
    path.lineTo(cube.get(4).vx, cube.get(4).vy);
    path.lineTo(cube.get(5).vx, cube.get(5).vy);
    path.lineTo(cube.get(6).vx, cube.get(6).vy);
    path.lineTo(cube.get(7).vx, cube.get(7).vy);
    path.lineTo(cube.get(4).vx, cube.get(4).vy);
    path.moveTo(cube.get(1).vx, cube.get(1).vy);
    path.lineTo(cube.get(5).vx, cube.get(5).vy);
    path.moveTo(cube.get(2).vx, cube.get(2).vy);
    path.lineTo(cube.get(6).vx, cube.get(6).vy);
    path.moveTo(cube.get(3).vx, cube.get(3).vy);
    path.lineTo(cube.get(7).vx, cube.get(7).vy);
    Rectangle r = SwingUtilities.calculateInnerArea(this, null);
    g2.setPaint(Color.WHITE);
    g2.fill(r);
    g2.translate(r.getCenterX(), r.getCenterY());
    g2.setPaint(Color.BLACK);
    g2.draw(path);
    g2.dispose();
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

class Vertex {
  public double vx;
  public double vy;
  private double dx;
  private double dy;
  private double dz;

  protected Vertex(double dx, double dy, double dz) {
    this.dx = dx;
    this.dy = dy;
    this.dz = dz;
    projectionTransformation();
  }

  private void projectionTransformation() {
    double screenDistance = 500d;
    double depth = 1000d;
    double gz = dz + depth;
    this.vx = screenDistance * dx / gz;
    this.vy = screenDistance * dy / gz;
  }

  public void rotateTransformation(double kx, double ky, double kz) {
    double x0 = dx * Math.cos(ky) - dz * Math.sin(ky);
    double y0 = dy;
    double z0 = dx * Math.sin(ky) + dz * Math.cos(ky);
    double y1 = y0 * Math.cos(kx) - z0 * Math.sin(kx);
    double z1 = y0 * Math.sin(kx) + z0 * Math.cos(kx);
    this.dx = x0 * Math.cos(kz) - y1 * Math.sin(kz);
    this.dy = x0 * Math.sin(kz) + y1 * Math.cos(kz);
    this.dz = z1;
    projectionTransformation();
  }
}
