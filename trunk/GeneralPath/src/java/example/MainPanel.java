package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(2,3));
        add(makeTitledPanel("GeneralPath",   new StarPanel1()));
        add(makeTitledPanel("Polygon",       new StarPanel2()));
        add(makeTitledPanel("Font(Outline)", new StarPanel3()));
        add(makeTitledPanel("Icon",          new JLabel(new StarIcon0())));
        add(makeTitledPanel("Icon(R=40)",    new JLabel(new StarIcon1())));
        add(makeTitledPanel("Icon(R=20,40)", new JLabel(new StarIcon2())));
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(c);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
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
class StarPanel1 extends JPanel{
    @Override public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        int w = getWidth();
        int h = getHeight();
        //<blockquote cite="%JAVA_HOME%/demo/jfc/Java2D/src/java2d/demos/Lines/Joins.java">
        GeneralPath p = new GeneralPath();
        p.moveTo(- w / 4.0f, - h / 12.0f);
        p.lineTo(+ w / 4.0f, - h / 12.0f);
        p.lineTo(- w / 6.0f, + h / 4.0f);
        p.lineTo(+     0.0f, - h / 4.0f);
        p.lineTo(+ w / 6.0f, + h / 4.0f);
        p.closePath();
        //</blockquote>
        g2.translate(w/2, h/2);
        g2.setColor(Color.YELLOW);
        g2.fill(p);
        g2.setColor(Color.BLACK);
        g2.draw(p);
    }
}
class StarPanel2 extends JPanel{
    @Override public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        int w = getWidth();
        int h = getHeight();
        Polygon p = new Polygon();
        p.addPoint(Math.round(- w / 4.0f), Math.round(- h / 12.0f));
        p.addPoint(Math.round(+ w / 4.0f), Math.round(- h / 12.0f));
        p.addPoint(Math.round(- w / 6.0f), Math.round(+ h / 4.0f));
        p.addPoint(Math.round(+     0.0f), Math.round(- h / 4.0f));
        p.addPoint(Math.round(+ w / 6.0f), Math.round(+ h / 4.0f));
        g2.translate(w/2, h/2);
        g2.setColor(Color.YELLOW);
        g2.fill(p);
        g2.setColor(Color.BLACK);
        g2.draw(p);
    }
}
class StarPanel3 extends JPanel{
    private static final int FONTSIZE = 80;
    private final Shape shape;
    public StarPanel3() {
        super();
        FontRenderContext frc = new FontRenderContext(null,true,true);
        Font font = new Font("serif", Font.PLAIN, FONTSIZE);
        shape = new TextLayout("\u2605", font, frc).getOutline(null);
    }
    @Override public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.translate(0,FONTSIZE);
        g2.setColor(Color.YELLOW);
        g2.fill(shape);
        g2.setColor(Color.BLACK);
        g2.draw(shape);
    }
}
// class StarPanel4 extends JPanel{
//     public void paintComponent(Graphics g) {
//         Graphics2D g2 = (Graphics2D)g;
//         int w = getWidth();
//         int h = getHeight();
//         Path2D.Double p = new Path2D.Double();
//         p.moveTo(- w / 4.0, - h / 12.0);
//         p.quadTo(+     0.0, - h / 4.0,  + w / 4.0, - h / 12.0);
//         p.quadTo(+ w / 6.0, + h / 4.0,  - w / 6.0, + h / 4.0);
//         p.quadTo(- w / 4.0, - h / 12.0, +     0.0, - h / 4.0);
//         p.quadTo(+ w / 4.0, - h / 12.0, + w / 6.0, + h / 4.0);
//         p.quadTo(- w / 6.0, + h / 4.0,  - w / 4.0, - h / 12.0);
//         p.closePath();
//         g2.translate(w/2, h/2);
//         g2.setColor(Color.YELLOW);
//         g2.fill(p);
//         g2.setColor(Color.BLACK);
//         g2.draw(p);
//     }
// }
class StarIcon0 implements Icon{
    private final GeneralPath path = new GeneralPath();
    public StarIcon0() {
        //<blockquote cite="http://gihyo.jp/dev/serial/01/javafx/0009?page=2">
        path.moveTo(50    *0.8 , 0     *0.8);
        path.lineTo(61.803*0.8 , 38.196*0.8);
        path.lineTo(100   *0.8 , 38.196*0.8);
        path.lineTo(69.098*0.8 , 61.804*0.8);
        path.lineTo(80.902*0.8 , 100   *0.8);
        path.lineTo(50    *0.8 , 76.394*0.8);
        path.lineTo(19.098*0.8 , 100   *0.8);
        path.lineTo(30.902*0.8 , 61.804*0.8);
        path.lineTo(0     *0.8 , 38.196*0.8);
        path.lineTo(38.197*0.8 , 38.196*0.8);
        path.closePath();
        //</blockquote>
    }
    @Override public int getIconWidth() {
        return 80;
    }
    @Override public int getIconHeight() {
        return 80;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(x, y);
        g2d.setPaint(Color.YELLOW);
        g2d.fill(path);
        g2d.setPaint(Color.BLACK);
        g2d.draw(path);
        g2d.translate(-x, -y);
    }
}
class StarIcon1 implements Icon{
    private static final int R = 40;
    private final AffineTransform at;
    private final Shape star;
      public StarIcon1() {
        double agl = 0.0;
        double add = 2*Math.PI/5;
        Path2D.Double p = new Path2D.Double();
        p.moveTo(R*1, R*0);
        for(int i=0;i<5;i++) {
            p.lineTo(R*Math.cos(agl), R*Math.sin(agl));
            agl+=add+add;
        }
        p.closePath();
        at = AffineTransform.getRotateInstance(-Math.PI/2,R,0);
        star = new Path2D.Double(p, at);
    }
    @Override public int getIconWidth() {
        return 2*R;
    }
    @Override public int getIconHeight() {
        return 2*R;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(x, y);
        g2d.setPaint(Color.YELLOW);
        g2d.fill(star);
        g2d.setPaint(Color.BLACK);
        g2d.draw(star);
        g2d.translate(-x, -y);
    }
}
class StarIcon2 implements Icon{
    private static final int R2 = 40;
    private static final int R1 = 20;
    //private static final double R1 = R2*Math.sin(Math.PI/10.0)/Math.cos(Math.PI/5.0); //=15.0;
    private static final int VC = 5; //16;
    private final AffineTransform at;
    private final Shape star;
    public StarIcon2() {
        double agl = 0.0;
        double add = 2*Math.PI/(VC*2);
        Path2D.Double p = new Path2D.Double();
        p.moveTo(R2*1, R2*0);
        for(int i=0;i<VC*2-1;i++) {
            agl+=add;
            if(i%2==0) {
                p.lineTo(R1*Math.cos(agl), R1*Math.sin(agl));
            }else{
                p.lineTo(R2*Math.cos(agl), R2*Math.sin(agl));
            }
        }
        p.closePath();
        at = AffineTransform.getRotateInstance(-Math.PI/2,R2,0);
        star = new Path2D.Double(p, at);
    }
    @Override public int getIconWidth() {
        return 2*R2;
    }
    @Override public int getIconHeight() {
        return 2*R2;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(x, y);
        g2d.setPaint(Color.YELLOW);
        g2d.fill(star);
        g2d.setPaint(Color.BLACK);
        g2d.draw(star);
        g2d.translate(-x, -y);
    }
}

