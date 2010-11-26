package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        add(new FontRotateAnimation("A"));
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class FontRotateAnimation extends JComponent implements ActionListener {
    private final javax.swing.Timer animator;
    private int rotate;
    private final Shape shape;
    private Shape s;
    public FontRotateAnimation(String str) {
        super();
        animator = new javax.swing.Timer(10, this);
        addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                JComponent c = (JComponent)e.getSource();
                if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0 &&
                   animator!=null && !c.isDisplayable()) {
                    animator.stop();
                }
            }
        });
        Font font = new Font("serif", Font.PLAIN, 200);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        shape = new TextLayout(str, font, frc).getOutline(null);
        s = shape;
        animator.start();
    }
    @Override public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        //g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.BLACK);
        g2.fill(s);
    }
    @Override public void actionPerformed(ActionEvent e) {
        repaint(s.getBounds());
        Rectangle2D b = shape.getBounds();
        Point2D.Double p = new Point2D.Double(b.getX() + b.getWidth()/2d, b.getY() + b.getHeight()/2d);
        AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(rotate), p.getX(), p.getY());
        AffineTransform toCenterAT = AffineTransform.getTranslateInstance(getWidth()/2d - p.getX(), getHeight()/2d - p.getY());

//         AffineTransform at = AffineTransform.getRotateInstance(
//             Math.toRadians(rotate),
//             b.getX() + b.getWidth()/2,
//             b.getY() + b.getHeight()/2);
//         AffineTransform toCenterAT = AffineTransform.getTranslateInstance(
//             getWidth()/2  - b.getWidth()/2  - b.getX(),
//             getHeight()/2 - b.getHeight()/2 - b.getY());

        Shape s1 = at.createTransformedShape(shape);
        s = toCenterAT.createTransformedShape(s1);
        repaint(s.getBounds());
        rotate = (rotate>=360) ? 0 : rotate+2;
    }
}
