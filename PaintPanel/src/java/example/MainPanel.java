package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
//import java.awt.geom.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(new PaintPanel());
        //add(new PaintPanel3());
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
    private Point startPoint = new Point(-10,-10);
    private Point p = new Point(-10,-10);
    public PaintPanel() {
        super();
        addMouseMotionListener(this);
        addMouseListener(this);
    }
    @Override public void paintComponent(Graphics g) {
        //super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setStroke(new BasicStroke(3.0F));
        g2d.setPaint(Color.BLACK);
        g2d.drawLine(startPoint.x, startPoint.y, p.x, p.y);
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
//     private Point startPoint = new Point(-1,-1);
//     private java.awt.image.BufferedImage offImage = null;
//     public PaintPanel2() {
//         super();
//         addMouseMotionListener(this);
//         addMouseListener(this);
//         offImage = new java.awt.image.BufferedImage(320, 240,
//                        java.awt.image.BufferedImage.TYPE_INT_ARGB);
//     }
//     @Override public void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         if(offImage!=null) { ((Graphics2D)g).drawImage(offImage, 0, 0, this); }
//     }
//     @Override public void mouseDragged(MouseEvent e) {
//         Point p = e.getPoint();
//         Graphics2D g2d = (Graphics2D)offImage.createGraphics();
//         g2d.setStroke(new BasicStroke(3.0F));
//         g2d.setPaint(Color.BLACK);
//         g2d.drawLine(startPoint.x, startPoint.y, p.x, p.y);
//         //repaint();
//         Rectangle r = getRepaintRectangle(startPoint, p);
//         repaint(r.x-2,r.y-2,r.width+2+2,r.height+2+2); //(3.0/2) = 1.5 < 2
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
// //         if(offImage==null) { //resized
// //             offImage = (BufferedImage)createImage(getWidth(), getHeight());
// //         }
//     }
//     @Override public void mouseMoved(MouseEvent e) {}
//     @Override public void mouseExited(MouseEvent e) {}
//     @Override public void mouseEntered(MouseEvent e) {}
//     @Override public void mouseReleased(MouseEvent e) {}
//     @Override public void mouseClicked(MouseEvent e) {}
// }
// class PaintPanel3 extends JPanel implements MouseMotionListener, MouseListener {
//     java.util.ArrayList<Shape> list = new java.util.ArrayList<Shape>();
//     Stroke stroke = new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
//     Path2D.Double path;
//     public PaintPanel3() {
//         super();
//         addMouseMotionListener(this);
//         addMouseListener(this);
//     }
//     @Override public void mousePressed(MouseEvent e) {
//         list.add(path = new Path2D.Double());
//         Point p = e.getPoint();
//         path.moveTo(p.x, p.y);
//         repaint();
//     }
//     @Override public void mouseDragged(MouseEvent e) {
//         Point p = e.getPoint();
//         path.lineTo(p.x, p.y);
//         repaint();
//     }
//     @Override public void paintComponent (Graphics g) {
//         Graphics2D g2d = (Graphics2D)g;
//         g2d.setPaint(Color.BLACK);
//         g2d.setStroke(stroke);
//         for(Shape s:list) g2d.draw(s);
//     }
//     @Override public void mouseMoved(MouseEvent e) {}
//     @Override public void mouseExited(MouseEvent e) {}
//     @Override public void mouseEntered(MouseEvent e) {}
//     @Override public void mouseReleased(MouseEvent e) {}
//     @Override public void mouseClicked(MouseEvent e) {}
// }
