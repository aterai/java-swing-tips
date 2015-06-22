package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        try {
            Image img = ImageIO.read(getClass().getResource("CRW_3857_JFR.jpg"));
            add(new JScrollPane(new ZoomAndPanePanel(img)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class ZoomAndPanePanel extends JPanel {
    private final AffineTransform coordTransform = new AffineTransform();
    private final transient Image img;
    private final Rectangle imgrect;
    private transient ZoomAndPanHandler handler;

    public ZoomAndPanePanel(Image img) {
        super();
        this.img = img;
        this.imgrect = new Rectangle(img.getWidth(null), img.getHeight(null));
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new Color(0x55FF0000, true));

        //AffineTransform at = g2.getTransform();
        //at.concatenate(coordTransform);
        //g2.setTransform(at);
        //g2.drawImage(img, 0, 0, this);
        //g2.fill(new Rectangle(500, 140, 150, 150));

        //g2.drawRenderedImage((java.awt.image.RenderedImage) img, coordTransform);
//         g2.drawImage(img, coordTransform, this);
//         g2.fill(coordTransform.createTransformedShape(new Rectangle(500, 140, 150, 150)));

        g2.setTransform(coordTransform);
        g2.drawImage(img, 0, 0, this);

        
        g2.dispose();
    }
    @Override public Dimension getPreferredSize() {
        Rectangle r = coordTransform.createTransformedShape(imgrect).getBounds();
        return new Dimension(r.width, r.height);
    }
    @Override public void updateUI() {
        removeMouseListener(handler);
        removeMouseMotionListener(handler);
        removeMouseWheelListener(handler);
        super.updateUI();
        handler = new ZoomAndPanHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
        addMouseWheelListener(handler);
    }

    protected class ZoomAndPanHandler extends MouseAdapter {
        private static final int MIN_ZOOM = -9;
        private static final int MAX_ZOOM = 16;
        private static final int EXTENT = 1;
        private final BoundedRangeModel zoomRange = new DefaultBoundedRangeModel(0, EXTENT, MIN_ZOOM, MAX_ZOOM + EXTENT);
        private final Point dragStartPoint = new Point();
        @Override public void mousePressed(MouseEvent e) {
            dragStartPoint.setLocation(e.getPoint());
        }
        @Override public void mouseDragged(MouseEvent e) {
            Point dragEndPoint = e.getPoint();
            Point dragStart = inversedPoint(dragStartPoint);
            Point dragEnd   = inversedPoint(dragEndPoint);
            coordTransform.translate(dragEnd.x - dragStart.x, dragEnd.y - dragStart.y);
            dragStartPoint.setLocation(dragEndPoint);
            e.getComponent().repaint();
        }
        @Override public void mouseWheelMoved(MouseWheelEvent e) {
            int dir = e.getWheelRotation();
            int z = zoomRange.getValue();
            zoomRange.setValue(z + EXTENT * (dir > 0 ? -1 : 1));
            if (z != zoomRange.getValue()) {
                Container c = SwingUtilities.getAncestorOfClass(JViewport.class, e.getComponent());
                if (c instanceof JViewport) {
                    Rectangle r = ((JViewport) c).getBounds();
                    Point p = new Point(r.x + r.width / 2, r.y + r.height / 2);
                    Point p1 = inversedPoint(p);

                    double s = 1d + zoomRange.getValue() * .1;
                    coordTransform.setToScale(s, s);

                    Point p2 = inversedPoint(p);
                    coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());

                    c.revalidate();
                    c.repaint();
                }
            }
        }
        //https://community.oracle.com/thread/1263955
        //How to implement Zoom & Pan in Java using Graphics2D
        private Point inversedPoint(Point p1) {
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
}
