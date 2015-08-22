package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
// import java.awt.geom.*;
// import java.awt.image.*;
// import java.util.*;
// import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        add(new PaintPanel());
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
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

class PaintPanel extends JPanel implements MouseMotionListener, MouseListener {
    private Point startPoint = new Point(-10, -10);
    private Point p = new Point(-10, -10);
    public PaintPanel() {
        super();
        addMouseMotionListener(this);
        addMouseListener(this);
    }
    @Override public void paintComponent(Graphics g) {
        //super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setStroke(new BasicStroke(3f));
        g2.setPaint(Color.BLACK);
        g2.drawLine(startPoint.x, startPoint.y, p.x, p.y);
        g2.dispose();
        startPoint = p;
    }
    @Override public void mouseDragged(MouseEvent e) {
        p = e.getPoint();
        repaint();
    }
    @Override public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
    }
    @Override public void mouseMoved(MouseEvent e)    { /* not needed */ }
    @Override public void mouseExited(MouseEvent e)   { /* not needed */ }
    @Override public void mouseEntered(MouseEvent e)  { /* not needed */ }
    @Override public void mouseReleased(MouseEvent e) { /* not needed */ }
    @Override public void mouseClicked(MouseEvent e)  { /* not needed */ }
}

// class PaintPanel2 extends JPanel implements MouseMotionListener, MouseListener {
//     private final Polygon polygon = new Polygon();
//     private Point startPoint = new Point(-1, -1);
//     private final transient BufferedImage offImage;
//     public PaintPanel2() {
//         super();
//         addMouseMotionListener(this);
//         addMouseListener(this);
//         offImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
//     }
//     @Override public void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         g.drawImage(offImage, 0, 0, this);
//     }
//     @Override public void mouseDragged(MouseEvent e) {
//         Point p = e.getPoint();
//         Graphics2D g2 = (Graphics2D) offImage.createGraphics();
//         g2.setStroke(new BasicStroke(3f));
//         g2.setPaint(Color.BLACK);
//         g2.drawLine(startPoint.x, startPoint.y, p.x, p.y);
//         g2.dispose();
//         //repaint();
//         Rectangle r = getRepaintRectangle(startPoint, p);
//         repaint(r.x - 2, r.y - 2, r.width + 2 + 2, r.height + 2 + 2); //(3.0 / 2) = 1.5 < 2
//         startPoint = p;
//     }
//     private Rectangle getRepaintRectangle(Point srcPoint, Point destPoint) {
//         polygon.reset();
//         polygon.addPoint(srcPoint.x,  srcPoint.y);
//         polygon.addPoint(destPoint.x, srcPoint.y);
//         polygon.addPoint(destPoint.x, destPoint.y);
//         polygon.addPoint(srcPoint.x,  destPoint.y);
//         return polygon.getBounds();
//     }
//     @Override public void mousePressed(MouseEvent e) {
//         startPoint = e.getPoint();
// //         if (offImage == null) { //resized
// //             offImage = (BufferedImage) createImage(getWidth(), getHeight());
// //         }
//     }
//     @Override public void mouseMoved(MouseEvent e)    { /* not needed */ }
//     @Override public void mouseExited(MouseEvent e)   { /* not needed */ }
//     @Override public void mouseEntered(MouseEvent e)  { /* not needed */ }
//     @Override public void mouseReleased(MouseEvent e) { /* not needed */ }
//     @Override public void mouseClicked(MouseEvent e)  { /* not needed */ }
// }

// class PaintPanel3 extends JPanel implements MouseMotionListener, MouseListener {
//     private static final Stroke STROKE = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
//     private final List<Shape> list = new ArrayList<>();
//     private Path2D.Double path;
//     public PaintPanel3() {
//         super();
//         addMouseMotionListener(this);
//         addMouseListener(this);
//     }
//     @Override public void mousePressed(MouseEvent e) {
//         path = new Path2D.Double();
//         list.add(path);
//         Point p = e.getPoint();
//         path.moveTo(p.x, p.y);
//         repaint();
//     }
//     @Override public void mouseDragged(MouseEvent e) {
//         Point p = e.getPoint();
//         path.lineTo(p.x, p.y);
//         repaint();
//     }
//     @Override public void paintComponent(Graphics g) {
//         Graphics2D g2 = (Graphics2D) g.create();
//         g2.setPaint(Color.BLACK);
//         g2.setStroke(STROKE);
//         for (Shape s: list) {
//             g2.draw(s);
//         }
//         g2.dispose();
//     }
//     @Override public void mouseMoved(MouseEvent e)    { /* not needed */ }
//     @Override public void mouseExited(MouseEvent e)   { /* not needed */ }
//     @Override public void mouseEntered(MouseEvent e)  { /* not needed */ }
//     @Override public void mouseReleased(MouseEvent e) { /* not needed */ }
//     @Override public void mouseClicked(MouseEvent e)  { /* not needed */ }
// }
