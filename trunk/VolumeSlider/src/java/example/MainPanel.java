package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout(5,5));

        JSlider slider1 = new JSlider(0,100,0);
        slider1.setUI(new TriSliderUI());
        slider1.setMajorTickSpacing(10);
        slider1.setMinorTickSpacing(5);
        slider1.setPaintTicks(true);
        slider1.setPaintLabels(true);

        JSlider slider2 = new JSlider(0,100,0);
        slider2.setUI(new javax.swing.plaf.metal.MetalSliderUI() {
            @Override protected void paintHorizontalLabel(Graphics g, int v, Component l) {
                JLabel lbl = (JLabel)l;
                lbl.setForeground(Color.GREEN);
                super.paintHorizontalLabel(g,v,lbl);
            }
        });
        //slider2.setBackground(Color.BLACK);
        slider2.setForeground(Color.BLUE);
        slider2.setMajorTickSpacing(10);
        slider2.setMinorTickSpacing(5);
        slider2.setPaintTicks(true);
        slider2.setPaintLabels(true);

        Box box = Box.createVerticalBox();
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("TriangleSliderUI", slider1));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("HorizontalLabelColor", slider2));
        box.add(Box.createVerticalGlue());
        add(box);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 200));
    }
    private JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
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
//         try{
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         }catch(Exception e) {
//             e.printStackTrace();
//         }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TriSliderUI extends javax.swing.plaf.metal.MetalSliderUI {
    @Override public void paintThumb(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //Rectangle thumb = thumbRect;
        g2.fillOval(thumbRect.x,thumbRect.y,thumbRect.width,thumbRect.height);
    }
    @Override public void paintTrack(Graphics g) {
        int cx, cy, cw, ch;
        int pad;
        Rectangle trackBounds = trackRect;
        if(slider.getOrientation() == JSlider.HORIZONTAL ) {
            Graphics2D g2 = (Graphics2D)g;
            pad = trackBuffer;
            cx = pad;
            cy = (trackBounds.height / 2) - 2;
            cw = trackBounds.width;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(trackBounds.x, trackBounds.y + cy);

            //g2.setPaint(new GradientPaint(0, 0, getShadowColor(), cw, 0, getHighlightColor(), true));
            g2.setPaint(Color.GRAY);
            g2.fillRect(0,-cy,cw,cy*2);

            int trackLeft = 0;
            int trackRight = 0;
            //int trackTop = 0;
            //int trackBottom = 0;

            //trackBottom = (trackRect.height - 1) - getThumbOverhang();
            //trackTop    = trackBottom - (getTrackWidth() - 1);
            trackRight  = trackRect.width - 1;

            int middleOfThumb = 0;
            //int fillTop = 0;
            //int fillBottom = 0;
            int fillLeft = 0;
            int fillRight = 0;

            middleOfThumb = thumbRect.x + (thumbRect.width / 2);
            middleOfThumb -= trackRect.x; // To compensate for the g.translate()
            //fillTop = !slider.isEnabled() ? trackTop : trackTop + 1;
            //fillBottom = !slider.isEnabled() ? trackBottom - 1 : trackBottom - 2;

            if( !drawInverted() ) {
                fillLeft = !slider.isEnabled() ? trackLeft : trackLeft + 1;
                fillRight = middleOfThumb;
            }else{
                fillLeft = middleOfThumb;
                fillRight = !slider.isEnabled() ? trackRight - 1 : trackRight - 2;
            }

            g2.setPaint(new GradientPaint(0, 0, new Color(0,100,100), cw, 0, new Color(0,255,100), true));
            g2.fillRect(0,-cy,fillRight-fillLeft,cy*2);

            g2.setPaint(slider.getBackground());
            Polygon polygon = new Polygon();
            polygon.addPoint(0, cy);
            polygon.addPoint(0, -cy);
            polygon.addPoint(cw, -cy);
            g2.fillPolygon(polygon);
            polygon.reset();

            g2.setPaint(Color.WHITE);
            g2.drawLine(0, cy, cw-1, cy);

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.translate(-trackBounds.x, -(trackBounds.y + cy));
        }else{
            super.paintTrack(g);
        }
    }
}

// class TriSliderUI extends javax.swing.plaf.basic.BasicSliderUI {
//         public TriSliderUI(JSlider b) {
//             super(b);
//         }

// pulbic JSlider makeSlider() {
//   JSlider slider = new JSlider(0,100);
//   slider.setMajorTickSpacing(10);
//   slider.setMinorTickSpacing(5);
//   //slider.setPaintTicks(true);
//   slider.setPaintLabels(true);
//   Dictionary dictionary = slider.getLabelTable();
//   if (dictionary != null) {
//     Enumeration elements = dictionary.elements();
//     while (elements.hasMoreElements()) {
//       JLabel label = (JLabel) elements.nextElement();
//       label.setIcon(new TickIcon());
//       label.setIconTextGap(0);
//       label.setVerticalAlignment(SwingConstants.TOP);
//       label.setVerticalTextPosition(SwingConstants.BOTTOM);
//       label.setHorizontalAlignment(SwingConstants.CENTER);
//      label.setHorizontalTextPosition(SwingConstants.CENTER);
//       label.setForeground(Color.RED);
//     }
//   }
// }
// class TickIcon implements Icon {
//   @Override public void paintIcon(Component c,Graphics g,int x,int y) {
//     g.setColor(Color.GREEN);
//     g.drawLine( x+2, y-1, x+2, y+1 );
//     g.drawLine( x+1, y+0, x+3, y+0 );
//     g.drawLine( x+0, y+1, x+4, y+1 );
//   }
//   @Override public int getIconWidth()  { return 5; }
//   @Override public int getIconHeight() { return 3; }
// }
