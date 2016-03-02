package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super();
        Icon i = new ImageIcon(getClass().getResource("duke.gif"));
        //Icon i = UIManager.getIcon("OptionPane.warningIcon");
        add(makeLabel("Default", i));
//*/
        add(makeLabel("Rotate: 180", new RotateIcon(i, 180)));
        add(makeLabel("Rotate:  90", new RotateIcon(i,  90)));
        add(makeLabel("Rotate: -90", new RotateIcon(i, -90)));
/*/
        setOpaque(true);
        setBackground(Color.RED);
        add(makeLabel("Rotate: 180", new QuadrantRotateIcon(i, QuadrantRotate.VERTICAL_FLIP)));
        add(makeLabel("Rotate: 90",  new QuadrantRotateIcon(i, QuadrantRotate.CLOCKWISE)));
        add(makeLabel("Rotate: -90", new QuadrantRotateIcon(i, QuadrantRotate.COUNTER_CLOCKWISE)));
//*/
        setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 32));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JLabel makeLabel(String title, Icon icon) {
        JLabel l = new JLabel(title, icon, SwingConstants.CENTER);
        l.setVerticalTextPosition(SwingConstants.BOTTOM);
        l.setHorizontalTextPosition(SwingConstants.CENTER);
        l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return l;
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

class RotateIcon implements Icon {
    private final Dimension d = new Dimension();
    private final Image image;
    private AffineTransform trans;
    protected RotateIcon(Icon icon, int rotate) {
        if (rotate % 90 != 0) {
            throw new IllegalArgumentException(rotate + ": Rotate must be (rotate % 90 == 0)");
        }
        d.setSize(icon.getIconWidth(), icon.getIconHeight());
        image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();

        int numquadrants = (rotate / 90) % 4;
        if (numquadrants == 1 || numquadrants == -3) {
            trans = AffineTransform.getTranslateInstance(d.height, 0);
            int v = d.width;
            d.width = d.height;
            d.height = v;
        } else if (numquadrants == -1 || numquadrants == 3) {
            trans = AffineTransform.getTranslateInstance(0, d.width);
            int v = d.width;
            d.width = d.height;
            d.height = v;
        } else if (Math.abs(numquadrants) == 2) {
            trans = AffineTransform.getTranslateInstance(d.width, d.height);
        } else {
            trans = AffineTransform.getTranslateInstance(0, 0);
        }
        trans.quadrantRotate(numquadrants);
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.drawImage(image, trans, c);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return d.width;
    }
    @Override public int getIconHeight() {
        return d.height;
    }
}

enum QuadrantRotate {
    CLOCKWISE(1),
    VERTICAL_FLIP(2),
    COUNTER_CLOCKWISE(-1);
    private final int numquadrants;
    QuadrantRotate(int numquadrants) {
        this.numquadrants = numquadrants;
    }
    public int getNumQuadrants() {
        return numquadrants;
    }

}
class QuadrantRotateIcon implements Icon {
    private final QuadrantRotate rotate;
    private final Icon icon;
    protected QuadrantRotateIcon(Icon icon, QuadrantRotate rotate) {
        this.icon = icon;
        this.rotate = rotate;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        switch (rotate) {
          case CLOCKWISE:
            g2.translate(h, 0);
            break;
          case VERTICAL_FLIP:
            g2.translate(w, h);
            break;
          case COUNTER_CLOCKWISE:
            g2.translate(0, w);
            break;
          default:
            throw new AssertionError("Unknown QuadrantRotateIcon");
        }
        g2.rotate(Math.toRadians(90 * rotate.getNumQuadrants()));
        icon.paintIcon(c, g2, 0, 0);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return rotate == QuadrantRotate.VERTICAL_FLIP ? icon.getIconWidth() : icon.getIconHeight();
    }
    @Override public int getIconHeight() {
        return rotate == QuadrantRotate.VERTICAL_FLIP ? icon.getIconHeight() : icon.getIconWidth();
    }
}
