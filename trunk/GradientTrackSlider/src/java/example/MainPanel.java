package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.plaf.metal.MetalSliderUI;

public class MainPanel extends JPanel {
    private static final TexturePaint TEXTURE = TextureFactory.createCheckerTexture(6, new Color(200,150,100,50));

    public MainPanel() {
        super(new BorderLayout());
        UIManager.put("Slider.horizontalThumbIcon", new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* Empty icon */ }
            @Override public int getIconWidth()  { return 15; }
            @Override public int getIconHeight() { return 64; }
        });
        System.out.println(UIManager.get("Slider.trackWidth"));
        System.out.println(UIManager.get("Slider.majorTickLength"));
        System.out.println(UIManager.getInt("Slider.trackWidth"));
        System.out.println(UIManager.getInt("Slider.majorTickLength"));
        UIManager.put("Slider.trackWidth", 64);
        UIManager.put("Slider.majorTickLength", 6);

        JSlider slider0 = makeSlider();
        JSlider slider1 = makeSlider();
        slider1.setUI(new GradientPalletSliderUI());
        slider1.setModel(slider0.getModel());

        Box box = Box.createVerticalBox();
        box.add(createPanel(slider0, "Default:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(slider1, "Gradient translucent track JSlider:"));
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(box, BorderLayout.NORTH);
        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setPaint(TEXTURE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
    private static JSlider makeSlider() {
        JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50);
        slider.setBackground(Color.GRAY);
        slider.setOpaque(false);
        slider.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                ((JComponent)e.getSource()).repaint();
            }
        });
        slider.addMouseWheelListener(new MouseWheelListener() {
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                JSlider source = (JSlider)e.getSource();
                int intValue = (int)source.getValue()-e.getWheelRotation();
                BoundedRangeModel model = source.getModel();
                if(model.getMaximum()>=intValue && model.getMinimum()<=intValue) {
                    source.setValue(intValue);
                }
            }
        });
        return slider;
    }
    private static JComponent createPanel(JComponent cmp, String str) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(str));
        panel.add(cmp);
        panel.setOpaque(false);
        return panel;
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

class GradientPalletSliderUI extends MetalSliderUI {
    private static final int[] GRADIENT_PALLET = GradientPalletFactory.makeGradientPallet();
    protected Color controlDarkShadow = new Color(100, 100, 100); //MetalLookAndFeel.getControlDarkShadow();
    protected Color controlHighlight  = new Color(200, 255, 200); //MetalLookAndFeel.getControlHighlight();
    protected Color controlShadow     = new Color(0, 100, 0); //MetalLookAndFeel.getControlShadow();
    @Override public void paintTrack(Graphics g) {
        //Color trackColor = !slider.isEnabled() ? MetalLookAndFeel.getControlShadow() : slider.getForeground();
        //boolean leftToRight = MetalUtils.isLeftToRight(slider);

        g.translate(trackRect.x, trackRect.y);

        int trackLeft = 0;
        int trackTop = 0;
        int trackRight = 0;
        int trackBottom = 0;
        if(slider.getOrientation() == JSlider.HORIZONTAL) {
            trackBottom = trackRect.height - 1 - getThumbOverhang();
            trackTop = trackBottom - getTrackWidth() + 1;
            trackRight = trackRect.width - 1;
        }else{
            //if(leftToRight) {
                trackLeft = trackRect.width - getThumbOverhang() - getTrackWidth();
                trackRight = trackRect.width - getThumbOverhang() - 1;
            //}else{
            //    trackLeft = getThumbOverhang();
            //    trackRight = getThumbOverhang() + getTrackWidth() - 1;
            //}
            trackBottom = trackRect.height - 1;
        }

        // Draw the track
        paintTrackBase(g, trackTop, trackLeft, trackBottom, trackRight);

        // Draw the fill
        paintTrackFill(g, trackTop, trackLeft, trackBottom, trackRight);

        // Draw the highlight
        paintTrackHighlight(g, trackTop, trackLeft, trackBottom, trackRight);

        g.translate(-trackRect.x, -trackRect.y);
    }

    protected void paintTrackBase(Graphics g, int trackTop, int trackLeft, int trackBottom, int trackRight) {
        if(slider.isEnabled()) {
            g.setColor(controlDarkShadow);
            g.drawRect(trackLeft, trackTop, trackRight - trackLeft - 1, trackBottom - trackTop - 1);

            g.setColor(controlHighlight);
            g.drawLine(trackLeft + 1, trackBottom, trackRight, trackBottom);
            g.drawLine(trackRight, trackTop + 1, trackRight, trackBottom);

            g.setColor(controlShadow);
            g.drawLine(trackLeft + 1, trackTop + 1, trackRight - 2, trackTop + 1);
            g.drawLine(trackLeft + 1, trackTop + 1, trackLeft + 1, trackBottom - 2);
        }else{
            g.setColor(controlShadow);
            g.drawRect(trackLeft, trackTop, trackRight - trackLeft - 1, trackBottom - trackTop - 1);
        }
    }

