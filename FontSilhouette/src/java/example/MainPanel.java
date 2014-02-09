package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.io.Serializable;
import javax.swing.*;

public class MainPanel extends JPanel {
    private static final int SIZE = 50;
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, SIZE);
    private MainPanel() {
        super(new GridLayout(4,6,0,0));
        // Inspired from java - 'Fill' Unicode characters in labels - Stack Overflow
        // http://stackoverflow.com/questions/18686199/fill-unicode-characters-in-labels
        String[] pieces = {
            "\u2654", "\u2655", "\u2656", "\u2657", "\u2658", "\u2659",
            "\u265A", "\u265B", "\u265C", "\u265D", "\u265E", "\u265F",
        };
        for(int i=0;i<pieces.length;i++) {
            add(initLabel(new JLabel(pieces[i], SwingConstants.CENTER), i));
        }
        for(int i=0;i<pieces.length;i++) {
            add(initLabel(new JLabel(new SilhouetteIcon(FONT, pieces[i], SIZE)), i));
        }
        setPreferredSize(new Dimension(320, 240));
    }
    private static JLabel initLabel(JLabel l, int i) {
        l.setFont(FONT);
        l.setOpaque(true);
        boolean f = i%2==0;
        if(i<6 ? !f : f) {
            l.setForeground(Color.WHITE);
            l.setBackground(Color.BLACK);
        }else{
            l.setForeground(Color.BLACK);
            l.setBackground(Color.WHITE);
        }
        return l;
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

class SilhouetteIcon implements Icon, Serializable {
    private static final Color PIECE_PAINT = new Color(150,100,20);
    private final Font font;
    private final String str;
    private final int size;
    public SilhouetteIcon(Font font, String str, int size) {
        this.font = font;
        this.str = str;
        this.size = size;
    }
    private static Area getOuterShape(Shape shape) {
        Area area = new Area();
        double[] coords = new double[6];
        PathIterator pi = shape.getPathIterator(null);
        Path2D.Double path = new Path2D.Double();
        while(!pi.isDone()) {
            int pathSegmentType = pi.currentSegment(coords);
            if(pathSegmentType == PathIterator.SEG_MOVETO) {
                if(area.isEmpty() || !area.contains(coords[0], coords[1])) {
                    path.moveTo(coords[0], coords[1]);
                }
            }else if(path.getCurrentPoint()==null) {
                pi.next();
                continue;
            }else{
                addPathSegment2Area(pathSegmentType, area, coords, path);
            }
            pi.next();
        }
        return area;
    }
    private static void addPathSegment2Area(int pathSegmentType, Area area, double[] coords, Path2D.Double path) {
        switch(pathSegmentType) {
          case PathIterator.SEG_MOVETO:
            path.moveTo(coords[0], coords[1]);
            break;
          case PathIterator.SEG_LINETO:
            path.lineTo(coords[0], coords[1]);
            break;
          case PathIterator.SEG_QUADTO:
            path.quadTo(coords[0], coords[1], coords[2], coords[3]);
            break;
          case PathIterator.SEG_CUBICTO:
            path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
            break;
          case PathIterator.SEG_CLOSE:
            path.closePath();
            area.add(new Area(path));
            path.reset();
            break;
          default:
            System.err.println("Unexpected value! " + pathSegmentType);
        }
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.translate(x, y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontRenderContext frc = g2.getFontRenderContext();
        Shape shape = font.createGlyphVector(frc, str).getOutline();
        Rectangle r = shape.getBounds();
        int sx = getIconWidth()  - r.width;
        int sy = getIconHeight() - r.height;
        AffineTransform at = AffineTransform.getTranslateInstance(-r.x + sx / 2, -r.y + sy / 2);
        Shape shapeCentered = at.createTransformedShape(shape);

        Shape silhouette = getOuterShape(shapeCentered);
        g2.setStroke(new BasicStroke(3));
        g2.setPaint(c.getForeground());
        g2.draw(silhouette);
        g2.setPaint(PIECE_PAINT);
        g2.fill(silhouette);

        g2.setStroke(new BasicStroke(1));
        g2.setPaint(c.getBackground());
        g2.fill(shapeCentered);
        //g2.setPaint(PIECE_PAINT.brighter());
        //g2.draw(shapeCentered);
        g2.dispose();
    }
    @Override public int getIconWidth()  { return size; }
    @Override public int getIconHeight() { return size; }
}
