package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private transient ZoomAndPanHandler zoomAndPanHandler;
    private transient Image img;
    public MainPanel() {
        super(new BorderLayout());
        try {
            img = ImageIO.read(getClass().getResource("CRW_3857_JFR.jpg"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setPreferredSize(new Dimension(320, 240));
    }
    @Override public void updateUI() {
        removeMouseListener(zoomAndPanHandler);
        removeMouseMotionListener(zoomAndPanHandler);
        removeMouseWheelListener(zoomAndPanHandler);
        super.updateUI();
        if (zoomAndPanHandler == null) {
            zoomAndPanHandler = new ZoomAndPanHandler();
        }
        addMouseListener(zoomAndPanHandler);
        addMouseMotionListener(zoomAndPanHandler);
        addMouseWheelListener(zoomAndPanHandler);
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setTransform(zoomAndPanHandler.getCoordTransform());
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class ZoomAndPanHandler extends MouseAdapter {
    private static final double ZOOM_MULTIPLICATION_FACTOR = 1.2;
    private static final int MIN_ZOOM = -10;
    private static final int MAX_ZOOM = 10;
    private static final int EXTENT = 1;
    private final BoundedRangeModel zoomRange = new DefaultBoundedRangeModel(0, EXTENT, MIN_ZOOM, MAX_ZOOM + EXTENT);
    private final AffineTransform coordTransform = new AffineTransform();
    private final Point dragStartPoint = new Point();
    @Override public void mousePressed(MouseEvent e) {
        dragStartPoint.setLocation(e.getPoint());
    }
    @Override public void mouseDragged(MouseEvent e) {
        Point dragEndPoint = e.getPoint();
        Point dragStart = transformPoint(dragStartPoint);
        Point dragEnd   = transformPoint(dragEndPoint);
        coordTransform.translate(dragEnd.x - dragStart.x, dragEnd.y - dragStart.y);
        dragStartPoint.setLocation(dragEndPoint);
        e.getComponent().repaint();
    }
    @Override public void mouseWheelMoved(MouseWheelEvent e) {
        int dir = e.getWheelRotation();
        int z = zoomRange.getValue();
        zoomRange.setValue(z + EXTENT * (dir > 0 ? -1 : 1));
        if (z == zoomRange.getValue()) {
            return;
        }
        Component c = e.getComponent();
        Rectangle r = c.getBounds();
        //Point p = e.getPoint();
        Point p = new Point(r.x + r.width / 2, r.y + r.height / 2);
        Point p1 = transformPoint(p);
        double scale = dir > 0 ? 1 / ZOOM_MULTIPLICATION_FACTOR : ZOOM_MULTIPLICATION_FACTOR;
        coordTransform.scale(scale, scale);
        Point p2 = transformPoint(p);
        coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
        c.repaint();
    }
    //https://forums.oracle.com/thread/1263955
    //How to implement Zoom & Pan in Java using Graphics2D
    private Point transformPoint(Point p1) {
        Point p2 = new Point();
        try {
            AffineTransform inverse = coordTransform.createInverse();
            inverse.transform(p1, p2);
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
        return p2;
    }
    public AffineTransform getCoordTransform() {
        return coordTransform;
    }
}
