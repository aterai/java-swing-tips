package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        add(new FontRotateAnimation("A"));
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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class FontRotateAnimation extends JComponent {
    private int rotate;
    private final Shape shape;
    private Shape s;
    private final Timer animator = new Timer(10, new ActionListener() {
        @Override public void actionPerformed(ActionEvent e) {
            repaint(s.getBounds());
            Rectangle2D b = shape.getBounds();
            AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(rotate), b.getCenterX(), b.getCenterY());
            AffineTransform toCenterAT = AffineTransform.getTranslateInstance(getWidth() / 2d - b.getCenterX(), getHeight() / 2d - b.getCenterY());

            Shape s1 = at.createTransformedShape(shape);
            s = toCenterAT.createTransformedShape(s1);
            repaint(s.getBounds());
            //rotate = rotate >= 360 ? 0 : rotate + 2;
            rotate = (rotate + 2) % 360;
        }
    });
    protected FontRotateAnimation(String str) {
        super();
        addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable()) {
                    animator.stop();
                }
            }
        });
        Font font = new Font(Font.SERIF, Font.PLAIN, 200);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        shape = new TextLayout(str, font, frc).getOutline(null);
        s = shape;
        animator.start();
    }
    @Override public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        //g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.BLACK);
        g2.fill(s);
        g2.dispose();
    }
}
