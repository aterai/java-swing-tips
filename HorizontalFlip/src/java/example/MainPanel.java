package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        Font font = new Font(Font.MONOSPACED, Font.BOLD, 200);
        AffineTransform at = AffineTransform.getScaleInstance(-1d, 1d);
        add(new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(Color.BLACK);
                FontRenderContext frc = g2.getFontRenderContext();
                Shape copyright = new TextLayout("©", font, frc).getOutline(null);
                // Rectangle r = copyright.getBounds()
                // at.translate(r.getWidth(), r.getHeight());
                // AffineTransform at = new AffineTransform(-1d, 0, 0, 1d, r.getWidth(), r.getHeight());
                Shape copyleft = at.createTransformedShape(copyright);
                Rectangle2D b = copyleft.getBounds2D();
                double cx = getWidth() / 2d - b.getCenterX();
                double cy = getHeight() / 2d - b.getCenterY();
                AffineTransform toCenterAtf = AffineTransform.getTranslateInstance(cx, cy);
                g2.fill(toCenterAtf.createTransformedShape(copyleft));
                g2.dispose();
            }
        });
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
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
