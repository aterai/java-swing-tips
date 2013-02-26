package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;

public class MainPanel extends JPanel{
    private static int[] makeGradientPallet() {
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        return pallet;
    }
    private static Color getColorFromPallet(int[] pallet, float x) {
//         if(x < 0.0 || x > 1.0) {
//             throw new IllegalArgumentException("Parameter outside of expected range");
//         }
        int i = (int)(pallet.length * x);
        int max = pallet.length-1;
        int index = i<0?0:i>max?max:i;
        int pix = pallet[index] & 0x00ffffff | (0x64 << 24);
        return new Color(pix, true);
    }
    private static TexturePaint makeCheckerTexture() {
        int cs = 6;
        int sz = cs*cs;
        BufferedImage img = new BufferedImage(sz,sz,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(new Color(200,150,100,50));
        g2.fillRect(0,0,sz,sz);
        for(int i=0;i*cs<sz;i++) {
            for(int j=0;j*cs<sz;j++) {
                if((i+j)%2==0) g2.fillRect(i*cs, j*cs, cs, cs);
            }
        }
        g2.dispose();
        return new TexturePaint(img, new Rectangle(0,0,sz,sz));
    }
    private JSlider makeSlider() {
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
    
    public MainPanel() {
        super(new BorderLayout());
        UIManager.put("Slider.horizontalThumbIcon", new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {}
            @Override public int getIconWidth()  { return 15; }
            @Override public int getIconHeight() { return 64; }
        });
        System.out.println(UIManager.get("Slider.trackWidth"));
        System.out.println(UIManager.get("Slider.majorTickLength"));
        System.out.println(UIManager.getInt("Slider.trackWidth"));
        System.out.println(UIManager.getInt("Slider.majorTickLength"));
        UIManager.put("Slider.trackWidth", 64);
        UIManager.put("Slider.majorTickLength", 6);

        JSlider slider = makeSlider();
        slider.setUI(new MetalSliderUI() {
            int[] pallet = makeGradientPallet();
            @Override public void paintTrack(Graphics g) {
                //Color trackColor = !slider.isEnabled() ? MetalLookAndFeel.getControlShadow() : slider.getForeground();
                boolean leftToRight     = true; //MetalUtils.isLeftToRight(slider);
                Color controlDarkShadow = new Color(100,100,100); //MetalLookAndFeel.getControlDarkShadow();
                Color controlHighlight  = new Color(200,255,200); //MetalLookAndFeel.getControlHighlight();
                Color controlShadow     = new Color(0,100,0); //MetalLookAndFeel.getControlShadow();

                g.translate(trackRect.x, trackRect.y);

                int trackLeft = 0;
                int trackTop = 0;
                int trackRight = 0;
                int trackBottom = 0;

                // Draw the track
                if(slider.getOrientation() == JSlider.HORIZONTAL) {
                    trackBottom = trackRect.height - 1 - getThumbOverhang();
                    trackTop = trackBottom - getTrackWidth() + 1;
                    trackRight = trackRect.width - 1;
                }else{
                    if(leftToRight) {
                        trackLeft = trackRect.width - getThumbOverhang() - getTrackWidth();
                        trackRight = trackRect.width - getThumbOverhang() - 1;
                    }else{
                        trackLeft = getThumbOverhang();
                        trackRight = getThumbOverhang() + getTrackWidth() - 1;
                    }
                    trackBottom = trackRect.height - 1;
                }

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

                // Draw the fill
                int middleOfThumb = 0;
                int fillTop = 0;
                int fillLeft = 0;
                int fillBottom = 0;
                int fillRight = 0;

                if(slider.getOrientation() == JSlider.HORIZONTAL) {
                    middleOfThumb = thumbRect.x + thumbRect.width / 2;
                    middleOfThumb -= trackRect.x; // To compensate for the g.translate()
                    fillTop = !slider.isEnabled() ? trackTop : trackTop + 1;
                    fillBottom = !slider.isEnabled() ? trackBottom - 1 : trackBottom - 2;

                    if(!drawInverted()) {
                        fillLeft = !slider.isEnabled() ? trackLeft : trackLeft + 1;
                        fillRight = middleOfThumb;
                    }else{
                        fillLeft = middleOfThumb;
                        fillRight = !slider.isEnabled() ? trackRight - 1 : trackRight - 2;
                    }
                }else{
                    middleOfThumb = thumbRect.y + thumbRect.height / 2;
                    middleOfThumb -= trackRect.y; // To compensate for the g.translate()
                    fillLeft = !slider.isEnabled() ? trackLeft : trackLeft + 1;
                    fillRight = !slider.isEnabled() ? trackRight - 1 : trackRight - 2;

                    if(!drawInverted()) {
                        fillTop = middleOfThumb;
                        fillBottom = !slider.isEnabled() ? trackBottom - 1 : trackBottom - 2;
                    }else{
                        fillTop = !slider.isEnabled() ? trackTop : trackTop + 1;
                        fillBottom = middleOfThumb;
                    }
                }

                if(slider.isEnabled()) {
                    g.setColor(slider.getBackground());
                    g.drawLine(fillLeft, fillTop, fillRight, fillTop);
                    g.drawLine(fillLeft, fillTop, fillLeft, fillBottom);

                    float x = (fillRight - fillLeft) / (float)(trackRight - trackLeft);
                    g.setColor(getColorFromPallet(pallet, x));
                    g.fillRect(fillLeft + 1, fillTop + 1, fillRight - fillLeft, fillBottom - fillTop);
                }else{
                    g.setColor(controlShadow);
                    g.fillRect(fillLeft, fillTop, fillRight - fillLeft, trackBottom - trackTop);
                }

                // Draw the highlight
                int yy = trackTop + (trackBottom - trackTop) / 2;
                for(int i=10;i>=0;i--) {
                    g.setColor(new Color(1f,1f,1f,i*0.07f));
                    g.drawLine(trackLeft + 2, yy, trackRight - trackLeft - 2, yy);
                    yy--;
                }
                g.translate(-trackRect.x, -trackRect.y);
            }
        });

        JSlider slider0 = makeSlider();
        slider0.setModel(slider.getModel());

        Box box = Box.createVerticalBox();
        box.add(createPanel(slider0, "Default:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(slider, "Gradient translucent track JSlider:"));
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(box, BorderLayout.NORTH);
        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
    }
    private final TexturePaint texture = makeCheckerTexture();
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(texture);
        g2.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
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
