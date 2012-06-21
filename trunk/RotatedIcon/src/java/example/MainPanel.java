package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

class MainPanel extends JPanel{
    public MainPanel() {
        super();
        Icon i = new ImageIcon(getClass().getResource("duke.gif"));
        //Icon i = UIManager.getIcon("OptionPane.warningIcon");
        add(makeLabel("Default", i));
//*/
        add(makeLabel("Rotate: 180", new RotateIcon(i,180)));
        add(makeLabel("Rotate: 90",  new RotateIcon(i, 90)));
        add(makeLabel("Rotate: -90", new RotateIcon(i,-90)));
/*/
        add(makeLabel("Rotate: 180", new QuadrantRotateIcon(i, QuadrantRotate.VERTICAL_FLIP)));
        add(makeLabel("Rotate: 90",  new QuadrantRotateIcon(i, QuadrantRotate.CLOCKWISE)));
        add(makeLabel("Rotate: -90", new QuadrantRotateIcon(i, QuadrantRotate.COUNTER_CLOCKWISE)));
//*/
        setBorder(BorderFactory.createEmptyBorder(0,32,0,32));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JLabel makeLabel(String title, Icon icon) {
        JLabel l = new JLabel(title, icon, SwingConstants.CENTER);
        l.setVerticalTextPosition(SwingConstants.BOTTOM);
        l.setHorizontalTextPosition(SwingConstants.CENTER);
        l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class RotateIcon implements Icon{
    private int width, height;
    private Image image;
    private AffineTransform trans;
    public RotateIcon(Icon icon, int rotate) {
        image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();

        width  = icon.getIconWidth();
        height = icon.getIconHeight();

        //int numquadrants = rotate/90;
        //System.out.println(numquadrants);

        if((rotate+360)%360==90) {
            trans = AffineTransform.getTranslateInstance(height, 0);
            trans.rotate(Math.toRadians(90)); //== trans.quadrantRotate(numquadrants);
        }else if((rotate+360)%360==270) {
            trans = AffineTransform.getTranslateInstance(0, width);
            trans.rotate(Math.toRadians(270)); //== trans.quadrantRotate(numquadrants);
        }else if((rotate+360)%360==180) {
            //trans = AffineTransform.getTranslateInstance(width, height);
            //trans.rotate(Math.toRadians(180)); //== trans.quadrantRotate(numquadrants);
            trans = AffineTransform.getScaleInstance(1.0, -1.0);
            trans.translate(0, -height);
            width  = icon.getIconHeight();
            height = icon.getIconWidth();
        }else{
            throw new IllegalArgumentException("Rotate must be (rotate % 90 == 0)");
        }
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.translate(x, y);
        g2.drawImage(image, trans, c);
        g2.translate(-x, -y);
        g2.dispose();
    }
    @Override public int getIconWidth()  {
        return height;
    }
    @Override public int getIconHeight() {
        return width;
    }
}

enum QuadrantRotate {
    CLOCKWISE(90),
    VERTICAL_FLIP(180),
    COUNTER_CLOCKWISE(-90);
    private final int quadrants;
    private QuadrantRotate(int quadrants) {
        this.quadrants = quadrants;
    }
    public int getQuadrants() {
        return quadrants;
    }
}
class QuadrantRotateIcon implements Icon{
    private Image image;
    private AffineTransform trans;
    private final QuadrantRotate rotate;
    private final Icon icon;
    public QuadrantRotateIcon(Icon icon, QuadrantRotate rotate) {
        this.icon = icon;
        this.rotate = rotate;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        Graphics2D g2 = (Graphics2D)g.create();
        switch(rotate) {
          case CLOCKWISE:         g2.translate(x+h, y  ); break;
          case VERTICAL_FLIP:     g2.translate(x+w, x+h); break;
          case COUNTER_CLOCKWISE: g2.translate(x,   y+w); break;
        }
        g2.rotate(Math.toRadians(rotate.getQuadrants()));
        icon.paintIcon(c, g2, 0, 0);
        g2.dispose();
    }
    @Override public int getIconWidth()  {
        return rotate==QuadrantRotate.VERTICAL_FLIP ? icon.getIconWidth() : icon.getIconHeight();
    }
    @Override public int getIconHeight() {
        return rotate==QuadrantRotate.VERTICAL_FLIP ? icon.getIconHeight() : icon.getIconWidth();
    }
}