    protected void paintTrackFill(Graphics g, int trackTop, int trackLeft, int trackBottom, int trackRight) {
        int middleOfThumb = 0;
        int fillTop    = 0;
        int fillLeft   = 0;
        int fillBottom = 0;
        int fillRight  = 0;

        if(slider.getOrientation() == JSlider.HORIZONTAL) {
            middleOfThumb = thumbRect.x + thumbRect.width / 2;
            middleOfThumb -= trackRect.x; // To compensate for the g.translate()
            fillTop    = trackTop + 1;
            fillBottom = trackBottom - 2;
            fillLeft   = trackLeft + 1;
            fillRight  = middleOfThumb - 2;
        }else{
            middleOfThumb = thumbRect.y + thumbRect.height / 2;
            middleOfThumb -= trackRect.y; // To compensate for the g.translate()
            fillLeft   = trackLeft;
            fillRight  = trackRight - 1;
            fillTop    = middleOfThumb;
            fillBottom = trackBottom - 1;
        }

//         if(slider.getOrientation() == JSlider.HORIZONTAL) {
//             middleOfThumb = thumbRect.x + thumbRect.width / 2;
//             middleOfThumb -= trackRect.x; // To compensate for the g.translate()
//             fillTop = slider.isEnabled() ? trackTop + 1 : trackTop;
//             fillBottom = slider.isEnabled() ? trackBottom - 2 : trackBottom - 1;
//
//             if(drawInverted()) {
//                 fillLeft = middleOfThumb;
//                 fillRight = slider.isEnabled() ? trackRight - 2 : trackRight - 1;
//             }else{
//                 fillLeft = slider.isEnabled() ? trackLeft +1 : trackLeft;
//                 fillRight = middleOfThumb;
//             }
//         }else{
//             middleOfThumb = thumbRect.y + thumbRect.height / 2;
//             middleOfThumb -= trackRect.y; // To compensate for the g.translate()
//             fillLeft = slider.isEnabled() ? trackLeft + 1 : trackLeft;
//             fillRight = slider.isEnabled() ? trackRight - 2 : trackRight - 1;
//
//             if(drawInverted()) {
//                 fillTop = slider.isEnabled() ? trackTop  + 1 : trackTop;
//                 fillBottom = middleOfThumb;
//             }else{
//                 fillTop = middleOfThumb;
//                 fillBottom = slider.isEnabled() ? trackBottom - 2 : trackBottom - 1;
//             }
//         }

        if(slider.isEnabled()) {
//             g.setColor(slider.getBackground());
//             g.drawLine(fillLeft, fillTop, fillRight, fillTop);
//             g.drawLine(fillLeft, fillTop, fillLeft, fillBottom);

            float x = (fillRight - fillLeft) / (float)(trackRight - trackLeft);
            g.setColor(GradientPalletFactory.getColorFromPallet(GRADIENT_PALLET, x));
            g.fillRect(fillLeft + 1, fillTop + 1, fillRight - fillLeft, fillBottom - fillTop);
        }else{
            g.setColor(controlShadow);
            g.fillRect(fillLeft, fillTop, fillRight - fillLeft, trackBottom - trackTop);
        }
    }

    protected void paintTrackHighlight(Graphics g, int trackTop, int trackLeft, int trackBottom, int trackRight) {
        int yy = trackTop + (trackBottom - trackTop) / 2;
        for(int i=10;i>=0;i--) {
            g.setColor(new Color(1f, 1f, 1f, i*0.07f));
            g.drawLine(trackLeft + 2, yy, trackRight - trackLeft - 2, yy);
            yy--;
        }
    }
}

class GradientPalletFactory {
    private GradientPalletFactory() { /* Singleton */ }
    public static int[] makeGradientPallet() {
        BufferedImage image = new BufferedImage(100, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2  = image.createGraphics();
        Point2D start  = new Point2D.Float(0f, 0f);
        Point2D end    = new Point2D.Float(99f, 0f);
        float[] dist   = {0.0f, 0.5f, 1.0f};
        Color[] colors = { Color.RED, Color.YELLOW, Color.GREEN };
        g2.setPaint(new LinearGradientPaint(start, end, dist, colors));
        g2.fillRect(0, 0, 100, 1);
        g2.dispose();

        int width  = image.getWidth(null);
        int[] pallet = new int[width];
        PixelGrabber pg = new PixelGrabber(image, 0, 0, width, 1, pallet, 0, width);
        try{
            pg.grabPixels();
        }catch(InterruptedException ex) {
            ex.printStackTrace();
        }
        return pallet;
    }
    public static Color getColorFromPallet(int[] pallet, float x) {
//         if(x < 0.0 || x > 1.0) {
//             throw new IllegalArgumentException("Parameter outside of expected range");
//         }
        int i = (int)(pallet.length * x);
        int max = pallet.length-1;
        int index = i<0?0:i>max?max:i;
        int pix = pallet[index] & 0x00ffffff | (0x64 << 24);
        return new Color(pix, true);
    }
}

class TextureFactory {
    private TextureFactory() { /* Singleton */ }
    public static TexturePaint createCheckerTexture(int cs, Color color) {
        int size = cs*cs;
        BufferedImage img = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(color);
        g2.fillRect(0, 0, size, size);
        for(int i=0; i*cs < size; i++) {
            for(int j=0; j*cs < size; j++) {
                if((i+j)%2 == 0) {
                    g2.fillRect(i*cs, j*cs, cs, cs);
                }
            }
        }
        g2.dispose();
        return new TexturePaint(img, new Rectangle(0,0,size,size));
    }
}
