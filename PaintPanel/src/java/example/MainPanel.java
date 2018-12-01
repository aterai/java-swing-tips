package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        add(new PaintPanel());
        setPreferredSize(new Dimension(320, 240));
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
//     private final Point startPoint = new Point();
//     private final Point p = new Point();
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
//         // super.paintComponent(g);
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
//     private final Point startPoint = new Point();
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
//                 // repaint();
//                 Rectangle r = getRepaintRectangle(startPoint, p);
//                 repaint(r.x - 2, r.y - 2, r.width + 2 + 2, r.height + 2 + 2); // (3.0 / 2) = 1.5 < 2
//                 startPoint.setLocation(p);
//             }
//             @Override public void mousePressed(MouseEvent e) {
//                 startPoint.setLocation(e.getPoint());
//                 // if (Objects.isNull(offImage)) { // resized
//                 //     offImage = (BufferedImage) createImage(getWidth(), getHeight());
//                 // }
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
//         polygon.addPoint(srcPoint.x, srcPoint.y);
//         polygon.addPoint(destPoint.x, srcPoint.y);
//         polygon.addPoint(destPoint.x, destPoint.y);
//         polygon.addPoint(srcPoint.x, destPoint.y);
//         return polygon.getBounds();
//     }
// }

class PaintPanel extends JPanel {
    private static final Stroke STROKE = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private transient MouseInputListener handler;
    private transient List<Shape> list;

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
        handler = new MouseInputAdapter() {
            private transient Path2D path = new Path2D.Double();
            @Override public void mousePressed(MouseEvent e) {
                path.moveTo(e.getX(), e.getY());
                getList().add(path);
                repaint();
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (Objects.nonNull(path)) {
                    path.lineTo(e.getX(), e.getY());
                    repaint();
                }
            }
        };
        addMouseMotionListener(handler);
        addMouseListener(handler);
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(Color.BLACK);
        g2.setStroke(STROKE);
        getList().forEach(g2::draw);
        g2.dispose();
    }
}
