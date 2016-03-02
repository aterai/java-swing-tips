package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final transient DraggableImageMouseListener di;
    public MainPanel() {
        super();
        di = new DraggableImageMouseListener(new ImageIcon(getClass().getResource("test.png")));
        addMouseListener(di);
        addMouseMotionListener(di);
        setPreferredSize(new Dimension(320, 240));
    }
    @Override protected void paintComponent(Graphics g) {
        //super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new GradientPaint(50, 0, new Color(200, 200, 200), getWidth(), getHeight(), new Color(100, 100, 100), true));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        di.paint(g, this);
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

class DraggableImageMouseListener extends MouseAdapter {
    private static final BasicStroke BORDER_STROKE = new BasicStroke(4f);
    private static final Color BORDER_COLOR = Color.WHITE;
    private static final Color HOVER_COLOR  = new Color(100, 255, 200, 100);
    private static final int IR = 40;
    private static final int OR = IR * 3;
    private final RoundRectangle2D.Double border;
    private final Ellipse2D.Double inner = new Ellipse2D.Double(0, 0, IR, IR);
    private final Ellipse2D.Double outer = new Ellipse2D.Double(0, 0, OR, OR);
    private final Point2D.Double imagePt = new Point2D.Double(10d, 50d);
    private final Point2D.Double startPt = new Point2D.Double(); //drag start point
    private final Point2D.Double centerPt = new Point2D.Double();
    private final Image image;
    private double radian = 45d * (Math.PI / 180d);
    private double startRadian; //drag start radian
    private boolean moverHover;
    private boolean rotatorHover;

    protected DraggableImageMouseListener(ImageIcon ii) {
        super();
        image = ii.getImage();
        int width  = ii.getIconWidth();
        int height = ii.getIconHeight();
        centerPt.x = width / 2d;
        centerPt.y = height / 2d;
        inner.x = imagePt.x + centerPt.x - IR / 2;
        inner.y = imagePt.y + centerPt.y - IR / 2;
        outer.x = imagePt.x + centerPt.x - OR / 2;
        outer.y = imagePt.y + centerPt.y - OR / 2;
        border  = new RoundRectangle2D.Double(0d, 0d, width, height, 10d, 10d);
    }
    public void paint(Graphics g, ImageObserver ior) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform at = AffineTransform.getTranslateInstance(imagePt.x, imagePt.y);
        at.rotate(radian, centerPt.x, centerPt.y);

        g2.setPaint(BORDER_COLOR);
        g2.setStroke(BORDER_STROKE);
        Shape s = new Rectangle2D.Double(border.x - 2, border.y - 2, border.width + 4, border.height + 20);
        g2.fill(at.createTransformedShape(s));
        g2.draw(at.createTransformedShape(s));

        g2.drawImage(image, at, ior);
        if (rotatorHover) {
            Area donut = new Area(outer);
            donut.subtract(new Area(inner));
            g2.setPaint(HOVER_COLOR);
            g2.fill(donut);
        } else if (moverHover) {
            g2.setPaint(HOVER_COLOR);
            g2.fill(inner);
        }
        g2.setStroke(BORDER_STROKE);
        g2.setPaint(BORDER_COLOR);
        g2.draw(at.createTransformedShape(border));
        g2.dispose();
    }
    @Override public void mouseMoved(MouseEvent e) {
        if (outer.contains(e.getX(), e.getY()) && !inner.contains(e.getX(), e.getY())) {
            moverHover = false;
            rotatorHover = true;
        } else if (inner.contains(e.getX(), e.getY())) {
            moverHover = true;
            rotatorHover = false;
        } else {
            moverHover = false;
            rotatorHover = false;
        }
        e.getComponent().repaint();
    }
    @Override public void mouseReleased(MouseEvent e) {
        rotatorHover = false;
        moverHover = false;
        e.getComponent().repaint();
    }
    @Override public void mousePressed(MouseEvent e) {
        if (outer.contains(e.getX(), e.getY()) && !inner.contains(e.getX(), e.getY())) {
            rotatorHover = true;
            startRadian = radian - Math.atan2(e.getY() - imagePt.y - centerPt.y, e.getX() - imagePt.x - centerPt.x);
            e.getComponent().repaint();
        } else if (inner.contains(e.getX(), e.getY())) {
            moverHover = true;
            startPt.x = e.getX();
            startPt.y = e.getY();
            e.getComponent().repaint();
        }
    }
    @Override public void mouseDragged(MouseEvent e) {
        if (rotatorHover) {
            radian = startRadian + Math.atan2(e.getY() - imagePt.y - centerPt.y, e.getX() - imagePt.x - centerPt.x);
            e.getComponent().repaint();
        } else if (moverHover) {
            imagePt.x += e.getX() - startPt.x;
            imagePt.y += e.getY() - startPt.y;
            inner.x = imagePt.x + centerPt.x - IR / 2;
            inner.y = imagePt.y + centerPt.y - IR / 2;
            outer.x = imagePt.x + centerPt.x - OR / 2;
            outer.y = imagePt.y + centerPt.y - OR / 2;
            startPt.x = e.getX();
            startPt.y = e.getY();
            e.getComponent().repaint();
        }
    }
}
