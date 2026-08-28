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
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final transient List<Vertex> cube = new ArrayList<>(8);

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
        v.rotate(rotX, rotY, rotZ);
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
    path.moveTo(cube.get(0).getScreenX(), cube.get(0).getScreenY());
    path.lineTo(cube.get(1).getScreenX(), cube.get(1).getScreenY());
    path.lineTo(cube.get(2).getScreenX(), cube.get(2).getScreenY());
    path.lineTo(cube.get(3).getScreenX(), cube.get(3).getScreenY());
    path.lineTo(cube.get(0).getScreenX(), cube.get(0).getScreenY());
    path.lineTo(cube.get(4).getScreenX(), cube.get(4).getScreenY());
    path.lineTo(cube.get(5).getScreenX(), cube.get(5).getScreenY());
    path.lineTo(cube.get(6).getScreenX(), cube.get(6).getScreenY());
    path.lineTo(cube.get(7).getScreenX(), cube.get(7).getScreenY());
    path.lineTo(cube.get(4).getScreenX(), cube.get(4).getScreenY());
    path.moveTo(cube.get(1).getScreenX(), cube.get(1).getScreenY());
    path.lineTo(cube.get(5).getScreenX(), cube.get(5).getScreenY());
    path.moveTo(cube.get(2).getScreenX(), cube.get(2).getScreenY());
    path.lineTo(cube.get(6).getScreenX(), cube.get(6).getScreenY());
    path.moveTo(cube.get(3).getScreenX(), cube.get(3).getScreenY());
    path.lineTo(cube.get(7).getScreenX(), cube.get(7).getScreenY());
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

class Vertex {
  private double worldX;
  private double worldY;
  private double worldZ;
  private double screenX;
  private double screenY;

  protected Vertex(double worldX, double worldY, double worldZ) {
    this.worldX = worldX;
    this.worldY = worldY;
    this.worldZ = worldZ;
    applyProjection();
  }

  public double getScreenX() {
    return screenX;
  }

  public double getScreenY() {
    return screenY;
  }

  private void applyProjection() {
    double screenDistance = 500d;
    double depth = 1000d;
    double distanceZ = worldZ + depth;
    this.screenX = screenDistance * worldX / distanceZ;
    this.screenY = screenDistance * worldY / distanceZ;
  }

  public void rotate(double angleX, double angleY, double angleZ) {
    // yaw: rotation around the y-axis
    double yawX = worldX * Math.cos(angleY) - worldZ * Math.sin(angleY);
    double yawZ = worldX * Math.sin(angleY) + worldZ * Math.cos(angleY);
    // pitch: rotation around the x-axis
    double pitchY = worldY * Math.cos(angleX) - yawZ * Math.sin(angleX);
    double pitchZ = worldY * Math.sin(angleX) + yawZ * Math.cos(angleX);
    // roll: rotation around the z-axis
    this.worldX = yawX * Math.cos(angleZ) - pitchY * Math.sin(angleZ);
    this.worldY = yawX * Math.sin(angleZ) + pitchY * Math.cos(angleZ);
    this.worldZ = pitchZ;
    applyProjection();
  }
}
