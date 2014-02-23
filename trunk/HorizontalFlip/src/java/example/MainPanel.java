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
        super(new BorderLayout());
        Font font = new Font(Font.MONOSPACED, Font.BOLD, 200);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        Shape copyright = new TextLayout("\u00a9", font, frc).getOutline(null);
        AffineTransform at = AffineTransform.getScaleInstance(-1.0, 1.0);
        //Rectangle r = copyright.getBounds()
        //at.translate(r.getWidth(), r.getHeight());
        //AffineTransform at = new AffineTransform(-1d, 0, 0, 1d, r.getWidth(), r.getHeight());
        final Shape copyleft = at.createTransformedShape(copyright);
        add(new JComponent() {
            @Override public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(Color.BLACK);
                Rectangle2D b = copyleft.getBounds();
                Point2D.Double p = new Point2D.Double(b.getX() + b.getWidth() / 2d, b.getY() + b.getHeight() / 2d);
                AffineTransform toCenterAT = AffineTransform.getTranslateInstance(getWidth() / 2d - p.getX(), getHeight() / 2d - p.getY());
                g2.fill(toCenterAT.createTransformedShape(copyleft));
                g2.dispose();
            }
        });
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
