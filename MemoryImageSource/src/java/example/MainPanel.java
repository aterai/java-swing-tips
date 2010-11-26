package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        add(new PaintPanel());
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class PaintPanel extends JPanel implements MouseMotionListener, MouseListener {
    private Point startPoint = new Point(-1,-1);
    private BufferedImage backImage = null;
    private TexturePaint texture  = makeTexturePaint();
    private int[] pixels = new int[320 * 240];
    private MemoryImageSource source = new MemoryImageSource(320, 240, pixels, 0, 320);
    public PaintPanel() {
        super();
        addMouseMotionListener(this);
        addMouseListener(this);
        backImage = new BufferedImage(320, 240, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = backImage.createGraphics();
        g2.setPaint(texture);
        g2.fillRect(0,0,320,240);
        g2.dispose();
    }
    private static BufferedImage makeBGImage() {
        Color color = new Color(200,150,100,50);
        int cs = 6, sz = cs*cs;
        BufferedImage img = new BufferedImage(sz,sz,BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(color);
        g2.fillRect(0,0,sz,sz);
        for(int i=0; i*cs<sz; i++) {
            for(int j=0; j*cs<sz; j++) {
                if((i+j)%2==0) g2.fillRect(i*cs, j*cs, cs, cs);
            }
        }
        g2.dispose();
        return img;
    }
    private static TexturePaint makeTexturePaint() {
        BufferedImage img = makeBGImage();
        int w = img.getWidth(), h = img.getHeight();
        Rectangle2D r2d = new Rectangle2D.Float(0,0,w,h);
        return new TexturePaint(img, r2d);
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(backImage!=null) {
            ((Graphics2D)g).drawImage(backImage, 0, 0, this);
        }
        if(source!=null) {
            g.drawImage(createImage(source), 0, 0, null);
        }
    }
    int penc = 0x0;
    @Override public void mouseDragged(MouseEvent e) {
        Point pt = e.getPoint();
        double xDelta = e.getX() - startPoint.getX();
        double yDelta = e.getY() - startPoint.getY();
        double delta = Math.max(Math.abs(xDelta), Math.abs(yDelta));

        double xIncrement = xDelta / delta;
        double yIncrement = yDelta / delta;
        double xStart = startPoint.x;
        double yStart = startPoint.y;
        for(int i=0; i<delta; i++) {
            Point p = new Point((int)xStart, (int)yStart);
            if(p.x<0 || p.y<0 || p.x>=320 || p.y>=240) break;
            pixels[p.x + p.y * 320] = penc;
            for(int n=-1;n<=1;n++) {
                for(int m=-1;m<=1;m++) {
                    int t = (p.x+n) + (p.y+m) * 320;
                    if(t>=0 && t<320*240) {
                        pixels[t] = penc;
                    }
                }
            }
            //source.newPixels(p.x-2, p.y-2, 4, 4);
            repaint(p.x-2, p.y-2, 4, 4);
            xStart += xIncrement;
            yStart += yIncrement;
        }
        startPoint = pt;
    }
    @Override public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
        penc = (e.getButton()==MouseEvent.BUTTON1)?0xff000000:0x0;
    }
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
}