//     //Java2D Shapes project.>http://java-sl.com/shapes.html
//     protected static int[] getXCoordinates(int x, int y, int r, int innerR, int vertexCount, double startAngle) {
//         int res[]=new int[vertexCount*2];
//         double addAngle=2*Math.PI/vertexCount;
//         double angle=startAngle;
//         double innerAngle=startAngle+Math.PI/vertexCount;
//         for(int i=0; i<vertexCount; i++) {
//             res[i*2]=(int)Math.round(r*Math.cos(angle))+x;
//             angle+=addAngle;
//             res[i*2+1]=(int)Math.round(innerR*Math.cos(innerAngle))+x;
//             innerAngle+=addAngle;
//         }
//         return res;
//     }
//     protected static int[] getYCoordinates(int x, int y, int r, int innerR, int vertexCount, double startAngle) {
//         int res[]=new int[vertexCount*2];
//         double addAngle=2*Math.PI/vertexCount;
//         double angle=startAngle;
//         double innerAngle=startAngle+Math.PI/vertexCount;
//         for(int i=0; i<vertexCount; i++) {
//             res[i*2]=(int)Math.round(r*Math.sin(angle))+y;
//             angle+=addAngle;
//             res[i*2+1]=(int)Math.round(innerR*Math.sin(innerAngle))+y;
//             innerAngle+=addAngle;
//         }
//         return res;
//     }
