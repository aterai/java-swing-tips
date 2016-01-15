package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
//import java.awt.image.*;
import java.util.*;
import java.util.List;
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

// class PaintPanel extends JPanel {
//     private final Point startPoint = new Point(-10, 0);
//     private final Point p = new Point(-10, 0);
//     private MouseAdapter handler;
//     @Override public void updateUI() {
//         removeMouseMotionListener(handler);
//         removeMouseListener(handler);
//         super.updateUI();
//         handler = new MouseAdapter() {
//             @Override public void mouseDragged(MouseEvent e) {
//                 p.setLocation(e.getPoint());
//                 repaint();
//             }
//             @Override public void mousePressed(MouseEvent e) {
//                 startPoint.setLocation(e.getPoint());
//             }
//         };
//         addMouseMotionListener(handler);
//         addMouseListener(handler);
//     }
//     @Override protected void paintComponent(Graphics g) {
//         //super.paintComponent(g);
//         Graphics2D g2 = (Graphics2D) g.create();
//         g2.setStroke(new BasicStroke(3f));
//         g2.setPaint(Color.BLACK);
//         g2.drawLine(startPoint.x, startPoint.y, p.x, p.y);
//         g2.dispose();
//         startPoint.setLocation(p);
//     }
// }

// class PaintPanel extends JPanel {
//     private final Polygon polygon = new Polygon();
//     private final Point startPoint = new Point(-10, 0);
//     private transient BufferedImage offImage;
//     private transient MouseAdapter handler;
//     @Override public void updateUI() {
//         removeMouseMotionListener(handler);
//         removeMouseListener(handler);
//         super.updateUI();
//         handler = new MouseAdapter() {
//             @Override public void mouseDragged(MouseEvent e) {
//                 Point p = e.getPoint();
//                 Graphics2D g2 = (Graphics2D) offImage.createGraphics();
//                 g2.setStroke(new BasicStroke(3f));
//                 g2.setPaint(Color.BLACK);
//                 g2.drawLine(startPoint.x, startPoint.y, p.x, p.y);
//                 g2.dispose();
//                 //repaint();
//                 Rectangle r = getRepaintRectangle(startPoint, p);
//                 repaint(r.x - 2, r.y - 2, r.width + 2 + 2, r.height + 2 + 2); //(3.0 / 2) = 1.5 < 2
//                 startPoint.setLocation(p);
//             }
//             @Override public void mousePressed(MouseEvent e) {
//                 startPoint.setLocation(e.getPoint());
//                 //if (offImage == null) { //resized
//                 //    offImage = (BufferedImage) createImage(getWidth(), getHeight());
//                 //}
//             }
//         };
//         addMouseMotionListener(handler);
//         addMouseListener(handler);
//         offImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
//     }
//     @Override protected void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         g.drawImage(offImage, 0, 0, this);
//     }
//     private Rectangle getRepaintRectangle(Point srcPoint, Point destPoint) {
//         polygon.reset();
//         polygon.addPoint(srcPoint.x,  srcPoint.y);
//         polygon.addPoint(destPoint.x, srcPoint.y);
//         polygon.addPoint(destPoint.x, destPoint.y);
//         polygon.addPoint(srcPoint.x,  destPoint.y);
//         return polygon.getBounds();
//     }
// }

class PaintPanel extends JPanel {
    private static final Stroke STROKE = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private transient List<Shape> list;
    private transient Path2D path;
    private transient MouseAdapter handler;
    @Override public void updateUI() {
        removeMouseMotionListener(handler);
        removeMouseListener(handler);
        super.updateUI();
        handler = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                path = new Path2D.Double();
                list.add(path);
                Point p = e.getPoint();
                path.moveTo(p.x, p.y);
                repaint();
            }
            @Override public void mouseDragged(MouseEvent e) {
                Point p = e.getPoint();
                path.lineTo(p.x, p.y);
                repaint();
            }
        };
        addMouseMotionListener(handler);
        addMouseListener(handler);
        list = new ArrayList<Shape>();
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (list != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(Color.BLACK);
            g2.setStroke(STROKE);
            for (Shape s: list) {
                g2.draw(s);
            }
            g2.dispose();
        }
    }
}
