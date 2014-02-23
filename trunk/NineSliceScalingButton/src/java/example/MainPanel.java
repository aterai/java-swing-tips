package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import javax.imageio.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());

        BufferedImage bi = null;
        try {
            //symbol_scale_2.jpg: Real World Illustrator: Understanding 9-Slice Scaling
            //http://rwillustrator.blogspot.jp/2007/04/understanding-9-slice-scaling.html
            bi = ImageIO.read(getClass().getResource("symbol_scale_2.jpg"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        JButton b1 = new ScalingButton("Scaling", bi);
        JButton b2 = new NineSliceScalingButton("9-Slice Scaling", bi);

        JPanel p1 = new JPanel(new GridLayout(1, 2, 5, 5));
        p1.add(b1);
        p1.add(b2);

        try {
            bi = ImageIO.read(getClass().getResource("blue.png"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        JButton b3 = new JButton("Scaling Icon", new NineSliceScalingIcon(bi, 0, 0, 0, 0));
        b3.setContentAreaFilled(false);
        b3.setBorder(BorderFactory.createEmptyBorder());
        b3.setForeground(Color.WHITE);
        b3.setHorizontalTextPosition(SwingConstants.CENTER);
        b3.setPressedIcon(new NineSliceScalingIcon(makeFilteredImage(bi, new PressedImageFilter()), 0, 0, 0, 0));
        b3.setRolloverIcon(new NineSliceScalingIcon(makeFilteredImage(bi, new RolloverImageFilter()), 0, 0, 0, 0));

        JButton b4 = new JButton("9-Slice Scaling Icon", new NineSliceScalingIcon(bi, 8, 8, 8, 8));
        b4.setContentAreaFilled(false);
        b4.setBorder(BorderFactory.createEmptyBorder());
        b4.setForeground(Color.WHITE);
        b4.setHorizontalTextPosition(SwingConstants.CENTER);
        b4.setPressedIcon(new NineSliceScalingIcon(makeFilteredImage(bi, new PressedImageFilter()), 8, 8, 8, 8));
        b4.setRolloverIcon(new NineSliceScalingIcon(makeFilteredImage(bi, new RolloverImageFilter()), 8, 8, 8, 8));

        JPanel p2 = new JPanel(new GridLayout(1, 2, 5, 5));
        p2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p2.add(b3);
        p2.add(b4);

        add(p1);
        add(p2, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private BufferedImage makeFilteredImage(BufferedImage src, ImageFilter filter) {
        ImageProducer ip = src.getSource();
        Image img = createImage(new FilteredImageSource(ip, filter));
        BufferedImage bi = new BufferedImage(img.getWidth(this), img.getHeight(this), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return bi;
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

class ScalingButton extends JButton {
    private final transient BufferedImage image;

    public ScalingButton(String title, BufferedImage image) {
        super();
        this.image = image;
        setModel(new DefaultButtonModel());
        init(title, null);
        setContentAreaFilled(false);
    }
//     @Override public Dimension getPreferredSize() {
//         Insets i = getInsets();
//         return new Dimension(image.getWidth(this) + i.right + i.left, 80);
//     }
//     @Override public Dimension getMinimumSize() {
//         return getPreferredSize();
//     }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int bw = getWidth();
        int bh = getHeight();
        g2.drawImage(image, 0, 0, bw, bh, this);
        g2.dispose();
        super.paintComponent(g);
    }
}

class NineSliceScalingButton extends JButton {
    private final transient BufferedImage image;

    public NineSliceScalingButton(String title, BufferedImage image) {
        super();
        this.image = image;
        setModel(new DefaultButtonModel());
        init(title, null);
        setContentAreaFilled(false);
    }
//     @Override public Dimension getPreferredSize() {
//         Dimension dim = super.getPreferredSize();
//         return new Dimension(dim.width + a + b, dim.height + c + d);
//     }
//     @Override public Dimension getMinimumSize() {
//         return getPreferredSize();
//     }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int iw = image.getWidth(this);
        int ih = image.getHeight(this);
        int bw = getWidth();
        int bh = getHeight();

        int a = 37;
        int b = 36;
        int c = 36;
        int d = 36;

        g2.drawImage(image.getSubimage(a, c, iw - a - b, ih - c - d), a, c, bw - a - b, bh - c - d, this);

        g2.drawImage(image.getSubimage(a, 0, iw - a - b, c), a, 0, bw - a - b, c, this);
        g2.drawImage(image.getSubimage(a, ih - d, iw - a - b, d), a, bh - d, bw - a - b, d, this);
        g2.drawImage(image.getSubimage(0, c, a, ih - c - d), 0, c, a, bh - c - d, this);
        g2.drawImage(image.getSubimage(iw - b, c, b, ih - c - d), bw - b, c, b, bh - c - d, this);

        g2.drawImage(image.getSubimage(0, 0, a, c), 0, 0, this);
        g2.drawImage(image.getSubimage(iw - b, 0, b, c), bw - b, 0, this);
        g2.drawImage(image.getSubimage(0, ih - d, a, d), 0, bh - d, this);
        g2.drawImage(image.getSubimage(iw - b, ih - d, b, d), bw - b, bh - d, this);

        g2.dispose();
        super.paintComponent(g);
    }
}
class NineSliceScalingIcon implements Icon {
    private final BufferedImage image;
    private final int a, b, c, d;
    private int width, height;
    public NineSliceScalingIcon(BufferedImage image, int a, int b, int c, int d) {
        this.image = image;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
    @Override public int getIconWidth() {
        return width; // Math.max(image.getWidth(null), width);
    }
    @Override public int getIconHeight() {
        return Math.max(image.getHeight(null), height);
    }
    @Override public void paintIcon(Component cmp, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Insets i;
        if (cmp instanceof JComponent) {
            i = ((JComponent) cmp).getBorder().getBorderInsets(cmp);
        } else {
            i = new Insets(0, 0, 0, 0);
        }

        //g2.translate(x, y); //1.8.0: work fine?

        int iw = image.getWidth(cmp);
        int ih = image.getHeight(cmp);
        width  = cmp.getWidth() - i.left - i.right;
        height = cmp.getHeight() - i.top - i.bottom;

        g2.drawImage(image.getSubimage(a, c, iw - a - b, ih - c - d), a, c, width - a - b, height - c - d, cmp);

        if (a > 0 && b > 0 && c > 0 && d > 0) {
            g2.drawImage(image.getSubimage(a, 0, iw - a - b, c), a, 0, width - a - b, c, cmp);
            g2.drawImage(image.getSubimage(a, ih - d, iw - a - b, d), a, height - d, width - a - b, d, cmp);
            g2.drawImage(image.getSubimage(0, c, a, ih - c - d), 0, c, a, height - c - d, cmp);
            g2.drawImage(image.getSubimage(iw - b, c, b, ih - c - d), width - b, c, b, height - c - d, cmp);

            g2.drawImage(image.getSubimage(0, 0, a, c), 0, 0, cmp);
            g2.drawImage(image.getSubimage(iw - b, 0, b, c), width - b, 0, cmp);
            g2.drawImage(image.getSubimage(0, ih - d, a, d), 0, height - d, cmp);
            g2.drawImage(image.getSubimage(iw - b, ih - d, b, d), width - b, height - d, cmp);
        }

        g2.dispose();
    }
}
class PressedImageFilter extends RGBImageFilter {
    @Override public int filterRGB(int x, int y, int argb) {
        int r = (int) (((argb >> 16) & 0xff) * 0.6);
        int g = (int) (((argb >>  8) & 0xff) * 1.0);
        int b = (int) (((argb)       & 0xff) * 1.0);
        return (argb & 0xff000000) | (r<<16) | (g<<8) | (b);
    }
}
class RolloverImageFilter extends RGBImageFilter {
    @Override public int filterRGB(int x, int y, int argb) {
        int r = (int) (((argb >> 16) & 0xff) * 1.0);
        int g = (int) (((argb >>  8) & 0xff) * 1.5); g = Math.min(255, g);
        int b = (int) (((argb)       & 0xff) * 1.5); b = Math.min(255, b);
        return (argb & 0xff000000) | (r<<16) | (g<<8) | (b);
    }
}
