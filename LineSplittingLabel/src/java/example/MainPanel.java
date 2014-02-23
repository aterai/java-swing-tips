package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(1, 2));
        add(new LineSplittingLabel("ABC"));
        add(new TricoloreLabel("DEF"));
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
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

class TricoloreLabel extends JComponent {
    private final GlyphVector gv;
    public TricoloreLabel(String str) {
        super();
        Font font = new Font(Font.SERIF, Font.PLAIN, 64);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        gv = font.createGlyphVector(frc, str);
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Rectangle2D b = gv.getVisualBounds();
        Point2D.Double p = new Point2D.Double(b.getX() + b.getWidth() / 2d, b.getY() + b.getHeight() / 2d);
        AffineTransform toCenterAT = AffineTransform.getTranslateInstance(w / 2d - p.getX(), h / 2d - p.getY());

        double d = b.getHeight()/3;
        Rectangle2D.Double clip  = new Rectangle2D.Double(b.getX(), b.getY(), b.getWidth(), b.getHeight());
        Rectangle2D.Double clip1 = new Rectangle2D.Double(b.getX(), b.getY(), b.getWidth(), d);
        Rectangle2D.Double clip2 = new Rectangle2D.Double(b.getX(), b.getY() + 2 * d, b.getWidth(), d);

        Shape s = toCenterAT.createTransformedShape(gv.getOutline());

        g2.setClip(toCenterAT.createTransformedShape(clip1));
        g2.setPaint(Color.BLUE);
        g2.fill(s);

        g2.setClip(toCenterAT.createTransformedShape(clip2));
        g2.setPaint(Color.RED);
        g2.fill(s);

        g2.setClip(toCenterAT.createTransformedShape(clip));
        g2.setPaint(Color.BLACK);
        g2.draw(s);
        g2.dispose();
    }
}

class LineSplittingLabel extends JComponent {
    private final Shape shape;
    public LineSplittingLabel(String str) {
        super();
        Font font = new Font(Font.SERIF, Font.PLAIN, 64);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        shape = new TextLayout(str, font, frc).getOutline(null);
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Rectangle2D b = shape.getBounds();
        Point2D.Double p = new Point2D.Double(b.getX() + b.getWidth() / 2d, b.getY() + b.getHeight() / 2d);
        AffineTransform toCenterAT = AffineTransform.getTranslateInstance(w / 2d - p.getX(), h / 2d - p.getY());

        Shape s = toCenterAT.createTransformedShape(shape);
        g2.setPaint(Color.BLACK);
        g2.fill(s);
        Rectangle2D.Double clip = new Rectangle2D.Double(b.getX(), b.getY(), b.getWidth(), b.getHeight() / 2);
        g2.setClip(toCenterAT.createTransformedShape(clip));
        g2.setPaint(Color.RED);
        g2.fill(s);
        g2.dispose();
    }
}
