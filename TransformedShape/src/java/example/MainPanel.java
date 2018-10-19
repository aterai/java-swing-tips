package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
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
        // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class FontRotateAnimation extends JComponent {
    protected int rotate;
    protected Shape shape;
    private final Timer animator = new Timer(10, null);
    protected FontRotateAnimation(String str) {
        super();
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable()) {
                animator.stop();
            }
        });
        Font font = new Font(Font.SERIF, Font.PLAIN, 200);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        Shape outline = new TextLayout(str, font, frc).getOutline(null);
        shape = outline;
        animator.addActionListener(e -> {
            repaint(shape.getBounds()); // clear prev
            Rectangle2D b = outline.getBounds2D();
            AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(rotate), b.getCenterX(), b.getCenterY());
            double cx = getWidth() / 2d - b.getCenterX();
            double cy = getHeight() / 2d - b.getCenterY();
            AffineTransform toCenterAtf = AffineTransform.getTranslateInstance(cx, cy);

            Shape s1 = at.createTransformedShape(outline);
            shape = toCenterAtf.createTransformedShape(s1);
            repaint(shape.getBounds());
            // rotate = rotate >= 360 ? 0 : rotate + 2;
            rotate = (rotate + 2) % 360;
        });
        animator.start();
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.BLACK);
        g2.fill(shape);
        g2.dispose();
    }
}
