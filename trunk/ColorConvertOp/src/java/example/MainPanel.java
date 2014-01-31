package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

class MainPanel extends JPanel {
    private final ImageIcon orgImage;
    public MainPanel() {
        super(new GridLayout(0,1));
        orgImage = new ImageIcon(getClass().getResource("i03-10.gif"));

        JPanel p1 = new JPanel(new GridLayout(1,2));
        p1.add(makeLabel(makeGrayImageIcon_1(orgImage.getImage()), "ColorConvertOp"));
        p1.add(makeLabel(makeGrayImageIcon_2(orgImage.getImage()), "TYPE_BYTE_GRAY"));
        add(p1);
        add(makeLabel(makeGrayImageIcon_3(orgImage.getImage()), "GrayFilter.createDisabledImage"));
        JPanel p3 = new JPanel(new GridLayout(1,2));
        p3.add(makeLabel(makeGrayImageIcon_4(orgImage.getImage()), "GrayFilter(true,50)"));
        p3.add(makeLabel(makeGrayImageIcon_5(orgImage.getImage()), "GrayImageFilter"));
        add(p3);

        p1.setBackground(Color.WHITE);
        p3.setBackground(Color.WHITE);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320,240));
    }
    private JLabel makeLabel(final ImageIcon image, String str) {
        JLabel label = new JLabel(str, image, JLabel.LEFT);
        label.addMouseListener(new MouseAdapter() {
            private boolean flag;
            @Override public void mouseClicked(MouseEvent e) {
                JLabel l = (JLabel)e.getSource();
                l.setIcon(flag ? image : orgImage);
                flag ^= true;
            }
        });
        return label;
    }

    private ImageIcon makeGrayImageIcon_1(Image img) {
        BufferedImage source = new BufferedImage(img.getWidth(this), img.getHeight(this), BufferedImage.TYPE_INT_ARGB);
        Graphics g = source.createGraphics();
        g.drawImage(img, 0, 0, this);
        g.dispose();
        ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        BufferedImage destination = colorConvert.filter(source, null);
        return new ImageIcon(destination);
    }

    private ImageIcon makeGrayImageIcon_2(Image img) {
        int w = img.getWidth(this);
        int h = img.getHeight(this);
        BufferedImage destination = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = destination.createGraphics();
        ////g.setColor(Color.WHITE);
        // https://forums.oracle.com/thread/1373262 Color to Grayscale to Binary
        //g.fillRect(0, 0, w, h); // need to pre-fill(alpha?)
        g.drawImage(img, 0, 0, this);
        g.dispose();
        return new ImageIcon(destination);
    }

    private ImageIcon makeGrayImageIcon_3(Image img) {
        //GrayFilter1
        return new ImageIcon(GrayFilter.createDisabledImage(img));
    }

    private ImageIcon makeGrayImageIcon_4(Image img) {
        //GrayFilter2
        ImageProducer ip = new FilteredImageSource(img.getSource(), new GrayFilter(true,50));
        return new ImageIcon(createImage(ip));
    }

    private ImageIcon makeGrayImageIcon_5(Image img) {
        //RGBImageFilter
        ImageProducer ip = new FilteredImageSource(img.getSource(), new GrayImageFilter());
        return new ImageIcon(createImage(ip));
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

class GrayImageFilter extends RGBImageFilter {
    //public GrayImageFilter() {
    //    canFilterIndexColorModel = false;
    //}
    @Override public int filterRGB(int x, int y, int argb) {
        //int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >>  8) & 0xff;
        int b = (argb      ) & 0xff;
        int m = (2*r+4*g+b)/7; //NTSC Coefficients
        //return new Color(m,m,m,a).getRGB();
        return (argb & 0xff000000) | (m<<16) | (m<<8) | (m);
    }
}
