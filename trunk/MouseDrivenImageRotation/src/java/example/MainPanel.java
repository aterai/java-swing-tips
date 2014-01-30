package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final DraggableImageMouseListener di;
    public MainPanel() {
        super();
        di = new DraggableImageMouseListener(new ImageIcon(getClass().getResource("test.png")));
        addMouseListener(di);
        addMouseMotionListener(di);
        setPreferredSize(new Dimension(320, 240));
    }
    @Override public void paintComponent(Graphics g) {
        //super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setPaint(new GradientPaint(50, 0, new Color(200,200,200),
                                       getWidth(), getHeight(), new Color(100,100,100), true));
        g2d.fillRect(0,0,getWidth(),getHeight());
        di.paint(g, this);
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
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class DraggableImageMouseListener extends MouseAdapter {
    private static final Color color = new Color(100,255,200,100);
    private static final int ir = 40, or = ir*3;
    private static final BasicStroke borderStroke = new BasicStroke(4.0f);
    private final RoundRectangle2D.Double border;
    private final Ellipse2D.Double inner = new Ellipse2D.Double(0,0,ir,ir);
    private final Ellipse2D.Double outer = new Ellipse2D.Double(0,0,or,or);
    public final Image image;
    public final int width;
    public final int height;
    public final double centerX, centerY;
    public double x = 10.0, y = 50.0, radian = 45.0 * (Math.PI / 180.0);
    public double startX, startY, startA;
    private boolean moverHover, rotatorHover;

    public DraggableImageMouseListener(ImageIcon ii) {
        super();
        image   = ii.getImage();
        width   = ii.getIconWidth();
        height  = ii.getIconHeight();
        centerX = width/2.0;
        centerY = height/2.0;
        inner.x = x+centerX-ir/2;
        inner.y = y+centerY-ir/2;
        outer.x = x+centerX-or/2;
        outer.y = y+centerY-or/2;
        border  = new RoundRectangle2D.Double(0, 0, width, height, 10.0, 10.0);
    }
    public void paint(Graphics g, ImageObserver ior) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.rotate(radian, centerX, centerY);

        g2d.setPaint(Color.WHITE);
        g2d.setStroke(borderStroke);
        Shape s = new Rectangle2D.Double(border.x-2,border.y-2,border.width+4,border.height+20);
        g2d.fill(at.createTransformedShape(s));
        g2d.draw(at.createTransformedShape(s));

        g2d.drawImage(image, at, ior);
        if(rotatorHover) {
            Area donut = new Area(outer);
            donut.subtract(new Area(inner));
            g2d.setPaint(color);
            g2d.fill(donut);
        }else if(moverHover) {
            g2d.setPaint(color);
            g2d.fill(inner);
        }
        g2d.setStroke(borderStroke);
        g2d.setPaint(Color.WHITE);
        g2d.draw(at.createTransformedShape(border));
    }
    @Override public void mouseMoved(MouseEvent e) {
        if(outer.contains(e.getX(), e.getY()) && !inner.contains(e.getX(), e.getY())) {
            moverHover = false; rotatorHover = true;
        }else if(inner.contains(e.getX(), e.getY())) {
            moverHover = true;  rotatorHover = false;
        }else{
            moverHover = rotatorHover =false;
        }
        ((JComponent)e.getSource()).repaint();
    }
    @Override public void mouseReleased(MouseEvent e) {
        rotatorHover = moverHover = false;
        ((JComponent)e.getSource()).repaint();
    }
    @Override public void mousePressed(MouseEvent e) {
        if(outer.contains(e.getX(), e.getY()) && !inner.contains(e.getX(), e.getY())) {
            rotatorHover = true;
            startA = radian - Math.atan2(e.getY()-y-centerY, e.getX()-x-centerX);
            ((JComponent)e.getSource()).repaint();
        }else if(inner.contains(e.getX(), e.getY())) {
            moverHover = true;
            startX = e.getX();
            startY = e.getY();
            ((JComponent)e.getSource()).repaint();
        }
    }
    @Override public void mouseDragged(MouseEvent e) {
        if(rotatorHover) {
            radian = startA + Math.atan2(e.getY()-y-centerY, e.getX()-x-centerX);
            ((JComponent)e.getSource()).repaint();
        }else if(moverHover) {
            x += e.getX() - startX;
            y += e.getY() - startY;
            inner.x = x+centerX-ir/2;
            inner.y = y+centerY-ir/2;
            outer.x = x+centerX-or/2;
            outer.y = y+centerY-or/2;
            startX = e.getX();
            startY = e.getY();
            ((JComponent)e.getSource()).repaint();
        }
    }
}
