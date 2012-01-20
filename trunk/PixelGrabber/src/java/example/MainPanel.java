package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel p = new JPanel(cardLayout);
    public MainPanel() {
        super(new BorderLayout());

//         Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("screenshot.png"));
//         MediaTracker tr = new MediaTracker(p);
//         tr.addImage(image, 1);
//         try{
//             tr.waitForID(1);
//         } catch (InterruptedException e) {
//             e.printStackTrace();
//         }

        BufferedImage image;
        try{
            image = javax.imageio.ImageIO.read(getClass().getResource("screenshot.png"));
        }catch(java.io.IOException ioe) {
            ioe.printStackTrace();
            return;
        }

        int width  = image.getWidth(p);
        int height = image.getHeight(p);

//         BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
//         Graphics2D g2d = bi.createGraphics();
//         g2d.drawImage(image, 0, 0, null);
//         g2d.setComposite(AlphaComposite.Clear);
//         g2d.setPaint(new Color(255,255,255,0));
//         //NW
//         g2d.drawLine(0,0,4,0);
//         g2d.drawLine(0,1,2,1);
//         g2d.drawLine(0,2,1,2);
//         g2d.drawLine(0,3,0,4);
//         //NE
//         g2d.drawLine(width-5,0,width-1,0);
//         g2d.drawLine(width-3,1,width-1,1);
//         g2d.drawLine(width-2,2,width-1,2);
//         g2d.drawLine(width-1,3,width-1,4);
//         g2d.dispose();

        int[] pix  = new int[height * width];
        PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pix, 0, width);
        try{
            pg.grabPixels();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //NW
        for(int y=0;y<5;y++) {
            for(int x=0;x<5;x++) {
                if((y==0 && x<5) || (y==1 && x<3) ||
                   (y==2 && x<2) || (y==3 && x<1) ||
                   (y==4 && x<1) ) pix[y*width+x] = 0x0;
            }
        }
        //NE
        for(int y=0;y<5;y++) {
            for(int x=width-5;x<width;x++) {
                if((y==0 && x>=width-5) || (y==1 && x>=width-3) ||
                   (y==2 && x>=width-2) || (y==3 && x>=width-1) ||
                   (y==4 && x>=width-1) ) pix[y*width+x] = 0x0;
            }
        }
//         int n=0;
//         for(int y=0;y<5;y++) {
//             for(int x=0;x<width;x++) {
//                 n = y * width + x;
//                 if(x>=5 && x<width-5) continue;
//                 else if(y==0 && (x<5 || x>=width-5)) pix[n] = 0x0;
//                 else if(y==1 && (x<3 || x>=width-3)) pix[n] = 0x0;
//                 else if(y==2 && (x<2 || x>=width-2)) pix[n] = 0x0;
//                 else if(y==3 && (x<1 || x>=width-1)) pix[n] = 0x0;
//                 else if(y==4 && (x<1 || x>=width-1)) pix[n] = 0x0;
//             }
//         }
        MemoryImageSource producer = new MemoryImageSource(width, height, pix, 0, width);
        Image img = p.createImage(producer);
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = bi.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

//         try{
//             javax.imageio.ImageIO.write(
//                 bi, "png", java.io.File.createTempFile("screenshot", ".png"));
//         }catch(java.io.IOException ioe) {
//             ioe.printStackTrace();
//         }

        p.add(new JLabel(new ImageIcon(image)), "original");
        p.add(new JLabel(new ImageIcon(bi)), "rounded");
        add(new JCheckBox(new AbstractAction("transparency at the rounded windows corners") {
            @Override public void actionPerformed(ActionEvent e) {
                cardLayout.show(p, ((JCheckBox)e.getSource()).isSelected()?"rounded":"original");
            }
        }), BorderLayout.NORTH);
        add(p);
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
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
